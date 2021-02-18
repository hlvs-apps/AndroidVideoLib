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

import java.util.List;

public class VideoSegment {
    private List<UriIdentifier> uriIdentifiers;
    public VideoSegment(List<UriIdentifier> parts){
        this.uriIdentifiers =parts;
    }

    public List<UriIdentifier> getUriIdentifiers() {
        return uriIdentifiers;
    }

    public void addUriIdentifier(UriIdentifier uriIdentifier){
        uriIdentifiers.add(uriIdentifier);
    }

    public void setUriIdentifiers(List<UriIdentifier> uriIdentifiers){
        this.uriIdentifiers = uriIdentifiers;
    }
}
