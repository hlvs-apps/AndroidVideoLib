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
 * Contains {@link UriIdentifier}, with Computed Length, set by {@link RendererTimeLine}
 * You don't need to use this, instead use {@link UriIdentifier}
 */
public class UriIdentifierPair implements Comparable<UriIdentifierPair>, Parcelable {
    private final UriIdentifier uriIdentifier;
    private final Integer frameStartInProject;
    private int lengthInFrames;
    private double lengthInSeconds;
    public UriIdentifierPair(UriIdentifier uriIdentifier,Integer frameStartInProject){
        this.uriIdentifier=uriIdentifier;
        this.frameStartInProject=frameStartInProject;
    }

    public int getLengthInFrames() {
        return lengthInFrames;
    }

    public UriIdentifierPair setLengthInFrames(int lengthInFrames) {
        this.lengthInFrames = lengthInFrames;
        return this;
    }

    public UriIdentifierPair setLengthInSeconds(double lengthInSeconds){
        this.lengthInSeconds=lengthInSeconds;
        return this;
    }

    public double getLengthInSeconds() {
        return lengthInSeconds;
    }

    public Integer getFrameStartInProject() {
        return frameStartInProject;
    }

    @Override
    public int compareTo (UriIdentifierPair o2) {
        return frameStartInProject-o2.getFrameStartInProject();
    }

    public UriIdentifier getUriIdentifier() {
        return uriIdentifier;
    }

    protected UriIdentifierPair(Parcel in) {
        uriIdentifier = (UriIdentifier) in.readValue(UriIdentifier.class.getClassLoader());
        frameStartInProject = in.readByte() == 0x00 ? null : in.readInt();
        lengthInFrames = in.readInt();
        lengthInSeconds = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(uriIdentifier);
        if (frameStartInProject == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(frameStartInProject);
        }
        dest.writeInt(lengthInFrames);
        dest.writeDouble(lengthInSeconds);
    }

    public static final Parcelable.Creator<UriIdentifierPair> CREATOR = new Parcelable.Creator<UriIdentifierPair>() {
        @Override
        public UriIdentifierPair createFromParcel(Parcel in) {
            return new UriIdentifierPair(in);
        }

        @Override
        public UriIdentifierPair[] newArray(int size) {
            return new UriIdentifierPair[size];
        }
    };


    public static class UriIdentifierPairList implements Parcelable {
        private final List<UriIdentifierPair> pairs;

        private UriIdentifierPairList(List<UriIdentifierPair> pairs) {
            this.pairs = pairs;
        }

        public static UriIdentifierPairList from(List<UriIdentifierPair> pairs){
            return new UriIdentifierPairList(pairs);
        }

        public List<UriIdentifierPair> getPairs() {
            return pairs;
        }

        protected UriIdentifierPairList(Parcel in) {
            if (in.readByte() == 0x01) {
                pairs = new ArrayList<>();
                in.readList(pairs, UriIdentifierPair.class.getClassLoader());
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

        public static final Parcelable.Creator<UriIdentifierPairList> CREATOR = new Parcelable.Creator<UriIdentifierPairList>() {
            @Override
            public UriIdentifierPairList createFromParcel(Parcel in) {
                return new UriIdentifierPairList(in);
            }

            @Override
            public UriIdentifierPairList[] newArray(int size) {
                return new UriIdentifierPairList[size];
            }
        };
    }


}
