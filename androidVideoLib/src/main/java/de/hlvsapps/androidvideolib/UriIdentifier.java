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

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A UriIdentifier contains a Video Uri and and Identifier String to Identify the Uri. The Identifier should be unique, otherwise the VideoProject might not be able to preRender or Render the Project.
 * You can add CustomData, in which you can pack all Information you need to render a this UriIdentifier.
 * You can use CustomData in implements Parcelable {@link RenderTask#render(List, List, int)} in the {@link VideoBitmap}.
 *
 * @author hlvs-apps
 */
public class UriIdentifier implements Parcelable{
    public static final int START_IN_VIDEO_SEGMENT_NOT_SET=-12354;
    private int customLengthInFrames;
    private int customLengthInSeconds;
    private String identifier;
    private Uri uri;
    private int startInVideoSegment;

    private List<Serializable> customData;

    /**
     * Creates a normal {@link UriIdentifier}
     * @param uri The Uri
     * @param identifier The unique Identifier
     * @param startInVideoSegment When does this UriIdentifier start in VideoSegment?
     */
    public UriIdentifier(Uri uri,String identifier,int startInVideoSegment){
        this.identifier=identifier;
        this.uri=uri;
        this.startInVideoSegment=startInVideoSegment;
    }

    /**
     * Creates an UriIdentifier without startInVideoSegment. In this state, it is not usable by the complete Project. This Method is only when you want this UriIdentifier as Store for your Uris,
     * not to add them to an {@link VideoPart}.
     * @param uri The Uri
     * @param identifier The unique Identifier
     */
    public UriIdentifier(Uri uri, String identifier){
        this.identifier=identifier;
        this.uri=uri;
        this.startInVideoSegment=START_IN_VIDEO_SEGMENT_NOT_SET;
    }

    /**
     * Gets a custom length in Frames, must not equal the real Length.
     * @return The Custom Length set by {@link UriIdentifier#setCustomLengthInFrames(int)}
     */
    public int getCustomLengthInFrames() {
        return customLengthInFrames;
    }

    /**
     * Sets a Custom Length in Frames, has no effect on real Length.
     * @param customLengthInFrames The Length.
     */
    public void setCustomLengthInFrames(int customLengthInFrames) {
        this.customLengthInFrames = customLengthInFrames;
    }

    public int getCustomLengthInSeconds() {
        return customLengthInSeconds;
    }

    /**
     * Adds Custom Object Data.
     * @param customData The Custom Data you want to add.
     * @see UriIdentifier
     */
    public void addCustomData(Serializable... customData){
        if(this.customData==null)this.customData=new ArrayList<>();
        this.customData.addAll(Arrays.asList(customData));
    }

    /**
     * Sets custom Object Data
     * @param customData The Custom Data you want to set, as new Custom Data.
     * @see UriIdentifier
     */
    public void setCustomData(List<Serializable> customData) {
        this.customData = customData;
    }

    /**
     * Adds Custom Object Data.
     * @param customData The Custom Data you want to add.
     * @see UriIdentifier
     */
    public void addCustomData(List<Object> customData){
        if(this.customData==null)this.customData=new ArrayList<>();
    }

    public void setCustomLengthInSeconds(int customLengthInSeconds) {
        this.customLengthInSeconds = customLengthInSeconds;
    }

    /**
     * Gets the Length of the Video of the Uri in Frames
     * Do not use this in AndroidVideoLib, instead use Cached Length in {@link UriIdentifierPair}
     * @param c Your Context
     * @return The Length in Frames
     * @throws IOException Thrown by {@link utils#getMP4LengthInFrames(Context, Uri)}
     */
    public int getRealLengthInFrames(Context c) throws IOException {
        return utils.getMP4LengthInFrames(c,uri);
    }

    /**
     * Gets the Length of the Video of the Uri in Seconds
     * Do not use this in AndroidVideoLib, instead use Cached Length in {@link UriIdentifierPair}
     * @param c Your Context
     * @return The Length in Seconds
     * @throws IOException Thrown by {@link utils#getMP4LengthInSeconds(Context, Uri)}
     */
    public double getRealLengthInSeconds(Context c) throws IOException {
        return utils.getMP4LengthInSeconds(c,uri);
    }


    //Getter and Setter Methods
    public int getStartInVideoSegment() {
        return startInVideoSegment;
    }

    public void setStartInVideoSegment(int startInVideoSegment) {
        this.startInVideoSegment = startInVideoSegment;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Uri getUri() {
        return uri;
    }

    protected UriIdentifier(Parcel in) {
        customLengthInFrames = in.readInt();
        customLengthInSeconds = in.readInt();
        identifier = in.readString();
        String uriString=(String) in.readValue(String.class.getClassLoader());
        uri = Uri.parse(uriString);
        startInVideoSegment = in.readInt();
        if (in.readByte() == 0x01) {
            customData = new ArrayList<Serializable>();
            in.readList(customData, Serializable.class.getClassLoader());
        } else {
            customData = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(customLengthInFrames);
        dest.writeInt(customLengthInSeconds);
        dest.writeString(identifier);
        dest.writeValue(uri.toString());
        dest.writeInt(startInVideoSegment);
        if (customData == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(customData);
        }
    }

    public static final Parcelable.Creator<UriIdentifier> CREATOR = new Parcelable.Creator<UriIdentifier>() {
        @Override
        public UriIdentifier createFromParcel(Parcel in) {
            return new UriIdentifier(in);
        }

        @Override
        public UriIdentifier[] newArray(int size) {
            return new UriIdentifier[size];
        }
    };
}
