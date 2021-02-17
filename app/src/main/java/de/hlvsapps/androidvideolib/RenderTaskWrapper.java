package de.hlvsapps.androidvideolib;

public class RenderTaskWrapper {
    private final RenderTask renderTask;
    private final int frameInPartFrom;
    private final int frameInPartTo;
    public RenderTaskWrapper(RenderTask task,int frameInPartFrom,int frameInPartTo){
        this.renderTask=task;
        this.frameInPartFrom=frameInPartFrom;
        this.frameInPartTo=frameInPartTo;
    }

    public RenderTask getRenderTask() {
        return renderTask;
    }

    public int getFrameInPartFrom() {
        return frameInPartFrom;
    }

    public int getFrameInPartTo() {
        return frameInPartTo;
    }
}
