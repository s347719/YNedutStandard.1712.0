package com.yineng.ynmessager.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.tencent.smtt.sdk.QbSdk;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.session.SessionFragment;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.update.UpdateCheckUtil;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.ClientInitConfig.AddressBean;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.OfflineMessageList;
import com.yineng.ynmessager.bean.OfflineMessageList.OfflineMsg;
import com.yineng.ynmessager.bean.contact.ContactGroupBean;
import com.yineng.ynmessager.bean.contact.ContactOrg;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.contact.UserStatus;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.offline.OfflineMsgList;
import com.yineng.ynmessager.bean.offline.UnRead;
import com.yineng.ynmessager.bean.offline.UnReadProvider;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.receiver.LoginStateReceiver;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.IQPacketListenerImpl;
import com.yineng.ynmessager.smack.MessagePacketListenerImpl;
import com.yineng.ynmessager.smack.PresencePacketListenerImpl;
import com.yineng.ynmessager.smack.PresencePacketListenerImpl.groupCreatedListener;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.OfflineMessageUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.yninterface.TokenLoadedListener;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.sasl.SASLMechanism.Failure;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * 后台服务，负责消息监听及用户状态监听
 *
 * @author YINENG
 */
public class XmppConnService extends Service implements ReceiveReqIQCallBack {
    protected final String mTag = this.getClass().getSimpleName();
    /**
     * 重登失败后的重登次数
     */
    private int mLoginedTimes = 0;
    /**
     * 是否是从登录界面进入
     */
    private boolean mIsLoginActivity = false;

    /**
     * 组织机构是否加载完毕
     */
    public boolean mOrgLoaded = false;
    /**
     * 群讨论组是否加载完毕
     */
    public boolean mGroupLoaded = false;
    public final int OFFLINE_MESSAGE_LOADED = 100;
    public final int CONTACT_DATA_LOADED = 101;
    private Context mContext;// 全局上下文
    private XmppConnectionManager mXmppConnManager;
    private PacketListener mIQListener;
    private PresencePacketListenerImpl mPresenceListener;
    private PacketListener mMessageListener;
    private PacketListener mFailureListener = new PacketListener() {

        @Override
        public void processPacket(Packet packet) {
            Failure tempFailure = (Failure) packet;
            if ("not-authorized".equals(tempFailure.getCondition())) {
                sendBroadcast(new Intent(Const.BROADCAST_ACTION_ID_PAST));
            }
        }
    };
    private int mCurrentStats = 0;
    private XMPPConnection mXMPPConnection;
    private PacketFilter mIqFilter;
    private PacketTypeFilter mPresenceFilter;
    private PacketTypeFilter mMSGFilter;

    private AppController mApplication;
    /**
     * 离线消息
     */
    private List<OfflineMsgList> mOffMsgList = new ArrayList();

    /**
     * 发送创建讨论组成功的广播
     */
    Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
    /**
     * 单人会话资料页创建讨论组成功的发送广播
     */
    Intent createGroupFromP2PIntent = new Intent(Const.BROADCAST_ACTION_CREATE_GROUP);

    /**
     * 正在加载联系人信息的广播
     */
    Intent mLoadingContactDataIntent = new Intent(Const.BROADCAST_ACTION_LOADING_CONTACT);

    /**
     * 加载联系人信息完毕的广播
     */
    Intent mLoadedContactDataIntent = new Intent(Const.BROADCAST_ACTION_LOADED_CONTACT);

    /**
     * 服务器推送消息的广播
     */
    Intent mServiceNoticeIntent = new Intent(Const.BROADCAST_ACTION_SERVICE_NOTICE);


    public static final String BROADCAST_ACTION_REFRESH_CONTACT = "action.refresh.contact";

