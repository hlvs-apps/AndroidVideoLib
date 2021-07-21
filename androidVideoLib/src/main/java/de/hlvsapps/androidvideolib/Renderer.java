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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

/**
 * <p>{@link Worker} implementation to Render your final Video to Images</p>
 * @see VideoProj#renderInTo(String, ProgressRender)
 * @author hlvs-apps
 */
public class Renderer extends Worker {

    static final String startOfFileName="VIDEO_EXPORT_NAME_ExternalExportStorage_VIDEORenderer";

    public static final String dataExtraImagesFromThisRendererStringListFileName="dataExtraImagesFromThisRendererStringListFileName";

    public static final String dataSUCCES ="dataSUCCES";

    public static final String countOfWorkersDataExtra ="countOfWorkersDataExtra";
    public static final String numOfThisWorkerDataExtra ="numOfThisWorkerDataExtra";
    public static final String rendererWhenStartDataExtra ="rendererWhenStartDataExtra";
    public static final String rendererWhenEndDataExtra ="rendererWhenEndDataExtra";
    public static final String projectLengthDataExtra="projectLengthDataExtra";
    public static final String renderTasksWithMatchingUriIdentifierPairsDataExtra="renderTasksWithMatchingUriIdentifierPairsDataExtra";
    public final static String pendingIntentCanonicalNameDataExtra ="pendingIntentCanonicalNameDataExtra";

    public static final int NOTIFICATION_ID =1037;//Plus num_of_workers next 1200
    public static final String CHANNEL_ID = "CHANNEL_ID_RENDER";
    public static final String END_OF_WAKE_LOCK_ID="::RenderLock";

    private final String WAKE_LOCK_ID;

    private final Context context;

