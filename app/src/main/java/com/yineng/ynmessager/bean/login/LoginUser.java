package com.yineng.ynmessager.bean.login;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author YINENG
 *         sb_LoginUser.append("CREATE TABLE [LoginUser]("); // 表名
 *         sb_LoginUser.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,");// 主键,自增
 *         sb_LoginUser.append("[account] varchar(50),"); // 登录帐号
 *         sb_LoginUser.append("[userNo] varchar(50),"); // 用户ID
 *         sb_LoginUser.append("[passWord] varchar(32),"); // 密码
 *         sb_LoginUser.append("[fileSavePath] text,"); //用户接收文件存储路径
 *         sb_LoginUser.append("[firstLoginDate] varchar(30),"); // 第一次登录时间
 *         sb_LoginUser.append("[lastLoginDate] varchar(30),"); // 最近登录时间
 *         sb_LoginUser.append("[lastLoginType] INTEGER ,");    //最后一次登录类型
 *         sb_LoginUser.append("[isUserAccountType] INTEGER ,");    //是否用帐号密码方式登陆过,0:未用过；1:用过
 *         sb_LoginUser.append("[lastLoginPhoneNum] varchar(30) )");    //最后一次登录手机号
 */
public class LoginUser implements Parcelable {
    private int id;
    /**
     * 用户唯一id
     */
    private String userNo;
    /**
     * 登录帐号
     */
    private String account;
    private String passWord;
    private String fileSavePath;
    private String firstLoginDate;
    private String lastLoginDate;//最后登录时间
    private int lastLoginType;//最后登录登录方式
    private String lastLoginPhoneNum;//用户登录手机
    private int isUserAccountType;//是否用帐号密码方式登陆过


    public int getIsUserAccountType() {
        return isUserAccountType;
    }

    public void setIsUserAccountType(int isUserAccountType) {
        this.isUserAccountType = isUserAccountType;
    }

    public String getLastLoginPhoneNum() {
        return lastLoginPhoneNum;
    }

    public void setLastLoginPhoneNum(String lastLoginPhoneNum) {
        this.lastLoginPhoneNum = lastLoginPhoneNum;
    }

    public int getLastLoginType() {
        return lastLoginType;
    }

    public void setLastLoginType(int lastLoginType) {
        this.lastLoginType = lastLoginType;
    }

    public static final Parcelable.Creator<LoginUser> CREATOR = new Parcelable.Creator<LoginUser>() {

        @Override
        public LoginUser createFromParcel(Parcel source) {
            return new LoginUser(source);
        }

        @Override
        public LoginUser[] newArray(int size) {
            return new LoginUser[size];
        }

    };

    public LoginUser() {
    }

    private LoginUser(Parcel source) {
        this.id = source.readInt();
        this.userNo = source.readString();
        this.account = source.readString();
        this.passWord = source.readString();
        this.fileSavePath = source.readString();
        this.firstLoginDate = source.readString();
        this.lastLoginDate = source.readString();
        this.lastLoginType = source.readInt();
    }

    public LoginUser(String loginAccount) {
        this.setAccount(loginAccount);
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(String lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getFirstLoginDate() {
        return firstLoginDate;
    }

    public void setFirstLoginDate(String firstLoginDate) {
        this.firstLoginDate = firstLoginDate;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileSavePath() {
        return fileSavePath;
    }

    public void setFileSavePath(String fileSavePath) {
        this.fileSavePath = fileSavePath;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.userNo);
        dest.writeString(this.account);
        dest.writeString(this.passWord);
        dest.writeString(this.fileSavePath);
        dest.writeString(this.firstLoginDate);
        dest.writeString(this.lastLoginDate);
        dest.writeInt(this.lastLoginType);
    }
}
