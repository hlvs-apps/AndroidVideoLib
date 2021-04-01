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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;


/**
 * Use this class for easier handling Progress Updates of {@link ProgressRender} and {@link ProgressPreRender}.
 * Look at the Implemented Methods for the Variables you can receive.<br>
 * These Methods are: {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)}, {@link SendProgressAsBroadcast#instantiateProgressesForRendering(int)}, {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)},
 * {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}.<br>
 * The Broadcast will be sent as Local Broadcast, so that no other App can use it<br>
 * Also, to Handle the Broadcast, you need the Constants {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_PROGRESS}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_MAX_PROGRESS},
 * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_FINISHED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE} and the enum
 * {@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED}.
 *
 * @author hlvs-apps
 */
public class SendProgressAsBroadcast implements ProgressRender,ProgressPreRender, Closeable {

    /**
     * Intent Extra Data Name for the Method currently sending Broadcast. The Possible Options are specified in {@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED}
     */
    public static final String INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED="INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED";

    /**
     * Intent Extra Data Value for Progress. Used in {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}, {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)} and {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}
     */
    public static final String INTENT_EXTRA_DATA_NAME_PROGRESS="INTENT_EXTRA_DATA_NAME_PROGRESS";

    /**
     * Intent Extra Data Value for Maximal Progress. Used in {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}, {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)} and {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}
     */
    public static final String INTENT_EXTRA_DATA_NAME_MAX_PROGRESS="INTENT_EXTRA_DATA_NAME_MAX_PROGRESS";

    /**
     * Intent Extra Data Value if Operation is finished. Used in {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}, {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)} and {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}
     */
    public static final String INTENT_EXTRA_DATA_NAME_FINISHED="INTENT_EXTRA_DATA_NAME_FINISHED";

    /**
     * Intent Extra Data Value for Number of Progresses to Initiate. Used in {@link SendProgressAsBroadcast#instantiateProgressesForRendering(int)}
     */
    public static final String INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE="INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE";

    /**
     * Intent Extra Data Value for Number of Progress to Update. Used in {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}
     */
    public static final String INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE="INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE";

    /**
     * The Intent Action for the Local Broadcast
     */
    public static final String intentFilterAction="SendProgressAsBroadcast::intentFilterAction";

    public static final String broadcastToReceiveAction ="SendProgressAsBroadcast::intentExtraBroadcastToReceive";

    public static final String intentExtraBroadcastToReceiveAction ="SendProgressAsBroadcast::broadcastToReceiveAction";

    private final Context context;

    private List<Intent> intentsSent=null;

    private Intent intentInit=null;

    private boolean record;

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            INTENT_EXTRA_DATA_NAME_OF_FUNCTION_TO_START serializableExtra = (INTENT_EXTRA_DATA_NAME_OF_FUNCTION_TO_START) intent.getSerializableExtra(intentExtraBroadcastToReceiveAction);
            switch (serializableExtra) {
                case startRecording:
                    record=true;
                    break;
                case sendRenderInstantiateProgressForRendering:
                    if(intentInit!=null) LocalBroadcastManager.getInstance(context).sendBroadcast(intentInit);
                    break;
                case sendRecordedBroadcastAndStopRecording:
                    if(intentsSent!=null) for (Intent send : intentsSent) {
                        try {
                            LocalBroadcastManager.getInstance(context).sendBroadcast(send);
                        }catch (Exception e){
                            utils.LogE(e);
                        }
                    }
                    intentsSent=null;
                    record=false;
                    break;
                case close:
                    close();
                    break;
            }
        }
    };

    private LocalBroadcastManager localBroadcastManager=null;

    public SendProgressAsBroadcast(Context context,boolean registerReceiver,boolean startRecording){
        this.context=context;
        record=startRecording;
        if(registerReceiver) {
            localBroadcastManager = LocalBroadcastManager.getInstance(context);
            IntentFilter filter = new IntentFilter(broadcastToReceiveAction);
            localBroadcastManager.registerReceiver(br, filter);
        }
    }

    public SendProgressAsBroadcast(Context context,boolean registerReceiver){
        this(context,registerReceiver,false);
    }

    /**
     * Constructor of {@link SendProgressAsBroadcast}.
     * @param context Your Context, which will be used to fire the Broadcasts
     */
    public SendProgressAsBroadcast(Context context){
        this(context,false);
    }

    /**
     * Method called from {@link ProgressPreRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_PROGRESS}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_MAX_PROGRESS} and {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_FINISHED}
     */
    @Override
    public void updateProgress(int state, int max, boolean finished) {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.preRenderUpdateProgress);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_PROGRESS,state);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_MAX_PROGRESS,max);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_FINISHED,finished);
        if(record) {
            if(intentsSent==null)intentsSent=new ArrayList<>();
            intentsSent.add(intent);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Method called from {@link ProgressPreRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}and  {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE}
     */
    @Override
    public void instantiateProgressesForRendering(int num) {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.renderInstantiateProgressForRendering);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NUM_FOR_INSTANTIATE,num);
        intentInit=intent;
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Method called from {@link ProgressPreRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_PROGRESS},
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_MAX_PROGRESS} and {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_FINISHED}.
     */
    @Override
    public void updateProgressOfX(int num, int progress, int max, boolean finished) {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.renderUpdateProgressOfX);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_PROGRESS,progress);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_MAX_PROGRESS,max);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_FINISHED,finished);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE,num);
        if(record) {
            if(intentsSent==null)intentsSent=new ArrayList<>();
            intentsSent.add(intent);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Method called from {@link ProgressPreRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_PROGRESS}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_MAX_PROGRESS} and
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_FINISHED}
     */
    @Override
    public void updateProgressOfSavingVideo(int progress, int max, boolean finished) {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.lastRenderUpdateProgressOfSavingVideo);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_PROGRESS,progress);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_MAX_PROGRESS,max);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_FINISHED,finished);
        if(record) {
            if(intentsSent==null)intentsSent=new ArrayList<>();
            intentsSent.add(intent);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void close() {
        if(localBroadcastManager!=null) localBroadcastManager.unregisterReceiver(br);
    }
}