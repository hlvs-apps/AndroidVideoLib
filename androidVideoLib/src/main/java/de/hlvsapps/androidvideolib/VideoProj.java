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

package de.hlvsapps.androidvideolib;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.apache.commons.lang3.SerializationUtils;
import org.jcodec.common.model.Picture;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static android.content.Context.POWER_SERVICE;

/**
 * The Main Class of AndroidVideoLib.
 * A VideoProj contains all Data required to Render a Video, and is used to start Rendering a Video.
 * A Input is Defined as VideoPart, a VideoProj can contain multiple VideoParts
 *
 * Before Rendering, you have to call {@link VideoProj#preRender(Runnable)} to save the Images of all Videos in your Apps Storage.
 * To Render call {@link VideoProj#renderInTo(String)}, {@link VideoProj#renderInTo(String, ProgressRender)}, {@link VideoProj#render(ProgressRender)} or {@link VideoProj#render()} to Render your Video to a specified Output.
 *
 * @author hlvs-apps
 */
public class VideoProj implements Serializable {
    private static final long serialVersionUID = 42L;
    // Custom deserialization logic
    // This will allow us to have additional deserialization logic on top of the default one e.g. decrypting object after deserialization
    private void readObject(@NotNull ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject(); // Calling the default deserialization logic
        WAKE_LOCK_ID=utils.getApplicationName(context)+END_OF_WAKE_LOCK_ID;
        utils.setVideoFolderName(videoFolderName);
        updateRenderTimeLine();
    }
    public static final String CHANNEL_ID = "CHANNEL_ID_RENDER";
    public static final String DATA_ID_RENDERER="DATA_ID_RENDERER";
    public static final String DATA_ID_RENDERER_START="DATA_ID_RENDERER_START";
    public static final String DATA_ID_RENDERER_END="DATA_ID_RENDERER_END";
    public static final String END_OF_WAKE_LOCK_ID="::RenderLock";
    transient public static String WAKE_LOCK_ID ;

    public static final int NOTIFICATION_ID =1034;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;

    List<String> [] inputs_from_last_render;
    Picture pic0=null;

    private String output;
    private List<VideoPart> input;
    private final AppCompatActivity context;

    private Class renderActivity;
    private PowerManager.WakeLock wakeLock;
    transient private int length;
    transient private double length_seconds;

    transient private List<UriIdentifier> allVideoUris;

    private List<UriIdentifier> manualVideoUris;

    private String videoFolderName;

    transient private List<RenderTaskWrapperWithUriIdentifierPairs> renderTasksWithMatchingUriIdentifierPairs;

    private Rational fps=null;

    private BigDecimal scaleFactor=BigDecimal.valueOf(1);

    private boolean[] which_task_finished;

    private RendererTimeLine rendererTimeLine;

