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

/**
 * Used for Rendering. Contains a {@link RenderTask} and time Information.
 * @author hlvs-apps
 */
public class RenderTaskWrapper implements Parcelable {
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

    protected RenderTaskWrapper(Parcel in) {
        renderTask = (RenderTask) in.readValue(RenderTask.class.getClassLoader());
        frameInPartFrom = in.readInt();
        frameInPartTo = in.readInt();
        frameInProjectFrom = in.readInt();
        frameInProjectTo = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(renderTask);
        dest.writeInt(frameInPartFrom);
        dest.writeInt(frameInPartTo);
        dest.writeInt(frameInProjectFrom);
        dest.writeInt(frameInProjectTo);
    }

    public static final Parcelable.Creator<RenderTaskWrapper> CREATOR = new Parcelable.Creator<RenderTaskWrapper>() {
        @Override
        public RenderTaskWrapper createFromParcel(Parcel in) {
            return new RenderTaskWrapper(in);
        }

        @Override
        public RenderTaskWrapper[] newArray(int size) {
            return new RenderTaskWrapper[size];
        }
    };
}
