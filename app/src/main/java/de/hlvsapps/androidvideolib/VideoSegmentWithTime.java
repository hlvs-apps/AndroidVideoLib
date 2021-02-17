package de.hlvsapps.androidvideolib;

import java.util.List;

public class VideoSegmentWithTime extends VideoSegment{
    private final int startTimeInPart;
    public VideoSegmentWithTime(List<UriIdentifier> parts,int startTimeInPart) {
        super(parts);
        this.startTimeInPart=startTimeInPart;
    }

    public int getStartTimeInPart() {
        return startTimeInPart;
    }
}
