package com.yineng.ynmessager.bean.login;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.DESUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.SystemUtil;

import org.apache.commons.lang3.StringUtils;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author YINENG Date 2014-12-31 登录线程类，登录界面调用
 */
public class LoginThread implements Runnable {
    public static final String mTAG = "LoginThread";
    // 登陆状态
    public final static int LOGIN_START = 1;
    public final static int LOGIN_FAIL = 2;
    public final static int LOGIN_SUCCESS = 3;
    public final static int LOGIN_TIMEOUT = 504;
    public final static int LOGIN_USER_NOT_EXIST = 5;
    public final static int LOGIN_SERVER_NOT_RESPON = 6;
    public final static int LOGIN_SERVER_ERROR = 502;
    //	public final static int LOGIN_SERVER_TIMEOUT = 504;
    public final static int LOGIN_USER_ACCOUNT_ERROR = 401;
    public final static int LOGIN_USER_ACCOUNT_OUT = 403;//账户失效
    public final static int LOGIN_CANCEL = 800;
    // public final static int LOGIN_INIT_OK = 5;
    // public final static int LOGIN_GETORG_OK = 6;
    // public final static int LOGIN_START_INIT = 7;
    // 用户状态
    public final static int USER_STATUS_ONLINE = LOGIN_SUCCESS;// 正常上线
    public final static int USER_STATUS_OFFLINE = LOGIN_FAIL;// 下线
    public final static int USER_STATUS_NETOFF = LOGIN_TIMEOUT;// 网络异常
    public final static int USER_STATUS_ONLOGIN = LOGIN_START;// 正在登陆
    public final static int USER_STATUS_LOGINED_OTHER = 9;// 其它地方已经登陆了
    public final static int USER_STATUS_SERVER_SHUTDOWN = 10;// 服务器关闭了
    public final static int USER_STATUS_CONNECT_OFF = 11;// xmpp连接断开了
    private String mUserAccount;
    private String mPassword;
    private String mResource;
    private Handler mHandler;
    private XMPPConnection mConnection;
    private boolean isStarting = false;
    private boolean isPlain = false;
    //连接服务器秒数计时
    private int mTimeoutCount = 0;
    //连接服务器超时秒数
    private static final int CONNECT_TIMEOUT = 8;

    /**
     * 登录状态
     */
    private int mLoginStatus = 0;

    /**
     * 用户名密码错误，鉴权失败
     */
    private String mAccountPasswordWrongStr = "SASL authentication failed using mechanism PLAIN";

    /**
     * 服务器没响应
     */
    private String mServerNoResponStr = "No response from the server";
    /**
     * 服务器没响应
     */
    private String mServerNoResponStr1 = "No response from server";
    /**
     * 是否是退出登录
     */
    private boolean mCancelLogin = false;
    private Context context;

    /**
     * 构造函数
     *
     * @param account
     * @param password
     * @param resource
     * @param handler
     * @param connection
     */
    public LoginThread(String account, String password, String resource,
                       Handler handler, XMPPConnection connection, Context context) {
        this.mUserAccount = account;
        this.mPassword = password;
        this.mResource = resource;
        this.mHandler = handler;
        this.mConnection = connection;
        this.context = context;
    }

    public LoginThread(String account, String password, String resource,
                       Handler handler, XMPPConnection connection, Context context, boolean isPlain) {
        this.mUserAccount = account;
        this.mPassword = password;
        this.mResource = resource;
        this.mHandler = handler;
        this.mConnection = connection;
        this.context = context;
        this.isPlain = isPlain;
    }

