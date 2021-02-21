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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.jcodec.common.model.Picture;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

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
public class VideoProj {
    public static final String CHANNEL_ID = "CHANNEL_ID_RENDER";
    public static final String DATA_ID_RENDERER="DATA_ID_RENDERER";
    public static final String END_OF_WAKE_LOCK_ID="::RenderLock";
    public static String WAKE_LOCK_ID ;

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
    private int length;
    private double length_seconds;

    private String videoFolderName;

    private Rational fps=null;

    private boolean[] which_task_finished;

    private RendererTimeLine rendererTimeLine;

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

    List<UriIdentifierPair> getAllUriIdentifierPairsFromInput(){
        return rendererTimeLine.getUriIdentifierPairs();
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


    private void updateRenderTimeLine(){
        length=rendererTimeLine.getVideoLengthInFrames(this);
        length_seconds=rendererTimeLine.getVideoLengthInSeconds(this);
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
        PreRenderer.progressPreRender=onProgress;
        askForBackgroundPermissions();
        Intent intent = new Intent(context, renderActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle("Rendering")
                .setContentText("Rendering in progress")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        createNotificationChannel();
        // Issue the initial notification with zero progress

        builder.setProgress(100, 1, true);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    // Do the job here that tracks the progress.

        PreRenderer.proj=this;
        PreRenderer.whatDoAfter=onFinish;
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
                .build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork("Render",ExistingWorkPolicy.REPLACE,renderRequest);
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
     * Ask for Background Execution Permissions.
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
        this.output=output;

        if(fps==null){
            throw new IllegalStateException("FPS can not be null");
        }


        //askForBackgroundPermissions();

        Intent intent = new Intent(context, renderActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
        builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID);
        builder.setContentTitle("Rendering")
                .setContentText("Rendering in progress")
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

        int i=0;
        List<RenderTaskWrapperWithUriIdentifierPairs> list=rendererTimeLine.getRenderTasksWithMatchingUriIdentifierPairs(this);
        inputs_from_last_render = new List[list.size()];
        which_task_finished=new boolean[list.size()];
        progressRender.instantiateProgressesForRendering(list.size());
        Renderer.progressRender=progressRender;
        LastRenderer.progressRender=progressRender;
        for(RenderTaskWrapperWithUriIdentifierPairs ignored:list) {
            Data.Builder b = new Data.Builder();
            b.putInt(DATA_ID_RENDERER, i);
            OneTimeWorkRequest renderRequest = new OneTimeWorkRequest.Builder(Renderer.class)
                    .setConstraints(constraints)
                    .setInputData(b.build())
                    .build();
            WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork("Render"+i, ExistingWorkPolicy.REPLACE, renderRequest);
            i++;
        }
    }

    void startLastRender(int which_renderer){
        which_task_finished[which_renderer]=true;
        LastRenderer.proj=this;
        if(utils.areAllTrue(which_task_finished)){
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
        render(new SendProgressAsBroadcast(context));
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
