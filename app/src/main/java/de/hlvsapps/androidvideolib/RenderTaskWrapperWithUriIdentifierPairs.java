package de.hlvsapps.androidvideolib;

import java.util.List;

public class RenderTaskWrapperWithUriIdentifierPairs extends RenderTaskWrapper{
    private final List<UriIdentifierPair> matchingUriIdentifierPairs;
    public RenderTaskWrapperWithUriIdentifierPairs(RenderTaskWrapper w,List<UriIdentifierPair> matchingUriIdentifierPairs){
        super(w.getRenderTask(),w.getFrameInPartFrom(),w.getFrameInPartTo(),w.getFrameInProjectFrom(),w.getFrameInProjectTo());
        this.matchingUriIdentifierPairs=matchingUriIdentifierPairs;
    }

    public List<UriIdentifierPair> getMatchingUriIdentifierPairs() {
        return matchingUriIdentifierPairs;
    }
}
