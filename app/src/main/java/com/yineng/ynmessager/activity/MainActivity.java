package com.yineng.ynmessager.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.Splash.CheckLoginImage;
import com.yineng.ynmessager.activity.Splash.CheckPopImageFactory;
import com.yineng.ynmessager.activity.Splash.CheckSplashImage;
import com.yineng.ynmessager.activity.app.NewAppFragment;
import com.yineng.ynmessager.activity.session.PlatformNoticeActivity;
import com.yineng.ynmessager.activity.session.SessionFragment;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.bean.event.NoticeEventEntity;
import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.NoticeEventTb;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IdPastListener;
import com.yineng.ynmessager.receiver.LoginStateReceiver;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.face.FaceConversionUtil;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import me.leolin.shortcutbadger.ShortcutBadger;
import okhttp3.Call;

public class MainActivity extends BaseActivity implements CordovaInterface {
    private View mRootView; // Activity的根布局
    private LoginStateReceiver loginStateReceiver;

    private PopupWindow mMenuWindow;
    private AlertDialog mExitConfirmDialog;
    /**
     * 身份验证过期的广播
     */
    private CommonReceiver mIdPastedReceiver;

    private MyTuisongReceive mNoticeEventTuiSongReceiver;
    public static final String ACTION_RETURN_TO_MAIN_RECENT = "return_to_main_recent";
    private static final String TAG = "MainFragment";
    /**
     * 网页布局
     */
    private TabHost mTabHost;
    private TextView mTxt_UnreadSession; // 未读会话的标签.
    private ImageView mTxtNewApp;    //新app标致
    private TextView mTxt_UnreadEvent; //未处理标签
    private RecentChatDao mRecentchatDao;
    private UnreadMsgCountReceiver mMsgCountReceiver;
    private LocalBroadcastManager mBroadcastManager;
    private V8TokenManager mV8TokenManager;
    private NoticeEventTb mNoticeEventTb;
    private SessionFragment mSessionFragment;
    private BroadcastReceiver mSwitchBroadcastReceiver;

