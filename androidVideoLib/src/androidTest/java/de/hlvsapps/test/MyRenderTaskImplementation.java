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

package de.hlvsapps.test;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hlvsapps.androidvideolib.RenderTask;
import de.hlvsapps.androidvideolib.VideoBitmap;
import de.hlvsapps.androidvideolib.utils;

public class MyRenderTaskImplementation implements RenderTask, Parcelable {
    @Override
    public List<Bitmap> render(List<VideoBitmap> bitmaps0, List<VideoBitmap> bitmaps1, int frameInProject) {
        Bitmap bitmap0 = null, bitmap1 = null;
        utils.LogD("RUN");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (VideoBitmap bitmap : bitmaps0) {
            if (bitmap.getIdentifier().equals("Test"))
                bitmap0 = bitmap.getBitmap();
        }
        for (VideoBitmap bitmap : bitmaps1) {
            if (bitmap.getIdentifier().equals("Test"))
                bitmap1 = bitmap.getBitmap();
        }
        if (bitmap0 != null && bitmap1 != null) {
            List<Bitmap> result = new ArrayList<>();
            result.add(bitmap0);
            return result;
        } else if (bitmap0 != null) return Collections.singletonList(bitmap0);
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    public static final Creator<MyRenderTaskImplementation> CREATOR=new Creator<MyRenderTaskImplementation>() {
        @Override
        public MyRenderTaskImplementation createFromParcel(Parcel source) {
            return new MyRenderTaskImplementation();
        }

        @Override
        public MyRenderTaskImplementation[] newArray(int size) {
            return new MyRenderTaskImplementation[0];
        }
    };
}
