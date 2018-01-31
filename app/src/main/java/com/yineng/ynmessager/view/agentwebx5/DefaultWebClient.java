package com.yineng.ynmessager.view.agentwebx5;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.alipay.sdk.app.H5PayCallback;
import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.util.H5PayResultModel;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.yineng.ynmessager.util.L;

import java.lang.ref.WeakReference;

/**
 * <b>@项目名：</b> agentweb<br>
 * <b>@包名：</b><br>
 * <b>@创建者：</b> cxz --  just<br>
 * <b>@创建时间：</b> &{DATE}<br>
 * <b>@公司：</b> <br>
 * <b>@邮箱：</b> cenxiaozhong.qqcom@qq.com<br>
 * <b>@描述</b><br>
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public class DefaultWebClient extends WrapperWebViewClient {

    private WebViewClientCallbackManager mWebViewClientCallbackManager;
    private WeakReference<Activity> mWeakReference = null;
    private static final int CONSTANTS_ABNORMAL_BIG = 7;
    private WebViewClient mWebViewClient;
    private boolean webClientHelper = false;
    private static final String WEBVIEWCLIENTPATH = "com.tencent.smtt.sdk.WebViewClient";
    public static final String INTENT_SCHEME = "intent://";
    public static final String WEBCHAT_PAY_SCHEME = "weixin://wap/pay?";
    //TODO 先放着，说不定以后有用
    private AgentWeb mAgentWeb;

    private static final boolean hasAlipayLib;

    static {
        boolean tag = true;
        try {
            Class.forName("com.alipay.sdk.app.PayTask");
        } catch (Throwable ignore) {
            tag = false;
        }
        hasAlipayLib = tag;

        L.i("Info", "static  hasAlipayLib:" + hasAlipayLib);
    }

    DefaultWebClient(AgentWeb agentWeb, @NonNull Activity activity, WebViewClient client, WebViewClientCallbackManager manager, boolean webClientHelper) {
        super(client);
        this.mAgentWeb = agentWeb;
        this.mWebViewClient = client;
        mWeakReference = new WeakReference<Activity>(activity);
        this.mWebViewClientCallbackManager = manager;
        this.webClientHelper = webClientHelper;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        L.i("Info", "shouldOverrideUrlLoading --->  url:" + url);
        if (webClientHelper && handleNormalLinked(url)) {
            return true;
        }
        int tag = -1;

        if (AgentWebUtils.isOverriedMethod(mWebViewClient, "shouldOverrideUrlLoading", WEBVIEWCLIENTPATH + ".shouldOverrideUrlLoading", WebView.class, String.class)
                && (((tag = 1) > 0) && super.shouldOverrideUrlLoading(view, url))) {
            return true;
        }

        if (webClientHelper && url.startsWith(INTENT_SCHEME)) { //拦截
            handleIntentUrl(url);
            return true;
        }

        if (webClientHelper && url.startsWith(WEBCHAT_PAY_SCHEME)) {
            startActivity(url);
            return true;
        }

        L.i("Info", "shouldOverrideUrlLoading --->  url:" + url);
        if (webClientHelper && handleNormalLinked(url)) {
            return true;
        }


        if (webClientHelper && hasAlipayLib && isAlipay(view, url)) {
            return true;
        }

        if (tag > 0) {
            return false;
        }

        return super.shouldOverrideUrlLoading(view, url);
    }

    /**
     * 判断是否是本地app支付
     */
    private boolean isAlipay(final WebView view, final String url) {
        Activity mActivity = null;
        if ((mActivity = mWeakReference.get()) == null) {
            return false;
        }
        /**
         * 推荐采用的新的二合一接口(payInterceptorWithUrl),只需调用一次
         */
        final PayTask task = new PayTask(mActivity);
        boolean isIntercepted = task.payInterceptorWithUrl(url, true, new H5PayCallback() {
            @Override
            public void onPayResult(final H5PayResultModel result) {
                final String url = result.getReturnUrl();
                if (!TextUtils.isEmpty(url)) {
                    AgentWebUtils.runInUiThread(new Runnable() {
                        @Override
                        public void run() {
                            view.loadUrl(url);
                        }
                    });
                }
            }
        });

        /**
         * 判断是否成功拦截
         * 若成功拦截，则无需继续加载该URL；否则继续加载
         */
        if (!isIntercepted)
            return false;
        return true;
    }

    private void handleIntentUrl(String intentUrl) {
        try {

            Intent intent = null;
            if (TextUtils.isEmpty(intentUrl) || !intentUrl.startsWith(INTENT_SCHEME)) {
                return;
            }

            Activity mActivity = null;
            if ((mActivity = mWeakReference.get()) == null) {
                return;
            }
            PackageManager packageManager = mActivity.getPackageManager();
            intent = new Intent().parseUri(intentUrl, Intent.URI_INTENT_SCHEME);
            ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            L.i("Info", "resolveInfo:" + info + "   package:" + intent.getPackage());
            if (info != null) {  //跳到该应用
                mActivity.startActivity(intent);
                return;
            }
            /*intent=new Intent().setData(Uri.parse("market://details?id=" + intent.getPackage()));
            info=packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            LogUtils.i("Info","resolveInfo:"+info);
            if (info != null) {  //跳到应用市场
                mActivity.startActivity(intent);
                return;
            }

            intent=new Intent().setData(Uri.parse("https://play.google.com/store/apps/details?id=" + intent.getPackage()));
            info=packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            LogUtils.i("Info","resolveInfo:"+info);
            if (info != null) {  //跳到浏览器
                mActivity.startActivity(intent);
                return;
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private boolean handleNormalLinked(String url) {
        if (url.startsWith(WebView.SCHEME_TEL) || url.startsWith("sms:") || url.startsWith(WebView.SCHEME_MAILTO)) {
            try {
                Activity mActivity = null;
                if ((mActivity = mWeakReference.get()) == null) {
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException ignored) {
            }
            return true;
        }
        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        L.i("Info", "onPageStarted");
        if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mWebViewClientCallbackManager.getPageLifeCycleCallback() != null) {
            mWebViewClientCallbackManager.getPageLifeCycleCallback().onPageStarted(view, url, favicon);
        }

        super.onPageStarted(view, url, favicon);
    }


    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        L.i("Info", "onReceivedError：" + description + "  CODE:" + errorCode);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        L.i("Info", "onReceivedError:" + error.toString());

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mWebViewClientCallbackManager.getPageLifeCycleCallback() != null) {
            mWebViewClientCallbackManager.getPageLifeCycleCallback().onPageFinished(view, url);
        }
        super.onPageFinished(view, url);

        L.i("Info", "onPageFinished");
    }


    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        L.i("Info", "shouldOverrideKeyEvent");
        return super.shouldOverrideKeyEvent(view, event);
    }


    private void startActivity(String url) {
        try {
            if (mWeakReference.get() == null) {
                return;
            }
            L.i("Info", "start wechat pay Activity");
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mWeakReference.get().startActivity(intent);
        } catch (Exception e) {
            L.i("Info", "支付异常");
            e.printStackTrace();
        }
    }

    @Override
    public void onScaleChanged(WebView view, float oldScale, float newScale) {


        if (AgentWebUtils.isOverriedMethod(mWebViewClient, "onScaleChanged", WEBVIEWCLIENTPATH + ".onScaleChanged", WebView.class, float.class, float.class)) {
            super.onScaleChanged(view, oldScale, newScale);
            return;
        }

        L.i("Info", "onScaleChanged:" + oldScale + "   n:" + newScale);
        if (newScale - oldScale > CONSTANTS_ABNORMAL_BIG) {
            view.setInitialScale((int) (oldScale / newScale * 100));
        }

    }
}
