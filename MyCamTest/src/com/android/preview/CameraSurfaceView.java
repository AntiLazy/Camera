package com.android.preview;

import java.io.IOException;

import com.android.activity.CameraActivity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
    private SurfaceHolder mSurfaceHolder;
    private CameraActivity cameraActivity;
    private static String TAG = "CameraActivity";
   
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        this.cameraActivity = (CameraActivity)context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.addCallback(this);
    }
    public SurfaceHolder getSurfaceHolder() {
        return this.mSurfaceHolder;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated,,,,");
//        try {
//            this.cameraActivity.getCamera().setPreviewDisplay(this.mSurfaceHolder);//be sure camera is not null
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            Log.d(TAG, "surfaceCreated,,,,+ excep:"+e.toString());
//        }
       
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d(TAG, "surfaceChanged......");
        this.cameraActivity.startPreview();
        
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "surfaceDestroyed......");
        this.cameraActivity.doStopCamera();
    }
    
    
}
