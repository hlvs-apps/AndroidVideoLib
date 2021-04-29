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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.PictureWithMetadata;
import org.jcodec.common.DemuxerTrack;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.Yuv420pToRgb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Utils class of this AndroidVideoLib
 * @author hlvs-apps
 */
public class utils {

    //CONSTANTS
    private static String VIDEO_FOLDER_NAME="AndroidVideoLib-Video";

     static final boolean BUILDCONFIGDEBUG=BuildConfig.DEBUG;

     static void setVideoFolderName(String videoFolderName) {
        VIDEO_FOLDER_NAME = videoFolderName;
    }

     public static String getVideoFolderName() {
        return VIDEO_FOLDER_NAME;
    }

     public static void saveToExternalStorage(Bitmap bitmapImage, Context c, String name){
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
    public static boolean areAllTrue(boolean... array) {
        for(boolean b : array) if(!b) return false;
        return true;
    }

    public static boolean isOneTrue(boolean... array){
        for(boolean value: array){
            if(value) return true;
        }
        return false;
    }

    public static Bitmap readFromExternalExportStorageAndDelete(Context c, String name){
         ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
         File directory;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             directory=new File(cw.getNoBackupFilesDir(),"imageCacheDirVideoExport");
         }else{
             directory = cw.getDir("imageCacheDirVideoExport",Context.MODE_PRIVATE);
         }
        if(!directory.exists()){
            directory.mkdirs();
        }
        File f=new File(directory, name);
        if(f.exists()) {
            Bitmap d=BitmapFactory.decodeFile(f.getPath());
            if(name.contains(Renderer.startOfFileName)) LogD(String.valueOf(f.delete()));
            return d;
        }else{
            return null;
        }
    }

    /**
     * Delete all Images saved to Apps External Storage starting with the name
     * @param c Your Context
     * @param name The name
     */
    public static void deleteAllVideoCacheContainingName(Context c,String name){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
        File directory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            directory=new File(cw.getNoBackupFilesDir(),"imageCacheDirVideoExport");
        }else{
            directory = cw.getDir("imageCacheDirVideoExport",Context.MODE_PRIVATE);
        }
        if(!directory.exists()){
            directory.mkdirs();
        }
        File[] filesList=directory.listFiles((file, s) -> s.startsWith(name));
        if(filesList==null)return;
        for (File f:filesList){
            if(f.delete())LogI("Deleted File "+f.getName());
            else LogI("Could not delete File "+f.getName());
        }
    }

    /**
     * Delete all Files in VideoCache, for all Projects. In most cases it is better to use {@link utils#deleteAllVideoCacheContainingName(Context, String)}
      * @param c Your context
     */
    public static void deleteAllVideoCache(Context c){
        ContextWrapper cw = new ContextWrapper(c.getApplicationContext());
        File directory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            directory=new File(cw.getNoBackupFilesDir(),"imageCacheDirVideoExport");
        }else{
            directory = cw.getDir("imageCacheDirVideoExport",Context.MODE_PRIVATE);
        }
        if(!directory.exists()){
            directory.mkdirs();
        }
        File[] filesList=directory.listFiles();
        if(filesList==null)return;
        for (File f:filesList){
            if(f.delete())LogI("Deleted File "+f.getName());
            else LogI("Could not delete File "+f.getName());
        }
    }

    public static Bitmap readFromExternalStorage(Context c, String name){
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
        return getMP4LengthInFrames(proj.getContext(),src);
    }

    public static int getMP4LengthInFrames(Context c,Uri src) throws IOException {
        ContentResolver resolver = c.getApplicationContext().getContentResolver();
        FileChannelWrapper ch = new FileChannelWrapper((new FileInputStream(resolver.openFileDescriptor(src, "r").getFileDescriptor())).getChannel());
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
        DemuxerTrack video_track = demuxer.getVideoTrack();
        int length = video_track.getMeta().getTotalFrames();
        ch.close();
        return length;
    }

     public static double getMP4LengthInSeconds(VideoProj proj,Uri src) throws IOException {
        return getMP4LengthInSeconds(proj.getContext(),src);
    }

    public static double getMP4LengthInSeconds(Context c, Uri src) throws IOException {
        ContentResolver resolver = c.getApplicationContext().getContentResolver();
        FileChannelWrapper ch = new FileChannelWrapper((new FileInputStream(resolver.openFileDescriptor(src, "r").getFileDescriptor())).getChannel());
        MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(ch);
        DemuxerTrack video_track = demuxer.getVideoTrack();
        double length = video_track.getMeta().getTotalDuration();
        ch.close();
        return length;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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
        String name="no label";
        if(applicationInfo.nonLocalizedLabel!=null) {
            name = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
        }
        return name.replaceAll(" ", "");
    }


    //From https://gist.github.com/omarmiatello/6711967
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    /**
     * Writes a byte Array to a temp file, which will be deleted at the Apps shutdown
     * @param context Your Context
     * @param array The byte Array
     * @return The File to the Temp file
     * @throws IOException IOException thrown by inner methods
     */
    public static File writeByteArrayToTempFile(Context context,byte[] array) throws IOException {
        File outputDir = context.getCacheDir(); // context being the Activity pointer
        File outputFile = File.createTempFile("TmpByteArray", "bytearray", outputDir);
        try(FileOutputStream stream = new FileOutputStream(outputFile)){
            stream.write(array);
        }
        return outputFile;
    }

    /**
     * Reads a File to an object implementing parcelable.
     * This should only used with temp files, because {@link Parcel} is not Designed for usage with multiple Versions of the Platform.
     * @param file The file
     * @param creator The Creator of the wanted Object
     * @return The Object
     * @throws IOException IOException thrown by inner methods
     * @see utils#writeByteArrayToTempFile(Context, byte[])
     */
    public static <T extends Parcelable> T getParcelableFromTempFile(File file,Parcelable.Creator<T> creator) throws IOException {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        return unmarshal(bytes, creator);
    }
    //From https://gist.github.com/omarmiatello/6711967
    public static <T extends Parcelable> T unmarshal(@NonNull byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshal(bytes);
        return creator.createFromParcel(parcel);
    }
    //From https://gist.github.com/omarmiatello/6711967
    public static Parcel unmarshal(@NonNull byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    public static Picture getPictureFromUriIdentifierPairs(List<UriIdentifierPair> pairs,Context context){
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Yuv420pToRgb ytb = new Yuv420pToRgb();
        for (UriIdentifierPair i : pairs) {
            try(ParcelFileDescriptor pfd=resolver.openFileDescriptor(i.getUriIdentifier().getUri(), "r")) {
                try (FileInputStream t = new FileInputStream(pfd.getFileDescriptor())) {
                    try (FileChannel c = t.getChannel()) {
                        try (FileChannelWrapper ch = new FileChannelWrapper(c)) {
                            FrameGrab grab = FrameGrab.createFrameGrab(ch);
                            PictureWithMetadata pic=grab.getNativeFrameWithMetadata();
                            Picture picture = pic.getPicture();
                            if (picture.getColor() == ColorSpace.YUV420) {
                                Picture pic3 = Picture.create(picture.getWidth(), picture.getHeight(), ColorSpace.RGB);
                                ytb.transform(picture, pic3);
                                picture=pic3;
                            }
                            return picture;
                        } catch (IOException | JCodecException e) {
                            utils.LogE(e);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                utils.LogE(e);
            } catch (IOException e) {
                utils.LogE(e);
            }
        }
        throw new IllegalStateException("No Picture found");
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
