/*-----------------------------------------------------------------------------
 - Copyright hlvs-apps                                                        -
 - This is a part of AndroidVideoLib                                          -
 - Licensed under Apache 2.0                                                  -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.androidvideolib;

import android.graphics.Bitmap;

import java.util.List;

public interface RenderTask {
    List<Bitmap> render(List<VideoBitmap> bitmaps0, List<VideoBitmap> bitmaps1, int frameInProject);
}
