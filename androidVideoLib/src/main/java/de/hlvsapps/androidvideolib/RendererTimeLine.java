/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
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

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A class you dont need to use
 *
 * @author hlvs-apps
 */
public class RendererTimeLine implements Parcelable {
    private final List<UriIdentifierPair> uriIdentifierPairs;
    private final List<RenderTaskWrapper> renderTaskWrappers;
    private int videoLengthInFrames;
    private double videoLengthInSeconds;

    public RendererTimeLine(){
        uriIdentifierPairs=new ArrayList<>();
        renderTaskWrappers=new ArrayList<>();
    }

    private void sortIdentifierPairsByTime(){
        Collections.sort(uriIdentifierPairs);
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
        while(i<uriIdentifierPairs.size()){
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

    public void setUriIdentifierPairsLengthInSeconds(VideoProj proj){
        int i=0;
        while(i<uriIdentifierPairs.size()){
            try {
                uriIdentifierPairs.set(i,
                        uriIdentifierPairs.get(i).setLengthInSeconds(
                                utils.getMP4LengthInSeconds(proj,
                                        uriIdentifierPairs.get(i).getUriIdentifier().getUri())));
            } catch (Exception e) {
                utils.LogE(e);
            }
            i++;
        }
    }

    public double getVideoLengthInSeconds(VideoProj proj){
        setUriIdentifierPairsLengthInSeconds(proj);
        videoLengthInSeconds=0;
        for(UriIdentifierPair p:uriIdentifierPairs){
            double actual_length;
            if(proj.getFps()!=null){
                actual_length = p.getLengthInSeconds() + (p.getFrameStartInProject() * (proj.getFps().getNum()*1D / proj.getFps().getDen()));
            }else {
                actual_length = p.getLengthInSeconds() + p.getFrameStartInProject() * (p.getLengthInFrames() / p.getLengthInSeconds());
            }
            if(actual_length>videoLengthInSeconds){
                videoLengthInSeconds=actual_length;
            }
        }
        return videoLengthInFrames;
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
            int start=w.getFrameInProjectFrom();
            int end=w.getFrameInProjectTo();
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


    protected RendererTimeLine(Parcel in) {
        uriIdentifierPairs = in.createTypedArrayList(UriIdentifierPair.CREATOR);
        renderTaskWrappers = in.createTypedArrayList(RenderTaskWrapper.CREATOR);
        videoLengthInFrames = in.readInt();
        videoLengthInSeconds = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(uriIdentifierPairs);
        dest.writeTypedList(renderTaskWrappers);
        dest.writeInt(videoLengthInFrames);
        dest.writeDouble(videoLengthInSeconds);
    }

    public static final Creator<RendererTimeLine> CREATOR = new Creator<RendererTimeLine>() {
        @Override
        public RendererTimeLine createFromParcel(Parcel in) {
            return new RendererTimeLine(in);
        }

        @Override
        public RendererTimeLine[] newArray(int size) {
            return new RendererTimeLine[size];
        }
    };
}
