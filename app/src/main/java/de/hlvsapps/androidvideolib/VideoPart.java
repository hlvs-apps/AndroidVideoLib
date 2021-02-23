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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A VideoPart contains {@link VideoSegmentWithTime}s and {@link RenderTaskWrapper}s.
 * Also it knows where it starts in {@link VideoProj}. A VideoPart can used as single source for a {@link VideoProj}, however you can add multiple VideoParts for more clarity.
 *
 * @author hlvs-apps
 */
public class VideoPart implements Serializable {
    private static final long serialVersionUID = 45L;
    private final List<RenderTaskWrapper> renderTaskWrappers;
    private final List<VideoSegmentWithTime> segments;
    private int frameStartInProject;
    public VideoPart(List<RenderTaskWrapper> renderTaskWrappers, int frameStartInProject){
        //this.renderTaskWrappers=renderTaskWrappers;
        this.renderTaskWrappers=new ArrayList<>();
        for(RenderTaskWrapper w:renderTaskWrappers){
            this.addRenderTaskWrapper(w);
        }
        this.frameStartInProject=frameStartInProject;
        segments=new ArrayList<>();
    }
    public VideoPart(RenderTaskWrapper renderTaskWrapper, int frameStartInProject){
        //this.renderTaskWrappers=renderTaskWrappers;
        this.renderTaskWrappers=new ArrayList<>();
        this.addRenderTaskWrapper(renderTaskWrapper);
        this.frameStartInProject=frameStartInProject;
        segments=new ArrayList<>();
    }

    public List<RenderTaskWrapper> getRenderTaskWrappers() {
        return renderTaskWrappers;
    }

    /**
     * Adds a {@link RenderTaskWrapper} to this Part
     * @param wrapper The RenderTaskWrapper
     */
    public void addRenderTaskWrapper(RenderTaskWrapper wrapper){
        utils.LogD(String.valueOf(frameStartInProject));
        utils.LogD(String.valueOf(wrapper.getFrameInPartTo()));
        utils.LogD(String.valueOf(wrapper.getFrameInPartFrom()));
        renderTaskWrappers.add(wrapper
                .setFrameInProjectFrom(wrapper.getFrameInPartFrom()+frameStartInProject)
                .setFrameInProjectTo((wrapper.getFrameInPartTo()!=-1)?(wrapper.getFrameInPartTo()+frameStartInProject):-1));
        utils.LogD(String.valueOf(renderTaskWrappers.get(renderTaskWrappers.size()-1).getFrameInPartTo()));
    }

    public List<VideoSegmentWithTime> getSegments() {
        return segments;
    }


    public void addVideoSegment(VideoSegment segment, int startFrameInPart){
        segments.add(new VideoSegmentWithTime(segment.getUriIdentifiers(),startFrameInPart));
    }

    public void addVideoSegment(VideoSegmentWithTime videoSegmentWithTime){
        segments.add(videoSegmentWithTime);
    }

    public void setFrameStartInProject(int frameStartInProject) {
        this.frameStartInProject = frameStartInProject;
    }

    public int getFrameStartInProject() {
        return frameStartInProject;
    }
}