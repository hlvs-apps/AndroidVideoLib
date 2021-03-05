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
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class Renderer extends Worker {

    static VideoProj proj;

    static ProgressRender progressRender=null;

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

    private @Nullable String identifierBitmapInVideoBitmaps(@NotNull List<VideoBitmap> videoBitmaps, Bitmap bitmap){
        for(VideoBitmap a:videoBitmaps) if (a.getBitmap() == bitmap) return a.getIdentifier();
        return null;
    }


    private synchronized Result renderSynchronus() {
        int which_renderer=-1;
        try {
            utils.LogI("Render");
            which_renderer = getInputData().getInt(VideoProj.DATA_ID_RENDERER, -1);
            //RenderTaskWrapperWithUriIdentifierPairs wrapper=proj.getRendererTimeLine().getRenderTasksWithMatchingUriIdentifierPairs(proj).get(which_renderer);
            //int from=wrapper.getFrameInProjectFrom();
            //int to=wrapper.getFrameInProjectTo();
            int from = getInputData().getInt(VideoProj.DATA_ID_RENDERER_START, -2);
            int to = getInputData().getInt(VideoProj.DATA_ID_RENDERER_END, -2);
            if (from == -2 || to == -2 || which_renderer == -1) return Result.failure();
            utils.LogD("From: " + from);
            utils.LogD("To1: " + to);
            if (to == -1) to = proj.getLength();
            utils.LogD("To2: " + to);
            proj.inputs_from_last_render[which_renderer] = new ArrayList<>();
            int actual_num_of_saved_image = 0;
            int max = to - from;
            for (int i = from; i < to; i++) {
                int actual_state = i - from;
                if (progressRender != null)
                    progressRender.updateProgressOfX(which_renderer, actual_state, max, false);
                for (RenderTaskWrapperWithUriIdentifierPairs wrapper : proj.getRenderTasksWithMatchingUriIdentifierPairs()) {
                    if (i >= wrapper.getFrameInProjectFrom() && i <= to) {
                        List<VideoBitmap> bitmap0 = new ArrayList<>();
                        List<VideoBitmap> bitmap1 = new ArrayList<>();
                        for (UriIdentifierPair p : wrapper.getMatchingUriIdentifierPairs()) {
                            String fileName = p.getUriIdentifier().getIdentifier();
                            int i_for_video = i - p.getFrameStartInProject();
                            bitmap0.add(new VideoBitmap(
                                    utils.readFromExternalStorage(proj.getContext(), fileName + i_for_video), p.getUriIdentifier()));
                            utils.LogD(fileName + i_for_video);
                            i_for_video++;
                            if ((proj.inputs_from_last_render.length == (which_renderer + 1)) ? (i + 1) < to : (i + 1) <= to) {
                                bitmap1.add(new VideoBitmap(
                                        utils.readFromExternalStorage(proj.getContext(), fileName + i_for_video), p.getUriIdentifier()));
                                utils.LogD(fileName + i_for_video);
                            } else {
                                bitmap1.add(new VideoBitmap(
                                        null, p.getUriIdentifier()
                                ));
                                utils.LogD(fileName + i_for_video + " not added because it should not exist");
                            }
                        }
                        try {
                            for (Bitmap bitmap : wrapper.getRenderTask().render(bitmap0, bitmap1, i)) {
                                try {
                                    if (bitmap != null) {
                                        String id1 = identifierBitmapInVideoBitmaps(bitmap0, bitmap);
                                        String id2 = identifierBitmapInVideoBitmaps(bitmap1, bitmap);
                                        String fileOutName;
                                        if (id1 == null && id2 == null) {
                                            fileOutName = "VIDEO_EXPORT_NAME_ExternalExportStorage_VIDEORenderer" + which_renderer + "_" + actual_num_of_saved_image;
                                            utils.LogD(fileOutName);
                                            utils.saveToExternalExportStorage(bitmap, proj.getContext(), fileOutName);
                                        } else {
                                            fileOutName = id1 == null ? id1 + i : id2 + (i + 1);
                                            utils.LogD(fileOutName);
                                        }
                                        proj.inputs_from_last_render[which_renderer].add(fileOutName);
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

            if (progressRender != null)
                progressRender.updateProgressOfX(which_renderer, 1, 1, true);
            proj.startLastRender(which_renderer);
            return Result.success();
        }catch (Exception e){
            utils.LogE(e);
            if(which_renderer!=-1) proj.workFailed(which_renderer);
            return Result.failure();
        }
    }
}
