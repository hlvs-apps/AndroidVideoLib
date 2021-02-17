package de.hlvsapps.androidvideolib;

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RendererTimeLine {
    private final List<UriIdentifierPair> uriIdentifierPairs;
    private final List<RenderTaskWrapper> renderTaskWrappers;
    private int videoLengthInFrames;

    public RendererTimeLine(){
        uriIdentifierPairs=new ArrayList<>();
        renderTaskWrappers=new ArrayList<>();
    }

    private void sortIdentifierPairsByTime(){
        Collections.sort(uriIdentifierPairs,new UriIdentifierPairComparator());
    }

    public List<UriIdentifier> getAllUrisFromUriIdentifiers(){
        List<UriIdentifier> result=new ArrayList<>();
        for(UriIdentifierPair p:uriIdentifierPairs){
            result.add(p.getUriIdentifier());
        }
        return result;
    }

    public List<UriIdentifierPair> getUriIdentifierPairs() {
        return uriIdentifierPairs;
    }

    public void setUriIdentifierPairsLength(VideoProj proj){
        int i=0;
        while(i<=uriIdentifierPairs.size()){
            try {
                uriIdentifierPairs.set(i,
                        uriIdentifierPairs.get(i).setLengthInFrames(
                                utils.getMP4LengthInFrames(proj,
                                        uriIdentifierPairs.get(i).getUriIdentifier().getUri())));
            } catch (Exception e) {
                utils.LogE(e);
            }
            i++;
        }
    }

    public int getVideoLengthInFrames(VideoProj proj){
        setUriIdentifierPairsLength(proj);
        videoLengthInFrames=0;
        for(UriIdentifierPair p:uriIdentifierPairs){
            int actual_length=p.getLengthInFrames()+p.getFrameStartInProject();
            if(actual_length>videoLengthInFrames){
                videoLengthInFrames=actual_length;
            }
        }
        return videoLengthInFrames;
    }

    public int getSavedVideoLengthInFrames(){
        return videoLengthInFrames;
    }

    public List<RenderTaskWrapperWithUriIdentifierPairs> getRenderTasksWithMatchingUriIdentifierPairs (VideoProj proj){
        getVideoLengthInFrames(proj);
        sortIdentifierPairsByTime();
        List<RenderTaskWrapperWithUriIdentifierPairs> renderTaskWrapperWithUriIdentifierPairs = new ArrayList<>();
        for(RenderTaskWrapper w:renderTaskWrappers){
            int start=w.getFrameInPartFrom();
            int end=w.getFrameInPartTo();
            if(end==-1){
                end=videoLengthInFrames;
            }
            List<String> identifiers=new ArrayList<>();
            List<UriIdentifierPair> result=new ArrayList<>();
            for(UriIdentifierPair p:uriIdentifierPairs){
                int start_p=p.getFrameStartInProject();
                int end_p=start_p+p.getLengthInFrames();
                if(((start>=start_p && end_p>=start)||(end>=end_p && start<end_p)) && (!(identifiers.contains(p.getUriIdentifier().getIdentifier())))){
                    result.add(p);
                    identifiers.add(p.getUriIdentifier().getIdentifier());
                }
            }

            renderTaskWrapperWithUriIdentifierPairs.add(
                    new RenderTaskWrapperWithUriIdentifierPairs(w,result));
        }
        return renderTaskWrapperWithUriIdentifierPairs;
    }

    public void addAllParts(List<VideoPart> parts){
        for (VideoPart p:parts){
            addAllFromVideoPart(p);
        }
    }

    public void addAllFromVideoPart(VideoPart part){
        int frameProjectStart=part.getFrameStartInProject();
        for(VideoSegmentWithTime seg:part.getSegments()){
            int frameStartInPart=seg.getStartTimeInPart();
            for(UriIdentifier identifier:seg.getUriIdentifiers()){
                uriIdentifierPairs.add(
                        new UriIdentifierPair(identifier,identifier.getStartInVideoSegment()+frameStartInPart+frameProjectStart));
            }
        }
        renderTaskWrappers.addAll(part.getRenderTaskWrappers());
    }

}
