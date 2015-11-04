package com.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class FloatDrawable extends Drawable {

	 private Context mContext;
	 private int offset = 50;
	 private Paint mLinePaint = new Paint();
	 private Paint mLinePaint2 = new Paint();
	 {
	  mLinePaint.setARGB(200, 50, 50, 50);
	  mLinePaint.setStrokeWidth(1F);
	  mLinePaint.setStyle(Paint.Style.STROKE);
	  mLinePaint.setAntiAlias(true);
	  mLinePaint.setColor(Color.WHITE);
	  //
	  mLinePaint2.setARGB(200, 50, 50, 50);
	  mLinePaint2.setStrokeWidth(7F);
	  mLinePaint2.setStyle(Paint.Style.STROKE);
	  mLinePaint2.setAntiAlias(true);
	  mLinePaint2.setColor(Color.WHITE);
	 }

	 public FloatDrawable(Context context) {
	  super();
	  this.mContext = context;

	 }

	 public int getBorderWidth() {
	  return dipTopx(mContext, offset);//\u6839\u636edip\u8ba1\u7b97\u7684\u50cf\u7d20\u503c\uff0c\u505a\u9002\u914d\u7528\u7684
	 }

	 public int getBorderHeight() {
	  return dipTopx(mContext, offset);
	 }

	 @Override
	 public void draw(Canvas canvas) {

	  int left = getBounds().left;
	  int top = getBounds().top;
	  int right = getBounds().right;
	  int bottom = getBounds().bottom;

	  Rect mRect = new Rect(left + dipTopx(mContext, offset) / 2, top
	    + dipTopx(mContext, offset) / 2, right
	    - dipTopx(mContext, offset) / 2, bottom
	    - dipTopx(mContext, offset) / 2);
	  //\u753b\u9ed8\u8ba4\u7684\u9009\u62e9\u6846
	  canvas.drawRect(mRect, mLinePaint);
	  //\u753b\u56db\u4e2a\u89d2\u7684\u56db\u4e2a\u7c97\u62d0\u89d2\u3001\u4e5f\u5c31\u662f\u516b\u6761\u7c97\u7ebf
	  canvas.drawLine((left + dipTopx(mContext, offset) / 2 - 3.5f), top
	    + dipTopx(mContext, offset) / 2,
	    left + dipTopx(mContext, offset) - 8f,
	    top + dipTopx(mContext, offset) / 2, mLinePaint2);
	  canvas.drawLine(left + dipTopx(mContext, offset) / 2,
	    top + dipTopx(mContext, offset) / 2,
	    left + dipTopx(mContext, offset) / 2,
	    top + dipTopx(mContext, offset) / 2 + 30, mLinePaint2);
	  canvas.drawLine(right - dipTopx(mContext, offset) + 8f,
	    top + dipTopx(mContext, offset) / 2,
	    right - dipTopx(mContext, offset) / 2,
	    top + dipTopx(mContext, offset) / 2, mLinePaint2);
	  canvas.drawLine(right - dipTopx(mContext, offset) / 2,
	    top + dipTopx(mContext, offset) / 2 - 3.5f,
	    right - dipTopx(mContext, offset) / 2,
	    top + dipTopx(mContext, offset) / 2 + 30, mLinePaint2);
	  canvas.drawLine((left + dipTopx(mContext, offset) / 2 - 3.5f), bottom
	    - dipTopx(mContext, offset) / 2,
	    left + dipTopx(mContext, offset) - 8f,
	    bottom - dipTopx(mContext, offset) / 2, mLinePaint2);
	  canvas.drawLine((left + dipTopx(mContext, offset) / 2), bottom
	    - dipTopx(mContext, offset) / 2,
	    (left + dipTopx(mContext, offset) / 2),
	    bottom - dipTopx(mContext, offset) / 2 - 30f, mLinePaint2);
	  canvas.drawLine((right - dipTopx(mContext, offset) + 8f), bottom
	    - dipTopx(mContext, offset) / 2,
	    right - dipTopx(mContext, offset) / 2,
	    bottom - dipTopx(mContext, offset) / 2, mLinePaint2);
	  canvas.drawLine((right - dipTopx(mContext, offset) / 2), bottom
	    - dipTopx(mContext, offset) / 2 - 30f,
	    right - dipTopx(mContext, offset) / 2,
	    bottom - dipTopx(mContext, offset) / 2 + 3.5f, mLinePaint2);

	 }

	 @Override
	 public void setBounds(Rect bounds) {
	  super.setBounds(new Rect(bounds.left - dipTopx(mContext, offset) / 2,
	    bounds.top - dipTopx(mContext, offset) / 2, bounds.right
	      + dipTopx(mContext, offset) / 2, bounds.bottom
	      + dipTopx(mContext, offset) / 2));
	 }

	 @Override
	 public void setAlpha(int alpha) {

	 }

	 @Override
	 public void setColorFilter(ColorFilter cf) {

	 }

	 @Override
	 public int getOpacity() {
	  return 0;
	 }

	 public int dipTopx(Context context, float dpValue) {
	  final float scale = context.getResources().getDisplayMetrics().density;
	  return (int) (dpValue * scale + 0.5f);
	 }

	}