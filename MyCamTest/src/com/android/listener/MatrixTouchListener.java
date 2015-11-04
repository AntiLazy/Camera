package com.android.listener;

import android.R.integer;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class MatrixTouchListener implements OnTouchListener {
    /**
     * 拖动照片模式
     */
    private static final int MODE_DRAG = 1;
    /**
     * 放大缩小照片模式
     */
    private static final int MODE_ZOON = 2;
    /**
     * 不支持Matrix
     */
    private static final int MODE_UNABLE = 3;
    /**
     * 最大缩放级别
     */
    private float mMaxScale = 6;
    /**
     * 双击时缩放级别
     */
    private float mDoubleClickScale = 2;
    
    private int mMode = 0;
    /**缩放开始时手指间距*/
    private float mStartDis;
    
    private Matrix mCurrentMatrix = new Matrix();
    
    private PointF startPointF = new PointF();
    public MatrixTouchListener() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
        return false;
    }

    private void isMatrixEnable() {
        //加载出错时不可缩放
//        if(getScale)
    }
}
