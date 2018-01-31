package com.yineng.ynmessager.view.agentwebx5;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.view.agentwebx5.interfae.EventInterceptor;
import com.yineng.ynmessager.view.agentwebx5.interfae.IVideo;


/**
 * Created by cenxiaozhong on 2017/6/10.
 * source code https://github.com/Justson/AgentWebX5
 */

public class VideoImpl implements IVideo, EventInterceptor {


    private Activity mActivity;
    private WebView mWebView;

    public VideoImpl(Activity mActivity, WebView webView) {
        this.mActivity = mActivity;
        this.mWebView = webView;

    }

    private View moiveView = null;
    private ViewGroup moiveParentView = null;
    private IX5WebChromeClient.CustomViewCallback mCallback;
    @Override
    public void onShowCustomView(View view, IX5WebChromeClient.CustomViewCallback callback) {

        L.i("Info", "onShowCustomView:" + view);

        Activity mActivity;
        if ((mActivity = this.mActivity) == null) {
            return;
        }
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (moiveView != null){
            callback.onCustomViewHidden();
            return;
        }

        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }

        if (moiveParentView == null) {
            FrameLayout mDecorView = (FrameLayout) mActivity.getWindow().getDecorView();
            moiveParentView = new FrameLayout(mActivity);
            moiveParentView.setBackgroundColor(Color.BLACK);
            mDecorView.addView(moiveParentView);
        }
        this.mCallback=callback;
        moiveParentView.addView(this.moiveView = view);


        moiveParentView.setVisibility(View.VISIBLE);






    }

    @Override
    public void onHideCustomView() {

        L.i("Info", "onHideCustomView:" + moiveView);
        if (moiveView == null) {
            return;
        }
        if (mActivity!=null&&mActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        moiveView.setVisibility(View.GONE);
        if (moiveParentView != null && moiveView != null) {
            moiveParentView.removeView(moiveView);

        }
        if (moiveParentView != null) {
            moiveParentView.setVisibility(View.GONE);
        }

        if(this.mCallback!=null) {
            mCallback.onCustomViewHidden();
        }
        this.moiveView = null;
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }



    }

    @Override
    public boolean isVideoState() {
        return moiveView != null;
    }

    @Override
    public boolean event() {

        L.i("Info", "event:" + isVideoState());
        if (isVideoState()) {
            onHideCustomView();
            return true;
        } else {
            return false;
        }

    }
}