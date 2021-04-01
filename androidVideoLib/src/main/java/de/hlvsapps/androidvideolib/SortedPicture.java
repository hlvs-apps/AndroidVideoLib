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

import android.graphics.Bitmap;

public class SortedPicture implements Comparable<SortedPicture> {
    private final double timestamp;
    private final Bitmap bitmap;
    private final double duration;

    public double getDuration() {
        return duration;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public double getTimestampPlusDuration(){
        return timestamp+duration;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public SortedPicture(double timestamp, Bitmap bitmap, double duration) {
        this.duration=duration;
        this.bitmap = bitmap;
        this.timestamp = timestamp;
    }

    public boolean doesTimeStampPlusDurationBeforeEqualThisTimeStamp(double valueBeforeThis){
        final int diff= (int) ((valueBeforeThis-timestamp)*1000D);
        return (1>=diff) && (diff >= -1);
    }

    @Override
    public int compareTo(SortedPicture other) {
        return (int) ((timestamp-other.getTimestamp())*100000D);
    }

}
