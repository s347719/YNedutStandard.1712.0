package com.yineng.ynmessager.view.agentwebx5;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.widget.EditText;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.view.agentwebx5.interfae.FileUploadPop;
import com.yineng.ynmessager.view.agentwebx5.interfae.IFileUploadChooser;
import com.yineng.ynmessager.view.agentwebx5.interfae.IVideo;
import com.yineng.ynmessager.view.agentwebx5.interfae.IndicatorController;

import java.lang.ref.WeakReference;

/**
 * <b>@项目名：</b> agentwebX5<br>
 * <b>@包名：</b><br>
 * <b>@创建者：</b> cxz --  just<br>
 * <b>@创建时间：</b> &{DATE}<br>
 * <b>@公司：</b> <br>
 * <b>@邮箱：</b> cenxiaozhong.qqcom@qq.com<br>
 * <b>@描述</b><br>
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public class DefaultChromeClient extends WebChromeClientProgressWrapper implements FileUploadPop<IFileUploadChooser> {


    //    private Activity mActivity;
    private WeakReference<Activity> mActivityWeakReference = null;
    private AlertDialog promptDialog = null;
    private AlertDialog confirmDialog = null;
    private JsPromptResult pJsResult = null;
    private JsResult cJsResult = null;
    private ChromeClientCallbackManager mChromeClientCallbackManager;

    public static final String ChromePath = "com.tencent.smtt.sdk.WebChromeClient";
    private WebChromeClient mWebChromeClient;
    private boolean isWrapper = false;

    private IFileUploadChooser mIFileUploadChooser;

    private IVideo mIVideo;


    public DefaultChromeClient(Activity activity, IndicatorController indicatorController, WebChromeClient chromeClient, ChromeClientCallbackManager chromeClientCallbackManager, @Nullable IVideo iVideo) {
        super(indicatorController, chromeClient);
        isWrapper = chromeClient != null ? true : false;
        this.mWebChromeClient = chromeClient;
//        this.mActivity = activity;
        mActivityWeakReference = new WeakReference<Activity>(activity);
        this.mChromeClientCallbackManager = chromeClientCallbackManager;

        this.mIVideo  = iVideo;
    }


    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);

        ChromeClientCallbackManager.AgentWebCompatInterface mAgentWebCompatInterface = null;
        if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mChromeClientCallbackManager != null && (mAgentWebCompatInterface = mChromeClientCallbackManager.getAgentWebCompatInterface()) != null) {
            mAgentWebCompatInterface.onProgressChanged(view, newProgress);
        }

    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        ChromeClientCallbackManager.ReceivedTitleCallback mCallback = null;
        if (mChromeClientCallbackManager != null && (mCallback = mChromeClientCallbackManager.getReceivedTitleCallback()) != null) {
            mCallback.onReceivedTitle(view, title);
        }

        if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mChromeClientCallbackManager != null && (mChromeClientCallbackManager.getAgentWebCompatInterface()) != null) {
            mChromeClientCallbackManager.getAgentWebCompatInterface().onReceivedTitle(view, title);
        }
        if (isWrapper) {
            super.onReceivedTitle(view, title);
        }
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {


        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onJsAlert", "public boolean " + ChromePath + ".onJsAlert", WebView.class, String.class, String.class, JsResult.class)) {

            return super.onJsAlert(view, url, message, result);
        }

        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null) {
            return true;
        }
        //
        AgentWebUtils.show(view,
                message,
                Snackbar.LENGTH_SHORT,
                Color.WHITE,
                mActivity.getResources().getColor(R.color.black),
                null,
                -1,
                null);
        result.confirm();

        return true;
    }


    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
    }

    @Override
    public void onGeolocationPermissionsHidePrompt() {
        super.onGeolocationPermissionsHidePrompt();
    }

    //location
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback callback) {

        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onGeolocationPermissionsShowPrompt", "public void " + ChromePath + ".onGeolocationPermissionsShowPrompt", String.class, GeolocationPermissions.Callback.class)) {
            super.onGeolocationPermissionsShowPrompt(origin, callback);
            return;
        }
        callback.invoke(origin, true, false);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {


        try {
            if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onJsPrompt", "public boolean " + ChromePath + ".onJsPrompt", WebView.class, String.class, String.class, String.class, JsPromptResult.class)) {

                return super.onJsPrompt(view, url, message, defaultValue, result);
            }
            if (AgentWebConfig.WEBVIEW_TYPE == AgentWebConfig.WEBVIEW_AGENTWEB_SAFE_TYPE && mChromeClientCallbackManager != null && mChromeClientCallbackManager.getAgentWebCompatInterface() != null) {

                L.i("Info", "mChromeClientCallbackManager.getAgentWebCompatInterface():" + mChromeClientCallbackManager.getAgentWebCompatInterface());
                if (mChromeClientCallbackManager.getAgentWebCompatInterface().onJsPrompt(view, url, message, defaultValue, result)) {
                    return true;
                }
            }
            showJsPrompt(message, result, defaultValue);
        } catch (Exception e) {
//            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onJsConfirm", "public boolean " + ChromePath + ".onJsConfirm", WebView.class, String.class, String.class, JsResult.class)) {

            return super.onJsConfirm(view, url, message, result);
        }
        showJsConfirm(message, result);
        return true;
    }


    private void toDismissDialog(Dialog dialog) {
        if (dialog != null) {
            dialog.dismiss();
        }

    }


    private void showJsConfirm(String message, final JsResult result) {

        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity != null) {
            return;
        }

        if (confirmDialog == null) {
            confirmDialog = new AlertDialog.Builder(mActivity)//
                    .setMessage(message)//
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toDismissDialog(confirmDialog);
                            toCancelJsresult(cJsResult);
                        }
                    })//
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toDismissDialog(confirmDialog);
                            if (DefaultChromeClient.this.cJsResult != null) {
                                DefaultChromeClient.this.cJsResult.confirm();
                            }

                        }
                    }).create();
        }
        this.cJsResult = result;
        confirmDialog.show();

    }

    private void toCancelJsresult(JsResult result) {
        if (result != null) {
            result.cancel();
        }
    }

    private void showJsPrompt(String message, final JsPromptResult js, String defaultstr) {

        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null) {
            return;
        }
        if (promptDialog == null) {

            final EditText et = new EditText(mActivity);
            et.setText(defaultstr);
            promptDialog = new AlertDialog.Builder(mActivity)//
                    .setView(et)//
                    .setTitle(message)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toDismissDialog(promptDialog);
                            toCancelJsresult(pJsResult);
                        }
                    })//
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toDismissDialog(promptDialog);

                            if (DefaultChromeClient.this.pJsResult != null) {
                                DefaultChromeClient.this.pJsResult.confirm(et.getText().toString());
                            }

                        }
                    }).create();
        }
        this.pJsResult = js;
        promptDialog.show();


    }

    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, WebStorage.QuotaUpdater quotaUpdater) {


        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onExceededDatabaseQuota", ChromePath + ".onExceededDatabaseQuota", String.class, String.class, long.class, long.class, long.class, WebStorage.QuotaUpdater.class)) {

            super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
            return;
        }
        quotaUpdater.updateQuota(totalQuota * 2);
    }

    @Override
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {


        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onReachedMaxAppCacheSize", ChromePath + ".onReachedMaxAppCacheSize", long.class, long.class, WebStorage.QuotaUpdater.class)) {

            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            return;
        }
        quotaUpdater.updateQuota(requiredStorage * 2);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        L.i("Info", "openFileChooser>=5.0");
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onShowFileChooser", ChromePath + ".onShowFileChooser", WebView.class, ValueCallback.class, WebChromeClient.FileChooserParams.class)) {

            return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
        openFileChooserAboveL(webView, filePathCallback, fileChooserParams);
        return true;
    }

    private void openFileChooserAboveL(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {


        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null) {
            return;
        }
        IFileUploadChooser mIFileUploadChooser = this.mIFileUploadChooser;
        this.mIFileUploadChooser = mIFileUploadChooser = new FileUpLoadChooserImpl(webView, mActivity, filePathCallback, fileChooserParams);
        mIFileUploadChooser.openFileChooser();

        L.i("Info","mIFileUploadChooser:"+this.mIFileUploadChooser);
    }

    // Android  >= 4.1
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        /*believe me , i never want to do this */
        L.i("Info", "openFileChooser>=4.1");
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "openFileChooser", ChromePath + ".openFileChooser", ValueCallback.class, String.class, String.class)) {
            super.openFileChooser(uploadFile, acceptType, capture);
            return;
        }
        createAndOpenCommonFileLoader(uploadFile);
    }

    //  Android < 3.0
    @Override
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "openFileChooser", ChromePath + ".openFileChooser", ValueCallback.class)) {
            super.openFileChooser(valueCallback);
            return;
        }
        Log.i("Info", "openFileChooser<3.0");
        createAndOpenCommonFileLoader(valueCallback);
    }

    //  Android  >= 3.0
    @Override
    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
        Log.i("Info", "openFileChooser>3.0");

        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "openFileChooser", ChromePath + ".openFileChooser", ValueCallback.class, String.class)) {
            super.openFileChooser(valueCallback, acceptType);
            return;
        }
        createAndOpenCommonFileLoader(valueCallback);
    }


    private void createAndOpenCommonFileLoader(ValueCallback valueCallback) {
        Activity mActivity = this.mActivityWeakReference.get();
        if (mActivity == null) {
            return;
        }
        ;
        this.mIFileUploadChooser = new FileUpLoadChooserImpl(mActivity, valueCallback);
        this.mIFileUploadChooser.openFileChooser();

    }

    @Override
    public IFileUploadChooser pop() {
        L.i("Info", "mIFileUploadChooser offer:" + mIFileUploadChooser);
        IFileUploadChooser mIFileUploadChooser = this.mIFileUploadChooser;
        this.mIFileUploadChooser = null;
        return mIFileUploadChooser;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        super.onConsoleMessage(consoleMessage);
        L.i("Info", "consoleMessage:" + consoleMessage.message() + "  lineNumber:" + consoleMessage.lineNumber());
        return true;
    }



    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        Log.i("Info", "view:" + view + "   callback:" + callback);
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onShowCustomView", ChromePath + ".onShowCustomView", View.class, IX5WebChromeClient.CustomViewCallback.class)) {
            super.onShowCustomView(view, callback);
            return;
        }


        if(mIVideo!=null) {
            mIVideo.onShowCustomView(view, callback);
        }


    }

    @Override
    public void onHideCustomView() {
        if (AgentWebUtils.isOverriedMethod(mWebChromeClient, "onHideCustomView", ChromePath + ".onHideCustomView")) {
            L.i("Info","onHide:"+true);
            super.onHideCustomView();
            return;
        }

        L.i("Info","Videa:"+mIVideo);
        if(mIVideo!=null) {
            mIVideo.onHideCustomView();
        }

    }


}
