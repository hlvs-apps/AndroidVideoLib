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
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

/**
 * <p>{@link Worker} implementation to Render your Images rendered with {@link Renderer} to a Video</p>
 * @see VideoProj#renderInTo(String, ProgressRender)
 * @author hlvs-apps
 */
public class LastRenderer extends Worker {
    private final Context context;

    public static final String fpsDataExtra="fpsDataExtra";
    private final Rational fps;

    public static final String fileNameDataExtra="fileNameDataExtra";
    private final String fileName;

    public static final String realListFileStorageDataExtra="realListFileStorageDataExtra";
    private final List<String> realList;

    public static final String VIDEO_FOLDER_NAME_DATA_EXTRA="VIDEO_FOLDER_NAME_DATA_EXTRA";
    private final String VIDEO_FOLDER_NAME;

    public static final String uriIdentifierPairListFileStorageDataExtra
            ="uriIdentifierPairListFileStorageDataExtra";
    private final List<UriIdentifierPair> uriIdentifierPairList;

    public static final String CHANNEL_ID = "CHANNEL_ID_EXPORT";
    public static final String END_OF_WAKE_LOCK_ID="::ExportLock";
    private final String WAKE_LOCK_ID;
    public static final int NOTIFICATION_ID = 1200;

    public static final String keyFailedExtraData="keyFailedExtraData";

