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
 *
 * @author hlvs-apps
 */
public class RenderTaskWrapperWithUriIdentifierPairs extends RenderTaskWrapper implements Parcelable {
    private final List<UriIdentifierPair> matchingUriIdentifierPairs;
    private final RenderTaskWrapper wrapper;
    public RenderTaskWrapperWithUriIdentifierPairs(RenderTaskWrapper wrapper,List<UriIdentifierPair> matchingUriIdentifierPairs){
        super(wrapper.getRenderTask(),wrapper.getFrameInPartFrom(),wrapper.getFrameInPartTo(),wrapper.getFrameInProjectFrom(),wrapper.getFrameInProjectTo());
        this.matchingUriIdentifierPairs=matchingUriIdentifierPairs;
        this.wrapper =wrapper;
    }

    public List<UriIdentifierPair> getMatchingUriIdentifierPairs() {
        return matchingUriIdentifierPairs;
    }

    protected RenderTaskWrapperWithUriIdentifierPairs(Parcel in) {
        this(getWrapperFromParcel(in),getMatchingUriIdentifierPairsFromParcel(in));
    }

    private static RenderTaskWrapper getWrapperFromParcel(Parcel in){
        return (RenderTaskWrapper) in.readValue(RenderTaskWrapper.class.getClassLoader());
    }

    private static List<UriIdentifierPair> getMatchingUriIdentifierPairsFromParcel(Parcel in){
        final List<UriIdentifierPair> matchingUriIdentifierPairs;
        if (in.readByte() == 0x01) {
            matchingUriIdentifierPairs = new ArrayList<>();
            in.readList(matchingUriIdentifierPairs, UriIdentifierPair.class.getClassLoader());
        } else {
            matchingUriIdentifierPairs = null;
        }
        return matchingUriIdentifierPairs;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (matchingUriIdentifierPairs == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(matchingUriIdentifierPairs);
        }
        dest.writeValue(wrapper);
    }

    public static final Parcelable.Creator<RenderTaskWrapperWithUriIdentifierPairs> CREATOR = new Parcelable.Creator<RenderTaskWrapperWithUriIdentifierPairs>() {
        @Override
        public RenderTaskWrapperWithUriIdentifierPairs createFromParcel(Parcel in) {
            return new RenderTaskWrapperWithUriIdentifierPairs(in);
        }

        @Override
        public RenderTaskWrapperWithUriIdentifierPairs[] newArray(int size) {
            return new RenderTaskWrapperWithUriIdentifierPairs[size];
        }
    };
}