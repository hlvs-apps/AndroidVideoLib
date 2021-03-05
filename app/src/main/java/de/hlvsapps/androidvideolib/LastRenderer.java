/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 - Copyright 2021 the authors of AndroidVideoLib                              -
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

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static de.hlvsapps.androidvideolib.VideoProj.WAKE_LOCK_ID;

public class LastRenderer extends Worker {
    static VideoProj proj;

    static ProgressRender progressRender=null;
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
        PowerManager powerManager = (PowerManager) proj.getContext().getSystemService(POWER_SERVICE);
        proj.setWakeLock(powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID));
        proj.getWakeLock().acquire(/*100*60*1000L /*100 minutes*/);
        SequenceEncoder enc=null;
        FileChannelWrapper ch=null;
        FileOutputStream stream1=null;
        ParcelFileDescriptor d=null;
        try {
            String fileName=proj.getOutputString();
            List<Object> re = utils.fileOutputStreamFromName(proj.getContext(), fileName);
            stream1 = (FileOutputStream) re.get(0);
            d= (ParcelFileDescriptor) re.get(1);
            ch = new FileChannelWrapper(stream1.getChannel());
            enc = SequenceEncoder.createWithFps(ch, proj.getFps());
            String[] reallist=  getRealList().toArray(new String[0]);
            int length=reallist.length;
            int i=0;
            for (String name : reallist) {
                //Amend
                utils.LogD(String.valueOf(i));
                utils.LogD(name);
                enc.encodeNativeFrame(AndroidUtil.fromBitmap(utils.readFromExternalExportStorageAndDelete(proj.getContext(), name), proj.getPic0().getColor()));
                proj.setNotificationProgress(length, i, false);
                /*setProgressAsync(new Data.Builder()
                        .putInt("progress",i)
                        .putInt("max", length)
                        .build());
                 */
                if(progressRender!=null)progressRender.updateProgressOfSavingVideo(i,length,false);
                i++;
                utils.LogD("Finished");
            }
            proj.setNotificationProgress(1, 1, true);
            if(progressRender!=null)progressRender.updateProgressOfSavingVideo(1,1,true);
            enc.finish();
            ch.close();
            stream1.close();
            if(d!=null)d.close();
            proj.getWakeLock().release();
            utils.LogD("Complete Finished");
            progressRender=null;
            proj=null;
            return Result.success();
        } catch (FileNotFoundException e) {
            utils.LogE(e);
            return Result.failure();
        } catch (IOException e) {
            utils.LogE(e);
            return Result.failure();
        }finally {
            if(progressRender!=null)progressRender.updateProgressOfSavingVideo(1,1,true);
            if(proj!=null)proj.setNotificationProgress(1, 1, true);
            progressRender=null;
            proj=null;
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
            if (proj != null) proj.getWakeLock().release();
        }
    }
}
