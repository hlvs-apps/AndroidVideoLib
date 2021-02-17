/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LastRenderer extends Worker {
    static VideoProj proj;
    public LastRenderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NotNull
    public Result doWork() {
        if(proj!=null) {
            try {
                return lastRender();
            } catch (Exception e) {
                utils.LogE(e);
                return ListenableWorker.Result.failure();
            }
        }else{
            return ListenableWorker.Result.failure();
        }
    }
    
    private List<String> getRealList(){
        List<String> result=new ArrayList<>();
        for(List<String> a:proj.inputs_from_last_render){
            result.addAll(a);
        }
        return result;
    }
    
    private synchronized Result lastRender(){
        SequenceEncoder enc=null;
        FileChannelWrapper ch=null;
        FileOutputStream stream1=null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = "AndroidVideoLib_Export_" + formatter.format(now);
            List<Object> re = utils.fileOutputStreamFromName(proj.getContext(), fileName);
            stream1 = (FileOutputStream) re.get(0);
            ch = new FileChannelWrapper(stream1.getChannel());
            enc = SequenceEncoder.createWithFps(ch, proj.getFps());
            String[] reallist=  getRealList().toArray(new String[0]);
            int length=reallist.length;
            int i=0;
            for (String name : reallist) {
                //Amend
                utils.LogD(String.valueOf(i));
                enc.encodeNativeFrame(AndroidUtil.fromBitmap(utils.readFromInternalExportStorageAndDelete(proj.getContext(), name), proj.pic0.getColor()));
                proj.setNotificationProgress(length, i, false);
                setProgressAsync(new Data.Builder()
                        .putInt("progress",i)
                        .putInt("max", length)
                        .build());
                i++;
            }
            enc.finish();
            ch.close();
            stream1.close();
        } catch (FileNotFoundException e) {
            utils.LogE(e);
            return Result.failure();
        } catch (IOException e) {
            utils.LogE(e);
            return Result.failure();
        }finally {
            try {
                enc.finish();
            } catch (Exception e) {
                utils.LogE(e);
            }
            try {
                ch.close();
            } catch (Exception e) {
                utils.LogE(e);
            }
            try {
                stream1.close();
            } catch (Exception e) {
                utils.LogE(e);
            }
        }
        return Result.success();
    }
}
