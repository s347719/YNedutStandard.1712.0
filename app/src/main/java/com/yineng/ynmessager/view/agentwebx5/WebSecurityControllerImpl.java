package com.yineng.ynmessager.view.agentwebx5;

import android.os.Build;
import android.support.v4.util.ArrayMap;

import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.view.agentwebx5.interfae.WebSecurityCheckLogic;
import com.yineng.ynmessager.view.agentwebx5.interfae.WebSecurityController;


/**
 * <b>@项目名：</b> <br>
 * <b>@包名：</b><br>
 * <b>@创建者：</b> cxz --  just<br>
 * <b>@创建时间：</b> &{DATE}<br>
 * <b>@公司：</b> <br>
 * <b>@邮箱：</b> cenxiaozhong.qqcom@qq.com<br>
 * <b>@描述</b>source code https://github.com/Justson/AgentWebX5<br>
 */

public class WebSecurityControllerImpl implements WebSecurityController<WebSecurityCheckLogic> {

    private WebView mWebView;
    private ArrayMap<String, Object> mMap;
    private AgentWeb.SecurityType mSecurityType;

    public WebSecurityControllerImpl(WebView view, ArrayMap<String, Object> map, AgentWeb.SecurityType securityType) {
        this.mWebView = view;
        this.mMap = map;
        this.mSecurityType=securityType;
    }

    @Override
    public void check(WebSecurityCheckLogic webSecurityCheckLogic) {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            webSecurityCheckLogic.dealHoneyComb(mWebView);
        }

        if (mMap != null &&mSecurityType== AgentWeb.SecurityType.strict&& !mMap.isEmpty()) {
            webSecurityCheckLogic.dealJsInterface(mMap,mSecurityType);
        }

    }
}
