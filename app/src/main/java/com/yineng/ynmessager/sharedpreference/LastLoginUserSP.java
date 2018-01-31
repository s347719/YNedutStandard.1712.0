package com.yineng.ynmessager.sharedpreference;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.bean.login.LoginConfig;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

/**
 * @author YINENG
 * @Date 2014-12-29 保存当前用户的登陆信息
 */
public class LastLoginUserSP {
    private static final String YNEDUT_URL = "YnedutUrl";
    private static LastLoginUserSP lastLoginUserSP;// 类变量
    private static SharedPreferences mSharedPreferences;// 成员sp
    private static SharedPreferences.Editor mEditor;// 成员Editor
    private static String KEY_SERVER_URLS = "user_server_urls";
    private final String TAG = this.getClass().getSimpleName();

    /**
     * 历史广播未读数量
     */
    public static final String UNREAD_BROADCAST_IDS = "unread_broadcast_ids";
    /**
     * 本地启动页图片id
     */
    public static final String SPLASH_LOCAL_IMAGE_ID = "splash_local_image_id";
    /**
     * 本地登录页页图片id
     */
    public static final String LOGIN_LOCAL_IMAGE_ID = "login_local_image_id";
    /**
     * 服务器登录配置
     */
    public static final String SERVICE_LOGIN_CONFIG = "service_login_config";
    /**
     * 本地登录配置
     */
    public static final String USER_LOGIN_CONFIG = "user_login_config";
    /**
     * 用户是否同步服务器APP
     */
    public static final String SYNC_SERVICE_APP = "sync_service_app";

    /**
     * 保存smesis 用户id
     */
    private final String SMESIS_USER_ID = "smesis_user_id";


    /**
     * @param context 私有构造函数
     */
    private LastLoginUserSP(Context context) {
        mSharedPreferences = context.getSharedPreferences("lastusersp", Context.MODE_MULTI_PROCESS);
        mEditor = mSharedPreferences.edit();
    }

    /**
     * @param context
     * @return 懒汉式单例方法
     */
    public static LastLoginUserSP getInstance(Context context) {
        if (lastLoginUserSP == null) {
            lastLoginUserSP = new LastLoginUserSP(context);
        }
        return lastLoginUserSP;
    }

    /**
     * 获取当前登录用户的唯一id
     */
    public static String getLoginUserNo(Context context) {
        return getLoginUserNo(context, true);
    }

