package de.hlvsapps.androidvideolib;

import java.util.List;

public class RenderTaskWrapperWithUriIdentifierPairs extends RenderTaskWrapper{
    private final List<UriIdentifierPair> matchingUriIdentifierPairs;
    public RenderTaskWrapperWithUriIdentifierPairs(RenderTask task, int frameInPartFrom, int frameInPartTo, List<UriIdentifierPair> matchingUriIdentifierPairs) {
        super(task, frameInPartFrom, frameInPartTo);
        this.matchingUriIdentifierPairs=matchingUriIdentifierPairs;
    }
    public RenderTaskWrapperWithUriIdentifierPairs(RenderTaskWrapper w,List<UriIdentifierPair> matchingUriIdentifierPairs){
        super(w.getRenderTask(),w.getFrameInPartFrom(),w.getFrameInPartTo());
        this.matchingUriIdentifierPairs=matchingUriIdentifierPairs;
    }

    public List<UriIdentifierPair> getMatchingUriIdentifierPairs() {
        return matchingUriIdentifierPairs;
    }
}
