package com.android.activity;

import java.util.ArrayList;

import com.android.filters.ImageFilterBitmaps;
import com.android.ui.CropImageView;
import com.android.util.StorageUtil;
import com.example.mycamtest.R;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class EditPicture extends Activity implements OnClickListener{
	
	private ResideMenu resideMenu;
	private ResideMenuItem itemHome;
	private ResideMenuItem itemCut;
	private EditPicture mContext;
	
	private CropImageView editView;
	private HorizontalScrollView horizontalScrollView;
	private ImageButton editOKButton;
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
        init();
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
		if (v == itemHome){
			horizontalScrollView.setVisibility(View.VISIBLE);
            resideMenu.closeMenu();
//            Toast.makeText(this, "home is clicked!", 1).show();
        }
		if(v == itemCut) {
			horizontalScrollView.setVisibility(View.GONE);
			layoutChangeToCutMode();
			resideMenu.closeMenu();
		}
	}
	/**
	 * change the layout in cut mode
	 */
	private void layoutChangeToCutMode() {
		
		ViewGroup.LayoutParams params = editView.getLayoutParams();
		params.height = 1050;
		editView.setLayoutParams(params);
		
		Rect rect = editView.getDrawableRect();
		int width = (int) (rect.width() * 1.2);
		int hight = (int) (rect.height() * 1.2);
		int left = rect.left - ((width - rect.width())>>1);
		Rect cropRect = new Rect(left, rect.top + 50, left + width, rect.top + 50 + hight);
		
        editView.setDrawableRect(cropRect);
		editView.setCropEnable(true);
		editView.setCropRect(cropRect);
	}
	
	private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
            Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
            
        }

        @Override
        public void closeMenu() {
            Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
//            processBar.postInvalidate();
        }
    };
    
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
    	if(!resideMenu.isOpened()) {
    	Rect rect = new Rect();
    	editView.getGlobalVisibleRect(rect);
    	if(rect.contains((int)ev.getX(), (int)ev.getY()))return editView.dispatchTouchEvent(ev);
    	}
    	return super.dispatchTouchEvent(ev);
    };
    
    private void init() {
		mContext = this;
		resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        
        resideMenu.setScaleValue(0.6f);
        itemHome = new ResideMenuItem(this, R.drawable.icon_home,"Home");
        itemCut = new ResideMenuItem(this, R.drawable.icon_cut, "Cut");
        
        itemHome.setOnClickListener(this);
        itemCut.setOnClickListener(this);
        
        resideMenu.addMenuItem(itemHome, ResideMenu.DIRECTION_RIGHT);
        resideMenu.addMenuItem(itemCut, ResideMenu.DIRECTION_RIGHT);
        
        resideMenu.addIgnoredView(editView);
        resideMenu.addIgnoredView(horizontalScrollView);

	}
}
