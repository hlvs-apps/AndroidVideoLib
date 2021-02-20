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

package de.hlvsapps.androidvideolib;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;

import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_FINISHED;
import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_MAX_PROGRESS;
import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED;
import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE;
import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE;
import static de.hlvsapps.androidvideolib.SendProgressAsBroadcast.INTENT_EXTRA_DATA_NAME_PROGRESS;


/**
 * Activity for Rendering Project. Don't Launch this Activity directly, instead use
 *
 * @author hlvs-apps
 */
public class ProgressActivity extends AppCompatActivity {
    private LocalBroadcastManager localBroadcastManager;
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED serializableExtra = (INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED) intent.getSerializableExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED);
            switch (serializableExtra) {
                case preRenderUpdateProgress:
                    //Do Nothing, this will not happen
                    break;
                case renderInstantiateProgressForRendering:
                    for (int i = 0; i < intent.getIntExtra(INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE, -1); i++) {
                        ProgressBar n = new ProgressBar(ProgressActivity.this,null, android.R.attr.progressBarStyleHorizontal);
                        n.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        tab.addView(n,i);
                    }
                    break;
                case renderUpdateProgressOfX:
                    int num = intent.getIntExtra(INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE, -1);
                    if (num != -1) {
                        ProgressBar b = (ProgressBar) tab.getChildAt(num);
                        b.setMax(intent.getIntExtra(INTENT_EXTRA_DATA_NAME_MAX_PROGRESS, 1));
                        b.setProgress(intent.getIntExtra(INTENT_EXTRA_DATA_NAME_PROGRESS, 0));
                    }
                    break;
                case lastRenderUpdateProgressOfSavingVideo:
                    progressBar.setMax(intent.getIntExtra(INTENT_EXTRA_DATA_NAME_MAX_PROGRESS, 1));
                    progressBar.setProgress(intent.getIntExtra(INTENT_EXTRA_DATA_NAME_PROGRESS, 0));
                    if (intent.getBooleanExtra(INTENT_EXTRA_DATA_NAME_FINISHED, false)) {
                        finish();
                    }
                    break;
            }
        }
    };

    private TableLayout tab;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        localBroadcastManager=LocalBroadcastManager.getInstance(this);
        tab=findViewById(R.id.tab);
        progressBar=findViewById(R.id.progressBar2);
        IntentFilter filter=new IntentFilter(SendProgressAsBroadcast.intentFilterAction);
        localBroadcastManager.registerReceiver(br, filter);
    }

    @Override
    protected void onDestroy() {
        localBroadcastManager.unregisterReceiver(br);
        super.onDestroy();
    }
}