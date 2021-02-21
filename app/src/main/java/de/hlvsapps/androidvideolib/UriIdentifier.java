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


public class UriIdentifier implements Serializable {
    private static final long serialVersionUID = 49L;
    public static final int START_IN_VIDEO_SEGMENT_NOT_SET=-12354;
    private String identifier;
    private Uri uri;
    private int startInVideoSegment;
    public UriIdentifier(Uri uri,String identifier,int startInVideoSegment){
        this.identifier=identifier;
        this.uri=uri;
        this.startInVideoSegment=startInVideoSegment;
    }

    public UriIdentifier(Uri uri,String identifier){
        this.identifier=identifier;
        this.uri=uri;
        this.startInVideoSegment=START_IN_VIDEO_SEGMENT_NOT_SET;
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