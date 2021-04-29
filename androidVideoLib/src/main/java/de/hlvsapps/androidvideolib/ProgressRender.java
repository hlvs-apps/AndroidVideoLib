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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Interface for Updating progress while Rendering.
 * While Rendering, you need x+1 views, x for the Tasks while Rendering, 1 for the Last Task, Saving the Video
 * @author hlvs-apps
 */
public interface ProgressRender {
    String progressRenderState="progressRenderState";
    String progressRenderMax="progressRenderMax";
    String progressRenderFinished="progressRenderFinished";
    String progressRenderNumberOfRenderer="progressRenderNumberOfRenderer";
    String progressRenderFunctionToCall ="functionToCall";
    enum FunctionToCall implements Parcelable {
        instantiateProgressesForRendering,updateProgressOfX,updateProgressOfSavingVideo,nothing;
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(ordinal());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<FunctionToCall> CREATOR = new Creator<FunctionToCall>() {
            @Override
            public FunctionToCall createFromParcel(Parcel in) {
                return FunctionToCall.values()[in.readInt()];
            }

            @Override
            public FunctionToCall[] newArray(int size) {
                return new FunctionToCall[size];
            }
        };
    }

    /**
     * Instantiate x views
     * @param num x views
     */
    void instantiateProgressesForRendering(int num);

    /**
     * Update Progress of render Task num ...
     * @param num The num of the Renderer to be updated
     * @param progress the progress
     * @param max the max progress
     * @param finished finished?true:false
     */
    void updateProgressOfX(int num,int progress,int max,boolean finished);

    /**
     * Render Task X Failed
     * @param num x
     */
    void xFailed(int num);

    /**
     * Update the Progress of Video Saving
     *
     * @param progress the progress
     * @param max the max progress
     * @param finished finished?true:false
     */
    void updateProgressOfSavingVideo(int progress, int max, boolean finished);

    /**
     * Exporting Video Failed
     */
    void exportFailed();
}
