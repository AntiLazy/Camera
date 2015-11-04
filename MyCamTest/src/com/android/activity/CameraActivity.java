package com.android.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Video.Thumbnails;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.android.preview.CameraSurfaceView;
import com.android.util.StorageUtil;
import com.example.mycamtest.R;

public class CameraActivity extends Activity implements OnClickListener,MediaRecorder.OnErrorListener,MediaRecorder.OnInfoListener{
    private Camera mCamera;
    private boolean isPreview = false;
    private CameraSurfaceView surfaceView;
    private ImageButton imageButton;
    private Switch buttonSwitch;
    private static String TAG = "CameraActivity";
    private float previewRate = 16f/9f;
    private ImageView imageView;
    private TextView timeteTextView;
    /**
     * 前后摄像头切换按钮
     */
    private ImageButton buttonCameraSwitch;
    private Handler myCameraHandler;
    private final static int START_PREVIEW = 1;
    private final static int PREPARE_BEFORE_RECORDING = 2;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private String videoPath;
    private boolean isFrontCamera = false;
    private int cameraId = 0;
    private String lastPicturePath = null;
    
    /**
     * Camera的模式，0为拍照模式，1为录像模式，2为正在录制。
     */
    private  int cameraStatus = 0;
    /**
     * 拍照模式
     */
    public static int PHOTO_MODE = 0;
    /**
     * 录像模式
     */
    public static int VIDEO_MODE = 1;
    /**
     * 正在记录模式
     */
    public static int VIDEO_RECORDING = 2;
    private Drawable drawable_photo;
    private Drawable drawable_video;
    private Drawable drawable_video_recording;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        setContentView(R.layout.activity_main);
        
