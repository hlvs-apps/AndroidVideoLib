package de.hlvsapps.androidvideolib;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.Demuxer;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.Yuv420pToRgb;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.POWER_SERVICE;
import static de.hlvsapps.androidvideolib.VideoProj.WAKE_LOCK_ID;


public class Renderer extends Worker{

     static VideoProj proj;
     static boolean allredy_running=false;

    public Renderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        //this.proj=proj;
    }


    @NotNull
    public Result doWork(){
        if(proj!=null && !allredy_running) {
            allredy_running=true;
            try {
                return renderSynchronus();
            } catch (Exception e) {
                utils.LogE(e);
                allredy_running = false;
                return Result.failure();
            }
        }else{
            return Result.failure();
        }
    }




    private synchronized Result renderSynchronus() throws IOException, JCodecException {
        FileOutputStream stream1=null;
        FileInputStream stream=null;
        ParcelFileDescriptor d=null;
        FileChannelWrapper ch=null;
        SequenceEncoder enc=null;
        setForegroundAsync(new ForegroundInfo(VideoProj.NOTIFICATION_ID,proj.builder.build()));
        //Enable Wakelook
        PowerManager powerManager = (PowerManager) proj.getContext().getSystemService(POWER_SERVICE);
        proj.setWakeLock( powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID));
        proj.getWakeLock().acquire(/*100*60*1000L /*100 minutes*/);


        try {
            InferenceImg img = new InferenceImg(proj.getContext());
            ContentResolver resolver = proj.getContext().getApplicationContext().getContentResolver();
            ch = new FileChannelWrapper((new FileInputStream(resolver.openFileDescriptor(proj.getInput(), "r").getFileDescriptor())).getChannel());
            MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
            DemuxerTrack video_track = demuxer.getVideoTrack();
            int lenght = video_track.getMeta().getTotalFrames();
            double durrition=video_track.getMeta().getTotalDuration();
            int fps = (int) (lenght / durrition);
            Yuv420pToRgb ytb = new Yuv420pToRgb();
            demuxer.close();
            d = resolver.openFileDescriptor(proj.getInput(), "r");
            stream = (new FileInputStream(d.getFileDescriptor()));
            ch = new FileChannelWrapper(stream.getChannel());
            FrameGrab grab = FrameGrab.createFrameGrab(ch);
            Bitmap bitmap0, bitmap1;
            Picture pic0, pic1;
            pic1 = grab.getNativeFrame();
            if (pic1.getColor() == ColorSpace.YUV420) {
                Picture pic3 = Picture.create(pic1.getWidth(), pic1.getHeight(), ColorSpace.RGB);
                ytb.transform(pic1, pic3);
                pic1 = pic3;
            }
            int num = 0;
            for (int i = 0; i < (lenght-1); i++) {
                pic0 = Picture.copyPicture(pic1);
                bitmap0 = AndroidUtil.toBitmap(pic0);
                utils.saveToInternalStorage(bitmap0, proj.getContext(), String.valueOf(num));
                num++;
                pic1 = grab.getNativeFrame();
                if (pic1.getColor() == ColorSpace.YUV420) {
                    Picture pic3 = Picture.create(pic1.getWidth(), pic1.getHeight(), ColorSpace.RGB);
                    ytb.transform(pic1, pic3);
                    pic1 = pic3;
                }
                bitmap1 = AndroidUtil.toBitmap(pic1);
                utils.LogD(String.valueOf(i));
                utils.LogD(pic0.getColor().toString());
                utils.LogD("Run Inferrence");
                for (Bitmap b : runInference(img, bitmap0, bitmap1, proj.getNewframes())) {
                    utils.saveToInternalStorage(b, proj.getContext(), String.valueOf(num));
                    num++;
                }
                setProgressAsync(new Data.Builder().putInt("progress",(int) (i*100/(lenght * 1.1)))
                        .putInt("max", 100)
                        .build());
                proj.setNotificationProgress((int) (lenght * 1.1), i, false);
            }

            pic0 = Picture.copyPicture(pic1);
            bitmap0 = AndroidUtil.toBitmap(pic0);

            utils.saveToInternalStorage(bitmap0, proj.getContext(), String.valueOf(num));

            //Fehlerbehebung, nach widereinklammerung entfernen!!
            ch.close();
            stream.close();
            d.close();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = "AndroidVideoLib_Export_"+formatter.format(now);
            List<Object> re=utils.fileOutputStreamFromName(proj.getContext(),fileName);
            stream1 =(FileOutputStream) re.get(0);
            d=(ParcelFileDescriptor)re.get(1);
            ch = new FileChannelWrapper(stream1.getChannel());
            enc = SequenceEncoder.createWithFps(ch, Rational.R(lenght,(int)durrition));
            //double ln = (lenght * 0.1) / num;
            for (int i = 0; i <= num; i++) {
                enc.encodeNativeFrame(AndroidUtil.fromBitmap(utils.readFromInternalStorageAndDelete(proj.getContext(), String.valueOf(i)), pic0.getColor()));
                proj.setNotificationProgress(100, (int) (90.0F+(i*10/num)), false);
                setProgressAsync(new Data.Builder()
                        .putInt("progress", (int) (90.0F+(i*10.0F/num)))
                        .putInt("max", 100)
                        .build());
            }
            proj.setNotificationProgress(1, 1, true);
            enc.finish();
            ch.close();
            stream1.close();
            if(d!=null)d.close();
            allredy_running = false;
            setProgressAsync(new Data.Builder().putInt("progress", -1).build());
            proj.getWakeLock().release();
            return Result.success();
        }finally {
            try {
                if(d!=null) d.close();
                if(ch!=null)ch.close();
                if(stream1!=null)stream1.close();
                if(stream!=null)stream.close();
                if(enc!=null) enc.finish();
            }catch (Exception e){
                utils.LogE(e);
            }
            try {
                proj.getWakeLock().release();
            }catch (Exception e){
                utils.LogE(e);
            }
        }
    }


}
