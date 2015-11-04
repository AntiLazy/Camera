package com.android.ui;

import android.R.integer;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class CropImageView extends View {
	 // \u5728touch\u91cd\u8981\u7528\u5230\u7684\u70b9\uff0c
	 private float mX_1 = 0;
	 private float mY_1 = 0;
	 // \u89e6\u6478\u4e8b\u4ef6\u5224\u65ad
	 private final int STATUS_SINGLE = 1;
	 private final int STATUS_MULTI_START = 2;
	 private final int STATUS_MULTI_TOUCHING = 3;
	 // \u5f53\u524d\u72b6\u6001
	 private int mStatus = STATUS_SINGLE;
	 // \u9ed8\u8ba4\u88c1\u526a\u7684\u5bbd\u9ad8
	 private int cropWidth;
	 private int cropHeight;
	 // \u6d6e\u5c42Drawable\u7684\u56db\u4e2a\u70b9
	 private final int EDGE_LT = 1;
	 private final int EDGE_RT = 2;
	 private final int EDGE_LB = 3;
	 private final int EDGE_RB = 4;
	 private final int EDGE_MOVE_IN = 5;
	 private final int EDGE_MOVE_OUT = 6;
	 private final int EDGE_NONE = 7;

	 public int currentEdge = EDGE_NONE;

	 protected float oriRationWH = 0;
	 protected final float maxZoomOut = 5.0f;
	 protected final float minZoomIn = 0.333333f;
	 private final int padding_num = 70;
	 private final static int MIN_DISTANCE = 4;
	 private boolean isMenuInfluence = false;
	 
	 private float[] mMatrixFloats = new float[] {  
			 0.22f, 0.5f, 0.1f, 0, 0,  
	         0.22f, 0.5f, 0.1f, 0, 0,  
	         0.22f, 0.5f, 0.1f, 0, 0,  
	         0, 0, 0, 1, 0  
		};  
	 private ColorMatrix colorMatrix = new ColorMatrix(mMatrixFloats);
	 private ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);

	 protected Drawable mDrawable;
	 public Drawable getmDrawable() {
		return mDrawable;
	}

	public void setmDrawable(Drawable mDrawable) {
		this.mDrawable = mDrawable;
		invalidate();
	}
	protected FloatDrawable mFloatDrawable;

	 protected Rect mDrawableSrc = new Rect();// \u56fe\u7247Rect\u53d8\u6362\u65f6\u7684Rect
	 protected Rect mDrawableDst = new Rect();// \u56fe\u7247Rect
	 protected Rect mDrawableFloat = new Rect();// \u6d6e\u5c42\u7684Rect
	 protected boolean isFrist = true;
	 protected boolean isFingerDown = false;
	 private boolean isTouchInSquare = true;//mark whether the touch point is in the rect

	 protected Context mContext;
	private boolean isCropEnable = false;
	private float mStartDistance;
	
	 public CropImageView(Context context) {
	  super(context);
	  init(context);
	 }

	 public CropImageView(Context context, AttributeSet attrs) {
	  super(context, attrs);
	  init(context);
	 }

	 public CropImageView(Context context, AttributeSet attrs, int defStyle) {
	  super(context, attrs, defStyle);
	  init(context);

	 }

	 @SuppressLint("NewApi")
	 private void init(Context context) {
	  this.mContext = context;
	  try {
	   if (android.os.Build.VERSION.SDK_INT >= 11) {
	    this.setLayerType(LAYER_TYPE_SOFTWARE, null);
	   }
	  } catch (Exception e) {
	   e.printStackTrace();
	  }
	  mFloatDrawable = new FloatDrawable(context);
	 }

	 public void setDrawable(Drawable mDrawable, int cropWidth, int cropHeight) {
	  this.mDrawable = mDrawable;
	  this.cropWidth = cropWidth;
	  this.cropHeight = cropHeight;
	  
	  this.isFrist = true;
	  invalidate();
	 }

	 @SuppressLint("ClickableViewAccessibility")
	 @Override
	 public boolean onTouchEvent(MotionEvent event) {

	  if (event.getPointerCount() > 1) {
	   if (mStatus == STATUS_SINGLE) {
	    mStatus = STATUS_MULTI_START;
	    mStartDistance = distance(event);
	    
	   } else if (mStatus == STATUS_MULTI_START) {
	    mStatus = STATUS_MULTI_TOUCHING;
	   }
	  } else {
	   if (mStatus == STATUS_MULTI_START
	     || mStatus == STATUS_MULTI_TOUCHING) {
	    mX_1 = event.getX();
	    mY_1 = event.getY();
	   }

	   mStatus = STATUS_SINGLE;
	  }

	  switch (event.getAction()) {
	  case MotionEvent.ACTION_DOWN:
	   mX_1 = event.getX();
	   mY_1 = event.getY();
	   currentEdge = getTouch((int) mX_1, (int) mY_1);
	   isTouchInSquare = mDrawableFloat.contains((int) event.getX(),
	     (int) event.getY());
	   isFingerDown = true;

	   break;

	  case MotionEvent.ACTION_UP:
	   checkBounds();
	   isFingerDown = false;
	   offSetToCenter();
	   invalidate();
	   break;
	   
	  case MotionEvent.ACTION_POINTER_UP:
	   currentEdge = EDGE_NONE;
	   break;

	  case MotionEvent.ACTION_MOVE:
	   if (mStatus == STATUS_MULTI_TOUCHING) {

		   //two points is pressed,change the size of the drawable
		   float movingDistance = distance(event);
		   float scale = movingDistance / mStartDistance;
		   mStartDistance = movingDistance;
		   scale = checkScale(scale);
		   resizeDrawable(scale);
		   
	   } else if (mStatus == STATUS_SINGLE) {
	    int dx = (int) (event.getX() - mX_1);
	    int dy = (int) (event.getY() - mY_1);

	    mX_1 = event.getX();
	    mY_1 = event.getY();
	    // \u6839\u64da\u5f97\u5230\u7684\u90a3\u4e00\u4e2a\u89d2\uff0c\u5e76\u4e14\u53d8\u6362Rect
	    if (!(dx == 0 && dy == 0)) {
	     switch (currentEdge) {
	     case EDGE_LT:
	      mDrawableFloat.set(mDrawableFloat.left + dx,
	        mDrawableFloat.top + dy, mDrawableFloat.right,
	        mDrawableFloat.bottom);
	      break;

	     case EDGE_RT:
	      mDrawableFloat.set(mDrawableFloat.left,
	        mDrawableFloat.top + dy, mDrawableFloat.right
	          + dx, mDrawableFloat.bottom);
	      break;

	     case EDGE_LB:
	      mDrawableFloat.set(mDrawableFloat.left + dx,
	        mDrawableFloat.top, mDrawableFloat.right,
	        mDrawableFloat.bottom + dy);
	      break;

	     case EDGE_RB:
	      mDrawableFloat.set(mDrawableFloat.left,
	        mDrawableFloat.top, mDrawableFloat.right + dx,
	        mDrawableFloat.bottom + dy);
	      break;

	     case EDGE_MOVE_IN:
	      if (isTouchInSquare) {
	       mDrawableFloat.offset((int) dx, (int) dy);
	      }
	      break;

	     case EDGE_MOVE_OUT:
	    	 //move the image rect
	    	 if(mDrawableDst.width() > mDrawableSrc.width()) {
	    		 mDrawableDst.offset((int) dx, (int) dy);
	    		 mDrawable.setBounds(mDrawableDst);
	    	 }
	      break;
	     }
	     mDrawableFloat.sort();
	     invalidate();
	    }
	   }
	   break;
	  }

	  return true;
	 }

	 private void resizeDrawable(float scale) {
		 if(scale == 1f) return;
	     int width = ((int)(mDrawableDst.width() * scale)-mDrawableDst.width())>>1;
		 int height = ((int)(mDrawableDst.height() * scale)-mDrawableDst.height())>>1;
		 mDrawableDst.set(mDrawableDst.left - width, mDrawableDst.top - height,
				 mDrawableDst.right + width, mDrawableDst.bottom +height);
		 
		 Log.d("zejia.ye", "resizeDrawable() left="+mDrawableDst.left+" right = "+mDrawableDst.right
				 +" top = "+mDrawableDst.top+ " bottom"+mDrawableDst.bottom);
		 invalidate();
	 }
	 
	 /**
	  * check the scale to prevent the drawable is smaller than the mDrawableSrc
	  * @param scale
	  * @return
	  */
	 private float checkScale(float scale) {
		 float scaleWidth = mDrawableDst.width()*scale;
//		 float scaleHeight = mDrawableDst.height()*scale;
//		 if(scaleWidth <= mDrawableSrc.width()||scaleHeight <= mDrawableSrc.height())
	     if(scaleWidth <= mDrawableSrc.width())
			 return mDrawableSrc.width() / scaleWidth;
		 if(scale>3f) scale = 2f;
		 return scale;
	 }
	 
	 private float distance(MotionEvent event) {
		 float dx = event.getX(1) - event.getX(0);
         float dy = event.getY(1) - event.getY(0);
         return (float)Math.sqrt(dx * dx + dy * dy);
	 }
	 
	 
	 // get the touch area,contains 4 angles,the area in the rect,the area out of rect.
	 public int getTouch(int eventX, int eventY) {
		 Log.d("zejia", "eventX = "+eventX+" eventY = "+eventY);
		 Log.d("zejia", "mFloatDrawable.getBounds().left = "+mFloatDrawable.getBounds().left);
		 Log.d("zejia", "mFloatDrawable.getBounds().top = "+mFloatDrawable.getBounds().top);
		 Log.d("zejia", "mFloatDrawable.getBounds().right = "+mFloatDrawable.getBounds().right);
		 Log.d("zejia", "mFloatDrawable.getBounds().bottom = "+mFloatDrawable.getBounds().bottom);
		 Log.d("zejia", "floatRect.left = "+mDrawableFloat.left);
		 Log.d("zejia", "floatRect.top = "+mDrawableFloat.top);
		 Log.d("zejia", "floatRect.right = "+mDrawableFloat.right);
		 Log.d("zejia", "floatRect.bottom = "+mDrawableFloat.bottom);
		 if(!isCropEnable) {
			 return EDGE_MOVE_OUT;
		 }
		 
		 
	  if (mFloatDrawable.getBounds().left <= eventX
	    && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
	      .getBorderWidth())
	    && mFloatDrawable.getBounds().top <= eventY
	    && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
	      .getBorderHeight())) {
	   return EDGE_LT;
	  } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
	    .getBorderWidth()) <= eventX
	    && eventX < mFloatDrawable.getBounds().right
	    && mFloatDrawable.getBounds().top <= eventY
	    && eventY < (mFloatDrawable.getBounds().top + mFloatDrawable
	      .getBorderHeight())) {
	   return EDGE_RT;
	  } else if (mFloatDrawable.getBounds().left <= eventX
	    && eventX < (mFloatDrawable.getBounds().left + mFloatDrawable
	      .getBorderWidth())
	    && (mFloatDrawable.getBounds().bottom - mFloatDrawable
	      .getBorderHeight()) <= eventY
	    && eventY < mFloatDrawable.getBounds().bottom) {
	   return EDGE_LB;
	  } else if ((mFloatDrawable.getBounds().right - mFloatDrawable
	    .getBorderWidth()) <= eventX
	    && eventX < mFloatDrawable.getBounds().right
	    && (mFloatDrawable.getBounds().bottom - mFloatDrawable
	      .getBorderHeight()) <= eventY
	    && eventY < mFloatDrawable.getBounds().bottom) {
	   return EDGE_RB;
	  } else if (mFloatDrawable.getBounds().contains(eventX, eventY)) {
	   return EDGE_MOVE_IN;
	  } else if (mDrawableDst.contains(eventX, eventY)) {
		  return EDGE_MOVE_OUT;
	}
	  return EDGE_NONE;
	 }

	 @Override
	 protected void onDraw(Canvas canvas) {

	  if (mDrawable == null) {
	   return;
	  }

	  if (mDrawable.getIntrinsicWidth() == 0
	    || mDrawable.getIntrinsicHeight() == 0) {
	   return;
	  }
	  
	  configureBounds();
//	  mDrawable.setColorFilter(colorFilter);
	  // 绘制图片
	  mDrawable.draw(canvas);
	  
	  if(isCropEnable) {
		  
		  canvas.save();
		  // 获得剪切框的补集
		  canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
		  // 对补集上色
		  if (isFingerDown) canvas.drawColor(Color.parseColor("#a0000000"),PorterDuff.Mode.SCREEN);
		  else canvas.drawColor(Color.parseColor("#30302E"));
		  canvas.restore();
		  // 绘制剪切框
		  mFloatDrawable.draw(canvas);
		  if(!isFingerDown)
		  checkBounds();
	  }
	  if(!isFingerDown)
		  offSetToCenter();
	 }
	 
	 protected void configureBounds() {
	  // configureBounds 初始化数据
	  // isFirst 第一次进入，需要初始化
	  if (isFrist) {
	   oriRationWH = ((float) mDrawable.getIntrinsicWidth())
	     / ((float) mDrawable.getIntrinsicHeight());

	   final float scale = mContext.getResources().getDisplayMetrics().density;
//	   int w = Math.min(getWidth(), (int) (mDrawable.getIntrinsicWidth()
//	     * scale + 0.5f));
	   
	   int h =  Math.min(getHeight(), (int) (mDrawable.getIntrinsicHeight()
			     * scale + 0.5f));
	   int w = (int) (h * oriRationWH);

	   int left = (getWidth() - w) / 2;
	   int top = (getHeight() - h) / 2;
	   int right = left + w;
	   int bottom = top + h;

	   mDrawableSrc.set(left, 0, right, bottom-top);
	   mDrawableDst.set(mDrawableSrc);

	   if(cropWidth == 0||cropHeight == 0) {
			  mDrawableFloat.set(mDrawableSrc);
	   } else {
		   int floatWidth = dipTopx(mContext, cropWidth);
		   int floatHeight = dipTopx(mContext, cropHeight);
		   
		   if (floatWidth > getWidth()) {
			   floatWidth = getWidth();
			   floatHeight = cropHeight * floatWidth / cropWidth;
		   }
		   
		   if (floatHeight > getHeight()) {
			   floatHeight = getHeight();
			   floatWidth = cropWidth * floatHeight / cropHeight;
		   }
		   
		   int floatLeft = (getWidth() - floatWidth) / 2;
		   int floatTop = (getHeight() - floatHeight) / 2;
		   mDrawableFloat.set(floatLeft, floatTop, floatLeft + floatWidth,
				   floatTop + floatHeight);
	   }

	   isFrist = false;
	  }

	  mDrawable.setBounds(mDrawableDst);
	  mFloatDrawable.setBounds(mDrawableFloat);
	 }

	 // check whether the bound is out of range
	 protected void checkBounds() {
	  int newLeft = mDrawableFloat.left;
	  int newTop = mDrawableFloat.top;
	  
	  {
		  
		  //Rect  = mDrawable.getBounds();
		  Log.d("zejia.ye", "left = "+mDrawableSrc.left+" top = "+mDrawableSrc.top+"right = "+mDrawableSrc.right+" bottom = "+mDrawableSrc.bottom);
	  }
	  boolean isChange = false;
//	  if (mDrawableFloat.left < getLeft()) {
//	   newLeft = getLeft();
//	   isChange = true;
//	  }
//
//	  if (mDrawableFloat.top < getTop()) {
//	   newTop = getTop();
//	   isChange = true;
//	  }
//
//	  if (mDrawableFloat.right > getRight()) {
//	   newLeft = getRight() - mDrawableFloat.width();
//	   isChange = true;
//	  }
//
//	  if (mDrawableFloat.bottom > getBottom()) {
//	   newTop = getBottom() - mDrawableFloat.height();
//	   isChange = true;
//	  }
	  if (mDrawableFloat.left < mDrawableDst.left) {
		  if(mDrawableDst.left - mDrawableFloat.left <=20) newLeft = mDrawableDst.left;
		  else
		   newLeft = mDrawableFloat.left+20;
		   isChange = true;
		  }

		  if (mDrawableFloat.top < mDrawableDst.top) {
			  if(mDrawableDst.top - mDrawableFloat.top <=20) newTop = mDrawableDst.top;
			  else
		   newTop = mDrawableFloat.top+20;
		   isChange = true;
		  }

		  if (mDrawableFloat.right > mDrawableDst.right) {
			  if(mDrawableFloat.right - mDrawableDst.right <=20) newLeft = mDrawableDst.right - mDrawableFloat.width();
		   newLeft = mDrawableFloat.left - 20;
		   isChange = true;
		  }

		  if (mDrawableFloat.bottom > mDrawableDst.bottom) {
			  if(mDrawableFloat.bottom - mDrawableDst.bottom <=20) newTop = mDrawableDst.bottom - mDrawableFloat.height();
		   newTop = mDrawableFloat.top - 20;
		   isChange = true;
		  }
		  //剪切框宽度大于图片宽度时，缩小剪切框宽度
		  if(mDrawableFloat.width() > mDrawableDst.width()) mDrawableFloat.right = mDrawableFloat.left+mDrawableDst.width();
		  if(mDrawableFloat.height()>mDrawableDst.height()) mDrawableFloat.bottom = mDrawableFloat.top + mDrawableDst.height();
	  mDrawableFloat.offsetTo(newLeft, newTop);
	  if (isChange) {
	   invalidate();
	  }
	 }

	 public Bitmap getCropImage() {
	  Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(),
	    Config.RGB_565);
	  Canvas canvas = new Canvas(tmpBitmap);
	  mDrawable.draw(canvas);
	  Matrix matrix = new Matrix();
	  float scale = (float) (mDrawableSrc.width())
	    / (float) (mDrawableDst.width());
	  matrix.postScale(scale, scale);

	  Bitmap ret = Bitmap.createBitmap(tmpBitmap, mDrawableFloat.left,
	    mDrawableFloat.top, mDrawableFloat.width(),
	    mDrawableFloat.height(), matrix, true);
	  tmpBitmap.recycle();
	  tmpBitmap = null;

	  return ret;
	 }

	 public int dipTopx(Context context, float dpValue) {
	  final float scale = context.getResources().getDisplayMetrics().density;
	  return (int) (dpValue * scale + 0.5f);
	 }
	 
	 public Rect getDrawableRect() {
		 return this.mDrawableDst;
	 }
	 
	 public void setDrawableRect(Rect mDrawableDst) {
		 this.mDrawableDst = mDrawableDst;
		 invalidate();
	 }
	 
	 public void resetRect() {
		 this.mDrawableDst.set(mDrawableSrc);
		 invalidate();
	 }
	 /**
	  * 将剪切框选中区域放大并居中
	  */
	 private void fitRect(){
		 
	 }

	public void setCropEnable(boolean isCropEnable) {
		this.isCropEnable = isCropEnable;
		invalidate();
	}

	public void setCropRect(Rect cropRect) {
		this.mDrawableFloat.set(cropRect);
		
	}

	public void setFitst(boolean isFirst) {
		this.isFrist = isFirst;
	}
	
	private void offSetToCenter() {
		boolean isChange = false;
		int middleX = getLeft() + (getWidth() >> 1);
		int middleY = getTop() + (getHeight() >>1) ;
	    int currentX = mDrawableDst.left + (mDrawableDst.width() >> 1);
	    int currentY = mDrawableDst.top + (mDrawableDst.height() >> 1);
	    int offSetX = Math.abs((middleX - currentX)/4) <= MIN_DISTANCE ? middleX - currentX:(middleX - currentX)/4;
	    int offsetY = Math.abs((middleY - currentY)/4) <= MIN_DISTANCE ? middleY - currentY:(middleY - currentY)/4;
	    isChange = (offSetX !=0||offsetY != 0);
	    if(isChange) {
	    	this.mDrawableDst.offset(offSetX, offsetY);
	    	invalidate();
	    }
	    
	    Log.d("zejia", "middleX="+middleX+" middleY="+middleY);
	    Log.d("zejia", "currentX="+currentX+" currentY="+currentY);
	    Log.d("zejia", "offSetX = "+offSetX+ " offSetY="+offsetY);
	}
	
	}