package com.yineng.ynmessager.app.update;

/**
 * Created by 舒欢
 * Created time: 2017/6/12
 */

public interface CheckVersionListener {
    /**
     * 检测升级信息结果
     *
     * @param checkResult 检测结果
     * @param isHandCheck 是否是关于界面，手动更新
     */
    void onCheckVerResult(int checkResult, boolean isHandCheck);

    /**
     * 处理更新版本方法
     *
     * @param handleType 处理方式
     * @param isHandCheck 是否是关于界面，手动更新
     */
    void onHandleUpdateVer(int handleType, boolean isHandCheck);
}
