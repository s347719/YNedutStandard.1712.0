package com.yineng.ynmessager.activity.contact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.TransmitActivity.TransSendImageOrFileOrMessageTask;
import com.yineng.ynmessager.activity.TransmitActivity.TransmitActivity;
import com.yineng.ynmessager.activity.dissession.OrgPathAdapter;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.activity.picker.content.SendMessageTask;
import com.yineng.ynmessager.activity.picker.file.SendFileTask;
import com.yineng.ynmessager.activity.picker.image.SendImageTask;
import com.yineng.ynmessager.adapter.ContactCommonAdapter;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.ContactActivityManager;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.onServiceNoticeListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceivePresenceCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.dialog.TransmitDialog;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.jivesoftware.smack.packet.Presence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ContactChildOrgActivity extends BaseActivity implements
        ReceivePresenceCallBack, onCancelSearchAnimationListener, ReceiveReqIQCallBack, StatusChangedCallBack {

    private final int VIEW_ENTER = 0;
    private final int VIEW_BACK = 1;
    private final int GET_RECEIPT = 6;// 获得回执的处理
    private final int REFRESH_UI = 5;// 刷新UI保存实例
    private HashMap<String, Object> beanMap = new HashMap<>();
    private List<OrganizationTree> mChildOrg;
    private List<User> mChildOrgUser;
    private List<Object> mChildContactObjectList = new ArrayList<Object>();
    private String mParentOrgNo;
    private ListView mContactOrgLV;
    private Context mContext;
    private TextView mContactOrgTitleTV;
    private OrganizationTree mParentOrg;
    private ContactOrgDao mContactOrgDao;
    private ContactCommonAdapter mChildContactAdapter;
    private BroadcastReceiver mCloseAllReceiver;

    private RelativeLayout mContactRelativeLayout;
    /*** 搜索联系人功能 ***/

    private int mCurrentStats = -1;
    /**
     * 显示搜索框动画
     */
    protected final int SHOW_SEARCH_VIEW = 0;
    /**
     * 取消搜索框动画
     */
    protected final int CANCEL_SEARCH_VIEW = 1;
    /**
     * 默认用于显示的搜索框
     */
    private RelativeLayout mRel_searchBox;
    /**
     * 上下动画滚动的高度
     */
    protected float searchViewY;
    //是否来自转发
    private boolean isTransmit = false;
    //当前转发图片或者文件状态
    private int transState;
    //转发图片的内容消息
    private String transInfo;
    //判断是接受者还是发送者
    private int transIsSend;
    private MessageBodyEntity bodyEntity;

    /**
     * XMPP连接管理类实例
     */
    protected XmppConnectionManager mXmppConnManager;

    /**
     * 图片、文件发送过程中的方法回调接口实例
     */
    protected SendingListener mSendingListener;

    // 提示框提示是否发送
    private TransmitDialog transdialog;
    /**
     * /**
     * 最近聊天信息工具类
     */
    private RecentChatDao mRecentChatDao;
    /**
     * 消息数据库工具
     */
    private P2PChatMsgDao mP2PChatMsgDao;
    private int chattype;

    /**
     * 搜索框
     */
    private SearchContactEditText mSearchContactEditText;
    private TextView mContactOrgBackBT;
//	private ImageView mImg_userCenter;

    /**
     * 组织机构快速跳转的弹出界面
     */
    private PopupWindow mOrgTitlePopWindow;
    /**
     * 弹出界面的路径列表
     */
    private ListView mOrgTitlePopwinList;
    private View view;
    /**
     * 弹出搜索框的动画
     */
    private TranslateAnimation showAnimation = null;
    /**
     * 隐藏搜索框的动画
     */
    private TranslateAnimation cancelAnimation = null;

    private CommonReceiver mCommonReceiver;
    /**
     * 当前对方的聊天帐号（该属性在子类实现中被初始化）
     */
    public String mChatUserNum;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {

                case GET_RECEIPT:
                    ReqIQResult reqIq = (ReqIQResult) msg.obj;
                    if (reqIq == null) {
                        break;
                    }
                    L.i(mTag, "转发回执：" + reqIq.toString());
                    BaseChatMsgEntity entity = (BaseChatMsgEntity) beanMap.get(reqIq.getPacketID());
                    if (entity == null) {
                        break;
                    }
                    long time = TimeUtil.getMillisecondByDate(reqIq.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
                    if (entity != null && entity.getIsSuccess() != BaseChatMsgEntity.SEND_SUCCESS) {
                        entity.setIsSuccess(BaseChatMsgEntity.SEND_SUCCESS);
                        entity.setTime(String.valueOf(time));
                        //更新最近会话表的发送时间
                        if (entity.getPacketId().equals(reqIq.getPacketID())) {
                            mRecentChatDao.updateRecentChatTime(reqIq.getSendTime(), mChatUserNum, Const.CHAT_TYPE_P2P);
                        }
                        if (chattype == Const.CHAT_TYPE_P2P) {
                            mP2PChatMsgDao.saveOrUpdate((P2PChatMsgEntity) entity);
                        }
                    }
                    beanMap.clear();
                    hideProgressDialog();
                    ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this, "转发成功", 200);
                    this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // 消除转发图片经过的Activity
                            BaseActivity.deleteContactChildActivity();
                            finish();
                        }
                    }, 1000);
                    break;

                case REFRESH_UI:
                    BaseChatMsgEntity en = (BaseChatMsgEntity) msg.obj;
                    if (!beanMap.containsKey(en.getPacketId())) {
                        beanMap.put(en.getPacketId(), en);
                    }
                    break;
                case SHOW_SEARCH_VIEW:
                    mSearchContactEditText.show();
                    mContactRelativeLayout.setY(-searchViewY);
                    break;
                case CANCEL_SEARCH_VIEW:
                    mContactRelativeLayout.setY(0);
                    break;
                case Const.USER_STATUS_CHANGED:
                    mChildContactAdapter.notifyDataSetChanged();
                    break;

                case LoginThread.USER_STATUS_ONLINE:
                    break;

                case LoginThread.USER_STATUS_LOGINED_OTHER:
                    mCurrentStats = -1;
                    hideProgressDialog();
                    break;
                case LoginThread.USER_STATUS_CONNECT_OFF:
                case LoginThread.USER_STATUS_SERVER_SHUTDOWN:
                    mCurrentStats = -1;
                    hideProgressDialog();
