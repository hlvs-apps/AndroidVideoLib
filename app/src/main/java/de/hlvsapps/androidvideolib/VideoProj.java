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
import androidx.work.WorkManager;

import java.util.List;

public class VideoProj {
    public static final String CHANNEL_ID = "CHANNEL_ID_RENDER";
    public static final String DATA_ID_RENDERER="DATA_ID_RENDERER";
    //TODO CHANGE
    public static final String WAKE_LOCK_ID = "AndroidVideoLib::RenderLock";

    public static final int NOTIFICATION_ID =1034;

    NotificationManagerCompat notificationManager;
    NotificationCompat.Builder builder;

    List<String> [] inputs_from_last_render;
    private String output;
    private List<VideoPart> input;
    private final AppCompatActivity context;
    private PowerManager.WakeLock wakeLock;
    private int length;

    private Rational fps;

    private boolean[] which_task_finished;

    private RendererTimeLine rendererTimeLine;

    public VideoProj(String outputname, List<VideoPart> input, Rational fps, AppCompatActivity context){
        this.output=outputname;
        this.input=input;
        this.fps=fps;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        updateRenderTimeLine();
    }

    public VideoProj(List<VideoPart> input,Rational fps, AppCompatActivity context){
        this.input=input;
        this.fps=fps;
        this.context=context;
        this.rendererTimeLine=new RendererTimeLine();
        this.rendererTimeLine.addAllParts(this.input);
        updateRenderTimeLine();
    }

    public void setFps(Rational fps) {
        this.fps = fps;
    }

    public Rational getFps() {
        return fps;
    }

    public void addInput(VideoPart input){
        this.input.add(input);
        this.rendererTimeLine.addAllFromVideoPart(input);
        updateRenderTimeLine();
    }
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

    public AppCompatActivity getContext() {
        return context;
    }


    private void updateRenderTimeLine(){
        length=rendererTimeLine.getVideoLengthInFrames(this);
    }

    /**
     * Extracts all Videos into Images in external files dir, to provide a better Rendering
     * Please Call this before you Render!
     */
    public void preRender(){
        askForBackgroundPermissions();
        Intent intent = new Intent(context, context.getClass());
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

    public RendererTimeLine getRendererTimeLine() {
        return rendererTimeLine;
    }

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
     * Render this Project to a Output String
     * Please use <pre>{@code setNotificationProgress(max, progress1, progress1 ==-1);}</pre> to update Status progress.
     *
     * @param output The Output File Name as String. New Output of this Proj will become that.
     *
     */
    public void renderInTo(String output){
        this.output=output;

        askForBackgroundPermissions();

        Intent intent = new Intent(context, context.getClass());
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

    public int getLength() {
        return length;
    }

    void setNotificationProgress(int max, int progress, boolean finsih){
        if(!finsih){
            builder.setProgress(max, progress, false);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }else{
            builder.setContentText("Rendering complete")
                    .setProgress(0,0,false);
            notificationManager.notify(NOTIFICATION_ID, builder.build());

        }
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


    public String getOutputString(){
        return this.output;
    }

    public void setOutput(String output){
        this.output=output;
    }

    public void render(){
        renderInTo(output);
    }
}