    /**
     * Sets the Scale Factor for preRendering.
     * Default 1.
     *
     * @param scaleFactor The scale Factor
     */
    public void setScaleFactor(BigDecimal scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    /**
     * Gets the Scale Factor for preRendering
     * @return the Scale Factor for preRendering
     */
    public BigDecimal getScaleFactor() {
        return scaleFactor;
    }

    Picture getPic0() {
        utils.LogD("Return Pic0. Pic0 is "+((pic0==null)?"null":"not null"));
        return pic0;
    }

    void setPic0(Picture pic0) {
        this.pic0 = pic0;
        utils.LogD("New Pic0. Pic0 is "+((pic0==null)?"null":"not null"));
    }

    /**
     * Sets the Activity class to show render Progress.
     * @param a The Render Activity Class
     */
    public void setShowRenderProgressActivity(Class a){
        renderActivity=a;
    }

    /**
     * Constructor of {@link VideoProj}.
     * @param outputname The Name of the Output of the Video.
     * @param input The VideoParts with the Videos
     * @param fps Output FPS
     * @param context Your Context
     */
    public VideoProj(String outputname, List<VideoPart> input, Rational fps, AppCompatActivity context){
        this.output=outputname;
        this.input=input;
        this.fps=fps;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        this.videoFolderName=utils.getApplicationName(context)+"-Video";
        renderActivity= context.getClass();
        WAKE_LOCK_ID=utils.getApplicationName(context)+END_OF_WAKE_LOCK_ID;
        utils.setVideoFolderName(videoFolderName);
        updateRenderTimeLine();
    }

    /**
     * Constructor of {@link VideoProj}.
     * @param outputname The Name of the Output of the Video.
     * @param input The VideoParts with the Videos
     * @param context Your Context
     */
    public VideoProj(String outputname, List<VideoPart> input, AppCompatActivity context){
        this.output=outputname;
        this.input=input;
        this.fps=null;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        this.videoFolderName=utils.getApplicationName(context)+"-Video";
        renderActivity= context.getClass();
        WAKE_LOCK_ID=utils.getApplicationName(context)+END_OF_WAKE_LOCK_ID;
        utils.setVideoFolderName(videoFolderName);
        updateRenderTimeLine();
    }

    /**
     * Constructor of {@link VideoProj}.
     * @param input The VideoParts with the Videos
     * @param fps Output FPS
     * @param context Your Context
     */
    public VideoProj(List<VideoPart> input,Rational fps, AppCompatActivity context){
        this.input=input;
        this.fps=fps;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        this.videoFolderName=utils.getApplicationName(context)+"-Video";
        renderActivity= context.getClass();
        WAKE_LOCK_ID=utils.getApplicationName(context)+END_OF_WAKE_LOCK_ID;
        utils.setVideoFolderName(videoFolderName);
        updateRenderTimeLine();
    }

    /**
     * Constructor of {@link VideoProj}.
     * @param input The VideoParts with the Videos
     * @param context Your Context
     */
    public VideoProj(List<VideoPart> input, AppCompatActivity context){
        this.input=input;
        this.fps=null;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        this.videoFolderName=utils.getApplicationName(context)+"-Video";
        renderActivity= context.getClass();
        WAKE_LOCK_ID=utils.getApplicationName(context)+END_OF_WAKE_LOCK_ID;
        utils.setVideoFolderName(videoFolderName);
        updateRenderTimeLine();
    }

    /**
     * Sets the output folder for your rendered video in gallery
     * @param name The name of the folder
     */
    public void setVideoFolderName(String name){
        videoFolderName=name;
        utils.setVideoFolderName(videoFolderName);
    }

    /**
     * Get all VideoParts set as Input.
     * When you want to make Changes, please set the new List with {@link VideoProj#setInput(List)}, OR call {@link VideoProj#updateRenderTimeLine()}, otherwise Changes will have no Effect.
     * @return all VideoParts
     */
    public List<VideoPart> getInput() {
        return input;
    }

    /**
     * Sets all Inputs, and Imports them
     * @param input All Inputs
     */
    public void setInput(List<VideoPart> input) {
        this.input = input;
        updateRenderTimeLine();
    }

    /**
     * Get the output folder for your rendered video in gallery
     * @return output folder for your rendered video in gallery
     */
    public String getVideoFolderName() {
        return videoFolderName;
    }

    /**
     * Get FPS of first clip
     * @return tge fps of the first clip as {@link Rational}.
     */
    public Rational getFpsOfFirstClip(){
        return new Rational(rendererTimeLine.getUriIdentifierPairs().get(0).getLengthInFrames(), (int) rendererTimeLine.getUriIdentifierPairs().get(0).getLengthInSeconds());
    }

    /**
     * Sets FPS of whole Project
     * @param fps FPS as {@link Rational}
     */
    public void setFps(Rational fps) {
        this.fps = fps;
    }

    /**
     * Gets FPS of whole Project
     * @return FPS of whole Project as {@link Rational}
     */
    public Rational getFps() {
        return fps;
    }

    /**
     * Adds a VideoPart to the Project
     * Don't forget to call {@link VideoProj#preRender(Runnable)} before Rendering
     * @param input The Video Part you want to add
     */
    public void addInput(VideoPart input){
        this.input.add(input);
        this.rendererTimeLine.addAllFromVideoPart(input);
        updateRenderTimeLine();
    }

    /**
     * Adds  VideoParts to the Project
     * Don't forget to call {@link VideoProj#preRender(Runnable)} before Rendering
     * @param inputs The Video Parts you want to add
     */
    public void addAllInput(List<VideoPart> inputs){
        this.input.addAll(inputs);
        this.rendererTimeLine.addAllParts(inputs);
        updateRenderTimeLine();
    }

    void setWakeLock(PowerManager.WakeLock wakeLock) {
        this.wakeLock = wakeLock;
    }

    /**
     * Get your Context
     * @return your Context
     */
    public AppCompatActivity getContext() {
        return context;
    }

    /**
     * Delete all Cache for a {@link UriIdentifier}
     * This Works by getting the UriIdentifiers Name and call {@link utils#deleteAllVideoCacheContainingName(Context, String)} with the UriIdentifiers identifier.
     * @param uriIdentifier The UriIdentifier
     * @see utils#deleteAllVideoCacheContainingName(Context, String)
     */
    public void deleteAllCacheForUriIdentifier(UriIdentifier uriIdentifier){
        utils.deleteAllVideoCacheContainingName(context,uriIdentifier.getIdentifier());
    }


    /**
     * Updates the {@link RendererTimeLine} of the Project.
     * Call this when you changed the inputs
     */
    public void updateRenderTimeLine(){
        length=rendererTimeLine.getVideoLengthInFrames(this);
        length_seconds=rendererTimeLine.getVideoLengthInSeconds(this);
        renderTasksWithMatchingUriIdentifierPairs = rendererTimeLine.getRenderTasksWithMatchingUriIdentifierPairs(this);
        allVideoUris=new ArrayList<>();
        allVideoUris.addAll(rendererTimeLine.getAllUrisFromUriIdentifiers());
        if(manualVideoUris!=null)allVideoUris.addAll(manualVideoUris);
        allVideoUris= new ArrayList<>(new HashSet<>(allVideoUris));
    }

    /**
     * Adds a {@link UriIdentifier} to Uri Sources that is not contained in a {@link VideoSegment}, and so can not be rendered but you want to have stored.
     * The Lists of UriIdentifiers of this Project can not contain Duplicates.
     *
     * When the UriIdentifier is in a {@link VideoSegment} you add with a {@link VideoPart}, this UriIdentifier will be added automatically to {@link VideoProj#allVideoUris}.
     *
     * You can get the List with {@link VideoProj#getAllVideoUris()}
     * Or when you only want manually added Uris with {@link VideoProj#getManualVideoUris()}.
     * @param i The UriIdentifier you want to add.
     */
    public void addUriIdentifierWithoutSegment(UriIdentifier i){
        if(allVideoUris==null)allVideoUris=new ArrayList<>();
        if(manualVideoUris==null)manualVideoUris.add(i);
        allVideoUris.add(i);
        allVideoUris= new ArrayList<>(new HashSet<>(allVideoUris));
        manualVideoUris= new ArrayList<>(new HashSet<>(manualVideoUris));
    }

    /**
     * Get all {@link UriIdentifier} contained in that Project, added automatically, and manually with {@link VideoProj#addUriIdentifierWithoutSegment(UriIdentifier)}.
     * @return A List with a Copy of {@link VideoProj#allVideoUris} list.
     */
    public List<UriIdentifier> getAllVideoUris() {
        return new ArrayList<>(allVideoUris);
    }

    /**
     * Get all {@link UriIdentifier} added manually with {@link VideoProj#addUriIdentifierWithoutSegment(UriIdentifier)}.
     * @return {@link VideoProj#manualVideoUris}
     */
    public List<UriIdentifier> getManualVideoUris(){
        return manualVideoUris;
    }

    /**
     * Extracts all Videos into Images in external files dir, to provide a better Rendering
     * Please Call this before you Render!
     */
    public void preRender(){
        preRender(() -> utils.LogI("Rendering finished"));
    }

    /**
     * Extracts all Videos into Images in external files dir, to provide a better Rendering
     * Please Call this before you Render!
     *
     * @param onFinish Runnable to Execute when preRendering finished
     */
    public void preRender(Runnable onFinish){
        preRender(onFinish,null);
    }

    /**
     * Extracts all Videos into Images in external files dir, to provide a better Rendering
     * Please Call this before you Render!
     *
     * @param onProgress On Do Progress
     */
    public void preRender(ProgressPreRender onProgress){
        preRender(() -> utils.LogI("Rendering finished"),onProgress);
    }

    /**
     * Extracts all Videos into Images in external files dir, to provide a better Rendering
     * Please Call this before you Render!
     *
     * @param onFinish Runnable to Execute when preRendering finished
     * @param onProgress On Do Progress
     */
    public void preRender(Runnable onFinish, ProgressPreRender onProgress){
        askForBackgroundPermissions();
        /*
        Intent intent = new Intent(context, renderActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle("Importing")
                .setContentText("Importing in progress")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        createNotificationChannel();
        // Issue the initial notification with zero progress

        builder.setProgress(100, 1, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());*/

    // Do the job here that tracks the progress.

        Constraints constraints;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build();
        }else{
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .build();
        }

        OneTimeWorkRequest renderRequest =new OneTimeWorkRequest.Builder(PreRenderer.class)
                .setConstraints(constraints)
                .setInputData((new Data.Builder())
                        .putByteArray(PreRenderer.parcelableByteArrayListUriIdentifierPair,utils.marshall(UriIdentifierPair.UriIdentifierPairList.from(rendererTimeLine.getUriIdentifierPairs())))
                        .putByteArray(PreRenderer.serializableByteArrayScaleFactor, SerializationUtils.serialize(scaleFactor))
                        .build())
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork("Import"+toString(),ExistingWorkPolicy.REPLACE,renderRequest);
        if(onFinish==null){
            onFinish= () -> {
            };
        }
        if(onProgress!=null) {
            final Runnable finalOnFinish = onFinish;
            WorkManager.getInstance(context.getApplicationContext())
                    .getWorkInfoByIdLiveData(renderRequest.getId())
                    .observeForever(new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null) {
                                Data progressD = workInfo.getProgress();
                                int progress = progressD.getInt(ProgressPreRender.progressPreRenderState, -11);
                                int max = progressD.getInt(ProgressPreRender.progressPreRenderMax, -11);
                                boolean finished = progressD.getBoolean(ProgressPreRender.progressPreRenderMax, false);
                                if (progress != -11 && max != -11) {
                                    onProgress.updateProgress(progress,max,finished);
                                }
                                if (workInfo.getState().isFinished()) {
                                    finalOnFinish.run();
                                    WorkManager.getInstance(context.getApplicationContext()).getWorkInfoByIdLiveData(renderRequest.getId()).removeObserver(this);
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Gets the {@link RendererTimeLine} Line for all {@link VideoProj}s
     * You usually don't have to use this
     * @return The Rendered Time Line
     */
    public RendererTimeLine getRendererTimeLine() {
        return rendererTimeLine;
    }


    /**
     * Ask for Background Execution Permissions. Do call this before Rendering
     */
    public void askForBackgroundPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);//Why? Otherwise the App is not able to Render when Screen is off
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
        //TODO add if
        activity_utils.startPowerSaverIntent(context);
    }


    /**
     * Render this Project to a Output String.
     * Uses {@link VideoProj#renderInTo(String, ProgressRender)} with null for progressRender.
     * Please don't forget to set the FPS with {@link VideoProj#setFps(Rational)}, otherwise we cant Render the Project.
     *
     * @param output The Output File Name as String. New Output of this VideoProj will become that.
     * @throws IllegalStateException When FPS is null
     */
    public void renderInTo(String output){
        renderInTo(output,null);
    }

    /**
     * Render this Project to a Output String
     * Please don't forget to set the FPS with {@link VideoProj#setFps(Rational)}, otherwise we cant Render the Project.
     *
     * @param output The Output File Name as String. New Output of this VideoProj will become that.
     * @param progressRender Progress of Rendering
     * @throws IllegalStateException When FPS is null
     */
    public void renderInTo(String output,ProgressRender progressRender) throws IllegalStateException{
        pic0=utils.getPictureFromUriIdentifierPairs(rendererTimeLine.getUriIdentifierPairs(),context);
        this.output=output;
        if(fps==null){
            throw new IllegalStateException("FPS can not be null");
        }
        PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
        setWakeLock(powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_ID));
        getWakeLock().acquire(/*100*60*1000L /*100 minutes*/);

        //askForBackgroundPermissions();

        Intent intent = new Intent(context, renderActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle("Rendering")
                .setContentText("Rendering in progress")
                //TODO update
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        createNotificationChannel();

// Issue the initial notification with zero progress

        builder.setProgress(100, 1, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

// Do the job here that tracks the progress.

        Renderer.proj=this;
        Constraints constraints;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .setRequiresDeviceIdle(false)
                    .build();
        }else{
            constraints = new Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiresBatteryNotLow(false)
                    .build();
        }

        int num_of_workers=Runtime.getRuntime().availableProcessors()*2+4;
        Renderer.progressRender=progressRender;
        LastRenderer.progressRender=progressRender;
        int frames_per_worker= (int) (Math.floor((getLength()*1D)/num_of_workers)-1);
        if(frames_per_worker<=0) {
            num_of_workers=num_of_workers/4;
            if(num_of_workers<=0)num_of_workers=1;
            frames_per_worker=(int) (Math.floor((getLength()*1D)/num_of_workers)-1);
        }
        inputs_from_last_render = new List[num_of_workers];
        which_task_finished=new boolean[num_of_workers];
        if(progressRender!=null)progressRender.instantiateProgressesForRendering(num_of_workers);
        if(frames_per_worker<0) {
            Data.Builder b = new Data.Builder();
            b.putInt(DATA_ID_RENDERER, 0);
            b.putInt(DATA_ID_RENDERER_START, 0);
            b.putInt(DATA_ID_RENDERER_END, -1);
            OneTimeWorkRequest renderRequest = new OneTimeWorkRequest.Builder(Renderer.class)
                    .setConstraints(constraints)
                    .setInputData(b.build())
                    .build();
            WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork("Render0", ExistingWorkPolicy.REPLACE, renderRequest);
        }else {
            for (int i = 0; i < num_of_workers; i++) {
                Data.Builder b = new Data.Builder();
                b.putInt(DATA_ID_RENDERER, i);
                b.putInt(DATA_ID_RENDERER_START, i * frames_per_worker);
                b.putInt(DATA_ID_RENDERER_END, (i + 1) == num_of_workers ? -1 : (i + 1) * frames_per_worker);
                OneTimeWorkRequest renderRequest = new OneTimeWorkRequest.Builder(Renderer.class)
                        .setConstraints(constraints)
                        .setInputData(b.build())
                        .build();
                WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork("Render" + i, ExistingWorkPolicy.REPLACE, renderRequest);
            }
        }
    }

    synchronized void workFailed(int which_renderer){
        which_task_finished[which_renderer]=true;
        if(utils.areAllTrue(which_task_finished)) getWakeLock().release();
    }

    synchronized void startLastRender(int which_renderer){
        which_task_finished[which_renderer]=true;
        LastRenderer.proj=this;
        if(utils.areAllTrue(which_task_finished)){
            try {
                getWakeLock().release();
            }catch (Exception e){
                utils.LogE("Ignore this",e);
            }

            Renderer.proj=this;
            Constraints constraints;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                constraints = new Constraints.Builder()
                        .setRequiresCharging(false)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresDeviceIdle(false)
                        .build();
            }else{
                constraints = new Constraints.Builder()
                        .setRequiresCharging(false)
                        .setRequiresBatteryNotLow(false)
                        .build();
            }

            OneTimeWorkRequest renderRequest = new OneTimeWorkRequest.Builder(LastRenderer.class)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork("Render", ExistingWorkPolicy.REPLACE, renderRequest);
        }
    }

    /**
     * @return Length In Frames
     */
    public int getLength() {
        return length;
    }

    /**
     * @return Length in Seconds
     */
    public double getLength_seconds() {
        return length_seconds;
    }

    void setNotificationProgress(int max, int progress, boolean finsih){
        if(!finsih){
            builder.setProgress(max, progress, false);
        }else{
            builder.setContentText("Rendering complete")
                    .setProgress(0,0,false);
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    PowerManager.WakeLock getWakeLock() {
        return wakeLock;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Render Channel";
            String description = "Render Video";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * Render a Frame at a Given Position. You must call <code>VideoProj#preRender</code> before. Because you can return Multiple Bitmaps in a {@link RenderTask}, this returns a List.
     * @param position The Position of the Frame you want to have
     * @return Returns a List of Bitmaps, and if no Renderer was found for the Task, it returns null.
     */
    public List<Bitmap> getFrameAtPosition(int position){
        for (RenderTaskWrapperWithUriIdentifierPairs wrapper: renderTasksWithMatchingUriIdentifierPairs){
            int to=wrapper.getFrameInProjectTo();
            if(to==-1)to=getLength();
            if(position>=wrapper.getFrameInProjectFrom() && position<=to){
                List<VideoBitmap> bitmap0=new ArrayList<>();
                List<VideoBitmap> bitmap1=new ArrayList<>();
                for(UriIdentifierPair p:wrapper.getMatchingUriIdentifierPairs()){
                    String fileName=p.getUriIdentifier().getIdentifier();
                    int i_for_video=position-p.getFrameStartInProject();
                    bitmap0.add(new VideoBitmap(
                            utils.readFromExternalStorage(getContext(),fileName+i_for_video),p.getUriIdentifier()));
                    utils.LogD(fileName+i_for_video);
                    i_for_video++;
                    if((position+1)<=to) {
                        bitmap1.add(new VideoBitmap(
                                utils.readFromExternalStorage(getContext(), fileName + i_for_video), p.getUriIdentifier()));
                        utils.LogD(fileName + i_for_video);
                    }else{
                        bitmap1.add(new VideoBitmap(
                                null,p.getUriIdentifier()
                        ));
                        utils.LogD(fileName + i_for_video+" not added because it should not exist");
                    }
                }
                return wrapper.getRenderTask().render(bitmap0, bitmap1, position);
            }
        }
        return null;
    }

    /**
     * Gets a Frame from a specified {@link UriIdentifier} at given position.
     * @param context Your Context
     * @param identifier The {@link UriIdentifier}
     * @param position The Position
     * @return The Frame at given position as {@link Bitmap}.
     */
    public static Bitmap getFrameFromUriIdentifierAtPosition(Context context,UriIdentifier identifier,int position){
        final String fileName=identifier.getIdentifier()+position;
        return utils.readFromExternalStorage(context,fileName);
    }

    List<RenderTaskWrapperWithUriIdentifierPairs> getRenderTasksWithMatchingUriIdentifierPairs() {
        return renderTasksWithMatchingUriIdentifierPairs;
    }

    /**
     * Gets a Frame from a specified {@link UriIdentifier} at given position.
     * @param identifier The {@link UriIdentifier}
     * @param position The Position
     * @return The Frame at given position as {@link Bitmap}.
     *
     * @see VideoProj#getFrameFromUriIdentifierAtPosition(Context, UriIdentifier, int)
     */
    public Bitmap getFrameFromUriIdentifierAtPosition(UriIdentifier identifier,int position){
        return getFrameFromUriIdentifierAtPosition(context,identifier,position);
    }


    /**
     * Gets the Output File Name
     * @return Output File Name
     */
    public String getOutputString(){
        return this.output;
    }

    /**
     * Set a new Output File Name
     * @param output the Output File Name
     */
    public void setOutput(String output){
        this.output=output;
    }

    /**
     * Rendering with showing Progress in {@link ProgressActivity}
     * @param output The output forwarded to {@link VideoProj#renderInTo(String)}
     */
    public void startRenderActivityAndRenderInTo(String output){
        setShowRenderProgressActivity(ProgressActivity.class);
        context.startActivity(new Intent(context,ProgressActivity.class));
        renderInTo(output,new SendProgressAsBroadcast(context));
    }

    /**
     * Rendering with showing Progress in {@link ProgressActivity}
     */
    public void startRenderActivityAndRenderInTo(){
        setShowRenderProgressActivity(ProgressActivity.class);
        context.startActivity(new Intent(context,ProgressActivity.class));
        render(new SendProgressAsBroadcast(context,true,true));
    }

    /**
     * Renders the VideoProj.
     * Calls {@link VideoProj#renderInTo(String)} with output Name, or when null or "" with your AppsName and the actual date.
     */
    public void render(){
        if(output==null || output.equals("")){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            output=utils.getApplicationName(context)+"_VideoExport "+formatter.format(now);
        }
        renderInTo(output);
    }

    /**
     * Renders the VideoProj.
     * Calls {@link VideoProj#renderInTo(String,ProgressRender)} with output Name, or when null or "" with your AppsName and the actual date.
     * @param progressRender The {@link ProgressRender} to call {@link VideoProj#renderInTo(String, ProgressRender)}
     */
    public void render(ProgressRender progressRender){
        if(output==null || output.equals("")){
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            output=utils.getApplicationName(context)+"_VideoExport "+formatter.format(now);
        }
        renderInTo(output,progressRender);
    }
}
