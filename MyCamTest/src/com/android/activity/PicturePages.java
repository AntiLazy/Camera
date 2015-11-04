package com.android.activity;


import java.util.ArrayList;

import com.example.mycamtest.R;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PicturePages extends Activity {

	private ViewPager picturePages;
	/**
	 * 图片绝对地址集合
	 */
	private ArrayList<String> pictureList;
	/**
	 * viewPage 页面集合，布局看activity_picture
	 */
	private ArrayList<View> pictureViews;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pictures_viewpage);
		this.picturePages = (ViewPager)findViewById(R.id.viewpager);
		
		Intent intent = getIntent();
//	    this.picturePath = intent.getStringExtra("picturePath");
//	    this.position = intent.getIntExtra("position",0);
	    this.pictureList = (ArrayList<String>)intent.getStringArrayListExtra("pictures");
	    int position = intent.getIntExtra("position",0);
	    Log.d("zejia.ye", "item = "+position+"  picture.size = "+pictureList.size()+" pictureList[2] = "+pictureList.get(2));
	    
	    initViews();
	    
	    PicturePageAdapter adapter = new PicturePageAdapter(this);
	    this.picturePages.setAdapter(adapter);
	    this.picturePages.setCurrentItem(position);
	    this.picturePages.setPageTransformer(true, new DepthPageTransformer());
		
	}
	/**
	 * 初始化显示的ViewPage单元界面，将界面集合存储于pictureViews中
	 */
	public void initViews() {
		this.pictureViews = new ArrayList<View>();
		for(String picturePath:pictureList) {
			View view = LayoutInflater.from(this).inflate(R.layout.activity_picture, null);
			ImageView imageView = (ImageView)view.findViewById(R.id.pictureImageView);
			imageView.setScaleType(ScaleType.CENTER_CROP);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			Bitmap bitmap = BitmapFactory.decodeFile(picturePath, options);
			ImageButton imageEditButton = (ImageButton)view.findViewById(R.id.image_edit);
//			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
			imageView.setImageBitmap(bitmap);
			imageEditButton.setOnClickListener(new EditClick(picturePath));
			this.pictureViews.add(view);
		}
	}
	
	class EditClick implements View.OnClickListener {
		/**
		 * 编辑的图片地址
		 */
		private String editedPicturePathString ;
		
		public EditClick(String editedPicturePath) {
			this.editedPicturePathString = editedPicturePath;
		}
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(PicturePages.this, PictureEditActivity.class);
//			Intent intent = new Intent(PicturePages.this, EditPicture.class);
			//将图片地址作参数传递到编辑界面
			intent.putExtra("picturePath", this.editedPicturePathString);
			intent.putExtra("pictures", pictureList);
			startActivity(intent);
		}
		
	}
	/**
	 * 页面适配器
	 * @author zejia.ye
	 *
	 */
	public class PicturePageAdapter extends PagerAdapter {
		
		Context context;
		
		public PicturePageAdapter(Context context) {
			this.context = context;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(pictureViews.get(position));
			return pictureViews.get(position);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pictureList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView(pictureViews.get(position));
		}
		
	}
	/**
	 *  ViewPage 翻页动画
	 * @author zejia.ye
	 *
	 */
     class CubeTransformer implements ViewPager.PageTransformer {
		
		/**
		 * position参数指明给定页面相对于屏幕中心的位置。它是一个动态属性，会随着页面的滚动而改变。当一个页面填充整个屏幕是，它的值是0，
		 * 当一个页面刚刚离开屏幕的右边时，它的值是1。当两个也页面分别滚动到一半时，其中一个页面的位置是-0.5，另一个页面的位置是0.5。基于屏幕上页面的位置
		 * ，通过使用诸如setAlpha()、setTranslationX()、或setScaleY()方法来设置页面的属性，来创建自定义的滑动动画。
		 */
		@Override
		public void transformPage(View view, float position) {
			if (position <= 0) {
				//从右向左滑动为当前View
				
				//设置旋转中心点；
				ViewHelper.setPivotX(view, view.getMeasuredWidth());
				ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);
				
				//只在Y轴做旋转操作
				ViewHelper.setRotationY(view, 90f * position);
			} else if (position <= 1) {
				//从左向右滑动为当前View
				ViewHelper.setPivotX(view, 0);
				ViewHelper.setPivotY(view, view.getMeasuredHeight() * 0.5f);
				ViewHelper.setRotationY(view, 90f * position);
			}
		}
	}
     class DepthPageTransformer implements ViewPager.PageTransformer {
         private static final float MIN_SCALE = 0.75f;
                              
         @Override
         public void transformPage(View view, float position) {
             int pageWidth = view.getWidth();
             if (position < -1) { // [-Infinity,-1)
                                     // This page is way off-screen to the left.
                 view.setAlpha(0);
             } else if (position <= 0) { // [-1,0]
                                         // Use the default slide transition when
                                         // moving to the left page
                 view.setAlpha(1);
                 view.setTranslationX(0);
                 view.setScaleX(1);
                 view.setScaleY(1);
             } else if (position <= 1) { // (0,1]
                                         // Fade the page out.
                 view.setAlpha(1 - position);
                 // Counteract the default slide transition
                 view.setTranslationX(pageWidth * -position);
                 // Scale the page down (between MIN_SCALE and 1)
                 float scaleFactor = MIN_SCALE + (1 - MIN_SCALE)
                         * (1 - Math.abs(position));
                 view.setScaleX(scaleFactor);
                 view.setScaleY(scaleFactor);
             } else { // (1,+Infinity]
                         // This page is way off-screen to the right.
                 view.setAlpha(0);
                              
             }
         }
                              
     }
	
}
