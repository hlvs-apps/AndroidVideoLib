/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.graphics.Bitmap;

public class VideoBitmap {
    private final Bitmap bitmap;
    private final String identifier;
    public VideoBitmap(Bitmap bitmap,String identifier){
        this.bitmap=bitmap;
        this.identifier=identifier;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getIdentifier() {
        return identifier;
    }
}