    private LocalBroadcastManager localBroadcastManager;
    /**
     * 启动登陆初始化的操作
     */
    private Runnable mDoInitRunnable = new Runnable() {
        @Override
        public void run() {
            //如果重登大于了一次，下次重登将重新初始化xmppManager
            if (mLoginedTimes > 1 && mXmppConnManager != null) {
                mXmppConnManager.setXmppConnectionConfigNull();
            }
            init();
        }
    };
    private Looper looper;
    private ServiceHandler mHandler;
    private final class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg)
        {
            int x = msg.arg1;
            switch (x) {

                case LoginThread.LOGIN_START:// 登陆中，登陆按钮不可用
                    L.v("XmppConnService ", "handleMessage ->LOGIN_START");
                    mCurrentStats = LoginThread.USER_STATUS_ONLOGIN;
                    break;
                case LoginThread.LOGIN_FAIL:// 登陆失败
                    L.v("XmppConnService ", "handleMessage ->LOGIN_FAIL");
                    //刷新主页-消息列表的提示
                    mHandler.removeCallbacks(mDoInitRunnable);
                    dispachtUserStatus();
                    break;
                case LoginThread.LOGIN_SUCCESS:// 登陆成功
                    L.v("XmppConnService ", "handleMessage ->LOGIN_SUCCESS");
                    mLoginedTimes = 0;
                    mHandler.removeCallbacks(mDoInitRunnable);
                    //如果存在已登录的帐号就执行Smack监听 并获取联系人
                    if (LastLoginUserSP.isExistLoginedUser(mContext)) {
                        /**NOTE:
                         * 1.已存在登录记录时，app被系统杀死后服务重启时
                         * 2.网络断开恢复时，
                         * 用户需要重登，重登成功后，此时需要初始化数据，并检测升级
                         */
                        // 注册消息接收监听器
                        initListener();
                        // 初始化数据并检测升级
                        initClientData();
                        dispachtUserStatus();
                    } else {
                        //概率很小的情况
                        /**
                         * NOTE:
                         * 防止注销帐号前，网络刚好恢复，又进入了自动重登的线程，最后登录成功
                         再去获取联系人，导致attempt to re-open an already-closed object崩溃的bug,
                         应该断掉smack连接，走一次帐号下线的流程
                         **/
                        /**
                         * 后期建议去掉自动定时登录的功能，改为消息列表界面手动下拉刷新登录的功能
                         **/
                        if (mXmppConnManager != null) {
                            mXmppConnManager.doExitThread();
                        }
                    }
                    break;

                case LoginThread.LOGIN_TIMEOUT:// 建立连接失败
                    L.v("XmppConnService ", "handleMessage ->LOGIN_TIMEOUT");
                    //刷新主页-消息列表的提示
                    mHandler.removeCallbacks(mDoInitRunnable);
                    dispachtUserStatus();
                    L.e("XmppConnService",getResources().getString(R.string.request_error_openfire));
//                    ToastUtil.toastAlerMessage(mContext,getResources().getString(R.string.request_failed) + " " + getResources().getString(R.string.request_error_openfire), 1000);
                    break;
                case LoginThread.LOGIN_USER_ACCOUNT_ERROR:// 用户名或密码错误
                    L.v("XmppConnService ", "LOGIN_USER_ACCOUNT_ERROR");
                    //刷新主页-消息列表的提示
                    mHandler.removeCallbacks(mDoInitRunnable);
                    mCurrentStats = LoginThread.LOGIN_USER_ACCOUNT_ERROR;
                    //发送退出广播
                    Intent intent = new Intent("com.ynmessager.login_state");
                    intent.putExtra("type", LoginStateReceiver.TYPE_ACCOUNT_ERROR);
                    intent.putExtra("state", LoginThread.LOGIN_USER_ACCOUNT_ERROR);
                    sendBroadcast(intent);
                    dispachtUserStatus();
                    break;
                case LoginThread.LOGIN_USER_ACCOUNT_OUT:
                    //发送退出广播
                    Intent intent2 = new Intent("com.ynmessager.login_state");
                    intent2.putExtra("type", LoginStateReceiver.TYPE_ACCOUNT_ERROR);
                    intent2.putExtra("state", LoginThread.LOGIN_USER_ACCOUNT_ERROR);
                    sendBroadcast(intent2);
                    break;
                case LoginThread.LOGIN_SERVER_ERROR:// 服务器或地址错误
                    L.i("XmppConnService ", "LOGIN_SERVER_ERROR");
                    //刷新主页-消息列表的提示
                    mHandler.removeCallbacks(mDoInitRunnable);
                    dispachtUserStatus();
                    break;
                case LoginThread.LOGIN_SERVER_NOT_RESPON:// 服务器没响应
                    L.i("XmppConnService ", "LOGIN_SERVER_NOT_RESPON");
                    //刷新主页-消息列表的提示
                    mHandler.removeCallbacks(mDoInitRunnable);
                    dispachtUserStatus();
                    L.e("XmppConnService",getResources().getString(R.string.request_error_openfire));
//                    ToastUtil.toastAlerMessage(mContext,getResources().getString(R.string.request_failed) + " " + getResources().getString(R.string.request_error_openfire), 1000);
                    break;
                default:
                    break;
            }
            switch (msg.what) {
                case OFFLINE_MESSAGE_LOADED:
                    handleOfflineMsgByThread();
                    break;
                case CONTACT_DATA_LOADED:
                    // 下载头像
                    L.e(" 下载头像");
                    FileUtil.downloadAvatarZipFile(mContext, null, "3");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * xmpp连接状态监听器
     */
    private ConnectionListener mXmppConListener = new ConnectionListener() {

        @Override
        public void reconnectionSuccessful() {
            L.i("reconnectionSuccessful");
            dispachtUserStatus();
        }

        @Override
        public void reconnectionFailed(Exception arg0) {
            L.e("reconnectionFailed");
            dispachtUserStatus();
        }

        @Override
        public void reconnectingIn(int arg0) {
            //  Auto-generated method stub
            L.e("reconnectingIn");
            dispachtUserStatus();
        }

        @Override
        public void connectionClosedOnError(Exception arg0) {
            //  Auto-generated method stub
            arg0.printStackTrace();
            L.e("connectionClosedOnError ：" + arg0.getMessage());
            if (mXmppConnManager == null) {
                return;
            }
            if (arg0.getMessage().contains("conflict")) {//再其他设备上登录
                //在其他设备上登录的时候同时解绑阿里推送帐号
                PushServiceFactory.getCloudPushService().unbindAccount(
                        new CommonCallback() {
                            @Override
                            public void onSuccess(String response) {
                            }

                            @Override
                            public void onFailed(String errorCode, String errorMessage) {
                            }
                        }
                );
                sendBroadcast(new Intent(Const.BROADCAST_ACTION_LOGINED_OTHER));
                mXmppConnManager.setLoginedOther(true);
            } else if (arg0.getMessage().contains("system-shutdown")) {
                //服务器关闭了
                mXmppConnManager.setIsServerShutDown(true);
            } else if (arg0.getMessage().contains("Connection timed out")) {
            }
            dispachtUserStatus();
        }

        @Override
        public void connectionClosed() {
            //  Auto-generated method stub
            L.e("connectionClosed");
            //为了避免在某些手机，进程存在的情况下，长时间不用掉线会有一个瞬间在首页显示掉线的提示，所以此时注掉
//            dispachtUserStatus();
        }
    };

    /**
     * 通过线程处理离线消息
     */
    protected void handleOfflineMsgByThread() {
        FileDownLoader.getInstance().getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                handleOfflineMsg();
                mContext.sendBroadcast(mLoadedContactDataIntent);
            }
        });
    }

    /**
     * 判断V8的相关地址是否为空
     *
     * @return
     */
    protected boolean isV8UrlEmpty() {
        return TextUtils.isEmpty(mApplication.CONFIG_INSIDE_IP)
                || TextUtils.isEmpty(mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP)
                || TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_URL)
                || TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST)
                || TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL)
                || TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_APP_MENUS_URL)
                || TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL);
    }

    /**
     * 服务重登成功后，根据条件判断是否需要重新获取联系人数据
     */
    protected void reloadContactsInfoData() {
        if (mContactOrgDao != null) {
            mOffMsgList.clear();
            mContactGroupList.clear();
            resetContactLoadStatus();
//			removeAllInitIQCallback();
//			addAllInitIqCallback();
            ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
            //如果V8的相关地址为空，或者openfire相关初始化数据为空，则重新获取初始化信息
            if (isV8UrlEmpty() || mClientInitConfig == null) {

                sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_CLIENT_INIT, false);// 客户端初始化请求
            }

            //查询组织机构根节点是否存在，如果不存在就IQ请求全量更新组织机构
            List<OrganizationTree> tempList = mContactOrgDao.queryOrgListByParentId("0");
            if (tempList == null) {
                sendRequestIQPacket(Const.ORG_UPDATE_ALL, Const.REQ_IQ_XMLNS_GET_ORG, false);// 组织机构全量请求
            } else {
                mOrgLoaded = true;
            }
            if (NetWorkUtil.isNetworkAvailable(mContext)) {
                mContext.sendBroadcast(mLoadingContactDataIntent);
                //获取群组和讨论组
                sendRequestIQPacket(Const.CONTACT_GROUP_TYPE,
                        Const.REQ_IQ_XMLNS_GET_GROUP, false);// 群请求
                sendRequestIQPacket(Const.CONTACT_DISGROUP_TYPE,
                        Const.REQ_IQ_XMLNS_GET_GROUP, false);// 讨论组请求
            }
        }
    }

    /**
     * 网络恢复后重新登录过程中，出现登录失败后启用定时重新登录的功能
     */
    protected void reLoginByRunableTimes() {
        mHandler.removeCallbacks(mDoInitRunnable);
//		if (NetWorkUtil.isNetworkAvailable(mContext)
//				&& mLoginedTimes < MAX_TIME && LastLoginUserSP.isExistLoginedUser(mContext)) {// 如果网络可用
//			mHandler.postDelayed(mDoInitRunnable, 6*1000);// 每6s自动登陆
//		}
        dispachtUserStatus();
    }

    /**
     * 处理离线消息
     */
    protected void handleOfflineMsg() {
        OfflineMsgList errorMsg = null;
        try {
            if (mOffMsgList.size() > 0) {
                //添加一个provider
                ProviderManager.getInstance().addExtensionProvider(UnRead.NAME, Const.BROADCAST_ACTION_UNREADER_MSG_NUM, new UnReadProvider());
                Iterator<OfflineMsgList> offlineMsgListIterator = mOffMsgList.listIterator();
                while (offlineMsgListIterator.hasNext()) {
                    OfflineMsgList offlineMsg = offlineMsgListIterator.next();
                    errorMsg = offlineMsg;
                    Message message = OfflineMessageUtil.parserLastMessage(offlineMsg.getLastMsg());
                    //添加未读消息数
                    UnRead unRead = new UnRead();
                    unRead.setNum(offlineMsg.getUnreadCount());
                    unRead.setUnreadMsgIds(offlineMsg.getUnreadMsgIds());
                    message.addExtension(unRead);
                    mMessageListener.processPacket(message);
                    offlineMsgListIterator.remove();
                }
            }
        } catch (Exception e) {
            mOffMsgList.remove(errorMsg);
            handleOfflineMsg();
        }
        mContext.sendBroadcast(mLoadedContactDataIntent);
    }

    /**
     * 联系人数据库操作类
     */
    private ContactOrgDao mContactOrgDao;
    private String mPacketId;
    /**
     * 登录失败的packet
     */
    private PacketTypeFilter mFailureFilter;
    /**
     * 网络状态变更广播监听器
     */
    private NetWorkChangedReceiver mNetWorkReceiver;
    /**
     * 用于一次性保存群组和讨论组信息数据的列表
     */
    private ArrayList<ContactGroupBean> mContactGroupList = new ArrayList<ContactGroupBean>();
    /**
     * 本次组织机构更新方式 0:全量  1：增量
     */
    private int ORG_UPDATE_TYPE = Const.ORG_UPDATE_ALL;

    /**
     * 如果是服务被创建了（当首次startService、stopService后调用startService
     * 以及App被系统杀死再启动Service时会调用onCreate）,则获取联系人的信息
     */
    private boolean isServiceCreate = false;
    private V8TokenManager v8TokenManager;

    private QbSdk.PreInitCallback myCallback = new QbSdk.PreInitCallback() {


        @Override
        public void onCoreInitFinished() {
            L.e("QbSdk: onCoreInitFinished");
        }

        @Override
        public void onViewInitFinished(boolean b) {
            L.e("QbSdk: onViewInitFinished");
        }


    };

    @Override
    public void onCreate() {
        super.onCreate();
        L.v("XmppService", " onCreate: QbSdk.isTbsCoreInited() == " + QbSdk.isTbsCoreInited() + " getApplicationContext() == " + getApplicationContext());
        mContext = this;
        isServiceCreate = true;
        if (!QbSdk.isTbsCoreInited()) {
            QbSdk.preInit(getApplicationContext(), myCallback); //这里必须启用非主进程的service来预热X5内核
        }
        HandlerThread thread = new HandlerThread("MessageDemoThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // 获取当前线程中的looper对象
        looper = thread.getLooper();
        //创建Handler对象，把looper传递过来使得handler、
        //looper和messageQueue三者建立联系
        mHandler = new ServiceHandler(looper);
        v8TokenManager = new V8TokenManager();
        v8TokenManager.setTokenLoadedListener(new TokenLoadedListener() {
            @Override
            public void tokenLoaded() {
                if (LastLoginUserSP.getInstance(mContext).isLogin()) {//用户在线开启定位服务
                    if (!AppUtils.isServiceWork(mContext, LocateService.class.getName())) {//不存在该服务启动定位服务
                        startService(new Intent(mContext, LocateService.class));
                    }
                }
                if (AppController.getInstance().isAliTuisong) {
                    L.e("------------", "推送跳转");
                    AppController.getInstance().isAliTuisong = false;
                    //如果是从推送过来的弹框点击进入直接去平台通知页面
                    localBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_TUISONG_EVENT));
                    //在登录的状态下初次进入消息页无法获取平台通知接口地址，所以在解析完成之后发送广播去告知刷新
                    localBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH_UNREAD));
                } else {
                    localBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH_UNREAD));
                }
            }
        });
        mApplication = AppController.getInstance();
        // filter
        mIqFilter = new PacketTypeFilter(IQ.class);
        mPresenceFilter = new PacketTypeFilter(Presence.class);
        mMSGFilter = new PacketTypeFilter(Message.class);
        mFailureFilter = new PacketTypeFilter(Failure.class);
        localBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mNetWorkReceiver = new NetWorkChangedReceiver();
        mNetWorkReceiver.isServiceStarted(false);
        IntentFilter mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(BROADCAST_ACTION_REFRESH_CONTACT);
        registerReceiver(mNetWorkReceiver, mIntentFilter);
        //注册eventbus
        EventBus.getDefault().register(this);

    }

    /**
     * 初始网络链接及监听器
     *
     * @NOTE: service是被创建的情况:
     * 1.当从登录界面进来，isServiceCreate和mIsLoginActivity都为true，只需获取联系人数据，不需检测升级
     * 2.已存在登录记录且服务已启动,app被系统杀死，服务再自动重启，isServiceCreate为true，mIsLoginActivity为false
     * 此时需要检测初始化数据并检测升级，第二种情况进入if(mXmppConnManager.isAuthenticated())的几率较小，
     * 因为服务重启后需要重新登录，应该会执行else的重登方法。
     ***/
    private void init() {
//		isLoadingContact = false;
        L.v(XmppConnService.class, "start init function");
        LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
        if (lastUser.isExistsUser()) {
            // 如果有用户登陆记录
            mContactOrgDao = new ContactOrgDao(mContext,
                    lastUser.getUserAccount());
            // mHandler.postDelayed(mStatusChangedRunnable, 3000);// 开启用户状态监听
            if (mXmppConnManager == null) {
                mXmppConnManager = XmppConnectionManager.getInstance();
            }
            //注册身份验证过期的消息监听  huyi 2016.1.22注释，移动到initListener里面
//			mXmppConnManager.removePacketListener(mFailureListener);
//			mXmppConnManager.addPacketListener(mFailureListener, mFailureFilter);//end
            if (!mXmppConnManager.isInit()) {
                mXmppConnManager.init(LoginThread.getHostFromAddress(LastLoginUserSP.getServerInfoOpenFireHost(mContext)), LoginThread
                                .getPortFromAddress(LastLoginUserSP.getServerInfoOpenFireHost(mContext)),
                        Const.RESOURSE_NAME);
                L.i(mTag, "接收包" + "初始化的信息：" + LoginThread.getHostFromAddress(LastLoginUserSP.getServerInfoOpenFireHost(mContext))
                        + " -- " + LoginThread.getPortFromAddress(LastLoginUserSP.getServerInfoOpenFireHost(mContext)));
            }
            if (mXmppConnManager.isAuthenticated()) {
                L.v("XmppConnService ", "init  ->isAuthenticated() == true");
                initListener();// 注册消息接收监听器
                if (isServiceCreate && !mIsLoginActivity) {
                    initClientData();
                } else if (isServiceCreate && mIsLoginActivity) {
//					initContactsInfoData(true);
                    EventBus.getDefault().post(new ContactOrg("1"));//更新联系人信息
                    isServiceCreate = false;
                }
                mIsLoginActivity = false;
                // 刷新消息列表的界面,改为已上线
                mXmppConnManager.doStatusChangedCallBack(LoginThread.USER_STATUS_ONLINE);
                v8TokenManager.initAppTokenData(true);
            } else {
                // 如果网络可用,开启登陆线程
                if (NetWorkUtil.isNetworkAvailable(mContext)) {
                    mLoginedTimes = mLoginedTimes + 1;
                    mXmppConnManager.doReLoginThread(lastUser.getUserAccount(),
                            lastUser.getUserPassword(), Const.RESOURSE_NAME,
                            mHandler, mContext, true);
                } else {
                    reLoginByRunableTimes();
                }
            }
        }
    }

    /**
     * 初始化所有消息监听器
     */
    private void initListener() {
        // cancel listener
        unRegisterPacketListener();
        // register
        registerPacketListener();
    }

    /**
     * 注册消息监听器
     */
    private void registerPacketListener() {
        //初始化smack监听器
        initSmackMsgListener();
        mXmppConnManager.setIsServerShutDown(false);
        mXmppConnManager.setLoginedOther(false);
        //注册全局的IqLisntener
        mXmppConnManager.addPacketListener(mIQListener, mIqFilter);
        //注册全局的PresenceLisntener
        mXmppConnManager.addPacketListener(mPresenceListener, mPresenceFilter);
        //注册全局的messageLisntener
        mXmppConnManager.addPacketListener(mMessageListener, mMSGFilter);
        //注册身份验证过期的消息监听
        mXmppConnManager.addPacketListener(mFailureListener, mFailureFilter);
        if (mXMPPConnection == null) {
            mXMPPConnection = mXmppConnManager.getConnection();
        }
        mXMPPConnection.removeConnectionListener(mXmppConListener);
        mXMPPConnection.addConnectionListener(mXmppConListener);

        mPresenceListener.setGroupCreatedListener(new groupCreatedListener() {

            @Override
            public void groupCreated(String mGroupName) {
                sendRequestIQPacket(mGroupName);
            }
        });
    }

    /**
     * 取消所有消息监听器
     */
    private void unRegisterPacketListener() {
        if (mXmppConnManager != null) {
            if (mIQListener != null) {
                mXmppConnManager.removePacketListener(mIQListener);
            }
            if (mPresenceListener != null) {
                mXmppConnManager.removePacketListener(mPresenceListener);
            }
            if (mMessageListener != null) {
                mXmppConnManager.removePacketListener(mMessageListener);
            }
            mXmppConnManager.removePacketListener(mFailureListener);
        }
    }

    /**
     * 初始化smack监听器
     */
    private void initSmackMsgListener() {
        // new listener
        mIQListener = null;
        mPresenceListener = null;
        mMessageListener = null;
        mIQListener = new IQPacketListenerImpl(this);
        mPresenceListener = new PresencePacketListenerImpl(this);
        mMessageListener = new MessagePacketListenerImpl(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.v("XmppService", " onStartCommand! ");
        mLoginedTimes = 0;
        if (intent != null) {
            mIsLoginActivity = intent.getBooleanExtra("isLoginActvity", false);
        }
        init();// 初始网络链接及监听器

        return START_STICKY;// 服务应该一直运行除非我们手动停止它
    }

    @Override
    public IBinder onBind(Intent arg0) {
        L.v("XmppService", " onBind! ");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.v("XmppService", " onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.v("XmppService", " onDestroy: ");
        // Unregister
        EventBus.getDefault().unregister(this);
        mHandler.removeCallbacks(mDoInitRunnable);
        mHandler.removeCallbacksAndMessages(null);
        unRegisterPacketListener();
        unregisterReceiver(mNetWorkReceiver);
        if (mXmppConnManager != null) {
            mXmppConnManager.doExitThread();
        }
    }
    @Subscribe(threadMode  = ThreadMode.POSTING)
    public void onEventMainThread(ContactOrg initContact) {
        if (initContact == null || TextUtils.isEmpty(initContact.getStatus())) {
            return;
        }
        switch (initContact.getStatus()) {
            case "0"://在已有登录记录重登，获取版本信息失败时，更新消息列表提示状态
                L.i("XmppService", "onEventMainThread 断开连接");
//				mXmppConnManager.setIsServerShutDown(true);
                dispachtUserStatus();
                break;
            case "1"://更新联系人
                mXmppConnManager.setIsServerShutDown(false);
                //当service是被创建的（当从登录界面进来，app被系统杀死时）去获取联系人信息
                if (isServiceCreate) {
                    isServiceCreate = false;
                    L.i("XmppService", "onEventMainThread 加载联系人");
                    // 获取联系人信息
                    initContactsInfoData(true);
                } else {
                    reloadContactsInfoData();
                }

                //发送登陆成功广播（！！！注意是用LocalBroadcastManager发送的，接收广播也需这个！！！）
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                localBroadcastManager.sendBroadcast(new Intent(Const.BROADCAST_ACTION_USER_LOGIN));
                break;
            default:
                break;
        }
    }

    /**
     * 当用户状态发生变化时，把最新状态分发给状态监听器
     */
    private void dispachtUserStatus() {
        if (mXmppConnManager != null) {
            int status = mXmppConnManager.getUserCurrentStatus();
            L.v("XmppService", " dispachtUserStatus:status = " + status
                    + " ;mCurrentStats=" + mCurrentStats + " mLoginedTimes="
                    + mLoginedTimes);
            //通知消息列表界面停止刷新
            mXmppConnManager.doStatusChangedCallBack(SessionFragment.STOP_REFRESHING);// 分发用户状态给所有状态监听器
            if (mCurrentStats != status) {
                //如果xmppConnect连接断开了的情况下，还要判断网络是否断开了.因为网络断开的情况优先提示
                if (status == LoginThread.USER_STATUS_CONNECT_OFF || status == LoginThread.USER_STATUS_OFFLINE||status == LoginThread.USER_STATUS_NETOFF) {
                    if (!NetWorkUtil.isNetworkAvailable(mContext)) {
                        status = LoginThread.USER_STATUS_NETOFF;
                    }
                }
                //如果此时是免登陆页面才发生的401错误那么是帐号验证过期或者已被删除的账户。在主页面提示
                if (mCurrentStats == LoginThread.LOGIN_USER_ACCOUNT_ERROR) {
                    mXmppConnManager.doStatusChangedCallBack(mCurrentStats);// 分发用户状态给所有状态监听器
                } else {
                    mXmppConnManager.doStatusChangedCallBack(status);// 分发用户状态给所有状态监听器
                }
                mCurrentStats = status;

            }
        }
    }

    /**
     * 获取客户端初始化信息(即openfire里配置的v8相关地址)，然后去检测升级
     */
    public void initClientData() {
        removeClientInitIQCallback();
        sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_CLIENT_INIT, true);// 客户端初始化请求
    }

    /**
     * 获取联系人（组织机构，群组，讨论组信息）数据
     *
     * @param sendBroadCast
     */
    public void initContactsInfoData(boolean sendBroadCast) {
        if (NetWorkUtil.isNetworkAvailable(mContext) && sendBroadCast) {
            mContext.sendBroadcast(mLoadingContactDataIntent);
//			isLoadingContact = true;
        }
        ORG_UPDATE_TYPE = Const.ORG_UPDATE_ALL;
        mOffMsgList.clear();
        mContactGroupList.clear();
        resetContactLoadStatus();
        removeAllInitIQCallback();
        sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_NOTICE, true);//服务器推送监听
        ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
        if (mClientInitConfig != null) {
            if (mClientInitConfig.getOrg_update_type() == 1) { // 增量更新
                sendRequestIQPacket(Const.ORG_UPDATE_SOME,
                        Const.REQ_IQ_XMLNS_GET_ORG, true);// 组织机构请求
            } else { // 全量更新
                sendRequestIQPacket(Const.ORG_UPDATE_ALL,
                        Const.REQ_IQ_XMLNS_GET_ORG, true);// 组织机构请求
            }
            sendRequestIQPacket(Const.CONTACT_GROUP_TYPE,
                    Const.REQ_IQ_XMLNS_GET_GROUP, true);// 群请求
            sendRequestIQPacket(Const.CONTACT_DISGROUP_TYPE,
                    Const.REQ_IQ_XMLNS_GET_GROUP, false);// 讨论组请求
        } else {
            sendRequestIQPacket(Const.ORG_UPDATE_ALL,
                    Const.REQ_IQ_XMLNS_GET_ORG, true);// 组织机构请求
            sendRequestIQPacket(Const.CONTACT_GROUP_TYPE,
                    Const.REQ_IQ_XMLNS_GET_GROUP, true);// 群请求
            sendRequestIQPacket(Const.CONTACT_DISGROUP_TYPE,
                    Const.REQ_IQ_XMLNS_GET_GROUP, false);// 讨论组请求
        }
//		sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_GET_STATUS,true);
//		loadOfflineMessages();
    }

    /**
     * 发送IQ请求
     *
     * @param action      接口方向
     * @param nameSpace   命名空间
     * @param addCallBack 是否添加监听
     */
    private void sendRequestIQPacket(int action, String nameSpace, boolean addCallBack) {
        if (addCallBack) {
            mXmppConnManager.addReceiveReqIQCallBack(nameSpace,
                    XmppConnService.this);
        }
        if (nameSpace.equals(Const.REQ_IQ_XMLNS_NOTICE)) {
            return;
        }
        ReqIQ iq = new ReqIQ();
        switch (action) {
            case Const.ORG_UPDATE_SOME:// 组织机构增量更新
                ORG_UPDATE_TYPE = Const.ORG_UPDATE_SOME;

                iq.setParamsJson("{\"servertime\":\""
                        + mContactOrgDao.getInitServerTime() + "\"}");
                if (!addCallBack) {//说明是Notice
                    mPacketId = iq.getPacketID();
                }
                break;
            case Const.GET_OFFLINE_MSG:// 离线消息
                iq.setParamsJson("");
                iq.setTo("admin@" + mXmppConnManager.getServiceName());
                iq.setAction(1);
                break;
            case Const.CONTACT_GROUP_TYPE:// 群组
            case Const.CONTACT_DISGROUP_TYPE:// 讨论组
                iq.setAction(action);
                mPacketId = iq.getPacketID();
                break;
            default:
                break;
        }
        iq.setNameSpace(nameSpace);
        iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getLoginUserNo(mContext)));
        L.i("XmppConnService", "iq xml ->" + iq.toXML());
        try {
            mXmppConnManager.sendPacket(iq);
        } catch (Exception e) {
            e.printStackTrace();
            mContext.sendBroadcast(mLoadedContactDataIntent);
        }
    }

    @Override
    public void receivedReqIQResult(ReqIQResult packet) {
        //如果帐号已经注销，或该帐号不存在已登录的状态(概率极小的情况)
        if (!LastLoginUserSP.isExistLoginedUser(mContext)) {
            return;
        }
        String nameSpace = packet.getNameSpace();
        if (Const.REQ_IQ_XMLNS_CLIENT_INIT.equals(nameSpace)) {
//			 初始化消息
            L.i("XmppConnService", "解析链接地址数据包" + packet.toXML());
            if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
                // 解析初始化信息并保存
                ClientInitConfig mClientInitConfig = JSON.parseObject(
                        packet.getResp(), ClientInitConfig.class);
                L.d("client init : org_update_type == " + mClientInitConfig.getOrg_update_type());
                mContactOrgDao.saveClientInitInfo(mClientInitConfig);
                configConstantUrl();
                v8TokenManager.initAppTokenData(true);
                //发送服务器广播
                Intent intent = new Intent("com.ynmessager.login_state");
                //TODO 临时获取serverTime
                String server = "";
                for (AddressBean addressBeen : mClientInitConfig.getAddressList()) {
                    if (addressBeen.getKey() == 50) {
                        server = addressBeen.getServer();
                    }
                }
                intent.putExtra("type", LoginStateReceiver.TYPE_SERVER_TIME);
                intent.putExtra("ServiceTime", server);
                sendBroadcast(intent);
                //通知界面检测升级
                L.e(mTag, "xmppconnService  开启检测更新");
                Map<String,String> params = new HashMap<>();
                params.put("access_token",V8TokenManager.sToken);
                OKHttpCustomUtils.get(URLs.GET_SERVICE_LOGIN_CONFIG, params, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject resObj, int id) {
                        final Context context = mContext;
                        try {
                            JSONObject resultObj = resObj.getJSONObject("result");

                            String centerHost = resultObj.getString("centerHostAndPost");
                            String cloudHost = resultObj.getString("cloudHostAndPost");
                            String companyName = resultObj.getString("companyName");
                            String companyCode = resultObj.getString("companycode");
                            String code = resultObj.optString("code");
                            String landSysVersion = resultObj.getString("landSystemVersion");
                            String openFireHost = resultObj.getString("openFireHostAndPost");
                            String serverInfoOpenFireHost = LastLoginUserSP.getServerInfoOpenFireHost(mContext);
                            if (resultObj.optBoolean("cloud", false)) {
                                serverInfoOpenFireHost =  openFireHost;
                            } else {
                                String openFirePort = resultObj.getString("openFirePort");
                                serverInfoOpenFireHost = serverInfoOpenFireHost.split(":")[0]+ ":" + openFirePort;
                                L.i(mTag, "自动登录新生成的openfirehost地址： " + serverInfoOpenFireHost);
                            }
                            LastLoginUserSP.saveServerInfoOpenFireHost(context, serverInfoOpenFireHost);

                            AppController.UPDATE_CHECK_IP = centerHost;//获取计费系统地址
                            //登录优化的时候去除第三个接口，采用第二个接口返回的版本号来作为判断在首页是否提示版本不可用，需要下载等功能 BaseActivity 238行
                            LastLoginUserSP.setYnedutVerion(context, landSysVersion);
                            LastLoginUserSP.saveServerInfoCenterHost(context, centerHost);
                            LastLoginUserSP.saveServerInfoCloudHost(context, cloudHost);
                            LastLoginUserSP.saveServerInfoCompanyName(context, companyName);
                            LastLoginUserSP.saveServerInfoCompanyCode(context, companyCode);
                            LastLoginUserSP.saveServerInfoCode(context, code);
                            LastLoginUserSP.saveServerInfoLandSysVersion(context, landSysVersion);

                        } catch (JSONException e) {
                            L.e(mTag, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        EventBus.getDefault().post(new CommonEvent(Const.IS_AUTO_LOGIN_XMPP, UpdateCheckUtil.getInstance()));
                        loadOffMsgAndUserStatus(false);
                    }

                });

            }

        } else if (Const.REQ_IQ_XMLNS_GET_ORG.equals(nameSpace)) {
            // 获取组织机构
//			L.v("XmppConnService", "org iq xml ->" + packet.getResp());
            if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
                final ContactOrg allContactOrg = JSON.parseObject(packet.getResp(),
                        ContactOrg.class);
                L.e("XmppConnService", "saveAllContactOrg begin");
                L.i("MSG", "接收包：" + packet.getResp());
                mOrgLoaded = mContactOrgDao.saveAllContactOrg(allContactOrg, ORG_UPDATE_TYPE);
                L.e("XmppConnService", "saveAllContactOrg end " + mOrgLoaded);
                //判断加载离线消息
                loadOffMsgAndUserStatus(true);
                L.e("XmppConnService", "getServertime ->" + allContactOrg.getServertime());
                String mServerTime = String.valueOf(allContactOrg.getServertime());
                mContactOrgDao.saveInitServerTime(mServerTime);
                User user =  mContactOrgDao.queryUserInfoByUserNo(LastLoginUserSP.getLoginUserNo(mContext));
                mApplication.mSelfUser = user!=null ? user : new User(LastLoginUserSP.getLoginUserNo(mContext),"",3,"","","","","","",0,0,0);
                mHandler.sendEmptyMessage(CONTACT_DATA_LOADED);
                /***如果是服务器端变更信息，就发送变更信息的广播***/
                if (mPacketId.equals(packet.getPacketID())) {
                    mPacketId = "0";
                    mContext.sendBroadcast(mServiceNoticeIntent);
                }
            }
        } else if (Const.REQ_IQ_XMLNS_GET_STATUS.equals(nameSpace)) {
//			L.v("XmppConnService", "status iq xml ->" + packet.getResp());
            if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
                UserStatus allOnlineUsers = JSON.parseObject(packet.getResp(), UserStatus.class);
                L.e("XmppConnService", "saveAllUserStatus begin");
                mContactOrgDao.saveAllUserStatusData(allOnlineUsers.getStatusList());
                L.e("XmppConnService", "saveAllUserStatus end");
                //更改数据库中我的状态为在线
                mContactOrgDao.updateOneUserStatusByAble(LastLoginUserSP.getLoginUserNo(mContext), Const.USER_ON_LINE_PHONE);
//				initMyInfomation();
            }
        } else if (Const.REQ_IQ_XMLNS_GET_GROUP.equals(nameSpace)) {
            //群组、讨论组
            if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS
                    && ("8".equals(packet.getAction())
                    || "9".equals(packet.getAction()) ||
                    "13".equals(packet.getAction()))) {
                ContactGroupBean mContactGroupBean = JSON.parseObject(
                        packet.getResp(), ContactGroupBean.class);
                if (mContactGroupBean.getGroupType() != 0) {
                    if (mContactGroupBean.getGroupType() == 1) {
                        mContactGroupBean.setGroupType(Const.CONTACT_GROUP_TYPE);//huyi 2016.1.14
                    } else {
                        mContactGroupBean.setGroupType(Const.CONTACT_DISGROUP_TYPE);//huyi 2016.1.14
                    }
                    //修改创建讨论组成员时，用户人数不对的bug huyi 2016.1.14
                    mContactOrgDao.saveOrUpdateContactGroup(mContactGroupBean);//end
                    updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mContactGroupBean.getGroupType());
                    mContext.sendBroadcast(updateViewIntent);
                    if (mContactGroupBean.getRoomList().size() > 0) {
                        createGroupFromP2PIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, mContactGroupBean.getRoomList().get(0));
                        sendBroadcast(createGroupFromP2PIntent);
                    }

                } else {
                    String mAction = packet.getAction();
                    if ("8".equals(mAction)) {
                        mContactGroupBean.setGroupType(Const.CONTACT_GROUP_TYPE);
                        mContactGroupList.add(mContactGroupBean);
                    } else if ("9".equals(mAction)) {
                        mContactGroupBean.setGroupType(Const.CONTACT_DISGROUP_TYPE);
                        mContactGroupList.add(mContactGroupBean);
                    }
                    if (mContactGroupList.size() == 2) {
                        L.e("XmppConnService", "saveAllGroup begin");
                        mGroupLoaded = mContactOrgDao.saveAllGroupDatas(mContactGroupList);
                        L.e("XmppConnService", "saveAllGroup end " + mGroupLoaded);
                        loadOffMsgAndUserStatus(true);
                    }

                }
            }
        } else if (Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST.equals(nameSpace)) {
            if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
                String action = packet.getAction();
                if ("1".equals(action)) {
                    //action 为1表示是离线会话列表
                    if(packet.getResp()!=null) {
                        List<OfflineMsgList> offlineMsgs = JSONArray.parseArray(packet.getResp(), OfflineMsgList.class);
                        if (offlineMsgs.size()>0) {
                            mOffMsgList.addAll(offlineMsgs);
                            //处理消息
                            mHandler.sendEmptyMessage(OFFLINE_MESSAGE_LOADED);
                        }
                    }
                }
            }
        } else if (Const.REQ_IQ_XMLNS_NOTICE.equals(nameSpace)) {
            if ("orgUpdate".equals(packet.getTypeStr())) {
                sendRequestIQPacket(Const.ORG_UPDATE_SOME,
                        Const.REQ_IQ_XMLNS_GET_ORG, false);// 组织机构请求
            }
        }
    }


    /**
     * 初始化配置app链接
     */
    private void configConstantUrl() {
        //当前的登录地址
        String landSysUrl = StringUtils.removeEnd(LastLoginUserSP.getUserLoginServiceUrl(mContext), "/");
        String cloudSysUrl = LastLoginUserSP.getServerInfoCloudHost(mContext);
        String openfireAddr = LastLoginUserSP.getServerInfoOpenFireHost(mContext);
        mApplication.CONFIG_INSIDE_IP = openfireAddr;
        L.d(mTag, "CONFIG_INSIDE_IP -> " + mApplication.CONFIG_INSIDE_IP);

        mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST = mApplication.CONFIG_YNEDUT_V8_URL;
        L.d(mTag, "CONFIG_YNEDUT_V8_SERVICE_HOST -> " + mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST);

        //文件传输
        String _openfireAddr[] = StringUtils.split(openfireAddr, ':');
        String ip = _openfireAddr.length > 0 ? _openfireAddr[0] : StringUtils.EMPTY;
        String port = "80";
        if (landSysUrl.split("/")[2].contains(":")) {
            port = landSysUrl.split("/")[2].split(":")[1];
        }
        mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP = ip + ":" + port;
        L.d(mTag, "CONFIG_INSIDE_FILE_TRANSLATE_IP -> " + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP);
        //调转到V8指定页面
        mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL = landSysUrl + URLs.V8_POSITON_PAGE;
        L.d(mTag, "CONFIG_YNEDUT_V8_APP_PAGE_URL -> " + mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL);
        //应用中心访问V8获取可用菜单
        mApplication.CONFIG_YNEDUT_V8_APP_MENUS_URL = landSysUrl + URLs.APP_CENTER_V8_MENU;
        L.d(mTag, "CONFIG_YNEDUT_V8_APP_MENUS_URL -> " + mApplication.CONFIG_YNEDUT_V8_APP_MENUS_URL);
        // 获取OA待办的地址
        mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL = landSysUrl + URLs.OA_TODO_ANDROID;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_OA_URL -> " + mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL);
        //跳转OA详情的地址,pc端使用的
        mApplication.CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL = landSysUrl + URLs.OA_TO_DETAIL;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL -> "
                + mApplication.CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL);
        mApplication.CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL = landSysUrl + URLs.JUMP_OA_POSITION_PAGE;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL -> "
                + mApplication.CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL);
        mApplication.CONFIG_LBS_URL_CONFIG = landSysUrl + URLs.GPS_GET_RULE;
        LastLoginUserSP.saveGpsRuleUrl(mContext, mApplication.CONFIG_LBS_URL_CONFIG);
        Settings.System.putString(mContext.getContentResolver(), "lastLoginGpsRuleUrl", landSysUrl + URLs.GPS_GET_RULE);
        L.d(mTag, "CONFIG_LBS_URL_CONFIG -> " + mApplication.CONFIG_LBS_URL_CONFIG);
        mApplication.CONFIG_LBS_URL_BATH_UPLOAD = landSysUrl + URLs.GPS_UPLOAD_MULTIPTE;
        LastLoginUserSP.saveBathGpsSubmitUrl(mContext, mApplication.CONFIG_LBS_URL_BATH_UPLOAD);
        Settings.System.putString(mContext.getContentResolver(), "lastLoginBathGpsSubmitUrl", landSysUrl + URLs.GPS_UPLOAD_MULTIPTE);
        L.d(mTag, "CONFIG_LBS_URL_BATH_UPLOAD -> " + mApplication.CONFIG_LBS_URL_BATH_UPLOAD);
    }

    /**
     * 添加所有初始化Iq消息监听
     */
    public void addAllInitIqCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_CLIENT_INIT, XmppConnService.this);
            mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_ORG, XmppConnService.this);
            mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP, XmppConnService.this);
            mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_NOTICE, XmppConnService.this);
        }
    }

    /**
     * 删除所有初始化IQ消息监听
     */
    public void removeAllInitIQCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_CLIENT_INIT);
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_ORG);
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
            mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_NOTICE);
        }
    }

    /**
     * 删除联系人IQ消息监听
     */
    public void removeContactIQCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_ORG);
        }
    }

    /**
     * 删除群组IQ消息监听
     */
    public void removeGroupIQCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
        }
    }

    /**
     * 删除初始化IQ消息监听
     */
    public void removeClientInitIQCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager
                    .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_CLIENT_INIT);
        }
    }


    /**
     * 删除通知IQ消息监听
     */
    public void removeNoticeIQCallback() {
        if (mXmppConnManager != null) {
            mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_NOTICE);
        }
    }


    /**
     * 是否开始加载离线消息
     */
    public void loadOffMsgAndUserStatus(boolean isFirst) {
        if (isFirst) {
            // 如果组织机构 群 讨论组加载完毕了，就开始加载离线消息
            if (mGroupLoaded && mOrgLoaded) {
                resetContactLoadStatus();
                //请求用户状态
                sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_GET_STATUS, true);
                sendRequestIQPacket(Const.GET_OFFLINE_MSG,
                        Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, true);
            }
        } else {
            sendRequestIQPacket(Const.GET_OFFLINE_MSG,
                    Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, true);
        }
    }

    /**
     * 重置联系人、群讨论组信息加载状态
     */
    public void resetContactLoadStatus() {
        mOrgLoaded = false;
        mGroupLoaded = false;
    }

    /**
     * 发送IQ请求
     *
     * @param mGroupName
     * @param mGroupName 讨论组id
     */
    private void sendRequestIQPacket(String mGroupName) {
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP,
                XmppConnService.this);
        ReqIQ iq = new ReqIQ();
        iq.setAction(13);
        iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
        String jsonParam = getParamsJson(mGroupName);
        iq.setParamsJson(jsonParam);
