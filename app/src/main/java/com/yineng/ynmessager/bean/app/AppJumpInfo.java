package com.yineng.ynmessager.bean.app;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：
 */

public class AppJumpInfo {


    /**
     * appId : leave_view
     * appName : 学生请假
     * appIconId : class_check
     * appUrl : /pages/ynedutapp/index.html#/leave_view
     */

    private String appId;
    private String appName;
    private String appIconId;
    private String appUrl;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppIconId() {
        return appIconId;
    }

    public void setAppIconId(String appIconId) {
        this.appIconId = appIconId;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }
}
