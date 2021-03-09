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

import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.model.Picture;

public class SortedPicture implements Comparable<SortedPicture> {
    private final double timestamp;
    private final Bitmap bitmap;

    public double getTimestamp() {
        return timestamp;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public SortedPicture(double timestamp, Bitmap bitmap) {
        this.bitmap = bitmap;
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(SortedPicture o2) {
        return (int) ((timestamp-o2.getTimestamp())*100000D);
    }

}
