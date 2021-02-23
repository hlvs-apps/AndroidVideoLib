/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 - Copyright 2021 the authors of AndroidVideoLib                              -
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

import android.net.Uri;

import java.io.Serializable;

/**
 * A UriIdentifier contains a Video Uri and and Identifier String to Identify the Uri. The Identifier should be unique, otherwise the VideoProject might not be able to preRender or Render the Project.
 *
 * @author hlvs-apps
 */
public class UriIdentifier implements Serializable {
    private static final long serialVersionUID = 49L;
    public static final int START_IN_VIDEO_SEGMENT_NOT_SET=-12354;
    private int customLengthInFrames,customLengthInSeconds;
    private String identifier;
    private Uri uri;
    private int startInVideoSegment;

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

    public void setCustomLengthInSeconds(int customLengthInSeconds) {
        this.customLengthInSeconds = customLengthInSeconds;
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
}