    private final int which_renderer;
    private final int from;
    private final int to;
    private final int count_of_workers;
    private final int project_length;
    private final List<RenderTaskWrapperWithUriIdentifierPairs> renderTaskWrapperWithUriIdentifierPairs;
    private final PendingIntent pendingIntentForNotification;
    public Renderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WAKE_LOCK_ID = utils.getApplicationName(context) + END_OF_WAKE_LOCK_ID;
        Data inputData=workerParams.getInputData();
        String pendingIntentCanonicalName=inputData.getString(pendingIntentCanonicalNameDataExtra);
        PendingIntent pendingIntentForNotification;
        if(pendingIntentCanonicalName==null){
            pendingIntentForNotification =null;
        }else{
            try {
                Intent intent = new Intent(context, Class.forName(pendingIntentCanonicalName));
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntentForNotification = PendingIntent.getActivity(context, 0, intent, 0);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                pendingIntentForNotification =null;
            }
        }
        this.pendingIntentForNotification = pendingIntentForNotification;
        which_renderer = inputData.getInt(numOfThisWorkerDataExtra, -1);
        from = inputData.getInt(rendererWhenStartDataExtra, -2);
        to = inputData.getInt(rendererWhenEndDataExtra, -2);
        count_of_workers = inputData.getInt(countOfWorkersDataExtra,0);
        project_length = inputData.getInt(projectLengthDataExtra,-1);
        this.context=context;
        final String origin=inputData.getString(renderTasksWithMatchingUriIdentifierPairsDataExtra);
        List<RenderTaskWrapperWithUriIdentifierPairs> renderTaskWrapperWithUriIdentifierPairs1;
        if(origin==null){
            renderTaskWrapperWithUriIdentifierPairs1 =new ArrayList<>();
        }else {
            try {
                renderTaskWrapperWithUriIdentifierPairs1 = utils.getParcelableFromTempFile(new File(origin), RenderTaskWrapperWithUriIdentifierPairs.RenderTaskWrapperWithUriIdentifierPairsList.CREATOR).getPairs();
            } catch (IOException e) {
                utils.LogE(e);
                renderTaskWrapperWithUriIdentifierPairs1 = new ArrayList<>();
            }
        }
        //this.proj=proj;
        renderTaskWrapperWithUriIdentifierPairs = renderTaskWrapperWithUriIdentifierPairs1;
    }


    @NotNull
    public Result doWork() {
        //Enable Wakelook
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKE_LOCK_ID);
        wakeLock.acquire(/*100*60*1000L /*100 minutes*/);

        new Handler().post(() -> setNotificationProgress(1,0));

        try {
            return renderSynchronous();
        } catch (Exception e) {
                utils.LogE(e);
                return Result.failure((new Data.Builder())
                        .putInt(ProgressRender.progressRenderNumberOfRenderer,which_renderer==-1?0:which_renderer)
                        .putBoolean(dataSUCCES,false)
                        .build());
        }
        finally {
            wakeLock.release();
        }
    }

    private @Nullable String identifierBitmapInVideoBitmaps(@NotNull List<VideoBitmap> videoBitmaps, Bitmap bitmap){
        for(VideoBitmap a:videoBitmaps) if (a.getBitmap() == bitmap) return a.getIdentifier();
        return null;
    }


    private Result renderSynchronous() {
        try {
            utils.LogI("Render");
            int to=this.to;
            if (from == -2 || to == -2 || which_renderer == -1 || project_length == -1) {
                utils.LogW("Worker Failed Because of not Matching Values. from="+from+"; which_renderer="+which_renderer+"; project_length="+project_length);
                return Result.failure((new Data.Builder())
                        .putInt(ProgressRender.progressRenderNumberOfRenderer, which_renderer == -1 ? 0 : which_renderer)
                        .putBoolean(dataSUCCES, false)
                        .build());
            }
            utils.LogD("From: " + from);
            utils.LogD("To1: " + to);
            if (to == -1) to = project_length;
            utils.LogD("To2: " + to);
            int actual_num_of_saved_image = 0;
            int max = to - from;
            setProgressAsync((new Data.Builder())
                    .putInt(ProgressRender.progressRenderNumberOfRenderer,which_renderer)
                    .putInt(ProgressRender.progressRenderState,0)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .putBoolean(ProgressRender.progressRenderFinished,false)
                    .putInt(ProgressRender.progressRenderFunctionToCall,ProgressRender.FunctionToCall.updateProgressOfX.ordinal())
                    .putString(dataExtraImagesFromThisRendererStringListFileName,"")
                    .build()
            );
            List<String> inputs_from_this_render=new ArrayList<>(max*2);
            for (int i = from; i < to; i++) {

                if(isStopped()){
                    utils.LogI("Cancelled worker"+which_renderer+". Returning!");
                    return Result.failure();
                }

                int actual_state = i - from;
                utils.LogI(String.valueOf(actual_state));
                setProgressAsync((new Data.Builder())
                        .putInt(ProgressRender.progressRenderNumberOfRenderer,which_renderer)
                        .putInt(ProgressRender.progressRenderState,actual_state)
                        .putInt(ProgressRender.progressRenderMax,max)
                        .putBoolean(ProgressRender.progressRenderFinished,false)
                        .putInt(ProgressRender.progressRenderFunctionToCall,ProgressRender.FunctionToCall.updateProgressOfX.ordinal())
                        .putString(dataExtraImagesFromThisRendererStringListFileName,"")
                        .build()
                );
                setNotificationProgress(max,actual_state);
                for (RenderTaskWrapperWithUriIdentifierPairs wrapper : renderTaskWrapperWithUriIdentifierPairs) {
                    if(isStopped()){
                        utils.LogI("Cancelled worker"+which_renderer+". Returning!");
                        return Result.failure();
                    }
                    if (i >= wrapper.getFrameInProjectFrom()) {
                        List<VideoBitmap> bitmap0 = new ArrayList<>();
                        List<VideoBitmap> bitmap1 = new ArrayList<>();
                        for (UriIdentifierPair p : wrapper.getMatchingUriIdentifierPairs()) {
                            String fileName = p.getUriIdentifier().getIdentifier();
                            int i_for_video = i - p.getFrameStartInProject();
                            bitmap0.add(new VideoBitmap(
                                    utils.readFromExternalStorage(context, fileName + i_for_video), p.getUriIdentifier()));
                            utils.LogD("Input: "+fileName + i_for_video);
                            i_for_video++;
                            if ((count_of_workers == (which_renderer + 1)) ? (i + 1) < to : (i + 1) <= to) {
                                bitmap1.add(new VideoBitmap(
                                        utils.readFromExternalStorage(context, fileName + i_for_video), p.getUriIdentifier()));
                                //utils.LogD(fileName + i_for_video);
                            } else {
                                bitmap1.add(new VideoBitmap(
                                        null, p.getUriIdentifier()
                                ));
                                //utils.LogD(fileName + i_for_video + " not added because it should not exist");
                            }
                        }
                        if(isStopped()){
                            utils.LogI("Cancelled worker"+which_renderer+". Returning!");
                            return Result.failure();
                        }
                        try {
                            for (Bitmap bitmap : wrapper.getRenderTask().render(bitmap0, bitmap1, i)) {
                                if(isStopped()){
                                    utils.LogI("Cancelled worker"+which_renderer+". Returning!");
                                    return Result.failure();
                                }
                                try {
                                    if (bitmap != null) {
                                        String id1 = identifierBitmapInVideoBitmaps(bitmap0, bitmap);
                                        String id2 = identifierBitmapInVideoBitmaps(bitmap1, bitmap);
                                        String fileOutName;
                                        if (id1 == null && id2 == null) {
                                            fileOutName = startOfFileName+ which_renderer + "_" + actual_num_of_saved_image;
                                            utils.LogD("Output: "+fileOutName);
                                            utils.saveToExternalStorage(bitmap, context, fileOutName);
                                        } else {
                                            fileOutName = id1 != null ? id1 + i : id2 + (i + 1);
                                            utils.LogD("Output: "+fileOutName);
                                        }
                                        inputs_from_this_render.add(fileOutName);
                                        actual_num_of_saved_image++;
                                    }
                                } catch (NullPointerException ignored) {
                                }
                            }
                        } catch (NullPointerException ignored) {
                        }
                        break;
                    }
                }
            }

            return Result.success((new Data.Builder())
                    .putInt(ProgressRender.progressRenderNumberOfRenderer,which_renderer)
                    .putInt(ProgressRender.progressRenderState,1)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .putBoolean(ProgressRender.progressRenderFinished,true)
                    .putInt(ProgressRender.progressRenderFunctionToCall,ProgressRender.FunctionToCall.updateProgressOfX.ordinal())
                    .putString(dataExtraImagesFromThisRendererStringListFileName,StringListParcelable.from(inputs_from_this_render).saveToFile(context).getPath())
                    .putBoolean(dataSUCCES,true)
                    .build());
        }catch (Exception e){
            utils.LogE(e);
            return Result.failure((new Data.Builder())
                    .putInt(ProgressRender.progressRenderNumberOfRenderer,which_renderer)
                    .putBoolean(dataSUCCES,false)
                    .build());
        }
    }

    @Override
    public void onStopped() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID+which_renderer);
        super.onStopped();
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(int progress, int max) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String title = (MessageFormat.format(context.getString(R.string.rendering_on_worker_x), which_renderer));
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
                .setSmallIcon(R.drawable.ic_baseline_group_work_24)
                .setProgress(max, progress, false)
                .setOngoing(true)
                // Add the cancel action to the notification which can
                // be used to cancel the worker
                .addAction(android.R.drawable.ic_delete, cancel, intent);
        if(pendingIntentForNotification!=null){
            notificationBuilder=notificationBuilder.setContentIntent(pendingIntentForNotification);
        }

        return new ForegroundInfo(NOTIFICATION_ID+which_renderer,notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = context.getString(R.string.renderChannelName);
        String description = context.getString(R.string.importingVideos);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    private void setNotificationProgress(int max, int progress){
        setForegroundAsync(createForegroundInfo(progress,max));
    }
}
