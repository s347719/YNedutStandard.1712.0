package com.yineng.ynmessager.app.update;

/**
 * Created by 舒欢
 * Created time: 2017/6/12
 */

public interface CheckResult {
    //云中心访问成功
    int CHECK_VER_SUCCESS = 700;

    //openfire初始化地址未获取到
    int INIT_URL_FAILED = 701;

    //token获取失败
    int YNEDUT_TOKEN_FAILED = 702;

    //YNedut版本号获取失败
    int YNEDUT_VER_FAILED = 703;

    //云中心访问失败
    int CENTER_APPINFO_FAILED = 704;

    //001云端判定当前版本可用，且有更新包（存在强制更新包），但获取更新包时出现异常
    int CENTER_GET_APP_FILE_FAILED = 713;

    //云中心访问成功，但redis错误（服务器的问题）
    int CENTER_REDIS_ERROR = 705;

    //此产品依赖版本号没有查到可用的产品版本！
    int CENTER_NOT_FOUNT_AVAIABLE_PRODUCT = 706;

    //您当前的版本与依赖的产品版本之间不对应，请卸载后重新安装
    int CENTER_APP_UNAVAIABLE = 707;

    //已是最新版本
    int CENTER_APP_IS_NEWEST = 708;

    //用户点了手动更新
    int CENTER_APP_UPDATE_HAND = 709;

    //强制更新时 用户点了退出程序
    int CENTER_APP_UPDATE_QUIT_MUST = 710;

    //用户点了强制更新
    int CENTER_APP_UPDATE_DO_MUST = 711;
    int CENTER_DEPEND_PRODUCE_VER_FAILED = 712;
}
