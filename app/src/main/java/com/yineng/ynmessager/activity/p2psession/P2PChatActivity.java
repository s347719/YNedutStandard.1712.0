package com.yineng.ynmessager.activity.p2psession;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.TransmitActivity.TransMsgEntity;
import com.yineng.ynmessager.activity.TransmitActivity.TransmitActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.adapter.ChatViewMsgAdapter;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.offline.HistoryMsg;
import com.yineng.ynmessager.bean.offline.MessageRevicerEntity;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.MessagePacketListenerImpl;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceivePresenceCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.OfflineMessageUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.view.tipView.TipView;
import com.yineng.ynmessager.view.tipView.TipViewItem;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.FaceRelativeLayout;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 用户聊天界面
 *
 * @author YINENG
 */
public class P2PChatActivity extends BaseChatActivity implements OnClickListener, ReceiveMessageCallBack,
        ReceiveReqIQCallBack, ReceivePresenceCallBack, AdapterView.OnItemLongClickListener, StatusChangedCallBack {
    public static final String ACCOUNT = "Account";
    public static final String CHOOSE_IMAGE_PATHS = "choose_image_paths";// 选择图片的路径列表
    private static final int REFRESH_TITLE = 0;

    private final String RECEIPT_BROADCAST = "receipt_broadcast";// 回执广播
    private final String BREAK_THREAD_TAG = "break_thread_tag";// 销毁线程的标识

    private final int GET_RECEIPT = 2;// 获得回执的处理
    private final int BROADCAST = 3;// 收到广播的处理
    private final int RECEIVE_MSG = 4;// 收到别人发送的消息
    private final int REFRESH_UI = 5;// 刷新UI
    private final int REFRESH_FACE_UI = 6;// 刷新表情UI
    private final int GET_HISTORY = 7;//获取历史消息

    private final int PAGE_SIZE = 20;// 分页查询的信息数量
    private final long TIME_INTERVAL = 60 * 5 * 1000;// 时间在5分钟内的消息不显示时间
    private final long RECEIPT_TIME_INTERVAL = 30 * 1000;// 超过半分钟未收到回执，则认为发送消息失败
    //表示拉取历史消息记录数
    private final int GET_HISTORY_COUNT = 40;
    //当前拉取条数
    private int getHistoryFaild = 0;
    /**
     * 未读消息列表
     */
    List<P2PChatMsgEntity> mUnreadList = new ArrayList<P2PChatMsgEntity>();
    private ClipboardManager mClipboard;
    private ReceiptThread mReceiptThread;
    private ReceipMessageQueue mReceipMessageQueue = new ReceipMessageQueue();// 回执消息处理队列
    private LinkedList<P2PChatMsgEntity> mMessageList = new LinkedList<>();
    private TextView mUnReadTV;
    private Button mSendBtn;
    private EditText mEditContentET;
    private ListView mListView;
    private PullToRefreshListView mPullToRefreshListView;
    private FaceRelativeLayout mFaceRelativeLayout;
    private ChatViewMsgAdapter mAdapter;
    private PendingIntent mReceiptPendingIntent;
    private AlarmManager mAlarmManager;
    private ReceiptBroadcastReceiver mReceiptBroadcastReceiver;
    private RecentChatDao mRecentChatDao;// 消息列表操作
    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;
    private int mPage = 0;
    private int mUnreadNum = 0;// 未读消息数量
    private boolean isBottom = true;// 消息显示界面是否在底部
    /**
     * 加载上一页的消息
     */
    protected Runnable mRefreshPrePageRunable = new Runnable() {
        @Override
        public void run() {
            //拉取离线消息
            sendHistoryMsgIQ(GET_HISTORY_COUNT, "");
            int mBeforeMoreCount = mAdapter.getCount();
            refreshUIByPageIndex(null);
            // mHandler.sendEmptyMessage(0);
            int mAfterMoreCount = mAdapter.getCount();
            int selectIndex = mAfterMoreCount - mBeforeMoreCount;
            mPullToRefreshListView.onRefreshComplete();
            if (selectIndex >= 0) {
                mListView.setSelection(selectIndex);
            }
        }
    };
    private boolean notBreak = true;// 销毁线程的标识
    private TextView mChatUserNameTV;
    private User myUserInfo;
    /**
     * 对方用户信息
     */
    private User mChatUserInfo;
    /**
     * 在线状态
     */
    private TextView mChatUserStatusTV;
    private ImageView mChatUserSexIV;
    private ContactOrgDao mContactOrgDao;
    private RelativeLayout layout_chat;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_RECEIPT:// 获得回执
                    ReqIQResult reqIq = (ReqIQResult) msg.obj;
                    //如果界面收到发送消息列     表数据的回执，则删除列表里该条数据,确保hashmap里存放的是未收到回执的数据
                    if (mChatMsgBeanMap.containsKey(reqIq.getPacketID())) {
                        mChatMsgBeanMap.remove(reqIq.getPacketID());
                    }
                    P2PChatMsgEntity entity = getEntityOfList(mMessageList, reqIq.getPacketID());
                    if (entity != null && entity.getIsSuccess() != P2PChatMsgEntity.SEND_SUCCESS) {
                        entity.setIsSuccess(P2PChatMsgEntity.SEND_SUCCESS);
                        //更新发送消息收到回执后的发送成功时间
                        long time = TimeUtil.getMillisecondByDate(reqIq.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
                        entity.setTime(String.valueOf(time));
                        //更新最近会话表的发送时间
                        if (entity.getPacketId().equals(reqIq.getPacketID())) {
                            mRecentChatDao.updateRecentChatTime(reqIq.getSendTime(), mChatUserNum, Const.CHAT_TYPE_P2P);
                        }
                        // updateMessageList(entity);
                        mP2PChatMsgDao.saveOrUpdate(entity);
                    }
                    notifyAdapterDataSetChanged();
                    break;
                case REFRESH_UI:// 刷新UI
                    //添加最近发送的一条消息到HashMap里，用于回执处理
                    P2PChatMsgEntity p2pChatMsgEntity = (P2PChatMsgEntity) msg.obj;
                    mChatMsgBeanMap.put(p2pChatMsgEntity.getPacketId(), msg.obj);
                    addToLastOrupdateMessageList((P2PChatMsgEntity) msg.obj);
                    notifyAdapterDataSetChanged();
                    break;
                case BROADCAST:// 回执超时处理广播
                    notifyAdapterDataSetChanged();
                    break;
                case RECEIVE_MSG:// 收到消息
                    addToLastOrupdateMessageList((P2PChatMsgEntity) msg.obj);
                    notifyAdapterDataSetChanged();
                    break;
                case REFRESH_FACE_UI:
                case REFRESH_TITLE:
                    updateUserStatusOnTitle();
                    break;
                case GET_HISTORY:
                    addHistoryOrUpdateMessageList(historyMessages);
                    break;
                default:
                    break;
            }
        }

    };
    private String myUserAccount;

    /**
     * 刷新未读消息提示数
     */
    private void refreshUnreadNumUI() {
        if (!isBottom) {
            // mUnreadNum++;
            mUnreadNum = mUnreadList.size();
            if (mUnreadNum > 0) {
                mUnReadTV.setVisibility(View.VISIBLE);
                mUnReadTV.setText(mUnreadNum + "");
            }
        } else// 底部
        {
            mUnReadTV.setVisibility(View.GONE);
            mUnreadNum = 0;
            mListView.setSelection(mAdapter.getCount());
            mUnreadList.clear();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (mChatUserInfo == null) {
            ToastUtil.toastAlerMessageBottom(P2PChatActivity.this, "暂无此人,请手动更新联系人.", 800);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUserObjectData();
        setContentView(R.layout.activity_p2p_chat_layout);
        showProgressD("加载中...", false);
        initialize();
        initEvent();
        updateUnreadCount();// 更新未读记录条数为0
        initDraft(); // 初始化加载草稿
        refreshDesktopIconBadge(); // 刷新桌面图标的未读消息数量气泡提示
        sendHistoryMsgIQ(GET_HISTORY_COUNT, "");//拉取历史列表
    }

    /**
     * 长按点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
        final P2PChatMsgEntity viewItem = (P2PChatMsgEntity) mAdapter.getItem(position - 1);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float OldListY = (float) location[1];
        float OldListX = (float) location[0];
        if (viewItem.getMessageType() == 12) {
            //此时代表的当前信息是文件接收的状态只有复制和删除功能
            new TipView.Builder(P2PChatActivity.this, layout_chat, (int) OldListX + view.getWidth() / 2, (int) OldListY)
                    .addItem(new TipViewItem("复制"))
                    .setOnItemClickListener(new TipView.OnItemClickListener() {
                        @Override
                        public void onItemClick(String str, final int position) {
                            if (position == 0) {
                                if (viewItem.getMessageType() == 0 || viewItem.getMessageType() == 1) { //自己封装的外层作为判断是否是文本或者图片类型
                                    String text = Html.fromHtml(viewItem.getSpannableString().toString()).toString();
                                    text = StringUtils.replace(text, "￼", ""); // 替换掉“￼”这个批符号
                                    mClipboard.setPrimaryClip(ClipData.newPlainText(mTag, text));
                                    showToast(R.string.common_copyToClipboard);
                                } else {
                                    showToast(R.string.commom_cannot_copyToClipboard);
                                }
                            }
                        }

                        @Override
                        public void dismiss() {

                        }
                    })
                    .create();
        } else {
            new TipView.Builder(P2PChatActivity.this, layout_chat, (int) OldListX + view.getWidth() / 2, (int) OldListY)
                    .addItem(new TipViewItem("复制"))
                    .addItem(new TipViewItem("转发"))
                    .setOnItemClickListener(new TipView.OnItemClickListener() {
                        @Override
                        public void onItemClick(String str, final int position) {
                            if (position == 0) {
                                if (viewItem.getMessageType() == 0 || viewItem.getMessageType() == 1) { //自己封装的外层作为判断是否是文本或者图片类型
                                    String text = Html.fromHtml(viewItem.getSpannableString().toString()).toString();
                                    text = StringUtils.replace(text, "￼", ""); // 替换掉“￼”这个批符号
                                    mClipboard.setPrimaryClip(ClipData.newPlainText(mTag, text));
                                    showToast(R.string.common_copyToClipboard);
                                } else {
                                    showToast(R.string.commom_cannot_copyToClipboard);
                                }

                            } else if (position == 1) {
                                // TODO 转发图片或者文件
                                if (viewItem.getIsSuccess() == 2) {
                                    this.dismiss();
                                    ToastUtil.toastAlerMessageCenter(P2PChatActivity.this, "发送中..请稍后重试..", 500);
                                } else {
                                    MessageBodyEntity bodyEntity = JSONObject.parseObject(viewItem.getMessage(), MessageBodyEntity.class);
                                    //  转发图片进入新的Activity
                                    if (bodyEntity.getFiles().size() > 0 ){
                                        MessageBodyEntity entity = JSONObject.parseObject(viewItem.getMessage(), MessageBodyEntity.class);
                                        if ("msg_pc".equalsIgnoreCase(entity.getResource())){
                                            if (viewItem.getIsSuccess()== BaseChatMsgEntity.DOWNLOAD_SUCCESS||viewItem.getIsSuccess()==BaseChatMsgEntity.SEND_SUCCESS){
                                                startTransmitActivity();
                                            }else {
                                                showToast(R.string.transmit_file_pc_cancel);
                                            }
                                        }else {
                                            startTransmitActivity();
                                        }

                                    }else if (bodyEntity.getImages().size() > 0 || bodyEntity.getContent().length() > 0){
                                        startTransmitActivity();
                                    }else {
                                        showToast(R.string.p2pChatActivity_transmitNotice);
                                    }
                                }
                            }
                        }

                        private void startTransmitActivity() {
                            Intent intent = new Intent(P2PChatActivity.this, TransmitActivity.class);
                            TransMsgEntity transEntity = new TransMsgEntity();
                            transEntity.setIsSuccess(viewItem.getIsSuccess());
                            transEntity.setMessage(viewItem.getMessage());
                            transEntity.setIsSend(viewItem.getIsSend());
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(TransmitActivity.TransEntity, transEntity);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }


                        @Override
                        public void dismiss() {

                        }
                    })
                    .create();
        }
        return true;
    }


    /**
     * 初始化聊天对象数据+
     */
    private void initUserObjectData() {
        mContactOrgDao = new ContactOrgDao(getApplicationContext());
        mChatUserInfo = getIntent().getParcelableExtra(Const.INTENT_USER_EXTRA_NAME);
        if (mChatUserInfo == null) {
            mChatUserNum = getIntent().getStringExtra(ACCOUNT);
            mChatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mChatUserNum);
        }
        myUserAccount = LastLoginUserSP.getInstance(P2PChatActivity.this).getUserAccount();
        myUserInfo = mContactOrgDao.queryUserInfoByUserNo(myUserAccount);
        if (mChatUserInfo != null) {
            mChatUserNum = mChatUserInfo.getUserNo();
        } else {
            mChatUserNum = getIntent().getStringExtra(ACCOUNT);
        }
    }

    @Override
    protected SendingListener initSendingListener() {
        SendingListener sendingListener = new SendingListener() {
            @Override
            public void onBeforeEachSend(int type, Message msg, int chatType) {
                updateRecentChatList(msg.getSubject());
                msg.setSubject(null);
                updateChatMsgEntity(type, msg);
            }

            @Override
            public void onEachDone(int type, Message msg, int chatType) {
                P2PChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                if (chatMsg != null) {
                    chatMsg.setMessage(msg.getBody());
                    mP2PChatMsgDao.saveOrUpdate(chatMsg);
                } else {
                    updateChatMsgEntity(type, msg);
                }
            }

            @Override
            public void onAllDone() {

            }

            @Override
            public void onFailedSend(int type, Message msg, int chatType) {
                //上传失败的回调方法，当上传失败时回调
                P2PChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                msg.setSubject(UploadResponseHandler.mFailedSend);
                if (chatMsg != null) {
                    chatMsg.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
                    mP2PChatMsgDao.saveOrUpdate(chatMsg);
                    //				mHandler.sendEmptyMessage(BROADCAST);

                    android.os.Message message = mHandler.obtainMessage();
                    message.obj = chatMsg;
                    message.what = REFRESH_UI;
                    mHandler.sendMessage(message);
                } else {
                    updateChatMsgEntity(type, msg);
                }
            }

        };
        return sendingListener;
    }

    @Override
    protected SendingListener initVoiceSendingListener() {
        SendingListener listener = new SendingListener() {
            @Override
            public void onBeforeEachSend(int type, Message msg, int chatType) {
                updateRecentChatList(msg.getSubject());
                msg.setSubject(null);

                P2PChatMsgEntity p2PChatMsgEntity = new P2PChatMsgEntity();
                p2PChatMsgEntity.setChatUserNo(mChatUserNum);
                p2PChatMsgEntity.setIsReaded(P2PChatMsgEntity.IS_READED);
                p2PChatMsgEntity.setMessage(msg.getBody());
                p2PChatMsgEntity.setMessageType(P2PChatMsgEntity.AUDIO_FILE);
                p2PChatMsgEntity.setIsSend(P2PChatMsgEntity.SEND);
                p2PChatMsgEntity.setTime(String.valueOf(System.currentTimeMillis()));
                p2PChatMsgEntity.setPacketId(msg.getPacketID());
                p2PChatMsgEntity.setIsSuccess(P2PChatMsgEntity.SEND_SUCCESS);
                mP2PChatMsgDao.saveOrUpdate(p2PChatMsgEntity);

                android.os.Message message = mHandler.obtainMessage();
                message.obj = p2PChatMsgEntity;
                message.what = REFRESH_UI;
                mHandler.sendMessage(message);
            }

            @Override
            public void onEachDone(int type, Message msg, int chatType) {

            }

            @Override
            public void onAllDone() {

            }

            @Override
            public void onFailedSend(int type, Message msg, int chatType) {
                //上传失败的回调方法，当上传失败时回调
                P2PChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                msg.setSubject(UploadResponseHandler.mFailedSend);
                if (chatMsg != null) {
                    chatMsg.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
                    mP2PChatMsgDao.saveOrUpdate(chatMsg);
                    //				mHandler.sendEmptyMessage(BROADCAST);

                    android.os.Message message = mHandler.obtainMessage();
                    message.obj = chatMsg;
                    message.what = REFRESH_UI;
                    mHandler.sendMessage(message);
                } else {
                    updateChatMsgEntity(type, msg);
                }
            }
        };
        return listener;
    }

    public void initialize() {
        initTitleView();
        mUnReadTV = (TextView) findViewById(R.id.tv_p2p_chat_tips);
        mSendBtn = (Button) findViewById(R.id.btn_send);
        mEditContentET = (EditText) findViewById(R.id.et_sendmessage);
        mFaceRelativeLayout = (FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout);
        mFaceRelativeLayout.setOtherButtonsClickListener(this);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pl_p2p_chat_pull_refresh_list);
        mListView = mPullToRefreshListView.getRefreshableView();
        layout_chat = (RelativeLayout) findViewById(R.id.layout_chat);
        mListView.setOnItemLongClickListener(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(RECEIPT_BROADCAST);
        mReceiptPendingIntent = PendingIntent.getBroadcast(this, 1, intent, Intent.FILL_IN_ACTION);
        mReceiptBroadcastReceiver = new ReceiptBroadcastReceiver();
        IntentFilter filter = new IntentFilter(RECEIPT_BROADCAST);
        registerReceiver(mReceiptBroadcastReceiver, filter);
        mP2PChatMsgDao = new P2PChatMsgDao(this);
        mRecentChatDao = new RecentChatDao(this);
        mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        /**优化聊天窗口**/
        mAdapter = new ChatViewMsgAdapter(P2PChatActivity.this, Const.CHAT_TYPE_P2P, mChatUserNum);
        mAdapter.setOnMsgResendListener(new ChatViewMsgAdapter.msgResendListener() {
            @Override
            public void onResend(Object obj) {
                send((P2PChatMsgEntity) obj);
            }
        });
        mListView.setAdapter(mAdapter);


        // 消息发送线程，回执处理线程
        mReceiptThread = new ReceiptThread();
        mReceiptThread.start();

        // 初始状态，从本地获得20条数据，刷新UI
        refreshUIByPageIndex(null);
        mPage = 0;
        mXmppConnManager.addReceiveMessageCallBack(mChatUserNum, this);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT, this);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, this);
        mXmppConnManager.addReceivePresCallBack(this);
        mXmppConnManager.addStatusChangedCallBack(this);

        // 回执处理广播
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, RECEIPT_TIME_INTERVAL, RECEIPT_TIME_INTERVAL,
                mReceiptPendingIntent);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            /**销毁gif引用**/
            mAdapter.destroyGifValue(false);
        }
        handleDraft();
        //当前页面暂停或停止时发送未读消息回执
        sendReceived();
    }


    private void initTitleView() {
        View mCommonTitleView = findViewById(R.id.group_chat_title_layout);
        mChatUserNameTV = (TextView) mCommonTitleView.findViewById(R.id.chat_common_title_view_name);
        if (mChatUserInfo == null) {
            mChatUserNum = getIntent().getStringExtra(ACCOUNT);
            mChatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mChatUserNum);
        }
        mChatUserNameTV.setText(mChatUserInfo != null ? mChatUserInfo.getUserName() : "单人会话");
        mChatUserNameTV.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
        mChatUserSexIV = (ImageView) mCommonTitleView.findViewById(R.id.chat_common_title_view_infomation);
        mChatUserStatusTV = (TextView) mCommonTitleView.findViewById(R.id.chat_common_title_view_status);
        mChatUserStatusTV.setVisibility(View.VISIBLE);
        updateUserStatusOnTitle();
    }

    /**
     * 更新该用户在线情况
     */
    private void updateUserStatusOnTitle() {
        if (mChatUserInfo != null) {
            mChatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mChatUserInfo.getUserNo());
            if (mChatUserInfo.getGender() == 1) {//男
                mChatUserSexIV.setImageResource(R.mipmap.user_center_men);
            } else if (mChatUserInfo.getGender() == 2) {//女
                mChatUserSexIV.setImageResource(R.mipmap.user_center_woman);
            } else {
                mChatUserSexIV.setImageResource(R.mipmap.user_center_men);
            }
            switch (mChatUserInfo.getUserStatus()) {
                case Const.USER_OFF_LINE:
                    mChatUserStatusTV.setText(R.string.p2pChatActivity_userStatus_offline);
                    break;
                case Const.USER_ON_LINE_PHONE:
                    mChatUserStatusTV.setText(R.string.p2pChatActivity_userStatus_phone);
                    break;
                case Const.USER_ON_LINE_PC:
                case Const.USER_ON_LINE_ALL:
                    mChatUserStatusTV.setText(R.string.p2pChatActivity_userStatus_pc);
                    break;
                default:
                    break;
            }
        }
    }

    private void initEvent() {
        mSendBtn.setOnClickListener(this);

        mPullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mHandler.postDelayed(mRefreshPrePageRunable, 1000);
            }
        });
        mListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case OnScrollListener.SCROLL_STATE_IDLE: //
                        int firstVisiblePos = view.getFirstVisiblePosition() - 1;
                        int lastVisiblePos = view.getLastVisiblePosition() - 1;
                        int sumCount = mAdapter.getCount();
                        L.e("listview stop : " + firstVisiblePos + " " + lastVisiblePos + " " + (sumCount));
                        for (int i = 0; i < sumCount; i++) {
                            if (i < firstVisiblePos || i > lastVisiblePos) {
                                P2PChatMsgEntity entity = (P2PChatMsgEntity) mAdapter.getItem(i);
                                if (entity.getSpannableString() != null) {
                                    SpannableString spannableString = entity.getSpannableString();
                                    mAdapter.destoryTempGifMemory(spannableString, false);
                                    entity.setSpannableString(null);
                                }
                            }
                        }
                        //将onScroll中的方法提到此处来判断是因为在为了避免初次进入的时候执行了onScroll里面的方法
                        //避免了在点击弹出框的时候在低版本的手机上也会执行onScroll方法会自动滑到底部
                        boolean toBottom = view.getLastVisiblePosition() == view.getCount() - 1;
                        if (toBottom) {//底部
                            isBottom = true;
                            mUnreadNum = 0;
                        } else {
                            isBottom = false;
                            // 滚动过程中，遇到新消息，更新未读气泡
                            P2PChatMsgEntity tempP2PChatMsg = (P2PChatMsgEntity) mAdapter.getItem(lastVisiblePos);
                            if (mUnreadList.contains(tempP2PChatMsg)) {
                                mUnreadList.remove(tempP2PChatMsg);
                            }
                        }
                        refreshUnreadNumUI();
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        mListView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    mFaceRelativeLayout.hideFaceView();
                }
                return false;
            }
        });
    }

    /**
     * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
     */
    private void notifyAdapterDataSetChanged() {
        List<Object> list = new ArrayList<Object>();
        long preShowTime = 0;
        int length = mMessageList.size();
        for (int i = 0; i < length; i++) {
            P2PChatMsgEntity entity = mMessageList.get(i);
            if (i == 0) {
                Log.i(mTag, "时间：  " + entity.getTime());
                entity.setShowTime(true);
                preShowTime = Long.valueOf(entity.getTime().trim());
            } else {
                if (compareTime(preShowTime, Long.valueOf(entity.getTime()))) {
                    preShowTime = Long.valueOf(entity.getTime());
                    entity.setShowTime(true);
                } else {
                    entity.setShowTime(false);
                }
            }
            list.add(entity);
        }
        // list作为临时的数据缓存，避免数据变更后，没有及时通知适配器，出现
        // The content of the adapter has changed but ListView did not receive a
        // notification的错误
        mAdapter.setData(list);
        mAdapter.notifyDataSetChanged();
        refreshUnreadNumUI();
    }

    /**
     * 本地分页查询数据，刷新UI
     *
     * @param deletedChatMsg 之前删除的某条消息记录，用来更新数据源（可选的，不是删除消息操作调用，可以传null）
     */
    public void refreshUIByPageIndex(P2PChatMsgEntity deletedChatMsg) {
        isBottom = true;
        LinkedList<P2PChatMsgEntity> list = mP2PChatMsgDao.getChatMsgEntitiesByPage(mChatUserNum, mPage, PAGE_SIZE);
        if (mMessageList.contains(deletedChatMsg)) {
            mMessageList.remove(deletedChatMsg);
        }
        if ((list != null && !list.isEmpty())) {
            for (P2PChatMsgEntity mP2PChatMsgEntity : list) {
                if (mMessageList.contains(mP2PChatMsgEntity)) {
                    continue;
                }
                mMessageList.addFirst(mP2PChatMsgEntity);
            }
            mPage++;
        }
        notifyAdapterDataSetChanged();
        hideProgessD();
    }

    @Override
    public void onBackPressed() {
        //如果正在录音，则不能响应返回键
        if (mFaceRelativeLayout.isVoiceFragmentRecording()) {
            return;
        }
        if (mFaceRelativeLayout.hideFaceView()) {
            return;
        }
        super.onBackPressed();
    }

    public void onTitleViewClickListener(View v) {
        switch (v.getId()) {
            case R.id.chat_common_title_view_back:
                //如果正在录音，则不能响应返回键
                if (mFaceRelativeLayout.isVoiceFragmentRecording()) {
                    return;
                }
                mFaceRelativeLayout.hideFaceView();
                finish();
                break;
            case R.id.chat_common_title_view_infomation:
                if (mChatUserInfo == null) {
                    ToastUtil.toastAlerMessageBottom(P2PChatActivity.this, "暂无此人,请手动更新联系人.", 800);
                    return;
                }
                Intent intent = new Intent(this, P2PChatInfoActivity.class);
                intent.putExtra(GroupChatActivity.CHAT_ID_KEY, mChatUserNum);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 发送消息
     */
    private void clickSend() {
        isBottom = true;
        mUnreadNum = 0;
        String contString = mEditContentET.getText().toString();
        // 如果没有输入消息，不能发送
        if (contString.isEmpty()) {
            return;
        }
        updateRecentChatList(contString);// 更新最近会话列表显示内容
        P2PChatMsgEntity p2PChatMsgEntity = new P2PChatMsgEntity();
        Message msg = new Message();
        // 封装body
        MessageBodyEntity body = new MessageBodyEntity();
        body.setContent(contString);
        body.setResource(MessageBodyEntity.SOURCE_PHONE);
        // body.setSendName(System.currentTimeMillis() + "");
        body.setSendName(myUserInfo != null ? myUserInfo.getUserName() : myUserAccount);
        body.setMsgType(Const.CHAT_TYPE_P2P);
        String jsonString = JSON.toJSONString(body);

        // 封装P2PChatMsgEntity对象
        p2PChatMsgEntity.setIsSuccess(P2PChatMsgEntity.SEND_ING);
        p2PChatMsgEntity.setChatUserNo(mChatUserNum);
        p2PChatMsgEntity.setIsReaded(P2PChatMsgEntity.IS_READED);
        p2PChatMsgEntity.setMessage(jsonString);
        p2PChatMsgEntity.setMessageType(P2PChatMsgEntity.MESSAGE);
        p2PChatMsgEntity.setIsSend(P2PChatMsgEntity.SEND);
        p2PChatMsgEntity.setTime(System.currentTimeMillis() + "");
        p2PChatMsgEntity.setPacketId(msg.getPacketID());
        // 发送操作
        send(p2PChatMsgEntity);
        mEditContentET.setText("");
    }

    /**
     * 发送回执消息
     */
    private void sendReceived() {
        Message msg = new Message();
        //封装回执消息
        MessageRevicerEntity revicerEntity = new MessageRevicerEntity(mChatUserNum);
        revicerEntity.setType(Const.CHAT_TYPE_P2P);
        msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserAccount));
        msg.setTo("admin@" + mXmppConnManager.getServiceName());
        msg.setPacketID(msg.getPacketID());
        msg.setType(Type.headline);
        msg.addExtension(revicerEntity);
        //网络可用的时候才能让xmpp发送msg，否则将发送的消息改为发送失败的状态
        if (NetWorkUtil.isNetworkAvailable(P2PChatActivity.this)) {
            mXmppConnManager.sendPacket(msg);
        }
        Log.i(mTag, "发送消息回执:" + msg.toXML());
    }

    /**
     * 发送获取历史消息iq
     */
    private void sendHistoryMsgIQ(int count, String lastTime) {
        //拉取最老的一条消息
        LinkedList<P2PChatMsgEntity> historys = new LinkedList<>();
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            historys.add((P2PChatMsgEntity) mAdapter.getData().get(i));
        }

        Collections.sort(historys, new TimeUtil.ComparatorMessageTimeDESC<P2PChatMsgEntity>());
        String sendTime = "";
        //获取当前列表最新的一条消息,判断是否有数据
        P2PChatMsgEntity p2pChat = null;
        try {
            p2pChat = historys.get(0);
            if (mPage == 0) {
                p2pChat = historys.get(historys.size() - 1);
            }
            sendTime = TimeUtil.getDateByMillisecond(Long.parseLong(p2pChat.getTime()), TimeUtil.FORMAT_DATETIME_24_mic);
        } catch (Exception e) {
            sendTime = "";
        }
        if (!StringUtils.isEmpty(lastTime)) {
            sendTime = TimeUtil.getDateByMillisecond(Long.parseLong(lastTime), TimeUtil.FORMAT_DATETIME_24_mic);
        }
        ReqIQ iq = new ReqIQ();
        iq.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserAccount));
        iq.setTo("admin@" + mXmppConnManager.getServiceName());
        iq.setAction(2);
        iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        HistoryMsg historyMsg = new HistoryMsg();
        historyMsg.setChatType(Const.CHAT_TYPE_P2P);
        historyMsg.setChatId(mChatUserNum);
        historyMsg.setSendTime(sendTime);
        historyMsg.setMsgCount(String.valueOf(count));
        String paramsJsonStr = JSON.toJSONString(historyMsg);
        iq.setParamsJson(paramsJsonStr);
        if (NetWorkUtil.isNetworkAvailable(this)) {
            mXmppConnManager.sendPacket(iq);
        }
        L.i("拉取历史消息IQ:" + iq.toXML());
    }

    /**
     * 发送消息
     *
     * @param p2PChatMsgEntity
     */
    public void send(final P2PChatMsgEntity p2PChatMsgEntity) {
        if (mChatUserInfo == null) {
            ToastUtil.toastAlerMessageBottom(P2PChatActivity.this, "暂无此人,请手动更新联系人.", 800);
            return;
        }
        Message msg = new Message();
        android.os.Message message = mHandler.obtainMessage();
        message.obj = p2PChatMsgEntity;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);
        switch (p2PChatMsgEntity.getMessageType()) {
            case P2PChatMsgEntity.MESSAGE:
                // msg.setBody(GZIPUtil.compress(p2PChatMsgEntity.getMessage()));
                msg.setBody(p2PChatMsgEntity.getMessage());
                msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserAccount));
                msg.setTo(JIDUtil.getSendToMsgAccount(mChatUserNum));
                // msg.setTo(JIDUtil.getJIDByAccount(mChatUserNum));
                msg.setType(Type.chat);
                msg.setPacketID(p2PChatMsgEntity.getPacketId());
                // msg.setPacketID(entity.getPacketId());
                try {
                    //网络可用的时候才能让xmpp发送msg，否则将发送的消息改为发送失败的状态
                    if (NetWorkUtil.isNetworkAvailable(P2PChatActivity.this)) {
                        mXmppConnManager.sendPacket(msg);
                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                p2PChatMsgEntity.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
                                mP2PChatMsgDao.saveOrUpdate(p2PChatMsgEntity);
                                android.os.Message failMessage = mHandler.obtainMessage();
                                failMessage.obj = p2PChatMsgEntity;
                                failMessage.what = REFRESH_UI;
                                mHandler.sendMessage(failMessage);
                            }
                        }, 1000);
                    }
                } finally {
                    mP2PChatMsgDao.saveOrUpdate(p2PChatMsgEntity);
                }
                break;
            case P2PChatMsgEntity.IMAGE:
                //重发 发送失败的
                if (p2PChatMsgEntity.getIsSuccess() == P2PChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(P2PChatActivity.this,
                            p2PChatMsgEntity, Const.CHAT_TYPE_P2P, mChatUserNum,
                            mSendingListener);
                }
                break;
            case P2PChatMsgEntity.FILE:
                //重发 发送失败的
                if (p2PChatMsgEntity.getIsSuccess() == P2PChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(P2PChatActivity.this,
                            p2PChatMsgEntity, Const.CHAT_TYPE_P2P, mChatUserNum,
                            mSendingListener);
                }
                break;
            case P2PChatMsgEntity.AUDIO_FILE:
                if (p2PChatMsgEntity.getIsSuccess() == P2PChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(P2PChatActivity.this,
                            p2PChatMsgEntity, Const.CHAT_TYPE_P2P, mChatUserNum,
                            mSendingListener);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 添加或更新消息列表
     *
     * @param entity
     */
    private void addToLastOrupdateMessageList(P2PChatMsgEntity entity) {
        if (entity == null || mMessageList == null) {
            return;
        }
        for (int i = (mMessageList.size() - 1); i >= 0; i--) {
            if (entity.getPacketId().equals(mMessageList.get(i).getPacketId())) {
                mMessageList.set(i, entity);
                return;
            }
        }
        mMessageList.addLast(entity);
    }

    /**
     * 添加历史消息
     *
     * @param entitys
     */
    private void addHistoryOrUpdateMessageList(List<P2PChatMsgEntity> entitys) {
        if (entitys == null) return;

        ArrayList<Object> curList = new ArrayList<>(mAdapter.getData());
        curList.removeAll(entitys);
        curList.addAll(entitys);

        LinkedList<P2PChatMsgEntity> historys = new LinkedList<>();
        for (int i = 0; i < curList.size(); i++) {
            historys.add((P2PChatMsgEntity) curList.get(i));
        }
        Collections.sort(historys, new TimeUtil.ComparatorMessageTimeDESC<P2PChatMsgEntity>());
        notifyAdapterDataSetChanged(historys);
    }

    /**
     * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
     */
    private void notifyAdapterDataSetChanged(LinkedList<P2PChatMsgEntity> data) {
        List<Object> list = new ArrayList<Object>();
        long preShowTime = 0;
        int length = data.size();
        for (int i = 0; i < length; i++) {
            P2PChatMsgEntity entity = data.get(i);
            if (i == 0) {
                Log.i(mTag, "时间：  " + entity.getTime());
                entity.setShowTime(true);
                preShowTime = Long.valueOf(entity.getTime().trim());
            } else {
                if (compareTime(preShowTime, Long.valueOf(entity.getTime()))) {
                    preShowTime = Long.valueOf(entity.getTime());
                    entity.setShowTime(true);
                } else {
                    entity.setShowTime(false);
                }
            }
            list.add(entity);
            mP2PChatMsgDao.saveMsg(entity);
        }
        //如果是第一页就刷新
        if (mPage == 0) {
            mMessageList.clear();
            refreshUIByPageIndex(null);
        }
    }

    /**
     * 点击了页面中的发送按钮，发送消息
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if (mChatUserInfo == null) {
                    ToastUtil.toastAlerMessageBottom(P2PChatActivity.this, "暂无此人,请手动更新联系人.", 800);
                    return;
                }
                clickSend();
                break;
        }
    }

    /**
     * Compare the time difference is greater than TIME_INTERVAL minutes
     *
     * @param preTime
     * @param nextTime
     * @return
     */
    public boolean compareTime(long preTime, long nextTime) {
        return (nextTime - preTime) >= TIME_INTERVAL;
    }

    //下载文件时、更新ui
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseChatMsgEntity eventObj) {
        if (eventObj == null || mMessageList == null) {
            return;
        }
        for (P2PChatMsgEntity tempMsgObj : mMessageList) {
            if (eventObj.getPacketId().equals(tempMsgObj.getPacketId())) {
                tempMsgObj.setReceiveProgress(eventObj.getReceiveProgress());
                tempMsgObj.setReceivedBytes(eventObj.getReceivedBytes());
                tempMsgObj.setIsSuccess(eventObj.getIsSuccess());
                // 文件下载成功发送回执
                if (eventObj.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_SUCCESS && eventObj.getMessageType() != BaseChatMsgEntity.AUDIO_FILE) {
                    MessageBodyEntity body = JSON.parseObject(tempMsgObj.getMessage(), MessageBodyEntity.class);
                    if(body.getMsgType()!=Const.CHAT_TYPE_P2P){
                        return;
                    }
                    //自己接收自己发送的文件不发送回执消息
                    if (!body.getSendName().equals(myUserInfo.getUserName())) {
                        String mes = "对方已在手机上成功接收文件：" + "“" + body.getFiles().get(0).getName() + "”";
                        sendFileIQMsg(mes);
                    }
                }
                break;
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 文件下载成功时给对方发送一个message通知对方已经下载成功
     * 因为在文件下载成功的同时（如果网络断开不会下载成功）发送mes到openfire所以此处不做
     * 网络判断处理
     *
     * @param mes
     */
    private void sendFileIQMsg(String mes) {
        Message msg = new Message();
        // 封装body
        MessageBodyEntity body = new MessageBodyEntity();
        body.setContent(mes);
        // body.setSendName(System.currentTimeMillis() + "");
        body.setSendName(myUserInfo != null ? myUserInfo.getUserName() : myUserAccount);
        body.setMsgType(Const.CHAT_TYPE_FILE);
        String jsonString = JSON.toJSONString(body);
        //添加文件消息回执节点
        msg.addExtension(new PacketExtension() {
            @Override
            public String getElementName() {
                return "filereceived";
            }

            @Override
            public String getNamespace() {
                return Const.REQ_IQ_XMLNS_MSG_RESULT;
            }

            @Override
            public String toXML() {
                return "<filereceived xmlns=\"com:yineng:receipt\"></filereceived>";
            }
        });
        msg.setBody(jsonString);
        msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserAccount));
        msg.setTo(JIDUtil.getSendToMsgAccount(mChatUserNum));
        msg.setType(Type.chat);
        mXmppConnManager.sendPacket(msg);
    }


    @Override
    protected void onDestroy() {
        if (mAdapter != null) {
            /**销毁gif引用**/
            mAdapter.destroyGifValue(true);
        }

        notBreak = false;
        mXmppConnManager.removeReceiveMessageCallBack(mChatUserNum);
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);
        mXmppConnManager.removeReceivePresCallBack(this);
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        mXmppConnManager.removeStatusChangedCallBack(this);

        unregisterReceiver(mReceiptBroadcastReceiver);
        mAlarmManager.cancel(mReceiptPendingIntent);

        // 销毁回执线程
        mReceipMessageQueue.putEntity(null);
        NoticesManager.getInstance(this).updateRecentChatList(mChatUserNum, Const.CHAT_TYPE_P2P);// 更新最近会话列表


        System.gc();
        super.onDestroy();
    }

    /**
     * 草稿的处理
     */
    private void handleDraft() {
        CharSequence draft = mEditContentET.getText();
        // : 2016/8/8 下面这句报打开已关闭的数据库对象，在query的时候
        RecentChat thisChat = mRecentChatDao.isChatExist(mChatUserNum, Const.CHAT_TYPE_P2P);
        if (!TextUtils.isEmpty(draft) && thisChat == null) // 最近会话列表中没有存在，但已经输入草稿，需保存草稿的情况
        {
            updateRecentChatList("", draft.toString());
            // 存在于最近会话里面，但没有任何聊天记录，也没有草稿的情况
        } else if (TextUtils.isEmpty(draft) && thisChat != null && TextUtils.isEmpty(thisChat.getContent())) {
            mRecentChatDao.deleteRecentChatById(thisChat.getId()); // 从最近会话列表里面删除
        } else
        // 适用于一般情况，直接更新草稿
        {
            mRecentChatDao.updateDraft(mChatUserNum, draft.toString());
        }
    }


    /**
     * 接收个人对话信息
     *
     * @param message
     */
    @Override
    public void receivedMessage(P2PChatMsgEntity message) {
        // 此方法被线程调用，不能刷新UI
        Log.i(mTag, "收到消息");
        if (!isBottom) {
            mUnreadList.add(message);
        }
        message.setIsReaded(P2PChatMsgEntity.IS_READED);
        if (message.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
            message.setIsReaded(P2PChatMsgEntity.IS_NOT_READED);
        }
        mP2PChatMsgDao.saveOrUpdate(message);
        android.os.Message msgMessage = mHandler.obtainMessage();
        msgMessage.what = RECEIVE_MSG;
        msgMessage.obj = message;
        mHandler.sendMessage(msgMessage);
        //发送消息回执
        if (isReceiver) {
            receiverTimer.start();
        }
    }

    /**
     * 获取消息时定时发送消息已读回执
     */
    private boolean isReceiver = true;
    private CountDownTimer receiverTimer = new CountDownTimer(1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            isReceiver = false;
        }

        @Override
        public void onFinish() {
            sendReceived();
            isReceiver = true;
        }
    };

    /**
     * 接收回执的方法
     *
     * @param packet
     */
    private List<P2PChatMsgEntity> historyMessages = new ArrayList<>();
    @Override
    public void receivedReqIQResult(ReqIQResult packet) {
        //拉取到历史消息
        if (packet.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST)) {
            List<HashMap> messagesJson = JSON.parseArray(packet.getResp(), HashMap.class);
            if (messagesJson.size() > 0) {
                for (int i = 0; i < messagesJson.size(); i++) {
                    String xml = String.valueOf(messagesJson.get(i).get("msg"));
                    Message message = OfflineMessageUtil.parserMessage(xml);
                    String jsonBody = MessagePacketListenerImpl.formatHtmlBodyToJson(message.getBody());
                    MessageBodyEntity tempBodyEntity = MessagePacketListenerImpl.getContentByBody(jsonBody);
                    String mFromAccount = JIDUtil.getAccountByJID(message.getFrom().trim());
                    String mToAccount = JIDUtil.getAccountByJID(message.getTo().trim());
                    //历史消息不拉取文件接收回执
                    if (tempBodyEntity.getMsgType() == Const.CHAT_TYPE_FILE && mFromAccount.equals(myUserAccount)) {
                        getHistoryFaild++;
                        continue;
                    }
                    String showAccount = mFromAccount;
                    if (mFromAccount.equals(myUserAccount)) {
                        showAccount = mToAccount;
                    }

                    P2PChatMsgEntity msg = (P2PChatMsgEntity) MessagePacketListenerImpl.generateMsgObj(showAccount, Const.CHAT_TYPE_P2P,
                            tempBodyEntity, jsonBody, message, null);
                    if (mFromAccount.equals(myUserAccount)) {
                        msg.setIsSend(0);
                        //如果是自己发送的文件
                        if (msg.getMessageType() == BaseChatMsgEntity.IMAGE || msg.getMessageType() == BaseChatMsgEntity.FILE || msg.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
                            msg.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
                        }
                    } else {
                        //如果是别人发送的文件
                        if (msg.getMessageType() == BaseChatMsgEntity.IMAGE || msg.getMessageType() == BaseChatMsgEntity.FILE || msg.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
                            msg.setIsReaded(BaseChatMsgEntity.IS_READED);
                        }
                    }
                    Long sendTime = TimeUtil.getMillisecondByDate(message.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
                    msg.setTime(String.valueOf(sendTime));
                    historyMessages.add(msg);
                }
                android.os.Message msgMessage = mHandler.obtainMessage();
                msgMessage.what = GET_HISTORY;
                mHandler.sendMessage(msgMessage);
            }
            //如果是发送消息回执
        } else if (packet.getNameSpace().equals(Const.REQ_IQ_XMLNS_MSG_RESULT)) {
            mReceipMessageQueue.putEntity(packet);
        }
        L.i(mTag, "回执：" + packet.toString());
    }

    /**
     * 返回指定packetId在list中的存储位置， 如果返回-1，则表示 list中未存储
     *
     * @param packetId
     * @return
     */
    private P2PChatMsgEntity getEntityOfList(final List<P2PChatMsgEntity> list, final String packetId) {
        for (int i = (list.size() - 1); i >= 0; i--) {
            P2PChatMsgEntity entity = list.get(i);
            if (packetId != null && packetId.trim().equals(entity.getPacketId().trim())) {
                return entity;
            }
        }
        return null;
    }

    /**
     * 更新未读记录
     */
    private void updateUnreadCount() {
        RecentChat recentChat = mRecentChatDao.isChatExist(mChatUserNum, Const.CHAT_TYPE_P2P);
        if (recentChat != null) {
            int unReadCount = recentChat.getUnReadCount();
            if (unReadCount > 0) {
                sendReceived(); //发送消息回执
            }
        }
        mRecentChatDao.updateUnreadCount(mChatUserNum, Const.CHAT_TYPE_P2P, 0);// 设置未读记录为0
        Intent intent = new Intent();
        intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        this.sendBroadcast(intent);
    }

    /**
     * 把消息内容更新到最近会话列表
     */
    private void updateRecentChatList(String content) {
        updateRecentChatList(content, "");
    }

    /**
     * 把消息内容更新到最近会话列表
     */
    private void updateRecentChatList(String content, String draft) {
        updateRecentChatList(content, new Date(), draft);
    }

    /**
     * 把消息内容更新到最近会话列表
     */
    private void updateRecentChatList(String content, Date timeStamp, String draft) {
        RecentChat recentChat = new RecentChat();
        recentChat.setChatType(Const.CHAT_TYPE_P2P);
        recentChat.setUserNo(mChatUserNum);
        recentChat.setTitle(mRecentChatDao.getUserNameByUserId(mChatUserNum, Const.CHAT_TYPE_P2P));
        recentChat.setContent(content);
        recentChat.setDateTime(DateFormatUtils.format(timeStamp, TimeUtil.FORMAT_DATETIME_24_mic));
        recentChat.setUnReadCount(0);
        recentChat.setDraft(draft);
        mRecentChatDao.saveRecentChat(recentChat);
    }

    /**
     * 初始化加载草稿
     */
    private void initDraft() {
        // 加载草稿
        String draft = mRecentChatDao.queryDraftByUserNo(mChatUserNum);
        SpannableString spannableDraft = FaceConversionUtil.getInstace().getExpressionString(this, draft);
        mEditContentET.setText(spannableDraft);
        mEditContentET.setSelection(spannableDraft.length());
    }

    /**
     * 刷新桌面图标的未读消息数量气泡提示
     */
    private void refreshDesktopIconBadge() {
        Context context = getApplicationContext();
        // 更新桌面图标未读气泡数字
        IconBadgerHelper.showIconBadger(context);
    }

    /**
     * 接收群、讨论组信息方法
     *
     * @param msg
     */
    @Override
    public void receivedMessage(GroupChatMsgEntity msg) {

    }

    /**
     * 登录状态监听
     *
     * @param status
     */
    @Override
    public void onStatusChanged(int status) {
        //重新登录获取历史消息
        if (status == LoginThread.USER_STATUS_ONLINE) {
            mHandler.post(mRefreshPrePageRunable);
        }
    }

    /**
     * 刷新UI
     */
    protected void updateChatMsgEntity(int type, Message msg) {
        P2PChatMsgEntity p2PChatMsgEntity = new P2PChatMsgEntity();
        p2PChatMsgEntity.setChatUserNo(mChatUserNum);
        p2PChatMsgEntity.setIsReaded(P2PChatMsgEntity.IS_READED);
        p2PChatMsgEntity.setMessage(msg.getBody());
        p2PChatMsgEntity.setMessageType(P2PChatMsgEntity.MESSAGE);
        p2PChatMsgEntity.setIsSend(P2PChatMsgEntity.SEND);
        p2PChatMsgEntity.setTime(String.valueOf(System.currentTimeMillis()));
        p2PChatMsgEntity.setPacketId(msg.getPacketID());
        p2PChatMsgEntity.setMessageType(type);
        if (msg.getSubject() != null) {
            //上传失败
            if (msg.getSubject().equals(UploadResponseHandler.mFailedSend)) {
                p2PChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                msg.setSubject(null);
            } else {
                p2PChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
            }
        } else {
            p2PChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        }
        mP2PChatMsgDao.saveOrUpdate(p2PChatMsgEntity);

        android.os.Message message = mHandler.obtainMessage();
        message.obj = p2PChatMsgEntity;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);
    }

    /**
     * 接收当前状态
     *
     * @param packet
     */
    @Override
    public void receivedPresence(Presence packet) {
        Presence p = packet;
        String xNameSpace = p.getxNameSpace();
        if (xNameSpace == null) {
            String mUserNo = JIDUtil.getAccountByJID(p.getFrom());
            if (mUserNo.equals(mChatUserNum)) {
                mHandler.sendEmptyMessage(REFRESH_TITLE);
            }
        }
    }


    /**
     * 更新整个消息链表，跟新本地存储信息
     *
     * @author YINENG
     */
    class ReceiptBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean flag = false;
            Log.i(mTag, "广播接收，处理回执");
            for (P2PChatMsgEntity entity : mMessageList) {
                //如果是发送文本类消息，则处理30s超时回执
                if (entity.getIsSend() == GroupChatMsgEntity.SEND
                        && entity.getMessageType() == GroupChatMsgEntity.MESSAGE) {
                    //如果是正在发送的文本，且超过30秒 则修改状态为失败状态
                    if (entity.getIsSuccess() == P2PChatMsgEntity.SEND_ING
                            && System.currentTimeMillis() - Long.valueOf(entity.getTime()) > RECEIPT_TIME_INTERVAL) {
                        entity.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
                        mP2PChatMsgDao.saveOrUpdate(entity);
                        flag = true;
                    }
                }
            }
            // UI数据有更新，则添加刷新UI的操作到主线程消息队列中
            if (flag) {
                mHandler.sendEmptyMessage(BROADCAST);
            }
        }
    }

