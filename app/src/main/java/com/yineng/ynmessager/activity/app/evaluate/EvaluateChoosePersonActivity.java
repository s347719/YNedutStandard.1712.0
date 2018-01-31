//***************************************************************
//*    2015-10-12  上午11:51:10
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.activity.app.evaluate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.alibaba.fastjson.JSONObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBaseActivity;
import com.yineng.ynmessager.adapter.EvaPersonAdapter;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.bean.app.evaluate.EvaPersonBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaPlanBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaTeachersBean;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.EvaluateSearchTeacherView;
import com.yineng.ynmessager.view.EvaluateSearchTeacherView.OnSearchItemClickListener;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅 选人界面
 */
public class EvaluateChoosePersonActivity extends AppBaseActivity {

	/**
	 * 上下拉刷新控件
	 */
	private PullToRefreshGridView mPullToRefreshGridView;
	/**
	 * 表格控件
	 */
	private GridView mRefreshGridView;
	/**
	 * 全部人员
	 */
	private List<EvaPersonBean> mAllEvaPersonList = new ArrayList<EvaPersonBean>();
	/**
	 * 待评人员
	 */
	private List<EvaPersonBean> mNotEvaPersonList = new ArrayList<EvaPersonBean>();
	/**
	 * 已评人员
	 */
	private List<EvaPersonBean> mEvaedPersonList = new ArrayList<EvaPersonBean>();
	
	/**
	 * 人员的适配器
	 */
	private EvaPersonAdapter mEvaPersonAdapter = new EvaPersonAdapter(EvaluateChoosePersonActivity.this);
//	
//	/**
//	 * 待评人员的适配器
//	 */
//	private EvaPersonAdapter mWaitEvaPersonAdapter = new EvaPersonAdapter(EvaluateChoosePersonActivity.this);
//	/**
//	 * 已评人员的适配器
//	 */
//	private EvaPersonAdapter mEvaedPersonAdapter = new EvaPersonAdapter(EvaluateChoosePersonActivity.this);
	
	private AsyncHttpClient mAsyncHttpClient;
	
	/**
	 * 筛选框
	 */
	private RadioGroup mFilterEvaPersonRG;
	
	/**
	 * 搜索界面
	 */
	private EvaluateSearchTeacherView mSearchEvaluateSearchTeacherView;
	
	/**
	 * 搜索框显示动画
	 */
	private Animation visbleAnimation;
	/**
	 * 搜索框隐藏动画
	 */
	private Animation inVisbleAnimation;
	
	/**
	 * 评教计划ID
	 */
	private String mPlanId = null;
	/**
	 * 全部页码
	 */
	private int mAllPageIndex = 0;
	/**
	 * 待评页码
	 */
	private int mWaitPageIndex = 0;
	/**
	 * 已评页码
	 */
	private int mEvaedPageIndex = 0;
	/**
	 * 每页数据总数
	 */
	private int mPageSize = 40;
	
	/**
	 * 1表示全部
	 */
	private final int TYPE_ALL_EVA_PERSON = 1;
	
	/**
	 * 2表示待评
	 */
	private final int TYPE_WAIT_EVA_PERSON = 2;
	