        init();
    }
    
    /**
     * 初始化控件和图标，为控件添加监听
     */
    private void init() {
    	surfaceView = (CameraSurfaceView)this.findViewById(R.id.camera_surfaceview);
        imageButton = (ImageButton)this.findViewById(R.id.btn_shutter);
        imageView = (ImageView)this.findViewById(R.id.imageView1);
        buttonSwitch = (Switch)this.findViewById(R.id.switch1);
        drawable_photo = getResources().getDrawable(R.drawable.btn_shutter_photo);
        drawable_video = getResources().getDrawable(R.drawable.btn_shutter_video);
        drawable_video_recording = getResources().getDrawable(R.drawable.btn_shutter_video_default);
        buttonCameraSwitch = (ImageButton)this.findViewById(R.id.imageButton1);
        
        buttonSwitch.setOnClickListener(new OnClickListener() {
        
            @Override
            public void onClick(View v) {
                if(buttonSwitch.isChecked()) {
                    cameraStatus = 0;
                    imageButton.setImageDrawable(drawable_photo);
                    
                } else {
                    cameraStatus = 1;
                    imageButton.setImageDrawable(drawable_video);
                }
                myCameraHandler.sendEmptyMessage(START_PREVIEW);
            }
        });
        buttonCameraSwitch.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
               cameraId = Math.abs(cameraId - 1);
               myCameraHandler.sendEmptyMessage(START_PREVIEW);
            }
        });
        //TODO 显示时间，待做
        timeteTextView = (TextView)this.findViewById(R.id.timeTextView);
        //获得相机状态
        this.cameraStatus = buttonSwitch.isChecked() ? 0 : 1;
        if(this.cameraStatus == 0) imageButton.setImageDrawable(drawable_photo);
        else imageButton.setImageDrawable(drawable_video);
        imageButton.setOnClickListener(this);
        imageView.setOnClickListener(this);
        myCameraHandler = new MyCameraHandler();
        initLastPicturePath();
        if(this.lastPicturePath != null) {
        	imageView.setImageBitmap(BitmapFactory.decodeFile(this.lastPicturePath));
        }
    }
    /**
     * 初始化左下角的图片，使之为最近拍摄的最新的图片
     */
    private void initLastPicturePath() {
    	if(this.lastPicturePath == null) {
    		 String path = StorageUtil.getStoragePath();
    	        File[] files = new File(path).listFiles();
    	        for(int index = files.length - 1;index >= 0;index--){
    	        	String fileName = files[index].getName();
    	        	Log.d("zejia.ye", "fileName = "+fileName);
    	        	if(fileName.endsWith(".jpg")
    	        			||fileName.endsWith(".png")||fileName.endsWith(".3gp")) {
    	        		this.lastPicturePath = files[index].getAbsolutePath();
    	        		break;
    	        	}
    	        }
    	}
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        
        this.cameraStatus = buttonSwitch.isChecked() ? 0 : 1;
        if(this.cameraStatus == 0) imageButton.setImageDrawable(drawable_photo);
        else imageButton.setImageDrawable(drawable_video);
         LayoutParams params = surfaceView.getLayoutParams();
         //获取屏幕宽高what
         DisplayMetrics dm = this.getResources().getDisplayMetrics();
         params.width = dm.widthPixels;
         params.height = dm.heightPixels;
         surfaceView.setLayoutParams(params);
         Log.d(TAG, "params.width = "+params.width+" params.height = "+params.height);
         this.myCameraHandler.sendEmptyMessage(CameraActivity.START_PREVIEW);
        super.onResume();
    }
    public Camera getCamera() {
        return this.mCamera;
    }
    /*8
     * 获取相机模式,0为拍照模式，1为录像模式，2为正在录制。
     */
    public  int getCameraStatus(){
        return cameraStatus;
    }
    public void startPreview(){
        imageButton.setEnabled(false);
        doStopCamera();
        mCamera = Camera.open(cameraId);
        
        if(this.isPreview)
            return;
        if(this.mCamera != null) {
            
            //Toast.makeText(this, "startPreview", 1).show();
            Log.d(TAG, "startPreview");
            Log.d(TAG, previewRate+"");
           Parameters parameters = mCamera.getParameters();
           parameters.setPictureFormat(ImageFormat.JPEG);
           // 设置为多媒体录制，加快打开的速度，设置这个之后录像的预览和录制结果的亮度跟接近外界
           if(this.cameraStatus == 1) parameters.setRecordingHint(true);
           
           if(parameters.isZoomSupported()) parameters.setZoom(0);
           parameters.setPictureSize(1920, 1080);
           
           parameters.set("cap-mode", "normal");
           parameters.set("cds-mode", "off");
           parameters.set("cur-exposure-time", 100);
           parameters.set("jpeg-quality:", 95);
           parameters.set("snapshot-fd-data-enable", 0);
           parameters.set("tintless","enable");
           parameters.set("touch-af-aec", "touch-on");
           
           
           //如果支持自动对焦，设置为自动连续对焦 
           List<String> focusModes = parameters.getSupportedFocusModes();
           if(focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
               parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
           }
//           if(parameters.getSupportedFlashModes().contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
//               parameters.setFlashMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
           if(parameters.isVideoStabilizationSupported()) parameters.setVideoStabilization(true);
//           Log.d(TAG, "parameters.flatten = "+parameters.flatten());
//           List<Size> previewSizes = parameters.getSupportedPreviewSizes();
//           List<Size> pictureSizes = parameters.getSupportedPictureSizes();
           if(parameters.getSupportedAntibanding().contains(Parameters.ANTIBANDING_AUTO))
               parameters.setAntibanding(Parameters.ANTIBANDING_AUTO);
           Log.d("zejia.ye", "antibangding = "+parameters.getAntibanding()+"  FlashMode="+parameters.getFlashMode());
//               parameters.setPreviewSize(1216, 800);
//               parameters.setPictureSize(1216, 800);
           //List<String> focusModeStrings = parameters.getSupporteFocusModes();
//               parameters.setRotation(90);
               parameters.setRotation((this.getOrientation()));
           mCamera.setParameters(parameters);
           
           mCamera.setDisplayOrientation(this.getOrientation());
//           mCamera.setDisplayOrientation(90);
           try {
//               mCamera.setPreviewDisplay(surfaceView.getSurfaceHolder());
            mCamera.setPreviewDisplay(surfaceView.getSurfaceHolder());
            mCamera.startPreview();
            this.isPreview = true;
            Log.d(TAG, "startPreview : success");
        } catch (Exception e) {
            Log.d(TAG, "error : "+e.toString());
        }
        }
        //如果是录像模式，提前初始化meidarecord
        if(cameraStatus == 1) prepareBeforeRecording();
        imageButton.setEnabled(true);
    }
    public void doStopCamera() {
        if(mCamera !=null) {
            this.mCamera.lock();
            if(this.mediaRecorder !=null) {
//                this.mediaRecorder.stop();
                this.mediaRecorder.release();
                this.mediaRecorder = null;
            }
            mCamera.stopPreview();
            isPreview = false;
            mCamera.release();
            mCamera = null;
            Log.d("zejia.ye", "stop camera!");
        }
    }
    
    public void prepareBeforeRecording() {
        this.imageButton.setEnabled(false);
        this.mediaRecorder = new MediaRecorder();
        CamcorderProfile profile;
        if(cameraId == CameraInfo.CAMERA_FACING_BACK) {
            //获取高清晰度的设置
            profile = CamcorderProfile
                    .get(CamcorderProfile.QUALITY_HIGH);
        }else {
            profile = CamcorderProfile
                    .get(CamcorderProfile.QUALITY_LOW);
        }
        Log.d(TAG, "profile: " + profile);
        this.videoPath = StorageUtil.getVideoPath();

        this.mCamera.unlock();
        this.mediaRecorder.setCamera(this.mCamera);

        this.mediaRecorder.setPreviewDisplay(surfaceView.getSurfaceHolder()
                .getSurface());

        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        
        Log.d(TAG, "format: " + profile.fileFormat);
        this.mediaRecorder.setProfile(profile);

        this.mediaRecorder.setOrientationHint(90);
        this.mediaRecorder.setOutputFile(this.videoPath);

        this.mediaRecorder.setOnErrorListener(this);
        this.mediaRecorder.setOnInfoListener(this);
        
        
        this.timeteTextView.setVisibility(View.VISIBLE);
        this.isRecording = false;
        this.isPreview = true;
        this.cameraStatus = 1;
        try {
            this.mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.imageButton.setEnabled(true);
    }

    public void doStartRecording() {
//        prepareBeforeRecording();
//            this.mediaRecorder = new MediaRecorder();
//
//        CamcorderProfile profile = CamcorderProfile
//                .get(CamcorderProfile.QUALITY_HIGH);
//        Log.d(TAG, "profile: " + profile);
//        this.videoPath = StorageUtil.getVideoPath();
//
//        this.mCamera.unlock();
//        this.mediaRecorder.setCamera(this.mCamera);
//
//        this.mediaRecorder.setPreviewDisplay(surfaceView.getSurfaceHolder()
//                .getSurface());
//
//        this.mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        
//        Log.d(TAG, "format: " + profile.fileFormat);
//        this.mediaRecorder.setProfile(profile);
//        
//        
//        //this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //加了这句与setProfile冲突
//        //this.mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);同上
////        this.mediaRecorder.setVideoFrameRate(50);
//
//        /*
//         * this
//         * .mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//         * this.mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
//         * this
//         * .mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//         * this.mediaRecorder.setVideoSize(1218, 800);
//         * this.mediaRecorder.setVideoFrameRate(50);
//         */
//
//        this.mediaRecorder.setOrientationHint(90);
//        this.mediaRecorder.setOutputFile(this.videoPath);
//
//        this.mediaRecorder.setOnErrorListener(this);
//        this.mediaRecorder.setOnInfoListener(this);
//        prepareBeforeRecording();
        this.mediaRecorder.start();
        this.isRecording = true;
        this.isPreview = true;
        this.cameraStatus = 2;
        this.imageButton.setImageDrawable(drawable_video_recording);
        
//        try {
//            this.mediaRecorder.prepare();
//            this.mediaRecorder.start();
//        } catch (Exception e) {
//            // TODO Auto-generated catch block
//            Log.d("zejia.ye", "abc" + e.toString(), new IllegalStateException());
//        }
//        this.timeteTextView.setVisibility(View.VISIBLE);
//        this.isRecording = true;
//        this.isPreview = true;
//        this.cameraStatus = 2;
//        this.imageButton.setImageDrawable(drawable_video_recording);

        Log.d("zejia.ye", "doStartRecording is finished!");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_shutter:
            Log.d("zejia.ye", "Shutter button is clicked!");
            switch (this.cameraStatus) {
            case 0:
                this.mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
                break;
            case 1:
                this.doStartRecording();
                break;
            case 2:
                this.saveRecord();
            default:
                break;
            }
            
            break;
        case R.id.imageView1:
            Log.d(TAG, "imageView1 is clicked.");
//            Intent intent = new Intent(CameraActivity.this, PictureUI.class);
//            intent.putExtra("picturePath", imageView.getTag().toString());
            try {
                Intent intent = new Intent(CameraActivity.this,PictureGallery.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
            
            overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left); 
            break;
        default:
            break;
        }
        
        
    }
    
    private void saveRecord() {
        // TODO Auto-generated method stub
        if(isRecording) {
            this.mediaRecorder.stop();
            this.mediaRecorder.release();
            this.mediaRecorder = null;
        }
        this.imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(this.videoPath, Thumbnails.MINI_KIND));
        this.isPreview = false;
        this.cameraStatus = 1;
        this.imageButton.setImageDrawable(drawable_video);
        myCameraHandler.sendEmptyMessage(START_PREVIEW);
    }

    private ShutterCallback shutterCallback = new ShutterCallback() {
        
        @Override
        public void onShutter() {
            Log.d(TAG, "Shutter button is clicked!");
            
        }
    };
    
    private PictureCallback rawCallback = new PictureCallback() {
        
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "RawCallback , and I do nothing here.");
        }
    };
    
    private PictureCallback jpegCallback = new PictureCallback() {
        
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if(!isPreview) return;
            if(data!=null) {
                //change the picture data from byte[] to bitmap
               Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
               //the angle of the picture is wrong and here do the rotate
//               Matrix matrix = new Matrix();
//               matrix.postRotate(90f);
//               bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
               //show the picture
               imageView.setImageBitmap(bitmap);
               //save the picture
              String picturePathString = StorageUtil.saveBitmap(bitmap);
              imageView.setTag(picturePathString);
              Log.d(TAG, "imageView.Tag = "+imageView.getTag().toString());
               isPreview = false;
               myCameraHandler.sendEmptyMessage(START_PREVIEW);
            }
        }
    };
    
    private class MyCameraHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
            case START_PREVIEW:
                startPreview();
                //如果是录像模式，做好mediarecord的设置准备录像
                //if(cameraStatus == 1) myCameraHandler.sendEmptyMessage(CameraActivity.PREPARE_BEFORE_RECORDING);
                break;
            case PREPARE_BEFORE_RECORDING:
                prepareBeforeRecording();
            default:
                break;
            }
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.d("zejia.ye", "Error what = "+what+" extra = "+ extra);
        
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        // TODO Auto-generated method stub
        Log.d("zejia.ye", "Info what = "+what+" extra = "+ extra);
    }
    
    private int getOrientation() {
        int result = 0;
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int degreee = 0;
        switch (rotation) {
        case Surface.ROTATION_0:
            degreee = 0; 
            break;
        case Surface.ROTATION_90:
            degreee = 90;
            break;
        case Surface.ROTATION_180:
            degreee = 180;
            break;
        case Surface.ROTATION_270:
            degreee = 270;
            break;
        default:
            break;
        }
        Log.d("zejia.ye", "surface.rotation   degree = "+degreee);
        CameraInfo info = new CameraInfo();
        if(isFrontCamera) {
            Camera.getCameraInfo(CameraInfo.CAMERA_FACING_FRONT, info);
            result = (info.orientation + degreee) % 360;
            result = 360 - result;
        }
        else {
            Camera.getCameraInfo(CameraInfo.CAMERA_FACING_BACK, info);
            result = (info.orientation - degreee + 360) % 360;
        }
        Log.d("zejia.ye", "oritation result = "+result);
        return result;
    }

}