//	/**
//	 * 回收bitmap暂用的内存空间
//	 * @param b
//	 */
//	private void destroyGifValue(boolean b)
//	{
//		List<Object> mAdapterList = mAdapter.getData();
//		if(mAdapterList != null)
//		{
//			for(Object P2PChatObj : mAdapterList)
//			{
//				P2PChatMsgEntity p2pChatMsgEntity = (P2PChatMsgEntity) P2PChatObj;
//				SpannableString tempSpan = p2pChatMsgEntity.getSpannableString();
//				if(tempSpan != null)
//				{
//					destoryTempGifMemory(tempSpan,b);
//				}
//			}
//		}
//	}
//
//	/**
//	 * 销毁gif内存
//	 * @param spannableString
//	 */
//	private void destoryTempGifMemory(SpannableString spannableString,boolean recycleBmp) {
//		AnimatedImageSpan[] tem = spannableString.getSpans(0, spannableString.length()-1, AnimatedImageSpan.class);
//		for (AnimatedImageSpan animatedImageSpan : tem) {
//			animatedImageSpan.recycleBitmaps(recycleBmp);
////			animatedImageSpan = null;
//		}
//		spannableString.removeSpan(tem);
    //	}

    /**
     * 收到回执的处理线程
     *
     * @author YINENG
     */
    class ReceiptThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (true) {
                ReqIQResult packet = mReceipMessageQueue.getEntity();
                if (!notBreak && packet == null) {
                    break;
                } else {
                    android.os.Message message = mHandler.obtainMessage();
                    message.obj = packet;
                    message.what = GET_RECEIPT;
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    /**
     * 收到回执消息的队列
     *
     * @author YINENG
     */
    class ReceipMessageQueue {
        private final LinkedList<ReqIQResult> queue = new LinkedList<>();
        public synchronized ReqIQResult getEntity() {
            while (queue.size() <= 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return queue.removeFirst();
        }
        public synchronized void putEntity(ReqIQResult entity) {
            queue.addLast(entity);
            notifyAll();
        }
    }
}