    private AppController appController;
    //未读消息数量
    private int msgCount = 0;
    private BroadcastReceiver mNoticeEventReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NoticeEvent.ACTION_REFRESH_UNREAD.equals(action) || Const.BROADCAST_ACTION_CLEAR_SESSION_LIST.equals(
                    action)) {
                //统计刷新最新的平台通知+会话消息+广播合计条数。huyi 2016.1.8
                setUnreadSessionCount();

            }
        }
    };
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_content_frame);
        String deviceId = PushServiceFactory.getCloudPushService().getDeviceId();
        L.i(mTag, "Deviceid：   " + deviceId);
        FaceConversionUtil.getInstace().getFileText(getApplication());
        StatService.start(this);
        //用途在于：在app被杀死的情况下进入app首先要拿到接口地址和token再发广播执行推送点击操作
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NoticeEvent.ACTION_TUISONG_EVENT);
        if (mNoticeEventTuiSongReceiver == null) {
            mNoticeEventTuiSongReceiver = new MyTuisongReceive();
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mNoticeEventTuiSongReceiver, intentFilter);
        // 获取最后登录的用户名
        String loginUserNo = LastLoginUserSP.getInstance(getApplicationContext()).getUserAccount();
        //初始化application里的用户对象数据
        initAppUserInfoData(loginUserNo);

        //短信动态码获取次数初始化为0,并且要是手机验证码登录才可以清0
        int loginType = getIntent().getIntExtra("loginType", LoginConfig.LOGIN_TYPE_ACCOUNT);
        if (loginType == LoginConfig.LOGIN_TYPE_PHONE) {
            LoginConfig loginConfig = LastLoginUserSP.getInstance(getApplicationContext()).getServiceLoginConfig();
            loginConfig.setLastGetVerNum(0);
            LastLoginUserSP.getInstance(getApplicationContext()).saveServiceLoginConfig(loginConfig);
        }

        initView(savedInstanceState); // 初始化一些界面元素
        initIdPastDueBroadcast(); // 注册身份过期的广播
        checkPopImage();//检查是否更新广告图片

        //判断用户是否超时自动登录
        loginStateReceiver = new LoginStateReceiver();
        loginStateReceiver.setonLoginStateListener(new LoginStateReceiver.LoginStateListener() {
            @Override
            public void loginState(boolean isLogOut) {
                if (isLogOut) {
                    initIdPastDialog("");
                }
            }
        });
    }

    /**
     * 接收来自推送的操作,并且同时释放这个广播接收者
     */
    public class MyTuisongReceive extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NoticeEvent.ACTION_TUISONG_EVENT.equals(intent.getAction())) {
                L.e("------------", "推送跳转MainActivity");
                Intent platNoticeIntent = new Intent(MainActivity.this,
                        PlatformNoticeActivity.class);
                startActivity(platNoticeIntent);
            }
        }
    }


    /**
     * 检查是否更新广告图片
     */
    private void checkPopImage() {
        Log.e(mTag, "token:" + V8TokenManager.obtain());
        //检测启动页图片
        CheckPopImageFactory splashFactory = new CheckSplashImage();
        splashFactory.setmContext(this);
        splashFactory.checkNewImage();

        //检测登录图片
        CheckPopImageFactory loginFactory = new CheckLoginImage();
        loginFactory.setmContext(this);
        loginFactory.checkNewImage();
    }


    /**
     * 初始化application里的用户对象数据
     *
     * @param loginUserNo 用户唯一ID
     */
    private void initAppUserInfoData(String loginUserNo) {
        // 从common数据库取得当前登陆的用户登录帐号
        String loginUserAccount = LastLoginUserSP.getInstance(getApplicationContext()).getUserLoginAccount();
        // 根据用户登录帐号从数据库取得当前登陆的用户对象，保存到Application当中
        LoginUserDao loginUserDao = new LoginUserDao(getApplicationContext());
        mApplication.mLoginUser = loginUserDao.getLoginUserByAccount(loginUserAccount);
        // 全局初始化当前登录的用户信息
        ContactOrgDao contactOrgDao = new ContactOrgDao(getApplicationContext());
        User user = contactOrgDao.queryUserInfoByUserNo(loginUserNo);
        mApplication.mSelfUser = user!=null ? user : new User(loginUserNo,"",3,"","","","","","",0,0,0);
    }

    /**
     * 注册身份过期的广播
     */
    private void initIdPastDueBroadcast() {
        CommonReceiver.mNetWorkTypeStr = "";
        mIdPastedReceiver = new CommonReceiver();
        IntentFilter mIdPastedFilter = new IntentFilter(Const.BROADCAST_ACTION_ID_PAST);
        mIdPastedReceiver.setIdPastListener(new IdPastListener() {

            @Override
            public void idPasted() {
                if (!mIdPastDialog.isShowing()) {
                    initIdPastDialog("");
                }
            }
        });
        registerReceiver(mIdPastedReceiver, mIdPastedFilter);
    }

    /**
     * 初始化一些界面元素
     */
    private void initView(Bundle savedInstanceState) {
        mTxt_UnreadSession = (TextView) findViewById(R.id.main_txt_sessions_unreadNumbers);
        mTxt_UnreadEvent = (TextView)findViewById(R.id.main_txt_event_unreadNumbers);
        mTxtNewApp = (ImageView) findViewById(R.id.main_text_app_new);

        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mNoticeEventTb = new NoticeEventTb(this);
        appController = AppController.getInstance();
        installTabs();
        mRecentchatDao = new RecentChatDao(this);
        setUnreadSessionCount();
        registerUnreadMsgCountReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NoticeEvent.ACTION_REFRESH_UNREAD);
        intentFilter.addAction(Const.BROADCAST_ACTION_CLEAR_SESSION_LIST);
        mBroadcastManager.registerReceiver(mNoticeEventReceiver, intentFilter);

        mSwitchBroadcastReceiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                mTabHost.setCurrentTab(intent.getIntExtra("index", 0));
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mSwitchBroadcastReceiver, new IntentFilter(ACTION_RETURN_TO_MAIN_RECENT));

        mV8TokenManager = new V8TokenManager();

        mRootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // 初始化菜单键弹出菜单的PopupWindow
        View mMenuPopView = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_menu_popwindow, null);
        mMenuWindow = new PopupWindow(mMenuPopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
        mMenuWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.color.transparent, getTheme()));
        mMenuWindow.setAnimationStyle(R.style.AnimBottom);
        mMenuWindow.setFocusable(true);

        AlertDialog.Builder dialogBuilder = new Builder(this);
        dialogBuilder.setTitle(R.string.main_exit);
        dialogBuilder.setMessage(getString(R.string.main_exitConfirmMsg));
        dialogBuilder.setPositiveButton(R.string.main_confirm, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishActiviy();
            }
        });
        dialogBuilder.setNegativeButton(R.string.main_cancel, null);
        mExitConfirmDialog = dialogBuilder.create();


        // 重置通知栏中的未读联系人数和消息数
        NoticesManager noticesManager = NoticesManager.getInstance(getApplicationContext());
        noticesManager.mReceivedUserList.clear();
        noticesManager.mUnreadUserCount = 0;
        noticesManager.mUnreadMsgCount = 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
                break;
            case KeyEvent.KEYCODE_MENU:
                mMenuWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
                break;
        }
        return true;
    }

    /**
     * 界面监听事件
     *
     * @param v
     */
    public void onClickListener(View v) {
        switch (v.getId()) {
            case R.id.tv_main_menu_popwindow_help:// /帮助反馈
                ToastUtil.toastAlerMessage(MainActivity.this, "敬请期待...", 1000);
                break;
            case R.id.tv_main_menu_popwindow_exit:// 退出
                mExitConfirmDialog.show();
                break;
        }
        mMenuWindow.dismiss();
    }

    @Override
    protected void onNetWorkChanged(String info) {
        super.onNetWorkChanged(info);
        if("none".equals(info))
        {
            mSessionFragment.showUserNetOff(true);
        }else
        {
            mSessionFragment.showUserNetOff(false);
        }
    }

    private void installTabs()
    {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);

        mTabHost.setup();

        Resources res = getResources();

        mTabHost.addTab(getTab(res.getString(R.string.main_session), R.drawable.main_tabitem_session_selector)
                .setContent(R.id.main_fragment_session));
        mTabHost.addTab(getTab(res.getString(R.string.main_contact), R.drawable.main_tabitem_contact_selector)
                .setContent(R.id.main_fragment_contact));
        mTabHost.addTab(getTab(res.getString(R.string.main_app), R.drawable.main_tabitem_app_selector)
                .setContent(R.id.main_fragment_app));
        mTabHost.addTab(getTab(res.getString(R.string.main_my), R.drawable.main_tabitem_my_selector)
                .setContent(R.id.main_fragment_my));
        mSessionFragment = SessionFragment.getInstance();

        //如果有新的app就显示标识
        final NewAppFragment myAppsFragment = NewAppFragment.getInstance();
        myAppsFragment.setResume(true);
        myAppsFragment.setOnNewAppListener(new NewAppFragment.OnNewAppListener() {
            @Override
            public void onNewAppNotify(boolean isNew) {
                if(isNew){
                    mTxtNewApp.setVisibility(View.VISIBLE);
                }else{
                    mTxtNewApp.setVisibility(View.GONE);
                }
            }
        });

        //如果是其他的tab里就不能刷新应用里的数据
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if(tabId.equals("应用")){
                    myAppsFragment.setResume(true);
                }else{
                    myAppsFragment.setResume(false);
                }
            }
        });
    }


    /**
     * 得到一个tab.
     *
     * @return tab
     * @Title: getTab
     * @Description: 方法描述
     */
    private TabHost.TabSpec getTab(String tabtitle, int iconId)
    {
        View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.main_tabitem_indicator, null);

        TextView tv = (TextView) v.findViewById(R.id.tv_title);
        tv.setText(tabtitle);

        Resources res = getResources();
        int iconWidth = res.getDimensionPixelSize(R.dimen.main_tabItem_icon_width);
        int iconHeight = res.getDimensionPixelSize(R.dimen.main_tabItem_icon_height);
        Drawable icon = ResourcesCompat.getDrawable(getResources(), iconId, this.getTheme());
        icon.setBounds(0, 0, iconWidth, iconHeight);

        tv.setCompoundDrawables(null, icon, null, null);
        TabHost.TabSpec tabSpec = mTabHost.newTabSpec(tabtitle);
        tabSpec.setIndicator(v);
        return tabSpec;
    }

    /**
     * 注册显示未读消息的接收者
     *
     * @Title: registerUnreadNoticeCountReceiver
     * @Description: 方法描述
     */
    private void registerUnreadMsgCountReceiver()
    {
        if (mMsgCountReceiver == null)
        {
            mMsgCountReceiver = new UnreadMsgCountReceiver();
        }
        // 注册广播
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        this.registerReceiver(mMsgCountReceiver, ifilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mV8TokenManager.isMustUpdateToken()) {
            mV8TokenManager.initAppTokenData(true);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApplication.isShownOnScreen = false; // 表示当前程序界面已经退出
        //清除一下缓存，要不然可能不会缓存到磁盘
        ImageLoader.getInstance().getMemoryCache().clear();
        if(mIdPastDialog!=null) {
            mIdPastDialog.dismiss();
        }
        mMenuWindow.dismiss();
        mExitConfirmDialog.dismiss();
        unRegisterUnreadMsgCountReceiver();
        mBroadcastManager.unregisterReceiver(mNoticeEventReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mSwitchBroadcastReceiver);
        unregisterReceiverSafe(mIdPastedReceiver);

        //停止登录服务
        Intent serviceIntent = new Intent(this, XmppConnService.class);
        stopService(serviceIntent);
    }

    /**
     * 主界面退出的事件方法
     */
    private void finishActiviy() {
        //如果设置中没有勾选“退出仍然接收消息”，则完全关闭所有界面以及进程
        if (mApplication.mUserSetting.getReceiveWhenExit() == 0) {
            MainActivity.super.closeXmppService();
            BaseActivity.exit(true);
        } else {
            BaseActivity.exit(false);
        }
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        L.d(mTag, "");
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        L.d(mTag, "");
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Object onMessage(String id, Object data) {
        return null;
    }

    @Override
    public ExecutorService getThreadPool() {
        return null;
    }

    @Override
    public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {

    }

    @Override
    public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {

    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }
    private class UnreadMsgCountReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (Const.ACTION_UPDATE_UNREAD_COUNT.equals(intent.getAction()))
            {
                setUnreadSessionCount();
            }
        }
    }
    private void unRegisterUnreadMsgCountReceiver()
    {

        if (mMsgCountReceiver != null)
        {
            this.unregisterReceiver(mMsgCountReceiver);
        }
    }

    /**
     * 设置未读消息数
     *
     * @Title: setUnreadNoticeCount
     * @Description: 方法描述
     */
    private void setUnreadSessionCount()
    {

        Map<String,String> params = new HashMap<>();
        params.put("msgType", "");
        params.put("userId", LastLoginUserSP.getLoginUserNo(this));
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        OKHttpCustomUtils.get(URLs.GET_NOTICE_URL_NUM, params, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                mNoticeEventTb.delete();//清空数据库中所有的内容
                try {
                    int status = response.optInt("status");
                    if (status==0){
                        JSONObject resultObj = response.getJSONObject("result");
                        msgCount = resultObj.optInt("msgCount");
                        appController.UnReedNoticeNum = msgCount;
                        NoticeEventEntity msgEntity = JSON.parseObject(resultObj.optString("msg"), NoticeEventEntity.class);
                        if (msgEntity!=null){
                            NoticeEvent event = new NoticeEvent();
                            event.setUserNo(msgEntity.getSenderId());
                            event.setUserName("sendName");
                            event.setReceiver(LastLoginUserSP.getLoginName(getActivity()));
                            event.setTitle(msgEntity.getSubject());
                            event.setMessage(msgEntity.getMsgContent());
                            event.setContent(msgEntity.getMsgContent());
                            event.setMsgId(msgEntity.getMsgId());
                            event.setHasAttachment(msgEntity.getHasAttachment());
                            event.setTimeStamp(TimeUtil.convertStringDate(msgEntity.getSendTime()));
                            event.setHasPic(msgEntity.getHasPic());
                            event.setMessageType(msgEntity.getMessageType());
                            event.setRead(!msgEntity.getReadStatus().equals("0"));
                            mNoticeEventTb.insert(event);
                        }
                    }

                } catch (JSONException e) {
                    L.e(mTag, e.getMessage(), e);
                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                L.i(mTag, " MainFragment广播通知获取数据失败");
                IconBadgerHelper.showIconBadger(getActivity());
            }

            @Override
            public void onAfter(int id) {
                //接收到来自其他位置刷新的通知之后再刷新消息页平台通知数量
                if (mSessionFragment != null) {
                    mSessionFragment.createPlatformNoticeListObj();
                    mSessionFragment.mSwipeListviewAdapter.notifyDataSetChanged();
                }
                if (mTxt_UnreadSession != null)
                {
                    // 更新未读通知条数
                    if (mRecentchatDao==null){
                        mRecentchatDao = new RecentChatDao(MainActivity.this);
                    }
                    int unreadNoticesCount = mRecentchatDao.getUnReadMsgCount();
                    //统计最新的平台通知+会话消息+广播合计条数。huyi 2016.1.7
                    unreadNoticesCount = unreadNoticesCount+msgCount;//end
                    if (unreadNoticesCount > 0)
                    {
                        if (unreadNoticesCount > 99)
                        {
                            mTxt_UnreadSession.setText(R.string.main_99plus);
                        } else
                        {
                            mTxt_UnreadSession.setText(String.valueOf(unreadNoticesCount));
                        }
                        mTxt_UnreadSession.setVisibility(View.VISIBLE);
                    } else
                    {
                        mTxt_UnreadSession.setText("");
                        mTxt_UnreadSession.setVisibility(View.INVISIBLE);
                    }
                    //刷新app图标提示数字
                    ShortcutBadger.applyCount(getActivity(), unreadNoticesCount);
                }else{
                    // 更新桌面图标当前登陆用户的未读消息的显示
                    IconBadgerHelper.showIconBadger(getActivity());
                }
            }
        });
    }
}
