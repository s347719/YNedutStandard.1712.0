package com.yineng.ynmessager.activity.Splash;

import android.content.Context;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.HashMap;

/**
 * Created by yn on 2017/6/27.
 */

public abstract class CheckPopImageFactory {
    /**
     * 启动页
     */
    public static final int SPLASH = 0;

    /**
     * 登录页
     */
    public static final int LOGIN = 1;

    // Application
    private final AppController mApplication = AppController.getInstance();

    private Context mContext;
    private HashMap<String, String> imageInfo; //图片信息

    private LastLoginUserSP lastUser = LastLoginUserSP.getInstance(getmContext());

    public AppController getmApplication() {
        return mApplication;
    }

    public HashMap<String, String> getImageInfo() {
        return imageInfo;
    }

    public void setImageInfo(HashMap<String, String> imageInfo) {
        this.imageInfo = imageInfo;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public LastLoginUserSP getLastUser() {
        return lastUser;
    }

    /**
     * 下载图片
     */
    public abstract void downLoadPopImage();

    /**
     * 判断是否新的图片
     *
     * @return
     */
    public abstract boolean checkNewImage();



}
