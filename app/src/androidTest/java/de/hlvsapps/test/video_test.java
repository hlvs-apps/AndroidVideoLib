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

package de.hlvsapps.test;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hlvsapps.androidvideolib.R;
import de.hlvsapps.androidvideolib.Rational;
import de.hlvsapps.androidvideolib.RenderTaskWrapper;
import de.hlvsapps.androidvideolib.UriIdentifier;
import de.hlvsapps.androidvideolib.VideoBitmap;
import de.hlvsapps.androidvideolib.VideoPart;
import de.hlvsapps.androidvideolib.VideoProj;
import de.hlvsapps.androidvideolib.VideoSegmentWithTime;
import de.hlvsapps.androidvideolib.utils;

public class video_test extends AppCompatActivity {

    private final int PICK_VIDEO_REQUEST = 190;
    ProgressBar progressBar;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);

        Button choose=findViewById(R.id.choose);
        textView=findViewById(R.id.textView);
        progressBar=findViewById(R.id.progressBar);
        choose.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            intent.setType("video/*");

            //intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_VIDEO_REQUEST);
        });

        /*binding.next.setOnClickListener(v -> {
            try {
                if(grab==null){
                    throw new NullPointerException("Grab cannot be null");
                }
                Picture picture = grab.getNativeFrame();
                binding.imageView3.setImageBitmap(AndroidUtil.toBitmap(picture));
            } catch (IOException | NullPointerException e) {
                utils.LogE(e);
            }

        });

         */
    }

    @Override
    protected void onResume() {
        if (WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWorkLiveData("Render").hasObservers()) {
            WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWorkLiveData("Render").removeObservers(this);
        }
        WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWorkLiveData("Render").observe(this, workInfos -> {
            if (workInfos.size() > 0) {
                WorkInfo info = workInfos.get(0);
                int progress1 = info.getProgress().getInt("progress", -2);
                int max = info.getProgress().getInt("max", 1);
                //Do something with progress variable
                updateState(progress1, max);
            }
        });
        super.onResume();
    }

    @Override
    protected void onPause() {
        WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWorkLiveData("Render").removeObservers(this);
        super.onPause();
    }

    private void updateState(int i, int l) {
        progressBar.setMax(l);
        progressBar.setProgress(i);
        textView.setText(i + "/" + l);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null) {
            //VideoProj videoProj=new VideoProj("Mein_erstes_Testvideo", new Rational(25,1),this);
            VideoPart part = new VideoPart(
                    new RenderTaskWrapper((bitmaps0, bitmaps1, frameInProject) -> {
                        Bitmap bitmap0 = null, bitmap1 = null;
                        utils.LogD("RUN");
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
                    }, 0, -1)
                    , 0);
            part.addVideoSegment(
                    new VideoSegmentWithTime(
                            Collections.singletonList(
                                    new UriIdentifier(data.getData(), "Test", 0)
                            ), 0
                    )
            );
            VideoProj videoProj = new VideoProj(Collections.singletonList(part), new Rational(5, 1), this);
            utils.LogD(String.valueOf(videoProj.getLength()));
            videoProj.preRender(videoProj::render);
        }
    }
}