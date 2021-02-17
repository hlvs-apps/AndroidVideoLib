package de.hlvsapps.androidvideolib;

import android.graphics.Bitmap;

import java.util.List;

public interface RenderTask {
    Bitmap render(List<VideoBitmap> bitmaps0, List<VideoBitmap> bitmaps1, int frameInProject, int frameInPart);
}
