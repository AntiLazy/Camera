package com.android.filters;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;

public class ImageFilterBitmaps {

	private ArrayList<float[]> colorMatrixList; 
	public ImageFilterBitmaps(){
		this.colorMatrixList = new ArrayList<float[]>();
		init();
	}
	public ArrayList<float[]> getColorMaArrayList() {
		return this.colorMatrixList;
	}
	/**
	 * 初始化颜色矩阵集合
	 */
	public void init() {
		
		colorMatrixList.add(getOrignalColorMatrix());
		colorMatrixList.add(getGrayColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		colorMatrixList.add(getOldColorMatrix());
		colorMatrixList.add(getHighCompareColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		colorMatrixList.add(getColorChangeColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		colorMatrixList.add(getGrayColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		colorMatrixList.add(getGrayColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		colorMatrixList.add(getGrayColorMatrix());
		colorMatrixList.add(getHighSaturatedColorMatrix());
		
	}
	/**
	 * 黑白矩阵
	 * @return
	 */
	public static float[] getGrayColorMatrix(){
		float[] mMatrixFloats = new float[] {  
				 0.22f, 0.5f, 0.1f, 0, 0,  
		         0.22f, 0.5f, 0.1f, 0, 0,  
		         0.22f, 0.5f, 0.1f, 0, 0,  
		         0, 0, 0, 1, 0  
			};
		return mMatrixFloats;
	}
	/**
	 * 获取高对比度矩阵
	 * @return
	 */
	public static float[] getHighCompareColorMatrix() {
		float[] mMatrixFloats = new float[] {  
				 5f, 0, 0, 0, -254,  
		         0, 5f, 0, 0, -254,  
		         0, 0, 5f, 0, -254,  
		         0, 0, 0, 1, 0  
			};
		return mMatrixFloats;
	}
	
	public static float[] getColorChangeColorMatrix() {
		float[] mMatrixFloats = new float[] {  
				 -0.36f, 1.691f, -0.32f, 0, 0,  
		         0.325f, 0.398f, 0.275f, 0, 0,  
		         0.79f, 0.796f, -0.76f, 0, 0,  
		         0, 0, 0, 1, 0  
			};
		return mMatrixFloats;
	}
	/**
	 * 高饱和度矩阵
	 * @return
	 */
	public static float[] getHighSaturatedColorMatrix() {
		float[] mMatrixFloats = new float[] {  
				 3.074f, -1.82f, -0.24f, 0, 50.8f,  
		         -0.92f, 2.171f, -0.24f, 0, 50.8f,  
		         -0.92f, -1.82f, 3.754f, 0, 50.8f,  
		         0, 0, 0, 1, 0  
			};
		return mMatrixFloats;
	}
	
	public static float[] getOrignalColorMatrix() {
		return new float[] {
				1,0,0,0,0,
				0,1,0,0,0,
				0,0,1,0,0,
				0,0,0,1,0
		};
	}
	/**
	 * 怀旧矩阵
	 * @return
	 */
	public static float[] getOldColorMatrix() {
		float[] mMatrixFloats = new float[] {
				0.393f,0.768f,0.189f,0,0,   
	            0.349f,0.686f,0.168f,0,0,   
	            0.272f,0.534f,0.131f,0,0,   
	            0,0,0,1,0
		};
		return mMatrixFloats;
	}
	/**
	 * 根据颜色矩阵转换bitmap
	 * @param mMatrixFloats 颜色矩阵
	 * @param bitmap 转换的位图
	 * @return 根据mMatrixFloats转换的Bitmap
	 */
	public static Bitmap translateBitmap(float[] mMatrixFloats,Bitmap bitmap) {
		if(mMatrixFloats == null) return bitmap;
		//建立颜色过滤器
		ColorMatrix colorMatrix = new ColorMatrix(mMatrixFloats);
		ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
		//创建与bitmap尺寸相同的空Bitmap
		Bitmap tmpBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
    		    Config.RGB_565);
		Canvas canvas = new Canvas(tmpBitmap);
		//创建画笔
		Paint paint = new Paint();
//		paint.seta
		paint.setColorFilter(colorFilter);
		//在画布上以画笔绘制位图
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return tmpBitmap;
	}
}
