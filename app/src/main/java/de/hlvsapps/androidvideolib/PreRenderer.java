/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:  https://githubcom/hlvs-apps/AndroidVideoLib/blob/master/AUTHOR.md
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

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.AndroidUtil;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.Yuv420pToRgb;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static de.hlvsapps.androidvideolib.VideoProj.WAKE_LOCK_ID;

public class PreRenderer extends Worker {
    static VideoProj proj;
    static Runnable whatDoAfter=null;

    static ProgressPreRender progressPreRender=null;

    public PreRenderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        //this.proj=proj;
    }

    @NotNull
    public ListenableWorker.Result doWork(){
        if(proj!=null) {
            try {
                return preRender();
            } catch (Exception e) {
                utils.LogE(e);
                return ListenableWorker.Result.failure();
            }
        }else{
            return ListenableWorker.Result.failure();
        }
    }

    private synchronized ListenableWorker.Result preRender() {
        //Enable Wakelook
        PowerManager powerManager = (PowerManager) proj.getContext().getSystemService(POWER_SERVICE);
        proj.setWakeLock( powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID));
        proj.getWakeLock().acquire(/*100*60*1000L /*100 minutes*/);

        utils.LogI("PreRender");
        try {
            List<UriIdentifierPair> workList = proj.getAllUriIdentifierPairsFromInput();
            int length = workList.size();
            ContentResolver resolver = proj.getContext().getApplicationContext().getContentResolver();
            int j=0;
            Yuv420pToRgb ytb = new Yuv420pToRgb();
            for (UriIdentifierPair i : workList) {
                String name = i.getUriIdentifier().getIdentifier();
                int video_length=i.getLengthInFrames();
                try(ParcelFileDescriptor pfd=resolver.openFileDescriptor(i.getUriIdentifier().getUri(), "r")) {
                    try (FileInputStream t = new FileInputStream(pfd.getFileDescriptor())) {
                        try (FileChannel c = t.getChannel()) {
                            try (FileChannelWrapper ch = new FileChannelWrapper(c)) {
                                FrameGrab grab = FrameGrab.createFrameGrab(ch);
                                int ii = 0;
                                Picture picture;
                                while (null != (picture = grab.getNativeFrame())) {
                                    if (picture.getColor() == ColorSpace.YUV420) {
                                        Picture pic3 = Picture.create(picture.getWidth(), picture.getHeight(), ColorSpace.RGB);
                                        ytb.transform(picture, pic3);
                                        picture = pic3;
                                    }
                                    Bitmap bitmap = AndroidUtil.toBitmap(picture);
                                    if(ii==0){
                                        proj.setPic0(Picture.copyPicture(picture));
                                    }
                                    if (proj.getPic0() == null) {
                                        proj.setPic0(Picture.copyPicture(picture));
                                    }
                                    utils.LogI("Save Image");
                                    utils.saveToExternalStorage(bitmap, proj.getContext(), name + ii);
                                    utils.LogD(name + ii);
                                    final int value = (int) (j * 10000 + ((ii* 1D) / video_length) * 10000);
                                    proj.setNotificationProgress(length * 10000, value, false);
                                    if(progressPreRender!=null)progressPreRender.updateProgress(value,length*10000,false);
                                    ii++;
                                }
                            } catch (IOException | JCodecException e) {
                                utils.LogE(e);
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                j++;
            }
        }catch (Exception e){
            proj.getWakeLock().release();
            proj.setNotificationProgress(1, 1, true);
            //setProgressAsync(new Data.Builder().putInt("progress", -1).build());
            if(progressPreRender!=null)progressPreRender.updateProgress(1,1,true);
            e.printStackTrace();
            throw e;
        }
        proj.setNotificationProgress(1, 1, true);
        if(progressPreRender!=null)progressPreRender.updateProgress(1,1,true);
        //setProgressAsync(new Data.Builder().putInt("progress", -1).build());
        proj.getWakeLock().release();
        if(whatDoAfter!=null)proj.getContext().runOnUiThread(whatDoAfter);
        proj=null;
        whatDoAfter=null;
        progressPreRender=null;
        return ListenableWorker.Result.success();
    }

}