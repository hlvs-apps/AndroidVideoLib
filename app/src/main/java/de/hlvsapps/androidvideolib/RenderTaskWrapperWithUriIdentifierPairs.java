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

public class RenderTaskWrapperWithUriIdentifierPairs extends RenderTaskWrapper{
    private final List<UriIdentifierPair> matchingUriIdentifierPairs;
    public RenderTaskWrapperWithUriIdentifierPairs(RenderTaskWrapper w,List<UriIdentifierPair> matchingUriIdentifierPairs){
        super(w.getRenderTask(),w.getFrameInPartFrom(),w.getFrameInPartTo(),w.getFrameInProjectFrom(),w.getFrameInProjectTo());
        this.matchingUriIdentifierPairs=matchingUriIdentifierPairs;
    }

    public List<UriIdentifierPair> getMatchingUriIdentifierPairs() {
        return matchingUriIdentifierPairs;
    }
}
