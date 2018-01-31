//***************************************************************
//*    2015-10-9  下午4:18:37
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.activity.app.evaluate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBaseActivity;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.bean.app.evaluate.EvaPlanBean;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.onEvaStatusRefreshListener;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 胡毅
 * 评教计划列表
 */
public class EvaluatePlanActivity extends AppBaseActivity {

	private PullToRefreshListView mPullToRefreshListView;
	private List<EvaPlanBean> mEvaPlanList = new ArrayList<EvaPlanBean>();
	private EvaPlanAdapter mEvaPlanAdapter;
	private ListView mRrefreshListView;
	/**
	 * 页码
	 */
	private int mPageIndex = 0;
	
	/**
	 * 每页数据总数
	 */
	private int mPageSize = 10;
	private String mUserType;
	
	private int ONE_SECOND = 1000;
	private Handler mHandler = new Handler() {
		
		@Override
        public void handleMessage(Message msg) {
			switch (msg.what) {
			case PULL_DOWN_TO_REFRESH:
				mPageIndex = 0;
				mEvaPlanList.clear();
				loadServerJsonData(mPageIndex);
				break;
			case PULL_UP_TO_REFRESH:
				if (mEvaPlanList.size() > 0) {
					loadServerJsonData(mPageIndex+1);
				} else {
					loadServerJsonData(0);
				}
				break;
			case REFRESH_NO_MORE_DATA:
				//没有更多了
				ToastUtil.toastAlerMessageBottom(EvaluatePlanActivity.this, "没有更多了", 700);
				break;
			case REFRESH_COUNT_TIME:
				if (mEvaPlanList.size() > 0) {
					for (EvaPlanBean evaPlan : mEvaPlanList) {
						if (evaPlan.getRestTime() > ONE_SECOND) {
							long restTime = evaPlan.getRestTime()-ONE_SECOND;
							evaPlan.setRestTime(restTime);
						} else {
							evaPlan.setRestTime(0);
						}
					}
					mEvaPlanAdapter.notifyDataSetChanged();
					mHandler.sendEmptyMessageDelayed(REFRESH_COUNT_TIME, ONE_SECOND);
				} else {
					mHandler.removeMessages(REFRESH_COUNT_TIME);
				}
				
				break;
			default:
				break;
			}
		}
	};
	private CommonReceiver mRefreshReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initRefreshListViewParams();
		setAppContentView(mPullToRefreshListView);
		initView();
		initViewListener();
	}
	
	/**
	 * 初始化上下拉刷新列表的属性
	 */
	private void initRefreshListViewParams() {
		mPullToRefreshListView = new PullToRefreshListView(this, Mode.DISABLED);
		LayoutParams listViewPrams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
//		listViewPrams.leftMargin = listViewPrams.rightMargin = 15;
		int pading = (int) getResources().getDimension(R.dimen.common_listItem_padding);
		mPullToRefreshListView.setPadding(pading, 0, pading, 0);
		mPullToRefreshListView.setLayoutParams(listViewPrams);
		mPullToRefreshListView.setBackgroundResource(R.color.common_content_bg);
		mRrefreshListView = mPullToRefreshListView.getRefreshableView();
		mRrefreshListView.setDivider(null);
		mRrefreshListView.setDividerHeight(pading);
		mRrefreshListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
	}
	
	public void initView() {
		setTitleName("评教");
		mEvaPlanAdapter = new EvaPlanAdapter(EvaluatePlanActivity.this);
		mUserType = getIntent().getStringExtra("AppUserType");
	}

	public void initViewListener() {
		mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				mHandler.sendEmptyMessageDelayed(PULL_DOWN_TO_REFRESH, 800);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				mHandler.sendEmptyMessageDelayed(PULL_UP_TO_REFRESH, 800);
			}
		});
		mRrefreshListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				L.e("pos == "+position+" pos == "+(position-1));
				position = position-1;
				Intent intent = new Intent(EvaluatePlanActivity.this, EvaluateChoosePersonActivity.class);
				intent.putExtra("AppUserType", mUserType);
				intent.putExtra("planObject", mEvaPlanList.get(position));
				startActivityForResult(intent, 0);
			}
		});
		mRrefreshListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				position = position-1;
				if (mEvaPlanList.size() > 0 && position < mEvaPlanList.size()) {
					EvaPlanBean showTitlePlan = mEvaPlanList.get(position);
					ToastUtil.toastAlerMessageBottom(EvaluatePlanActivity.this,showTitlePlan.getPlanName(), Toast.LENGTH_SHORT);
				}
				return true;
			}
		});
		mRefreshReceiver = new CommonReceiver();
		mRefreshReceiver.setOnEvaStatusRefreshListener(new onEvaStatusRefreshListener() {
			
			@Override
			public void onRefreshEvaStatus(String planId) {
				for (EvaPlanBean tempBean : mEvaPlanList) {
					if (tempBean.getId().equals(planId)) {
						tempBean.setEvaluatedCount(tempBean.getEvaluatedCount()+1);
						break;
					}
				}
				mEvaPlanAdapter.notifyDataSetChanged();
			}
		});
		IntentFilter filter = new IntentFilter(CommonReceiver.BROADCAST_ACTION_REFRESH_EVA_STATUS);
		registerReceiver(mRefreshReceiver, filter);
		loadServerJsonData(0);
	}

	/**
	 * 根据页码请求服务器数据
	 * @param pageIndex
	 */
	private void loadServerJsonData(int pageIndex) {
		if (mApplication.CONFIG_YNEDUT_V8_URL == null) {
			return;
		}
		if (TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_URL)) {
			return;
		}
		String url = mApplication.CONFIG_YNEDUT_V8_URL+EvaParam.APP_EVALUATE_LOAD_PLANS_API;
		url = MessageFormat.format(url, mLastUserSP.getUserAccount(),pageIndex,mPageSize,mApplication.mAppTokenStr);
		L.e("eva plan == "+url);
		loadServiceData(url);
		
	}

	@Override
	public void backView() {
		finish();
	}

	@Override
	public void onPreTask() {
		L.e(" pre == "+mRrefreshListView.getCount()+" adapter count == "+mEvaPlanAdapter.getCount());
		if ((mPageIndex == 0 && mRrefreshListView.getCount() == 0)
				||(mPageIndex == 0 && mRrefreshListView.getCount() == 2)) {
//			dismissEmptyView();
			showLoadingView();
		}
	}

	@Override
	public Object onDoingTask(String resultJson) {
		try {
			org.json.JSONArray test = new org.json.JSONArray(resultJson);
		} catch (JSONException e) {
			e.printStackTrace();
			ToastUtil.toastAlerMessageBottom(EvaluatePlanActivity.this, "JSON格式错误", 800);
			return null;
		}
		List<EvaPlanBean> resultEvaPlanList = JSONArray.parseArray(resultJson, EvaPlanBean.class);
		if (resultEvaPlanList == null) {
			return null;
		} else {
			//当不为第一页时，该数据大小大于0，如果这次请求的内容也大于0，说明页码应该+1
			if (resultEvaPlanList.size() > 0 && mEvaPlanList.size() > 0) {
				mPageIndex = mPageIndex + 1;
			} else if (resultEvaPlanList.size() == 0 && mEvaPlanList.size() > 0) {
				mHandler.sendEmptyMessage(REFRESH_NO_MORE_DATA);
				return null;
			} 
			mEvaPlanList.addAll(resultEvaPlanList);
		}
		return mEvaPlanList;
	}

	@Override
	public void onDoneTask(Object result) {
		if (mPullToRefreshListView.isRefreshing()) {
			mPullToRefreshListView.onRefreshComplete();
		}
		L.e("mEvaPlanList == "+mEvaPlanList.size() + mRrefreshListView.getAdapter());
		if (mRrefreshListView.getAdapter() == null) {
			mRrefreshListView.setAdapter(mEvaPlanAdapter);
		}
		if (mEvaPlanList.size() > 0) {
			mPullToRefreshListView.setMode(Mode.BOTH);
			mEvaPlanAdapter.notifyDataSetChanged();
			//启用倒计时刷新界面功能
			if (!mHandler.hasMessages(REFRESH_COUNT_TIME)) {
				mHandler.sendEmptyMessageDelayed(REFRESH_COUNT_TIME, ONE_SECOND);
			}
		} else {
			if (mPageIndex == 0 && mEvaPlanAdapter.getCount() == 0) {
				mPullToRefreshListView.setMode(Mode.DISABLED);
				showContentFailedView("当前没有需要处理的评教任务!"+"\n"+"单击刷新");
			}
		}
	}

	@Override
	public void onFailedTask(int urlQuesStatusCode) {
		if (mPullToRefreshListView.isRefreshing()) {
			mPullToRefreshListView.onRefreshComplete();
		}
		switch (urlQuesStatusCode) {
		case 2:
			if (mPageIndex == 0 && mEvaPlanAdapter.getCount() == 0) {
				mPullToRefreshListView.setMode(Mode.DISABLED);
				mEvaPlanAdapter.notifyDataSetChanged();
				showContentFailedView();
			} else {
				ToastUtil.toastAlerMessageBottom(EvaluatePlanActivity.this, "加载失败,请重试!", 800);
			}
			break;
		case 3:
			if (mPageIndex == 0 && mEvaPlanAdapter.getCount() == 0) {
				mPullToRefreshListView.setMode(Mode.DISABLED);
				mEvaPlanAdapter.notifyDataSetChanged();
				showContentFailedView("网络异常，请检查重试!");
			} else {
				ToastUtil.toastAlerMessageBottom(EvaluatePlanActivity.this, "网络异常，请检查重试!", 800);
			}
			break;
		default:
			break;
		}
	}
	
	public class EvaPlanAdapter extends BaseAdapter {

		private Context mContext;

		public EvaPlanAdapter(Context context) {
			mContext = context;
		}
		
		@Override
		public int getCount() {
			return mEvaPlanList.size() == 0 ? 0 : mEvaPlanList.size();
		}

		@Override
		public Object getItem(int position) {
			return mEvaPlanList == null ? null : mEvaPlanList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PlanViewHolder mPlanViewHolder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_evaluate_plans_listitem, parent,false);
				mPlanViewHolder = new PlanViewHolder();
				mPlanViewHolder.mPlanNameTV = (TextView) convertView.findViewById(R.id.app_evaluate_plan_name_tv);
				mPlanViewHolder.mPlanEvaCountTV = (TextView) convertView.findViewById(R.id.app_evaluate_plan_eva_count);
				mPlanViewHolder.mPlanEvaedCountTV = (TextView) convertView.findViewById(R.id.app_evaluate_plan_evaed_count);
				mPlanViewHolder.mPlanCountDownTV = (TextView) convertView.findViewById(R.id.app_evaluate_plan_count_down);
				mPlanViewHolder.mPlanDurationTV = (TextView) convertView.findViewById(R.id.app_evaluate_plan_duration);
				convertView.setTag(mPlanViewHolder);
			} else {
				mPlanViewHolder = (PlanViewHolder) convertView.getTag();
			}
			EvaPlanBean itemEvaPlanBean = mEvaPlanList.get(position);
			mPlanViewHolder.mPlanNameTV.setText(itemEvaPlanBean.getPlanName());
			mPlanViewHolder.mPlanEvaCountTV.setText(getString(R.string.evaluateCount, itemEvaPlanBean.getEvaluateCount()+""));
			mPlanViewHolder.mPlanEvaedCountTV.setText(getString(R.string.evaluatedCount, itemEvaPlanBean.getEvaluatedCount()+""));
			String beginTime = TimeUtil.getDateByDateStr(itemEvaPlanBean.getBeginTimeString(), TimeUtil.FORMAT_DATETIME_MM_DD);
			String endTime = TimeUtil.getDateByDateStr(itemEvaPlanBean.getEndTimeString(), TimeUtil.FORMAT_DATETIME_MM_DD);
			mPlanViewHolder.mPlanDurationTV.setText(beginTime+" ~ "+endTime);
//			mPlanViewHolder.mPlanDurationTV.setText(itemEvaPlanBean.getBeginTimeString()+" ~ "+itemEvaPlanBean.getEndTimeString());
			String restTimeStr = TimeUtil.getRestTimeCountDownDay(itemEvaPlanBean.getRestTime());
//			L.e("rest time == "+restTimeStr);
			mPlanViewHolder.mPlanCountDownTV.setText(restTimeStr+"后结束");
			return convertView;
		}
		
		public class PlanViewHolder {
			public TextView mPlanNameTV;
			public TextView mPlanEvaCountTV;
			public TextView mPlanEvaedCountTV;
			public TextView mPlanCountDownTV;
			public TextView mPlanDurationTV;
		}
	}
	
	@Override
	public void onReloadClick(View v) {
		loadServerJsonData(0);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiverSafe(mRefreshReceiver);
	}
	
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		//重新返回到该界面时，从服务器上获取adapter的数据总数，用以刷新状态
//		mPageSize = mEvaPlanAdapter.getCount();
//		if (mPageSize == 0) {
//			mPageSize = 10;
//		}
//		mEvaPlanList.clear();
//		loadServerJsonData(0);
//		mPageSize = 10;
//	}
}
