package com.yineng.ynmessager.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


/**
 * Created by yn on 2017/9/25.
 * 同步服务器登录配置
 */

public class SyncServerLoginConfig extends Service {

    private LastLoginUserSP lastLogin = null;
    private final String TAG = getClass().getSimpleName();
    private Context mContext;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
//        V8ContextAddress.bind(this,URLs.class);
        lastLogin = LastLoginUserSP.getInstance(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        syncConfig();
        return super.onStartCommand(intent, flags, startId);
    }

    private void syncConfig() {
        //服务器地址不能为空
        final String address = lastLogin.getUserServicesAddress();

        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected String doInBackground(Void... params) {
                return V8TokenManager.forceRefresh();
            }

            @Override
            protected void onPostExecute(String token) {
                //T获取服务器登录配置
                getServiceLoginConfig(token);
            }
        };

        if (!StringUtils.isEmpty(address)) {
            AsyncTaskCompat.executeParallel(tokenTask);
        } else {
            Log.e(TAG, "服务器地址为空");
        }

    }

    /**
     * 获取服务器登录配置
     */
    private void getServiceLoginConfig(String token) {
        String version = "V1.0";
        Map<String, String> params = new HashMap<>();
        params.put("version", version);
        params.put("access_token", token);
        OKHttpCustomUtils.get(URLs.GET_SERVICE_LOGIN_CONFIG, this, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                try {
                    JSONObject resultObj = response.getJSONObject("result");
                    LoginConfig loginConfig = JSON.parseObject(resultObj.getString("loginRule"), LoginConfig.class);
                    int lastGetVerNum = 0;
                    try {
                        lastGetVerNum = lastLogin.getServiceLoginConfig().getLastGetVerNum();
                    } catch (NullPointerException e) {
                        lastGetVerNum = 0;
                    }
                    loginConfig.setLastGetVerNum(lastGetVerNum);
                    lastLogin.saveServiceLoginConfig(loginConfig);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {

                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
//                ToastUtil.toastAlerMessage(mContext, getResources().getString(R.string.request_failed) + " " + getResources().getString(R.string.request_error_config), 1000);
            Log.e(TAG,getResources().getString(R.string.request_failed) + " " + getResources().getString(R.string.request_error_config));
            }
        });
    }
}
