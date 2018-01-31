package com.yineng.ynmessager.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.permession.PermessionManager;
import com.yineng.ynmessager.util.permession.PermissionTool;
import com.yineng.ynmessager.view.dialog.CameraRequestDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 自定义相机拍照
 *  Created by 舒欢
 */

public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private Camera mCamera;
    private int cameraPosition = 0;//0代表前置摄像头,1代表后置摄像头,默认打开前置摄像头
    SurfaceHolder holder;

    SurfaceView mSurfaceView; //自定义相机视图
    View openLight; // 闪光灯视图
    View focusIndex; //聚焦
    View bootomRly;// 底部按键布局
    private float pointX, pointY;
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private int mode;                      //0是聚焦 1是放大
    //放大缩小
    int curZoomValue = 0;
    private float dist;
    Camera.Parameters parameters;
    private Handler handler = new Handler();
    boolean safeToTakePicture = true;

    private ImageView imageshow;   //拍完照之后展示图片用的布局
    private ImageView camera_light_image;
    private View takePhoto;  //拍照
    private View make_ok;  // 保存图片按钮

    //保存的图片文件名
    private String fileName ;
    private File file ;

    private String imagePath;  //图片保存的地址
    private Bitmap bmp; // 拍照生成的图片

    private View cameraSwitch;  // 转换前后摄像头的按钮
    private TextView take_again;  // 重拍按钮

    private View image_info;
    private TextView cancle;

    /**
     * 拍照定位的时候需要添加的水印信息
     *
     */
    private String name ;
    private String time ;
    private String address ;

    private TextView nameview ;
    private TextView timeview ;
    private TextView addressview ;

    private CameraRequestDialog dialog;

    private SensorControler mSensorControler;

    /* 图像数据处理完成后的回调函数 */
    private Camera.PictureCallback mJpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            mSensorControler.lockFocus();
            mCamera.stopPreview();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        //将照片改为竖直方向
                        bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Matrix matrix = new Matrix();
                        switch (cameraPosition) {
                            case 0://前
                                matrix.preRotate(90);
                                break;
                            case 1:
                                matrix.preRotate(270);
                                break;
                        }
                        int bmpWidth = bmp.getWidth();
                        int bmpHeight = bmp.getHeight();
                        bmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);

                        new AnotherTask().execute("");

                    } catch (Exception e) {
                        L.i("CameraAc","CameraAc mjpeg");
                        e.printStackTrace();
                    }

                    safeToTakePicture = true;
