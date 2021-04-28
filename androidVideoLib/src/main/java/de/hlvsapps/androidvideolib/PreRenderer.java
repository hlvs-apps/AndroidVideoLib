/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
 - Unless otherwise noted, this is                                            -
 - Licensed under the Apache License, Version 2.0 (the "License");            -
 - you may not use this file except in compliance with the License.           -
 - You may obtain a copy of the License at                                    -
 -                                                                            -
 -     http://www.apache.org/licenses/LICENSE-2.0                             -
 -                                                                            -
 - Unless required by applicable law or agreed to in writing, software        -
 - distributed under the License is distributed on an "AS IS" BASIS,          -
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   -
 - See the License for the specific language governing permissions and        -
 - limitations under the License.                                             -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.ListenableWorker;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.apache.commons.lang3.SerializationUtils;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.Yuv420pToRgb;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static de.hlvsapps.androidvideolib.VideoProj.END_OF_WAKE_LOCK_ID;

/**
 * <p>{@link Worker} implementation to save the Pictures of the Videos contained in {@link de.hlvsapps.androidvideolib.UriIdentifierPair.UriIdentifierPairList}</p>
 * <p>Usage:</p>
 * <p><pre><code>
 *             OneTimeWorkRequest renderRequest =new OneTimeWorkRequest.Builder(PreRenderer.class)
 *                 .setConstraints(constraints)
 *                 .setInputData((new Data.Builder())
 *                         .putByteArray(PreRenderer.parcelableByteArrayListUriIdentifierPair,utils.marshall(UriIdentifierPair.UriIdentifierPairList.from(<strong>(Your {@link de.hlvsapps.androidvideolib.UriIdentifierPair.UriIdentifierPairList})</strong>>)))
 *                         .putByteArray(PreRenderer.serializableByteArrayScaleFactor, SerializationUtils.serialize(scaleFactor))
 *                         .build())
 *                 .build();
 *         WorkManager.getInstance(context.getApplicationContext())
 *                 .enqueueUniqueWork("Import"+toString(),ExistingWorkPolicy.REPLACE,renderRequest);
 * </code></pre></p>
 * <p>To get Progress Update:</p>
 * <p><pre><code>
 *                 WorkManager.getInstance(context.getApplicationContext())
 *                     .getWorkInfoByIdLiveData(renderRequest.getId())
 *                     .observeForever(new Observer<WorkInfo>() {
 *                         &#64;Override
 *                         public void onChanged(WorkInfo workInfo) {
 *                             if (workInfo != null) {
 *                                 Data progressD = workInfo.getProgress();
 *                                 int progress = progressD.getInt(ProgressPreRender.progressPreRenderState, -11);
 *                                 int max = progressD.getInt(ProgressPreRender.progressPreRenderMax, -11);
 *                                 boolean finished = progressD.getBoolean(ProgressPreRender.progressPreRenderMax, false);
 *                                 // Do something with progress, max, finished
 *                                 if (workInfo.getState().isFinished()) {
 *                                     WorkManager.getInstance(context.getApplicationContext()).getWorkInfoByIdLiveData(renderRequest.getId()).removeObserver(this);
 *                                 }
 *                             }
 *                         }
 *                     });
 * </code></pre></p>
 * @see VideoProj#preRender(Runnable, ProgressPreRender)
 * @author hlvs-apps
 */
public class PreRenderer extends Worker {
    private ExecutorPool pool;
    private final Context context;
    private final List<UriIdentifierPair> workList;
    private final BigDecimal scaleFactor;

    public static final int NOTIFICATION_ID =1036;
    public static final String CHANNEL_ID = "CHANNEL_ID_PRE_RENDER";

    private final String WAKE_LOCK_ID;

    //private NotificationManagerCompat notificationManager;
    //private NotificationCompat.Builder builder;

    final static String parcelableByteArrayListUriIdentifierPair="ParcelableByteArrayListUriIdentifierPair";
    final static String serializableByteArrayScaleFactor="serializableByteArrayScaleFactor";

    public PreRenderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context=context;
        WAKE_LOCK_ID = utils.getApplicationName(context) + END_OF_WAKE_LOCK_ID;
        Data inputData=workerParams.getInputData();
        workList = utils.unmarshal(inputData.getByteArray(parcelableByteArrayListUriIdentifierPair), UriIdentifierPair.UriIdentifierPairList.CREATOR).getPairs();
        scaleFactor= SerializationUtils.deserialize(inputData.getByteArray(serializableByteArrayScaleFactor));

