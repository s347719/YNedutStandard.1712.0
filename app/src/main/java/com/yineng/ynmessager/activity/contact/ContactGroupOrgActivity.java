package com.yineng.ynmessager.activity.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.TransmitActivity.TransSendImageOrFileOrMessageTask;
import com.yineng.ynmessager.activity.TransmitActivity.TransmitActivity;
import com.yineng.ynmessager.activity.dissession.DisChatActivity;
import com.yineng.ynmessager.activity.dissession.DisCreateActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.picker.SendingListener;
import com.yineng.ynmessager.activity.picker.content.SendMessageTask;
import com.yineng.ynmessager.activity.picker.file.SendFileTask;
import com.yineng.ynmessager.activity.picker.image.SendImageTask;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.UploadResponseHandler;
import com.yineng.ynmessager.util.ViewHolder;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.dialog.TransmitDialog;

import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ContactGroupOrgActivity extends BaseActivity implements
		onCancelSearchAnimationListener, ReceiveReqIQCallBack, StatusChangedCallBack {
	private List<ContactGroup> mContactGroupList;
	private ListView mContactGroupOrgLV;
	private Context mContext;
	private TextView mContactOrgTitleTV;
	private ImageView mCreateDisGroupBtn;
	private String mChildGroupTitle;
	private ContactOrgDao mContactOrgDao;
	private int mCurrentStats = -1;
	private int mGroupType = 0;
	/**
	 *
	 /是否来自图片或者文件转发
	 */
	private boolean isTransmit = false;
	/**
	 *
	 /判断是接受者还是发送者
	 */
	private int transIsSend ;
	/**
	 *
	 /当前转发图片或者文件状态
	 */
	private int transState;
	/**
	 /转发图片或者文件的内容消息
	 *
	 */
	private String transInfo;
	private MessageBodyEntity bodyEntity;
	/**
	 * XMPP连接管理类实例
	 */
	protected XmppConnectionManager mXmppConnManager;

	/**
	 * 图片、文件发送过程中的方法回调接口实例
	 */
	protected SendingListener mSendingListener;

	/**
	 *
	 / 提示框提示是否发送
	 */
	private TransmitDialog transdialog;
	/**
	 /当前提示框出现时搜索框返回的接受者id和类型
	 *
	 */
	private int chattype;

	private RelativeLayout mContactRelativeLayout;
	private static final int[] LOGO_BACKGROUND = { R.mipmap.icon_org_1,
			R.mipmap.icon_org_2,
			R.mipmap.icon_org_3,
			R.mipmap.icon_org_4,
			R.mipmap.icon_org_5,
			R.mipmap.icon_org_6,
			R.mipmap.icon_org_7};
	/*** 搜索联系人功能 ***/

	/**
	 * 显示搜索框动画
	 */
	protected final int SHOW_SEARCH_VIEW = 0;
	/**
	 * 取消搜索框动画
	 */
	protected final int CANCEL_SEARCH_VIEW = 1;
	/**
	 *
	 / 获得回执的处理
	 */
	private final int GET_RECEIPT = 6;
	/**
	 *
	 / 刷新UI保存实例
	 */
	private final int REFRESH_UI = 5;
	private HashMap<String, Object> beanMap = new HashMap<>();
	/**
	 * 群数据库工具类
	 */
	private GroupChatDao mGroupChatDao;
	/**
	 * 讨论组工具类
	 */
	private DisGroupChatDao mDisGroupChatDao;
	/**
	 * 最近聊天信息工具类
	 */
	private RecentChatDao mRecentChatDao;
	/**
	 * 默认用于显示的搜索框
	 */
	private RelativeLayout mRelSearchBox;
	private ContactGroup mGroupObject;
	private String mGroupName;
	/**
	 * 上下动画滚动的高度
	 */
	protected float searchViewY;
	/**
	 * 自定义搜索框
	 */
	private SearchContactEditText mSearchContactEditText;
	/**
	 * 当前对方的聊天帐号（该属性在子类实现中被初始化）
	 */
	public String mChatUserNum;

	private Handler mHandler = new Handler() {
		@Override
		@SuppressLint("NewApi")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case GET_RECEIPT:
					ReqIQResult reqIq = (ReqIQResult) msg.obj;
					if (reqIq == null) {break;}
					L.i(mTag,"转发回执："+reqIq.toString());
					BaseChatMsgEntity entity = (BaseChatMsgEntity) beanMap.get(reqIq.getPacketID());
					if (entity == null){break;}
					long time = TimeUtil.getMillisecondByDate(reqIq.getSendTime(), TimeUtil.FORMAT_DATETIME_24_mic);
					if (entity != null && entity.getIsSuccess() != BaseChatMsgEntity.SEND_SUCCESS) {
						entity.setIsSuccess(BaseChatMsgEntity.SEND_SUCCESS);
						entity.setTime(String.valueOf(time));
						//更新最近会话表的发送时间
						if (entity.getPacketId().equals(reqIq.getPacketID())) {
							mRecentChatDao.updateRecentChatTime(reqIq.getSendTime(), mChatUserNum, Const.CHAT_TYPE_P2P);
						}
						if (chattype == Const.CHAT_TYPE_GROUP) {
							mGroupChatDao.saveOrUpdate((GroupChatMsgEntity) entity);
						} else if (chattype == Const.CHAT_TYPE_DIS) {
							mDisGroupChatDao.saveOrUpdate((GroupChatMsgEntity) entity);
						}
					}
					beanMap.clear();
					hideProgressDialog();
					ToastUtil.toastAlerMessageCenter(ContactGroupOrgActivity.this,"转发成功",200);
					this.postDelayed(new Runnable() {
						@Override
						public void run() {
							// 消除转发图片经过的Activity
							BaseActivity.deleteTransmitActivity();
							finish();
						}
					},1000);

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
//					ToastUtil.toastAlerMessageCenter(ContactGroupOrgActivity.this,getResources().getString(R.string.main_server_shutdown),500);
					break;

				case LoginThread.USER_STATUS_NETOFF:
					// 服务器连接断开
					mCurrentStats = 0;
					hideProgressDialog();
					ToastUtil.toastAlerMessageCenter(ContactGroupOrgActivity.this,getResources().getString(R.string.main_badNetwork),500);
					break;

				case LoginThread.USER_STATUS_OFFLINE:
					// 下线了
					mCurrentStats = 0;
					hideProgressDialog();
					if (LastLoginUserSP.getInstance(mContext).isLogin()) {
						ToastUtil.toastAlerMessageCenter(ContactGroupOrgActivity.this,getResources().getString(R.string.main_alreadyOffline),500);
					}
					break;
				default:
					break;
			}
		}
	};
	private CommonReceiver mCommonReceiver;
	private GroupListAdapter mGroupListAdapter;
	/**
	 * 标题栏返回键
	 */
	private TextView mContactReturnBT;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ContactGroupOrgActivity.this;


		initData();
		setContentView(R.layout.fragment_main_contact_layout);
		findViews();
		initListener();
	}

	private void initData() {
		isTransmit = getIntent().getBooleanExtra("isTransmit", false);
		transIsSend = getIntent().getIntExtra(TransmitActivity.TransIsSend,BaseChatMsgEntity.COM_MSG);
		//得到图片转发前的状态
		transState = getIntent().getIntExtra(TransmitActivity.TransStatus, BaseChatMsgEntity.SEND_SUCCESS);
		//得到图片信息
		transInfo = getIntent().getStringExtra(TransmitActivity.TransInfo);
		bodyEntity = JSON.parseObject(transInfo, MessageBodyEntity.class);
		mXmppConnManager = XmppConnectionManager.getInstance();
		mSendingListener = initSendListener();

		mRecentChatDao = new RecentChatDao(this);
		mDisGroupChatDao = new DisGroupChatDao(this);
		mGroupChatDao = new GroupChatDao(this);

		mContactOrgDao = new ContactOrgDao(mContext);
		final Intent orgDataIntent = getIntent();
		mChildGroupTitle = orgDataIntent.getStringExtra("childGroupTitle");
		mContactGroupList = (List<ContactGroup>) orgDataIntent
				.getSerializableExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME);
		mGroupType = orgDataIntent.getIntExtra("groupType", 0);
		if (mContactGroupList != null) {
			Log.e("GroupOrg", "mGroupOrg.size() == " + mContactGroupList.size());
		} else {
			mContactGroupList = mContactOrgDao.queryGroupList(mGroupType);
		}
	}

	private void findViews() {
		findSearchContactView();
		mCreateDisGroupBtn = (ImageView) findViewById(R.id.contact_org_create_dis_group);
		mContactOrgTitleTV = (TextView) findViewById(R.id.contact_org_title);
		mContactGroupOrgLV = (ListView) findViewById(R.id.contact_org_listview);
		mContactReturnBT = (TextView) findViewById(R.id.bt_contact_org_title_return_button);
		mContactReturnBT.setVisibility(View.VISIBLE);
		mContactOrgTitleTV.setText(mChildGroupTitle);
		mGroupListAdapter = new GroupListAdapter(mContext, mContactGroupList);
		mContactGroupOrgLV.setAdapter(mGroupListAdapter);
		if (mGroupType != Const.CONTACT_GROUP_TYPE && !isTransmit) {
			mCreateDisGroupBtn.setVisibility(View.VISIBLE);
		}
		setListEmptyView();

		//添加当前状态监听
		XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
		onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
	}

	/**
	 * 设置list为空的界面提示
	 */
	private void setListEmptyView() {
		TextView mContactGroupEmptyTV = (TextView) findViewById(R.id.tv_listview_data_empty);
		if (mGroupListAdapter.getContactGroupList() == null || mGroupListAdapter.getContactGroupList().size() <= 0) {
			if (mGroupType == Const.CONTACT_GROUP_TYPE) {
				mContactGroupEmptyTV.setText("您暂无群组");
			} else {
				mContactGroupEmptyTV.setText("您暂无讨论组");
			}
			mContactGroupOrgLV.setEmptyView(mContactGroupEmptyTV);
		}
	}

	private void initListener() {
		initSearchContactViewListener();

		mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT, ContactGroupOrgActivity.this);

		mCreateDisGroupBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
				int sumDisGroups = mContactOrgDao.queryGroupSumCreatedByMe(LastLoginUserSP.getLoginUserNo(getApplicationContext()));
				L.i(mTag, String.format("我创建的讨论组已有%d个", sumDisGroups));
				if (mClientInitConfig != null) {
					int maxDisGroups = Integer.parseInt(mClientInitConfig.getMax_disdisgroup_can_create());
					if (sumDisGroups >= maxDisGroups) {
						ToastUtil.toastAlerMessage(mContext, getString(R.string.p2pChatInfo_myDiscussCountOutOfMax, maxDisGroups), Toast.LENGTH_SHORT);
						return;
					}
				}
				final Intent intent = new Intent(ContactGroupOrgActivity.this,
						DisCreateActivity.class);
				startActivity(intent);
			}
		});
		mContactGroupOrgLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									final long arg3) {
				mChatUserNum = mContactGroupList.get(arg2).getGroupName();
				if (mGroupType == Const.CONTACT_GROUP_TYPE) {
					// 群组
					chattype = Const.CHAT_TYPE_GROUP;
				} else {
					// 讨论组
					chattype = Const.CHAT_TYPE_DIS;
				}
				if (isTransmit) {
					if (bodyEntity.getFiles().size()>0 && chattype==Const.CHAT_TYPE_GROUP){
						showToast(getString(R.string.trans_group_dialog));
					}else {
						transdialog = new TransmitDialog(ContactGroupOrgActivity.this, R.style.MyDialog, new TransmitDialog.OnclickListener() {
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
						}, bodyEntity, mContactGroupList.get(arg2).getSubject(),transIsSend);
						transdialog.show();
					}
				} else {
					final ContactGroup tempGroup = mContactGroupList.get(arg2);
					final Intent chatIntent = new Intent();
					chatIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, tempGroup);
					chatIntent.putExtra("Account", tempGroup.getGroupName());
					if (mGroupType == Const.CONTACT_GROUP_TYPE) {
						// 群组
						chatIntent.setClass(mContext, GroupChatActivity.class);
					} else {
						// 讨论组
						chatIntent.setClass(mContext, DisChatActivity.class);
					}
					startActivity(chatIntent);
					finish();
					LocalBroadcastManager.getInstance(getApplicationContext())
							.sendBroadcast(new Intent(MainActivity.ACTION_RETURN_TO_MAIN_RECENT).putExtra("index", 0));
				}
			}


		});
		addGroupUpdatedListener();
	}

	/**
	 * 添加群组、讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {

			@Override
			public void updateGroupData(int mGroupType) {
				if (mGroupType == ContactGroupOrgActivity.this.mGroupType) {
					mContactGroupList = mContactOrgDao.queryGroupList(mGroupType);
					mGroupListAdapter.setContactGroupList(mContactGroupList);
					mGroupListAdapter.notifyDataSetChanged();
					setListEmptyView();
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);
	}


	private void findSearchContactView() {
		mSearchContactEditText = new SearchContactEditText(mContext);
		if (isTransmit) {
			mSearchContactEditText.setSessionFragment(false, false, false, true);
		}
		mSearchContactEditText.setClickSendImageListener(new SearchContactEditText.clickSendImageListener() {
			@Override
			public void clickSendImage(String nameId, String name, int type) {
				mChatUserNum = nameId;
				chattype = type;
				transdialog = new TransmitDialog(ContactGroupOrgActivity.this, R.style.MyDialog, new TransmitDialog.OnclickListener() {
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
				}, bodyEntity, name,transIsSend);
				transdialog.show();
			}
		});
		mRelSearchBox = (RelativeLayout) findViewById(R.id.searchBox);
		mContactRelativeLayout = (RelativeLayout) findViewById(R.id.ll_contact_org_frame);
	}

	private void initSearchContactViewListener() {
		mRelSearchBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showSearchContactAnimation();
			}
		});

		mSearchContactEditText.setOnCancelSearchAnimationListener(this);
	}

	TranslateAnimation showAnimation = null;
	TranslateAnimation cancelAnimation = null;
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

	public void showSearchContactAnimation() {
		isShowSearchEditText = true;
		RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mRelSearchBox
				.getLayoutParams();
		searchViewY = mRelSearchBox.getY() - etParamTest.topMargin;
		showAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
		showAnimation.setDuration(200);
		showAnimation.setAnimationListener(showAnimationListener);
		mContactRelativeLayout.startAnimation(showAnimation);
	}

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
	protected void onDestroy() {
		mXmppConnManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_MSG_RESULT);

		unregisterReceiver(mCommonReceiver);
		System.gc();
		super.onDestroy();
	}

	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		android.os.Message message = mHandler.obtainMessage();
		message.obj = packet;
		message.what = GET_RECEIPT;
		mHandler.sendMessage(message);
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
	 * 群/讨论组适配器
	 *
	 * @author yineng
	 */
	public class GroupListAdapter extends BaseAdapter {
		private List<ContactGroup> nContactGroupList;
		private LayoutInflater inflater;

		public GroupListAdapter(Context context, List<ContactGroup> mContactGroupList) {
			inflater = LayoutInflater.from(context);
			nContactGroupList = mContactGroupList;
		}

		public List<ContactGroup> getContactGroupList() {
			//  Auto-generated method stub
			return nContactGroupList;
		}

		public void setContactGroupList(List<ContactGroup> mContactGroupList) {
			nContactGroupList = mContactGroupList;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.contact_org_orglist_item, parent, false);
			}
			if (nContactGroupList != null) {// 组织结构
				TextView tag = ViewHolder.get(convertView, R.id.tv_contact_orglist_item_tag);
				TextView name = ViewHolder.get(convertView, R.id.tv_contact_orglist_item_name);
				TextView userCount = ViewHolder.get(convertView, R.id.tv_contact_orglist_item_count);
				ImageView logo = ViewHolder.get(convertView, R.id.main_contact_listItem_logo);

				ContactGroup tempGroupOrg = nContactGroupList.get(position);

				// 随机设置LOGO
				int type = tempGroupOrg.getGroupType();
				if (type == Const.CONTACT_GROUP_TYPE) {
					logo.setImageResource(R.mipmap.session_group);
				} else if (type == Const.CONTACT_DISGROUP_TYPE) {
					int avatar=-1;
					ContactGroupUser group = mContactOrgDao.getContactGroupUserById(tempGroupOrg.getGroupName(), LastLoginUserSP.getLoginUserNo(mContext), Const.CHAT_TYPE_DIS);
					if (group!=null&&group.getRole()==Const.GROUP_CREATER_TYPE) {
						//自己创建的讨论组
						avatar = R.mipmap.session_creat_discus;
					} else {//自己加入的讨论组
						avatar = R.mipmap.session_join_discus;
					}
					logo.setImageResource(avatar);
				}

				String mGroupName;
				if (mGroupType == Const.CONTACT_GROUP_TYPE) {
					mGroupName = tempGroupOrg.getNaturalName();
				} else {
					if (tempGroupOrg.getSubject() != null && !tempGroupOrg.getSubject().isEmpty()) {
						mGroupName = tempGroupOrg.getSubject();
					} else {
						mGroupName = tempGroupOrg.getNaturalName();
					}
				}
				name.setText(mGroupName);
				List<User> mTempList = mContactOrgDao.queryUsersByGroupName(
						tempGroupOrg.getGroupName(), mGroupType);
				int mOrgCount = 0;
				int index=0;
				if (mTempList != null) {
					mOrgCount = mTempList.size();
					// 总人数
					for (User user: mTempList) {
						if (user.getUserStatus()==0){
							index++;
						}
					}
				}
				userCount.setText((mOrgCount-index)+"/"+mOrgCount);
			}

			return convertView;
		}

		@Override
		public int getCount() {
			if (nContactGroupList == null) {
				return 0;
			}
			return nContactGroupList.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (nContactGroupList == null) {
				return 0;
			}
			return nContactGroupList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			//  Auto-generated method stub
			return 0;
		}

	}

	public void back(View v) {
		finish();
	}

	private SendingListener initSendListener() {

		SendingListener sendingListener = new SendingListener() {
			@Override
			public void onBeforeEachSend(int type , org.jivesoftware.smack.packet.Message msg, int chatType) {
				switch (chatType) {
					case Const.CHAT_TYPE_P2P:
						break;

					case Const.CHAT_TYPE_GROUP:
						updateRecentChatListGroup(msg.getSubject(), new Date(), chatType);
						msg.setSubject(null);
						updateChatGroupMsgEntity(msg);
						break;

					case Const.CHAT_TYPE_DIS:
						updateRecentChatListDis(msg.getSubject(), new Date(), chatType);
						msg.setSubject(null);
						updateChatDisMsgEntity(type, msg);

						break;

					default:
						break;

				}
			}

			@Override
			public void onEachDone(int type, org.jivesoftware.smack.packet.Message msg, int chatType) {
				switch (chatType) {
					case Const.CHAT_TYPE_P2P:
						break;

					case Const.CHAT_TYPE_GROUP:
						updateChatGroupMsgEntity(msg);
						break;

					case Const.CHAT_TYPE_DIS:
						updateChatDisMsgEntity(type, msg);
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
					ToastUtil.toastAlerMessageCenter(ContactGroupOrgActivity.this,"转发失败",200);
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							BaseActivity.deleteTransmitActivity();
							finish();
						}
					},1000);
				}else {
					// 消除转发图片经过的Activity
					BaseActivity.deleteTransmitActivity();
					finish();
				}

			}

		};
		return sendingListener;
	}


	/**
	 * 保存最近的讨论组到最近聊天库中
	 */
	private void updateRecentChatListDis(String content, Date temp, int chatType) {

		RecentChat recentChat = new RecentChat();
		recentChat.setChatType(Const.CHAT_TYPE_DIS);
		recentChat.setUserNo(mChatUserNum);
		mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum,Const.CONTACT_GROUP_TYPE);
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
		android.os.Message message = mHandler.obtainMessage();
		message.obj = chatMsg;
		message.what = REFRESH_UI;
		mHandler.sendMessage(message);

	}


	/**
	 * 保存群聊天信息
	 */
	private void updateRecentChatListGroup(String content, Date timeStamp, int chatType) {

		RecentChat recentChat = new RecentChat();
		recentChat.setChatType(Const.CHAT_TYPE_GROUP);
		recentChat.setUserNo(mChatUserNum);
		mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum,Const.CONTACT_GROUP_TYPE);
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
		recentChat.setDateTime(DateFormatUtils.format(timeStamp, TimeUtil.FORMAT_DATETIME_24_mic));
		recentChat.setUnReadCount(0);
		recentChat.setDraft("");
		mRecentChatDao.saveRecentChat(recentChat);

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
		mGroupChatDao.saveOrUpdate(chatMsg);
		android.os.Message message = mHandler.obtainMessage();
		message.obj = chatMsg;
		message.what = REFRESH_UI;
		mHandler.sendMessage(message);
	}

	/**
	 * 点击弹出框中的确定按钮所做的操作
	 */
	private void doSendClick() {
		if (bodyEntity.getImages().size()>0) {
			if (transState == BaseChatMsgEntity.SEND_SUCCESS) {
				TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, transInfo);
				transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
				AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);

			} else {
				List<ImageFile> imageFileList = new ArrayList<ImageFile>();
				imageFileList.add(new ImageFile(bodyEntity.getImages().get(0).getSdcardPath()));
				// 启动发送图片的AsyncTask
				SendImageTask sendImageTask = new SendImageTask(mXmppConnManager, Const.CHAT_TYPE_P2P, mChatUserNum);
				sendImageTask.setSendImageListener(mSendingListener);
				AsyncTaskCompat.executeParallel(sendImageTask, imageFileList);
			}
		}else if (bodyEntity.getFiles().size()>0){
			// TODO 文件是由对方发过来的不管是否下载，或者是自己发送出去的且状态是成功发送那么发送message数据,否则发送本地文件
			if (transIsSend == BaseChatMsgEntity.COM_MSG || (transIsSend == BaseChatMsgEntity.TO_MSG && (transState == BaseChatMsgEntity.SEND_SUCCESS||transState == BaseChatMsgEntity.DOWNLOAD_SUCCESS))) {
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
		}else if (bodyEntity.getContent().length()>0){
			//转发文字
			if (transIsSend == BaseChatMsgEntity.COM_MSG || (transIsSend== BaseChatMsgEntity.TO_MSG && transState== BaseChatMsgEntity.SEND_SUCCESS)){
				TransSendImageOrFileOrMessageTask transSendImageOrFileOrMessageTask = new TransSendImageOrFileOrMessageTask(mXmppConnManager, chattype, mChatUserNum, transInfo);
				transSendImageOrFileOrMessageTask.setSendImageListener(mSendingListener);
				AsyncTaskCompat.executeParallel(transSendImageOrFileOrMessageTask);
			}
			else {
				SendMessageTask sendMessageTask = new SendMessageTask(mXmppConnManager,chattype,mChatUserNum);
				sendMessageTask.setSendMessageListener(mSendingListener);
				MessageBodyEntity bodyEntity  = JSON.parseObject(transInfo,MessageBodyEntity.class);
				String content = bodyEntity.getContent();
				AsyncTaskCompat.executeParallel(sendMessageTask,content);
			}
		}
	}
}
