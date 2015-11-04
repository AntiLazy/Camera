package com.android.activity;

import java.util.ArrayList;

import com.android.filters.ImageFilterBitmaps;
import com.android.ui.CropImageView;
import com.android.util.StorageUtil;
import com.example.mycamtest.R;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class PictureEditActivity extends Activity implements View.OnClickListener,OnSeekBarChangeListener{
	private CropImageView editView;
	
	
	private HorizontalScrollView horizontalScrollView;
	private View cutView;
	private View customColorView;
	
	float[] matrix;
	
	private ImageButton editOKButton;
	private ImageButton editColor;
	private ImageButton editCrop;
	private ImageButton editCustom;
	
	private ImageButton cropCancel;
	private ImageButton cropCut;
	
	private SeekBar seekBarRed;
	private SeekBar seekBarGreen;
	private SeekBar seekBarBlue;
	private SeekBar seekBarAlpha;
	private static final int SEEKBAR_MAX = 510;
	
	
	private final static int STATUS_COLOR = 0;
	private final static int STATUS_CROP = 1;
	private final static int STATUS_CUSTOM = 2;
	
	private int mStatus = 0;
	/**
	 * 原始图片
	 */
	private Bitmap orignalBitmap;
	/**
	 * 原编辑的bitmap，可能是原始图片，也可能是剪切后的原图片
	 */
	private Bitmap editBitmap;
	/**
	 * 编辑过的bitmap
	 */
	private Bitmap currentBitmap;
	/**
	 * 颜色矩阵
	 */
	private ArrayList<float[]> matrixList;
	
	private int currentMatrixIndex = 0;
	/**
	 * 编辑的bitmap的镜像，为指定尺寸的editBitmap
	 */
	private Bitmap editBitmapMirror;
	private static int MIRROR_WIDTH = 90;
	private static int MIRROR_HEIGHT = 160;
	/**
	 * 编辑的文件路径
	 */
	private String picturePath;
	/**
	 * 图片集合
	 */
	private ArrayList<String> pictureList;
	
	LinearLayout layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_edit);
		Intent intent = getIntent();
		//获取编辑的图片路径
		this.picturePath = intent.getStringExtra("picturePath");
		this.pictureList = (ArrayList<String>)intent.getStringArrayListExtra("pictures");
		
		this.editView = (CropImageView)findViewById(R.id.cropimage);
		this.editView.setCropEnable(false);
		this.editView.setDrawable(new BitmapDrawable(getResources(),picturePath),0,0);
		//获取编辑的图片位图
		this.orignalBitmap = BitmapFactory.decodeFile(picturePath);
		this.editBitmap = this.orignalBitmap;
		
		this.horizontalScrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView1);
		this.cutView = findViewById(R.id.layout_crop);
		this.customColorView = findViewById(R.id.layout_custom);
		
		
		
		 layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        this.editBitmapMirror = Bitmap.createScaledBitmap(editBitmap, MIRROR_WIDTH, MIRROR_HEIGHT, false);
        
        ImageFilterBitmaps filter = new ImageFilterBitmaps();
        filter.init();
        this.matrixList = filter.getColorMaArrayList();
        
        for(int i = 0;i<matrixList.size();i++) {
        
        	final int index = i;
        	Bitmap tmpBitmap = ImageFilterBitmaps.translateBitmap(matrixList.get(index), editBitmapMirror);
        	ImageView imageView = new ImageView(this);
        	imageView.setImageBitmap(tmpBitmap);
        	imageView.setClickable(true);
        	imageView.setBackgroundColor(Color.GRAY);
        	imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(currentMatrixIndex == index) return;
					currentBitmap = ImageFilterBitmaps.translateBitmap(matrixList.get(index), editBitmap);
					editView.setmDrawable(new BitmapDrawable(PictureEditActivity.this.getResources(),currentBitmap));
					currentMatrixIndex = index;
				}
			});
        	
        	
        	layout.addView(imageView);
        }
        horizontalScrollView.addView(layout);
        
        initViews();

        
	}
	
	private void initViews() {
        this.editOKButton = (ImageButton)findViewById(R.id.edit_ok);
        this.editOKButton.setOnClickListener(this);
        this.editColor = (ImageButton)findViewById(R.id.edit_color);
        this.editColor.setOnClickListener(this);
        this.editCrop = (ImageButton)findViewById(R.id.edit_crop);
        this.editCrop.setOnClickListener(this);
        this.editCustom = (ImageButton)findViewById(R.id.edit_custom);
        this.editCustom.setOnClickListener(this);
		this.cropCancel = (ImageButton)this.findViewById(R.id.crop_cancel);
		this.cropCancel.setOnClickListener(this);
		this.cropCut = (ImageButton)this.findViewById(R.id.crop_cut);
		this.cropCut.setOnClickListener(this);
		
		initSeekBar(SEEKBAR_MAX);
		
	}
	private void initSeekBar(int max) {
		this.seekBarRed = (SeekBar)findViewById(R.id.seekBar_red);
		this.seekBarRed.setOnSeekBarChangeListener(this);
		this.seekBarGreen = (SeekBar)findViewById(R.id.seekBar_green);
		this.seekBarGreen.setOnSeekBarChangeListener(this);
		this.seekBarBlue = (SeekBar)findViewById(R.id.seekBar_blue);
		this.seekBarBlue.setOnSeekBarChangeListener(this);
		this.seekBarAlpha = (SeekBar)findViewById(R.id.seekBar_alpha);
		this.seekBarAlpha.setOnSeekBarChangeListener(this);
		this.seekBarRed.setMax(max);
		this.seekBarGreen.setMax(max);
		this.seekBarBlue.setMax(max);
		this.seekBarAlpha.setMax(200);
		int initNum = max >> 1;
        this.seekBarRed.setProgress(initNum);
        this.seekBarBlue.setProgress(initNum);
        this.seekBarGreen.setProgress(initNum);
        this.seekBarAlpha.setProgress(100);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public void onClick(View v) {
		if (v == editOKButton) {
		
			//保存编辑后的图片
			String savePath = StorageUtil.saveBitmap(currentBitmap);
			 Intent intent = new Intent(this, PicturePages.class);
			 this.pictureList.add(savePath);
			 intent.putExtra("pictures", this.pictureList);
			 intent.putExtra("position", this.pictureList.size()-1);
			 startActivity(intent);
//			 intent.pute
		}
		if(v == editColor) {
			if(mStatus == STATUS_COLOR) return;
			
			if(mStatus == STATUS_CROP) {
				this.editView.setCropEnable(false);
				this.cutView.setVisibility(View.GONE);
			} else {
				this.customColorView.setVisibility(View.GONE);
			}
			this.horizontalScrollView.setVisibility(View.VISIBLE);
			mStatus = STATUS_COLOR;
		}
		if(v == editCrop) {
			if(mStatus == STATUS_CROP) return;
			this.editView.setCropEnable(true);
			
			if(mStatus == STATUS_COLOR) {
				this.horizontalScrollView.setVisibility(View.GONE);
			} else {
				this.customColorView.setVisibility(View.GONE);
			}
			this.cutView.setVisibility(View.VISIBLE);
			mStatus = STATUS_CROP;
		}
		if(v == editCustom) {
			if(mStatus == STATUS_CUSTOM) return;
			if(mStatus == STATUS_COLOR) {
				this.horizontalScrollView.setVisibility(View.GONE);
			} else {
				this.editView.setCropEnable(false);
				this.cutView.setVisibility(View.GONE);
			}
			this.customColorView.setVisibility(View.VISIBLE);
			mStatus = STATUS_CUSTOM;
		}
		if(v == cropCancel) {
			this.editBitmap = this.orignalBitmap;
			this.currentBitmap = this.editBitmap;
			this.editView.setFitst(true);
			this.editView.setDrawable(new BitmapDrawable(this.getResources(),currentBitmap), 0, 0);
		}
		if(v == cropCut) {
			Bitmap cropBitmap = this.editView.getCropImage();
			this.editBitmap = cropBitmap;
			this.currentBitmap = cropBitmap;
			this.editView.setFitst(true);
			this.editView.setDrawable(new BitmapDrawable(this.getResources(),cropBitmap), 0, 0);
		}
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		 matrix = matrixList.get(currentMatrixIndex);
		if(seekBar == seekBarRed) matrix[4] = progress - 255;
		else if(seekBar == seekBarGreen) matrix[9] = progress - 255;
		else if(seekBar == seekBarBlue) matrix[14] = progress - 255;
		else matrix[19] = progress - 100;
		Log.d("yejia", "progress = "+progress);
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		currentBitmap = ImageFilterBitmaps.translateBitmap(matrix, editBitmap);
		editView.setmDrawable(new BitmapDrawable(this.getResources(),ImageFilterBitmaps.translateBitmap(matrix, editBitmap)));
	}
}
