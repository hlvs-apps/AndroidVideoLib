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

public class RenderTaskWrapper {
    private final RenderTask renderTask;
    private final int frameInPartFrom;
    private final int frameInPartTo;
    private int frameInProjectFrom;
    private int frameInProjectTo;
    public RenderTaskWrapper(RenderTask task,int frameInPartFrom,int frameInPartTo){
        this.renderTask=task;
        this.frameInPartFrom=frameInPartFrom;
        this.frameInPartTo=frameInPartTo;
    }
    public RenderTaskWrapper(RenderTask task,int frameInPartFrom,int frameInPartTo,int frameInProjectFrom,int frameInProjectTo){
        this.renderTask=task;
        this.frameInPartFrom=frameInPartFrom;
        this.frameInPartTo=frameInPartTo;
        this.frameInProjectFrom=frameInProjectFrom;
        this.frameInProjectTo=frameInProjectTo;
    }

    public RenderTaskWrapper setFrameInProjectFrom(int frameInProjectFrom) {
        this.frameInProjectFrom = frameInProjectFrom;
        return this;
    }

    public int getFrameInProjectFrom() {
        return frameInProjectFrom;
    }

    public RenderTaskWrapper setFrameInProjectTo(int frameInProjectTo) {
        this.frameInProjectTo = frameInProjectTo;
        return this;
    }

    public int getFrameInProjectTo() {
        return frameInProjectTo;
    }

    public RenderTask getRenderTask() {
        return renderTask;
    }

    public int getFrameInPartFrom() {
        return frameInPartFrom;
    }

    public int getFrameInPartTo() {
        return frameInPartTo;
    }
}
