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

package de.hlvsapps.androidvideolib.implementation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import de.hlvsapps.androidvideolib.LastRenderer;
import de.hlvsapps.androidvideolib.PreRenderer;
import de.hlvsapps.androidvideolib.ProgressRender;
import de.hlvsapps.androidvideolib.utils;


/**
 * Use this class for easier handling Progress Updates of {@link ProgressRender} and {@link PreRenderer.ProgressPreRender}.
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
public class SendProgressAsBroadcast implements ProgressRender, PreRenderer.ProgressPreRender, Closeable, Parcelable {

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
        public void onReceive(Context context, @NotNull Intent intent) {
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

    private final boolean registeredReceiver;

    public SendProgressAsBroadcast(Context context,boolean registerReceiver,boolean startRecording){
        this.context=context;
        record=startRecording;
        this.registeredReceiver=registerReceiver;
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
     * Method called from {@link PreRenderer.ProgressPreRender}.<br>
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
     * Method called from {@link PreRenderer.ProgressPreRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}
     */
    @Override
    public void failed() {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.preRenderFailed);
        if(record) {
            if(intentsSent==null)intentsSent=new ArrayList<>();
            intentsSent.add(intent);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Method called from {@link ProgressRender}.<br>
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
     * Method called from {@link ProgressRender}.<br>
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
     * Method called from {@link ProgressRender}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}, {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE}.
     */
    @Override
    public void xFailed(int num) {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.xFailed);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NUM_TO_UPDATE,num);
        if(record) {
            if(intentsSent==null)intentsSent=new ArrayList<>();
            intentsSent.add(intent);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Method called from {@link LastRenderer}.<br>
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

    /**
     * Method called from {@link LastRenderer}.<br>
     * This will fire a Broadcast with your Context and intentAction, both set in {@link SendProgressAsBroadcast#SendProgressAsBroadcast(Context)}.<br>
     * This Broadcast will contain following Parameters:<br>
     * {@link SendProgressAsBroadcast#INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED}
     */
    @Override
    public void exportFailed() {
        Intent intent = new Intent();
        intent.setAction(intentFilterAction);
        intent.putExtra(INTENT_EXTRA_DATA_NAME_NAME_OF_METHOD_CALLED, INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED.lastRenderFailed);
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

    protected SendProgressAsBroadcast(@NotNull Parcel in) {
        context = (Context) in.readValue(Context.class.getClassLoader());
        if (in.readByte() == 0x01) {
            intentsSent = new ArrayList<>();
            in.readList(intentsSent, Intent.class.getClassLoader());
        } else {
            intentsSent = null;
        }
        intentInit = (Intent) in.readValue(Intent.class.getClassLoader());
        record = in.readByte() != 0x00;
        registeredReceiver = in.readByte() != 0x00;
        if(registeredReceiver) {
            localBroadcastManager = LocalBroadcastManager.getInstance(context);
            IntentFilter filter = new IntentFilter(broadcastToReceiveAction);
            localBroadcastManager.registerReceiver(br, filter);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(context);
        if (intentsSent == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(intentsSent);
        }
        dest.writeValue(intentInit);
        dest.writeByte((byte) (record ? 0x01 : 0x00));
        dest.writeByte((byte) (registeredReceiver ? 0x01 : 0x00));
    }

    public static final Parcelable.Creator<SendProgressAsBroadcast> CREATOR = new Parcelable.Creator<SendProgressAsBroadcast>() {
        @Override
        public SendProgressAsBroadcast createFromParcel(Parcel in) {
            return new SendProgressAsBroadcast(in);
        }

        @Override
        public SendProgressAsBroadcast[] newArray(int size) {
            return new SendProgressAsBroadcast[size];
        }
    };

    /**
     * Enum for possible Calling Methods.<br>
     * The following Table Shows which Member of this Enum is used for which Method:
     * <table border="1">
     * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#preRenderUpdateProgress}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgress(int, int, boolean)}.</td></tr>
     * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#renderInstantiateProgressForRendering}</td><td>for</td></td><td> {@link SendProgressAsBroadcast#instantiateProgressesForRendering(int)}.</td></tr>
     * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#renderUpdateProgressOfX}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgressOfX(int, int, int, boolean)}.</td></tr>
     * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#lastRenderUpdateProgressOfSavingVideo}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#updateProgressOfSavingVideo(int, int, boolean)}.</td></tr>
     * <tr><td>{@link INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED#xFailed}</td><td>for</td></td></td> {@link SendProgressAsBroadcast#xFailed(int)}.</td></tr>
     * </table>
     *
     * @see SendProgressAsBroadcast
     */
    public enum INTENT_EXTRA_DATA_VALUE_NAME_OF_METHOD_CALLED{
        preRenderUpdateProgress,preRenderFailed,renderInstantiateProgressForRendering,renderUpdateProgressOfX,xFailed,lastRenderUpdateProgressOfSavingVideo,lastRenderFailed
    }

    /**
     * <p>
     * Enum for calling Methods by {@link LocalBroadcastManager} of this Class, Receiver is Registered in Constructor, if (registerReceiver==true).
     * </p><p>
     * You can command the {@link SendProgressAsBroadcast} instance to record all incoming Calls by {@link INTENT_EXTRA_DATA_NAME_OF_FUNCTION_TO_START#startRecording}.
     * </p><p>
     * You can get them by {@link INTENT_EXTRA_DATA_NAME_OF_FUNCTION_TO_START#sendRecordedBroadcastAndStopRecording}.
     * </p><p>
     * The Callable Methods are:
     * </p><p>
     * {@link SendProgressAsBroadcast#instantiateProgressesForRendering(int)}
     * </p><p>
     * {@link SendProgressAsBroadcast#close()}
     * </p>
     */
    public enum INTENT_EXTRA_DATA_NAME_OF_FUNCTION_TO_START {
        sendRenderInstantiateProgressForRendering,startRecording,sendRecordedBroadcastAndStopRecording,close
    }
}
