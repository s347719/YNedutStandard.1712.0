package com.yineng.ynmessager.activity.dissession;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.dissession.HorizontalListViewAdapter.onRemoveSelectedMemgerListener;
import com.yineng.ynmessager.adapter.ContactCommonAdapter;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.contact.ContactCommonBean;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.dissession.DisSessionBean;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.HorizontalListView;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.SearchContactEditText.onResultSearchedListener;

import org.jivesoftware.smack.packet.IQ.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 创建讨论组的界面
 * @author 刘大砚 
 */
public class DisCreateActivity extends BaseActivity implements onCancelSearchAnimationListener,OnClickListener{
	public static final String DIS_GROUP_ID_KEY = "discussion_group_id";// 获得传过来的讨论组IDkey
	public static final int REFRESH_SEARCH_LIST_UI = 8;
	/**
	 * 搜索框
	 */
	private RelativeLayout mRel_searchBox;
	private ListView mOrgListLV;
	/**
	 * 标题栏
	 */
	private TextView mTitleTV;
	/**
	 * 完成按钮
	 */
	private Button mCreateBtn;
	private HorizontalListView mHorizontalListView;
	private ListView mOrgTitlePopwinList;
	private PopupWindow mOrgTitlePopWindow;
	private ContactOrgDao mContactOrgDao;
	private boolean isRootList = true;
	public static final int REFRESH_GALLARY_UI = 0;// 刷新画廊UI
	public static final int REFRESH_LIST_UI = 1;// 刷新listview的UI
	public static final int REFRESH_GUID_UI = 2;// 刷新组织机构跳转UI
	public static final int CREATE_SUCCESS = 3;// 成功创建讨论组
	public static final int CREATE_FAILED = 4;// 创建讨论组失败
	public static final int OVER_FLOW_MAX_MEMBERS = 5;//成员超限
	private String mGroupId;
	private XmppConnectionManager mXmppConnectionManager;
	private String mCreatePacketId; // 创建讨论组的请求ID
	private String mCurrentGroupId;

	/**
	 * 显示当前界面所需要的数据
	 */

	private List<Object> mCurrentObjectList = new ArrayList<Object>();
	
	private List<Object> mSearchResultObjects = new ArrayList<Object>();
	
	/**
	 * 顶部跳转按钮数据
	 */
	private LinkedList<OrganizationTree> mGuideList = new LinkedList<OrganizationTree>();

	/**
	 * 创建讨论组时选择的用户
	 */
	private List<User> mNewAddUserList = new ArrayList<User>();

	/**
	 * 组织机构列表适配器
	 */
	private DisCreateSelectListAdapter mDisCreateSelectListAdapter;
	/**
	 * 组织机构快速跳转的适配器
	 */
	private OrgPathAdapter mOrgPathAdapter;
	/**
	 * 底部已选中的横向列表
	 */
	private HorizontalListViewAdapter mHorizontalListViewAdapter;

	private TextView mTxt_previous;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_GALLARY_UI:
				mHorizontalListViewAdapter.setData(mNewAddUserList);
				mHorizontalListViewAdapter.notifyDataSetChanged();
				if (mHorizontalListViewAdapter.getCount()>0) {
					mCreateBtn.setText("完成("+mHorizontalListViewAdapter.getCount()+")");
					if (mSearchContactEditText.mSearchCreateDisGroupBt != null) {
						mSearchContactEditText.mSearchCreateDisGroupBt.setText("完成("+mHorizontalListViewAdapter.getCount()+")");
					}
				} else {
					mCreateBtn.setText("完成");
					if (mSearchContactEditText.mSearchCreateDisGroupBt != null) {
						mSearchContactEditText.mSearchCreateDisGroupBt.setText("完成");
					}
				}

				break;
			case REFRESH_LIST_UI:
				updateTitleView();
				mDisCreateSelectListAdapter.setData(mCurrentObjectList);
				mDisCreateSelectListAdapter.notifyDataSetChanged();
				break;
			case REFRESH_GUID_UI:
				mOrgPathAdapter.setData(mGuideList);
				mOrgPathAdapter.notifyDataSetChanged();
				break;
			case CREATE_SUCCESS:
				finish();
				break;
			case CREATE_FAILED:
				break;
			case OVER_FLOW_MAX_MEMBERS:
				ToastUtil.toastAlerMessage(DisCreateActivity.this, "您的讨论组人数已达到"+mMaxMemberNum+"人上限！", Toast.LENGTH_SHORT);
				break;
			case DisAddActivity.SHOW_SEARCH_VIEW:
				mCreateDisGroupLayout.setVisibility(View.GONE);
				mSearchContactEditText.show();
				mCreateDisGroupLayoutLL.setY(-searchViewY);
				mSearchContactEditText.mSearchCreateDisGroupBt = (Button) mSearchContactEditText.findViewById(R.id.btn_search_contact_create_disgroup_btn);
				mSearchContactEditText.mSearchCreateDisGroupBt.setOnClickListener(DisCreateActivity.this);
				mSearchContactEditText.mSearchCreateDisGroupBt.setText("完成("+mHorizontalListViewAdapter.getCount()+")");
				break;
			case DisAddActivity.CANCEL_SEARCH_VIEW:
				mCreateDisGroupLayoutLL.setY(0);
				mCreateDisGroupLayout.setVisibility(View.VISIBLE);
				break;
			case REFRESH_SEARCH_LIST_UI:
				mDisCreateSelectListAdapter.setData(mSearchResultObjects);
				mDisCreateSelectListAdapter.notifyDataSetChanged();
				//判断是否显示空数据界面
				if (mSearchResultObjects.size() <= 0) {
					mSearchContactEditText.mContactSearchListView.setEmptyView(mSearchContactEditText.mContactSearchResultEmptyTV);
				} else {
					mSearchContactEditText.mContactSearchResultEmptyTV.setVisibility(View.GONE);
					mSearchContactEditText.mContactSearchListView.setEmptyView(null);
				}
				break;
			default:
				break;
			}
		}

	};
	private DisSessionBean mDisSessionBean;
	private int mMaxMemberNum = 100;

	/**
	 * 搜索框
	 */
	private SearchContactEditText mSearchContactEditText;
	private LinearLayout mCreateDisGroupLayoutLL;
	protected float searchViewY;
	private boolean isShowSearchEditText = false;
	private LinearLayout mCreateDisGroupLayout;
	/**
	 * 用户历史操作记录
	 */
	private LinkedList<Object> mHistoryPathList = new LinkedList<Object>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_disgroup);
		initialize();
		initEvent();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		if(mOrgTitlePopWindow != null)
		{
			mOrgTitlePopWindow.dismiss();
		}
	}
	
	/**
	 * 初始化界面和数据
	 */
	private void initialize() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
