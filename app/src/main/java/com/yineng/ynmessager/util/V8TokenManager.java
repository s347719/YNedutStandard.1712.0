//***************************************************************
//*    2015-10-12  下午5:12:36
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.util;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.yninterface.TokenLoadedListener;

import org.apache.http.Header;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * 获取V8 Token的工具类
 *
 * @author 贺毅柳
 *
 */
public class V8TokenManager
{
	public static final String TAG = "V8TokenUtil";
	public static String sToken = "";
    private static long sExpireTime = 0;
	private TokenLoadedListener tokenLoadedListener;
    private static SyncHttpClient sHttpClient = new SyncHttpClient();
	/**
	 * 获取v8 token。如果还未过期，会直接返回上次请求成功的token<br>
	 * <b>Note：</b>该方法会请求网络，需要在子线程中执行
     *
     * @return token 返回实际获取的token字符串，获取失败返回空字符串
     */
    @NonNull
    public static String obtain() {
        if (System.currentTimeMillis() >= sExpireTime || TextUtils.isEmpty(sToken)) {
            forceRefresh();
        } else {
            L.v(TAG, "return token straightly (without a new http request)");
        }
        return sToken;
    }

    /**
     * 直接从服务器获取最新的v8 token<br>
     * <b>Note：</b>该方法会请求网络，需要在子线程中执行
     *
     * @return token 返回实际获取的token字符串，获取失败返回空字符串
     */
    @NonNull
    public static String forceRefresh() {
        String url = URLs.GET_TOKEN;
        //				String url = "http://60196.ynedut.com.cn/ynedut/oauth/token.htm?client_id=yn-message&client_secret=yn-message-yn88888888yn&grant_type=password";
        L.d("token url : " + url);

		if(TextUtils.isEmpty(url))
		{
			L.w(TAG, "the url for V8 access token is empty!!");
			return "";
		}
        sHttpClient.setMaxRetriesAndTimeout(1, 4000);
        sHttpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                long current = System.currentTimeMillis();

                sToken = response.optString("access_token");
                //更新application里的token
                AppController.getInstance().mAppTokenStr = sToken;
                sExpireTime = current + (response.optInt("expires_in") * 1000);
                L.i(TAG, "new token : " + sToken);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                sToken = "";
                sExpireTime = 0;
                L.e(TAG, "31003");
            }

        });
        return sToken;
    }

    public void setTokenLoadedListener(TokenLoadedListener tokenLoadedListener) {
        this.tokenLoadedListener = tokenLoadedListener;
    }

    /**
     * 是否需要更新token
     */
    public boolean isMustUpdateToken() {
        return System.currentTimeMillis() >= sExpireTime || TextUtils.isEmpty(sToken);
    }

    /**
     * 初始化获取、解析相关数据
     *
     * @param forceRefreshToken 是否强制重新请求刷新Token来访问应用菜单接口
     */
    @SuppressLint("StaticFieldLeak")
    public void initAppTokenData(boolean forceRefreshToken) {
        new AsyncTask<Boolean, Integer, String>() {

            @Override
            protected String doInBackground(Boolean... params) {
                String token;
                if (params[0]) {
                    token = V8TokenManager.forceRefresh();
                } else {
                    token = V8TokenManager.obtain();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String result) {
                AppController.getInstance().mAppTokenStr = result;
                L.i(TAG, "获取token成功");
                if (tokenLoadedListener != null) {
                    tokenLoadedListener.tokenLoaded();
                }
            }
        }.execute(forceRefreshToken);
    }
}