	/**
	 * 3表示已评
	 */
	private final int TYPE_ALREADY_EVAED_PERSON = 3;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
        public void handleMessage(Message msg) {
			switch (msg.what) {
			case PULL_DOWN_TO_REFRESH:
				refreshFirstPage();
				break;
			case PULL_UP_TO_REFRESH:
				switch (mFilterEvaPersonRG.getCheckedRadioButtonId()) {
				case R.id.app_evaluate_choose_person_rb_all:
					if (mAllEvaPersonList.size() > 0) {
//						loadServerJsonData(mAllPageIndex + 1,TYPE_ALL_EVA_PERSON);
						requestCurrentPageView(false,TYPE_ALL_EVA_PERSON);
					} else {
						requestCurrentPageView(true,TYPE_ALL_EVA_PERSON);
					}
					break;
				case R.id.app_evaluate_choose_person_rb_not_eva:
					if (mNotEvaPersonList.size() > 0) {
//						loadServerJsonData(mWaitPageIndex + 1,TYPE_WAIT_EVA_PERSON);
						requestCurrentPageView(false,TYPE_WAIT_EVA_PERSON);
					} else {
						requestCurrentPageView(true,TYPE_WAIT_EVA_PERSON);
					}
					break;
				case R.id.app_evaluate_choose_person_rb_evaed:
					if (mEvaedPersonList.size() > 0) {
//						loadServerJsonData(mEvaedPageIndex + 1,TYPE_ALREADY_EVAED_PERSON);
						requestCurrentPageView(false,TYPE_ALREADY_EVAED_PERSON);
					} else {
						requestCurrentPageView(true,TYPE_ALREADY_EVAED_PERSON);
					}
					break;
				default:
					break;
				}
				break;
			case REFRESH_NO_MORE_DATA:
				// 没有更多了
				ToastUtil.toastAlerMessageBottom(EvaluateChoosePersonActivity.this,
						"没有更多了", 700);
				break;
			default:
				break;
			}
		}
	};
	private EvaPlanBean mPlanObject;
	private String mUserType;
	private RadioButton mAllRadioBt;
	private RadioButton mAllUnevaRadioBt;
	private RadioButton mAllEvaedRadioBt;
	private String mTotalEvaPersonCount;
	private String mEvaedPersonCount;
	
	/**
	 * 请求当前页数据
	 * @param isPullDownToRefresh 是否是下拉刷新
	 * @param requestType 1 全部， 2 待评 ， 3 已评
	 */
	protected void requestCurrentPageView(boolean isPullDownToRefresh ,int requestType) {
		switch (requestType) {
		case TYPE_ALL_EVA_PERSON:
			if (isPullDownToRefresh) {
				mAllPageIndex = 0;
				mAllEvaPersonList.clear();
				loadServerJsonData(mAllPageIndex,TYPE_ALL_EVA_PERSON);
			} else {
				loadServerJsonData(mAllPageIndex+1,TYPE_ALL_EVA_PERSON);
			}
			break;
		case TYPE_WAIT_EVA_PERSON:
			if (isPullDownToRefresh) {
				mWaitPageIndex = 0;
				mNotEvaPersonList.clear();
				loadServerJsonData(mWaitPageIndex,TYPE_WAIT_EVA_PERSON);
			} else {
				loadServerJsonData(mWaitPageIndex + 1,TYPE_WAIT_EVA_PERSON);
			}
			break;
		case TYPE_ALREADY_EVAED_PERSON:
			if (isPullDownToRefresh) {
				mEvaedPageIndex = 0;
				mEvaedPersonList.clear();
				loadServerJsonData(mEvaedPageIndex,TYPE_ALREADY_EVAED_PERSON);
			} else {
				loadServerJsonData(mEvaedPageIndex + 1,TYPE_ALREADY_EVAED_PERSON);
			}
			
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAppContentView(R.layout.activity_app_evaluate_choose_person);
		initView();
		initViewListener();
	}

	public void initView() {
		setTitleLeftIcon(R.mipmap.app_evaluate_choose_back);
		setTitleRightIcon(R.mipmap.app_evaluate_choose_search);
		setTitleName("选择评教对象");
		setTitleRightVisible();
		findViews();
		mUserType = getIntent().getStringExtra("AppUserType");
		mPlanObject = (EvaPlanBean) getIntent().getSerializableExtra("planObject");
		if (mPlanObject != null) {
			mPlanId = mPlanObject.getId();
		}
		visbleAnimation = AnimationUtils.loadAnimation(EvaluateChoosePersonActivity.this, R.anim.slide_right_in);
		inVisbleAnimation = AnimationUtils.loadAnimation(EvaluateChoosePersonActivity.this, R.anim.slide_right_out);
	}

	/**
	 * 找到视图并初始化
	 */
	private void findViews() {
		mPullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.app_evaluate_choose_person_gridview);
		mPullToRefreshGridView.setMode(Mode.DISABLED);
		mRefreshGridView = mPullToRefreshGridView
				.getRefreshableView();
		mRefreshGridView.setPadding(25, 25, 25, 0);
		mRefreshGridView.setHorizontalSpacing(25);
		mRefreshGridView.setVerticalSpacing(25);
		mRefreshGridView.setNumColumns(4);
		mFilterEvaPersonRG = (RadioGroup) findViewById(R.id.app_evaluate_choose_person_radiogroup);
		mSearchEvaluateSearchTeacherView = (EvaluateSearchTeacherView) findViewById(R.id.app_evalute_search_person_view);
	}

	public void initViewListener() {
		mPullToRefreshGridView
				.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

					// 下拉刷新
					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						dismissContentStatusView();
						mHandler.sendEmptyMessageDelayed(PULL_DOWN_TO_REFRESH, 500);
					}

					// 上拉加载更多
					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						mHandler.sendEmptyMessageDelayed(PULL_UP_TO_REFRESH, 500);
					}
				});
		mRefreshGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//点击事件
				List<EvaPersonBean> list = mEvaPersonAdapter.getEvaPersonList();
				if (list != null && list.size() > 0 && position < list.size()) {
					EvaPersonBean onClickPerson = list.get(position);
					startAnswerQuesActivity(onClickPerson);
				}
			}
		});
		mFilterEvaPersonRG.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				dismissContentStatusView();
				L.e("onCheckedChanged");
				switch (checkedId) {
				case R.id.app_evaluate_choose_person_rb_all:
					if (mAllEvaPersonList.size() == 0) {
						mPullToRefreshGridView.setMode(Mode.DISABLED);
						requestCurrentPageView(true,TYPE_ALL_EVA_PERSON);
					} else {
						mPullToRefreshGridView.setMode(Mode.BOTH);
						mEvaPersonAdapter.setEvaPersonList(mAllEvaPersonList);
						mEvaPersonAdapter.notifyDataSetChanged();
					}
					break;
				case R.id.app_evaluate_choose_person_rb_not_eva:
					if (mNotEvaPersonList.size() == 0) {
						mPullToRefreshGridView.setMode(Mode.DISABLED);
						requestCurrentPageView(true,TYPE_WAIT_EVA_PERSON);
					} else {
						mPullToRefreshGridView.setMode(Mode.BOTH);
						mEvaPersonAdapter.setEvaPersonList(mNotEvaPersonList);
						mEvaPersonAdapter.notifyDataSetChanged();
					}
					break;
				case R.id.app_evaluate_choose_person_rb_evaed:
					if (mEvaedPersonList.size() == 0) {
						mPullToRefreshGridView.setMode(Mode.DISABLED);
						requestCurrentPageView(true,TYPE_ALREADY_EVAED_PERSON);
					} else {
						mPullToRefreshGridView.setMode(Mode.BOTH);
						mEvaPersonAdapter.setEvaPersonList(mEvaedPersonList);
						mEvaPersonAdapter.notifyDataSetChanged();
					}
					break;
				default:
					break;
				}
			}
		});
		mAllRadioBt = (RadioButton) mFilterEvaPersonRG.findViewById(R.id.app_evaluate_choose_person_rb_all);
		mAllUnevaRadioBt = (RadioButton) mFilterEvaPersonRG.findViewById(R.id.app_evaluate_choose_person_rb_not_eva);
		mAllEvaedRadioBt = (RadioButton) mFilterEvaPersonRG.findViewById(R.id.app_evaluate_choose_person_rb_evaed);
		mAllRadioBt.setChecked(true);
		if (mPlanObject != null) {
			refreshRadioGroupView(true);
		}
		mSearchEvaluateSearchTeacherView.mBackLinLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismissSearchEvaPersonView();
			}
		});
		mSearchEvaluateSearchTeacherView.setSearchItemClickListener(new OnSearchItemClickListener() {
			
			@Override
			public void onItemClick(EvaPersonBean onClickPerson) {
				startAnswerQuesActivity(onClickPerson);
			}
		});
	}
	
	/**
	 * 根据页码请求服务器数据
	 * @param pageIndex
	 */
	private void loadServerJsonData(int pageIndex,int filterType) {
		if (mApplication.CONFIG_YNEDUT_V8_URL == null) {
			return;
		}
		if (mApplication.CONFIG_YNEDUT_V8_URL.isEmpty()) {
			return;
		}
//		//.......
//		mApplication.CONFIG_YNEDUT_V8_URL = "http://10.6.23.137/ynedut";
//		mApplication.mAppTokenStr = "f5ac410c-6e40-4cad-a7b8-dffc90583abe";
		//.......
		String url = mApplication.CONFIG_YNEDUT_V8_URL+EvaParam.APP_EVALUATE_LOAD_PERSONS_API;
		url = MessageFormat.format(url, mLastUserSP.getUserAccount(),mPlanId,pageIndex,mPageSize,filterType,mApplication.mAppTokenStr);
		L.e("eva person == "+url);
//		//.......
//		url = "http://www.baidu.com";
		loadServiceData(url,filterType);
	}
	
	@Override
	public void titleRightClick(View v) {
		if (!mSearchEvaluateSearchTeacherView.isShown()) {
			mSearchEvaluateSearchTeacherView.setVisibility(View.VISIBLE);
			mSearchEvaluateSearchTeacherView.setAnimation(visbleAnimation);
			mSearchEvaluateSearchTeacherView.startAnimation(visbleAnimation);
			mSearchEvaluateSearchTeacherView.setUrlParam(mLastUserSP.getUserAccount(),mPlanId);
		} 
	}
	
	@Override
	public void backView() {
		if (dismissSearchEvaPersonView()) {
			return;
		}
		finish();
	}
	
	/**
	 * 更新筛选框
	 * @param isInit
	 */
	private void refreshRadioGroupView(boolean isInit) {
		if (isInit) {
			int unEvaPersonCount = mPlanObject.getEvaluateCount() - mPlanObject.getEvaluatedCount();
			mAllRadioBt.setText(getString(R.string.evaluateCheckAllCount, mPlanObject.getEvaluateCount()+""));
			mAllUnevaRadioBt.setText(getString(R.string.evaluateUncheckCount, unEvaPersonCount+""));
			mAllEvaedRadioBt.setText(getString(R.string.evaluateCheckedCount, mPlanObject.getEvaluatedCount()+""));
		} else {
			mAllRadioBt.setText(getString(R.string.evaluateCheckAllCount, mTotalEvaPersonCount));
			mAllEvaedRadioBt.setText(getString(R.string.evaluateCheckedCount, mEvaedPersonCount));
			int totalCount = Integer.parseInt(mTotalEvaPersonCount);
			int evaedCount = Integer.parseInt(mEvaedPersonCount);
			mAllUnevaRadioBt.setText(getString(R.string.evaluateUncheckCount, (totalCount-evaedCount)+""));
		}
	}
	
	/**
	 * 隐藏搜索界面
	 * @return
	 */
	public boolean dismissSearchEvaPersonView() {
		if (mSearchEvaluateSearchTeacherView.isShown()) {
			mSearchEvaluateSearchTeacherView.setAnimation(inVisbleAnimation);
			mSearchEvaluateSearchTeacherView.startAnimation(inVisbleAnimation);
			mSearchEvaluateSearchTeacherView.setVisibility(View.GONE);
			mSearchEvaluateSearchTeacherView.clearSearchEditText();
			mSearchEvaluateSearchTeacherView.clearAdapter();
			return true;
		} 
		return false;
	}


	@Override
	public void onPreTask() {
		switch (mFilterEvaPersonRG.getCheckedRadioButtonId()) {
		case R.id.app_evaluate_choose_person_rb_all:
			if (mAllPageIndex == 0 && mAllEvaPersonList.size() == 0) {
				mEvaPersonAdapter.setEvaPersonList(mAllEvaPersonList);
				mEvaPersonAdapter.notifyDataSetChanged();
				showLoadingView();
			}
			break;
		case R.id.app_evaluate_choose_person_rb_not_eva:
			if (mWaitPageIndex == 0 && mNotEvaPersonList.size() == 0) {
				mEvaPersonAdapter.setEvaPersonList(mNotEvaPersonList);
				mEvaPersonAdapter.notifyDataSetChanged();
				showLoadingView();
			}
			break;
		case R.id.app_evaluate_choose_person_rb_evaed:
			if (mEvaedPageIndex == 0 && mEvaedPersonList.size() == 0) {
				mEvaPersonAdapter.setEvaPersonList(mEvaedPersonList);
				mEvaPersonAdapter.notifyDataSetChanged();
				showLoadingView();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public Object onDoingTask(String resultJson) {
		return null;
	}
	
	@Override
	protected void onDoingTask(String resultJson, int type) {
		try {
			org.json.JSONObject test = new org.json.JSONObject(resultJson);
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtil.toastAlerMessageBottom(EvaluateChoosePersonActivity.this, "JSON格式错误", 800);
			return;
		}
		EvaTeachersBean tempEvaTeachersBean = JSONObject.parseObject(resultJson, EvaTeachersBean.class);
		if (tempEvaTeachersBean == null) {
			return;
		}
		List<EvaPersonBean> resultEvaPersonBean = tempEvaTeachersBean.wjEvaluateVO;
		mTotalEvaPersonCount = tempEvaTeachersBean.getTotalEvaluate();
		mEvaedPersonCount = tempEvaTeachersBean.getEvaedcount();
		//刷新筛选框
		refreshRadioGroupView(false);
		if (resultEvaPersonBean == null) {
			return;
		} else {
			switch (type) {
			case TYPE_ALL_EVA_PERSON:
				//第一页
				if (resultEvaPersonBean.size() > 0 && mAllEvaPersonList.size() == 0) {
					mAllEvaPersonList.addAll(resultEvaPersonBean);
				} else if (resultEvaPersonBean.size() > 0 && mAllEvaPersonList.size() > 0 &&
						!mAllEvaPersonList.containsAll(resultEvaPersonBean)) {
					//当不为第一页时，该数据大小大于0，如果这次请求的内容也大于0，且界面数据不包含此次请求的数据，
					// 说明页码应该+1
					mAllEvaPersonList.addAll(resultEvaPersonBean);
					mAllPageIndex = mAllPageIndex + 1;
				} else if (resultEvaPersonBean.size() == 0 && mAllEvaPersonList.size() > 0) {
					mHandler.sendEmptyMessage(REFRESH_NO_MORE_DATA);
				}
				break;
			case TYPE_WAIT_EVA_PERSON:
				//第一页
				if (resultEvaPersonBean.size() > 0 && mNotEvaPersonList.size() == 0) {
					mNotEvaPersonList.addAll(resultEvaPersonBean);
				} else if (resultEvaPersonBean.size() > 0 && mNotEvaPersonList.size() > 0 &&
						!mNotEvaPersonList.containsAll(resultEvaPersonBean)) {
					//当不为第一页时，该数据大小大于0，如果这次请求的内容也大于0，且界面数据不包含此次请求的数据，
					// 说明页码应该+1
					mNotEvaPersonList.addAll(resultEvaPersonBean);
					mWaitPageIndex = mWaitPageIndex + 1;
				} else if (resultEvaPersonBean.size() == 0 && mNotEvaPersonList.size() > 0) {
					//没有更多了
					mHandler.sendEmptyMessage(REFRESH_NO_MORE_DATA);
				}
				break;
			case TYPE_ALREADY_EVAED_PERSON:
				//第一页
				if (resultEvaPersonBean.size() > 0 && mEvaedPersonList.size() == 0) {
					mEvaedPersonList.addAll(resultEvaPersonBean);
				} else if (resultEvaPersonBean.size() > 0 && mEvaedPersonList.size() > 0 &&
						!mEvaedPersonList.containsAll(resultEvaPersonBean)) {
					//当不为第一页时，该数据大小大于0，如果这次请求的内容也大于0，且界面数据不包含此次请求的数据，
					// 说明页码应该+1
					mEvaedPersonList.addAll(resultEvaPersonBean);
					mEvaedPageIndex = mEvaedPageIndex + 1;
				} else if (resultEvaPersonBean.size() == 0 && mEvaedPersonList.size() > 0) {
					mHandler.sendEmptyMessage(REFRESH_NO_MORE_DATA);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onDoneTask(Object result) {
		refreshCurrentPageView();
	}

	/**
	 * 刷新网络请求后的界面
	 */
	private void refreshCurrentPageView() {
//		dismissContentStatusView();
		if (mPullToRefreshGridView.isRefreshing()) {
			mPullToRefreshGridView.onRefreshComplete();
		}
		if (mRefreshGridView.getAdapter() == null) {
			mRefreshGridView.setAdapter(mEvaPersonAdapter);
		}
		switch (mFilterEvaPersonRG.getCheckedRadioButtonId()) {
		case R.id.app_evaluate_choose_person_rb_all:
			showContentView(mAllEvaPersonList,mAllPageIndex,0);
			break;
		case R.id.app_evaluate_choose_person_rb_not_eva:
			showContentView(mNotEvaPersonList,mWaitPageIndex,1);
			break;
		case R.id.app_evaluate_choose_person_rb_evaed:
			showContentView(mEvaedPersonList,mEvaedPageIndex,2);
			break;
		default:
			break;
		}
		mEvaPersonAdapter.notifyDataSetChanged();
	}

	/**
	 * 显示数据内容
	 * @param mAllEvaPersonList2
	 * @param mAllPageIndex2
	 */
	private void showContentView(List<EvaPersonBean> mEvaPersonList,
			int mPageIndex,int mTabIndex) {
		if (mEvaPersonList.size() > 0) {
			mPullToRefreshGridView.setMode(Mode.BOTH);
			mEvaPersonAdapter.setEvaPersonList(mEvaPersonList);
		} else {
			if (mPageIndex == 0 && mEvaPersonList.size() == 0) {
				switch (mTabIndex) {
				case 0:
					showEmptyView("没有可选择的评教对象!");
					break;
				case 1:
					showEmptyView("没有待评的评教对象!");
					break;
				case 2:
					showEmptyView("没有已完成的评教对象!");
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void onFailedTask(int urlQuesStatusCode) {
		if (mPullToRefreshGridView.isRefreshing()) {
			mPullToRefreshGridView.onRefreshComplete();
		}
		switch (mFilterEvaPersonRG.getCheckedRadioButtonId()) {
		case R.id.app_evaluate_choose_person_rb_all:
			showFailedView(mAllPageIndex,urlQuesStatusCode);
			break;
		case R.id.app_evaluate_choose_person_rb_not_eva:
			showFailedView(mWaitPageIndex,urlQuesStatusCode);
			break;
		case R.id.app_evaluate_choose_person_rb_evaed:
			showFailedView(mEvaedPageIndex,urlQuesStatusCode);
			break;
		default:
			break;
		}

	}
	
	/**
	 * 显示加载失败的视图
	 */
	private void showFailedView(int mPageIndex, int urlQuesStatusCode) {
		switch (urlQuesStatusCode) {
		case 2:
			if (mPageIndex == 0 && mEvaPersonAdapter.getCount() == 0) {
				mPullToRefreshGridView.setMode(Mode.DISABLED);
				showContentFailedView();
			} else {
				ToastUtil.toastAlerMessageBottom(
						EvaluateChoosePersonActivity.this, "加载失败,请重试!", 800);
			}
			break;
		case 3:
			if (mPageIndex == 0 && mEvaPersonAdapter.getCount() == 0) {
				mPullToRefreshGridView.setMode(Mode.DISABLED);
				showContentFailedView("网络异常，请检查重试!");
			} else {
				ToastUtil.toastAlerMessageBottom(
						EvaluateChoosePersonActivity.this, "网络异常，请检查重试!", 800);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 启动答题界面
	 * @param onClickPerson
	 */
	protected void startAnswerQuesActivity(EvaPersonBean onClickPerson) {
		if (onClickPerson.getEvaluateStatus() == 1) {
			ToastUtil.toastAlerMessageBottom(EvaluateChoosePersonActivity.this, "已评过了，请选其他被评人！", 1000);
			return;
		}
		Intent intent = new Intent(EvaluateChoosePersonActivity.this, EvaluateAnswerActivity.class);
		intent.putExtra("AppUserType", mUserType);
		intent.putExtra("planId", mPlanId != null ? mPlanId : "");
		intent.putExtra("evaUser", onClickPerson);
//		startActivity(intent);
		startActivityForResult(intent, 1);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case 1://刷新已评的人的状态
			if (data == null) {
				return;
			}
			EvaPersonBean mEvaedPersonObject = (EvaPersonBean) data.getSerializableExtra("evaedUser");
			if (mEvaedPersonObject == null) {
				return;
			}
			if (resultCode == RESULT_OK) {
				//发送更新评教计划已评人数状态的广播
				Intent refreshIntent = new Intent(CommonReceiver.BROADCAST_ACTION_REFRESH_EVA_STATUS);
				if (mPlanId == null) {
                    return;
                }
				if (mPlanId.isEmpty()) {
                    return;
                }
				refreshIntent.putExtra("planId", mPlanId);
				sendBroadcast(refreshIntent);
				
				//刷新状态栏
				int evaedCount = Integer.parseInt(mEvaedPersonCount);
				mEvaedPersonCount = (evaedCount+1)+"";
				refreshRadioGroupView(false);
				
				//如果搜索结果界面可见，刷新搜索结果界面
				if (mSearchEvaluateSearchTeacherView.isShown()) {
					List<EvaPersonBean> mSearchPersonList = mSearchEvaluateSearchTeacherView.mEvaPersonResultAdapter.getEvaPersonList();
					for (EvaPersonBean evaPersonBean : mSearchPersonList) {
						if (mEvaedPersonObject.getId().equals(evaPersonBean.getId())) {
							evaPersonBean.setEvaluateStatus(1);
							break;
						}
					}
					mSearchEvaluateSearchTeacherView.mEvaPersonResultAdapter.notifyDataSetChanged();
				}
				//刷新全部列表
				for (EvaPersonBean evaPersonBean : mAllEvaPersonList) {
					if (mEvaedPersonObject.getId().equals(evaPersonBean.getId())) {
						evaPersonBean.setEvaluateStatus(1);
						break;
					}
				}
				//刷新待评列表
				for (EvaPersonBean evaPersonBean : mNotEvaPersonList) {
					if (mEvaedPersonObject.getId().equals(evaPersonBean.getId())) {
						//删除待评里面已经被评的人
						mNotEvaPersonList.remove(evaPersonBean);
						break;
					}
				}
				mEvaedPersonList.add(mEvaedPersonObject);
				mEvaPersonAdapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	public void onReloadClick(View v) {
		refreshFirstPage();
	}
	
	/**
	 * 刷新加载第一页
	 */
	public void refreshFirstPage() {
		switch (mFilterEvaPersonRG.getCheckedRadioButtonId()) {
		case R.id.app_evaluate_choose_person_rb_all:
			requestCurrentPageView(true,TYPE_ALL_EVA_PERSON);
			break;
		case R.id.app_evaluate_choose_person_rb_not_eva:
			requestCurrentPageView(true,TYPE_WAIT_EVA_PERSON);
			break;
		case R.id.app_evaluate_choose_person_rb_evaed:
			requestCurrentPageView(true,TYPE_ALREADY_EVAED_PERSON);
			break;
		default:
			break;
		}
	}
}