        //this.proj=proj;
    }

    @NotNull
    public ListenableWorker.Result doWork(){
        try {
                return preRender();
            } catch (Exception e) {
                utils.LogE(e);
                return ListenableWorker.Result.failure();
            }
    }


    private synchronized Result preRender() {
        //Enable Wakelook
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKE_LOCK_ID);
        wakeLock.acquire(/*100*60*1000L /*100 minutes*/);


        setForegroundAsync(createForegroundInfo(0,1,true,false));

        pool=new ExecutorPool(10);

        utils.LogI("PreRender");
        try {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            int j=0;
            Yuv420pToRgb ytb = new Yuv420pToRgb();
            boolean doScale=scaleFactor.compareTo(new BigDecimal(1))!=0;
            int complete_length=0,currentEndPos=0;
            for(UriIdentifierPair i: workList){
                complete_length += i.getLengthInFrames();
            }
            for (UriIdentifierPair i : workList) {
                String name = i.getUriIdentifier().getIdentifier();
                int video_length=i.getLengthInFrames();
                try(ParcelFileDescriptor pfd=resolver.openFileDescriptor(i.getUriIdentifier().getUri(), "r")) {
                    try (FileInputStream t = new FileInputStream(pfd.getFileDescriptor())) {
                        try (FileChannel c = t.getChannel()) {
                            try (FileChannelWrapper ch = new FileChannelWrapper(c)) {
                                FrameGrab grab = FrameGrab.createFrameGrab(ch);
                                int ii = 0;
                                double before=0;
                                ArrayList<SortedPicture> pics=null;
                                for (int iji = 0; iji < video_length; iji++) {
                                    if(pics==null)pics=new ArrayList<>();
                                    PictureWithMetadata pic=grab.getNativeFrameWithMetadata();
                                    utils.LogI(String.valueOf(pic.getTimestamp()));
                                    utils.LogI(String.valueOf(pic.getDuration()));
                                    Picture picture = pic.getPicture();
                                    if (picture.getColor() == ColorSpace.YUV420) {
                                        Picture pic3 = Picture.create(picture.getWidth(), picture.getHeight(), ColorSpace.RGB);
                                        ytb.transform(picture, pic3);
                                        picture = pic3;
                                    }
                                    int angel;
                                    DemuxerTrackMeta.Orientation orientation = pic.getOrientation();
                                    switch (orientation) {
                                        case D_90:
                                            angel = 90;
                                            break;
                                        case D_180:
                                            angel = 180;
                                            break;
                                        case D_270:
                                            angel = 270;
                                            break;
                                        default:
                                            angel = 0;
                                            break;
                                    }
                                    pics.add(new SortedPicture(pic.getTimestamp(),(angel != 0) ? utils.RotateBitmap(AndroidUtil.toBitmap(picture), angel) : AndroidUtil.toBitmap(picture),pic.getDuration()));
                                    //See https://github.com/jcodec/jcodec/issues/165
                                    try {
                                        setNotificationProgress(complete_length, iji+currentEndPos, false);
                                        setProgressAsync(new Data.Builder().putInt(ProgressPreRender.progressPreRenderState,iji+currentEndPos)
                                                .putInt(ProgressPreRender.progressPreRenderMax,complete_length)
                                                .putBoolean(ProgressPreRender.progressPreRenderFinished,false)
                                                .build());
                                    }catch (Exception ignored){}
                                    if(iji!=0 && iji%10==0){
                                        utils.LogD("Start Saving with "+iji);
                                        Triple<Integer,ArrayList<SortedPicture>,Double> triple =sortImagesAndSave(pics,ii,doScale,scaleFactor,name, false,before);
                                        ii= triple.first;
                                        pics= triple.second;
                                        before=triple.third;
                                    }
                                }
                                if(pics!=null)sortImagesAndSave(pics,ii,doScale,scaleFactor,name, true,before);
                            } catch (IOException | JCodecException e) {
                                utils.LogE(e);
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    utils.LogE(e);
                } catch (IOException e) {
                    utils.LogE(e);
                }
                currentEndPos += video_length;
                j++;
            }
        }catch (Exception e){
            utils.LogE(e);
            wakeLock.release();
            try {
                setNotificationProgress(1, 1, true);
                //setProgressAsync(new Data.Builder().putInt("progress", -1).build());
                setProgressAsync(new Data.Builder().putInt(ProgressPreRender.progressPreRenderState,1)
                        .putInt(ProgressPreRender.progressPreRenderMax,1)
                        .putBoolean(ProgressPreRender.progressPreRenderFinished,true)
                        .build());
            }catch (Exception ignored){}
            throw e;
        }
        setNotificationProgress(1, 1, true);
        setProgressAsync(new Data.Builder().putInt(ProgressPreRender.progressPreRenderState,1)
                .putInt(ProgressPreRender.progressPreRenderMax,1)
                .putBoolean(ProgressPreRender.progressPreRenderFinished,true)
                .build());
        //setProgressAsync(new Data.Builder().putInt("progress", -1).build());
        wakeLock.release();
        return Result.success();
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(int progress,int max,boolean intermediate,boolean finished) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String title = context.getString(R.string.importingVideos);
        String cancel = context.getString(R.string.cancel);
        // This PendingIntent can be used to cancel the worker
        PendingIntent intent = WorkManager.getInstance(context)
                .createCancelPendingIntent(getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setChannelId(CHANNEL_ID)
                .setNotificationSilent()
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_baseline_import_export_24);
        if(!finished) {
            notificationBuilder=notificationBuilder.setProgress(max, progress, intermediate)
                    .setOngoing(true)
                    // Add the cancel action to the notification which can
                    // be used to cancel the worker
                    .addAction(android.R.drawable.ic_delete, cancel, intent);
        }else{
            notificationBuilder=notificationBuilder.setOngoing(false)
                    .setContentText(context.getString(R.string.importingComplete))
                    .setProgress(1,1,false);
        }

        return new ForegroundInfo(NOTIFICATION_ID,notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = context.getString(R.string.preRenderChannelName);
        String description = context.getString(R.string.importingVideos);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    private void setNotificationProgress(int max, int progress, boolean finsih){
        setForegroundAsync(createForegroundInfo(progress,max,false,finsih));
    }

    private void saveBitmap(Bitmap b, String fileName, boolean doScale,BigDecimal scaleFactor){
        long timeBefore=System.currentTimeMillis();
        pool.attachToExecutorOrExecuteWhenNoExecutorAvailable(() -> {
            Bitmap bitmap=b;
            if (doScale) {
                int newHeight = scaleFactor.multiply(new BigDecimal(bitmap.getHeight())).intValue();
                int newWidth = scaleFactor.multiply(new BigDecimal(bitmap.getWidth())).intValue();
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }
            utils.saveToExternalStorage(bitmap, context, fileName);
        });
        utils.LogI("Required Time for Saving Image: "+(System.currentTimeMillis()-timeBefore)+" Millis");
    }

    private Triple<Integer,ArrayList<SortedPicture>,Double> sortImagesAndSave(ArrayList<SortedPicture> pics, int ii, boolean doScale, BigDecimal scaleFactor, String name, boolean isLast, double endBefore){
        Collections.sort(pics);
        ArrayList<SortedPicture> rest=null;
        for(final SortedPicture realPic:pics) {
            if (isLast || endBefore==0 || realPic.doesTimeStampPlusDurationBeforeEqualThisTimeStamp(endBefore)) {
                endBefore= realPic.getTimestampPlusDuration();
                Bitmap bitmap = realPic.getBitmap();
                final String fileName=name + ii;
                ii++;
                saveBitmap(bitmap,fileName,doScale,scaleFactor);
            }else{
                if(rest==null)rest=new ArrayList<>();
                rest.add(realPic);
            }
        }
        return Triple.createFinalTriple(ii,rest,endBefore);
    }

    public static class ExecutorPool{
        private final int num_of_executes;
        private final Executor[] executorList;
        private final boolean[] finished;
        public ExecutorPool(int num_of_executors){
            this.num_of_executes=num_of_executors;
            executorList=new Executor[num_of_executors];
            finished=new boolean[num_of_executors];
            for(int i=0;i<num_of_executors;i++){
                final int finalI = i;
                finished[finalI]=true;
                executorList[finalI]=new Executor(() -> finished[finalI]=false, () -> finished[finalI]=true);
            }
        }
        public void attachToExecutorOrExecuteWhenNoExecutorAvailable(Runnable run){
            boolean started=false;
            for(int i=0;i<num_of_executes;i++){
                if(finished[i]) {
                    executorList[i].execute(run);
                    started=true;
                    break;
                }
            }
            if(!started)run.run();
        }
        public static class Executor implements java.util.concurrent.Executor{
            private final RunBeforeOrAfter runAfter;
            private final RunBeforeOrAfter runBefore;
            public Executor(RunBeforeOrAfter runBefore, RunBeforeOrAfter runAfter){
                this.runAfter = runAfter;
                this.runBefore=runBefore;
            }
            public Executor(RunBeforeOrAfter runAfter){
                this(null,runAfter);
            }
            public Executor(){
                this(null);
            }
            public interface RunBeforeOrAfter {
                void run();
            }
            @Override
            public void execute(Runnable runnable) {
                new Thread(() -> {
                    long timeBefore=System.currentTimeMillis();
                    if(runBefore!=null) runBefore.run();
                    runnable.run();
                    if(runAfter !=null) runAfter.run();
                    utils.LogI("Real Required Time for Operation: "+(System.currentTimeMillis()-timeBefore)+" Millis");
                }).start();
            }
        }
    }

}