/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.POWER_SERVICE;
import static de.hlvsapps.androidvideolib.VideoProj.WAKE_LOCK_ID;


public class Renderer extends Worker {

    static VideoProj proj;

    public Renderer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        //this.proj=proj;
    }


    @NotNull
    public Result doWork() {
        if (proj != null) {
            try {
                return renderSynchronus();
            } catch (Exception e) {
                utils.LogE(e);
                return Result.failure();
            }
        } else {
            return Result.failure();
        }
    }


    private synchronized Result renderSynchronus() {
        //setForegroundAsync(new ForegroundInfo(VideoProj.NOTIFICATION_ID, proj.builder.build()));
        //Enable Wakelook
        utils.LogI("Render");
        PowerManager powerManager = (PowerManager) proj.getContext().getSystemService(POWER_SERVICE);
        proj.setWakeLock(powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID));
        proj.getWakeLock().acquire(/*100*60*1000L /*100 minutes*/);
        int which_renderer=getInputData().getInt(VideoProj.DATA_ID_RENDERER,-1);
        if(which_renderer==-1) return Result.failure();
        RenderTaskWrapperWithUriIdentifierPairs wrapper=proj.getRendererTimeLine().getRenderTasksWithMatchingUriIdentifierPairs(proj).get(which_renderer);
        int from=wrapper.getFrameInProjectFrom();
        int to=wrapper.getFrameInProjectTo();
        utils.LogD(String.valueOf(to));
        if(to==-1)to=proj.getLength();
        utils.LogD(String.valueOf(to));
        proj.inputs_from_last_render[which_renderer]=new ArrayList<>();
        int actual_num_of_saved_image=0;
        for(int i=from;i<=to;i++){
            List<VideoBitmap> bitmap0=new ArrayList<>();
            List<VideoBitmap> bitmap1=new ArrayList<>();
            for(UriIdentifierPair p:wrapper.getMatchingUriIdentifierPairs()){
                String fileName=p.getUriIdentifier().getIdentifier();
                int i_for_video=i-p.getFrameStartInProject();
                bitmap0.add(new VideoBitmap(
                        utils.readFromInternalStorage(proj.getContext(),fileName+i_for_video),p.getUriIdentifier().getIdentifier()));
                i_for_video++;
                bitmap1.add(new VideoBitmap(
                        utils.readFromInternalStorage(proj.getContext(),fileName+i_for_video),p.getUriIdentifier().getIdentifier()));
                try {
                    for (Bitmap bitmap : wrapper.getRenderTask().render(bitmap0, bitmap1, i)) {
                        String fileOutName = which_renderer + "_" + actual_num_of_saved_image;
                        utils.saveToInternalExportStorage(bitmap, proj.getContext(), fileOutName);
                        proj.inputs_from_last_render[which_renderer].add(fileOutName);
                        actual_num_of_saved_image++;
                    }
                }catch (NullPointerException ignored){ ;
                }
            }
        }

        proj.getWakeLock().release();
        proj.startLastRender(which_renderer);
        return Result.success();
    }
}
