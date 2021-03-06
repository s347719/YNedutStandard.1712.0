package com.yineng.ynmessager.view.agentwebx5;

import android.os.Build;

import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.view.agentwebx5.interfae.WebLifeCycle;


/**
 * Created by cenxiaozhong on 2017/6/3.
 *
 * source CODE  https://github.com/Justson/AgentWebX5
 */

public class DefaultWebLifeCycleImpl implements WebLifeCycle {
    private WebView mWebView;

    DefaultWebLifeCycleImpl(WebView webView) {
        this.mWebView = webView;
    }

    @Override
    public void onResume() {
        if (this.mWebView != null) {

            if (Build.VERSION.SDK_INT >= 11) {
                this.mWebView.onResume();
            }

            this.mWebView.resumeTimers();

        }


    }

    @Override
    public void onPause() {

        if (this.mWebView != null) {
            this.mWebView.pauseTimers();
            if (Build.VERSION.SDK_INT >= 11) {
                this.mWebView.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {

        AgentWebUtils.clearWebView(this.mWebView);
    }
}
