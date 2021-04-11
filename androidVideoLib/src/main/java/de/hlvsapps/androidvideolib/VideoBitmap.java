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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Bitmap with UriIdentifier
 *
 * @author hlvs-apps
 */
public class VideoBitmap implements Parcelable {
    private final Bitmap bitmap;
    private final UriIdentifier uIdentifier;
    public VideoBitmap(Bitmap bitmap, UriIdentifier uriIdentifier){
        this.bitmap=bitmap;
        this.uIdentifier=uriIdentifier;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getIdentifier() {
        return uIdentifier.getIdentifier();
    }

    public UriIdentifier getUriIdentifier(){
        return uIdentifier;
    }

    protected VideoBitmap(Parcel in) {
        bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        uIdentifier = (UriIdentifier) in.readValue(UriIdentifier.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(bitmap);
        dest.writeValue(uIdentifier);
    }

    public static final Parcelable.Creator<VideoBitmap> CREATOR = new Parcelable.Creator<VideoBitmap>() {
        @Override
        public VideoBitmap createFromParcel(Parcel in) {
            return new VideoBitmap(in);
        }

        @Override
        public VideoBitmap[] newArray(int size) {
            return new VideoBitmap[size];
        }
    };
}