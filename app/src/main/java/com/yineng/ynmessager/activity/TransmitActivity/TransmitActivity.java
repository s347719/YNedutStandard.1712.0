package com.yineng.ynmessager.activity.TransmitActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.os.AsyncTaskCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactChildOrgActivity;
import com.yineng.ynmessager.activity.contact.ContactGroupOrgActivity;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.activity.picker.content.SendMessageTask;
import com.yineng.ynmessager.activity.picker.file.SendFileTask;
import com.yineng.ynmessager.activity.picker.image.SendImageTask;
import com.yineng.ynmessager.activity.picker.voice.SendVoiceTask;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.dialog.TransmitDialog;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TransmitActivity extends BaseActivity implements SearchContactEditText.onCancelSearchAnimationListener, StatusChangedCallBack, ReceiveReqIQCallBack, ReceiveMessageCallBack, View.OnClickListener {

    private static final String TAG = "TransmitActivity";
    public static String TransEntity = "TransEntity"; //转发的实体类
    public static String TransStatus = "TransStatus";   //转发的图片在服务端是否存在的状态
    public static String TransInfo = "TransInfo"; //转发的图片信息
    public static String TransIsSend = "TransIsSend"; //判断是发送还是接收
    /**
     * XMPP连接管理类实例
     */
    protected XmppConnectionManager mXmppConnManager;

    /**
     * 图片、文件发送过程中的方法回调接口实例
     */
    protected SendingListener mSendingListener;

    /**
     * 语音过程中的方法回调接口实例
     */
    protected SendingListener mVoiceSendingListener;

    //转发的上下文
    private Context mContext;
    private View main_session_contain;

    private int mCurrentStats = 0;
    private final int REFRESH_UI = 5;// 刷新UI保存实例
    /**
     * 显示搜索框动画
     */
    protected final int SHOW_SEARCH_VIEW = 0;
    /**
     * 取消搜索框动画
     */
    protected final int CANCEL_SEARCH_VIEW = 1;
    /**
     *  获得回执的处理
     */
    private final int GET_RECEIPT = 6;
    /**
     * 取消按钮
     */
    private TextView tv_main_session_cancle;
    /**
     * 上下动画滚动的高度
     */
    protected float searchViewY;
    /**
     * 搜索框
     */
    private View searchBox;
    /**
     * 自定义搜索框
     */
    private SearchContactEditText mSearchContactEditText;
    /**
     * 存放回执IQ
     */
    private HashMap<String,Object> beanMap = new HashMap<>();

    //提示框中的提示文字
    private TextView session_alert_text;
//    转发过程中已经存在过消息联系的列表
    private ListView session_listview;
    //顶部的群、组织、讨论组的视图
    private View top_org;
    private View top_group;
    private View top_dis;
    /**
     * 在一页中尽可能全部显示最近的聊天个人信息
     */
    private final int mPageSize = Integer.MAX_VALUE;
    /**
     * 最近聊天信息工具类
     */
    private RecentChatDao mRecentChatDao;
    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;
    /**
     * 群数据库工具类
     */
    private GroupChatDao mGroupChatDao;
    /**
     * 讨论组工具类
     */
    private DisGroupChatDao mDisGroupChatDao;
    /**
     * 联系人数据库
     */
    private ContactOrgDao mContactOrgDao;
    //当前群的信息
    private ContactGroup mGroupObject;
    private String mGroupName;
    /**
     * 当前对方的聊天帐号（该属性在子类实现中被初始化）
     */
    public String mChatUserNum;
    //存储的最近的消息数据
    private LinkedList<RecentChat> mRecentChatsList ;
    // 最近消息的适配器
    private TransmitSessionAdapter transmitSessionAdapter ;
    // 销毁线程的标识
    private boolean notBreak = true;
    // 提示框提示是否发送
    private TransmitDialog transdialog;
    //当前所属聊天类型
    private int chattype;
    //转发的实体类信息
    private TransMsgEntity entity;
    private MessageBodyEntity bodyEntity;
    private TramsScrollView scrollview;
    /**
     * 跳转进入网络设置界面
     */
    protected View.OnClickListener mSettingNetListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case GET_RECEIPT:
                    ReqIQResult reqIq = (ReqIQResult) msg.obj;
                    if (reqIq==null){break;}
                    L.i(mTag,"转发回执:"+reqIq.toString());
                    BaseChatMsgEntity entity= (BaseChatMsgEntity) beanMap.get(reqIq.getPacketID());
                    if (entity==null){break;}
                    long time = TimeUtil.getMillisecondByDate(reqIq.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
                    if (entity.getIsSuccess() != BaseChatMsgEntity.SEND_SUCCESS) {
                        entity.setIsSuccess(BaseChatMsgEntity.SEND_SUCCESS);
                        entity.setTime(String.valueOf(time));
                        //更新最近会话表的发送时间
                        if (entity.getPacketId().equals(reqIq.getPacketID()) ) {
                            mRecentChatDao.updateRecentChatTime(reqIq.getSendTime(),mChatUserNum, Const.CHAT_TYPE_P2P);
                        }
                    }
                    if(chattype== Const.CHAT_TYPE_P2P){
                        mP2PChatMsgDao.saveOrUpdate((P2PChatMsgEntity) entity);
                    }else if (chattype== Const.CHAT_TYPE_GROUP){
                        mGroupChatDao.saveOrUpdate((GroupChatMsgEntity) entity);
                    }else if (chattype== Const.CHAT_TYPE_DIS){
                        mDisGroupChatDao.saveOrUpdate((GroupChatMsgEntity) entity);
                    }
                    beanMap.clear();
                    hideProgressDialog();
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,"转发成功",200);
                    this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1000);
                    break;
                case REFRESH_UI:
                    BaseChatMsgEntity en = (BaseChatMsgEntity) msg.obj;
                    if (!beanMap.containsKey(en.getPacketId())){
                        beanMap.put(en.getPacketId(),en);
                    }else {
                        beanMap.put(en.getPacketId(),en);
                    }
                    break;
                case SHOW_SEARCH_VIEW:
                    mSearchContactEditText.show();
                    main_session_contain.setY(-searchViewY);
                    break;

                case CANCEL_SEARCH_VIEW:
                    main_session_contain.setY(0);
                    break;
                case LoginThread.USER_STATUS_ONLINE:
                    break;

                case LoginThread.USER_STATUS_LOGINED_OTHER:
                    mCurrentStats = 0;
                    hideProgressDialog();
                    break;
                case LoginThread.USER_STATUS_CONNECT_OFF:
                case LoginThread.USER_STATUS_SERVER_SHUTDOWN:
                    mCurrentStats = 0;
                    hideProgressDialog();
