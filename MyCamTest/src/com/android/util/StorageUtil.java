package com.android.util;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

public class StorageUtil {
    private static String TAG = "CameraActivity";
    /**
     * get the storage path
     * @return storage path
     */
    public static  String getStoragePath() {
        return Environment.getExternalStorageDirectory().toString()+"/DCIM/Camera";
    }
    
    public static String saveBitmap(Bitmap bitmap) {
        String fileName = System.currentTimeMillis()+".jpg";
        String absolutePath = getStoragePath() + "/" +fileName;
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(absolutePath);
            BufferedOutputStream buff = new BufferedOutputStream(fileOutputStream);
            bitmap.compress(CompressFormat.JPEG, 100, buff);
            buff.flush();
            buff.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            Log.d(TAG, "saveBitmap success");
            return absolutePath;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "saveBitmap fail : file not found --"+e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "saveBitmap fail : IO --"+e.getMessage());
        }
        return "saveError";
    }
    
    public static String getVideoPath() {
        return getStoragePath()+"/Video"+System.currentTimeMillis()+".3gp";
    }
}
