package com.android.activity;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.example.mycamtest.R;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;

public class PictureUI extends Activity {
    private GridView gridView;
    private ImageView imageView;
    private Bitmap bitmap;
    private String picturePath;
    private int position;
    private List<String> pictureList;
@Override
protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture);
    this.imageView = (ImageView)this.findViewById(R.id.pictureImageView);
    Intent intent = getIntent();
    this.picturePath = intent.getStringExtra("picturePath");
    this.position = intent.getIntExtra("position",0);
    this.pictureList = (ArrayList<String>)intent.getStringArrayListExtra("pictures");
    this.bitmap = BitmapFactory.decodeFile(picturePath);
    if(this.bitmap!=null) imageView.setImageBitmap(this.bitmap);
}
}
