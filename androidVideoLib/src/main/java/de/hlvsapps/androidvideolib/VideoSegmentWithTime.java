/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:  https://githubcom/hlvs-apps/AndroidVideoLib/blob/master/AUTHOR.md
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
import java.util.List;

/**
 * A {@link VideoSegment} with Time Information: When does this {@link VideoSegment} start in {@link VideoPart}
 *
 * @author hlvs-apps
 */
public class VideoSegmentWithTime extends VideoSegment implements Parcelable {
    private final int startTimeInPart;
    private final List<UriIdentifier> parts;

    /**
     * @param parts {@link UriIdentifier}s to be contained in this VideoSegment
     * @param startTimeInPart When does this VideoSegment start in {@link VideoPart}, in Frames.
     * @see VideoSegmentWithTime
     */
    public VideoSegmentWithTime(List<UriIdentifier> parts,int startTimeInPart) {
        super(parts);
        this.parts=parts;
        this.startTimeInPart=startTimeInPart;
    }

    /**
     * The same as {@link VideoSegmentWithTime#VideoSegmentWithTime(List, int)}, but with VideoSegment instead of {@link UriIdentifier}s
     * @param videoSegment The {@link VideoSegment} to extract the {@link UriIdentifier}s
     * @param startTimeInPart When does this VideoSegment start in {@link VideoPart}, in Frames
     * @see VideoSegmentWithTime
     */
    public VideoSegmentWithTime(VideoSegment videoSegment, int startTimeInPart){
        this(videoSegment.getUriIdentifiers(),startTimeInPart);
    }

    /**
     * Get start in {@link VideoPart}
     * @return start in {@link VideoPart}, in frames
     */
    public int getStartTimeInPart() {
        return startTimeInPart;
    }

    protected VideoSegmentWithTime(Parcel in) {
        this(readParts(in),in.readInt());
    }

    private static List<UriIdentifier> readParts(Parcel in){
        ArrayList<UriIdentifier> parts;
        if (in.readByte() == 0x01) {
            parts = new ArrayList<UriIdentifier>();
            in.readList(parts, UriIdentifier.class.getClassLoader());
        } else {
            parts = null;
        }
        return parts;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (parts == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(parts);
        }
        dest.writeInt(startTimeInPart);
    }

    public static final Parcelable.Creator<VideoSegmentWithTime> CREATOR = new Parcelable.Creator<VideoSegmentWithTime>() {
        @Override
        public VideoSegmentWithTime createFromParcel(Parcel in) {
            return new VideoSegmentWithTime(in);
        }

        @Override
        public VideoSegmentWithTime[] newArray(int size) {
            return new VideoSegmentWithTime[size];
        }
    };
}
