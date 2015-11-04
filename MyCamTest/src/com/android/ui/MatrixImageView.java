package com.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class MatrixImageView extends ImageView {
    private final static String TAG = "MatrixImageView";
    /**
     * 矩阵
     */
    private Matrix mMatrix = new Matrix();
    
    private float mImageWidth;
    
    private float mImageHeight;

    private GestureDetector mGestureDetector;
    public MatrixImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        MatrixTouchListener mListener = new MatrixTouchListener();
        setOnTouchListener(mListener);
        mGestureDetector = new GestureDetector(getContext(), new GestureListener(mListener));
        setBackgroundColor(Color.BLACK);
        //缩放类型为FIT_CENTER,表示把图片按比例扩大/缩小大View的宽度，剧中
        setScaleType(ScaleType.FIT_CENTER);
    }
    
    @Override
    public void setImageBitmap(Bitmap bm) {
        // TODO Auto-generated method stub
        super.setImageBitmap(bm);
        //设置完图片后，获取改图片的坐标矩阵
        mMatrix.set(getImageMatrix());
        float[] values = new float[9];
        mMatrix.getValues(values);
        mImageWidth = getWidth()/values[Matrix.MSCALE_X];
        mImageHeight = (getHeight() - values[Matrix.MTRANS_Y]*2)/values[Matrix.MSCALE_Y];
    }
    
    
    public class MatrixTouchListener implements OnTouchListener {
        /**
         * 拖动照片模式
         */
        private static final int MODE_DRAG = 1;
        /**
         * 放大缩小照片模式
         */
        private static final int MODE_ZOOM = 2;
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
        
        private PointF startPoint = new PointF();

        @Override
        public boolean onTouch(View v,MotionEvent event) {
            switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mMode = MODE_DRAG;
                startPoint.set(event.getX(), event.getY());
                isMatrixEnable();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                reSetMatrix();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mMode == MODE_ZOOM) {
                    setZoomMatrix(event);
                }else if(mMode == MODE_DRAG) {
                    setDragMatrix(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(mMode == MODE_UNABLE) return true;
                mMode = MODE_ZOOM;
                mStartDis = distance(event);
                break;
            default:
                break;
            }
            return false;
        }
        private void setDragMatrix(MotionEvent event) {
            if(isZoomChanged()) {
                float dx = event.getX() - startPoint.x;
                float dy = event.getY() - startPoint.y;
                //避免与双击冲突，大于10f才是拖动
                if(Math.sqrt(dx * dx + dy * dy) > 10f) {
                   startPoint.set(event.getX(), event.getY());
                   //在当前基础上移动
                   mCurrentMatrix.set(getImageMatrix());
                   float[] values = new float[9];
                   mCurrentMatrix.getValues(values);
                   dx = checkDxBound(values,dx);
                   dy = checkDyBound(values, dy);
                   mCurrentMatrix.postTranslate(dx, dy);
                   setImageMatrix(mCurrentMatrix);
                }
            }
        }

        private float checkDxBound(float[] values, float dx) {
            float width = getWidth();
            if(mImageWidth * values[Matrix.MSCALE_X] < width) return 0;
            if(values[Matrix.MTRANS_X] + dx > 0)
                dx = -values[Matrix.MTRANS_X];
            else if(values[Matrix.MTRANS_X]+dx < -(mImageWidth*values[Matrix.MSCALE_X] - width))
                dx = -(mImageWidth*values[Matrix.MSCALE_X] - width)-values[Matrix.MTRANS_X];
            return dx;
        }

        /**
         * 设置缩放Matrix
         * @param event
         */
        private void setZoomMatrix(MotionEvent event) {
            // 只有同时触屏两个点才执行
            if(event.getPointerCount() < 2) return;
            float endDis = distance(event);//结束距离
            //两个手指并拢像素大于10
            if(endDis > 10f) {
                float scale = endDis/mStartDis;//获取缩放倍数
                mStartDis = endDis; //重置距离
                mCurrentMatrix.set(getImageMatrix());//初始化Matrix
                float[] values = new float[9];
                mCurrentMatrix.getValues(values);
                
                scale = checkMaxScale(scale,values);
                setImageMatrix(mCurrentMatrix);
            }
        }
        /**
         * 检查scale，使图像缩放后不会超出最大倍数
         * @param scale
         * @param values
         * @return
         */
        private float checkMaxScale(float scale, float[] values) {
            if(scale*values[Matrix.MSCALE_X] > mMaxScale)
                scale = mMaxScale / values[Matrix.MSCALE_X];
            mCurrentMatrix.postScale(scale, scale, getWidth()>>1, getHeight()>>1);
            return scale;
        }

        private float distance(MotionEvent event) {
            float dx = event.getX(1) - event.getX(0);
            float dy = event.getY(1) - event.getY(0);
            return (float)Math.sqrt(dx * dx + dy * dy);
        }

        private void isMatrixEnable() {
            //加载出错时不可缩放
            if(getScaleType()!= ScaleType.CENTER) {
                setScaleType(ScaleType.MATRIX);
            } else {
                mMode = MODE_UNABLE;//设置为不支持手势
            }
        }
        /**
         * 判断是否需要重置
         * @return 当缩放级别小于模板缩放级别时，重置
         */
        private boolean checkRest() {
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //获取当前x轴的缩放级别
            float scale = values[Matrix.MSCALE_X];
            //获得模板的x轴缩放级别，两者坐比较
            mMatrix.getValues(values);
            return scale<values[Matrix.MSCALE_X];
        }
        /**
         * 重置Matrix
         */
        private void reSetMatrix(){
            if(checkRest()) {
                mCurrentMatrix.set(mMatrix);
                setImageMatrix(mCurrentMatrix);
            }
        }
        
        private boolean isZoomChanged(){
            float[] values = new float[9];
            getImageMatrix().getValues(values);
            //获取当前x轴缩放级别
            float scale = values[Matrix.MSCALE_X];
            mMatrix.getValues(values);
            return scale!=values[Matrix.MSCALE_X];
        }
        
        private float checkDyBound(float[] values,float dy) {
            float height = getHeight();
            if(mImageHeight*values[Matrix.MSCALE_Y]<height) return 0;
            if(values[Matrix.MTRANS_Y]+dy > 0) {
                dy = values[Matrix.MTRANS_Y];
            } else if(values[Matrix.MTRANS_Y] + dy < -(mImageHeight*values[Matrix.MSCALE_Y]-height)){
                dy=-(mImageHeight*values[Matrix.MSCALE_Y]-height)-values[Matrix.MTRANS_Y];
            }
            return dy;
        }
        
        public void onDoubleClick() {
            float scale = isZoomChanged()?1:mDoubleClickScale;
            mCurrentMatrix.set(mMatrix);
            mCurrentMatrix.postScale(scale, scale, getWidth()>>1, getHeight()>>2);
            setImageMatrix(mCurrentMatrix);
        }
    }
    private class GestureListener extends SimpleOnGestureListener {
        private final MatrixTouchListener listener;
        public GestureListener(MatrixTouchListener listener) {
            this.listener = listener;
        }
        
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            listener.onDoubleClick();
            return true;
        }
    }
}