//		mXmppConnectionManager
//				.addReceiveReqIQCallBack("com:yineng:group", this);
		mContactOrgDao = new ContactOrgDao(this);
		mCreateBtn = (Button) findViewById(R.id.btn_create_disgroup_createbtn);
		mTitleTV = (TextView) findViewById(R.id.tv_create_disgroup_title);
		mRel_searchBox = (RelativeLayout) findViewById(R.id.searchBox);
		mOrgListLV = (ListView) findViewById(R.id.lv_create_disgroup_listview);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.gl_create_disgroup_horizontallistView);
		mCreateDisGroupLayout = (LinearLayout) findViewById(R.id.ll_create_disgroup_thumbnail_layout_);
		mTxt_previous = (TextView) findViewById(R.id.disAdd_previous);
		// mGroupId是上一级界面传过来的讨论组ID，如果是新建讨论组，则key为null
		mGroupId = (String) getIntent().getCharSequenceExtra(DIS_GROUP_ID_KEY);

		// 组织结构跳转适配器
		mOrgPathAdapter = new OrgPathAdapter(this);

		// 画廊适配器
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
//		User existedUser = (User) getIntent().getParcelableExtra("disGroupAddedUser");
//		if (existedUser != null) {
//			mNewAddUserList.add(existedUser);
//			mHorizontalListViewAdapter.setData(mNewAddUserList);
//		}
		mHorizontalListView.setAdapter(mHorizontalListViewAdapter);

		// 人员或组织结构显示列表
		mDisCreateSelectListAdapter = new DisCreateSelectListAdapter(this);
		mOrgListLV.setAdapter(mDisCreateSelectListAdapter);
		showRootListUI();
		initMaxUser();
		findSearchContactView();
	}

	/**
	 * 初始化最大成员数
	 */
	private void initMaxUser() {
		ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
		if (mClientInitConfig != null) {
			mMaxMemberNum = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
		}		
	}
	
	/**
	 * 初始化界面监听器
	 */
	private void initEvent() {
		mTxt_previous.setOnClickListener(this);
		mCreateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGroupAction();
			}
		});

		mTitleTV.setOnClickListener(new OnClickListener() {
			
			View actionbar = findViewById(R.id.createDisgroup_rel_actionbar);
			
			@Override
			public void onClick(View v) {
				boolean isGroup = false;
				if (!mHistoryPathList.isEmpty()) {
					Object mObject = mHistoryPathList.getLast();
					if (mObject instanceof ContactGroup) {
						isGroup  = true;
					}
				}
				if (!isRootList && !isGroup) {
					showWindow(actionbar);
				}
			}
		});
		initSearchContactViewListener();
		mHorizontalListViewAdapter.setAdapterAttr(true,new onRemoveSelectedMemgerListener() {

					@Override
					public void onRemoveSelected(User mUser) {
						//  Auto-generated method stub
						if (mUser.isSelected()) {
							mUser.setSelected(false);
							removeFromUserList(mUser);
						}
						if (mCurrentObjectList.contains(mUser)) {
							int index = mCurrentObjectList.indexOf(mUser);
							User tempUser = (User) mCurrentObjectList
									.get(index);
							tempUser.setSelected(false);
						}
						mHandler.sendEmptyMessage(REFRESH_GALLARY_UI);
						if (mSearchContactEditText.isShowing()
								&& mSearchContactEditText.getEditTextStr().length() > 0) {
							if (mSearchResultObjects.contains(mUser)) {
								int index = mSearchResultObjects.indexOf(mUser);
								User tempUser = (User) mSearchResultObjects
										.get(index);
								tempUser.setSelected(false);
							}
							updateCurrentObjectList(mNewAddUserList, mSearchResultObjects);
							mHandler.sendEmptyMessage(REFRESH_SEARCH_LIST_UI);
						} else {
							updateCurrentObjectList(mNewAddUserList, mCurrentObjectList);
							mHandler.sendEmptyMessage(REFRESH_LIST_UI);
						}
					}
				});
	}

	/**
	 * 创建讨论组的操作
	 * 
	 * @throws IOException
	 */
	private void createGroupAction() {
		if (mNewAddUserList == null || mNewAddUserList.isEmpty()) {
			ToastUtil.toastAlerMessage(this, "未选择成员", Toast.LENGTH_SHORT);
			return;
		} else {
//			if (mNewAddUserList.size()<2) {
//				ToastUtil.toastAlerMessage(this, "成员数量不能少于2个", Toast.LENGTH_SHORT);
//				return;
//			} else {
//				ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
//				if (mClientInitConfig != null) {
//					int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
//					if (mNewAddUserList.size() > maxUsers) {
//						ToastUtil.toastAlerMessage(this, "成员数量超过上限", Toast.LENGTH_SHORT);
//						return;
//					}
//				}
//			}
			ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
			if (mClientInitConfig != null) {
				int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
				if (mNewAddUserList.size() > (maxUsers-1)) {
					ToastUtil.toastAlerMessage(this, "成员数量超过上限", Toast.LENGTH_SHORT);
					return;
				}
			}
		}
		mDisSessionBean = new DisSessionBean();
		String groupNameString = calculateGroupName(mNewAddUserList, null);
		if (AppController.getInstance().mSelfUser != null) {
			mDisSessionBean.setDesc(AppController.getInstance().mSelfUser
					.getUserName()
					+ " 创建于 "
					+ TimeUtil.getCurrenDateTime("yyyy-MM-dd hh:mm:ss"));
		} else {
			mDisSessionBean.setDesc("");
		}
		mDisSessionBean.setSubject(groupNameString);
		mDisSessionBean.setNaturalName(groupNameString);
		mDisSessionBean.setGroupType(DisSessionBean.DISSESSION);
		List<String> memberList = new ArrayList<String>();
		for (User user : mNewAddUserList) {
			memberList.add(user.getUserNo());
		}
		mDisSessionBean.setMemberList(memberList);
		String json = JSON.toJSONString(mDisSessionBean);
		ReqIQ iq = new ReqIQ();
		iq.setAction(1);
		iq.setType(Type.SET);
		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
		iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getInstance(this)
				.getUserAccount()));
		iq.setTo("admin@" + mXmppConnectionManager.getServiceName());
		iq.setParamsJson(json);
		mCreatePacketId = iq.getPacketID();