//					ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this,getResources().getString(R.string.main_server_shutdown),500);
                    break;
                case LoginThread.USER_STATUS_NETOFF:// 服务器连接断开
                    mCurrentStats = -1;
                    hideProgressDialog();
                    ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this, getResources().getString(R.string.main_badNetwork), 500);
                    break;

                case LoginThread.USER_STATUS_OFFLINE:// 下线了
                    mCurrentStats = -1;
                    hideProgressDialog();
                    if (LastLoginUserSP.getInstance(mContext).isLogin()) {
                        ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this, getResources().getString(R.string.main_alreadyOffline), 500);
                    }
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = ContactChildOrgActivity.this;
        initData();
        setContentView(R.layout.fragment_main_contact_layout);
        findViews();
        initListenerByCommonAdapter();
    }

    private void initData() {
        Intent orgDataIntent = getIntent();

        isTransmit = orgDataIntent.getBooleanExtra("isTransmit", false);
        transIsSend = getIntent().getIntExtra(TransmitActivity.TransIsSend, BaseChatMsgEntity.COM_MSG);
        transState = orgDataIntent.getIntExtra(TransmitActivity.TransStatus, BaseChatMsgEntity.SEND_SUCCESS);//得到图片转发前的状态
        transInfo = orgDataIntent.getStringExtra(TransmitActivity.TransInfo);//得到转发信息
        bodyEntity = JSON.parseObject(transInfo, MessageBodyEntity.class);
        mParentOrg = (OrganizationTree) orgDataIntent
                .getSerializableExtra("parentOrg");
        if (mParentOrg == null) {
            // orgDataIntent.getStringExtra("childOrgTitle");
            mParentOrgNo = orgDataIntent.getStringExtra("parentOrgNo");
        }

        int isViewBack = orgDataIntent.getIntExtra("isBack", 0);
        if (isViewBack == 0) {
            ContactActivityManager.mTitleOrgList.add(mParentOrg);
        }

        mChildOrg = (List<OrganizationTree>) orgDataIntent
                .getSerializableExtra("childOrgList");
        mChildOrgUser = (List<User>) orgDataIntent
                .getSerializableExtra("childOrgUser");
        if (mChildOrgUser != null) {
            Log.e("childOrgUser",
                    "childOrgUser.size() == " + mChildOrgUser.size());
            mChildContactObjectList.addAll(mChildOrgUser);
        } else {
            mChildOrgUser = new ArrayList<User>();
        }
        if (mChildOrg != null) {
            Log.e("childOrg", "childOrg.size() == " + mChildOrg.size());
            mChildContactObjectList.addAll(mChildOrg);
        } else {
            mChildOrg = new ArrayList<OrganizationTree>();
        }
        mContactOrgDao = new ContactOrgDao(mContext);

        mXmppConnManager = XmppConnectionManager.getInstance();
        mSendingListener = initSendListener();

        mRecentChatDao = new RecentChatDao(this);
        mP2PChatMsgDao = new P2PChatMsgDao(this);
    }

    private void findViews() {
        mContactOrgTitleTV = (TextView) findViewById(R.id.contact_org_title);
        mContactOrgBackBT = (TextView) findViewById(R.id.bt_contact_org_title_return_button);
//		mImg_userCenter = (ImageView) findViewById(R.id.main_img_contact_showUserCenter);

        mContactOrgBackBT.setVisibility(View.VISIBLE);
//		mImg_userCenter.setVisibility(View.INVISIBLE);
        if (!mParentOrg.getOrgNo().equals("0")) {
            mContactOrgBackBT.setText(getString(R.string.contactChildOrg_previous));
        }
        mContactOrgTitleTV.setText(mParentOrg.getOrgName());
        mContactOrgTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                getResources().getDrawable(R.mipmap.contact_arrow_down), null);
        mContactOrgLV = (ListView) findViewById(R.id.contact_org_listview);
        findSearchContactView();
        //添加当前状态监听
        XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
        onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
    }

    private void findSearchContactView() {
        mSearchContactEditText = new SearchContactEditText(mContext);
        if (isTransmit) {
            mSearchContactEditText.setSessionFragment(false, false, false, true);
        }
        //搜索框点击对象传回的当前类型和接收者的id
        mSearchContactEditText.setClickSendImageListener(new SearchContactEditText.clickSendImageListener() {
            @Override
            public void clickSendImage(String nameId, String name, int type) {
                mChatUserNum = nameId;
                chattype = type;
                transdialog = new TransmitDialog(ContactChildOrgActivity.this, R.style.MyDialog, new TransmitDialog.OnclickListener() {
                    @Override
                    public void clicklistener(View view) {
                        switch (view.getId()) {
                            //点击提示框中的确定按钮
                            case R.id.ok:
                                doSendClick();
                                transdialog.dismiss();
                                showProgressDialog("");
                                onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
                                break;
                            default:

                                break;
                        }
                    }


                }, bodyEntity, name, transIsSend);
                transdialog.show();
            }
        });
        mRel_searchBox = (RelativeLayout) findViewById(R.id.searchBox);
        mContactRelativeLayout = (RelativeLayout) findViewById(R.id.ll_contact_org_frame);
    }

    /**
     * 获取数据
     */
    private void initListenerByCommonAdapter() {
        initSearchContactViewListener();
        mContactOrgTitleTV.setOnClickListener(new OnClickListener() {

            View actionbar = findViewById(R.id.rl_contact_org_title_layout);
            LayoutInflater inflater = getLayoutInflater();

            @Override
            public void onClick(View v) {
                showWindow(actionbar, inflater);
            }
        });
        mChildContactAdapter = new ContactCommonAdapter(mContext, mChildContactObjectList);
        mContactOrgLV.setAdapter(mChildContactAdapter);

        mContactOrgLV.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Object mObject = mChildContactAdapter.getItem(arg2);
                if (mObject instanceof User) {
                    // 用户
                    final User mUser = (User) mObject;
                    mChatUserNum = mUser.getUserNo();//当前个人用户的个人信息
                    chattype = Const.CHAT_TYPE_P2P;
                    if (isTransmit) {
                        if (!mChatUserNum.equals(AppController.getInstance().mSelfUser.getUserNo())) {
                            transdialog = new TransmitDialog(ContactChildOrgActivity.this, R.style.MyDialog, new TransmitDialog.OnclickListener() {
                                @Override
                                public void clicklistener(View view) {
                                    switch (view.getId()) {
                                        //点击提示框中的确定按钮
                                        case R.id.ok:
                                            doSendClick();
                                            transdialog.dismiss();
                                            showProgressDialog("");
                                            onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }, bodyEntity, mUser.getUserName(), transIsSend);
                            transdialog.show();
                        } else {
                            ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this, "不可以转发给自己", 1000);
                        }
                    } else {
                        startPersonInfoActivity(mUser);
                    }
                } else if (mObject instanceof OrganizationTree) {// 组织机构
                    OrganizationTree tempOrg = (OrganizationTree) mObject;
                    startChildOrgActivity(tempOrg, VIEW_ENTER);
                }
            }
        });
        mXmppConnManager.addReceivePresCallBack(this);
