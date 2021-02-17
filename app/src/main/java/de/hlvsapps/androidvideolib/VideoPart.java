package de.hlvsapps.androidvideolib;

import java.util.ArrayList;
import java.util.List;

public class VideoPart {
    private final List<RenderTaskWrapper> renderTaskWrappers;
    private final List<VideoSegmentWithTime> segments;
    private int frameStartInProject;
    public VideoPart(List<RenderTaskWrapper> renderTaskWrappers, int frameStartInProject){
        this.renderTaskWrappers=renderTaskWrappers;
        this.frameStartInProject=frameStartInProject;
        segments=new ArrayList<>();
    }

    public List<RenderTaskWrapper> getRenderTaskWrappers() {
        return renderTaskWrappers;
    }

    public void addRenderTaskWrapper(RenderTaskWrapper wrapper){
        renderTaskWrappers.add(wrapper
                .setFrameInProjectFrom(wrapper.getFrameInPartFrom()+frameStartInProject)
                .setFrameInProjectTo((wrapper.getFrameInPartTo()!=-1)?wrapper.getFrameInPartTo()+frameStartInProject:-1));
    }

    public List<VideoSegmentWithTime> getSegments() {
        return segments;
    }

    @Deprecated
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