//		if (NetWorkUtil.isNetworkAvailable(DisCreateActivity.this)) {
//			mXmppConnectionManager.sendPacket(iq);
//			mHandler.sendEmptyMessage(CREATE_SUCCESS);
//		} else {
//			ToastUtil.toastAlerMessage(DisCreateActivity.this, "没有网络",Toast.LENGTH_SHORT);
//		}
		boolean success = sendIqPacketCommon(iq);
		if (success) {
			mHandler.sendEmptyMessage(CREATE_SUCCESS);
		}
	}

	/**
	 * 
	 * 通过讨论组成员信息或name参数，返回讨论组名称
	 * 
	 * @param list
	 * @return
	 */
	public static String calculateGroupName(List<User> list, String name) {
		StringBuilder builder = new StringBuilder();
		if (list == null) {
			return "";
		}

		if (name != null) {
			return name;
		} else {
			if (AppController.getInstance().mSelfUser != null) {
				builder.append(AppController.getInstance().mSelfUser.getUserName());
			}
		}
		for (User user : list) {
			builder.append(user.getUserName());
			if (builder.length()>20) {
				builder = builder.delete(20, builder.length());
				builder.append("...");
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * 
	 * 判断objectList中，User对象集合中，哪些存在在UserList中， 存在则将setSelected(boolean
	 * )传入true，否则传入false
	 * 
	 * @param UserList
	 * @param ObjectList
	 */
	private void updateCurrentObjectList(List<User> userList,
			List<Object> objectList) {
		List<User> list = new ArrayList<User>();
		if (userList != null && !userList.isEmpty()) {
			list.addAll(userList);
		}

		if (list.isEmpty()) {
			//当前页如果没有选择勾选其他人，就判断该页面是否有自己
			int pos = objectList.indexOf(AppController.getInstance().mSelfUser);
			if (pos != -1) {
				User existUser = (User) objectList.get(pos);
				existUser.setExited(true);
			}
		} else {
			//当前页有勾选其他人，判断自己在该页没有
			for (Object obj : objectList) {
				if (obj instanceof User) {
					if (obj.equals(AppController.getInstance().mSelfUser)) {
						((User) obj).setExited(true);
					}
					for (User user : list) {
						// 判断当前显示界面的数据有哪些是已选择的
						if (((User) obj).getUserNo().equals(user.getUserNo())) {
							((User) obj).setSelected(true);
							break;
						}

					}
				}
			}
		}
	}

	/****************************************************************/
	// mGuideList的操作方法
	/***************************************************************/

	/**
	 * 删除最后一个
	 */
	private void removeGuidLastOne() {
		if (!mGuideList.isEmpty()) {
			mGuideList.removeLast();
		}
	}

	/**
	 * 
	 * 获得mGuideList中最后一个OrganizationTree的组织机构ID
	 * 
	 * @return
	 */
	private String getCurrentOrgNum() {
		if (mGuideList.size() > 0) {
			return mGuideList.getLast().getOrgNo();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * 删除mGuideList中指定对象之后的所有单元
	 * 
	 * @param entity
	 */
	private void removeGuideListTail(int position) {
		while ((mGuideList.size() - 1) > position) {
			mGuideList.removeLast();
		}
	}

	/**
	 * 
	 * 根据传入的组织机构ID，查询出当前组织机构下的所有子组织机构和用户， 并且将已选择的用户标识出来
	 * 
	 * @param orgNum
	 */
	private boolean updateCurrentObjectList(String orgNum) {
		if (orgNum == null) {
			return false;
		}
		// 获取用户列表
		ArrayList<User> tempUserList = (ArrayList<User>) mContactOrgDao
				.queryUsersByOrgNo(orgNum);
		// 获得组织列表
		ArrayList<OrganizationTree> tempOrgList = (ArrayList<OrganizationTree>) mContactOrgDao
				.queryOrgListByParentId(orgNum);
		mCurrentObjectList.clear();

		// 成员放在上面
		if (tempUserList != null) {
			for (User user : tempUserList) {
				mCurrentObjectList.add(user);
			}
		}

		// 组织机构放在下面
		if (tempOrgList != null) {
			for (OrganizationTree entity : tempOrgList) {
				mCurrentObjectList.add(entity);
			}
		}

		// 将已在讨论组中的用户标识出来
		updateCurrentObjectList(mNewAddUserList, mCurrentObjectList);

		return true;
	}

	/**
	 * 显示“组织机构，群，讨论组”
	 */
	private void showRootListUI() {
		mCurrentObjectList.clear();
		ContactCommonBean bean = new ContactCommonBean();
		bean.setName("组织机构");
		bean.setNum("0");
		mCurrentObjectList.add(bean);
		ContactCommonBean bean1 = new ContactCommonBean();
		bean1.setName("群");
		mCurrentObjectList.add(bean1);
		ContactCommonBean bean2 = new ContactCommonBean();
		bean2.setName("讨论组");
		mCurrentObjectList.add(bean2);
		mHandler.sendEmptyMessage(REFRESH_LIST_UI);
	}

	/**
	 * 
	 * 显示组织机构跳转按钮布局
	 * 
	 * @param parent
	 */
	private void showWindow(View parent) {
		TextView firstTitleTV;
		if (mOrgTitlePopWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(
					R.layout.contact_orgtitle_popwindow, null);
			firstTitleTV = (TextView) view
					.findViewById(R.id.tv_contact_title_jump_my_org);
			mOrgTitlePopwinList = (ListView) view
					.findViewById(R.id.lv_contact_title_current_path);

			mOrgTitlePopwinList.setAdapter(mOrgPathAdapter);
			// 创建一个PopuWidow对象
			mOrgTitlePopWindow = new PopupWindow(view, parent.getWidth(),
					LayoutParams.WRAP_CONTENT);
			firstTitleTV.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击跳转到当前用户所在的组织机构
					OrganizationTree entity = mContactOrgDao
							.queryMyOrg(DisCreateActivity.this);
					if (entity == null) {
						return;
					}
					resetOrgPathList(entity);
					mHistoryPathList.clear();
					mHistoryPathList.addAll(mGuideList);
					updateCurrentObjectList(entity.getOrgNo());
					closePopWindow();
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				}
			});
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
					mTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
							getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
				}
			});
		}
		mHandler.sendEmptyMessage(REFRESH_GUID_UI);
		mOrgTitlePopWindow.setFocusable(true);
		mOrgTitlePopWindow.update();
		// 设置允许在外点击消失
		ColorDrawable dw = new ColorDrawable(0x00000000);
		mOrgTitlePopWindow.setBackgroundDrawable(dw);
		mOrgTitlePopWindow.setOutsideTouchable(true);

		mOrgTitlePopWindow.showAsDropDown(parent);
		mTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
				getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_up), null);
		mOrgTitlePopwinList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				closePopWindow();
				removeGuideListTail(position);
				mHistoryPathList.clear();
				mHistoryPathList.addAll(mGuideList);
				if (updateCurrentObjectList(getCurrentOrgNum())) {
					// 点击跳转，更新listview
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				}
			}
		});
	}

	/**
	 * 关闭组织机构跳转布局
	 */
	private void closePopWindow() {
		if (mOrgTitlePopWindow != null && mOrgTitlePopWindow.isShowing()) {
			mOrgTitlePopWindow.dismiss();
		}
	}

	/**
	 * 组织机构列表适配器
	 */
	class DisCreateSelectListAdapter extends BaseAdapter {
		private Context nContext;
		ViewHolder viewHolder = null;
		/**
		 * 显示当前界面所需要的数据
		 */
		private List<Object> nListObjects = new ArrayList<Object>();
		private ContactOrgDao mContactOrgDao;

		public DisCreateSelectListAdapter(Context context) {
			nContext = context;
			mContactOrgDao = new ContactOrgDao(nContext);
		}

		public void setData(List<Object> nListObjects) {
			this.nListObjects = nListObjects;
		}

		@Override
		public int getCount() {
			return nListObjects.size();
		}

		@Override
		public Object getItem(int position) {
			return nListObjects.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Object tempResultObject = nListObjects.get(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(
						R.layout.dis_create_select_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvContactItemTag = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_tag);
				viewHolder.logo = (ImageView)convertView.findViewById(R.id.contactChildOrg_img_listItem_logo);
				viewHolder.llContactItemOrg = (RelativeLayout) convertView
						.findViewById(R.id.ll_dis_create_item_org);
				viewHolder.tvContactItemOrgName = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_orgname);
				viewHolder.tvContactItemOrgCount = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_personcount);

				viewHolder.llContactItemPerson = (LinearLayout) convertView
						.findViewById(R.id.ll_dis_create_item_person);
				viewHolder.ivContactUserAvatar = (CircleImageView) convertView.findViewById(R.id.iv_dis_create_item_personicon);
				viewHolder.tvContactItemPersonName = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_personname);
				viewHolder.ivContactSelected = (ImageView) convertView
						.findViewById(R.id.cb_dis_create_item_checkBox);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.tvContactItemOrgCount.setVisibility(View.VISIBLE);
			viewHolder.tvContactItemTag.setVisibility(View.GONE);
			viewHolder.llContactItemOrg.setVisibility(View.GONE);
			viewHolder.llContactItemPerson.setVisibility(View.GONE);
			viewHolder.ivContactSelected.setEnabled(true);

			if (tempResultObject instanceof User) {// 用户
				Object temp = null;
				if (position > 0) {
					temp = nListObjects.get(position - 1);
				}
				// 当前的position是第一个或者前一个item是组织机构，则显示tag
				if (temp == null || temp instanceof OrganizationTree) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("成员");
				}

				User tempUser = (User) nListObjects.get(position);
				if (tempUser.isExited()) {
					viewHolder.ivContactSelected.setImageResource(R.mipmap.report_option_selelcting);
				} else {
					if (tempUser.isSelected()) {
						viewHolder.ivContactSelected.setImageResource(R.mipmap.report_option_selelcting);
					} else {
						viewHolder.ivContactSelected.setImageResource(R.mipmap.report_option_not_selelcted);
					}
				}

				int avatar=-1;
				if (tempUser.getGender() == 1) {//对方是男性
					avatar = R.mipmap.session_p2p_men;
				} else if (tempUser.getGender() == 2) {//对方是女性
					avatar = R.mipmap.session_p2p_woman;
				} else {avatar= R.mipmap.session_no_sex;}
				//设置头像
				File userIcon = FileUtil.getAvatarByName(tempUser.getUserNo());
				if (userIcon == null || !userIcon.exists()) {
					viewHolder.ivContactUserAvatar.setImageResource(avatar);
				} else {
					viewHolder.ivContactUserAvatar.setImageURI(Uri.fromFile(userIcon));
				}

				viewHolder.llContactItemPerson.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemPersonName.setText(tempUser.getUserName());
				viewHolder.llContactItemPerson.setTag(position);
				viewHolder.llContactItemPerson.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						int p = (Integer) v.getTag();
						User user = (User) nListObjects.get(p);
						if (user.isExited()) {
							return;
						}
						if (user.isSelected()) {
							user.setSelected(false);
							removeFromUserList(user);
						} else {
							int mSumUser = mNewAddUserList.size();                                                                                                                                          
							if (mSumUser > mMaxMemberNum-2) {//去掉自己
								mHandler.sendEmptyMessage(OVER_FLOW_MAX_MEMBERS);
								return;
							} 
							user.setSelected(true);
							addToUserList(user);
						}
						notifyDataSetChanged();
						mHandler.sendEmptyMessage(REFRESH_GALLARY_UI);
					}
				});