//		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
        iq.setFrom(LastLoginUserSP.getLoginUserNo(mContext) + "@" + mXmppConnManager.getServiceName());
        iq.setTo("admin@" + mXmppConnManager.getServiceName());
        L.i("PresencePacketListenerImpl", "iq request xml ->" + iq.toXML());
        mPacketId = iq.getPacketID();
        mXmppConnManager.sendPacket(iq);
    }

    /**
     * 拼接字符串
     *
     * @return eg： {"groupName":"test"}
     */
    private String getParamsJson(String mGroupName) {
        StringBuilder mBuilder = new StringBuilder();
        mBuilder.append("{\"groupName\":\"");
        mBuilder.append(mGroupName);
        mBuilder.append("\"}");
        return mBuilder.toString();
    }


    public class NetWorkChangedReceiver extends BroadcastReceiver {

        private boolean serviceStarted;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 网络变更
            if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
                L.v(XmppConnService.class, "isNetAvailable :"
                        + NetWorkUtil.isNetworkAvailable(context) + " !serviceStarted == " + (!serviceStarted));
                if (!serviceStarted) {
                    serviceStarted = true;
                    return;
                }
                // 如果网络可用且存在登录帐号就执行init方法，init根据xmpp.isAuth方法判断是否需要重新登录
                if (NetWorkUtil.isNetworkAvailable(context)) {
                    if (LastLoginUserSP.isExistLoginedUser(context)) {
                        // 如果网络正常，但是下线了，重新登陆
                        mLoginedTimes = 0;
                        mHandler.removeCallbacks(mDoInitRunnable);
                        mHandler.post(mDoInitRunnable);
                    }
                } else {
                    //网络不可用，提示消息列表网络错误
                    dispachtUserStatus();
                    //将正在发送的消息置为发送失败 huyi 2016.1.17
                    changeSendingMsgToFailed();
                }
            } else if (action.equals(BROADCAST_ACTION_REFRESH_CONTACT)) {//当点击了联系人界面的刷新按钮时进行刷新
                initContactsInfoData(false);
            }
        }

        /**
         * @param b
         */
        public void isServiceStarted(boolean b) {
            serviceStarted = b;
        }
    }

    /**
     * 将正在发送的消息置为发送失败
     */
    public void changeSendingMsgToFailed() {
        Iterator<Map.Entry<String, Object>> iter = BaseActivity.mChatMsgBeanMap.entrySet().iterator();
        if (!iter.hasNext()) {
            return;
        }
        P2PChatMsgDao mP2pChatMsgDao = new P2PChatMsgDao(mContext);
        GroupChatDao mGroupChatDao = new GroupChatDao(mContext);
        DisGroupChatDao mDisGroupChatDao = new DisGroupChatDao(mContext);
        while (iter.hasNext()) {
            Map.Entry<String, Object> entryObj = iter.next();
            Object msg = entryObj.getValue();
            if (msg instanceof BaseChatMsgEntity) {
                BaseChatMsgEntity baseMsg = (BaseChatMsgEntity) msg;
                if (baseMsg.getIsSuccess() == BaseChatMsgEntity.SEND_ING) {
                    baseMsg.setIsSuccess(BaseChatMsgEntity.SEND_FAILED);
                    MessageBodyEntity body = JSON.parseObject(baseMsg.getMessage(), MessageBodyEntity.class);
                    if (body == null) {
                        return;
                    }
                    switch (body.getMsgType()) {
                        case Const.CHAT_TYPE_P2P:
                            mP2pChatMsgDao.updateMsgSendStatus(baseMsg.getPacketId(), BaseChatMsgEntity.SEND_FAILED);
                            break;
                        case Const.CHAT_TYPE_GROUP:
                            mGroupChatDao.updateMsgSendStatus(baseMsg.getPacketId(), BaseChatMsgEntity.SEND_FAILED);
                            break;
                        case Const.CHAT_TYPE_DIS:
                            mDisGroupChatDao.updateMsgSendStatus(baseMsg.getPacketId(), BaseChatMsgEntity.SEND_FAILED);
                            break;
                        default:
                            break;
                    }
                    EventBus.getDefault().post(msg);
                }
            }
        }
    }

}
