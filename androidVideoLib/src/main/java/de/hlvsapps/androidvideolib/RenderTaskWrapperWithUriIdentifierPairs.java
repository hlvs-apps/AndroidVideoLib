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
        this(getWrapperFromParcel(in),in.createTypedArrayList(UriIdentifierPair.CREATOR));
    }

    private static RenderTaskWrapper getWrapperFromParcel(Parcel in){
        return (RenderTaskWrapper) in.readParcelable(RenderTaskWrapper.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(wrapper,flags);
        dest.writeTypedList(matchingUriIdentifierPairs);
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

    public static class RenderTaskWrapperWithUriIdentifierPairsList implements Parcelable {
        private final List<RenderTaskWrapperWithUriIdentifierPairs> pairs;

        private RenderTaskWrapperWithUriIdentifierPairsList(List<RenderTaskWrapperWithUriIdentifierPairs> pairs) {
            this.pairs = pairs;
        }

        public static RenderTaskWrapperWithUriIdentifierPairsList from(List<RenderTaskWrapperWithUriIdentifierPairs> pairs) {
            return new RenderTaskWrapperWithUriIdentifierPairsList(pairs);
        }

        public List<RenderTaskWrapperWithUriIdentifierPairs> getPairs() {
            return pairs;
        }

        protected RenderTaskWrapperWithUriIdentifierPairsList(Parcel in) {
            if (in.readByte() == 0x01) {
                pairs = new ArrayList<RenderTaskWrapperWithUriIdentifierPairs>();
                in.readList(pairs, RenderTaskWrapperWithUriIdentifierPairs.class.getClassLoader());
            } else {
                pairs = null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (pairs == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(pairs);
            }
        }

        public static final Parcelable.Creator<RenderTaskWrapperWithUriIdentifierPairsList> CREATOR = new Parcelable.Creator<RenderTaskWrapperWithUriIdentifierPairsList>() {
            @Override
            public RenderTaskWrapperWithUriIdentifierPairsList createFromParcel(Parcel in) {
                return new RenderTaskWrapperWithUriIdentifierPairsList(in);
            }

            @Override
            public RenderTaskWrapperWithUriIdentifierPairsList[] newArray(int size) {
                return new RenderTaskWrapperWithUriIdentifierPairsList[size];
            }
        };
    }
}