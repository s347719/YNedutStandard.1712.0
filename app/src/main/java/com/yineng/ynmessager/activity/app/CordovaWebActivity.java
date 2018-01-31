package com.yineng.ynmessager.activity.app;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.bean.app.Submenu;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CordovaWebActivity extends CordovaActivity implements View.OnClickListener {

    protected final String mTag = this.getClass().getSimpleName();

    private AppController mApplication;
    private View cordove_progress;


    private final int showProgress = 1;
    private final int hideProgress = 2;
    private List<Submenu> mAppMenus = new ArrayList<>(); //菜单

    //传递过来的参数
    private String url ;
    private String title;
    private String token ;
    private boolean initMenu;
    private String id;

    //头布局变量
    private LinearLayout mAppTitleBackLL;
    private ImageView mAppTitleBackIV;
    private TextView mAppTitleNameTV;
    private LinearLayout mAppTitleRightLL;
    private ImageView mAppTitleRightIV;

    private boolean mIsShowDropMenu;
    private PopupWindow mSubMenuWindow;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case showProgress:
                    cordove_progress.setVisibility(View.VISIBLE);
                    break;
                case hideProgress:
                    cordove_progress.setVisibility(View.INVISIBLE);
                    break;
            }

        }
    };

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
//            super.init();
            setContentView(R.layout.fragment_main_cordova_web);

            mApplication = AppController.getInstance();
            token = V8TokenManager.obtain();
            title = getIntent().getStringExtra("title");
            initMenu = getIntent().getBooleanExtra("initMenu",false);
            id = getIntent().getStringExtra("id");
            url = getIntent().getStringExtra("url");

            cordove_progress = findViewById(R.id.cordove_progress);
            findViews(findViewById(R.id.main_layout_title));

            mAppTitleNameTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSubMenuWindow == null) {
                        return;
                    }
                    ((TextView)v).setCompoundDrawablesWithIntrinsicBounds(null, null,ResourcesCompat.getDrawable(getResources(),R.mipmap.contact_fast_jump_arrow_up,CordovaWebActivity.this.getTheme()), null);
                    PopupWindowCompat.showAsDropDown(mSubMenuWindow, v, 0, 0, Gravity.NO_GRAVITY);
                }
            });

            loadUrl(id,title,initMenu);

        }

    @Override
    protected CordovaWebView makeWebView() {
        SystemWebView webView = (SystemWebView) findViewById(R.id.cordovaWebView);
        CordovaWebView cordovaWebView = new CordovaWebViewImpl(new SystemWebViewEngine(webView));
        return cordovaWebView;
    }

    @Override
    protected void createViews() {
        if (preferences.contains("BackgroundColor")) {
            try {
                int backgroundColor = preferences.getInteger("BackgroundColor", Color.BLACK);
                // Background of activity:
                appView.getView().setBackgroundColor(backgroundColor);
            }
            catch (NumberFormatException e){
                e.printStackTrace();
            }
        }
        appView.getView().requestFocusFromTouch();

    }

    @Override
    protected CordovaInterfaceImpl makeCordovaInterface() {
        return new CordovaInterfaceImpl(this){
            @Override
            public Object onMessage(String id, Object data) {
                if (id.equals("onPageStarted")){
                    handler.sendEmptyMessage(showProgress);
                }else if (id.equals("onReceivedError")||id.equals("onPageFinished")){
                    // 此处为什么停止2秒是因为CordovaWebViewImpl中onPageFinishedLoading停止了2秒造成了视图错觉
                    handler.sendEmptyMessageDelayed(hideProgress,1500);
                }
                return null;
            }
        };
    }

    //找出头布局各变量
    private void findViews(View topview) {

        mAppTitleBackLL = (LinearLayout) topview.findViewById(R.id.app_common_titleview_left);
        mAppTitleBackIV = (ImageView) topview.findViewById(R.id.app_common_title_view_back);
        mAppTitleNameTV = (TextView) topview.findViewById(R.id.app_common_title_view_name);
        mAppTitleRightLL = (LinearLayout) topview.findViewById(R.id.app_common_titleview_right);
        mAppTitleRightLL.setVisibility(View.VISIBLE);
        mAppTitleRightIV = (ImageView) topview.findViewById(R.id.app_common_title_view_infomation);
        mAppTitleRightIV.setImageResource(R.mipmap.refresh);
        mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                mIsShowDropMenu ? ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.contact_fast_jump_arrow_down, CordovaWebActivity.this.getTheme()) : null, null);
        mAppTitleBackIV.setOnClickListener(this);
        mAppTitleNameTV.setOnClickListener(this);
        mAppTitleRightIV.setOnClickListener(this);

    }
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.app_common_title_view_back:
                appView.stopLoading();
                finish();
                break;
            case R.id.app_common_title_view_infomation:
                String js = "javascript:window.location.reload(true)";
                L.i(mTag, String.format("refreshing web page by executing \"%s\"", js));
                loadUrl(js);
                break;
            case R.id.app_common_title_view_name:
                if (mSubMenuWindow == null) {
                    return;
                }
                ((TextView)v).setCompoundDrawablesWithIntrinsicBounds(null, null,ResourcesCompat.getDrawable(getResources(),R.mipmap.contact_fast_jump_arrow_up,CordovaWebActivity.this.getTheme()), null);
                PopupWindowCompat.showAsDropDown(mSubMenuWindow, v, 0, 0, Gravity.NO_GRAVITY);
                break;
        }
    }




    private void loadUrl( final String id,final String title, final boolean initMenu){
        if (title.length()>0) {
            mAppTitleNameTV.setText(title);
        }
        if (initMenu) {
            initAppMenus(id);
        }
        //不需要链接地址最后的“/”符号
        L.i(mTag, url);
        loadUrl(url);
    }

    /**
     * 如果当前模块有子菜单，加载子菜单并显示
     * @param parentId 主页被点击的模块ID
     *
     */
    private void initAppMenus(String parentId) {
        if (!StringUtils.isEmpty(parentId)) {
            //获取submenu
            NewMyAppsDao dao = new NewMyAppsTb(this);
            NewMyApps app = dao.queryById(parentId);
            this.mAppMenus = JSON.parseArray(app.getSubmenu(),Submenu.class);
            int size = mAppMenus.size();

            boolean isShow = size > 0;
            if (isShow) {
                mIsShowDropMenu = true;
            }

            if (isShow) {
                ArrayList<HashMap<String, String>> subMenuData = new ArrayList<>(size);
                HashMap<String, String> subMenu;
                for (Submenu submenu : mAppMenus) {
                    subMenu = new HashMap<>();
                    subMenu.put("menu",submenu.getName());
                    subMenuData.add(subMenu);
                }

                SimpleAdapter mSubMenuListAdapter = new SimpleAdapter(CordovaWebActivity.this,subMenuData,R.layout.item_web_app_sub_menu,new String[] {"menu"},
                        new int[] {R.id.webApp_txt_subMenu_listItem_content});
                View subMenuView = this.getLayoutInflater().inflate(R.layout.view_web_app_sub_menu, null);
                ListView subMenuList = (ListView)subMenuView.findViewById(R.id.webApp_lst_subMenuList);
                subMenuList.setAdapter(mSubMenuListAdapter);
                subMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (mSubMenuWindow != null) {
                            mSubMenuWindow.dismiss();
                        }
                        Submenu menuApp = mAppMenus.get(position);
                        String originUrl = StringUtils.chop(mApplication.CONFIG_YNEDUT_V8_URL);
                        String appUrl = mAppMenus.get(position).getRoute();
                        try {
                            originUrl = URLEncoder.encode(originUrl, "utf-8");
                            if(!StringUtils.isEmpty(appUrl)) {
                                appUrl = URLEncoder.encode(appUrl, "utf-8");
                            }
                        } catch (UnsupportedEncodingException e) {
                            L.e(mTag, e.getMessage(), e);
                        }
                        final Context context = CordovaWebActivity.this;
                        StringBuilder urlArg = new StringBuilder("file:///android_asset/www/index.html#/")
                                .append(appUrl)
                                .append("?access_token=")
                                .append(token)
                                .append("&userId=")
                                .append(LastLoginUserSP.getLoginUserNo(context))
                                .append("&userType=")
                                .append(LastLoginUserSP.getUserType(context))
                                .append("&originUrl=")
                                .append(originUrl);

                        url = urlArg.toString();
                        L.i(mTag,"跳转url:"+url);
                        appView.getEngine().loadUrl(url,false);

                    }
                });

                //初始化弹出框
                mSubMenuWindow = new PopupWindow(subMenuView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,true);
                mSubMenuWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.color.main_content_bg,CordovaWebActivity.this.getTheme()));
                mSubMenuWindow.setOutsideTouchable(true);
                mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
                //popwin消失时改变箭头方向
                mSubMenuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
                    }
                });
            } else {
                mAppTitleNameTV.setCompoundDrawables(null,null,null,null);
                if (mSubMenuWindow != null) {
                    mSubMenuWindow = null;
                }
            }
        } else {
            mAppTitleNameTV.setCompoundDrawables(null,null,null,null);
            if (mSubMenuWindow != null) {
                mSubMenuWindow = null;
            }
        }
    }

}
