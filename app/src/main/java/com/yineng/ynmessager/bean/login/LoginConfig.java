package com.yineng.ynmessager.bean.login;

/**
 * Created by yhu on 2017/9/12.
 * 登录配置
 */

public class LoginConfig {

    /**
     * 登录方式为手机登录
     */
    public static final int LOGIN_TYPE_PHONE = 1;
    /**
     * 登录方式为帐号登录
     */
    public static final int LOGIN_TYPE_ACCOUNT = 0;

    /*************************************************************************/
    /**
     * 是否支持帐号登录true:开启 false:关闭
     */
    private boolean loginByAccount;
    /**
     * 是否支持手机登录true:开启 false:关闭
     */
    private boolean loginByMobile;
    /**
     * 优先登录方式 0:帐号+密码 1:手机号+动态码
     */
    private int firstLoginWay;
    /**
     * Messenger手机端自动登录天数限制 为空代表不限制天数
     */
    private int msgMobileAutoLoginDay;

    /**
     * 获取第几次后显示验证码 默认第3次
     */
    private int showValidateCodeNumber;

    /**
     * 获取短信动态码的次数
     */
    private int lastGetVerNum;

    public int getShowValidateCodeNumber() {
        return showValidateCodeNumber;
    }

    public int getLastGetVerNum() {
        return lastGetVerNum;
    }

    public void setLastGetVerNum(int lastGetVerNum) {
        this.lastGetVerNum = lastGetVerNum;
    }

    public void setShowValidateCodeNumber(int showValidateCodeNumber) {
        this.showValidateCodeNumber = showValidateCodeNumber;
    }

    public boolean isLoginByAccount() {
        return loginByAccount;
    }

    public void setLoginByAccount(boolean loginByAccount) {
        this.loginByAccount = loginByAccount;
    }

    public boolean isLoginByMobile() {
        return loginByMobile;
    }

    public void setLoginByMobile(boolean loginByMobile) {
        this.loginByMobile = loginByMobile;
    }

    public int getFirstLoginWay() {
        return firstLoginWay;
    }

    public void setFirstLoginWay(int firstLoginWay) {
        this.firstLoginWay = firstLoginWay;
    }

    public int getMsgMobileAutoLoginDay() {
        return msgMobileAutoLoginDay;
    }

    public void setMsgMobileAutoLoginDay(int msgMobileAutoLoginDay) {
        this.msgMobileAutoLoginDay = msgMobileAutoLoginDay;
    }

    @Override
    public String toString() {
        return "LoginConfig{" +
                "isLoginByAccount=" + loginByAccount +
                ", isLoginByMobile=" + loginByMobile +
                ", firstLoginWay=" + firstLoginWay +
                ", msgMobileAutoLoginDay=" + msgMobileAutoLoginDay +
                ", showValidateCodeNumber=" + showValidateCodeNumber +
                '}';
    }
}
