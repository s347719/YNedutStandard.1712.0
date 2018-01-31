package com.yineng.ynmessager.activity.picker.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.view.photoview.PhotoViewAttacher;

import java.io.File;


public class ImageDetailFragment extends Fragment {
    private String mImageUrl;
    private String packetId;
    private int type;
    private boolean downLoad;
    public ImageView mImageView;
    private PhotoViewAttacher mAttacher;
    private PhotoViewAttacher.OnPhotoTapListener listener;
    private Bitmap bitmap;

    private ImageLoader mImageLoader = ImageLoader.getInstance();
    DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build();

    public static ImageDetailFragment newInstance(String imageUrl, int type) {
        final ImageDetailFragment f = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        args.putInt("type", type);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (bitmap!=null){
            bitmap.recycle();
            bitmap=null;
        }

        if (getArguments()!=null){
            mImageUrl = getArguments().getString("url");
            type = getArguments().getInt("type");
            downLoad = getArguments().getBoolean("downLoad");
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.image_view_scan_layout, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageview_scan);
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnPhotoTapListener(listener);
        return v;
    }


    public void setClick(PhotoViewAttacher.OnPhotoTapListener onPhotoTapListener) {
        this.listener = onPhotoTapListener;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!mImageUrl.startsWith("file:")){
            String cacheImgPath = mImageLoader.getDiskCache().get(mImageUrl).getPath();
            File cacheImg = new File(cacheImgPath);
            if (cacheImg.exists()) {
                Bitmap downloadedPic = ImageUtil.getBitmapThumbnail(cacheImgPath,ImageUtil.mDefaultWidth, ImageUtil.mDefaultHeight);
                mImageView.setImageBitmap(downloadedPic);
                mAttacher.update();
            }else {
                loadImage(mImageUrl);
            }
        }else {
            mImageLoader.displayImage(mImageUrl, mImageView, imageOptions, new SimpleImageLoadingListener() {

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
                            message = "图片无法显示";
                            break;
                        case UNKNOWN:
                            message = "未知的错误";
                            break;
                        default:
                                break;
                    }
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mAttacher.update();
                }

            });
        }
    }

    private synchronized void loadImage(String url) {
        mImageLoader.loadImage(url.trim(),imageOptions, new ImageLoadingListener() {

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
                        message = "图片无法显示";
                        break;
                    case UNKNOWN:
                        message = "未知的错误";
                        break;
                    default:
                            break;
                }
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                mImageView.setImageBitmap(loadedImage);
                mAttacher.update();
//                mHandler.sendEmptyMessage(0);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }

        });
    }


}
