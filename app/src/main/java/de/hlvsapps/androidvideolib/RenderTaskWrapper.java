package de.hlvsapps.androidvideolib;

public class RenderTaskWrapper {
    private final RenderTask renderTask;
    private final int frameInPartFrom;
    private final int frameInPartTo;
    private int frameInProjectFrom;
    private int frameInProjectTo;
    public RenderTaskWrapper(RenderTask task,int frameInPartFrom,int frameInPartTo){
        this.renderTask=task;
        this.frameInPartFrom=frameInPartFrom;
        this.frameInPartTo=frameInPartTo;
    }
    public RenderTaskWrapper(RenderTask task,int frameInPartFrom,int frameInPartTo,int frameInProjectFrom,int frameInProjectTo){
        this.renderTask=task;
        this.frameInPartFrom=frameInPartFrom;
        this.frameInPartTo=frameInPartTo;
        this.frameInProjectFrom=frameInProjectFrom;
        this.frameInProjectTo=frameInProjectTo;
    }

    public RenderTaskWrapper setFrameInProjectFrom(int frameInProjectFrom) {
        this.frameInProjectFrom = frameInProjectFrom;
        return this;
    }

    public int getFrameInProjectFrom() {
        return frameInProjectFrom;
    }

    public RenderTaskWrapper setFrameInProjectTo(int frameInProjectTo) {
        this.frameInProjectTo = frameInProjectTo;
        return this;
    }

    public int getFrameInProjectTo() {
        return frameInProjectTo;
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
