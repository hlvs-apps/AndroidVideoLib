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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * A VideoSegment is a Container for {@link UriIdentifier}.
 * @author hlvs-apps
 */
public class VideoSegment implements Serializable {
    private static final long serialVersionUID = 43L;
    private List<UriIdentifier> uriIdentifiers;

    /**
     * @param parts {@link List<UriIdentifier>} The UriIdentifiers to be contained in this {@link VideoSegment}
     */
    public VideoSegment(List<UriIdentifier> parts){
        setUriIdentifiers(parts);
    }

    /**
     * Get all {@link UriIdentifier}s
     * @return all {@link UriIdentifier}s contained in this {@link VideoSegment}
     */
    public List<UriIdentifier> getUriIdentifiers() {
        return uriIdentifiers;
    }

    /**
     * Add {@link UriIdentifier} to {@link VideoSegment#uriIdentifiers}
     * @param uriIdentifier The {@link UriIdentifier} to add.
     */
    public void addUriIdentifier(UriIdentifier uriIdentifier){
        if(uriIdentifier.getStartInVideoSegment()==UriIdentifier.START_IN_VIDEO_SEGMENT_NOT_SET) throw new IllegalStateException("Start must be set");
        uriIdentifiers.add(uriIdentifier);
    }

    /**
     * Set new List of {@link UriIdentifier}s
     * @param uriIdentifiers The new List to set as {@link VideoSegment#uriIdentifiers}
     */
    public void setUriIdentifiers(List<UriIdentifier> uriIdentifiers){
        this.uriIdentifiers=new ArrayList<>();
        for (UriIdentifier i:uriIdentifiers){
            addUriIdentifier(i);
        }
    }
}
