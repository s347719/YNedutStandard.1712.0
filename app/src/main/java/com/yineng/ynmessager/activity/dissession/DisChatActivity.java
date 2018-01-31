package com.yineng.ynmessager.activity.dissession;

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
import com.yineng.ynmessager.activity.p2psession.BaseChatActivity;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.adapter.ChatViewMsgAdapter;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.offline.HistoryMsg;
import com.yineng.ynmessager.bean.offline.MessageRevicerEntity;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.MessagePacketListenerImpl;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.ImageUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.OfflineMessageUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.FaceRelativeLayout;
import com.yineng.ynmessager.view.tipView.TipView;
import com.yineng.ynmessager.view.tipView.TipViewItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * 讨论组界面
 *
 * @author YINENG
 */
public class DisChatActivity extends BaseChatActivity implements OnClickListener,
        ReceiveMessageCallBack, ReceiveReqIQCallBack, FaceRelativeLayout.OtherButtonsClickListener, AdapterView.OnItemLongClickListener {

    private static final String RECEIPT_BROADCAST = "receipt_broadcast";// 回执广播
    private static final long RECEIPT_TIME_INTERVAL = 30 * 1000;// 超过半分钟未收到回执，则认为发送消息失败
    private static final int PAGE_SIZE = 20;// 分页查询的信息数量
    private static final int GET_RECEIPT = 2;// 获得回执的处理
    private static final int BROADCAST = 3;// 收到广播的处理
    private static final int RECEIVE_MSG = 4;// 收到别人发送的消息
    private static final int REFRESH_UI = 5;// 刷新UI
    private final int GET_HISTORY = 7;//获取历史消息
    private static final long TIME_INTERVAL = 60 * 5 * 1000;// 时间在5分钟内的消息不显示时间
    //是否销毁Activity
    protected boolean isFinishAcitivity = false;
    //表示拉取历史消息记录数
    private final int GET_HISTORY_COUNT = 40;
    /**
     * 未读消息列表
     */
    List<GroupChatMsgEntity> mUnreadList = new ArrayList<>();
    private RecentChatDao mRecentChatDao;// 消息列表操作
    private Context mContext;
    private ClipboardManager mClipboard;
    private TextView mUnReadTV;
    private Button mSendBtn;
    private EditText mEditContentET;
    private ListView mListView;
    private FaceRelativeLayout mFaceRelativeLayout;
    private PullToRefreshListView mPullToRefreshListView;
    private ChatViewMsgAdapter mAdapter;
    private PendingIntent mReceiptPendingIntent;
    private AlarmManager mAlarmManager;
    private ReceiptBroadcastReceiver mReceiptBroadcastReceiver;
    private ReceipMessageQueue mReceipMessageQueue = new ReceipMessageQueue();// 回执消息处理队列
    private LinkedList<GroupChatMsgEntity> mMessageList = new LinkedList<>();
    private boolean notBreak = true;// 销毁线程的标识
    private int mPage = 0;
    private int mUnreadNum = 0;// 未读消息数量
    private boolean isBottom = true;// 消息显示界面是否在底部
    private ContactGroup mGroupObject;
    private DisGroupChatDao mDisGroupChatDao;
    private RelativeLayout layout_chat;
    /**
     * 加载上一页的消息
     */
    protected Runnable mRefreshPrePageRunable = new Runnable() {
        @Override
        public void run() {
            //拉取离线消息
            sendHistoryMsgIQ(GET_HISTORY_COUNT, false);
            int mBeforeMoreCount = mAdapter.getCount();
            refreshUIByPageIndex(null);
            //			mHandler.sendEmptyMessage(0);
            int mAfterMoreCount = mAdapter.getCount();
            int selectIndex = mAfterMoreCount - mBeforeMoreCount;
            mPullToRefreshListView.onRefreshComplete();
            if (selectIndex >= 0) {
                mListView.setSelection(selectIndex);
            }
        }
    };
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_RECEIPT:// 获得回执
                    ReqIQResult reqIq = (ReqIQResult) msg.obj;
                    //如果界面收到发送消息列表数据的回执，则删除列表里该条数据,确保hashmap里存放的是未收到回执的数据
                    if (mChatMsgBeanMap.containsKey(reqIq.getPacketID())) {
                        mChatMsgBeanMap.remove(reqIq.getPacketID());
                    }
                    GroupChatMsgEntity entity = getEntityOfList(mMessageList, reqIq.getPacketID());
                    if (entity != null
                            && entity.getIsSuccess() != GroupChatMsgEntity.SEND_SUCCESS) {
                        entity.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
                        //更新发送消息收到回执后的发送成功时间
                        long time = TimeUtil.getMillisecondByDate(reqIq.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
                        entity.setTime(String.valueOf(time));
                        //更新最近会话表的发送时间
                        if (entity.getPacketId().equals(reqIq.getPacketID())) {
                            mRecentChatDao.updateRecentChatTime(reqIq.getSendTime(), mChatUserNum, Const.CHAT_TYPE_DIS);
                        }
                        mDisGroupChatDao.saveOrUpdate(entity);
                    }
                    notifyAdapterDataSetChanged();
                    break;
                case REFRESH_UI:// 刷新UI
                    //添加最近发送的一条消息到HashMap里，用于回执处理
                    GroupChatMsgEntity groupChatMsgEntity = (GroupChatMsgEntity) msg.obj;
                    mChatMsgBeanMap.put(groupChatMsgEntity.getPacketId(), msg.obj);
                    addToLastOrupdateMessageList(groupChatMsgEntity);
                    notifyAdapterDataSetChanged();
                    break;
                case BROADCAST:// 回执超时处理广播
                    notifyAdapterDataSetChanged();
                    break;
                case RECEIVE_MSG:// 收到消息
                    addToLastOrupdateMessageList((GroupChatMsgEntity) msg.obj);
                    notifyAdapterDataSetChanged();
                    break;
                case GET_HISTORY:
                    addHistoryOrUpdateMessageList(historyMessages);
                    break;
                default:
                    break;
            }
        }

    };
    private String mGroupName;
    private ContactOrgDao mContactOrgDao;
    private User myUserInfo;
    private List<User> mUserList;
    /**
     * 监听名称更改的广播
     */
    private CommonReceiver mCommonReceiver;
    /**
     * 我的唯一id
     */
    private String myUserNo;

    /**
     * Compare the time difference is greater than TIME_INTERVAL minutes
     */
    public static boolean compareTime(long preTime, long nextTime) {
        return (nextTime - preTime) >= TIME_INTERVAL;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = DisChatActivity.this;
        setContentView(R.layout.activity_group_chat_layout);
        initialize();
        initEvent();
        updateUnreadCount();// 更新未读记录条数为0
        initDraft(); // 加载草稿
        refreshDesktopIconBadge(); // 刷新桌面图标的未读消息数量气泡提示
        sendHistoryMsgIQ(GET_HISTORY_COUNT, true);//发送接收历史消息回执
    }

    /**
     * 长按弹出框点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        {
            final GroupChatMsgEntity viewItem = (GroupChatMsgEntity) mAdapter.getItem(position - 1);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            float OldListY = (float) location[1];
            float OldListX = (float) location[0];
            new TipView.Builder(DisChatActivity.this, layout_chat, (int) OldListX + view.getWidth() / 2, (int) OldListY)
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
                                if (viewItem.getIsSuccess() == 2) {
                                    this.dismiss();
                                    ToastUtil.toastAlerMessageCenter(DisChatActivity.this, "发送中..请稍后重试..", 500);
                                } else {
                                    MessageBodyEntity bodyEntity = JSONObject.parseObject(viewItem.getMessage(), MessageBodyEntity.class);
                                    //  转发图片进入新的Activity
                                    if (bodyEntity.getFiles().size() > 0) {
//                                        if (viewItem.getIsSend()== BaseChatMsgEntity.TO_MSG)
                                        MessageBodyEntity entity = JSONObject.parseObject(viewItem.getMessage(), MessageBodyEntity.class);
                                        if ("msg_pc".equalsIgnoreCase(entity.getResource())) {
                                            if (viewItem.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_SUCCESS || viewItem.getIsSuccess() == BaseChatMsgEntity.SEND_SUCCESS) {
                                                startTransmitActivity();
                                            } else {
                                                showToast(R.string.transmit_file_pc_cancel);
                                            }
                                        } else {
                                            startTransmitActivity();
                                        }

                                    } else if (bodyEntity.getImages().size() > 0 || bodyEntity.getContent().length() > 0) {
                                        startTransmitActivity();
                                    } else {
                                        showToast(R.string.p2pChatActivity_transmitNotice);
                                    }
                                }
                            }
                        }

                        private void startTransmitActivity() {
                            Intent intent = new Intent(DisChatActivity.this, TransmitActivity.class);
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


            return true;
        }
    }

    public void initialize() {
        mClipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mContactOrgDao = new ContactOrgDao(mContext);

        myUserNo = LastLoginUserSP.getInstance(mContext)
                .getUserAccount();
        myUserInfo = mContactOrgDao.queryUserInfoByUserNo(myUserNo);
        mGroupObject = (ContactGroup) getIntent().getSerializableExtra(
                Const.INTENT_GROUP_EXTRA_NAME);
        //初始化该讨论组对象数据
        initDisGroupObject();
        layout_chat = (RelativeLayout) findViewById(R.id.layout_chat);
        ImageView mImg_chatInfo = (ImageView) findViewById(R.id.chat_common_title_view_infomation);
        mUnReadTV = (TextView) findViewById(R.id.tv_p2p_chat_tips);
        mSendBtn = (Button) findViewById(R.id.btn_send);
        mEditContentET = (EditText) findViewById(R.id.et_sendmessage);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.chat_pull_refresh_list);
        mFaceRelativeLayout = (FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout);
        mListView = mPullToRefreshListView.getRefreshableView();
        mListView.setOnItemLongClickListener(this);

        mImg_chatInfo.setImageResource(R.mipmap.contact_group_white);
        mFaceRelativeLayout.setOtherButtonsClickListener(this);
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(RECEIPT_BROADCAST);
        mReceiptPendingIntent = PendingIntent.getBroadcast(this, 1, intent, Intent.FILL_IN_ACTION);
        mReceiptBroadcastReceiver = new ReceiptBroadcastReceiver();
        IntentFilter filter = new IntentFilter(RECEIPT_BROADCAST);
        registerReceiver(mReceiptBroadcastReceiver, filter);
        mDisGroupChatDao = new DisGroupChatDao(this);
        mRecentChatDao = new RecentChatDao(this);

        /**优化聊天窗口**/
        mAdapter = new ChatViewMsgAdapter(mContext, Const.CHAT_TYPE_DIS, mChatUserNum);
        mAdapter.setOnMsgResendListener(new ChatViewMsgAdapter.msgResendListener() {
            @Override
            public void onResend(Object obj) {
                send((GroupChatMsgEntity) obj);
            }
        });
        mListView.setAdapter(mAdapter);

        // 消息发送线程，回执处理线程
        ReceiptThread mReceiptThread = new ReceiptThread();
        mReceiptThread.start();

        // 初始状态，从本地获得20条数据，刷新UI
        refreshUIByPageIndex(null);
        mPage = 0;
        mXmppConnManager.addReceiveMessageCallBack(mChatUserNum, this);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT, this);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, this);
        // 回执处理广播
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                RECEIPT_TIME_INTERVAL, RECEIPT_TIME_INTERVAL,
                mReceiptPendingIntent);
    }

    /**
     * 初始化该讨论组对象数据
     */
    private void initDisGroupObject() {
        if (mGroupObject != null) {
            mChatUserNum = mGroupObject.getGroupName();
        } else {
            mChatUserNum = getIntent().getStringExtra("Account");
            mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
        }
        mUserList = mContactOrgDao.queryUsersByGroupName(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
        initDisGroupChatTitle();
    }

    /**
     * 初始化讨论组名称
     */
    private void initDisGroupChatTitle() {
        if (mGroupObject != null) {
            if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
                mGroupName = mGroupObject.getSubject();
            } else {
                mGroupName = mGroupObject.getNaturalName();
            }
        } else {
            mGroupName = "讨论组";
        }
        TextView mTitleTextView = (TextView) findViewById(R.id.chat_common_title_view_name);
        if (mUserList != null) {
            mTitleTextView.setText(mGroupName + "(" + mUserList.size() + ")");
        } else {
            mTitleTextView.setText(mGroupName);
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
                GroupChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                if (chatMsg != null) {
                    chatMsg.setMessage(msg.getBody());
                    mDisGroupChatDao.saveOrUpdate(chatMsg);
                } else {
                    updateChatMsgEntity(type, msg);
                }
            }

            @Override
            public void onAllDone() {
                System.gc();
            }

            @Override
            public void onFailedSend(int type, Message msg, int chatType) {
                //上传失败的回调方法，当上传失败时回调
                GroupChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                msg.setSubject(UploadResponseHandler.mFailedSend);
                if (chatMsg != null) {
                    chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                    mDisGroupChatDao.saveOrUpdate(chatMsg);
                    //				mHandler.sendEmptyMessage(BROADCAST);

                    android.os.Message message = mHandler.obtainMessage();
                    message.obj = chatMsg;
                    message.what = REFRESH_UI;
                    mHandler.sendMessage(message);
                } else {
                    updateChatMsgEntity(type, msg);
                }
            }

            //		@Override
            //		public void onProgressUpdateSend(Message msg,
            //				MessageBodyEntity messageBodyEntity, int progress) {
            //			L.e("progress == "+progress);
            //			messageBodyEntity.setProgress(progress);
            //			msg.setBody(null);
            //			msg.setBody(JSON.toJSONString(messageBodyEntity));
            ////			for (int i = 0; i < 1000; i++) {
            ////				test.add(i+"");
            ////			}
            //			GroupChatMsgEntity chatMsg = getEntityOfList(mMessageList,msg.getPacketID());
            //			if (chatMsg != null) {
            //				chatMsg.setMessage(msg.getBody());
            //				mDisGroupChatDao.saveOrUpdate(chatMsg);
            //				android.os.Message message = mHandler.obtainMessage();
            //				message.obj = chatMsg;
            //				message.what = REFRESH_UI;
            //				mHandler.sendMessage(message);
            //			}
            //		}

        };
        return sendingListener;
    }

    @Override
    protected SendingListener initVoiceSendingListener() {
        return new SendingListener() {

            @Override
            public void onBeforeEachSend(int type, Message msg, int chatType) {
                updateRecentChatList(msg.getSubject());
                msg.setSubject(null);

                GroupChatMsgEntity chatMsg = new GroupChatMsgEntity();
                chatMsg.setPacketId(msg.getPacketID());
                chatMsg.setGroupId(mChatUserNum);
                chatMsg.setChatUserNo(mApplication.mSelfUser.getUserNo());
                chatMsg.setSenderName(mApplication.mSelfUser.getUserName());
                chatMsg.setMessageType(GroupChatMsgEntity.AUDIO_FILE);
                chatMsg.setIsSend(GroupChatMsgEntity.SEND);
                chatMsg.setMessage(msg.getBody());
                chatMsg.setTime(String.valueOf(System.currentTimeMillis()));
                chatMsg.setIsReaded(GroupChatMsgEntity.IS_READED);
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);

                mDisGroupChatDao.saveOrUpdate(chatMsg);

                android.os.Message message = mHandler.obtainMessage();
                message.obj = chatMsg;
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
                GroupChatMsgEntity chatMsg = getEntityOfList(mMessageList, msg.getPacketID());
                msg.setSubject(null);
                msg.setSubject(UploadResponseHandler.mFailedSend);
                if (chatMsg != null) {
                    chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                    mDisGroupChatDao.saveOrUpdate(chatMsg);
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
    }

    private void initEvent() {
        mSendBtn.setOnClickListener(this);

        mPullToRefreshListView
                .setOnRefreshListener(new OnRefreshListener<ListView>() {

                    @Override
                    public void onRefresh(
                            PullToRefreshBase<ListView> refreshView) {
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
                                GroupChatMsgEntity entity = (GroupChatMsgEntity) mAdapter.getItem(i);
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
                            GroupChatMsgEntity tempGroupChatMsg = (GroupChatMsgEntity) mAdapter
                                    .getItem(lastVisiblePos);
                            if (mUnreadList.contains(tempGroupChatMsg)) {
                                mUnreadList.remove(tempGroupChatMsg);
                            }
                        }
                        refreshUnreadNumUI();
                        break;
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        L.v("正在滑动...");
                        // 正在滑动...
                        break;
                    case OnScrollListener.SCROLL_STATE_FLING:
                        L.v("开始滚动...");
                        // 开始滚动...
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

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
        addGroupUpdatedListener();
    }

    /**
     * 添加讨论组信息更改监听器
     */
    private void addGroupUpdatedListener() {
        mCommonReceiver = new CommonReceiver();
        mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {

            @Override
            public void updateGroupData(int mGroupType) {
                if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
//					mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
//					initDisGroupChatTitle();
                    if (!isFinishAcitivity) {
                        mGroupObject = null;
                        initDisGroupObject();
                    } else {
                        isFinishAcitivity = false;
                    }
                }
            }
        });
        mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {

            @Override
            public void IQuitMyGroup(int mGroupType) {
                if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
                    isFinishAcitivity = true;
                    finish();
                }
            }
        });
        IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
        mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
        mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
        registerReceiver(mCommonReceiver, mIntentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        //如果没有该群组，则发送按钮不可用
        if (mGroupObject == null) {
            ToastUtil.toastAlerMessageBottom(mContext, "暂无该讨论组,请手动更新联系人.", 800);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                if (mGroupObject == null) {
                    ToastUtil.toastAlerMessageBottom(mContext, "暂无该讨论组,请手动更新联系人.", 800);
                    return;
                }
                Intent intent = new Intent(this, DisInfoActivity.class);
                intent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, mGroupObject);
                intent.putExtra(DisCreateActivity.DIS_GROUP_ID_KEY, mChatUserNum);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 返回指定packetId在list中的存储位置， 如果返回-1，则表示 list中未存储
     *
     * @param
     * @return
     */
    private GroupChatMsgEntity getEntityOfList(
            LinkedList<GroupChatMsgEntity> List, String packetId) {
        for (int i = (List.size() - 1); i >= 0; i--) {
            GroupChatMsgEntity entity = List.get(i);
            if (packetId != null
                    && packetId.trim().equals(entity.getPacketId().trim())) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //如果没有该群组，则发送按钮不可用
                if (mGroupObject == null) {
                    ToastUtil.toastAlerMessageBottom(mContext, "暂无该讨论组,请手动更新联系人.", 800);
                    return;
                }
                clickSend();
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
    private void addToLastOrupdateMessageList(GroupChatMsgEntity entity) {
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
    private void addHistoryOrUpdateMessageList(List<GroupChatMsgEntity> entitys) {
        if (entitys == null) return;

        ArrayList<Object> curList = new ArrayList<>(mAdapter.getData());
        curList.removeAll(entitys);
        curList.addAll(entitys);

        LinkedList<GroupChatMsgEntity> historys = new LinkedList<>();
        for (int i = 0; i < curList.size(); i++) {
            historys.add((GroupChatMsgEntity) curList.get(i));
        }
        Collections.sort(historys, new TimeUtil.ComparatorMessageTimeDESC<GroupChatMsgEntity>());
        notifyAdapterDataSetChanged(historys);
    }

    /**
     * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
     */
    private void notifyAdapterDataSetChanged(LinkedList<GroupChatMsgEntity> data) {
        List<Object> list = new ArrayList<Object>();
        long preShowTime = 0;
        int length = data.size();
        for (int i = 0; i < length; i++) {
            GroupChatMsgEntity entity = data.get(i);
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
            mDisGroupChatDao.saveGroupChatMsg(entity);
        }
        //如果是第一页就刷新
        if (mPage == 0) {
            mMessageList.clear();
            refreshUIByPageIndex(null);
        }
    }

    /**
     * 本地分页查询数据，刷新UI
     *
     * @param deletedChatMsg 之前删除的某条消息记录，用来更新数据源（可选的，不是删除消息操作调用，可以传null）
     */
    public void refreshUIByPageIndex(GroupChatMsgEntity deletedChatMsg) {
        isBottom = true;
        LinkedList<GroupChatMsgEntity> list = mDisGroupChatDao
                .getChatMsgEntitiesByPage(mChatUserNum, mPage, PAGE_SIZE);
        if (mMessageList.contains(deletedChatMsg)) {
            mMessageList.remove(deletedChatMsg);
        }
        if (list != null && !list.isEmpty()) {
            for (GroupChatMsgEntity mGroupChatMsgEntity : list) {
                if (mMessageList.contains(mGroupChatMsgEntity)) {
                    continue;
                }
                mMessageList.addFirst(mGroupChatMsgEntity);
            }
            mPage++;
        }
        notifyAdapterDataSetChanged();
    }

    @Override
    public void receivedMessage(GroupChatMsgEntity message) {
        if (!isBottom) {
            mUnreadList.add(message);
        }
        // 此方法被线程调用，不能刷新UI
        message.setIsReaded(GroupChatMsgEntity.IS_READED);
        if (message.getMessageType() == BaseChatMsgEntity.AUDIO_FILE)
            message.setIsReaded(P2PChatMsgEntity.IS_NOT_READED);
        mDisGroupChatDao.saveOrUpdate(message);
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
     * 接收回执
     *
     * @param packet
     */
    private List<GroupChatMsgEntity> historyMessages = new ArrayList<>();

    @Override
    public void receivedReqIQResult(ReqIQResult packet) {
        //action 为2表示拉取到历史消息
        if (packet.getNameSpace().equals(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST)) {
            List<HashMap> messagesJson = JSON.parseArray(packet.getResp(), HashMap.class);
            if (messagesJson.size() > 0) {
                for (int i = 0; i < messagesJson.size(); i++) {
                    String xml = String.valueOf(messagesJson.get(i).get("msg"));
                    Message message = OfflineMessageUtil.parserMessage(xml);
                    String jsonBody = MessagePacketListenerImpl.formatHtmlBodyToJson(message.getBody());
                    MessageBodyEntity tempBodyEntity = MessagePacketListenerImpl.getContentByBody(jsonBody);
                    String mFromGroupMemberAccount = JIDUtil.getResouceNameByJID(message.getFrom().trim());
                    String mFromAccount = JIDUtil.getAccountByJID(message.getFrom().trim());
                    GroupChatMsgEntity msg = (GroupChatMsgEntity) MessagePacketListenerImpl.generateMsgObj(mFromAccount, Const.CHAT_TYPE_GROUP,
                            tempBodyEntity, jsonBody, message, mFromGroupMemberAccount);
                    if (mFromGroupMemberAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount())) {
                        msg.setIsSend(0);
                        //如果是自己发送的文件
                        if (msg.getMessageType() == BaseChatMsgEntity.IMAGE || msg.getMessageType() == BaseChatMsgEntity.FILE || msg.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
                            msg.setIsSuccess(BaseChatMsgEntity.DOWNLOAD_NOT_YET);
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
     * 发送获取历史消息iq
     */
    private void sendHistoryMsgIQ(int count, boolean isNewHistory) {
        //拉取最老的一条消息
        LinkedList<GroupChatMsgEntity> historys = new LinkedList<>();
        for (int i = 0; i < mAdapter.getData().size(); i++) {
            historys.add((GroupChatMsgEntity) mAdapter.getData().get(i));
        }
        String sendTime = "";
        GroupChatMsgEntity groupChat = null;
        try {
            Collections.sort(historys, new TimeUtil.ComparatorMessageTimeDESC<GroupChatMsgEntity>());
            //获取当前列表最新的一条消息
            groupChat = historys.get(0);
            if (mPage == 0) {
                groupChat = historys.get(historys.size() - 1);
            }
            sendTime = TimeUtil.getDateByMillisecond(Long.parseLong(groupChat.getTime()), TimeUtil.FORMAT_DATETIME_24_mic);
        } catch (Exception e) {
            sendTime = "";
        }
        if (isNewHistory) {
            sendTime = "";
        }
        ReqIQ iq = new ReqIQ();
        iq.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserNo));
        iq.setTo("admin@" + mXmppConnManager.getServiceName());
        iq.setAction(2);
        iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        HistoryMsg historyMsg = new HistoryMsg();
        historyMsg.setChatType(Const.CHAT_TYPE_GROUP);
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
     * 发送回执消息
     */
    private void sendReceived() {
        Message msg = new Message();
        //封装回执消息
        MessageRevicerEntity revicerEntity = new MessageRevicerEntity(mChatUserNum);
        revicerEntity.setType(Const.CHAT_TYPE_GROUP);
        msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserNo));
        msg.setTo("admin@" + mXmppConnManager.getServiceName());
        msg.setPacketID(msg.getPacketID());
        msg.setType(Type.headline);
        msg.addExtension(revicerEntity);
        //网络可用的时候才能让xmpp发送msg，否则将发送的消息改为发送失败的状态
        if (NetWorkUtil.isNetworkAvailable(mContext)) {
            mXmppConnManager.sendPacket(msg);
        }
        Log.i(mTag, "发送消息回执:" + msg.toXML());
    }

    /**
     * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
     */
    private void notifyAdapterDataSetChanged() {
        List<Object> list = new ArrayList<Object>();
        long preShowTime = 0;
        for (int i = 0; i < mMessageList.size(); i++) {
            GroupChatMsgEntity entity = mMessageList.get(i);
            if (i == 0) {
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

    private void clickSend() {
        isBottom = true;
        mUnreadNum = 0;
        String contString = mEditContentET.getText().toString();
        //如果没有输入消息，不能发送
        if (contString.isEmpty()) {
            return;
        }
        updateRecentChatList(contString);// 更新最近会话列表显示内容
        GroupChatMsgEntity mGroupChatMsgEntity = new GroupChatMsgEntity();
        Message msg = new Message();
        // 封装body
        MessageBodyEntity body = new MessageBodyEntity();
        body.setContent(contString);
        // body.setSendName(System.currentTimeMillis() + "");
        body.setSendName(myUserInfo != null ? myUserInfo.getUserName() : LastLoginUserSP.getLoginName(mContext));
        body.setMsgType(Const.CHAT_TYPE_DIS);
        String jsonString = JSON.toJSONString(body);
        mGroupChatMsgEntity.setGroupId(mChatUserNum);
        mGroupChatMsgEntity.setChatUserNo(myUserInfo != null ? myUserInfo.getUserNo() : myUserNo);
        mGroupChatMsgEntity.setSenderName(mApplication.mSelfUser.getUserName());
        mGroupChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        mGroupChatMsgEntity.setIsReaded(GroupChatMsgEntity.IS_READED);
        mGroupChatMsgEntity.setMessage(jsonString);
        mGroupChatMsgEntity.setMessageType(GroupChatMsgEntity.MESSAGE);
        mGroupChatMsgEntity.setIsSend(GroupChatMsgEntity.SEND);
        mGroupChatMsgEntity.setTime(System.currentTimeMillis() + "");
        mGroupChatMsgEntity.setPacketId(msg.getPacketID());
        L.i(mTag, "packedid==" + msg.getPacketID());
        mEditContentET.setText("");
        // 发送操作
        send(mGroupChatMsgEntity);
    }

    public void send(final GroupChatMsgEntity groupChatMsgEntity) {
        Message msg = new Message();
        android.os.Message message = mHandler.obtainMessage();
        message.obj = groupChatMsgEntity;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);
        switch (groupChatMsgEntity.getMessageType()) {
            case GroupChatMsgEntity.MESSAGE:
                // msg.setBody(GZIPUtil.compress(entity.getMessage()));
                msg.setBody(groupChatMsgEntity.getMessage());
                msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo != null ? myUserInfo.getUserNo() : myUserNo));
                msg.setTo(JIDUtil.getGroupJIDByAccount(mChatUserNum));
                msg.setType(Type.groupchat);
                msg.setPacketID(groupChatMsgEntity.getPacketId());
                try {
                    //网络可用的时候才能让xmpp发送msg，否则将发送的消息改为发送失败的状态
                    if (NetWorkUtil.isNetworkAvailable(mContext)) {
                        mXmppConnManager.sendPacket(msg);
                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                groupChatMsgEntity.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
                                mDisGroupChatDao.saveOrUpdate(groupChatMsgEntity);
                                android.os.Message failMessage = mHandler.obtainMessage();
                                failMessage.obj = groupChatMsgEntity;
                                failMessage.what = REFRESH_UI;
                                mHandler.sendMessage(failMessage);
                            }
                        }, 1000);
                    }
                } finally {
                    mDisGroupChatDao.saveOrUpdate(groupChatMsgEntity);
                }
                break;
            case GroupChatMsgEntity.IMAGE:
                //重发 发送失败的
                if (groupChatMsgEntity.getIsSuccess() == GroupChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(DisChatActivity.this,
                            groupChatMsgEntity, Const.CHAT_TYPE_DIS, mChatUserNum,
                            mSendingListener);
                }
                break;
            case GroupChatMsgEntity.FILE:
                //重发 发送失败的
                if (groupChatMsgEntity.getIsSuccess() == GroupChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(DisChatActivity.this,
                            groupChatMsgEntity, Const.CHAT_TYPE_DIS, mChatUserNum,
                            mSendingListener);
                }
                break;
            case GroupChatMsgEntity.AUDIO_FILE:
                if (groupChatMsgEntity.getIsSuccess() == GroupChatMsgEntity.SEND_ING) {
                    ImageUtil.reSendFailedImgChatMsgBean(DisChatActivity.this,
                            groupChatMsgEntity, Const.CHAT_TYPE_DIS, mChatUserNum,
                            mSendingListener);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        //  Auto-generated method stub
        super.onStop();
        if (mAdapter != null) {
            /**销毁gif引用**/
            mAdapter.destroyGifValue(false);
        }
        handleDraft();
        //当前页面暂停或停止时发送未读消息回执
        sendReceived();
    }

    //下载文件时、更新ui
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseChatMsgEntity eventObj) {
        if (eventObj == null || mMessageList == null) {
            return;
        }
        MessageBodyEntity body = JSON.parseObject(eventObj.getMessage(), MessageBodyEntity.class);
        if (body.getMsgType() == Const.CHAT_TYPE_DIS || body.getMsgType() == Const.CHAT_VOICE_TYPE_DISGROUP) {
            android.os.Message message = mHandler.obtainMessage();
            message.obj = eventObj;
            message.what = REFRESH_UI;
            mHandler.sendMessage(message);
        }
    }

    @Override
    protected void onDestroy() {
        notBreak = false;
        if (mAdapter != null) {
            /**销毁gif引用**/
            mAdapter.destroyGifValue(true);
        }
        mXmppConnManager.removeReceiveMessageCallBack(mChatUserNum);
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST);
        unregisterReceiver(mReceiptBroadcastReceiver);
        mAlarmManager.cancel(mReceiptPendingIntent);
        // 销毁回执线程
        mReceipMessageQueue.putEntity(null);

        NoticesManager.getInstance(this).updateRecentChatList(mChatUserNum,
                Const.CHAT_TYPE_DIS);// 更新最近会话列表
        unregisterReceiver(mCommonReceiver);

        System.gc();
        super.onDestroy();
    }

    /**
     * 草稿的处理
     */
    private void handleDraft() {
        CharSequence draft = mEditContentET.getText();
        RecentChat thisChat = mRecentChatDao.isChatExist(mChatUserNum, Const.CHAT_TYPE_DIS);
        if (!TextUtils.isEmpty(draft) && thisChat == null)  //最近会话列表中没有存在，但已经输入草稿，需保存草稿的情况
        {
            updateRecentChatList("", draft.toString());
        } else if (TextUtils.isEmpty(draft) && thisChat != null && TextUtils.isEmpty(thisChat.getContent()))  //存在于最近会话里面，但没有任何聊天记录，也没有草稿的情况
        {
            mRecentChatDao.deleteRecentChatById(thisChat.getId());  //从最近会话列表里面删除
        } else  //适用于一般情况，直接更新草稿
        {
            mRecentChatDao.updateDraft(mChatUserNum, draft.toString());
        }
    }

    /**
     * 更新未读记录
     */
    private void updateUnreadCount() {
        RecentChat recentChat = mRecentChatDao.isChatExist(mChatUserNum, Const.CHAT_TYPE_DIS);
        if (recentChat != null) {
            int unReadCount = recentChat.getUnReadCount();
            if (unReadCount > 0) {
                sendReceived(); //发送消息回执
            }
        }
        mRecentChatDao.updateUnreadCount(mChatUserNum, Const.CHAT_TYPE_DIS, 0);// 设置未读记录为0
        Intent intent = new Intent();
        intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
        this.sendBroadcast(intent);
    }

    /**
     * 加载草稿
     */
    private void initDraft() {
        //加载草稿
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

    /**
     * 把消息内容更新到最近会话列表
     */
    private void updateRecentChatList(String content) {
        updateRecentChatList(content, "");
    }

    private void updateRecentChatList(String content, String draft) {
        updateRecentChatList(content, new Date(), draft);
    }

    private void updateRecentChatList(String content, Date timeStamp, String draft) {
        RecentChat recentChat = new RecentChat();
        recentChat.setChatType(Const.CHAT_TYPE_DIS);
        recentChat.setUserNo(mChatUserNum);
        //保存名字
        if (mGroupObject == null) {
            mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
            if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
                mGroupName = mGroupObject.getSubject();
            } else {
                mGroupName = mGroupObject.getNaturalName();
            }
        }
        recentChat.setTitle(mGroupName);
        recentChat.setSenderName(mApplication.mSelfUser.getUserName());
        recentChat.setContent(content);
        recentChat.setDateTime(DateFormatUtils.format(timeStamp, TimeUtil.FORMAT_DATETIME_24_mic));
        recentChat.setUnReadCount(0);
        recentChat.setDraft(draft);
        mRecentChatDao.saveRecentChat(recentChat);
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void receivedMessage(P2PChatMsgEntity msg) {

    }

    /**
     * 刷新UI
     */
    protected void updateChatMsgEntity(int type, Message msg) {
        GroupChatMsgEntity chatMsg = new GroupChatMsgEntity();
        chatMsg.setPacketId(msg.getPacketID());
        chatMsg.setGroupId(mChatUserNum);
        chatMsg.setChatUserNo(mApplication.mSelfUser.getUserNo());
        chatMsg.setSenderName(mApplication.mSelfUser.getUserName());
        chatMsg.setMessageType(type);
        chatMsg.setIsSend(GroupChatMsgEntity.SEND);
        chatMsg.setMessage(msg.getBody());
        chatMsg.setTime(String.valueOf(System.currentTimeMillis()));
        chatMsg.setIsReaded(GroupChatMsgEntity.IS_READED);
        if (msg.getSubject() != null) {
            //上传失败
            if (msg.getSubject().equals(UploadResponseHandler.mFailedSend)) {
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                msg.setSubject(null);
            } else {
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
            }
        } else {
            chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        }

        mDisGroupChatDao.saveOrUpdate(chatMsg);

        android.os.Message message = mHandler.obtainMessage();
        message.obj = chatMsg;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);
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
            Log.i("group", "广播接收，处理回执");
            for (GroupChatMsgEntity entity : mMessageList) {
                //如果是发送文本类消息，则处理30s超时回执
                if (entity.getIsSend() == GroupChatMsgEntity.SEND
                        && entity.getMessageType() == GroupChatMsgEntity.MESSAGE) {
                    synchronized (mMessageList) {
                        //如果是正在发送的文本，且超过30秒 则修改状态为失败状态
                        if (entity.getIsSuccess() == GroupChatMsgEntity.SEND_ING
                                && System.currentTimeMillis() - Long.valueOf(entity.getTime()) > RECEIPT_TIME_INTERVAL) {
                            entity.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                            android.os.Message message = mHandler.obtainMessage();
                            message.obj = entity;
                            message.what = REFRESH_UI;
                            mHandler.sendMessage(message);
                        }
                    }
                }
            }
        }


    }

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
        private final LinkedList<ReqIQResult> queue = new LinkedList<ReqIQResult>();

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

        public synchronized void putEntity(ReqIQResult packet) {
            queue.addLast(packet);
            notifyAll();
        }
    }
}