//                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,getResources().getString(R.string.main_server_shutdown),500);
                    break;

                case LoginThread.USER_STATUS_NETOFF:// 服务器连接断开
                    mCurrentStats = 0;
                    hideProgressDialog();
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,getResources().getString(R.string.main_badNetwork),500);
                    break;

                case LoginThread.USER_STATUS_OFFLINE:// 下线了
                    mCurrentStats = 0;
                    hideProgressDialog();
                    if (LastLoginUserSP.getInstance(mContext).isLogin()) {
                        ToastUtil.toastAlerMessageCenter(TransmitActivity.this, getResources().getString(R.string.main_alreadyOffline), 500);
                    }
                    break;
                default:
                    break;
            }
        }
    };

//    回执消息
    @Override
    public void receivedReqIQResult(ReqIQResult packet) {

        Message message = mHandler.obtainMessage();
        message.obj = packet;
        message.what = GET_RECEIPT;
        mHandler.sendMessage(message);
    }

    // 接收别人发过来的消息
    @Override
    public void receivedMessage(P2PChatMsgEntity message) {
        message.setIsReaded(P2PChatMsgEntity.IS_READED);
        if (message.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
            message.setIsReaded(P2PChatMsgEntity.IS_NOT_READED);
        }
        mP2PChatMsgDao.saveOrUpdate(message);
    }

    //接收别人发过来的消息
    @Override
    public void receivedMessage(GroupChatMsgEntity msg) {
        msg.setIsReaded(GroupChatMsgEntity.IS_READED);
        if (msg.getMessageType() == BaseChatMsgEntity.AUDIO_FILE) {
            msg.setIsReaded(P2PChatMsgEntity.IS_NOT_READED);
        }
        mGroupChatDao.saveOrUpdate(msg);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);
        initViews();
        entity = (TransMsgEntity) getIntent().getSerializableExtra(TransEntity);
        bodyEntity  = JSONObject.parseObject(entity.getMessage(),MessageBodyEntity.class);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT,TransmitActivity.this);
        //获取最近的聊天数据
        loadRecentChatByPage(0);

        /**
         * 点击了转发页面中下半部分综合了个人聊天，群、讨论组会话
         * 点击item转发图片的点击事件
         */
        session_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //点击将转发的图片发送给最近聊天的对象
                L.i(TAG,"点击了转发中的最近聊天信息title= ："+mRecentChatsList.get(position).getUserNo());
                RecentChat recentChat = mRecentChatsList.get(position);
                mChatUserNum =recentChat.getUserNo();
                chattype = recentChat.getChatType();
                //在转发的时候显示提示框，但是如果是文件转发到群里是不可以的
                //点击的是搜索框出来的对象内容才会有这个dialog出现
                if (mChatUserNum.equals(mApplication.mSelfUser.getUserNo())){
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,"不可以转发给自己",1000);
                }
                else if(bodyEntity.getFiles().size()>0 && chattype== Const.CHAT_TYPE_GROUP ){
                    showToast("文件不可转发到群");
                }
                else {
                    transdialog  = new TransmitDialog(TransmitActivity.this, R.style.MyDialog,new TransmitDialog.OnclickListener(){
                        @Override
                        public void clicklistener(View view) {
                            switch (view.getId()){
                                //点击提示框中的确定按钮
                                case R.id.ok:
                                    mXmppConnManager.addReceiveMessageCallBack(mChatUserNum,TransmitActivity.this);
                                    doSendClick();
                                    transdialog.dismiss();
                                    showProgressDialog("");
                                    onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
                                    break;
                                default:

                                    break;
                            }
                        }


                    },bodyEntity,recentChat.getTitle(),entity.getIsSend());
                    transdialog.show();
                }


            }
        });
    }

    @Override
    protected void onDestroy() {

        notBreak = false;
        if (mChatUserNum!=null) {
            mXmppConnManager.removeReceiveMessageCallBack(mChatUserNum);
        }
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);
        System.gc();
        super.onDestroy();
    }
    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }

    //初始化视图
    private void initViews() {
        mContext = this;
        mRecentChatsList = new LinkedList<>();
        mXmppConnManager = XmppConnectionManager.getInstance();
        mSendingListener = initSendListener();
        mVoiceSendingListener = initVoiceSendingListener();
        main_session_contain = findViewById(R.id.main_session_contain);
        tv_main_session_cancle = (TextView) findViewById(R.id.tv_main_session_cancle);
        tv_main_session_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        session_alert_text = (TextView)findViewById(R.id.tv_main_session_alert_text);
        session_listview = (ListView) findViewById(R.id.session_listview);
        transmitSessionAdapter= new TransmitSessionAdapter(TransmitActivity.this);
        transmitSessionAdapter.setmSessionDatas(mRecentChatsList);
        session_listview.setAdapter(transmitSessionAdapter);
        //listview是重写的会全部展示，所以scrollview需要从开始的时候置顶,因为滑动是要在消息栈内处理的
        // 在布局很多的情况下可能会偶尔出现布局在底部的情况，所以在这里使用一个线程来处理
        scrollview = (TramsScrollView) findViewById(R.id.scrollview);
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        top_org = findViewById(R.id.top_org);
        top_group = findViewById(R.id.top_group);
        top_dis = findViewById(R.id.top_dis);
        top_org.setOnClickListener(this);
        top_group.setOnClickListener(this);
        top_dis.setOnClickListener(this);

        mRecentChatDao = new RecentChatDao(this);
        mP2PChatMsgDao = new P2PChatMsgDao(this);
        mGroupChatDao = new GroupChatDao(this);
        mDisGroupChatDao = new DisGroupChatDao(this);
        mContactOrgDao =  new ContactOrgDao(mContext);
        searchBox = findViewById(R.id.searchBox);
        mSearchContactEditText = new SearchContactEditText(TransmitActivity.this);
        mSearchContactEditText.setSessionFragment(false, false, false,true);
        //搜索框点击对象传回的当前类型和接收者的id
        mSearchContactEditText.setClickSendImageListener(new SearchContactEditText.clickSendImageListener() {
            @Override
            public void clickSendImage(String nameId,String name,int type) {
                mChatUserNum = nameId;
                chattype = type;
                if (mChatUserNum.equals(mApplication.mSelfUser.getUserNo())){
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,"不可以转发给自己",1000);
                }
                else if(bodyEntity.getFiles().size()>0 && chattype== Const.CHAT_TYPE_GROUP ){
                    showToast(getString(R.string.trans_group_dialog));
                }else {
                    transdialog = new TransmitDialog(TransmitActivity.this, R.style.MyDialog, new TransmitDialog.OnclickListener() {
                        @Override
                        public void clicklistener(View view) {
                            switch (view.getId()) {
                                //点击提示框中的确定按钮发送转发信息
                                case R.id.ok:
                                    mXmppConnManager.addReceiveMessageCallBack(mChatUserNum, TransmitActivity.this);
                                    doSendClick();
                                    transdialog.dismiss();
                                    showProgressDialog("");
                                    onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
                                    break;
                                default:
                                    break;
                            }
                        }
                    }, bodyEntity, name,entity.getIsSend());
                    transdialog.show();
                }
            }
        });
        mSearchContactEditText.setOnCancelSearchAnimationListener(this);//搜索框添加的取消动画监听
        searchBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View paramView) {
                showSearchContactAnimation();
            }
        });
        //添加当前状态监听
        XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
        onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());


    }

    /**
     * 初始化语音发送监听
     * @return
     */
    private SendingListener initVoiceSendingListener() {
        SendingListener listener = new SendingListener() {
            @Override
            public void onBeforeEachSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {

                switch (chatType) {
                    case Const.CHAT_TYPE_P2P:
                        updateRecentChatListP2p(msg.getSubject(), new Date(), chatType);
                        msg.setSubject(null);
                        updateChatp2pMsgEntity(type, msg);
                        break;


                    case Const.CHAT_TYPE_DIS:
                        updateRecentChatListDis(msg.getSubject(), new Date(), chatType);
                        msg.setSubject(null);
                        updateChatDisMsgEntity(type, msg);

                        break;
                }
            }

            @Override
            public void onEachDone(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {

            }

            @Override
            public void onAllDone() {

            }

            @Override
            public void onFailedSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {

                switch (chatType){
                    case Const.CHAT_TYPE_P2P:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatp2pMsgEntity(type, msg);
                        break;
                    case Const.CHAT_TYPE_DIS:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatDisMsgEntity(type, msg);
                        break;
                }
                hideProgressDialog();
                if (mCurrentStats == LoginThread.USER_STATUS_ONLINE){
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,"转发失败",200);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1000);
                }else {
                    finish();
                }
            }
        };
        return listener;
    }

    /**
     * 初始化发送图片文件文字监听
     * @return
     */
    private SendingListener initSendListener() {

        SendingListener sendingListener = new SendingListener()
        {
            @Override
            public void onBeforeEachSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType){
                    case Const.CHAT_TYPE_P2P:
                        updateRecentChatListP2p(msg.getSubject(),new Date(),chatType);
                        msg.setSubject(null);
                        updateChatp2pMsgEntity(type, msg);
                        break;

                    case Const.CHAT_TYPE_GROUP:
                        updateRecentChatListGroup(msg.getSubject(),new Date(),chatType);
                        msg.setSubject(null);
                        updateChatGroupMsgEntity(msg);
                        break;

                    case Const.CHAT_TYPE_DIS:
                        updateRecentChatListDis(msg.getSubject(),new Date(),chatType);
                        msg.setSubject(null);
                        updateChatDisMsgEntity(type, msg);

                        break;

                }
            }

            @Override
            public void onEachDone(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType){
                    case Const.CHAT_TYPE_P2P:
                        updateChatp2pMsgEntity(type, msg);
                        break;

                    case Const.CHAT_TYPE_GROUP:
                        updateChatGroupMsgEntity(msg);
                        break;

                    case Const.CHAT_TYPE_DIS:
                        updateChatDisMsgEntity(type, msg);
                        break;
                    default:
                        break;
                }

            }
            // 在发送图片异步线程调取 onAllDone方法之后我们取消销毁这个activity 返回上层视图
            @Override
            public void onAllDone()
            {
            }

            @Override
            public void onFailedSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType){
                    case Const.CHAT_TYPE_P2P:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatp2pMsgEntity(type, msg);
                        break;

                    case Const.CHAT_TYPE_GROUP:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatGroupMsgEntity(msg);
                        break;

                    case Const.CHAT_TYPE_DIS:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatDisMsgEntity(type, msg);
                        break;

                }
                hideProgressDialog();
                if (mCurrentStats == LoginThread.USER_STATUS_ONLINE){
                    ToastUtil.toastAlerMessageCenter(TransmitActivity.this,"转发失败",200);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    },1000);
                }else {
                    finish();
                }


            }

        };
        return sendingListener;
    }



    /**
     * 保存讨论组当前信息
     */
    private void updateChatDisMsgEntity(int type, org.jivesoftware.smack.packet.Message msg) {
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
            }else {
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
            }
        } else {
            chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        }

        mDisGroupChatDao.saveOrUpdate(chatMsg);
        Message message = mHandler.obtainMessage();
        message.obj = chatMsg;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);

    }


    /**
     * 保存最近的讨论组到最近聊天库中
     */
    private void updateRecentChatListDis(String content, Date temp, int chatType) {

        RecentChat recentChat = new RecentChat();
        recentChat.setChatType(Const.CHAT_TYPE_DIS);
        recentChat.setUserNo(mChatUserNum);
        mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_GROUP_TYPE);
        //保存名字
        if (mGroupObject == null) {
            mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
            if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
                mGroupName = mGroupObject.getSubject();
            } else {
                mGroupName = mGroupObject.getNaturalName();
            }
        }else {
            mGroupName = mGroupObject.getNaturalName();
        }
        recentChat.setTitle(mGroupName);
        recentChat.setSenderName(mApplication.mSelfUser.getUserName());
        recentChat.setContent(content);
        recentChat.setDateTime(DateFormatUtils.format(temp, TimeUtil.FORMAT_DATETIME_24_mic));
        recentChat.setUnReadCount(0);
        recentChat.setDraft("");
        mRecentChatDao.saveRecentChat(recentChat);
    }

    /**
     * 保存群聊天信息
     */
    private void updateRecentChatListGroup(String content, Date timeStamp, int chatType) {

        RecentChat recentChat = new RecentChat();
        recentChat.setChatType(Const.CHAT_TYPE_GROUP);
        recentChat.setUserNo(mChatUserNum);
        mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_GROUP_TYPE);
        //保存名字
        if (mGroupObject == null) {
            mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
            if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
                mGroupName = mGroupObject.getSubject();
            } else {
                mGroupName = mGroupObject.getNaturalName();
            }
        }else {
            mGroupName = mGroupObject.getNaturalName();
        }
        recentChat.setSenderName(mApplication.mSelfUser.getUserName());
        recentChat.setTitle(mGroupName);
        recentChat.setContent(content);
        recentChat.setDateTime(DateFormatUtils.format(timeStamp, TimeUtil.FORMAT_DATETIME_24_mic));
        recentChat.setUnReadCount(0);
        recentChat.setDraft("");
        mRecentChatDao.saveRecentChat(recentChat);

    }

    /**
     * 更新最近聊天信息 p2p类型
     */
    private void updateRecentChatListP2p(String content, Date date, int chatType) {

        RecentChat recentChat = new RecentChat();
        recentChat.setChatType(chatType);
        recentChat.setUserNo(mChatUserNum);
        recentChat.setTitle(mRecentChatDao.getUserNameByUserId(mChatUserNum, chatType));
        recentChat.setContent(content);
        recentChat.setDateTime(DateFormatUtils.format(date, TimeUtil.FORMAT_DATETIME_24_mic));
        recentChat.setUnReadCount(0);
        recentChat.setDraft("");
        mRecentChatDao.saveRecentChat(recentChat);
    }

    /**
     * 保存p2p 聊天信息
     */
    protected void updateChatp2pMsgEntity(int type, org.jivesoftware.smack.packet.Message msg) {
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
            }else {
                p2PChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
            }
        } else {
            p2PChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        }
        mP2PChatMsgDao.saveOrUpdate(p2PChatMsgEntity);

        Message message = mHandler.obtainMessage();
        message.obj = p2PChatMsgEntity;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);

    }

    /**
     * 保存群消息
     */
    protected void updateChatGroupMsgEntity(org.jivesoftware.smack.packet.Message msg) {
        GroupChatMsgEntity chatMsg = new GroupChatMsgEntity();
        chatMsg.setPacketId(msg.getPacketID());
        chatMsg.setGroupId(mChatUserNum);
        chatMsg.setChatUserNo(mApplication.mSelfUser.getUserNo());
        chatMsg.setSenderName(mApplication.mSelfUser.getUserName());
        chatMsg.setMessageType(GroupChatMsgEntity.IMAGE);
        chatMsg.setIsSend(GroupChatMsgEntity.SEND);
        chatMsg.setMessage(msg.getBody());
        chatMsg.setTime(String.valueOf(System.currentTimeMillis()));
        chatMsg.setIsReaded(GroupChatMsgEntity.IS_READED);
        if (msg.getSubject() != null) {
            //上传失败
            if (msg.getSubject().equals(UploadResponseHandler.mFailedSend)) {
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
                msg.setSubject(null);
            }else {
                chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
            }
        } else {
            chatMsg.setIsSuccess(GroupChatMsgEntity.SEND_ING);
        }
        mGroupChatDao.saveOrUpdate(chatMsg);
        Message message = mHandler.obtainMessage();
        message.obj = chatMsg;
        message.what = REFRESH_UI;
        mHandler.sendMessage(message);
    }

    //获取最近的聊天数据
    private void loadRecentChatByPage(int pages)
    {
        new AsyncTask<String, Integer, List<RecentChat>>() {
            private int mPageIndex = 0;

            // 访问数据库前执行
            @Override
            protected void onPreExecute()
            {
                // mPullToRefreshListView.setRefreshing();
            }
            @Override
            protected List<RecentChat> doInBackground(String... params)
            {
                mPageIndex = Integer.valueOf(params[0]);
                return mRecentChatDao.queryRecentChatPage(mPageIndex,mPageSize);
            }

            // 返回数据
            @Override
            protected void onPostExecute(List<RecentChat> list)
            {

                L.e(TAG,"list的数据："+list.size());
                if (list.size() == 0) {
                    session_listview.setVisibility(View.INVISIBLE);
                } else
                {
                    for(RecentChat chat : list)
                    {
                        if(!mRecentChatsList.contains(chat))
                        {
                            //在这里进行分辨提取只提取个人对话，群对话，讨论对话三种
                            int chatType = chat.getChatType();
                            if ( chatType== Const.CHAT_TYPE_P2P || chatType == Const.CHAT_TYPE_GROUP || chatType == Const.CHAT_TYPE_DIS){
                                mRecentChatsList.addLast(chat);
                            }
                        }
                    }
                    session_listview.setVisibility(View.VISIBLE);
                    //将取到的数据放入适配器和listview中
                    transmitSessionAdapter.setmSessionDatas(mRecentChatsList);
                    transmitSessionAdapter.notifyDataSetChanged();

                }
            }
        }.execute(String.valueOf(pages));
    }


    private boolean isShowSearchEditText = false;
    TranslateAnimation mShowAnimation = null;
    TranslateAnimation mCancelAnimation = null;
    private Animation.AnimationListener mShowAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation)
        {

        }

        @Override
        public void onAnimationRepeat(Animation animation)
        {

        }

        @Override
        public void onAnimationEnd(Animation animation)
        {
            if(isShowSearchEditText)
            {
                mHandler.sendEmptyMessage(SHOW_SEARCH_VIEW);
            }else
            {
                mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
            }
        }
    };
    public void showSearchContactAnimation()
    {
        isShowSearchEditText = true;
        LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams)searchBox.getLayoutParams();
        searchViewY = searchBox.getY() - (float)etParamTest.topMargin;
        mShowAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
        mShowAnimation.setDuration(200);
        mShowAnimation.setAnimationListener(mShowAnimationListener);
        main_session_contain.startAnimation(mShowAnimation);
    }

    @Override
    public void cancelSearchContactAnimation()
    {
        isShowSearchEditText = false;
        mSearchContactEditText.dismiss();
        mCancelAnimation = new TranslateAnimation(0, 0, 0, searchViewY);
        mCancelAnimation.setDuration(200);
        mCancelAnimation.setAnimationListener(mShowAnimationListener);
        main_session_contain.startAnimation(mCancelAnimation);

    }

    @Override
    public void onStatusChanged(int status) {
        if(mCurrentStats != status)
        {
            mHandler.sendEmptyMessage(status);
            mCurrentStats = status;
        }
    }

    /**
     * 顶部三个选择项的点击事件
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case  R.id.top_org:
                /****************通过对象去获取组织机构*********************/
                Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
                childOrgIntent.putExtra("isTransmit",true);
                childOrgIntent.putExtra(TransStatus,entity.getIsSuccess());
                childOrgIntent.putExtra(TransInfo,entity.getMessage());
                childOrgIntent.putExtra(TransIsSend,entity.getIsSend());
                ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId("0");
                if (tempList != null) {
                    childOrgIntent.putExtra("childOrgList", tempList);
                }
                // 表示进入子组织机构界面
                OrganizationTree firstOrganizationTree = new OrganizationTree("0", "-1", getResources().getString(R.string.main_org), 0, "0", 0);
                childOrgIntent.putExtra("parentOrg",firstOrganizationTree);
                startActivity(childOrgIntent);
                break;

            case  R.id.top_group:
                if (bodyEntity.getFiles().size()>0){
                    showToast(R.string.trans_group_dialog);
                }else {
                    ArrayList<ContactGroup> mContactGroupList;
                    Intent groupIntent = new Intent(mContext, ContactGroupOrgActivity.class);
                    groupIntent.putExtra("isTransmit", true);
                    groupIntent.putExtra(TransStatus, entity.getIsSuccess());
                    groupIntent.putExtra(TransInfo, entity.getMessage());
                    groupIntent.putExtra(TransIsSend, entity.getIsSend());
                    groupIntent.putExtra("childGroupTitle", getResources().getString(R.string.main_group));
                    mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(8);
                    groupIntent.putExtra("groupType", 8);
                    if (mContactGroupList != null) {
                        groupIntent.putExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME, mContactGroupList);
                    }
                    startActivity(groupIntent);
                }
                break;

            case  R.id.top_dis:
                ArrayList<ContactGroup> mContactDisList;
                Intent disIntent = new Intent(mContext, ContactGroupOrgActivity.class);
                disIntent.putExtra("isTransmit",true);
                disIntent.putExtra(TransStatus,entity.getIsSuccess());
                disIntent.putExtra(TransInfo,entity.getMessage());
                disIntent.putExtra(TransIsSend,entity.getIsSend());
                disIntent.putExtra("childGroupTitle", getResources().getString(R.string.main_discussion));
                mContactDisList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(9);
                disIntent.putExtra("groupType", 9);
                if (mContactDisList != null) {
                    disIntent.putExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME, mContactDisList);
                }
                startActivity(disIntent);
                break;
            default:
                break;
        }
    }
    /**
     * 点击提示框中的确定按钮进行的转发操作
     */
    private void doSendClick() {
        if (bodyEntity.getImages().size()>0) {
            //转发图片
            if (entity.getIsSuccess() == BaseChatMsgEntity.SEND_SUCCESS) {//当前的图片状态是发送成功的，直接发送message包的信息
                L.i(TAG, "图片在服务端存在再转发" + entity.getMessage());
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, entity.getMessage());
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            } else {
                L.i(TAG, "图片在服务端不存在再转发会上传图片文件");
                List<ImageFile> imageFileList = new ArrayList<ImageFile>();
                ImageFile imageFile = new ImageFile(bodyEntity.getImages().get(0).getSdcardPath());
                imageFileList.add(imageFile);
                // 启动发送图片的AsyncTask
                SendImageTask sendImageTask = new SendImageTask(mXmppConnManager, chattype, mChatUserNum);
                sendImageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(sendImageTask, imageFileList);
            }
        }else if (bodyEntity.getFiles().size()>0){
            //转发文件
            if (entity.getIsSend() == BaseChatMsgEntity.COM_MSG || (entity.getIsSend() == BaseChatMsgEntity.TO_MSG && (entity.getIsSuccess() == BaseChatMsgEntity.SEND_SUCCESS||entity.getIsSuccess() == BaseChatMsgEntity.DOWNLOAD_SUCCESS))) {
                L.i(TAG, "文件在服务端存在再转发" + entity.getMessage());
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, entity.getMessage());
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            } else {
                SendFileTask sendFileTask = new SendFileTask(mXmppConnManager, chattype, mChatUserNum);
                sendFileTask.setSendFileListener(mSendingListener);
                HashSet<File> selectedFiles = new HashSet<File>();
                selectedFiles.add(new File(bodyEntity.getFiles().get(0).getSdcardPath()));
                AsyncTaskCompat.executeParallel(sendFileTask, selectedFiles);
            }
        }else if (bodyEntity.getContent().length()>0){
            //转发文字
            if (entity.getIsSend()== BaseChatMsgEntity.COM_MSG || (entity.getIsSend()== BaseChatMsgEntity.TO_MSG && entity.getIsSuccess()== BaseChatMsgEntity.SEND_SUCCESS)){
                L.i(TAG, "文字在服务端存在再转发" + entity.getMessage());
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, entity.getMessage());
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            }
            else {
                SendMessageTask sendMessageTask = new SendMessageTask(mXmppConnManager,chattype,mChatUserNum);
                sendMessageTask.setSendMessageListener(mSendingListener);
                String content = bodyEntity.getContent();
                AsyncTaskCompat.executeParallel(sendMessageTask,content);
            }
        }else if (bodyEntity.getVoice()!=null){
            Set<File> voiceFiles = new HashSet<>();
            voiceFiles.add(new File(FileUtil.getUserSDPath(false,bodyEntity.getVoice().getId())));
            SendVoiceTask sendVoiceTask = new SendVoiceTask(mXmppConnManager, chattype, mChatUserNum);
            sendVoiceTask.setSendFileListener(mVoiceSendingListener);
            AsyncTaskCompat.executeParallel(sendVoiceTask, voiceFiles);
        }
    }
}
