package com.yineng.ynmessager.imageloader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.photoview.PhotoViewAttacher;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ImageDetailActivity extends Activity {

    private String url;
    private ImageView image_show;
    private PhotoViewAttacher mAttacher;
    private ImageLoader mImageLoader = ImageLoader.getInstance();
    private DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .cacheInMemory(true).cacheOnDisc(true)
            .showImageForEmptyUri(R.mipmap.defalut_image_break)
            .showImageOnFail(R.mipmap.defalut_image_break)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        image_show = (ImageView) findViewById(R.id.image_show);
        url = getIntent().getStringExtra("url");

        if (url.startsWith("http")){
            httpdownimage();
        }else {
            //把URL解码
            try {
                url = URLDecoder.decode(url.replaceFirst("file://", ""), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Log.e("yhu", url);
            Bitmap bitmap = BitmapFactory.decodeFile(url);
            image_show.setImageBitmap(bitmap);
            mAttacher = new PhotoViewAttacher(image_show);
            mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                }
            });
        }
    }

    private void httpdownimage() {
        mImageLoader.displayImage(url, image_show, imageOptions, new SimpleImageLoadingListener(){

            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }
            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "下载错误";
                        break;
                    case DECODING_ERROR:
                        message = "图片无法显示";
                        break;
                    case NETWORK_DENIED:
                        message = "网络有问题，无法下载";
                        break;
                    case OUT_OF_MEMORY:
                        message = "图片太大无法显示";
                        break;
                    case UNKNOWN:
                        message = "未知的错误";
                        break;
                }
                ToastUtil.toastAlerMessageBottom(ImageDetailActivity.this,message,500);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mAttacher = new PhotoViewAttacher(image_show);
                mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float x, float y) {
                        finish();
                    }
                });
            }

        });
    }


}
