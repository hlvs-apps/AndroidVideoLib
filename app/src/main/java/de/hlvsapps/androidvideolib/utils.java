/*-----------------------------------------------------------------------------
 - Copyright 2021 hlvs-apps                                                   -
 - This is a part of AndroidVideoLib.                                         -
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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;

import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class utils {

    //CONSTANTS
    private static String VIDEO_FOLDER_NAME="AndroidVideoLib-Video";

     static final boolean BUILDCONFIGDEBUG=BuildConfig.DEBUG;

     static void setVideoFolderName(String videoFolderName) {
        VIDEO_FOLDER_NAME = videoFolderName;
    }

     static String getVideoFolderName() {
        return VIDEO_FOLDER_NAME;
    }

     static void saveToExternalStorage(Bitmap bitmapImage, Context c, String name){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            directory=new File(cw.getNoBackupFilesDir(),"imageCacheDirVideoExport");
            if(!directory.exists()){
                directory.mkdirs();
            }
        }else{
            directory = cw.getDir("imageCacheDirVideoExport",Context.MODE_PRIVATE);
        }
        // Create imageDir
        File mypath=new File(directory,name);
        try (FileOutputStream fos= new FileOutputStream(mypath)){
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            LogE(e);
        }
    }

     static boolean areAllTrue(boolean... array)
    {
        for(boolean b : array) if(!b) return false;
        return true;
    }

     static void saveToExternalExportStorage(Bitmap bitmapImage, Context c, String name){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
         File directory;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             directory=new File(cw.getNoBackupFilesDir(),"imageCacheExportDirVideoExport");
             if(!directory.exists()){
                 directory.mkdirs();
             }
         }else{
             directory = cw.getDir("imageCacheExportDirVideoExport",Context.MODE_PRIVATE);
         }
        // Create imageDir
        File mypath=new File(directory,name);
        try (FileOutputStream fos= new FileOutputStream(mypath)){
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            LogE(e);
        }
    }

     static Bitmap readFromExternalExportStorageAndDelete(Context c, String name){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
         File directory;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             directory=new File(cw.getNoBackupFilesDir(),"imageCacheExportDirVideoExport");
             if(!directory.exists()){
                 directory.mkdirs();
             }
         }else{
             directory = cw.getDir("imageCacheExportDirVideoExport",Context.MODE_PRIVATE);
         }
        File f=new File(directory, name);
        if(f.exists()) {
            Bitmap d=BitmapFactory.decodeFile(f.getPath());
            LogD(String.valueOf(f.delete()));
            return d;
        }else{
            return null;
        }
    }

     static Bitmap readFromExternalStorage(Context c, String name){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
         File directory;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             directory=new File(cw.getNoBackupFilesDir(),"imageCacheDirVideoExport");
             if(!directory.exists()){
                 directory.mkdirs();
             }
         }else{
             directory = cw.getDir("imageCacheDirVideoExport",Context.MODE_PRIVATE);
         }
        File f=new File(directory, name);
        if(f.exists()) {
            utils.LogD(name+" does exist");
            return BitmapFactory.decodeFile(f.getPath());
        }else{
            utils.LogD(name+" does not exist");
            return null;
        }
    }

     static int getMP4LengthInFrames(VideoProj proj,Uri src) throws IOException {
        ContentResolver resolver = proj.getContext().getApplicationContext().getContentResolver();
        FileChannelWrapper ch = new FileChannelWrapper((new FileInputStream(resolver.openFileDescriptor(src, "r").getFileDescriptor())).getChannel());
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
        DemuxerTrack video_track = demuxer.getVideoTrack();
        int length = video_track.getMeta().getTotalFrames();
        ch.close();
        return length;
    }

     public static double getMP4LengthInSeconds(VideoProj proj,Uri src) throws IOException {
        ContentResolver resolver = proj.getContext().getApplicationContext().getContentResolver();
        FileChannelWrapper ch = new FileChannelWrapper((new FileInputStream(resolver.openFileDescriptor(src, "r").getFileDescriptor())).getChannel());
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
        DemuxerTrack video_track = demuxer.getVideoTrack();
        double length = video_track.getMeta().getTotalDuration();
        ch.close();
        return length;
    }


    static List<Object> fileOutputStreamFromName(Context c, String name) throws FileNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = c.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + VIDEO_FOLDER_NAME);
            Uri video = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            LogI(video.toString());
            ParcelFileDescriptor pc=resolver.openFileDescriptor(video,"rw");
            FileDescriptor d=pc.getFileDescriptor();
            List<Object> re=new ArrayList<>();
            re.add(new FileOutputStream(d));
            re.add(pc);
            return re;
            //fos = resolver.openOutputStream(imageUri);
        } else {
            String imagesDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM).toString() + File.separator + VIDEO_FOLDER_NAME;

            File file = new File(imagesDir);

            if (!file.exists()) {
                file.mkdir();
            }

            File video = new File(imagesDir, name + ".mp4");
            LogI(video.getAbsolutePath());
            List<Object> re=new ArrayList<>();
            re.add(new FileOutputStream(video));
            re.add(null);
            return re;
        }
    }

    //Parts From https://stackoverflow.com/questions/11229219/android-how-to-get-application-name-not-package-name
    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String name= stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        return name.replaceAll(" ", "_");
    }




    public static void LogI(String msg) {
        if (msg==null){
            msg="null";
        }
        if (BUILDCONFIGDEBUG) {

            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.
            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";
            Log.i(info, msg);
        }
    }


    public static void LogD(String msg) {
        if (msg==null){
            msg="null";
        }
        if (BUILDCONFIGDEBUG) {

            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.

            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";

            Log.d(info, msg);
        }
    }


    public static void LogE(String msg, final Throwable tr) {
        if (msg==null){
            msg="null";
        }
        if (BUILDCONFIGDEBUG) {

            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.

            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";

            Log.e(info, msg, tr);

        }
    }
    public static void LogE(final Throwable tr) {
        if (BUILDCONFIGDEBUG) {

            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.

            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";

            Log.e(info, "", tr);

        }
    }
    public static void LogE(String msg) {
        if (msg==null){
            msg="null";
        }
        if (BUILDCONFIGDEBUG) {

            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.

            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";

            Log.e(info, msg);
        }
    }
    public static void LogW(String msg) {
        if (msg==null){
            msg="null";
        }
        if (BUILDCONFIGDEBUG) {
            final StackTraceElement stackTrace = new Exception().getStackTrace()[1];

            String fileName = stackTrace.getFileName();
            if (fileName == null)
                fileName = "";  // It is necessary if you want to use proguard obfuscation.

            final String info = stackTrace.getMethodName() + " (" + fileName + ":"
                    + stackTrace.getLineNumber() + ")";
            Log.w(info, msg);
        }
    }
}