//				viewHolder.mCheckBox.setTag(position);
//				viewHolder.mCheckBox
//						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//							@Override
//							public void onCheckedChanged(
//									CompoundButton buttonView, boolean isChecked) {
//								//  Auto-generated method stub
//								int position = (Integer) buttonView.getTag();
//								User user = (User) nListObjects.get(position);
//								if (isChecked) {
//									user.setSelected(true);
//
//									// 新添加的用户，则将该标识设置为false，方便刷新UI
//									user.setExited(false);
//									addToUserList(user);
//								} else {
//									user.setSelected(false);
//									removeFromUserList(user);
//								}
//								mHandler.sendEmptyMessage(REFRESH_GALLARY_UI);
//							}
//						});
			}

			if (tempResultObject instanceof OrganizationTree) {// 组织机构
				// 随机设置LOGO
				int logoBackground = ContactCommonAdapter.LOGO_BACKGROUND[(int)(Math.random()*6)];
				viewHolder.logo.setBackgroundResource(logoBackground);

				Object temp = null;
				if (position >= 1) {
					temp = nListObjects.get(position - 1);
				}
				// 当前的position是第一个或者前一个item是用户，则显示tag
				if (temp == null || temp instanceof User) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("部门");
				}
				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				List<User> userList=new ArrayList<>();
				mContactOrgDao.getOrgUserByOrgIdFromDb((OrganizationTree) nListObjects.get(position),userList);
				int index=0;
				for (User user:userList) {
					if (user.getUserStatus()==0){//离线人数
						index++;
					}
				}
				viewHolder.tvContactItemOrgCount.setText("("+(userList.size()-index)+"/"+userList.size()+")");

				viewHolder.tvContactItemOrgName
						.setText(((OrganizationTree) nListObjects.get(position))
								.getOrgName());
				viewHolder.llContactItemOrg
						.setTag(nListObjects.get(position));
				viewHolder.llContactItemOrg
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								//  Auto-generated method stub
								isRootList = false;
								if (mSearchContactEditText.isShowing()) {
									cancelSearchContactAnimation();
								}
								OrganizationTree entity = (OrganizationTree) v
										.getTag();
								resetOrgPathList(entity);
								mHistoryPathList.add(entity);
								updateCurrentOrgList(entity);
							}
						});
			}

			if (tempResultObject instanceof ContactCommonBean) {// 联系界面顶层分类（组织机构、群、讨论组）
				// 随机设置LOGO
				viewHolder.logo.setImageResource(0);
				if (position==0){
					viewHolder.logo.setBackgroundResource(R.mipmap.contact_org_green);
				}else if (position==1){
					viewHolder.logo.setBackgroundResource(R.mipmap.session_group);
				}else if (position==2){
					viewHolder.logo.setBackgroundResource(R.mipmap.session_join_discus);
				}

				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemOrgName
						.setText(((ContactCommonBean) nListObjects
								.get(position)).getName());
				viewHolder.llContactItemOrg.setTag(position);
				viewHolder.tvContactItemOrgCount.setVisibility(View.GONE);
				viewHolder.llContactItemOrg
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								//  Auto-generated method stub
								isRootList = false;
								switch ((Integer) v.getTag()) {
								case 0:
									OrganizationTree entity = new OrganizationTree("0", "-1", "组织机构", 0, "0", 0);
									resetOrgPathList(entity);
									mHistoryPathList.add(entity);
									updateCurrentOrgList(entity);
									break;
								case 1:// 群
									ContactGroup tempGroup = new ContactGroup();
									tempGroup.setGroupName("0");
									tempGroup.setNaturalName("群组");
									tempGroup.setGroupType(Const.CONTACT_GROUP_TYPE);
//									addGroupGuideList(tempGroup);
									mHistoryPathList.add(tempGroup);
									updateCurrentGroupList(tempGroup);
									break;
								case 2:// 讨论组
									ContactGroup tempDisGroup = new ContactGroup();
									tempDisGroup.setGroupName("0");
									tempDisGroup.setNaturalName("讨论组");
									tempDisGroup.setGroupType(Const.CONTACT_DISGROUP_TYPE);
//									addGroupGuideList(tempDisGroup);
									mHistoryPathList.add(tempDisGroup);
									updateCurrentGroupList(tempDisGroup);
									break;

								default:
									break;
								}
							}
						});
			}

			if (tempResultObject instanceof ContactGroup) {// 群组
				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				viewHolder.logo.setImageResource(0);
				ContactGroup bean = (ContactGroup) tempResultObject;
				if (bean.getGroupType() == 8) { // 群组
					viewHolder.tvContactItemOrgName.setText(bean.getNaturalName());
					viewHolder.logo.setBackgroundResource(R.mipmap.session_group);
				} else { // 讨论组
					int avatar=-1;
					ContactGroupUser group = mContactOrgDao.getContactGroupUserById(bean.getGroupName(), LastLoginUserSP.getLoginUserNo(nContext), Const.CHAT_TYPE_DIS);
					if (group!=null&&group.getRole()==10) {//自己创建的讨论组
						avatar = R.mipmap.session_creat_discus;
					} else {//自己加入的讨论组
						avatar = R.mipmap.session_join_discus;
					}
					viewHolder.logo.setBackgroundResource(avatar);
					if (bean.getSubject() != null && !bean.getSubject().isEmpty()) {
						viewHolder.tvContactItemOrgName.setText(bean.getSubject());
					} else {
						viewHolder.tvContactItemOrgName.setText(bean.getNaturalName());
					}
				}
				List<User> list = mContactOrgDao.queryUsersByGroupName(
						bean.getGroupName(), bean.getGroupType());
				int mUserCount = 0;
				if (list != null) {
					mUserCount = list.size();// 总人数
				}
				int index=0;
				for (User user: list) {
					if (user.getUserStatus()==0){
						index++;
					}
				}
				viewHolder.tvContactItemOrgCount.setText("("+(mUserCount-index)+"/"+mUserCount+")");
				viewHolder.llContactItemOrg.setTag(bean);
				viewHolder.llContactItemOrg.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//  Auto-generated method stub
						isRootList = false;
						if (mSearchContactEditText.isShowing()) {
							cancelSearchContactAnimation();
						}
						ContactGroup tempBean = (ContactGroup) v.getTag();
//						addGroupGuideList(tempBean);
						mHistoryPathList.add(tempBean);
						updateCurrentGroupList(tempBean);
					}
				});
			}
			return convertView;
		}

		class ViewHolder {
			public TextView tvContactItemPersonName;
			public CircleImageView ivContactUserAvatar;
			public ImageView logo;
			public LinearLayout llContactItemPerson;
			public TextView tvContactItemOrgCount;
			public TextView tvContactItemOrgName;
			public RelativeLayout llContactItemOrg;
			public TextView tvContactItemTag;
			public ImageView ivContactSelected;
		}
	}

	/**
	 * 
	 * 添加entity到gallery的数据链表中
	 * 
	 * @param user
	 */
	private void addToUserList(User user) {
		user.setExited(false);
		mNewAddUserList.add(user);
	}

	/**
	 * 从gallery数据中删除entity
	 * 
	 * @param user
	 */
	private void removeFromUserList(User user) {
		for (User entity : mNewAddUserList) {
			if (entity.getUserNo().equals(user.getUserNo())) {
				mNewAddUserList.remove(entity);
				return;
			}
		}
	}
	
	/**
	 * 返回功能
	 */
	private void back() {
		if (!mHistoryPathList.isEmpty()) {
			mHistoryPathList.removeLast();
		}
		if (mHistoryPathList.isEmpty()) {
			if (!isRootList) {
				showRootListUI();
				isRootList = true;
				mGuideList.clear();
			} else {
				finish();
			}
		} else {
			Object nowOrg = mHistoryPathList.getLast();
			if (nowOrg instanceof OrganizationTree) {
				resetOrgPathList((OrganizationTree) nowOrg);
				updateCurrentOrgList((OrganizationTree) nowOrg);
			} else if (nowOrg instanceof ContactGroup) {
				updateCurrentGroupList((ContactGroup) nowOrg);
			} 
		}
	}
	@Override
	public void onBackPressed() {
		back();
	}

	/**
	 * @param entity
	 */
	protected void updateCurrentOrgList(OrganizationTree entity) {
		if (entity.getOrgNo().equals("0")) {
			ArrayList<OrganizationTree> list1 = (ArrayList<OrganizationTree>) mContactOrgDao
					.queryOrgListByParentId("0");
			mCurrentObjectList.clear();
			if (list1 != null && !list1.isEmpty()) {
				for (OrganizationTree entity1 : list1) {
					mCurrentObjectList.add(entity1);
				}
			}
		} else {
			updateCurrentObjectList(entity.getOrgNo());
		}
		mHandler.sendEmptyMessage(REFRESH_LIST_UI);
	}
	
	/**
	 * @param tempBean
	 */
	protected void updateCurrentGroupList(ContactGroup tempBean) {
		//  Auto-generated method stub
		if (tempBean.getGroupName().equals("0")) {
			List<ContactGroup> list;
			if (tempBean.getGroupType() == Const.CONTACT_GROUP_TYPE) {
				list = mContactOrgDao.queryGroupList(Const.CONTACT_GROUP_TYPE);
			} else {
				list = mContactOrgDao.queryGroupList(Const.CONTACT_DISGROUP_TYPE);
			}
			mCurrentObjectList.clear();
			if (list != null && !list.isEmpty()) {
				for (ContactGroup entity2 : list) {
					mCurrentObjectList.add(entity2);
				}
			}
		} else {
			List<User> list = mContactOrgDao.queryUsersByGroupName(
					tempBean.getGroupName(), tempBean.getGroupType());
			mCurrentObjectList.clear();
			if (list != null && !list.isEmpty()) {
				for (User entity : list) {
					mCurrentObjectList.add(entity);
				}
				updateCurrentObjectList(mNewAddUserList,mCurrentObjectList);
			}
		}
		mHandler.sendEmptyMessage(REFRESH_LIST_UI);
	}
	
	private void findSearchContactView() {
		mSearchContactEditText = new SearchContactEditText(this);
		mSearchContactEditText.setSessionFragment(false,false,true);
		mSearchContactEditText.setmHorizontalListViewAdapter(mHorizontalListViewAdapter);
		mSearchContactEditText.setOnResultSearchedListener(true, new onResultSearchedListener() {
			
			@Override
			public void onResultSearched(List<Object> tempSearchResultObjects) {
				
//				if (mSearchResultObjects != null) {
//					updateCurrentObjectList(mNewAddUserList,mSearchResultObjects);
//					if (mSearchListAdapter != null) {
//						mSearchListAdapter.setData(mSearchResultObjects);
//					} else {
//						mSearchListAdapter = new DisCreateSelectListAdapter(DisCreateActivity.this);
//						mSearchListAdapter.setData(mSearchResultObjects);
//						mSearchContactEditText.mContactSearchListView.setAdapter(mSearchListAdapter);
//					}
//					//判断是否显示空数据界面
//					if (mSearchContactEditText.mSearchEditText.getText().length()>0 && mSearchResultObjects.size() <= 0) {
//						mSearchContactEditText.mContactSearchListView.setEmptyView(mSearchContactEditText.mContactSearchResultEmptyTV);
//					} else {
//						mSearchContactEditText.mContactSearchResultEmptyTV.setVisibility(View.GONE);
//						mSearchContactEditText.mContactSearchListView.setEmptyView(null);
//					}
//					mSearchListAdapter.notifyDataSetChanged();
//				}
				
				if (mSearchContactEditText.getEditTextStr().length() > 0) {
					if (tempSearchResultObjects != null) {
						mSearchResultObjects = tempSearchResultObjects;
						updateCurrentObjectList(mNewAddUserList,mSearchResultObjects);
						if (mSearchContactEditText.mContactSearchListView.getAdapter() == null) {
							mSearchContactEditText.mContactSearchListView.setAdapter(mDisCreateSelectListAdapter);
						}
						mHandler.sendEmptyMessage(REFRESH_SEARCH_LIST_UI);
					}
				} else {
					mSearchContactEditText.mContactSearchListView.setAdapter(null);
					mSearchContactEditText.mContactSearchResultEmptyTV.setVisibility(View.GONE);
					mSearchContactEditText.mContactSearchListView.setEmptyView(null);
					updateCurrentObjectList(mNewAddUserList, mCurrentObjectList);
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				}
			}
		});
		mRel_searchBox = (RelativeLayout) findViewById(R.id.searchBox);
		mCreateDisGroupLayoutLL = (LinearLayout) findViewById(R.id.ll_create_disgroup_layout);
	}
	
	private void initSearchContactViewListener() {
		mRel_searchBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				isShowSearchEditText = true;
				LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams) mRel_searchBox
						.getLayoutParams();
				searchViewY = mRel_searchBox.getY() - etParamTest.topMargin;
				TranslateAnimation showAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
				showAnimation.setDuration(200);
				showAnimation.setAnimationListener(showAnimationListener);
				mCreateDisGroupLayoutLL.startAnimation(showAnimation);
			}
		});
		mSearchContactEditText.setOnCancelSearchAnimationListener(this);
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
				mHandler.sendEmptyMessage(DisAddActivity.SHOW_SEARCH_VIEW);
			} else {
				mHandler.sendEmptyMessage(DisAddActivity.CANCEL_SEARCH_VIEW);
			}
		}
	};
	
	@Override
	public void cancelSearchContactAnimation() {
		isShowSearchEditText = false;
		mSearchContactEditText.dismiss();
		TranslateAnimation cancelAnimation = new TranslateAnimation(0, 0, 0, searchViewY);
		cancelAnimation.setDuration(200);
		cancelAnimation.setAnimationListener(showAnimationListener);
		mCreateDisGroupLayoutLL.startAnimation(cancelAnimation);		
	}
	
	/**
	 * 根据跳转到的org，寻找该org所属的组织机构
	 * 
	 * @param tempOrg
	 */
	protected void resetOrgPathList(OrganizationTree tempOrg) {
		ArrayList<OrganizationTree> mMyOrgPathList = new ArrayList<OrganizationTree>();
		mGuideList.clear();
		mGuideList.addFirst(new OrganizationTree("0", "-1", "组织机构", 0, "0", 0));
		mContactOrgDao.queryOrgBelongListByOrgNo(tempOrg, mMyOrgPathList);
		if (mMyOrgPathList.size() > 0) {
			Collections.reverse(mMyOrgPathList);
		}
		mGuideList.addAll(mMyOrgPathList);
		if (!tempOrg.getOrgNo().equals("0")) {
			mGuideList.add(tempOrg);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_search_contact_create_disgroup_btn:
			mCreateBtn.performClick();
			break;	
		case R.id.disAdd_previous:
			back();
			break;
		default:
			break;
		}

	}
	
	/**
	 * 更新标题栏标题
	 */
	protected void updateTitleView() {
		if (!mHistoryPathList.isEmpty()) {
			Object mObject = mHistoryPathList.getLast();
			if (mObject instanceof ContactGroup) {
				ContactGroup tempObject = (ContactGroup) mObject;
				if (tempObject.getGroupType() == Const.CONTACT_GROUP_TYPE) {
					if (tempObject.getGroupName().equals(0)) {
						mTitleTV.setText("群组");
					} else {
						mTitleTV.setText(tempObject.getNaturalName());
					}
				} else {
					if (tempObject.getGroupName().equals(0)) {
						mTitleTV.setText("讨论组");
					} else {
						if (tempObject.getSubject() !=null && !tempObject.getSubject().isEmpty()) {
							mTitleTV.setText(tempObject.getSubject());
						} else {
							mTitleTV.setText(tempObject.getNaturalName());
						}
					}
				}
			} else if (mObject instanceof OrganizationTree) {
				OrganizationTree tempObject = (OrganizationTree) mObject;
				mTitleTV.setText(tempObject.getOrgName());
				mTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
			}
		} else {
			mTitleTV.setText("选择联系人");
			mTitleTV.setCompoundDrawablesWithIntrinsicBounds(null, null,null, null);
		}		
	}
	
