/*-----------------------------------------------------------------------------
 - Copyright 2021 hlvs-apps                                                   -
 - This is a part of AndroidVideoLib.                                         -
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

public class UriIdentifierPair {
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


    public UriIdentifier getUriIdentifier() {
        return uriIdentifier;
    }
}