//                    mCamera.startPreview();
                }
            }).start();

        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        //接收来自cameraplugin传递过来的参数
        Intent data = getIntent();
        name = data.getStringExtra("name");
        time = data.getStringExtra("time");
        address = data.getStringExtra("address");
        mSensorControler = SensorControler.getInstance();
        mSensorControler.setCameraFocusListener(new SensorControler.CameraFocusListener() {
            @Override
            public void onFocus() {
                if(!mSensorControler.isFocusLocked()){
                    autoFocus();
                }
            }
        });
        CameraUtil.init(this);
        //初始化视图
        initView();
        //初始化自定义相机信息
        initData();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorControler != null) {
            mSensorControler.onStart();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mSensorControler != null) {
            mSensorControler.onStop();
        }
    }

    private void initView() {

        mSurfaceView = (SurfaceView) findViewById(R.id.my_surfaceView);
        mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
        imageshow = (ImageView) findViewById(R.id.imageshow);

        nameview = (TextView) findViewById(R.id.name);
        timeview = (TextView) findViewById(R.id.time);
        addressview = (TextView) findViewById(R.id.address);
        nameview.setText(name);
        timeview.setText(time);
        addressview.setText(address);

        openLight = findViewById(R.id.openLight);
        camera_light_image = (ImageView) findViewById(R.id.camera_light_image);
        focusIndex = findViewById(R.id.focus_index);
        bootomRly = findViewById(R.id.bootomRly);

        cancle = (TextView) findViewById(R.id.cancle);
        takePhoto =  findViewById(R.id.takePhoto);
        make_ok =  findViewById(R.id.make_ok);
        cameraSwitch = findViewById(R.id.cameraSwitch);
        take_again = (TextView) findViewById(R.id.take_again);

        image_info = findViewById(R.id.image_info);
        image_info.setVisibility(View.GONE);
        cancle.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
        make_ok.setOnClickListener(this);
        openLight.setOnClickListener(this);
        cameraSwitch.setOnClickListener(this);
        take_again.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

//点击取消
            case R.id.cancle:
                releaseCamera();
                finish();

                break;
//点击照相
            case R.id.takePhoto:

                if (safeToTakePicture) {
                    safeToTakePicture = false;
                    mCamera.takePicture(null, null, mJpeg);
                }
                break;
// 点击确定，保存图片
            case R.id.make_ok:

                new MakeOkTask(CameraActivity.this).execute("");

                break;
//点击是否需要闪光
            case R.id.openLight:

                turnLight(mCamera);

                break;
//点击转换摄像头
            case R.id.cameraSwitch:

                releaseCamera();
                cameraPosition = (cameraPosition + 1) % mCamera.getNumberOfCameras();
                if (cameraPosition==0){
                    mSensorControler.lockFocus();
                }else {
                    mSensorControler.unlockFocus();
                }
                mCamera = getCamera(cameraPosition);
                if (holder != null) {
                    startPreview(mCamera, holder);
                }
                break;
//点击重拍
            case R.id.take_again:
                releaseCamera();
                mCamera = getCamera(cameraPosition);
                if (holder != null) {
                    startPreview(mCamera, holder);
                }
                take_again.setVisibility(View.INVISIBLE);
                cameraSwitch.setVisibility(View.VISIBLE);
                //销毁当前的展示的图片,避免内存泄露
                if (null != bmp && !bmp.isRecycled()){
                    bmp.recycle();
                    bmp = null;
                }
                break;
            //取消去开启权限
            case R.id.tv_camera_cancle:
                dialog.dismiss();
                finish();
                break;

            case R.id.tv_camera_ok:
                dialog.dismiss();
                PermessionManager.getInstance().applyPermission(this);
                deleteCanmeraActivity();
                break;

        }

    }

    /**
     * 传回所得到图片的文件地址
     */
    private class MakeOkTask extends AsyncTask<String, Void, String> {

        Activity mActivity;
        public MakeOkTask(Activity activity ){
            super();
            this.mActivity = activity;
        }


        @Override
        protected void onPostExecute(String result) {

            //根据图片存放地址获取比例压缩的图片
            Bitmap scaleImage = ImageUtil.getScaleImage(imagePath);

            //质量压缩
            scaleImage  = ImageUtil.getQuaitlyImage(scaleImage);

            //将定位人员的信息加入到图片生成带有水印的照片
            scaleImage = createWaterMaskBitmap(scaleImage,0,scaleImage.getHeight(),name,time,address);

            //保存添加过水印的图片
            ImageUtil.saveWaterBit(scaleImage,imagePath);

            Intent intent = new Intent();
            Bundle res = new Bundle();
            res.putString("imageString",Uri.parse("file://" + new File(imagePath)).toString());
            intent.putExtras(res);

            setResult(RESULT_OK, intent);

            releaseCamera();
            if(scaleImage!=null && !scaleImage.isRecycled()){
                scaleImage.recycle();
            }
            deleteCanmeraActivity();
            this.mActivity.finish();

        }





        @Override
        protected String doInBackground(String... params) {
            //耗时的操作
            return params[0];
        }
    }



    protected void initData() {
        holder = mSurfaceView.getHolder();
        holder.addCallback(this); // 回调接口

        bootomRly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    // 主点按下
                    case MotionEvent.ACTION_DOWN:
                        pointX = event.getX();
                        pointY = event.getY();
                        mode = FOCUS;
                        break;
                    // 副点按下
                    case MotionEvent.ACTION_POINTER_DOWN:
                        dist = spacing(event);
                        // 如果连续两点距离大于10，则判定为多点模式
                        if (spacing(event) > 10f) {
                            mode = ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = FOCUS;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == FOCUS) {
                        } else if (mode == ZOOM) {
                            float newDist = spacing(event);
                            if (newDist > 10f) {
                                float tScale = (newDist - dist) / dist;
                                if (tScale < 0) {
                                    tScale = tScale * 10;
                                }
                                addZoomIn((int) tScale);
                            }
                        }
                        break;
                }
                return false;
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    pointFocus((int) pointX, (int) pointY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
                layout.setMargins((int) pointX - 60, (int) pointY - 60, 0, 0);
                focusIndex.setLayoutParams(layout);
                focusIndex.setVisibility(View.VISIBLE);
                ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                sa.setDuration(800);
                focusIndex.startAnimation(sa);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        focusIndex.setVisibility(View.INVISIBLE);
                    }
                }, 500);
            }
        });
    }

    public void onResume() {
        Log.i("CameraActivity","onResume");
        super.onResume();
        if (PermessionManager.getInstance().checkPermission(this, Const.PERMISSION_OP_CAMERA) || PermissionTool.isCameraCanUse()){
            startCreame();
        }else {
            dialog = new CameraRequestDialog(CameraActivity.this,R.style.MyDialog,CameraActivity.this);
            dialog.show();
        }
    }
    private void startCreame(){
        if (mCamera == null) {
            mCamera = getCamera(cameraPosition);
            if (holder != null) {
                startPreview(mCamera, holder);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    private class AnotherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPostExecute(String result) {
            //对UI组件的更新操作

            takePhoto.setVisibility(View.INVISIBLE);
            make_ok.setVisibility(View.VISIBLE);
            mSurfaceView.setVisibility(View.INVISIBLE);

            take_again.setVisibility(View.VISIBLE);
            cameraSwitch.setVisibility(View.INVISIBLE);
            openLight.setVisibility(View.INVISIBLE);
            imageshow.setVisibility(View.VISIBLE);

            // 首先保存图片
            File appDir = new File(Environment.getExternalStorageDirectory(), "yncamera");
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            fileName = System.currentTimeMillis() + ".jpg";
            file = new File(appDir, fileName);

            //将生产的图片写入文件
            try {
                FileOutputStream fos = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(CameraActivity.this, "保存图片失败", Toast.LENGTH_SHORT).show();
            }

            imagePath = file.toString();
            imageshow.setImageBitmap(bmp);

        }

        @Override
        protected String doInBackground(String... params) {
            //耗时的操作
            return params[0];
        }
    }




    /**
     * 生成带信息的水印图片
     * @param src
     * @param paddingLeft
     * @param paddingTop
     * @param name
     * @param time
     * @param address
     * @return
     */
    private Bitmap createWaterMaskBitmap(Bitmap src, int paddingLeft, int paddingTop, String name, String time, String address) {
        if (src == null) {
            return null;
        }
        Bitmap  bmptime = BitmapFactory.decodeResource(this.getResources(), R.mipmap.camera_time);
        Bitmap  bmpaddress = BitmapFactory.decodeResource(this.getResources(), R.mipmap.camera_address);

        int image_w = 30;//压缩的小图片的宽度
        int image_h = 30;//压缩的小图片的高度
        int bootom_margin = 50;//距离底部距离
        int left_margin = 20; // 距离左边的距离
        int span = 10;// 字体和图片的距离
        bmptime = getBitmapThumbnail(bmptime,image_w,image_h);
        bmpaddress = getBitmapThumbnail(bmpaddress,image_w,image_h);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(35.0F);
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
        paint.setStrokeWidth((float) 3.0);
        android.graphics.Bitmap.Config bitmapConfig = src.getConfig();
        paint.setDither(true); // 获取跟清晰的图像采样
        paint.setFilterBitmap(true);// 过滤一些
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        src = src.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(src);

        //写入name
        canvas.drawText(name, left_margin, paddingTop-image_h*2-span*2-bootom_margin, paint);
        //设置时间地址的字体大小
        paint.setTextSize(35.0F);
        //写入时间和时间的图标
        canvas.drawBitmap(bmptime,left_margin,paddingTop-image_h*2-span-bootom_margin,null);
        canvas.drawText(time, left_margin+image_w+span, paddingTop-image_h-span-bootom_margin, paint);

        //写入地址和地址图标
        canvas.drawBitmap(bmpaddress,left_margin,paddingTop-image_h-bootom_margin,null);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
        textPaint.setTextSize(25.0F);
        StaticLayout sl = new StaticLayout(address,textPaint,src.getWidth()-image_w*2, Layout.Alignment.ALIGN_NORMAL,1.0f,1.0f,false);
        canvas.translate(left_margin+image_w+span,paddingTop-bootom_margin-image_h);
        sl.draw(canvas);

        // 保存
        canvas.save(Canvas.ALL_SAVE_FLAG);
        // 存储
        canvas.restore();
        paint.reset();
        textPaint.reset();
        return src;
    }

    /*
     * 获取Bitmap的缩略图
     */
    public static Bitmap getBitmapThumbnail(Bitmap bmp, int width, int height) {
        Bitmap bitmap = null;
        if (bmp != null) {
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();
            if (width != 0 && height != 0) {
                Matrix matrix = new Matrix();
                float scaleWidth = ((float) width / bmpWidth);
                float scaleHeight = ((float) height / bmpHeight);
                matrix.postScale(scaleWidth, scaleHeight);
                bitmap = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
                        matrix, true);
            } else {
                bitmap = bmp;
            }
        }
        return bitmap;
    }
    /**
     * 闪光灯开关   开->关->自动
     *
     * @param mCamera
     */
    private void turnLight(Camera mCamera) {
        if (mCamera == null || mCamera.getParameters() == null
                || mCamera.getParameters().getSupportedFlashModes() == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        String flashMode = mCamera.getParameters().getFlashMode();
        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();//系统提供可以选择的模式
        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode) && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {//关闭状态
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(parameters);
            camera_light_image.setImageResource(R.mipmap.light_on);
        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {//开启状态
            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera_light_image.setImageResource(R.mipmap.light_off);
                mCamera.setParameters(parameters);
            }
        }
    }


    /**
     * 释放相机资源
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mSensorControler.restFoucs();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        try {
            mSurfaceView.setVisibility(View.VISIBLE);
            imageshow.setVisibility(View.INVISIBLE);
            image_info.setVisibility(View.VISIBLE);
            openLight.setVisibility(View.VISIBLE);
            takePhoto.setVisibility(View.VISIBLE);
            make_ok.setVisibility(View.INVISIBLE);

            mSensorControler.restFoucs();
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CameraUtil.getInstance().setCameraDisplayOrientation(this, cameraPosition, camera);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取Camera实例
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }
    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported 自动对焦
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        Camera.Size previewSize = CameraUtil.findBestPreviewResolution(camera);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        Camera.Size pictrueSize = CameraUtil.getInstance().getPropPictureSize(parameters.getSupportedPictureSizes(), 1000);
        parameters.setPictureSize(pictrueSize.width, pictrueSize.height);

        camera.setParameters(parameters);

    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera!=null){
            mCamera.stopPreview();
            startPreview(mCamera, holder);
            autoFocus();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 两点的距离
     */
    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 缩放
     * @param delta
     */
    private void addZoomIn(int delta) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            Log.d("Camera", "Is support Zoom " + params.isZoomSupported());
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                mCamera.setParameters(params);
                return;
            } else {
                mCamera.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定点对焦的代码
    private void pointFocus(int x, int y) {
        mCamera.cancelAutoFocus();
        parameters = mCamera.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        mCamera.setParameters(parameters);
        autoFocus();
    }


    private void showPoint(int x, int y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            if (parameters.getMaxNumMeteringAreas() > 0) {
                List<Camera.Area> areas = new ArrayList<Camera.Area>();
                //xy变换了
                int rectY = -x * 2000 / CameraUtil.screenWidth + 1000;
                int rectX = y * 2000 / CameraUtil.screenHeight - 1000;

                int left = rectX < -900 ? -1000 : rectX - 100;
                int top = rectY < -900 ? -1000 : rectY - 100;
                int right = rectX > 900 ? 1000 : rectX + 100;
                int bottom = rectY > 900 ? 1000 : rectY + 100;
                Rect area1 = new Rect(left, top, right, bottom);
                areas.add(new Camera.Area(area1, 800));
                parameters.setMeteringAreas(areas);
            }
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mCamera == null) {
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            setupCamera(camera);//实现相机的参数初始化
                        }
                    }
                });
            }
        };
    }
}