//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//
//		// 接收到回执信息，判断讨论组是否创建成功
//		if (mCreatePacketId.equals(packet.getPacketID())) {
//			if (packet.getCode() == 200) {
//				String resultJson = packet.getResp();
//				L.i("resultJson == "+resultJson);
//				String groupName = null;
//				try {
//					JSONObject mJsonObject = new JSONObject(resultJson);
//					groupName = mJsonObject.optString("groupName");
//				} catch (JSONException e) {
//					//  Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				if (groupName != null) {
//					insertContactGroupDataToDB(mDisSessionBean,groupName);
//					//发送创建讨论组成功的广播
//					Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_CREATE_GROUP);
//					sendBroadcast(updateViewIntent);
//				}
//				mHandler.sendEmptyMessage(CREATE_SUCCESS);
//			} else {
//				mHandler.sendEmptyMessage(CREATE_FAILED);
//			}
//		}
//	}
	
//	/**
//	 * 添加讨论组信息到数据库
//	 * @param bean 讨论组实例
//	 * @param groupName IQ回执的讨论组ID
//	 */
//	public void insertContactGroupDataToDB(DisSessionBean bean, String groupName) {
//		ContactGroup mContactGroup = new ContactGroup();
//		mContactGroup.setGroupName(groupName);
//		mContactGroup.setNaturalName(bean.getNaturalName());
//		mContactGroup.setDesc(bean.getDesc());
//		mContactGroup.setSubject(bean.getSubject());
//		ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
//		if (mClientInitConfig != null) {
//			int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
//			mContactGroup.setMaxUsers(maxUsers);
//		}
//		mContactGroup.setGroupType(Const.CONTACT_DISGROUP_TYPE);
//		mContactGroup.setCreateTime(System.currentTimeMillis()+"");
//		mContactGroup.setCreateUser(AppController.getInstance().mMyUser.getUserNo());
//		
//		//插入一条讨论组记录
//		mContactOrgDao.insertOneContactGroupData(mContactGroup, Const.CONTACT_DISGROUP_TYPE);
//		
//		for (String userNo : bean.getMemberList()) {
//			ContactGroupUser tempGroupUser = new ContactGroupUser();
//			tempGroupUser.setGroupName(groupName);
//			tempGroupUser.setUserNo(userNo);
//			tempGroupUser.setJid(userNo);
//			tempGroupUser.setRole(50);
//			//插入一条讨论组成员信息
//			mContactOrgDao.insertOneGroupUserRelationData(tempGroupUser,Const.CONTACT_DISGROUP_TYPE);
//		}
//	}
	
//	@Override
//	protected void onDestroy() {
//		//  Auto-generated method stub
//		super.onDestroy();
//		mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
//	}
}