//		mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);
        mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT, this);
        //注册服务器信息变更的监听
        addServiceNoticeListener();

        mCloseAllReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mCloseAllReceiver, new IntentFilter(MainActivity.ACTION_RETURN_TO_MAIN_RECENT));
    }

    /**
     * 打开个人详情页
     *
     * @param mUser
     */
    public void startPersonInfoActivity(User mUser) {
        Intent infoIntent = new Intent(mContext,
                ContactPersonInfoActivity.class);
        infoIntent.putExtra("parentOrg", mParentOrg);
        infoIntent.putExtra("contactInfo", mUser);
        startActivity(infoIntent);
    }

    /**
     * 打开下级界面或返回上级界面
     *
     * @param mOrgTree 要返回到组织机构
     * @param i        0:进入子界面 1:返回上级界面
     */
    protected void startChildOrgActivity(OrganizationTree mOrgTree, int i) {
        // 表示进入子组织机构界面
        Intent childOrgIntent = new Intent(mContext,
                ContactChildOrgActivity.class);
        if (isTransmit) {
            childOrgIntent.putExtra("isTransmit", true);
            childOrgIntent.putExtra(TransmitActivity.TransStatus, transIsSend);
            childOrgIntent.putExtra(TransmitActivity.TransStatus, transState);
            childOrgIntent.putExtra(TransmitActivity.TransInfo, transInfo);
        }
        childOrgIntent.putExtra("isBack", i);
        childOrgIntent.putExtra("parentOrg", mOrgTree);
        // 必须转为ArrayList 才能通过PutExtra把list传到子activity
        ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao
                .queryOrgListByParentId(mOrgTree.getOrgNo());
        if (tempList != null) {
            childOrgIntent.putExtra("childOrgList", tempList);
        }
        // 通过数据库获取用户列表，主要是获取用户当前状态
        ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao
                .queryUsersByOrgNo(mOrgTree.getOrgNo());
        if (tempUsers != null) {
            childOrgIntent.putExtra("childOrgUser", tempUsers);
        }

        if (i == 0) {
            mChildContactAdapter.enterMenuAnimation();
        } else {
            mChildContactAdapter.backMenuAnimation();
        }

        mContext.startActivity(childOrgIntent);
        finish();
    }

    private void initSearchContactViewListener() {
        mRel_searchBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View paramView) {
                showSearchContactAnimation();
            }
        });
        mSearchContactEditText.setOnCancelSearchAnimationListener(this);
    }

    /**
     * 注册服务器信息变更的监听
     */
    private void addServiceNoticeListener() {
        mCommonReceiver = new CommonReceiver();
        mCommonReceiver.setOnServiceNoticeListener(new onServiceNoticeListener() {

            @Override
            public void onServiceNoticed() {
                //当服务器增删改查当前组织机构的某个人时，即时更新界面
                String parentId;
                if (mParentOrg == null) {
                    parentId = mParentOrgNo;
                } else {
                    parentId = mParentOrg.getOrgNo();
                }
                mChildOrg = mContactOrgDao.queryOrgListByParentId(parentId);
                mChildOrgUser = mContactOrgDao.queryUsersByOrgNo(parentId);
                refreshChildContactList();
            }
        });
        IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_SERVICE_NOTICE);
        registerReceiver(mCommonReceiver, mIntentFilter);
    }

    /**
     * 动画过程监听
     */
    private AnimationListener showAnimationListener = new AnimationListener() {
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
    private boolean isShowSearchEditText;

    /**
     * 展示搜索框动画
     */
    public void showSearchContactAnimation() {
        isShowSearchEditText = true;
        RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mRel_searchBox
                .getLayoutParams();
        searchViewY = mRel_searchBox.getY() - etParamTest.topMargin;
        showAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
        showAnimation.setDuration(200);
        showAnimation.setAnimationListener(showAnimationListener);
        mContactRelativeLayout.startAnimation(showAnimation);
    }

    /**
     * 隐藏搜索框动画
     */
    @Override
    public void cancelSearchContactAnimation() {
        isShowSearchEditText = false;
        mSearchContactEditText.dismiss();
        cancelAnimation = new TranslateAnimation(0, 0, 0, searchViewY);
        cancelAnimation.setDuration(200);
        cancelAnimation.setAnimationListener(showAnimationListener);
        mContactRelativeLayout.startAnimation(cancelAnimation);
    }


    @Override
    protected void onPause() {
        if (mOrgTitlePopWindow != null) {
            mOrgTitlePopWindow.dismiss();
        }
        mXmppConnManager.removeReceivePresCallBack(this);
        mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);
        unregisterReceiverSafe(mCommonReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mCloseAllReceiver);
        super.onPause();
    }

    /**
     * 监听Presence消息
     */
    @Override
    public void receivedPresence(Presence packet) {
        if ((packet.getType() == Presence.Type.available)
                || (packet.getType() == Presence.Type.unavailable)) {
            refreshOrgUserListUI(packet);
        }
    }

    /**
     * 刷新联系人列表用户的在线离线状态
     *
     * @param packet
     */
    private void refreshOrgUserListUI(Presence packet) {
        String mUserNo = JIDUtil.getAccountByJID(packet.getFrom());
        // mContactOrgDao.updateOneUserStatusByAble(mUserNo,i);
        if (mChildOrgUser != null) {
            String mOrgNo = null;
            if (mParentOrg == null) {
                mOrgNo = mParentOrgNo;
            } else {
                mOrgNo = mParentOrg.getOrgNo();
            }
            boolean isExist = mContactOrgDao.isUserRelationExist(mUserNo,
                    mOrgNo);
            if (isExist) {// 如果该User属于当前页面，则执行刷新UI的操作
                mChildOrgUser.clear();
                mChildOrgUser = mContactOrgDao.queryUsersByOrgNo(mOrgNo);
                refreshChildContactList();
            }
        }
    }

    private void refreshChildContactList() {
        mChildContactObjectList.clear();
        if (mChildOrgUser != null) {
            mChildContactObjectList.addAll(mChildOrgUser);
        }
        if (mChildOrg != null) {
            mChildContactObjectList.addAll(mChildOrg);
        }
        mChildContactAdapter.setnListObjects(mChildContactObjectList);
        mHandler.sendEmptyMessage(Const.USER_STATUS_CHANGED);
    }

    //搜索框弹出窗口
    private void showWindow(View parent, LayoutInflater inflater) {

        if (mOrgTitlePopWindow == null) {
            view = inflater.inflate(R.layout.contact_orgtitle_popwindow, null);
            mOrgTitlePopwinList = (ListView) view.findViewById(R.id.lv_contact_title_current_path);

            OrgPathAdapter groupAdapter = new OrgPathAdapter(this, ContactActivityManager.mTitleOrgList);
            mOrgTitlePopwinList.setAdapter(groupAdapter);
            // 创建一个PopuWidow对象
            mOrgTitlePopWindow = new PopupWindow(view, parent.getWidth(),
                    LayoutParams.WRAP_CONTENT);
            View mPopwinBG = view.findViewById(R.id.v_popwin_bg);
            mPopwinBG.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mOrgTitlePopWindow.dismiss();
                }
            });
            mOrgTitlePopWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    mContactOrgTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                            getResources().getDrawable(R.mipmap.contact_arrow_down), null);
                }
            });
        }
        mOrgTitlePopWindow.setFocusable(true);
        mOrgTitlePopWindow.update();
        // 设置允许在外点击消失
        ColorDrawable dw = new ColorDrawable(0x00000000);
        mOrgTitlePopWindow.setBackgroundDrawable(dw);
        mOrgTitlePopWindow.setOutsideTouchable(true);
        mOrgTitlePopWindow.showAsDropDown(parent);
        mContactOrgTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                getResources().getDrawable(R.mipmap.contact_arrow_up), null);
        mOrgTitlePopwinList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                if (mOrgTitlePopWindow != null) {
                    mOrgTitlePopWindow.dismiss();
                }
                if (position == ContactActivityManager.mTitleOrgList.size() - 1) {// 最后一个
                    // 不跳转
                    return;
                } else {
                    OrganizationTree mOrganizationTree = new OrganizationTree();
                    mOrganizationTree = ContactActivityManager.mTitleOrgList
                            .get(position);
                    ContactActivityManager
                            .finishAllActivityAndOrgFromIndex(position);
                    startChildOrgActivity(mOrganizationTree, VIEW_ENTER);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void back(View v) {
        back();
    }

    /**
     *
     */
    private void back() {
        ContactActivityManager.finishTopOrg();
        if (ContactActivityManager.mTitleOrgList.size() > 0) {
            int lastIndex = ContactActivityManager.mTitleOrgList.size() - 1;
            startChildOrgActivity(
                    ContactActivityManager.mTitleOrgList.get(lastIndex),
                    VIEW_BACK);
        } else {
            finish();
        }
    }

    /**
     * 跳转到我的组织机构
     *
     * @param v
     */
    public void onTurnToMyOrgListener(View v) {
        switch (v.getId()) {
            case R.id.tv_contact_title_jump_my_org:
                if (mOrgTitlePopWindow != null) {
                    mOrgTitlePopWindow.dismiss();
                }
                OrganizationTree myOrg = mContactOrgDao.queryMyOrg(mContext);
                if (myOrg != null) {
                    resetOrgPathList(myOrg);
                    startChildOrgActivity(myOrg, VIEW_ENTER);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 根据跳转到的org，寻找该org所属的组织机构
     *
     * @param tempOrg
     */
    protected void resetOrgPathList(OrganizationTree tempOrg) {
        ArrayList<OrganizationTree> mMyOrgPathList = new ArrayList<OrganizationTree>();
        if (ContactActivityManager.mTitleOrgList.size() > 0) {
            OrganizationTree rootOrgZZJG = ContactActivityManager.mTitleOrgList
                    .get(0);
            if (rootOrgZZJG.getParentOrgNo().equals("0")) {
                ContactActivityManager.mTitleOrgList.add(0,
                        new OrganizationTree("0", "-1", "组织机构", 0, "0", 0));
            }
        } else {
            ContactActivityManager.mTitleOrgList.add(0, new OrganizationTree(
                    "0", "-1", "组织机构", 0, "0", 0));
        }
        ContactActivityManager.finishAllOrgFromIndex(1);// 不删除组织机构节点
        mContactOrgDao.queryOrgBelongListByOrgNo(tempOrg, mMyOrgPathList);
        if (mMyOrgPathList.size() > 0) {
            Collections.reverse(mMyOrgPathList);
        }
        ContactActivityManager.mTitleOrgList.addAll(mMyOrgPathList);
    }

    @Override
    public void receivedReqIQResult(ReqIQResult packet) {
        android.os.Message message = mHandler.obtainMessage();
        message.obj = packet;
        message.what = GET_RECEIPT;
        mHandler.sendMessage(message);
    }

    private SendingListener initSendListener() {

        SendingListener sendingListener = new SendingListener() {
            @Override
            public void onBeforeEachSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType) {
                    case Const.CHAT_TYPE_P2P:
                        updateRecentChatListP2p(msg.getSubject(), new Date(), chatType);
                        msg.setSubject(null);
                        updateChatp2pMsgEntity(type, msg);
                        break;
                }
            }

            @Override
            public void onEachDone(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType) {
                    case Const.CHAT_TYPE_P2P:
                        updateChatp2pMsgEntity(type, msg);
                        break;
                    default:
                        break;
                }

            }

            // 在发送图片异步线程调取 onAllDone方法之后我们取消销毁这个activity 返回上层视图
            @Override
            public void onAllDone() {
            }

            @Override
            public void onFailedSend(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
                switch (chatType) {
                    case Const.CHAT_TYPE_P2P:
                        msg.setSubject(null);
                        msg.setSubject(UploadResponseHandler.mFailedSend);
                        updateChatp2pMsgEntity(type, msg);
                }
                hideProgressDialog();
                if (mCurrentStats == LoginThread.USER_STATUS_ONLINE) {
                    ToastUtil.toastAlerMessageCenter(ContactChildOrgActivity.this, "转发失败", 200);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BaseActivity.deleteContactChildActivity();
                            finish();
                        }
                    }, 1000);
                } else {
                    // 消除转发图片经过的Activity
                    BaseActivity.deleteContactChildActivity();
                    finish();
                }
            }

        };
        return sendingListener;
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
     * 点击提示框中的确定按钮所做的操作
     */
    private void doSendClick() {
        if (bodyEntity.getImages().size() > 0) {
            if (transState == BaseChatMsgEntity.SEND_SUCCESS) {
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, transInfo);
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            } else {
                List<ImageFile> imageFileList = new ArrayList<ImageFile>();
                ImageFile imageFile = new ImageFile(bodyEntity.getImages().get(0).getSdcardPath());
                imageFileList.add(imageFile);
                // 启动发送图片的AsyncTask
                SendImageTask sendImageTask = new SendImageTask(mXmppConnManager, Const.CHAT_TYPE_P2P, mChatUserNum);
                sendImageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(sendImageTask, imageFileList);
            }
        } else if (bodyEntity.getFiles().size() > 0) {
            // TODO 文件是由对方发过来的不管是否下载，或者是自己发送出去的且状态是成功发送那么发送message数据,否则发送本地文件
            if (transIsSend == BaseChatMsgEntity.COM_MSG || (transIsSend == BaseChatMsgEntity.TO_MSG && transState == BaseChatMsgEntity.SEND_SUCCESS)) {
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, transInfo);
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            } else {
                SendFileTask sendFileTask = new SendFileTask(mXmppConnManager, chattype, mChatUserNum);
                sendFileTask.setSendFileListener(mSendingListener);
                HashSet<File> selectedFiles = new HashSet<File>();
                selectedFiles.add(new File(bodyEntity.getFiles().get(0).getSdcardPath()));
                AsyncTaskCompat.executeParallel(sendFileTask, selectedFiles);
            }
        }
        else if (bodyEntity.getContent().length() > 0) {
            //转发文字
            if (transIsSend == BaseChatMsgEntity.COM_MSG || (transIsSend == BaseChatMsgEntity.TO_MSG && transState == BaseChatMsgEntity.SEND_SUCCESS)) {
                TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, transInfo);
                transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
                AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
            } else {
                SendMessageTask sendMessageTask = new SendMessageTask(mXmppConnManager, chattype, mChatUserNum);
                sendMessageTask.setSendMessageListener(mSendingListener);
                MessageBodyEntity bodyEntity = JSON.parseObject(transInfo, MessageBodyEntity.class);
                String content = bodyEntity.getContent();
                AsyncTaskCompat.executeParallel(sendMessageTask, content);
            }
        }
    }


    @Override
    public void onStatusChanged(int status) {
        if (mCurrentStats != status) {
            mHandler.sendEmptyMessage(status);
            mCurrentStats = status;
        }
    }
}
