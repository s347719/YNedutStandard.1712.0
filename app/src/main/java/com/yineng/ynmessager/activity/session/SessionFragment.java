package com.yineng.ynmessager.activity.session;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.activity.settings.ConfirmNotifyModeWindow;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.offline.HistoryMsg;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.NoticeEventTb;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.SwipeListViewItem;
import com.yineng.ynmessager.view.SwipeListViewItem.SwipeViewItemOpendListener;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SessionFragment extends BaseFragment implements StatusChangedCallBack, onCancelSearchAnimationListener,
        SwipeViewItemOpendListener {
    private static final String TAG = "SessionFragment";
    private LinearLayout mFrameLinearLayout;
    private LinearLayout mStatusLinearLayout;
    private TextView mStatusText;
    private int mCurrentStats = 0;
    private PullToRefreshListView mPullToRefreshListView;
    private UnreadMsgCountReceiver mMsgCountReceiver;
    private SwipeListViewItem mSwipeListViewItem;
    public SwipeListviewAdapter mSwipeListviewAdapter;
    private RecentChatDao mRecentChatDao;
    private GroupChatDao mGroupChatDao;
    private DisGroupChatDao mDisGroupChatDao;
    private P2PChatMsgDao mP2pChatMsgDao;
    private LinkedList<RecentChat> mRecentChatsList;
    private ContactOrgDao mContactOrgDao;
    private ConfirmNotifyModeWindow mNotifyModeConfirmWin;
    private RelativeLayout mTxt_notify;
    private RelativeLayout mTxt_silence;
    private RelativeLayout mTxt_cancel;
    private ImageView mImg_notifyChecked;
    private ImageView mImg_silenceChecked;
    private ContactGroup mGroupToChangeNotifyMode; // 要改变消息提醒方式的ContactGroup对象

    /**
     * 上下动画滚动的高度
     */
    protected float searchViewY;
    private RelativeLayout mRel_SearchBox;
    /**
     * 自定义搜索框
     */
    private SearchContactEditText mSearchContactEditText;
    /**
     * 显示搜索框动画
     */
    protected final int SHOW_SEARCH_VIEW = 0;
    /**
     * 取消搜索框动画
     */
    protected final int CANCEL_SEARCH_VIEW = 1;
    /**
     * 一页显示的条数
     */
    private final int mPageSize = 80;
    public final int PULL_DOWN_TO_REFRESH = 1000;

    public static final int STOP_REFRESHING = 1001;

    /**
     * 这个字段存在的意义是防止进入APP的时候开启xmppConnservice两次造成登录不成功的现象
     * 也是为了增加在掉线的时候打开屏幕自动上线功能
     */
    private boolean isResume = false;
    //表示是否删除所有会话
    private boolean isDeleteAll = false;
    /**
     * 跳转进入网络设置界面
     */
    protected OnClickListener mSettingNetListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SEARCH_VIEW:
                    mSearchContactEditText.show();
                    mFrameLinearLayout.setY(-searchViewY);
                    break;

                case CANCEL_SEARCH_VIEW:
                    mFrameLinearLayout.setY(0);
                    break;
                case PULL_DOWN_TO_REFRESH:
                    // 下拉刷新
                    if (mCurrentStats == LoginThread.USER_STATUS_CONNECT_OFF || mCurrentStats == LoginThread.USER_STATUS_SERVER_SHUTDOWN
                            || mCurrentStats == LoginThread.USER_STATUS_OFFLINE
                            || mCurrentStats == LoginThread.USER_STATUS_LOGINED_OTHER) {
                        //启动刷新登录功能
                        Intent serviceIntent = new Intent(mParentActivity.getApplicationContext(), XmppConnService.class);
                        getActivity().startService(serviceIntent);
                        L.e(mTag, "下拉刷新判断掉线之后重新登录");
                    } else {
                        if (mPullToRefreshListView.isRefreshing()) {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                    }
                    break;
                case LoginThread.USER_STATUS_ONLINE:
                    mStatusLinearLayout.setVisibility(View.GONE);
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete();
                    }
                    bindAliAcount();
                    // ToastUtil.toastAlerMessageCenter(mParentActivity,"已经上线！",1000);
                    break;

                case LoginThread.USER_STATUS_LOGINED_OTHER:
                    mStatusText.setText(R.string.main_anotherLogin);
                    mStatusLinearLayout.setVisibility(View.VISIBLE);
                    mStatusLinearLayout.setOnClickListener(null);
                    break;
                case LoginThread.USER_STATUS_CONNECT_OFF:
                case LoginThread.USER_STATUS_SERVER_SHUTDOWN:
                    mStatusText.setText(R.string.main_server_shutdown);
                    mStatusLinearLayout.setVisibility(View.VISIBLE);
                    mStatusLinearLayout.setOnClickListener(null);
                    break;

                case LoginThread.USER_STATUS_NETOFF:// 服务器连接断开
                    mStatusText.setText(R.string.main_badNetwork);
                    mStatusLinearLayout.setVisibility(View.VISIBLE);
                    mStatusLinearLayout.setOnClickListener(mSettingNetListener);
                    break;

                case LoginThread.USER_STATUS_OFFLINE:// 下线了
                    mStatusText.setText(R.string.main_alreadyOffline);
                    mStatusLinearLayout.setVisibility(View.VISIBLE);
                    mStatusLinearLayout.setOnClickListener(null);
                    break;
                case STOP_REFRESHING:// 停止刷新
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete();
                    }
                    break;
                case LoginThread.LOGIN_USER_ACCOUNT_OUT:// 用户登录验证过期
                    getActivity().sendBroadcast(new Intent(Const.BROADCAST_ACTION_ID_PAST));
                    break;
                default:
                    L.e("session default--");
                    break;
            }
        }
    };


    /**
     * 绑定阿里推送帐号
     */
    private void bindAliAcount() {
        String userNo = LastLoginUserSP.getLoginUserNo(getActivity()) + "@" + LastLoginUserSP.getServerInfoCode(getActivity());
        L.i(mTag, "阿里帐号：" + userNo);
        PushServiceFactory.getCloudPushService().bindAccount(userNo, new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                //绑定成功
                L.i(mTag, "绑定成功");
            }

            @Override
            public void onFailed(String s, String s1) {
                //绑定失败
                L.i(mTag, "绑定失败" + s + "   " + s1);
            }
        });
    }

    /**
     * 没有聊天记录
     */
    private View mEmptyChatView;

    private class NotifyModeConfirmOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.groupMsgNotifySetting_rel_notify:
                    // 改变提醒方式，更新到数据库
                    mGroupToChangeNotifyMode.setNotifyMode(ContactGroup.NOTIFYMODE_NOTIFY);
                    mContactOrgDao.updateGroupOrDiscuss(mGroupToChangeNotifyMode);
                    // 更新列表UI
                    mSwipeListviewAdapter.notifyDataSetChanged();
                    mNotifyModeConfirmWin.dismiss();
                    break;
                case R.id.groupMsgNotifySetting_rel_silence:
                    // 改变提醒方式，更新到数据库
                    mGroupToChangeNotifyMode.setNotifyMode(ContactGroup.NOTIFYMODE_SILENCE);
                    mContactOrgDao.updateGroupOrDiscuss(mGroupToChangeNotifyMode);
                    // 更新列表UI
                    mSwipeListviewAdapter.notifyDataSetChanged();
                    mNotifyModeConfirmWin.dismiss();
                    break;
                case R.id.groupMsgNotifySetting_rel_cancel:
                    mGroupToChangeNotifyMode = null;
                    mNotifyModeConfirmWin.dismiss();
                    break;
            }
        }

    }

    private static SessionFragment sInstance = null;

    public static SessionFragment getInstance() {
        return sInstance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sInstance = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_session_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        registerUnreadMsgCountReceiver();
        registerRefreshListBroadcastReceiver();
    }

    /**
     * 当网络改变的时候改变顶部提示x
     *
     * @param show
     */
    public void showUserNetOff(boolean show) {
        if (show) {
            mHandler.sendEmptyMessage(LoginThread.USER_STATUS_NETOFF);
            mCurrentStats = LoginThread.USER_STATUS_CONNECT_OFF;
        } else {
            if (mCurrentStats == LoginThread.USER_STATUS_CONNECT_OFF || mCurrentStats == LoginThread.USER_STATUS_SERVER_SHUTDOWN
                    || mCurrentStats == LoginThread.USER_STATUS_OFFLINE
                    || mCurrentStats == LoginThread.USER_STATUS_LOGINED_OTHER) {
                //启动刷新登录功能
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent serviceIntent = new Intent(mParentActivity.getApplicationContext(), XmppConnService.class);
                        getActivity().startService(serviceIntent);
                    }
                }, 2000);
                L.e(mTag, "网络变换的时候判断掉线之后重新登录");
            } else {
                //防止马上更新桌面显示效果之后又接收到xmpp内部发送过来的状态
                mHandler.sendEmptyMessageDelayed(LoginThread.USER_STATUS_ONLINE, 2000);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        loadRecentChatByPage(0);
    }

    @Override
    public void onStop() {
        super.onStop();
        destoryGifMemory(null, -1);
    }

    /**
     * 当用户在锁屏的情况下进程被杀死
     * 重新进入的时候判断是否是  服务关闭或者状态下线或者xmpp链接断开 10 2 11
     * 重新开始xmpp服务
     */
    @Override
    public void onResume() {
        super.onResume();
        int status = XmppConnectionManager.getInstance().getUserCurrentStatus();
        L.i(mTag, "sessionFragment 再次进入的时候XmppConn当前状态" + status + " ");
        if ((status == LoginThread.USER_STATUS_SERVER_SHUTDOWN || status == LoginThread.USER_STATUS_OFFLINE || status == LoginThread.USER_STATUS_CONNECT_OFF) && isResume) {
//            启动刷新登录功能
            Intent serviceIntent = new Intent(mParentActivity.getApplicationContext(), XmppConnService.class);
            getActivity().startService(serviceIntent);
            L.e(mTag, "重新进入首页的时候判断掉线之后重新登录");
        }
        isResume = true;
    }

    private void initViews(View view) {
        mRecentChatsList = new LinkedList<RecentChat>();
        mRecentChatDao = new RecentChatDao(mParentActivity.getApplicationContext());
        mGroupChatDao = new GroupChatDao(mParentActivity.getApplicationContext());
        mDisGroupChatDao = new DisGroupChatDao(mParentActivity.getApplicationContext());
        mP2pChatMsgDao = new P2PChatMsgDao(mParentActivity.getApplicationContext());
        mContactOrgDao = new ContactOrgDao(mParentActivity.getApplicationContext());
        mSwipeListviewAdapter = new SwipeListviewAdapter(this, mParentActivity, mRecentChatsList);
        mStatusLinearLayout = (LinearLayout) view.findViewById(R.id.ll_main_session_alertlayer);
        mFrameLinearLayout = (LinearLayout) view.findViewById(R.id.ll_main_session_frame);
        mStatusText = (TextView) view.findViewById(R.id.tv_main_session_alert_text);
        mPullToRefreshListView = (PullToRefreshListView) view.findViewById(R.id.prlv_main_session_refresh);
        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mPullToRefreshListView.setAdapter(mSwipeListviewAdapter);
        View header = LayoutInflater.from(getActivity()).inflate(R.layout.layout_session_header, null);
        mRel_SearchBox = (RelativeLayout) header.findViewById(R.id.searchBox);
        mPullToRefreshListView.getRefreshableView().addHeaderView(header);
        mSearchContactEditText = new SearchContactEditText(this.mParentActivity);
        mSearchContactEditText.setSessionFragment(true, true, false);
        mRel_SearchBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View paramView) {
                showSearchContactAnimation();
            }
        });
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (mCurrentStats == LoginThread.USER_STATUS_CONNECT_OFF || mCurrentStats == LoginThread.USER_STATUS_SERVER_SHUTDOWN
                        || mCurrentStats == LoginThread.USER_STATUS_OFFLINE
                        || mCurrentStats == LoginThread.USER_STATUS_LOGINED_OTHER) {
                    mHandler.sendEmptyMessageDelayed(PULL_DOWN_TO_REFRESH, 500);
                } else {
                    mHandler.sendEmptyMessageDelayed(PULL_DOWN_TO_REFRESH, 1000);
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            }
        });
        XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
        mSearchContactEditText.setOnCancelSearchAnimationListener(this);

        mEmptyChatView = view.findViewById(R.id.ll_main_session_empty);
        mNotifyModeConfirmWin = new ConfirmNotifyModeWindow(mParentActivity);
        View popwinView = mNotifyModeConfirmWin.getContentView();
        mTxt_notify = (RelativeLayout) popwinView.findViewById(R.id.groupMsgNotifySetting_rel_notify);
        mTxt_silence = (RelativeLayout) popwinView.findViewById(R.id.groupMsgNotifySetting_rel_silence);
        mTxt_cancel = (RelativeLayout) popwinView.findViewById(R.id.groupMsgNotifySetting_rel_cancel);
        mImg_notifyChecked = (ImageView) popwinView.findViewById(R.id.groupMsgNotifySetting_img_notifyChecked);
        mImg_silenceChecked = (ImageView) popwinView.findViewById(R.id.groupMsgNotifySetting_img_silenceChecked);
        NotifyModeConfirmOnClickListener popwinOnClickListener = new NotifyModeConfirmOnClickListener();
        mTxt_notify.setOnClickListener(popwinOnClickListener);
        mTxt_silence.setOnClickListener(popwinOnClickListener);
        mTxt_cancel.setOnClickListener(popwinOnClickListener);
    }

    TranslateAnimation mShowAnimation = null;
    TranslateAnimation mCancelAnimation = null;
    private AnimationListener mShowAnimationListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (isShowSearchEditText) {
                mHandler.sendEmptyMessage(SHOW_SEARCH_VIEW);
            } else {
                mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
            }
        }
    };

    private boolean isShowSearchEditText = false;

    public void showSearchContactAnimation() {
        isShowSearchEditText = true;
        LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams) mRel_SearchBox.getLayoutParams();
        searchViewY = mRel_SearchBox.getY() - (float) etParamTest.topMargin;
        mShowAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
        mShowAnimation.setDuration(200);
        mShowAnimation.setAnimationListener(mShowAnimationListener);
        mFrameLinearLayout.startAnimation(mShowAnimation);
    }

    @Override
    public void cancelSearchContactAnimation() {
        isShowSearchEditText = false;
        mSearchContactEditText.dismiss();
        mCancelAnimation = new TranslateAnimation(0, 0, 0, searchViewY);
        mCancelAnimation.setDuration(200);
        mCancelAnimation.setAnimationListener(mShowAnimationListener);
        mFrameLinearLayout.startAnimation(mCancelAnimation);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XmppConnectionManager.getInstance().removeStatusChangedCallBack(this);
        unRegisterUnreadMsgCountReceiver();
        unregisterRefreshListBroadcaseReceiver();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadRecentChatByPage(int pages) {
        new AsyncTask<String, Integer, List<RecentChat>>() {
            private int mPageIndex = 0;

            // 访问数据库前执行
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected List<RecentChat> doInBackground(String... params) {
                mPageIndex = Integer.valueOf(params[0]);
                return mRecentChatDao.queryRecentChatPage(mPageIndex, mPageSize);
            }

            // 返回数据
            @Override
            protected void onPostExecute(List<RecentChat> list) {
                if (mPageIndex == 0) {
                    mRecentChatsList.clear();
                    createPlatformNoticeListObj();
                    mRecentChatsList.addAll(list);
                } else {
                    for (RecentChat chat : list) {
                        if (!mRecentChatsList.contains(chat)) {
                            mRecentChatsList.addLast(chat);
                        }
                    }
                }
                mSwipeListviewAdapter.notifyDataSetChanged();
                mPullToRefreshListView.onRefreshComplete();
                Log.e(TAG, "mRecentChatsList.size == " + mRecentChatsList.size());
                if (mRecentChatsList.size() == 0) {
                    mPullToRefreshListView.setEmptyView(mEmptyChatView);
                }
            }
        }.execute(String.valueOf(pages));
    }

    /**
     * 创建列表中的平台通知对象
     */
    public RecentChat createPlatformNoticeListObj() {
        RecentChat noticeRecentChat = null;
        try {
            NoticeEventTb mNoticeEventTb = new NoticeEventTb(getActivity());
            noticeRecentChat = new RecentChat();
            noticeRecentChat.setUserNo("-1");
            noticeRecentChat.setIsTop(2);
            noticeRecentChat.setTitle(getString(R.string.session_platform_notice_title));
            noticeRecentChat.setUserNo(LastLoginUserSP.getInstance(getActivity()).getUserAccount());
            noticeRecentChat.setChatType(Const.CHAT_TYPE_PLATFORM_NOTICE);
            //统计通知总数
            //获取第一条通知，用于在消息列表中进行展示
            NoticeEvent firstNotice = mNoticeEventTb.queryFirstNotice();
            if (firstNotice != null) {
                String timeStr = DateFormatUtils.format(firstNotice.getTimeStamp(), TimeUtil.FORMAT_DATETIME_24);
                L.e("时间 == " + timeStr);
                noticeRecentChat.setDateTime(timeStr);
                String titleStr = "";
                switch (firstNotice.getMessageType()) {
                    case NoticeEvent.SYSTEM_MSG:
                        titleStr = "【系统消息】" + firstNotice.getTitle();
                        break;
                    case NoticeEvent.NOTICE_MSG:
                        titleStr = "【通知公告】" + firstNotice.getTitle();
                        break;
                    default:
                        titleStr = firstNotice.getTitle();
                        break;
                }
                noticeRecentChat.setContent(titleStr);
            } else {
                noticeRecentChat.setDateTime("");
                noticeRecentChat.setContent(getString(R.string.platform_no_notice));
            }
            //统计未读通知数量
            noticeRecentChat.setUnReadCount(mApplication.UnReedNoticeNum);
            if (mRecentChatsList.size() > 0) {
                mRecentChatsList.removeFirst();
            }
            mRecentChatsList.addFirst(noticeRecentChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return noticeRecentChat;
    }

    /**
     * 注册显示未读消息的接收者
     *
     * @Title: registerUnreadNoticeCountReceiver
     * @Description: 方法描述
     */
    private void registerUnreadMsgCountReceiver() {
        if (mMsgCountReceiver == null) {
            mMsgCountReceiver = new UnreadMsgCountReceiver();
        }
        // 注册广播
        IntentFilter ifilter = new IntentFilter();
        ifilter.addAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        this.mParentActivity.registerReceiver(mMsgCountReceiver, ifilter);
    }

    private void unRegisterUnreadMsgCountReceiver() {
        if (mMsgCountReceiver != null) {
            this.mParentActivity.unregisterReceiver(mMsgCountReceiver);
        }
    }

    private class UnreadMsgCountReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Const.ACTION_UPDATE_UNREAD_COUNT)) {
                String account = intent.getStringExtra("userno");
                int type = intent.getIntExtra("chattype", Const.CHAT_TYPE_P2P);
                destoryGifMemory(account, type);
                RecentChat recentChat = mRecentChatDao.isChatExist(account, type);
                if (recentChat != null) {
                    if (mRecentChatsList.contains(recentChat)) {
                        mRecentChatsList.remove(recentChat);
                    }
                    sortRecentChatList(mRecentChatsList, recentChat);
                } else {
                    //当接收到平台通知时，type为Const.CHAT_TYPE_EVENT，account为Const.BROADCAST_ID
                    // 要获取第一条平台通知用于刷新最近会话列表的界面 huyi 2016.1.12
                    if (type == Const.CHAT_TYPE_EVENT) {
                        createPlatformNoticeListObj();
                    } else {
                        // 退出讨论组时已经删掉了最近会话的该讨论组的聊天记录
                        for (RecentChat tempRecentChat : mRecentChatsList) {
                            //不能是广播
                            if (tempRecentChat.getUserNo().equals(account) && tempRecentChat.getUserNo() != Const.BROADCAST_ID) {
                                mRecentChatsList.remove(tempRecentChat);
                                break;
                            }
                        }
                    }
                }
                mSwipeListviewAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 注册广播接收器 设置-辅助功能中的清空会话消息列表
     */
    private void registerRefreshListBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.BROADCAST_ACTION_CLEAR_SESSION_LIST);
        filter.addAction(Const.BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG);
        LocalBroadcastManager.getInstance(getContext())
                .registerReceiver(mRefreshListBroadcastReceiver, filter);
    }

    /**
     * 反注册广播接收器 设置-辅助功能中的清空会话消息列表
     */
    private void unregisterRefreshListBroadcaseReceiver() {
        LocalBroadcastManager.getInstance(getContext())
                .unregisterReceiver(mRefreshListBroadcastReceiver);
    }

    /**
     * 广播接收器，接收处理 设置 - 辅助功能中的清空会话消息列表
     */
    private BroadcastReceiver mRefreshListBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            destoryGifMemory(null, -1);
            if (Const.BROADCAST_ACTION_CLEAR_SESSION_LIST.equals(action)) // 清空会话列表
            {
                showProgressDialog("清除中...");
                isDeleteAll = true;
                mSwipeListviewAdapter.sendDeleteIQ("all", 0);
                loadRecentChatByPage(0); // 再加载数据库到UI
                ToastUtil.toastAlerMessageCenter(SessionFragment.this.mParentActivity,
                        getString(R.string.session_clearListDone), 2000);
            }
        }
    };

    @Override
    public void onStatusChanged(int status) {
        L.e(" mCurrentStats == " + mCurrentStats + " status == " + status);
        if (status == STOP_REFRESHING) {
            mHandler.sendEmptyMessage(status);
            return;
        }
        if (mCurrentStats != status) {
            mHandler.sendEmptyMessage(status);
            mCurrentStats = status;
        }
    }

    @Override
    public void onSwipeViewItemOpend(SwipeListViewItem item) {
        if (mSwipeListViewItem != null && mSwipeListViewItem != item && mSwipeListViewItem.isOpen()) {
            mSwipeListViewItem.smoothScrollTo(0, 0);
        }
        mSwipeListViewItem = item;
    }

    /**
     * 会话列表排序方法
     *
     * @param list
     * @param recentChat
     */
    public static void sortRecentChatList(LinkedList<RecentChat> list, RecentChat recentChat) {
        int count = list.size();
        int maxIndex = count - 1;
        if (count > 0) {
            if (recentChat.getIsTop() == 1) {
                /**由于把通知修改倒主页消息列表的平台通知中去，
                 * 且平台通知始终固定在第一行显示,所以注释掉之前的置顶代码，huyi 2015.1.8**/
                list.add(1, recentChat);
//				list.addFirst(recentChat);
            } else {
                for (int i = 0; i < count; i++) {
                    if (list.get(i).getIsTop() == 0
                            && TimeUtil.getMillisecondByDate(list.get(i).getDateTime(), TimeUtil.FORMAT_DATETIME_24) <= TimeUtil
                            .getMillisecondByDate(recentChat.getDateTime(), TimeUtil.FORMAT_DATETIME_24)) {
                        list.add(i, recentChat);// 插入到i位置
                        break;
                    }
                    if (i == maxIndex) {
                        // 插入到末尾
                        list.addLast(recentChat);
                    }
                }
            }
        } else {
            list.add(recentChat);
        }
    }

    public class SwipeListviewAdapter extends BaseAdapter implements ReceiveReqIQCallBack {
        private LinkedList<RecentChat> mSessionDatas;
        private LayoutInflater mInflater;
        private int mScreenWidth;
        private Context mContext;
        private SwipeViewItemOpendListener mOpendListener;
        private SimpleDateFormat mDateFormat = new SimpleDateFormat(TimeUtil.FORMAT_DATETIME_24);
        private int currentPos = 0;

        /**
         * XMPP连接管理类实例
         */
        protected XmppConnectionManager mXmppConnManager;

        public SwipeListviewAdapter(SwipeViewItemOpendListener listener, Context context,
                                    LinkedList<RecentChat> sessionDatas) {
            this.mSessionDatas = sessionDatas;
            mInflater = LayoutInflater.from(context);
            mScreenWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
            mContext = context;
            mOpendListener = listener;
            mXmppConnManager = XmppConnectionManager.getInstance();
        }

        public LinkedList<RecentChat> getmSessionDatas() {
            return mSessionDatas;
        }

        public void setmSessionDatas(LinkedList<RecentChat> mSessionDatas) {
            this.mSessionDatas = mSessionDatas;
        }

        @Override
        public int getCount() {
            return mSessionDatas != null ? mSessionDatas.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return mSessionDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            final RecentChat recentChat = mSessionDatas.get(position);
            // 如果是群、讨论组消息，则先从数据库查询出这个对象
            ContactGroup contactGroup = null;
            final int chatType = recentChat.getChatType();
            if (chatType == Const.CHAT_TYPE_GROUP || chatType == Const.CHAT_TYPE_DIS) {
                String groupName = recentChat.getUserNo();
                contactGroup = mContactOrgDao.queryGroupOrDiscussByGroupName(groupName);
            }

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item_main_session_swipelist, parent, false);

                viewHolder.mSessionFront = (RelativeLayout) convertView.findViewById(R.id.ll_main_session_front);
                viewHolder.mSessionPhoto = (CircleImageView) convertView.findViewById(R.id.iv_main_session_item_headicon);
                viewHolder.mSessionDateTime = (TextView) convertView.findViewById(R.id.tv_main_session_item_datetime);
                viewHolder.mSessionContent = (TextView) convertView.findViewById(R.id.tv_main_session_item_content);
                viewHolder.mSessionUnreadCoun = (TextView) convertView
                        .findViewById(R.id.tv_main_session_item_unreadcount);
                viewHolder.mSessionTitle = (TextView) convertView.findViewById(R.id.tv_main_session_item_title);
                viewHolder.mSessionSetLayout = (LinearLayout) convertView.findViewById(R.id.ll_session_back);
                viewHolder.mSessionSetTopButton = (RelativeLayout) convertView.findViewById(R.id.bt_session_item_settop);
                viewHolder.mSessionTop = (ImageView) convertView.findViewById(R.id.iv_main_session_item_top);
                viewHolder.mSessionAlert = (RelativeLayout) convertView.findViewById(R.id.bt_session_item_alert);
                viewHolder.item_black = convertView.findViewById(R.id.item_black);
                viewHolder.item_session_online_type = (ImageView) convertView.findViewById(R.id.item_session_online_type);
                viewHolder.item_contanct_isline = (CircleImageView) convertView.findViewById(R.id.item_contanct_isline);
                // 可移到item的mesure和layout中处理
                // viewHolder.mSessionBack.getLayoutParams().width =
                // mScreenWidth;
                viewHolder.mSessionFront.getLayoutParams().width = mScreenWidth;

                viewHolder.mSessionDeleteButton = (RelativeLayout) convertView.findViewById(R.id.bt_session_item_delete);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final SwipeListViewItem swipeListViewItem = (SwipeListViewItem) convertView;
            swipeListViewItem.setRecentChat(recentChat);
            swipeListViewItem.setItemOpendListener(mOpendListener);
            /**由于把通知修改倒主页消息列表的平台通知中去，
             * 且平台通知始终固定在第一行显示,所以去掉第一行左滑显示操作界面的代码，huyi 2015.1.8**/
            if (position == 0) {
                viewHolder.mSessionSetLayout.setVisibility(View.GONE);
                viewHolder.item_black.setVisibility(View.VISIBLE);
                // 获取会话中最后一次消息的聊天时间
                if (!TextUtils.isEmpty(recentChat.getDateTime())) {
                    String relative = TimeUtil.getDateByDateStr(recentChat.getDateTime(), TimeUtil.FORMAT_DATETIME_MM_DD);
                    viewHolder.mSessionDateTime.setText(relative);
                } else {
                    viewHolder.mSessionDateTime.setText("");
                }
            } else {
                viewHolder.mSessionSetLayout.setVisibility(View.VISIBLE);
                viewHolder.item_black.setVisibility(View.GONE);
                // 获取会话中最后一次消息的聊天时间
                if (!TextUtils.isEmpty(recentChat.getDateTime())) {
                    Date date = convertStringDate(recentChat.getDateTime());
                    // 转换成与当前时间的关系文字
                    String relative = TimeUtil.getTimeRelationFromNow2(mParentActivity.getApplicationContext(), date);
                    viewHolder.mSessionDateTime.setText(relative);
                } else {
                    viewHolder.mSessionDateTime.setText("");
                }
            }
            User user = mContactOrgDao.queryUserInfoByUserNo(recentChat.getUserNo());
            if (user != null)
                viewHolder.mSessionDeleteButton.setVisibility(View.VISIBLE);
            {
                // 设置显示的头像
                int avatar = -1;
                switch (chatType) {
                    case Const.CHAT_TYPE_P2P:
                        File userIcon = FileUtil.getAvatarByName(recentChat.getUserNo());
                        if(user != null && userIcon.exists()){
                            if( user.getUserStatus()==2){
                                viewHolder.mSessionPhoto.setImageURI(Uri.fromFile(userIcon));
                                viewHolder.item_session_online_type.setVisibility(View.VISIBLE);
                            }else if (user.getUserStatus()==0){
                                viewHolder.mSessionPhoto.setImageBitmap(ImageUtil.convertToBlackWhite(BitmapFactory.decodeFile(userIcon.getAbsolutePath())));
                                viewHolder.item_session_online_type.setVisibility(View.GONE);
                            }else {
                                viewHolder.mSessionPhoto.setImageURI(Uri.fromFile(userIcon));
                                viewHolder.item_session_online_type.setVisibility(View.GONE);
                            }
                        }else {
                            if (user != null && user.getGender() == 1) {
                                //对方是男性
                                if (user.getUserStatus() == 0) {
                                    //离线
                                    avatar = R.mipmap.icon_man_upline;
                                    viewHolder.item_session_online_type.setVisibility(View.GONE);
                                } else {//在线
                                    avatar = R.mipmap.session_p2p_men;
                                    if (user.getUserStatus() == 2) {//单聊判断对方是什么形式在线
                                        viewHolder.item_session_online_type.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                                    }
                                }
                            } else if (user != null && user.getGender() == 2) {//对方是女性
                                if (user.getUserStatus() == 0) {//离线
                                    avatar = R.mipmap.icon_women_upline;
                                    viewHolder.item_session_online_type.setVisibility(View.GONE);
                                } else {//在线
                                    avatar = R.mipmap.session_p2p_woman;
                                    if (user.getUserStatus() == 2) {//单聊判断对方是什么形式在线
                                        viewHolder.item_session_online_type.setVisibility(View.VISIBLE);
                                    } else {
                                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                if (user != null && user.getUserStatus() == 0) {
                                    avatar = R.mipmap.session_no_sex_unline;
                                    viewHolder.item_session_online_type.setVisibility(View.GONE);
                                } else {
                                    if (user != null && user.getUserStatus() == 2) {//单聊判断对方是什么形式在线
                                        avatar = R.mipmap.session_no_sex;
                                        viewHolder.item_session_online_type.setVisibility(View.VISIBLE);
                                    } else {
                                        avatar = R.mipmap.session_no_sex;
                                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                                    }
                                }
                            }
                            viewHolder.mSessionPhoto.setImageResource(avatar);
                        }
                        break;
                    case Const.CHAT_TYPE_GROUP://群组
                        avatar = R.mipmap.session_group;
                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                        viewHolder.item_contanct_isline.setVisibility(View.GONE);
                        break;
                    case Const.CHAT_TYPE_DIS://讨论组
                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                        viewHolder.item_contanct_isline.setVisibility(View.GONE);
                        ContactGroupUser group = mContactOrgDao.getContactGroupUserById(recentChat.getUserNo(), LastLoginUserSP.getLoginUserNo(mContext), Const.CHAT_TYPE_DIS);
                        if (group != null && group.getRole() == 10) {//自己创建的讨论组
                            avatar = R.mipmap.session_creat_discus;
                        } else {//自己加入的讨论组
                            avatar = R.mipmap.session_join_discus;
                        }
                        break;
                    case Const.CHAT_TYPE_BROADCAST:
                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                        viewHolder.item_contanct_isline.setVisibility(View.GONE);
                        viewHolder.mSessionDeleteButton.setVisibility(View.GONE);
                        avatar = R.mipmap.session_broadcast;
                        break;
                    case Const.CHAT_TYPE_PLATFORM_NOTICE:
                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                        viewHolder.item_contanct_isline.setVisibility(View.GONE);
                        avatar = R.mipmap.session_platform_notic;
                        break;
                    default:
                        viewHolder.item_session_online_type.setVisibility(View.GONE);
                        viewHolder.item_contanct_isline.setVisibility(View.GONE);
                        avatar = R.mipmap.session_mail; // 目前未知的会话类型设置的是一个蓝色邮件的图标，仅供目前加以区分其他的会话类型
                        break;
                }
                if (chatType != Const.CHAT_TYPE_P2P) {
                    viewHolder.mSessionPhoto.setImageResource(avatar);
                }
            }
            // 设置显示草稿还是消息内容
            String draft = recentChat.getDraft();
            String mContentStr;
            if (TextUtils.isEmpty(draft)) // 如果没有草稿
            {
                if ((recentChat.getChatType() == Const.CHAT_TYPE_DIS || recentChat.getChatType() == Const.CHAT_TYPE_GROUP) && recentChat.getSenderName() != null) {
                    if (recentChat.getSenderName().equals(mApplication.mSelfUser.getUserName())) {
                        mContentStr = "我" + ": " + recentChat.getContent();
                    } else {
                        mContentStr = recentChat.getSenderName() + ": " + recentChat.getContent();
                    }
                } else {
                    mContentStr = recentChat.getContent();
                }
            } else
            // 如果有草稿
            {
                mContentStr = RecentChat.DRAFT_PREFIX + draft;
            }

            // 添加表情
            SpannableString spannableString;
            if (recentChat.getSpannableString() != null) {
                spannableString = recentChat.getSpannableString();
            } else {
                // 对内容做处理
                spannableString = FaceConversionUtil.getInstace().handlerRecentChatContent(mContext, viewHolder.mSessionContent,
                        mContentStr);
                recentChat.setSpannableString(spannableString);
            }
            viewHolder.mSessionContent.setText(spannableString);

            int unreadCount = recentChat.getUnReadCount();
            if (unreadCount > 0) {
                if (unreadCount > 99) {
                    viewHolder.mSessionUnreadCoun.setText("99+");
                } else {
                    viewHolder.mSessionUnreadCoun.setText(String.valueOf(unreadCount));
                }
                viewHolder.mSessionUnreadCoun.setVisibility(View.VISIBLE);
            } else {
                viewHolder.mSessionUnreadCoun.setVisibility(View.GONE);
            }

            viewHolder.mSessionTitle.setText(recentChat.getTitle());

            viewHolder.mSessionSetTopButton.setTag(position);
            viewHolder.mSessionDeleteButton.setTag(position);
            if (recentChat.getIsTop() == 1) {
                viewHolder.mSessionTop.setVisibility(View.VISIBLE);
                viewHolder.mSessionFront.setBackgroundResource(R.color.common_bg);
            } else {
                viewHolder.mSessionTop.setVisibility(View.INVISIBLE);
                viewHolder.mSessionFront.setBackgroundResource(android.R.color.transparent);
            }

            viewHolder.mSessionDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 在这里根据位置获取itemview，空指针异常,对于已有的没有问题，对于新拉动出来的会有问题,可与onitemclicked结合
                    // 参考swipelistview，在项view或者listview中添加消除动画以及恢复试图
                    final int pos = (Integer) v.getTag();
                    final SwipeListViewItem swipeListViewItem = (SwipeListViewItem) v.getParent().getParent()
                            .getParent().getParent();

                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.session_slide_out);
                    animation.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            showProgressDialog("删除中...");
                            currentPos = pos;
                            isDeleteAll = false;
                            //发送删除iq
                            int type = Const.CHAT_TYPE_P2P;
                            if (mSessionDatas.get(pos).getChatType() == Const.CHAT_TYPE_GROUP || mSessionDatas.get(pos).getChatType() == Const.CHAT_TYPE_DIS) {
                                type = Const.CHAT_TYPE_GROUP;
                            }
                            sendDeleteIQ(mSessionDatas.get(pos).getUserNo(), type);
                            swipeListViewItem.scrollTo(0, 0);
                        }
                    });
                    swipeListViewItem.startAnimation(animation);
                }
            });

            viewHolder.mSessionSetTopButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int pos = (Integer) v.getTag();
                    RecentChat recentChat = mSessionDatas.get(pos);
                    mSessionDatas.remove(pos);
                    if (recentChat.getIsTop() == 1) {// 取消置顶
                        recentChat.setIsTop(0);
                        mRecentChatDao.updateIsTop(recentChat.getId(), 0);
                        SessionFragment.sortRecentChatList(mSessionDatas, recentChat);
                    } else {// 置顶
                        recentChat.setIsTop(1);
                        /**由于把通知修改倒主页消息列表的平台通知中去，
                         * 且平台通知始终固定在第一行显示,所以注释掉之前的置顶代码，huyi 2015.1.8**/
                        mSessionDatas.add(1, recentChat);
//						mSessionDatas.addFirst(recentChat);
                        mRecentChatDao.updateIsTop(recentChat.getId(), 1);
                    }

                    final SwipeListViewItem swipeListViewItem = (SwipeListViewItem) v.getParent().getParent().getParent().getParent();
                    swipeListViewItem.scrollTo(0, 0);
                    notifyDataSetChanged();
                }
            });
            // 群、讨论组的话，设置 “提醒” 按钮
            if (contactGroup != null) {
                // 显示提醒按钮
                viewHolder.mSessionAlert.setVisibility(View.VISIBLE);
                // 更新到数据库
                final ContactGroup _contactGroup = contactGroup;
                viewHolder.mSessionAlert.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mGroupToChangeNotifyMode = _contactGroup;
                        // 显示PopupWindow
                        int notifyMode = _contactGroup.getNotifyMode();
                        if (notifyMode == ContactGroup.NOTIFYMODE_NOTIFY) {
                            mImg_notifyChecked.setVisibility(View.VISIBLE);
                            mImg_silenceChecked.setVisibility(View.INVISIBLE);
                        } else if (notifyMode == ContactGroup.NOTIFYMODE_SILENCE) {
                            mImg_notifyChecked.setVisibility(View.INVISIBLE);
                            mImg_silenceChecked.setVisibility(View.VISIBLE);
                        }
                        mNotifyModeConfirmWin.showAtLocation(mFrameLinearLayout, Gravity.BOTTOM, 0, 0);
                    }
                });
            } else {
                // 个人会话则隐藏提醒按钮
                viewHolder.mSessionAlert.setVisibility(View.GONE);
            }

            // 在被更新之后自动关闭有有划开的界面
            if (convertView.getScrollX() > 0) {
                convertView.scrollTo(0, 0);
            }
            return convertView;
        }

        /**
         * 将日期时间字符串转换为Date对象
         *
         * @param date 要转换的日期时间字符串
         * @return 想要获得的Date对象
         */
        private Date convertStringDate(String date) {
            Date d = null;
            // 2015-05-08 09:56:58
            try {
                d = mDateFormat.parse(date);
            } catch (ParseException e) {
                L.e(TAG, e.getMessage(), e);
            }
            return d;
        }

        private final class ViewHolder {
            RelativeLayout mSessionFront;
            CircleImageView mSessionPhoto;
            ImageView mSessionTop;
            TextView mSessionTitle;
            TextView mSessionContent;
            TextView mSessionDateTime;
            TextView mSessionUnreadCoun;
            RelativeLayout mSessionDeleteButton;
            RelativeLayout mSessionSetTopButton;
            RelativeLayout mSessionAlert;
            public LinearLayout mSessionSetLayout;
            View item_black;
            ImageView item_session_online_type;
            CircleImageView item_contanct_isline;
        }

        /**
         * 发送删除会话iq
         */
        private void sendDeleteIQ(String chatId, int type) {
            ReqIQ iq = new ReqIQ();
            iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getInstance(getActivity()).getUserAccount()));
            iq.setTo("admin@" + mXmppConnManager.getServiceName());
            iq.setAction(4);
            iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
            HistoryMsg historyMsg = new HistoryMsg();
            historyMsg.setChatType(type);
            historyMsg.setChatId(chatId);
            String paramsJsonStr = JSON.toJSONString(historyMsg);
            iq.setParamsJson(paramsJsonStr);
            if (NetWorkUtil.isNetworkAvailable(getActivity())) {
                if (!mXmppConnManager.sendPacket(iq)) ;
                {
                    hideProgressDialog();
                }
                mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, this);
            }
        }

        /**
         * 接收删除回执
         *
         * @param packet
         */
        @Override
        public void receivedReqIQResult(ReqIQResult packet) {
            if (packet.getCode() == 200) {
                hideProgressDialog();
                if (!isDeleteAll) {
                    mRecentChatDao.deleteRecentChatById(mSessionDatas.get(currentPos).getId());
                    RecentChat deletedChat = mSessionDatas.remove(currentPos);
                    // 释放gif内存
                    releaseSpanMemory(deletedChat);
                } else {
                    mRecentChatDao.deleteAllNotNotifiy(); // 先清空整个表除了广播
                    loadRecentChatByPage(0); // 再加载数据库到UI
                }
                Intent intent = new Intent();
                intent.setAction(URLs.ACTION_UPDATE_UNREAD_COUNT);
                mContext.sendBroadcast(intent);
            }
        }
    }

    /**
     * 销毁gif内存
     */
    public void destoryGifMemory(String mAccount, int mType) {
        LinkedList<RecentChat> tempRecentChatsList = mSwipeListviewAdapter.getmSessionDatas();
        if (mAccount == null && mType == -1) {
            for (RecentChat recentChat : tempRecentChatsList) {
                releaseSpanMemory(recentChat);
            }
        } else {
            for (RecentChat recentChat : tempRecentChatsList) {
                if (recentChat.getUserNo().equals(mAccount) && mType == recentChat.getChatType()) {
                    releaseSpanMemory(recentChat);
                }
            }
        }
        System.gc();
    }

    public void releaseSpanMemory(RecentChat recentChat) {
        SpannableString tempSpan = recentChat.getSpannableString();
        if (tempSpan != null) {
            AnimatedImageSpan[] tem = tempSpan.getSpans(0, tempSpan.length() - 1, AnimatedImageSpan.class);
            for (AnimatedImageSpan animatedImageSpan : tem) {
                animatedImageSpan.recycleBitmaps(true);
//				animatedImageSpan = null;
            }
            tempSpan.removeSpan(tem);
        }
    }
}
