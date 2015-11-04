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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PictureEditActivity extends Activity implements View.OnClickListener{
	private CropImageView editView;
	private HorizontalScrollView horizontalScrollView;
	private ImageButton editOKButton;
	private ImageButton editColor;
	private ImageButton editCrop;
	private ImageButton editCustom;
	
	
	private final static int STATUS_COLOR = 0;
	private final static int STATUS_CROP = 1;
	private final static int STATUS_CUSTOM = 2;
	
	private int mStatus = 0;
	/**
	 * 原编辑的bitmap
	 */
	private Bitmap editBitmap;
	/**
	 * 编辑过的bitmap
	 */
	private Bitmap currentBitmap;
	
	private ArrayList<float[]> matrixList;
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
		this.editBitmap = BitmapFactory.decodeFile(picturePath);
		
		this.horizontalScrollView = (HorizontalScrollView)findViewById(R.id.horizontalScrollView1);
		LinearLayout layout = new LinearLayout(this);
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
					currentBitmap = ImageFilterBitmaps.translateBitmap(matrixList.get(index), editBitmap);
					editView.setmDrawable(new BitmapDrawable(currentBitmap));
					
				}
			});
        	
        	
        	layout.addView(imageView);
        }
        horizontalScrollView.addView(layout);
        
        this.editOKButton = (ImageButton)findViewById(R.id.edit_ok);
        this.editOKButton.setOnClickListener(this);
        this.editColor = (ImageButton)findViewById(R.id.edit_color);
        this.editColor.setOnClickListener(this);
        this.editCrop = (ImageButton)findViewById(R.id.edit_crop);
        this.editCrop.setOnClickListener(this);
        this.editCustom = (ImageButton)findViewById(R.id.edit_custom);
        this.editCustom.setOnClickListener(this);
        
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
				this.horizontalScrollView.setVisibility(View.VISIBLE);
				this.editView.setCropEnable(false);
			}
			
			mStatus = STATUS_COLOR;
		}
		if(v == editCrop) {
			if(mStatus == STATUS_CROP) return;
			this.editView.setCropEnable(true);
			this.horizontalScrollView.setVisibility(View.INVISIBLE);
			
			mStatus = STATUS_CROP;
		}
		if(v == editCustom) {
			if(mStatus == STATUS_CUSTOM) return;
			
			this.horizontalScrollView.setVisibility(View.GONE);
			findViewById(R.id.layout_custom).setVisibility(View.VISIBLE);
		}
	}
}
