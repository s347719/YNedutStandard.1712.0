package com.yineng.ynmessager.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.contact.ContactChildOrgActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.dissession.DisChatActivity;
import com.yineng.ynmessager.activity.dissession.HorizontalListViewAdapter;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.p2psession.P2PChatActivity;
import com.yineng.ynmessager.adapter.ContactCommonAdapter;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.ContactActivityManager;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.onServiceNoticeListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 主页消息，联系人，组织机构，群，讨论组搜索框
 * @author 胡毅
 *
 */
public class SearchContactEditText extends Dialog implements
		OnFocusChangeListener, TextWatcher {

	private Context mContext;
	/**
	 * 搜索结果列表
	 */
	public ListView mContactSearchListView;
	/**
	 * 搜索框
	 */
	public EditText mSearchEditText;
	private ContactOrgDao mContactOrgDao;
	private InputMethodManager inputMM;
	/**
	 * 搜索结果
	 */
	private List<Object> mSearchResultObjects = null;
	private ContactCommonAdapter mContactSearchAdapter;
	/**
	 * 取消搜索按钮
	 */
	private Button mContactCancelSearchBt;

	/**
	 * 是否是消息列表的搜索框
	 */
	private boolean isSessionFragment = false;
	/**
	 * 是否是主界面的搜索框
	 */
	private boolean isMainActivity = false;
	/**
	 * 是否是转发界面的搜索框
	 */
	private boolean isTransmitActivity = false;
	public TextView mContactSearchResultEmptyTV;
	/**
	 * 是否是选择联系人的搜索框
	 */
	private boolean isDisAddActivity = false;
	/**
	 * mHorizontalListView: 显示已选择用户的画廊
	 */
	private HorizontalListView mHorizontalListView;

	/**
	 * mHorizontalListViewAdapter: 画廊适配器
	 */
	private HorizontalListViewAdapter mHorizontalListViewAdapter;

	public Button mSearchCreateDisGroupBt;
	private LinearLayout mSearchCreateDisGroupLayout;
	/**
	 * @param isSessionFragment 是否是主页的消息页面
	 * @param isMainActivity 是否是主页
	 * @param isDisAddActivity 是否是选择联系人界面
	 */
	public void setSessionFragment(boolean isSessionFragment,boolean isMainActivity,boolean isDisAddActivity) {
		this.isSessionFragment = isSessionFragment;
		this.isMainActivity = isMainActivity;
		this.isDisAddActivity = isDisAddActivity;
	}

	/**
	 *
	 * @param isSessionFragment
	 * @param isMainActivity
	 * @param isDisAddActivity
	 * @param isTransmitActivity  是否是转发界面
	 */
	public void setSessionFragment(boolean isSessionFragment,boolean isMainActivity,boolean isDisAddActivity,boolean isTransmitActivity){
		this.isSessionFragment = isSessionFragment;
		this.isMainActivity = isMainActivity;
		this.isDisAddActivity = isDisAddActivity;
		this.isTransmitActivity = isTransmitActivity;
	}

	public HorizontalListViewAdapter getmHorizontalListViewAdapter() {
		return mHorizontalListViewAdapter;
	}

	public void setmHorizontalListViewAdapter(
			HorizontalListViewAdapter mHorizontalListViewAdapter) {
		this.mHorizontalListViewAdapter = mHorizontalListViewAdapter;
	}

	public SearchContactEditText(Context context) {
		// super(mContext);
		super(context, R.style.mydialog);
		this.mContext = context;
		inputMM = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		// getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//  Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContactOrgDao = new ContactOrgDao(mContext);
		initView();
		initSearchEditViewListener();
	}

	/**
	 * 初始化界面元素
	 */
	public void initView() {
		Window win = getWindow();
		win.getDecorView().setPadding(0, 0, 0, 0);
		win.setWindowAnimations(0);
		win.setGravity(Gravity.TOP);
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.FILL_PARENT;
		win.setAttributes(lp);

		this.setContentView(R.layout.contact_search_view);
		mSearchEditText = (EditText) findViewById(R.id.se_contact_org_search);
		mContactSearchListView = (ListView) findViewById(R.id.lv_search_contact_org_listview);
		mContactCancelSearchBt = (Button) findViewById(R.id.bt_cancel_search);
		mContactSearchResultEmptyTV = (TextView) findViewById(R.id.tv_search_contact_result_list_no_data);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.gl_search_contact_create_disgroup_hlv);
//		mSearchCreateDisGroupBt = (Button) findViewById(R.id.btn_search_contact_create_disgroup_btn);
		mSearchCreateDisGroupLayout = (LinearLayout) findViewById(R.id.ll_search_contact_create_disgroup_layout);
		if (isDisAddActivity) {
			mSearchCreateDisGroupLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 删除按钮的引用
	 */
	private Drawable mClearDrawable;

	/**
	 * 初始化界面控件的监听器
	 */
	private void initSearchEditViewListener() {
		// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
		mClearDrawable = mSearchEditText.getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			mClearDrawable = mContext.getResources().getDrawable(
					R.mipmap.emotionstore_progresscancelbtn);
		}
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());
		setClearIconVisible(false);

		mContactCancelSearchBt
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mOnCancelSearchAnimationListener != null) {
							mOnCancelSearchAnimationListener
									.cancelSearchContactAnimation();
						}
					}
				});

		setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				if (inputMM.isActive()) {
					inputMM.toggleSoftInput(0,
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});

		mSearchEditText.setOnFocusChangeListener(this);
		mSearchEditText.addTextChangedListener(this);
		mSearchEditText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent event) {
				if (mSearchEditText.getCompoundDrawables()[2] != null) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						boolean touchable = event.getX() > (mSearchEditText
								.getWidth() - mSearchEditText.getPaddingRight() - mClearDrawable
								.getIntrinsicWidth())
								&& (event.getX() < ((mSearchEditText.getWidth() - mSearchEditText
								.getPaddingRight())));
						if (touchable) {
							mSearchEditText.setText("");
						}
					}
				}
				return false;
			}
		});

		mContactSearchListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
				if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					if (mContactSearchListView.getCount() <= 0) {
						mOnCancelSearchAnimationListener
								.cancelSearchContactAnimation();
						return true;
					} else {
						HideKeyBoardAndClearFocus();
					}
				}
				return false;
			}
		});
		//搜索结果界面列表点击监听
		mContactSearchListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
											int pos, long arg3) {
						if (!isDisAddActivity) {
							Object mObject = mContactSearchAdapter.getItem(pos);
							if (mObject instanceof User) {
								User mUser = (User) mObject;
								if (isSessionFragment) {
									startP2PChatActivity(mUser);
								} else {
									if (isTransmitActivity){
										mOnCancelSearchAnimationListener.cancelSearchContactAnimation();
										clickSendImageListener.clickSendImage(mUser.getUserNo(),mUser.getUserName(),Const.CHAT_TYPE_P2P);
									}else {
										startPersonInfoActivity(mUser);
									}
								}
							} else if (mObject instanceof OrganizationTree) {
								OrganizationTree tempOrg = (OrganizationTree) mObject;
//								startChildOrgActivity(tempOrg);
								resetOrgPathList(tempOrg);
								startChildOrgActivity(tempOrg,0);
								dismiss();
							} else if (mObject instanceof ContactGroup) {//跳转至会话界面
								final ContactGroup tempContactGroup = (ContactGroup) mObject;
								final Intent chatIntent = new Intent();
								chatIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, tempContactGroup);
								chatIntent.putExtra("Account", tempContactGroup.getGroupName());
								if (tempContactGroup.getGroupType() == Const.CONTACT_GROUP_TYPE) { // 群组
									if (isTransmitActivity){
										mOnCancelSearchAnimationListener.cancelSearchContactAnimation();
										clickSendImageListener.clickSendImage(tempContactGroup.getGroupName(),"",Const.CHAT_TYPE_GROUP);
									}else {
										chatIntent.setClass(mContext, GroupChatActivity.class);
										mContext.startActivity(chatIntent);
									}
								} else { // 讨论组
									if (isTransmitActivity){
										mOnCancelSearchAnimationListener.cancelSearchContactAnimation();
										clickSendImageListener.clickSendImage(tempContactGroup.getGroupName(),"",Const.CHAT_TYPE_DIS);
									}else {
										chatIntent.setClass(mContext, DisChatActivity.class);
										mContext.startActivity(chatIntent);
									}

								}
							}
						}
					}
				});
	}

	/**
	 * 根据跳转到的org，寻找该org所属的组织机构
	 * @param tempOrg
	 */
	protected void resetOrgPathList(OrganizationTree tempOrg) {
		ArrayList<OrganizationTree> mMyOrgPathList = new ArrayList<OrganizationTree>();
		if (ContactActivityManager.mTitleOrgList.size() > 0) {
			OrganizationTree rootOrgZZJG = ContactActivityManager.mTitleOrgList.get(0);
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

	/**
	 * 打开两人会话界面
	 * @param mUser
	 */
	public void startP2PChatActivity(User mUser) {
		LastLoginUserSP lastUserSP = LastLoginUserSP.getInstance(mContext);
		//如果在消息会话列表中搜索出了自己，点击时不进入会话界面
		if (mUser.getUserNo().equals(lastUserSP.getUserAccount())) {
			startPersonInfoActivity(mUser);
			return;
		}
		Intent chatIntent = new Intent(mContext, P2PChatActivity.class);
		chatIntent.putExtra(Const.INTENT_USER_EXTRA_NAME, mUser);
		mContext.startActivity(chatIntent);
	}

	/**
	 * 打开个人详细信息界面
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(mContext,ContactPersonInfoActivity.class);
//		OrganizationTree mParentOrg = mContactOrgDao.queryUserRelationByUserNo(mUser.getUserNo());
//		infoIntent.putExtra("parentOrg", mParentOrg);
		infoIntent.putExtra("contactInfo", mUser);
		mContext.startActivity(infoIntent);
	}

	/**
	 * 打开下级界面或返回上级界面
	 * @param mOrgTree 要返回到组织机构
	 * @param i 0:进入子界面  1:返回上级界面
	 */
	protected void startChildOrgActivity(OrganizationTree mOrgTree, int i) {
		// 表示进入子组织机构界面
		Intent childOrgIntent = new Intent(mContext,ContactChildOrgActivity.class);
		childOrgIntent.putExtra("isBack", i);
		childOrgIntent.putExtra("parentOrg",mOrgTree);
		//必须转为ArrayList 才能通过PutExtra把list传到子activity
		ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId(mOrgTree.getOrgNo());
		if (tempList != null) {
			childOrgIntent.putExtra("childOrgList", tempList);
		}
		//通过数据库获取用户列表，主要是获取用户当前状态
		ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao.queryUsersByOrgNo(mOrgTree.getOrgNo());
		if (tempUsers != null) {
			childOrgIntent.putExtra("childOrgUser", tempUsers);
		}

		mContext.startActivity(childOrgIntent);
		//如果是从主界面搜索，不能关闭主activity
		if (!isMainActivity) {
			((Activity) mContext).finish();
		} else {
			mOnCancelSearchAnimationListener
					.cancelSearchContactAnimation();
		}
		if (i == 0) {
			mContactSearchAdapter.enterMenuAnimation();
		} else {
			mContactSearchAdapter.backMenuAnimation();
		}
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 *
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? mClearDrawable : null;
		mSearchEditText.setCompoundDrawables(
				mSearchEditText.getCompoundDrawables()[0],
				mSearchEditText.getCompoundDrawables()[1], right,
				mSearchEditText.getCompoundDrawables()[3]);
	}

	/**
	 * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(mSearchEditText.getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * 当输入框里面内容发生变化的时候回调的方法
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		setClearIconVisible(s.length() > 0);
	}

	/**
	 * 当输入框里面内容发生变化之前的时候回调的方法
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
								  int after) {
	}

	/**
	 * 当输入框里面内容发生变化之后的时候回调的方法
	 */
	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {
			mSearchResultObjects = mContactOrgDao.querySearchResultByKeyWords(s
					.toString());
			mContactSearchListView.setBackgroundResource(R.color.white);
		} else {
			if (mSearchResultObjects != null) {
				mSearchResultObjects.clear();
			}
			mContactSearchListView.setBackgroundResource(Color.TRANSPARENT);
		}
		showPopupWindow(mSearchResultObjects);
	}

	/** 去掉搜索框焦点和隐藏软键盘 **/
	public void HideKeyBoardAndClearFocus() {
		inputMM.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
	}

	/**
	 * 显示搜索结果
	 * @param mSearchResultObjects 从数据库里查找到的结果
	 */
	public void showPopupWindow(List<Object> mSearchResultObjects) {
		if (isGroupSearch) {
			mOnResultSearchedListener.onResultSearched(mSearchResultObjects);
		} else {
			if (mContactSearchAdapter == null) {
				mContactSearchAdapter = new ContactCommonAdapter(mContext,
						mSearchResultObjects);
				mContactSearchListView.setAdapter(mContactSearchAdapter);
			} else {
				mContactSearchAdapter.setnListObjects(mSearchResultObjects);
				mContactSearchAdapter.resetViewTag();
				mContactSearchAdapter.notifyDataSetChanged();
			}
			//判断是否显示空数据界面
			if (mSearchEditText.getText().length()>0 && mContactSearchAdapter.getCount() <= 0) {
				mContactSearchListView.setEmptyView(mContactSearchResultEmptyTV);
			} else {
				mContactSearchResultEmptyTV.setVisibility(View.GONE);
				mContactSearchListView.setEmptyView(null);
			}
		}
	}


	/**
	 * 取消搜索时的动画监听器
	 */
	private onCancelSearchAnimationListener mOnCancelSearchAnimationListener;
	/**
	 * 传回信息的监听器
	 */
	private clickSendImageListener clickSendImageListener;

	/**
	 * 设置取消搜索时的动画监听
	 * @param tempOnCancelSearchAnimationListener 取消搜索时的动画监听器
	 */
	public void setOnCancelSearchAnimationListener(
			onCancelSearchAnimationListener tempOnCancelSearchAnimationListener) {
		this.mOnCancelSearchAnimationListener = tempOnCancelSearchAnimationListener;
	}

	/**
	 * 转发图片传回个人信息监听
	 */
	public void setClickSendImageListener(clickSendImageListener clickSendImageListener){
		this.clickSendImageListener = clickSendImageListener;
	}

	/**
	 * 转发图片接口
	 */
	public interface clickSendImageListener{
		void clickSendImage(String receivename,String userName,int chattype);
	}
	/**
	 * 取消搜索的回调接口
	 * @author HY
	 *
	 */
	public interface onCancelSearchAnimationListener {
		void cancelSearchContactAnimation();
	}

	/**
	 * 清除编辑框的焦点和文字
	 */
	public void clearSearchEditText() {
		mSearchEditText.clearFocus();
		mSearchEditText.setText("");
	}

	/**
	 * 搜索界面消失
	 */
	@Override
	public void dismiss() {
		HideKeyBoardAndClearFocus();
		super.dismiss();
		clearSearchEditText();
		unregisterReceiverSafe(mCommonReceiver);
	}

	/**
	 * 显示搜索界面
	 */
	@Override
	public void show() {
		//  Auto-generated method stub
		super.show();
		if (isDisAddActivity && mHorizontalListViewAdapter != null) {
			if (mHorizontalListView.getAdapter() != null) {
				mHorizontalListViewAdapter = (HorizontalListViewAdapter) mHorizontalListView.getAdapter();
			} else {
				mHorizontalListView.setAdapter(mHorizontalListViewAdapter);
			}
		}
		addGroupUpdatedListener();
	}

	@Override
	public void onBackPressed() {
		mOnCancelSearchAnimationListener.cancelSearchContactAnimation();
		super.onBackPressed();
	}

	private onResultSearchedListener mOnResultSearchedListener;
	/**
	 * 是否是群、讨论组添加成员中的搜索框
	 */
	private boolean isGroupSearch = false;
	/**
	 * 群、讨论组信息变更广播
	 */
	private CommonReceiver mCommonReceiver;

	public interface onResultSearchedListener {
		void onResultSearched(List<Object> mSearchResultObjects);
	}

	/**
	 * 搜索结果监听器
	 * @param mGroupSearch 是否是添加讨论组成员的界面
	 * @param tempListener 回调结构
	 */
	public void setOnResultSearchedListener(boolean mGroupSearch,onResultSearchedListener tempListener){
		isGroupSearch = mGroupSearch;
		mOnResultSearchedListener = tempListener;
	}

	/**
	 * 添加讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		//讨论组信息变更了
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {

			@Override
			public void updateGroupData(int mGroupType) {
				String keyString = mSearchEditText.getText().toString();
				if (keyString.length() > 0) {
					mSearchResultObjects = mContactOrgDao.querySearchResultByKeyWords(keyString);
					mContactSearchListView.setBackgroundResource(R.color.white);
				} else {
					if (mSearchResultObjects != null) {
						mSearchResultObjects.clear();
					}
					mContactSearchListView.setBackgroundResource(Color.TRANSPARENT);
				}
				showPopupWindow(mSearchResultObjects);
			}
		});
		//我退出了讨论组
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {

			@Override
			public void IQuitMyGroup(int mGroupType) {
				String keyString = mSearchEditText.getText().toString();
				if (keyString.length() > 0) {
					mSearchResultObjects = mContactOrgDao.querySearchResultByKeyWords(keyString);
					mContactSearchListView.setBackgroundResource(R.color.white);
				} else {
					if (mSearchResultObjects != null) {
						mSearchResultObjects.clear();
					}
					mContactSearchListView.setBackgroundResource(Color.TRANSPARENT);
				}
				showPopupWindow(mSearchResultObjects);
			}
		});
		mCommonReceiver.setOnServiceNoticeListener(new onServiceNoticeListener() {

			@Override
			public void onServiceNoticed() {
				// 当服务器增删改查当前组织机构的某个人时，即时更新界面
				if (mSearchEditText.getText().length() > 0) {
					mSearchResultObjects = mContactOrgDao.querySearchResultByKeyWords(mSearchEditText.getText()
							.toString());
					mContactSearchListView.setBackgroundResource(R.color.white);
				} else {
					if (mSearchResultObjects != null) {
						mSearchResultObjects.clear();
					}
					mContactSearchListView.setBackgroundResource(Color.TRANSPARENT);
				}
				showPopupWindow(mSearchResultObjects);
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_SERVICE_NOTICE);
		mContext.registerReceiver(mCommonReceiver, mIntentFilter);
	}

	// just sample, 可以写入工具类
	// 第一眼我看到这段代码，靠，太粗暴了，但是回头一想，要的就是这么简单粗暴，不要把一些简单的东西搞的那么复杂。
	private void unregisterReceiverSafe(CommonReceiver receiver) {
		try {
			getContext().unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			// ignore
		}
	}

	/**
	 * 返回编辑框的字符串
	 * @return
	 */
	public String getEditTextStr() {
		return mSearchEditText.getText().toString();
	}
}