    /**
     * 发送到Handler消息队列
     *
     * @param arg
     * @param delaymillis
     */
    private void sendHandlerMessage(int arg, long delaymillis) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = arg;
        mHandler.sendMessageDelayed(msg, delaymillis);
    }

    @Override
    public void run() {
        mLoginStatus = 0;
        sendHandlerMessage(LOGIN_START, 0);
        try {
            if (mConnection == null) {
                sendHandlerMessage(LOGIN_FAIL, 500);
                return;
            }
            if (mConnection.isConnected()) {// 首先判断是否还连接着服务器，需要先断开
                mConnection.disconnect();
            }
            if (mConnection != null && !mConnection.isConnected()) {
                //设置socket超时时间
                mConnection.setSocketConnectOutTime(7 * 1000);
                //设置登录超时时间
                mConnection.setLoginTimeout(7 * 1000);
                mConnection.connect();
            }

            //如果是手机登录
            if (isPlain) {
                if (mConnection != null && mConnection.isConnected()) {
                    if (mConnection != null && mConnection.isConnected() && !mConnection.isAuthenticated()) {
                        SASLAuthentication.supportSASLMechanism("PLAIN"); //非匿名
                        String companyCode =
                                LastLoginUserSP.getServerInfoCompanyCode(AppController.getInstance().getApplicationContext());
                        Log.i("登录方式", "非匿名登录；" + "account:" + mUserAccount + ";password:" + mPassword);
                        mConnection.login(mUserAccount, formatPasswordByPhone(mPassword, companyCode), mResource);
                    } else {
                        mLoginStatus = LOGIN_FAIL;
                    }
                } else {
                    sendHandlerMessage(LOGIN_TIMEOUT, 10);
                }
                return;
            }
            //正常登录
            if (mConnection != null && mConnection.isConnected()) {
                if (new LoginUserDao(context).isExistUserNo(LastLoginUserSP.getInstance(context).getUserAccount())) {//首次登录采用匿名登录
                    SASLAuthentication.supportSASLMechanism("PLAIN");
                    L.i("登录方式", "PLAIN" + "非匿名");
                } else {
                    SASLAuthentication.supportSASLMechanism("ANONYMOUS");
                    L.i("登录方式", "ANONYMOUS" + "匿名");
                }
                if (mConnection != null && mConnection.isConnected() && !mConnection.isAuthenticated()) {
                    String companyCode =
                            LastLoginUserSP.getServerInfoCompanyCode(AppController.getInstance().getApplicationContext());
                    Log.i("登录方式", "account:" + mUserAccount + ";password:" + mPassword);
                    mConnection.login(mUserAccount, formatPassword(mPassword, companyCode), mResource);
                } else {
                    mLoginStatus = LOGIN_FAIL;
                }
            } else {
                sendHandlerMessage(LOGIN_TIMEOUT, 10);
                return;
            }
        } catch (XMPPException e) {
            e.printStackTrace();
            handleExceptionStatus(e);
        } finally {
            if (mConnection != null && mConnection.isConnected() && mConnection.isAuthenticated()) {
                if (mCancelLogin) {//取消登录
                    sendHandlerMessage(LOGIN_CANCEL, 0);
                } else {
                    sendHandlerMessage(LOGIN_SUCCESS, 0);
                }
            } else {
                if (mCancelLogin) {
                    return;
                }
                L.e(mTAG,"登录线程异常状态码： "+mLoginStatus+"");
                sendHandlerMessage(mLoginStatus, 500);
            }
        }
    }

    /**
     * 处理登录过程中的异常
     * <table border=1>
     * <hr><td><b>Code</b></td><td><b>XMPP Error</b></td><td><b>Type</b></td></hr>
     * <tr><td>500</td><td>interna-server-error</td><td>WAIT</td></tr>
     * <tr><td>403</td><td>forbidden</td><td>AUTH</td></tr>
     * <tr><td>400</td<td>bad-request</td><td>MODIFY</td>></tr>
     * <tr><td>404</td><td>item-not-found</td><td>CANCEL</td></tr>
     * <tr><td>409</td><td>conflict</td><td>CANCEL</td></tr>
     * <tr><td>501</td><td>feature-not-implemented</td><td>CANCEL</td></tr>
     * <tr><td>302</td><td>gone</td><td>MODIFY</td></tr>
     * <tr><td>400</td><td>jid-malformed</td><td>MODIFY</td></tr>
     * <tr><td>406</td><td>no-acceptable</td><td> MODIFY</td></tr>
     * <tr><td>405</td><td>not-allowed</td><td>CANCEL</td></tr>
     * <tr><td>401</td><td>not-authorized</td><td>AUTH</td></tr>
     * <tr><td>402</td><td>payment-required</td><td>AUTH</td></tr>
     * <tr><td>404</td><td>recipient-unavailable</td><td>WAIT</td></tr>
     * <tr><td>302</td><td>redirect</td><td>MODIFY</td></tr>
     * <tr><td>407</td><td>registration-required</td><td>AUTH</td></tr>
     * <tr><td>404</td><td>remote-server-not-found</td><td>CANCEL</td></tr>
     * <tr><td>504</td><td>remote-server-timeout</td><td>WAIT</td></tr>
     * <tr><td>502</td><td>remote-server-error</td><td>CANCEL</td></tr>
     * <tr><td>500</td><td>resource-constraint</td><td>WAIT</td></tr>
     * <tr><td>503</td><td>service-unavailable</td><td>CANCEL</td></tr>
     * <tr><td>407</td><td>subscription-required</td><td>AUTH</td></tr>
     * <tr><td>500</td><td>undefined-condition</td><td>WAIT</td></tr>
     * <tr><td>400</td><td>unexpected-condition</td><td>WAIT</td></tr>
     * <tr><td>408</td><td>request-timeout</td><td>CANCEL</td></tr>
     * </table>
     *
     * @param e 异常
     */
    private void handleExceptionStatus(XMPPException e) {
        XMPPError error = e.getXMPPError();
        L.e("error == " + error);
        if (error != null) {
            L.e("error.code == " + error.getCode() + " Condition == " + error.getCondition() + " e.getMessage() == " + e.getMessage());
            if (error.getCode() != 0) {
                switch (error.getCode()) {
                    case LOGIN_SERVER_ERROR:
                        mLoginStatus = LOGIN_SERVER_ERROR;
                        break;
                    case LOGIN_TIMEOUT:
                        mLoginStatus = LOGIN_TIMEOUT;
                        break;
                    default:
                        mLoginStatus = LOGIN_FAIL;
                        break;
                }
            } else {
                if ("remote-server-error".equals(error.getCondition())) {
                    mLoginStatus = LOGIN_SERVER_ERROR;
                } else if ("remote-server-timeout".equals(error.getCondition())) {
                    mLoginStatus = LOGIN_TIMEOUT;
                } else if (e.getMessage().contains(mServerNoResponStr)) {
                    mLoginStatus = LOGIN_SERVER_NOT_RESPON;
                } else if (e.getMessage().contains(mServerNoResponStr1)) {
                    mLoginStatus = LOGIN_SERVER_NOT_RESPON;
                } else if (mAccountPasswordWrongStr.equals(e.getMessage())) {
                    mLoginStatus = LOGIN_USER_ACCOUNT_ERROR;
                } else {
                    mLoginStatus = LOGIN_FAIL;
                }
            }
        } else {
            if (e.getMessage().contains(mServerNoResponStr)) {
                mLoginStatus = LOGIN_SERVER_NOT_RESPON;
            } else if (e.getMessage().contains(mServerNoResponStr1)) {
                mLoginStatus = LOGIN_SERVER_NOT_RESPON;
            } else if (mAccountPasswordWrongStr.equals(e.getMessage())) {
                mLoginStatus = LOGIN_USER_ACCOUNT_OUT;
            } else if (e.getMessage().contains("remote-server-timeout")) {
                mLoginStatus = LOGIN_TIMEOUT;
            } else if ("remote-server-error".equals(e.getMessage())) {
                mLoginStatus = LOGIN_SERVER_ERROR;
            } else {
                mLoginStatus = LOGIN_FAIL;
            }
        }
    }

    /**
     * @param password 对密码加密处理
     * @return
     */
    public static String formatPassword(String password, String companyCode) {
        String pass = StringUtils.EMPTY;
        try {
            pass = new JSONObject().put("passwd", password)
                    .put("cversion", 100)
                    .put("code", companyCode)
                    .put("type", 3)
                    .put("localip", StringUtils.EMPTY)
                    .put("reqip", StringUtils.EMPTY)
                    .put("mac", SystemUtil.getImei(AppController.getInstance().getApplicationContext()))
                    .put("mobilesystem", "Android_" + Build.VERSION.SDK_INT + "_" + Build.VERSION.RELEASE)
                    .toString();
        } catch (JSONException e) {
            L.e(mTAG, e.getMessage(), e);
        }

        pass = DESUtil.encrypt(pass, Const.DES_KEY);
        return pass;
    }

    /**
     * @param password 对密码加密处理
     * @return
     */
    public static String formatPasswordByPhone(String password, String companyCode) {
        String pass = StringUtils.EMPTY;
        try {
            pass = new JSONObject().put("verificationType", password)
                    .put("cversion", 100)
                    .put("code", companyCode)
                    .put("type", 3)
                    .put("localip", StringUtils.EMPTY)
                    .put("reqip", StringUtils.EMPTY)
                    .put("mac", SystemUtil.getImei(AppController.getInstance().getApplicationContext()))
                    .put("mobilesystem", "Android_" + Build.VERSION.SDK_INT + "_" + Build.VERSION.RELEASE)
                    .toString();
        } catch (JSONException e) {
            L.e(mTAG, e.getMessage(), e);
        }

        pass = DESUtil.encrypt(pass, Const.DES_KEY);
        return pass;
    }

    /**
     * @param address
     * @return 获取主机名
     */
    public static String getHostFromAddress(String address) {

        if (address.contains(":")) {

            return address.split(":")[0];

        } else {

            return address;
        }
    }

    /**
     * @param address
     * @return 获取端口号
     */
    public static int getPortFromAddress(String address) {

        if (address.contains(":")) {

            return Integer.parseInt(address.split(":")[1]);

        } else {

            return Const.SERVER_PORT;
        }
    }

    /**
     * 设置当前是否取消登录
     *
     * @param isCancelLogin
     */
    public void setCancelLogin(boolean isCancelLogin) {
        mCancelLogin = isCancelLogin;
    }

    /**
     * 获取当前是否取消登录
     *
     * @return
     */
    public boolean getCancelLogin() {
        return mCancelLogin;
    }
}
