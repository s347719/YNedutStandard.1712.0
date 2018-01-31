package com.yineng.ynmessager.activity.Splash;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by yn on 2017/6/14.
 */

public class SaveImageAsyncTask extends AsyncTask<Bitmap, Integer, String> {

    private Context mContext;
    private HashMap<String, String> imgInfo; //图片信息
    private int saveFlag; //保存类型0：启动页；1：登录页

    /**
     * 启动页
     */
    public static final int SAVE_SPLASH = 0;

    /**
     * 登录页
     */
    public static final int SAVE_LOGIN = 1;

    private static final String IMAGE_PATH() {

        return FileUtil.getUserSDPath(false,"")+ File.separator + "ad";
    }

    public SaveImageAsyncTask(Context context, HashMap<String, String> imgInfo, int saveFlag) {
        mContext = context;
        this.imgInfo = imgInfo;
        this.saveFlag = saveFlag;
    }

    public interface OnSaveImageListener {
        void onSaveImage(Bitmap bitmap);
    }

    /**
     * 获取本地文件夹路径
     *
     * @param flag
     * @return
     */
    public static String localImagePath(int flag) {
        String imageType = "splash";
        //判断是启动页还是登录页
        if (flag == SAVE_LOGIN) {
            imageType = "login";
        }
        String path = IMAGE_PATH() + File.separator + imageType + ".png";
        return path;
    }


    private File mkdirFile(int flag) {
        String imageType = "splash";
        //判断是启动页还是登录页
        if (flag == SAVE_LOGIN) {
            imageType = "login";
        }
        File popularizeImageFile = new File(IMAGE_PATH());
        Log.e("yhu", popularizeImageFile.getPath());
        try {
            if (!popularizeImageFile.exists()) {
                popularizeImageFile.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return popularizeImageFile;
    }

    /**
     * 判断照片是否存在
     *
     * @param flag
     * @return
     */
    public static boolean hasImage(int flag) {
        File file = new File(localImagePath(flag));
        return file.exists();
    }

    public static void deleteImage(int flag) {
        File file = new File(localImagePath(flag));
        Log.e("yhu", "删除展示图片");
        if (file.exists()) {
            file.delete();
        }
    }


    @Override
    protected String doInBackground(Bitmap... params) {
        //文件名
        String fileName = "splash.png";
        if (saveFlag == SAVE_LOGIN) {
            fileName = "login.png";
        }
        File bitmapFile = new File(mkdirFile(saveFlag), fileName);
        if (bitmapFile.exists()) {
            bitmapFile.delete();
        }
        try {
            //保存图片
            Bitmap bitmap = params[0];
            FileOutputStream mFileOutputStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutputStream);
            mFileOutputStream.flush();
            mFileOutputStream.close();
            return bitmapFile.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    protected void onPostExecute(String path) {
        super.onPostExecute(path);
        //保存本地图片地址
        if (!path.equals("") || path.length() != 0) {
            LastLoginUserSP lastLoginUserSP = LastLoginUserSP.getInstance(mContext);
            //判断保存图片类型
            if (saveFlag == SAVE_SPLASH) {
                lastLoginUserSP.saveSplashImageId(imgInfo);
            } else {
                lastLoginUserSP.saveLoginImageId(imgInfo);
            }
        }
    }

    /**
     * byte 转 bitmap
     *
     * @param b
     * @return
     */
    private Bitmap bytes2Bitmap(byte[] b) {
        if (b.length != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(b, 0, b.length, options);
        }
        return null;
    }

}
