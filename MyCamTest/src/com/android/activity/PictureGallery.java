package com.android.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.android.util.StorageUtil;

import com.example.mycamtest.R;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PictureGallery extends Activity {
    private static String TAG = "CameraActivity";
    private GridView pictureGridView;
    
    
    /**
     * 图片列表
     */
    private ArrayList<String> pictureList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        this.pictureGridView = (GridView)this.findViewById(R.id.pictures_gridview);
//        Thread thread = new Thread(new Runnable() {
//            
//            @Override
//            public void run() {
//                // TODO Auto-generated method stub
//                loadValidePictureFile();
//                pictureGridView.setAdapter(new ImageAdapter(PictureGallery.this));
//            }
//        });
//        thread.start();
        ImageAdapter adapter = new ImageAdapter(this);
        this.pictureGridView.setAdapter(adapter);
        
        LoadPicturesTask task = new LoadPicturesTask(this, adapter);
    	task.execute(StorageUtil.getStoragePath());
    }
    
//    private class ImageAdapter implements ListAdapter{
//        private Context context;
//        public ImageAdapter(Context context) {
//            this.context = context;
//        }
//        @Override
//        public void registerDataSetObserver(DataSetObserver observer) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public void unregisterDataSetObserver(DataSetObserver observer) {
//            // TODO Auto-generated method stub
//            
//        }
//
//        @Override
//        public int getCount() {
//            // TODO Auto-generated method stub
//            return pictureList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            // TODO Auto-generated method stub
//            return pictureList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            // TODO Auto-generated method stub
//            return position;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if(convertView == null) {
//                convertView =  LayoutInflater.from(this.context).inflate(R.layout.picture_item, null);
//            }
//            final ImageView imageView = (ImageView)convertView.findViewById(R.id.picture_item);
//            final int index = position;
//            imageView.setImageBitmap(BitmapFactory.decodeFile(pictureList.get(position)));
//            imageView.setTag(pictureList.get(position));
//            imageView.setOnClickListener(new OnClickListener() {
//                
//                @Override
//                public void onClick(View v) {
//                    Log.d(TAG, "gallery imageview is clicked!");
////                    Intent intent = new Intent(PictureGallery.this, PictureUI.class);
//                    Intent intent = new Intent(PictureGallery.this, PicturePages.class);
//                    intent.putExtra("picturePath", imageView.getTag().toString());
//                    intent.putExtra("position", index);
//                    intent.putExtra("pictures", pictureList);
//                    startActivity(intent);
//                }
//            });
//            return convertView;
//        }
//
//        @Override
//        public int getItemViewType(int position) {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        @Override
//        public int getViewTypeCount() {
//            // TODO Auto-generated method stub
//            return pictureList.size();
//        }
//
//        @Override
//        public boolean isEmpty() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean areAllItemsEnabled() {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        @Override
//        public boolean isEnabled(int position) {
//            // TODO Auto-generated method stub
//            return false;
//        }
//        
//    }
    public class ImageAdapter extends BaseAdapter{
        private Context context;
        private ArrayList<Bitmap> pictureList;
        private ArrayList<String> picturePathList;
        private int mChildCount = 0;
        
        public ImageAdapter(Context context) {
        	this.context = context;
        	this.pictureList = new ArrayList<Bitmap>();
        	this.picturePathList = new ArrayList<String>();
        }
       
        @Override
        public int getItemViewType(int position) {
        	// TODO Auto-generated method stub
        	return super.getItemViewType(position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return pictureList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return pictureList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	if(convertView == null) {
              convertView =  LayoutInflater.from(this.context).inflate(R.layout.picture_item, null);
          }
//            if(convertView == null) {
//                convertView =  new ImageView(context);
//            }
        	final ImageView imageView = (ImageView)convertView.findViewById(R.id.picture_item);
        	final int index = position;
        	imageView.setImageBitmap(pictureList.get(position));
        	imageView.setTag(this.picturePathList.get(position));
        	imageView.setOnClickListener(new OnClickListener() {
              
              @Override
              public void onClick(View v) {
                  Log.d(TAG, "gallery imageview is clicked!");
//                  Intent intent = new Intent(PictureGallery.this, PictureUI.class);
                  Intent intent = new Intent(PictureGallery.this, PicturePages.class);
                  intent.putExtra("picturePath", imageView.getTag().toString());
                  intent.putExtra("position", index);
                  intent.putExtra("pictures", picturePathList);
                  startActivity(intent);
              }
          });
            return convertView;
        }



        @Override
        public boolean isEmpty() {
            // TODO Auto-generated method stub
            return pictureList.size()==0;
        }

        @Override
        public boolean areAllItemsEnabled() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            // TODO Auto-generated method stub
            return false;
        }
        
        public void addItem(Bitmap bitmap) {
        	this.pictureList.add(bitmap);
        }
        public void removeItem(Bitmap bitmap) {
        	if(this.pictureList.contains(bitmap)) 
        		this.pictureList.remove(bitmap);
        }
        public void removeItem(int index) {
        	if(index > 0 ||index < this.pictureList.size())
        		this.pictureList.remove(index);
        }
        public ArrayList<Bitmap> getBitmaps() {
        	return this.pictureList;
        }
        
        public ArrayList<String> getPaths() {
        	return this.picturePathList;
        }
        
    }
    private void loadValidePictureFile() {
       pictureList = new ArrayList<String>();
        String path = StorageUtil.getStoragePath();
        File[] files = new File(path).listFiles();
        for(File file:files) {
            Log.d(TAG, "file.getName() = " + file.getName());
            if(file.getName().endsWith(".jpg")) {
                Log.d(TAG, "file.getAbsolutePath() = "+file.getAbsolutePath());
                pictureList.add(file.getAbsolutePath());
            }
        }
    }
}