    public LastRenderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        WAKE_LOCK_ID = utils.getApplicationName(context) + END_OF_WAKE_LOCK_ID;
        this.context=context;
        Data inputData=workerParams.getInputData();
        this.VIDEO_FOLDER_NAME=inputData.getString(VIDEO_FOLDER_NAME_DATA_EXTRA);
        this.fps=utils.unmarshal(inputData.getByteArray(fpsDataExtra),Rational.CREATOR);
        this.fileName=inputData.getString(fileNameDataExtra);
        List<String> realList;
        List<UriIdentifierPair> uriIdentifierPairList;
        try {
            realList = StringListParcelable.from(
                    new File(inputData.getString(realListFileStorageDataExtra))).getStringList();
        }catch (IOException exception){
            utils.LogE(exception);
            realList =new ArrayList<>(1);
        }
        try {
            uriIdentifierPairList = utils.getParcelableFromTempFile(
                    new File(inputData.getString(uriIdentifierPairListFileStorageDataExtra)),
                    UriIdentifierPair.UriIdentifierPairList.CREATOR).getPairs();
        } catch (IOException exception) {
            utils.LogE(exception);
            uriIdentifierPairList = new ArrayList<>(1);
        }
        this.realList = realList;
        this.uriIdentifierPairList = uriIdentifierPairList;
    }

    @NotNull
    public Result doWork() {
            try {
                return lastRender();
            } catch (Exception e) {
                utils.LogE(e);
                return Result.failure(new Data.Builder()
                        .putBoolean(keyFailedExtraData,true)
                        .putBoolean(ProgressRender.progressRenderFinished,true)
                        .putInt(ProgressRender.progressRenderState,1)
                        .putInt(ProgressRender.progressRenderMax,1)
                        .build());
            }
    }
    
    public static List<String> getRealList(List<String>[] inputs){
        List<String> result=new ArrayList<>();
        for (List<String> a : inputs) for (String s : a) if (s != null) result.add(s);
        return result;
    }
    
    private Result lastRender(){
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        final PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,WAKE_LOCK_ID);
        wakeLock.acquire(/*100*60*1000L /*100 minutes*/);

        setForegroundAsync(createForegroundInfo(0,1,true));


        SequenceEncoder enc=null;
        FileChannelWrapper ch=null;
        FileOutputStream stream1=null;
        ParcelFileDescriptor d=null;
        try {
            List<Object> re = utils.fileOutputStreamFromName(context, fileName,VIDEO_FOLDER_NAME);
            stream1 = (FileOutputStream) re.get(0);
            d= (ParcelFileDescriptor) re.get(1);
            ch = new FileChannelWrapper(stream1.getChannel());
            enc = SequenceEncoder.createWithFps(ch, fps);
            int length=realList.size();
            int i=0;
            utils.LogD("get Color");
            ColorSpace pic0=utils.getPictureFromUriIdentifierPairs(
                    uriIdentifierPairList,context).getColor();
            utils.LogD("got Color");
            for (String name : realList) {
                //Amend
                utils.LogD(String.valueOf(i));
                utils.LogD(name);
                Bitmap b=utils.readFromExternalExportStorageAndDelete(context, name);
                Picture picture=AndroidUtil.fromBitmap(b,pic0);
                utils.LogD(picture.getColor().toString());
                enc.encodeNativeFrame(picture);

                setProgress(length, i,false);

                i++;
                utils.LogD("Finished");
            }
            enc.finish();
            ch.close();
            stream1.close();
            if(d!=null)d.close();
            new Handler().postDelayed(() -> {
                wakeLock.release();
                utils.LogD("Complete Finished");
            },100);
            return Result.success(new Data.Builder()
                    .putBoolean(keyFailedExtraData,false)
                    .putBoolean(ProgressRender.progressRenderFinished,true)
                    .putInt(ProgressRender.progressRenderState,1)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .build());
        } catch (FileNotFoundException e) {
            utils.LogE(e);
            return Result.failure(new Data.Builder()
                    .putBoolean(keyFailedExtraData,true)
                    .putBoolean(ProgressRender.progressRenderFinished,true)
                    .putInt(ProgressRender.progressRenderState,1)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .build());
        } catch (IOException e) {
            utils.LogE(e);
            return Result.failure(new Data.Builder()
                    .putBoolean(keyFailedExtraData,true)
                    .putBoolean(ProgressRender.progressRenderFinished,true)
                    .putInt(ProgressRender.progressRenderState,1)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .build());
        }catch (Exception exception){
            setProgress(1,1,true);
            return Result.failure(new Data.Builder()
                    .putBoolean(keyFailedExtraData,true)
                    .putBoolean(ProgressRender.progressRenderFinished,true)
                    .putInt(ProgressRender.progressRenderState,1)
                    .putInt(ProgressRender.progressRenderMax,1)
                    .build());
        }finally {
            try {
                if(enc!=null)enc.finish();
            } catch (Exception ignored) {
            }
            try {
                if(ch!=null)ch.close();
            } catch (Exception e) {
                utils.LogE(e);
            }
            try {
                if(stream1!=null)stream1.close();
            } catch (Exception e) {
                utils.LogE(e);
            }
            try {
                if(d!=null)d.close();
            }catch (Exception e){
                utils.LogE(e);
            }
            wakeLock.release();
        }
    }

    @NonNull
    private ForegroundInfo createForegroundInfo(int progress, int max, boolean intermediate) {
        // Build a notification using bytesRead and contentLength

        Context context = getApplicationContext();
        String title = context.getString(R.string.exporting);
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
        notificationBuilder=notificationBuilder.setProgress(max, progress, intermediate)
                    .setOngoing(true)
                    // Add the cancel action to the notification which can
                    // be used to cancel the worker
                    .addAction(android.R.drawable.ic_delete, cancel, intent);


        return new ForegroundInfo(NOTIFICATION_ID,notificationBuilder.build());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        CharSequence name = context.getString(R.string.lastRenderChannelName);
        String description = context.getString(R.string.exporting);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }


    private void setProgress(int max, int progress, boolean finished){
        setForegroundAsync(createForegroundInfo(progress,max,false));
        setProgressAsync(new Data.Builder()
                .putInt(ProgressRender.progressRenderMax,max)
                .putInt(ProgressRender.progressRenderState,progress)
                .putBoolean(ProgressRender.progressRenderFinished,finished)
                .build());
    }
}
