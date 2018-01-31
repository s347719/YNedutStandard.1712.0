package com.yineng.ynmessager.activity.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.ViewHolder;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;

import java.util.ArrayList;

public class ContactFragment extends BaseFragment implements onCancelSearchAnimationListener{
	private ListView mContactOrgLV;
	private Context mContext;
	private ContactOrgDao mContactOrgDao;
	private int[] mRootTitle = {R.string.main_org,R.string.main_group,R.string.main_discussion};
	private int[] mRootIcon = {R.mipmap.contact_org_green,R.mipmap.session_group,R.mipmap.session_join_discus};
//	private ImageView mImg_showUserCenter;
	private RelativeLayout mContactRelativeLayout;
	/***搜索联系人功能***/
	
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
	
	/**
	 * 自定义搜索框
	 */
	private SearchContactEditText mSearchContactEditText;
	
	/**
	 * 点击刷新按钮执行的广播意图
	 */
	private Intent mRefreshContactIntent = new Intent(XmppConnService.BROADCAST_ACTION_REFRESH_CONTACT);
	
	private Handler mHandler = new Handler() {
		@Override
        public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_SEARCH_VIEW:
				mSearchContactEditText.show();
				mContactRelativeLayout.setY(-searchViewY);
				break;
			case CANCEL_SEARCH_VIEW:
				mContactRelativeLayout.setY(0);
				break;
			default:
				break;
			}
		}
	};
	private ImageView mRefreshContactBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getActivity();
		mContactOrgDao = new ContactOrgDao(mContext);
//		mRootOrg = mContactOrgDao.queryOrgListByParentId("0");
//		mContactOrgDao.queryAllOrgListByParentId(OrganizationTree.getInstance(),"0");
//		mRootOrg = (ArrayList<OrganizationTree>) OrganizationTree.mOrganizationTree.getChildOrgTreeMap().get("0");
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_contact_layout, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViews(view);
		initListener();
	}
	
	private void findViews(View view) {
		mContactOrgLV = (ListView) view.findViewById(R.id.contact_org_listview);
//		mImg_showUserCenter = (ImageView)view.findViewById(R.id.main_img_contact_showUserCenter);
		mRefreshContactBtn = (ImageView) view.findViewById(R.id.contact_org_create_dis_group);
		mRefreshContactBtn.setVisibility(View.VISIBLE);
		mRefreshContactBtn.setImageResource(R.mipmap.refresh);
		mRefreshContactBtn.setScaleX(0.6f);
		mRefreshContactBtn.setScaleY(0.6f);
		findSearchContactView(view);
	}
	
	private void findSearchContactView(View view) {
		mSearchContactEditText = new SearchContactEditText(mContext);
		mSearchContactEditText.setSessionFragment(false,true,false);
		mRel_searchBox =  (RelativeLayout) view.findViewById(R.id.searchBox);
		mContactRelativeLayout= (RelativeLayout) view.findViewById(R.id.ll_contact_org_frame);
	}
	
	private void initListener() {
//		mImg_showUserCenter.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v)
//			{
//				((MainActivity)mParentActivity).showMenu();
//			}
//		});
		mRefreshContactBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (NetWorkUtil.isNetworkAvailable(mContext)) {
					final MainActivity mainActivity = (MainActivity) getActivity();
					if (mainActivity != null){
						mainActivity.showProgressD("正在加载数据...",true);
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								mainActivity.hideProgessD();
							}
						}, 2500);
					}
					mContext.sendBroadcast(mRefreshContactIntent);
				} else {
					ToastUtil.toastAlerMessageBottom(mContext, "网络不可用，请稍后重试", 1000);
				}
			}
		});
		initSearchContactViewListener();
		mContactOrgLV.setAdapter(new SampleAdapter(mContext));
		mContactOrgLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/****************通过数据库去获取组织机构*********************/
//				Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
//				childOrgIntent.putExtra("childOrgTitle", mRootOrg.get(arg2).getOrgName());
//				//必须转为ArrayList 才能通过PutExtra把list传到子activity
//				ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId(mRootOrg.get(arg2).getOrgNo());
//				if (tempList != null) {
//					childOrgIntent.putExtra("childOrg", tempList);
//				}
//				ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao.queryUsersByOrgNo(mRootOrg.get(arg2).getOrgNo());
//				if (tempUsers != null) {
//					childOrgIntent.putExtra("childOrgUser", tempUsers);
//				}
//				startActivity(childOrgIntent);
				
				switch (arg2) {
				case 0:
					/*****************通过对象去获取组织机构**********************/
					Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
//					childOrgIntent.putExtra("childOrgTitle", root[arg2]);
					//必须转为ArrayList 才能通过PutExtra把list传到子activity
					ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId("0");
					if (tempList != null) {
						childOrgIntent.putExtra("childOrgList", tempList);
					}
					// 表示进入子组织机构界面
					OrganizationTree firstOrganizationTree = new OrganizationTree("0", "-1", getString(mRootTitle[arg2]), 0, "0", 0);
					childOrgIntent.putExtra("parentOrg",firstOrganizationTree);
					startActivity(childOrgIntent);
					break;
				case 1:
				case 2:
					ArrayList<ContactGroup> mContactGroupList;
					Intent groupIntent = new Intent(mContext, ContactGroupOrgActivity.class);
					groupIntent.putExtra("childGroupTitle", getString(mRootTitle[arg2]));
					if (arg2 == 1) {
						mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(8);
						groupIntent.putExtra("groupType", 8);
					} else {
						mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(9);
						groupIntent.putExtra("groupType", 9);
					}
					if (mContactGroupList != null) {
						groupIntent.putExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME, mContactGroupList);
					}
					startActivity(groupIntent);
					break;
				default:
					break;
				}
			}
		});
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
		RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mRel_searchBox
				.getLayoutParams();
		searchViewY = mRel_searchBox.getY() - etParamTest.topMargin;
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
	
	public class SampleAdapter extends BaseAdapter {

		private Context nContext;
		public SampleAdapter(Context context) {
			nContext = context;
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(R.layout.contact_org_orglist_item, parent,false);
				TextView mOrgUserCountTV = (TextView) convertView.findViewById(R.id.tv_contact_orglist_item_count);
				mOrgUserCountTV.setText("");
			}
			ImageView icon = ViewHolder.get(convertView,R.id.main_contact_listItem_logo);
			TextView title = ViewHolder.get(convertView,R.id.tv_contact_orglist_item_name);
			
			title.setText(mRootTitle[position]);
			icon.setImageResource(mRootIcon[position]);
			icon.setVisibility(View.VISIBLE);
			
			return convertView;
		}

		@Override
		public int getCount() {
			//  Auto-generated method stub
			return mRootTitle.length;
		}

		@Override
		public Object getItem(int arg0) {
			//  Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			//  Auto-generated method stub
			return 0;
		}
	}
	
}
