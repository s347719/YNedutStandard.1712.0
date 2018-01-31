package com.yineng.ynmessager.view.agentwebx5;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.view.agentwebx5.interfae.IndicatorController;

import java.lang.reflect.Method;

/**
 * <b>@项目名：</b> agentwebX5<br>
 * <b>@包名：</b><br>
 * <b>@创建者：</b> cxz --  just<br>
 * <b>@创建时间：</b> &{DATE}<br>
 * <b>@公司：</b> <br>
 * <b>@邮箱：</b> cenxiaozhong.qqcom@qq.com<br>
 * <b>@描述:source CODE  https://github.com/Justson/AgentWebX5</b><br>
 */

public class WebChromeClientProgressWrapper extends ChromeClientProgress {
    protected WebChromeClient mRealWebChromeClient;

    public WebChromeClientProgressWrapper(IndicatorController indicatorController, WebChromeClient realWebChromeClient) {
        this(indicatorController);
        this.mRealWebChromeClient = realWebChromeClient;
    }

    public WebChromeClientProgressWrapper(IndicatorController indicatorController) {
        super(indicatorController);

    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view,newProgress);
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onProgressChanged(view, newProgress);
            return;
        }

    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onReceivedTitle(view, title);
            return;
        }
        super.onReceivedTitle(view,title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onReceivedIcon(view, icon);
            return ;
        }
        super.onReceivedIcon(view,icon);

    }

    @Override
    public void onReceivedTouchIconUrl(WebView view, String url,
                                       boolean precomposed) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
            return ;
        }
        super.onReceivedTouchIconUrl(view,url,precomposed);
    }


    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onShowCustomView(view, callback);
            return ;
        }
        super.onShowCustomView(view,callback);
    }



    @Override
    public void onShowCustomView(View view, int requestedOrientation,
                                 IX5WebChromeClient.CustomViewCallback callback) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onShowCustomView(view, requestedOrientation, callback);
            return;
        }
        super.onShowCustomView(view,requestedOrientation,callback);
    }



    @Override
    public void onHideCustomView() {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onHideCustomView();
            return;
        }
        super.onHideCustomView();
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog,
                                  boolean isUserGesture, Message resultMsg) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
        }
        return super.onCreateWindow(view,isDialog,isUserGesture,resultMsg);
    }

    @Override
    public void onRequestFocus(WebView view) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onRequestFocus(view);
            return;
        }
        super.onRequestFocus(view);
    }

    @Override
    public void onCloseWindow(WebView window) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onCloseWindow(window);
            return;
        }
        super.onCloseWindow(window);
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message,
                             JsResult result) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onJsAlert(view, url, message, result);
        }
        return super.onJsAlert(view,url,message,result);
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
                               JsResult result) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onJsConfirm(view, url, message, result);
        }
        return super.onJsConfirm(view,url,message,result);
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, String message,
                              String defaultValue, JsPromptResult result) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onJsPrompt(view, url, message, defaultValue, result);
        }
        return super.onJsPrompt(view,url,message,defaultValue,result);
    }

    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message,
                                    JsResult result) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onJsBeforeUnload(view, url, message, result);
        }
        return super.onJsBeforeUnload(view,url,message,result);
    }

    @Override
    @Deprecated
    public void onExceededDatabaseQuota(String url, String databaseIdentifier,
                                        long quota, long estimatedDatabaseSize, long totalQuota,
                                        WebStorage.QuotaUpdater quotaUpdater) {
        // This default implementation passes the current quota back to WebCore.
        // WebCore will interpret this that new quota was declined.
        //注掉
//        quotaUpdater.updateQuota(quota);
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
            return;
        }
        super.onExceededDatabaseQuota(url,databaseIdentifier,quota,estimatedDatabaseSize,totalQuota,quotaUpdater);

    }

    @Override
    @Deprecated
    public void onReachedMaxAppCacheSize(long requiredStorage, long quota,
                                         WebStorage.QuotaUpdater quotaUpdater) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            return;
        }
        super.onReachedMaxAppCacheSize(requiredStorage,quota,quotaUpdater);
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
                                                   GeolocationPermissionsCallback callback) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onGeolocationPermissionsShowPrompt(origin,  callback);
            return;
        }
        super.onGeolocationPermissionsShowPrompt(origin,callback);

    }

    /**
     * Notify the host application that a request for Geolocation permissions,
     * made with a previous call to
     * {@link #onGeolocationPermissionsShowPrompt(String, GeolocationPermissions.Callback) onGeolocationPermissionsShowPrompt()}
     * has been canceled. Any related UI should therefore be hidden.
     */
    @Override
    public void onGeolocationPermissionsHidePrompt() {

        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.onGeolocationPermissionsHidePrompt();
            return ;
        }

        super.onGeolocationPermissionsHidePrompt();
    }




    @Override
    public boolean onJsTimeout() {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onJsTimeout();
        }
        return super.onJsTimeout();
    }




    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        /*onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(),
                consoleMessage.sourceId());*/

        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onConsoleMessage(consoleMessage);
        }
        return super.onConsoleMessage(consoleMessage);
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.getDefaultVideoPoster();
        }
        return super.getDefaultVideoPoster();
    }

    @Override
    public View getVideoLoadingProgressView() {
        L.i("Info","getVideoLoadingProgressView  call back");
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.getVideoLoadingProgressView();
        }
        return super.getVideoLoadingProgressView();
    }

    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        if (this.mRealWebChromeClient != null){
            this.mRealWebChromeClient.getVisitedHistory(callback);
            return ;
        }
        super.getVisitedHistory(callback);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                     FileChooserParams fileChooserParams) {
        if (this.mRealWebChromeClient != null) {
            return this.mRealWebChromeClient.onShowFileChooser(webView, filePathCallback, fileChooserParams);
        }
        return super.onShowFileChooser(webView,filePathCallback,fileChooserParams);
    }


    // Android  >= 4.1
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
        /*believe me , i never want to do this */
        commonRefect(this.mRealWebChromeClient, "openFileChooser", new Object[]{uploadFile, acceptType, capture}, ValueCallback.class, String.class, String.class);
    }

    //  Android < 3.0
    public void openFileChooser(ValueCallback<Uri> valueCallback) {
        Log.i("Infoss", "openFileChooser");
        commonRefect(this.mRealWebChromeClient, "openFileChooser", new Object[]{valueCallback}, ValueCallback.class);
    }

    //  Android  >= 3.0
    public void openFileChooser(ValueCallback valueCallback, String acceptType) {
        Log.i("Infoss", "openFileChooser.1");
        commonRefect(this.mRealWebChromeClient, "openFileChooser", new Object[]{valueCallback, acceptType}, ValueCallback.class, String.class);
    }


    private void commonRefect(WebChromeClient o, String mothed, Object[] os, Class... clazzs) {
        try {
            if (o == null) {
                return;
            }
            Class<?> clazz = o.getClass();
            Method mMethod = clazz.getMethod(mothed, clazzs);
            mMethod.invoke(o, os);
        } catch (Exception igore) {
//            igore.printStackTrace();
        }

    }


    /*public void setupAutoFill(Message msg) {
        if (this.mRealWebChromeClient != null)
            return this.mRealWebChromeClient.setupAutoFill();
    }*/

}