    public static String getLoginUserNo(Context context, boolean isOriginal) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        String originId = nowLoginUserSP.getUserAccount();
        return isOriginal ? originId : StringUtils.substringBefore(originId, "_");
    }

    /**
     * 是否存在已登录的帐号
     *
     * @return
     */
    public static boolean isExistLoginedUser(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        if (nowLoginUserSP.isExistsUser()) {
            nowLoginUserSP.saveIsLogin(true);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存用户类型
     *
     * @param context
     * @param userType 用户类型：0：所有类型 1：老师 2：学生 3：家长  4：企业
     * @return Returns true if the new values were successfully written to persistent storage.
     */
    public static boolean saveUserType(Context context, int userType) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.saveUserType(userType);
    }

    /**
     * 获取用户类型
     *
     * @return 当前用户在V8的角色类型：0：所有类型 1：老师 2：学生 3：家长  4：企业，失败（没有对应的键值）返回-1
     */
    public static int getUserType(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getUserType();
    }

    public static void saveServerInfoCenterHost(@NonNull Context context, String centerHost) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_CenterHost", centerHost).apply();
    }

    public static String getServerInfoCenterHost(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_CenterHost", StringUtils.EMPTY);
    }

    public static void saveServerInfoCloudHost(@NonNull Context context, String cloudHost) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_CloudHost", cloudHost).apply();
    }

    public static String getServerInfoCloudHost(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_CloudHost", StringUtils.EMPTY);
    }

    public static void saveServerInfoCompanyName(@NonNull Context context, String companyName) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_CompanyName", companyName).apply();
    }

    public static String getServerInfoCompanyName(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_CompanyName", StringUtils.EMPTY);
    }

    //保存学校的code 纯数字
    public static void saveServerInfoCompanyCode(@NonNull Context context, String companyCode) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_CompanyCode", companyCode).apply();
    }
    public static String getServerInfoCompanyCode(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_CompanyCode", StringUtils.EMPTY);
    }


    // 保存学校当前的code 用于区分安装的applicationid
    public static void saveServerInfoCode(@NonNull Context context, String code) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_Code", code).apply();
    }
    public static String getServerInfoCode(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_Code", StringUtils.EMPTY);
    }


    //    保存服务器的依赖版本号
    public static void saveServerInfoLandSysVersion(@NonNull Context context, String landSysVersion) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_LandSysVersion", landSysVersion).apply();
    }

    public static String getServerInfoLandSysVersion(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_LandSysVersion", StringUtils.EMPTY);
    }

    public static void saveServerInfoOpenFireHost(@NonNull Context context, String openFireHost) {
        LastLoginUserSP.getInstance(context).getEditor().putString("ServerInfo_OpenFireHost", openFireHost).apply();
    }

    public static String getServerInfoOpenFireHost(Context context) {
        return LastLoginUserSP.getInstance(context)
                .getSharedPreferences()
                .getString("ServerInfo_OpenFireHost", StringUtils.EMPTY);
    }

    public static void saveGpsRuleUrl(@NonNull Context context, String url) {
        LastLoginUserSP.getInstance(context).getEditor().putString("GPS_Rule", url).apply();
    }

    public static String getGpsRuleUrl(Context context) {
        return LastLoginUserSP.getInstance(context).getSharedPreferences().getString("GPS_Rule", StringUtils.EMPTY);
    }

    public static void saveGpsSubmitUrl(@NonNull Context context, String url) {
        LastLoginUserSP.getInstance(context).getEditor().putString("GPS_Submit", url).apply();
    }

    public static String getGpsSubmitUrl(Context context) {
        return LastLoginUserSP.getInstance(context).getSharedPreferences().getString("GPS_Submit", StringUtils.EMPTY);
    }

    /**
     * 保存gps定位批量提交的接口
     *
     * @param context
     * @param url
     * @return
     */
    public static void saveBathGpsSubmitUrl(Context context, String url) {
        LastLoginUserSP.getInstance(context).getEditor().putString("GPS_Bath_Submit", url).apply();
    }

    /**
     * 获取gps批量提交的接口
     *
     * @param context
     * @return
     */
    public static String getBathGpsSubmitUrl(Context context) {
        return LastLoginUserSP.getInstance(context).getSharedPreferences().getString("GPS_Bath_Submit", StringUtils.EMPTY);
    }


    /**
     * @return 获取现在登陆的地址
     */
    public static String getUserLoginServiceUrl(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getUserServicesAddress();
    }

    /**
     * 获取登录帐号
     */
    public static String getLoginName(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getUserLoginAccount();
    }

    /**
     * @param context
     * @return获取用户登录过的服务器地址
     */
    public static Set<String> getUserServerUrls(Context context) {
        return getUserServiceUrls(context);
    }

    /**
     * @return 保存用户登录过的服务器地址
     */
    public static boolean setUserServerUrls(Context context, Set<String> urlSet) {
        if (urlSet == null) {
            return false;
        }
        return saveUerServerUrl(context, urlSet);
    }

    /**
     * @param v8version 保存V8版本号
     */
    public static boolean setYnedutVerion(Context context, String v8version) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.saveYnedutVersion(v8version);
    }

    /**
     * 获取v8版本号
     */
    public static String getYnedutVersion(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getYnedutVersion();
    }

    /**
     * 获取新版本信息
     * @return
     */
    public  String getNewVersionCode() {
        return mSharedPreferences.getString("new_ynedut_version", "0");
    }
    /**
     * 保存新版本信息
     * @param newVersion
     * @return
     */
    public  boolean setNewVersionCode(String newVersion){
        mEditor.putString("new_ynedut_version", newVersion);
        return mEditor.commit();
    }


    /**
     * 获取当前用户id的头像servertime
     */
    public static String getAvatarServerTime(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getSharedPreferences().getString(nowLoginUserSP.getUserAccount(), "0");
    }

    /**
     * 设置当前头像的servertime
     */
    public static boolean setAvatarServerTime(Context context, String userNo, String serverTime) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getEditor().putString(userNo, serverTime).commit();
    }

    /**
     * 保存openfire配置的v8服务器地址
     */
    public static boolean saveYnedutServerIP(Context context, String v8Url) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getEditor().putString(YNEDUT_URL, v8Url).commit();
    }

    /**
     * 获取当前openfire对应的V8服务器地址
     */
    public static String getYnedutServerIP(Context context) {
        LastLoginUserSP nowLoginUserSP = getInstance(context);
        return nowLoginUserSP.getSharedPreferences().getString(YNEDUT_URL, "");
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public SharedPreferences.Editor getEditor() {
        return mEditor;
    }

    /**
     * @return 获取用户昵称
     */
    public String getUserName() {
        return mSharedPreferences.getString("lastLoginUser_name", "");
    }

    /**
     * @param name 保存用户昵称
     */
    public void saveUserName(String name) {
        mEditor.putString("lastLoginUser_name", name);
        mEditor.commit();
    }

    /**
     * @return获取用户登录帐号
     */
    public String getUserLoginAccount() {
        return mSharedPreferences.getString("lastLoginUser_account", "");
    }

    /**
     * @param number 保存用户登录帐号
     */
    public void saveUserLoginAccount(String number) {
        mEditor.putString("lastLoginUser_account", number);
        mEditor.commit();
    }

    /**
     * @return获取用户唯一ID
     */
    public String getUserAccount() {
        return mSharedPreferences.getString("lastLoginUser_no", "");
    }

    /**
     * @param number 保存用户唯一ID
     */
    public void saveUserAccount(String number) {
        mEditor.putString("lastLoginUser_no", number);
        mEditor.commit();
    }

    /**
     * @return 获取用户登陆密码
     */
    public String getUserPassword() {
        return mSharedPreferences.getString("lastLoginUser_password", null);

    }

    /**
     * @param password 保存用户登陆密码
     */
    public void saveUserPassword(String password) {
        mEditor.putString("lastLoginUser_password", password);
        mEditor.commit();
    }

    /**
     * oa待办中发起申请提示条目
     *
     * @param toast
     */
    public void saveEventSendRequestHint(boolean toast) {
        mEditor.putBoolean("event_send_request_hint", toast);
        mEditor.commit();
    }

    /**
     * oa待办中取出值是否提示
     *
     * @return
     */
    public boolean getEventSendRequestHint() {
        return mSharedPreferences.getBoolean("event_send_request_hint", false);
    }


    /**
     * @param saddress 保存登陆地址
     */
    public void saveUserServicesAddress(final String saddress) {
        mEditor.putString("lastLoginUser_saddress", saddress);
        mEditor.commit();
    }

    /**
     * @return 获取登陆地址
     */
    public String getUserServicesAddress() {
        return mSharedPreferences.getString("lastLoginUser_saddress", "");
    }

    /**
     * @param userType 当前用户在V8的角色类型：0：所有类型 1：老师 2：学生 3：家长  4：企业
     *                 保存用户类型
     */
    public boolean saveUserType(int userType) {
        mEditor.putInt("lastUserType", userType);
        return mEditor.commit();
    }

    /**
     * @return 获取用户类型：0：所有类型 1：老师 2：学生 3：家长  4：企业，失败（没有对应的键值）返回-1
     */
    public int getUserType() {
        return mSharedPreferences.getInt("lastUserType", -1);
    }

    public void clearAllUserInfo() {
        mEditor.clear();
        mEditor.commit();
    }

    /**
     * @param accountKey 是否首次登陆
     * @return
     */
    public boolean isFirstLogin(String accountKey) {
        return mSharedPreferences.getBoolean("isFirstLogin", true);
    }

    /**
     * @param accountKey
     * @param isFirstLogin 设置是否首次登陆
     */
    public void setIsFirstLogin(String accountKey, boolean isFirstLogin) {

        mEditor.putBoolean("isFirstLogin", isFirstLogin);
        mEditor.commit();
    }

    public boolean isExistsUser() {
        if (!TextUtils.isEmpty(this.getUserAccount()) && !TextUtils.isEmpty(this.getUserPassword())
                && !TextUtils.isEmpty(this.getUserServicesAddress())) {
            this.saveIsLogin(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean isLogin() {
        if (getIsLogin()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return
     */
    public boolean getIsLogin() {
        return mSharedPreferences.getBoolean("isLogin", false);
    }

    /**
     * @param
     */
    public void saveIsLogin(Boolean b) {
        mEditor.putBoolean("isLogin", b);
        mEditor.commit();
    }

    /**
     * @return获取用户登录的服务器地址
     */
    private static Set<String> getUserServiceUrls(Context context) {
        SharedPreferences server_history = context.getSharedPreferences("login_server_history", Context.MODE_MULTI_PROCESS);
        return server_history.getStringSet(KEY_SERVER_URLS, null);
    }

    /**
     * 保存启动页图片本地地址
     *
     * @param imgInfo
     * @return
     */
    public boolean saveLoginImageId(HashMap<String, String> imgInfo) {
        JSONObject js = new JSONObject(imgInfo);
        String jsonStr = js.toString();
        mEditor.putString(LOGIN_LOCAL_IMAGE_ID, jsonStr);
        return mEditor.commit();
    }

    /**
     * 判断是否有新的图片
     *
     * @return
     */
    public boolean hasImageId(String jsonStr, String flag) {
        try {
            JSONObject jsonObject = new JSONObject(mSharedPreferences.getString(flag, ""));
            JSONObject newJsonObj = new JSONObject(jsonStr);
            String id = jsonObject.getString("id");
            String filePath = jsonObject.getString("filePath");
            String url = jsonObject.getString("url");

            String newId = newJsonObj.getString("id");
            String newFilePatth = newJsonObj.getString("filePath");
            String newUrl = newJsonObj.getString("url");

            if (!id.equals(newId) || !filePath.equals(newFilePatth) || !url.equals(newUrl)) {
                return true;
            }
            Log.e("yhu", "objStr:" + jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    /**
     * 保存启动页图片本地地址和
     *
     * @param imgInfo
     * @return
     */
    public boolean saveSplashImageId(HashMap<String, String> imgInfo) {
        JSONObject js = new JSONObject(imgInfo);
        String jsonStr = js.toString();
        mEditor.putString(SPLASH_LOCAL_IMAGE_ID, jsonStr);
        return mEditor.commit();
    }

    /**
     * 清除展示页或登录页图片信息
     *
     * @param type
     * @return
     */
    public boolean deletePopImage(String type) {
        mEditor.putString(type, "");
        return mEditor.commit();
    }

    /**
     * 获取图片信息
     *
     * @param type
     * @return
     * @throws JSONException
     */
    public HashMap<String, String> getImageInfo(String type) throws JSONException {
        HashMap<String, String> resultInfo = null;
        try {
            resultInfo = new HashMap<>();
            String jsonStr = mSharedPreferences.getString(type, "");
            JSONObject jsonObject = new JSONObject(jsonStr);
            resultInfo.put("id", jsonObject.getString("id"));
            resultInfo.put("url", jsonObject.getString("url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultInfo;
    }


    /**
     * 保存用户登录的服务器地址
     *
     * @param context
     * @param urlSet
     * @return保存成功与否
     */
    private static boolean saveUerServerUrl(Context context, Set<String> urlSet) {
        SharedPreferences.Editor edit = context.getSharedPreferences("login_server_history", Context.MODE_MULTI_PROCESS).edit();
        edit.clear();
        edit.putStringSet(KEY_SERVER_URLS, urlSet);
        return edit.commit();
    }

    /**
     * 清空用户登录的服务器地址
     *
     * @param context
     */
    public void cleanUserServerUrls(Context context) {
        SharedPreferences.Editor edit = context.getSharedPreferences("login_server_history", Context.MODE_MULTI_PROCESS).edit();
        edit.clear();
    }

    /**
     * @param version 保存V8版本号
     */
    public boolean saveYnedutVersion(String version) {
        mEditor.putString("ynedut_version", version);
        return mEditor.commit();
    }

    /**
     * 保存服务器登录配置
     *
     * @param loginConfig
     */
    public void saveServiceLoginConfig(LoginConfig loginConfig) {
        String json = JSON.toJSONString(loginConfig);
        mEditor.putString(SERVICE_LOGIN_CONFIG, json);
        Log.i(TAG, "保存服务器登录配置:" + json);
        mEditor.commit();
    }

    /**
     * 获取服务器登录配置
     *
     * @return
     */
    public LoginConfig getServiceLoginConfig() {
        LoginConfig loginConfig = null;
        try {
            String jsonStr = mSharedPreferences.getString(SERVICE_LOGIN_CONFIG, "");
            loginConfig = JSON.parseObject(jsonStr, LoginConfig.class);
            Log.i(TAG, "获取服务器登录配置:" + loginConfig.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return loginConfig;
    }

    /**
     * 保存smesis 用户id
     *
     * @return
     */
    public boolean saveSmesisUserId(String smesisId) {
        mEditor.putString(SMESIS_USER_ID, smesisId);
        return mEditor.commit();
    }

    /**
     * 获取smesis用户id
     *
     * @return
     */
    public String getSmesisUserId() {
        String result = mSharedPreferences.getString(SMESIS_USER_ID, "");
        return result;
    }

    /**
     * 保存未读广播消息数量
     *
     * @param ids
     * @return
     */
    public boolean saveUnreadBroadcastIds(Set<String> ids) {
        mEditor.putStringSet(UNREAD_BROADCAST_IDS, ids);
        return mEditor.commit();
    }

    /**
     * 获取未读广播消息数量
     *
     * @return
     */
    public Set<String> getUnreadBroadcastIds() {
        Set<String> ids = mSharedPreferences.getStringSet(UNREAD_BROADCAST_IDS, null);
        return ids;
    }


    public String getYnedutVersion() {
        return mSharedPreferences.getString("ynedut_version", "0");
    }
}
