//***************************************************************
//*    2015-10-13  上午11:17:47
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBaseActivity;
import com.yineng.ynmessager.adapter.EvaPersonAdapter;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.evaluate.EvaParam;
import com.yineng.ynmessager.bean.app.evaluate.EvaPersonBean;
import com.yineng.ynmessager.bean.app.evaluate.EvaTeachersBean;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Request;

/**
 * @author 胡毅
 * 
 */
public class EvaluateSearchTeacherView extends RelativeLayout implements
		OnFocusChangeListener, TextWatcher {

    public final int PULL_DOWN_TO_REFRESH = 0;
    public final int PULL_UP_TO_REFRESH = 1;
    public final int REFRESH_NO_MORE_DATA = 2;
    public LinearLayout mBackLinLayout;
    public EvaPersonAdapter mEvaPersonResultAdapter;
    protected List<EvaPersonBean> mEvaPersonResultList = new ArrayList<EvaPersonBean>();
    /**
     * 搜索出来的总人数
     */
    protected String mTotalEvaPersonCount;
    private EditText mSearchPersonET;
	private Context mContext;
	/**
	 * 删除按钮的引用
	 */
	private Drawable mClearDrawable;
	private PullToRefreshGridView mPullToRefreshGridView;
	private GridView mRefreshGridView;
	private TextView mAdapterCountTV;
    /**
     * 网络请求状态码:1 成功； 2 失败；  3 网络超时
     */
	private int urlQuesStatusCode = 0;
	/**
	 * 页码
	 */
	private int mPageIndex;
	/**
	 * 用户id
	 */
	private String mUserId;
	/**
	 * 计划id
	 */
	private String mPlanId;
	/**
	 * 每页数量
	 */
	private int mPageSize = 40;
    private ProgressBar mLoadingProBar;


	@SuppressLint("HandlerLeak")
	public Handler findHandler = new Handler() {
		@Override
        public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PULL_DOWN_TO_REFRESH:
				mPageIndex = 0;
				mEvaPersonResultList.clear();
				loadServiceData(mPageIndex, mSearchPersonET.getText().toString());
				break;
			case PULL_UP_TO_REFRESH:
				if (mEvaPersonResultList.size() > 0) {
					loadServiceData(mPageIndex + 1, mSearchPersonET.getText().toString());
				} else {
					mPageIndex = 0;
					mEvaPersonResultList.clear();
					loadServiceData(mPageIndex, mSearchPersonET.getText().toString());
				}
				break;
			case REFRESH_NO_MORE_DATA:
				// 没有更多了
				ToastUtil.toastAlerMessageBottom(mContext,
						"没有更多了", 700);
				break;
			default:
				break;
			}
        }
    };
    private OnSearchItemClickListener onSearchItemClickListener;

	public EvaluateSearchTeacherView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		LayoutInflater.from(context).inflate(
				R.layout.view_app_evaluate_search_person, this);
		initView();
	}

	public EvaluateSearchTeacherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater.from(context).inflate(
				R.layout.view_app_evaluate_search_person, this);
		initView();
	}

	public EvaluateSearchTeacherView(Context context) {
		super(context);
		mContext = context;
		LayoutInflater.from(context).inflate(
				R.layout.view_app_evaluate_search_person, this);
		initView();
	}

    /**
     * @param userAccount
     * @param mPlanId
     */
    public void setUrlParam(String userAccount, String mPlanId) {
        mPageIndex = 0;
        mUserId = userAccount;
        this.mPlanId = mPlanId;
    }

    /**
     *
     */
	private void initView() {
		mBackLinLayout = (LinearLayout) findViewById(R.id.app_eva_choose_search_titleview_right);
		mSearchPersonET = (EditText) findViewById(R.id.app_eva_choose_search_et);
		mAdapterCountTV = (TextView) findViewById(R.id.app_eva_search_person_result);
		mLoadingProBar = (ProgressBar) findViewById(R.id.app_eva_search_person_content_loading_data);
		// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
		mClearDrawable = mSearchPersonET.getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			mClearDrawable = ResourcesCompat.getDrawable(mContext.getResources(),R.mipmap.emotionstore_progresscancelbtn,mContext.getTheme());
		}
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());
		setClearIconVisible(false);

		mPullToRefreshGridView = (PullToRefreshGridView) findViewById(R.id.app_eva_search_person_gridview);
		mPullToRefreshGridView.setMode(Mode.PULL_UP_TO_REFRESH);
		mRefreshGridView = mPullToRefreshGridView
				.getRefreshableView();
		mRefreshGridView.setPadding(25, 25, 25, 0);
		mRefreshGridView.setHorizontalSpacing(25);
		mRefreshGridView.setVerticalSpacing(25);
		mRefreshGridView.setNumColumns(4);
		mEvaPersonResultAdapter = new EvaPersonAdapter(mContext,mEvaPersonResultList);
//		mRefreshGridView.setAdapter(mEvaPersonResultAdapter);
		initViewListener();
	}

    /**
     *
	 */
	private void initViewListener() {
		mSearchPersonET.setOnFocusChangeListener(this);
		mSearchPersonET.addTextChangedListener(this);
		mSearchPersonET.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent event) {
				if (mSearchPersonET.getCompoundDrawables()[2] != null) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						boolean touchable = event.getX() > (mSearchPersonET
								.getWidth() - mSearchPersonET.getPaddingRight() - mClearDrawable
									.getIntrinsicWidth())
								&& (event.getX() < ((mSearchPersonET.getWidth() - mSearchPersonET
										.getPaddingRight())));
						if (touchable) {
							mSearchPersonET.setText("");
						}
					}
				}
				return false;
			}
		});

		mPullToRefreshGridView
				.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<GridView>() {

					// 下拉刷新
					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						findHandler.sendEmptyMessageDelayed(PULL_DOWN_TO_REFRESH, 800);
					}

					// 上拉加载更多
					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						findHandler.sendEmptyMessageDelayed(PULL_UP_TO_REFRESH, 800);
					}
				});
		mRefreshGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EvaPersonBean onClickPerson = mEvaPersonResultList.get(position);
				if (onSearchItemClickListener != null) {
					onSearchItemClickListener.onItemClick(onClickPerson);
				}
//				if (onClickPerson.getEvaluateStatus() == 1) {
//					return;
//				}
//				mContext.startActivity(new Intent(mContext,
//						EvaluateAnswerActivity.class));
			}
		});
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? mClearDrawable : null;
		mSearchPersonET.setCompoundDrawables(
				mSearchPersonET.getCompoundDrawables()[0],
				mSearchPersonET.getCompoundDrawables()[1], right,
				mSearchPersonET.getCompoundDrawables()[3]);
	}

	/**
	 * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(mSearchPersonET.getText().length() > 0);
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
		clearAdapter();
		if (s.length() > 0) {
			loadServiceData(mPageIndex, s.toString());
		} else {
			mAdapterCountTV.setText("0个搜索结果");
//			mAdapterCountTV.setVisibility(View.GONE);
        }
	}

    public void clearAdapter() {
		mPageIndex = 0;
		if (mEvaPersonResultAdapter != null) {
			mEvaPersonResultList.clear();
			mEvaPersonResultAdapter.setEvaPersonList(mEvaPersonResultList);
			mEvaPersonResultAdapter.notifyDataSetChanged();
        }
	}

	/**
	 * 搜索
	 */
	public void loadServiceData(int pageIndex,String searchName) {
		String url = configServerUrl(pageIndex,searchName);
		OKHttpCustomUtils.get(url, new StringCallback() {
			@Override
			public void onError(Call call, Exception e, int id) {
				L.w("onFailure", "error.getMessage() == " + e.getMessage());
				if (e.getMessage().contains("time out")) {
					urlQuesStatusCode = 3;
				} else {
					urlQuesStatusCode = 2;
				}
				if (e.getMessage().contains("500")){
					ToastUtil.toastAlerMessageBottom(mContext, "内部服务器错误", 700);
				}else if (e.getMessage().contains("401")&& e.getMessage().contains("invalid_token")){
					AppBaseActivity.initAppTokenData(true);
				}
			}

			@Override
			public void onResponse(String response, int id) {
				if ("error".equals(response)) {//请求成功，但是服务器返回error字符串，说明服务器有问题
					urlQuesStatusCode = 2;
					return;
				}
				try {
					org.json.JSONObject test = new org.json.JSONObject(response);
				} catch (JSONException e) {
					e.printStackTrace();
					ToastUtil.toastAlerMessageBottom(mContext, "JSON格式错误", 800);
					return;
				}
				urlQuesStatusCode = 1;
				EvaTeachersBean tempEvaTeachersBean =
						com.alibaba.fastjson.JSONObject.parseObject(response, EvaTeachersBean.class);
				if (tempEvaTeachersBean == null) {
					return;
				}
				List<EvaPersonBean> resultEvaPersonBean = tempEvaTeachersBean.wjEvaluateVO;
				mTotalEvaPersonCount = tempEvaTeachersBean.getTotalEvaluate();

				//			List<EvaPersonBean> resultEvaPersonBean = JSONArray.parseArray(resultJson, EvaPersonBean.class);
				if (resultEvaPersonBean == null) {
					return;
				}
				//当不为第一页时，该数据大小大于0，如果这次请求的内容也大于0，说明页码应该+1
				if (resultEvaPersonBean.size() > 0 && mEvaPersonResultList.size() > 0) {
					mPageIndex = mPageIndex + 1;
				} else if (resultEvaPersonBean.size() == 0 && mEvaPersonResultList.size() > 0) {
					findHandler.sendEmptyMessage(REFRESH_NO_MORE_DATA);
					return;
				}

				mEvaPersonResultList.addAll(resultEvaPersonBean);
			}

			@Override
			public void onBefore(Request request, int id) {
				urlQuesStatusCode = 0;
				if (mPageIndex == 0) {
					showLoadingProgress(true);
				}
			}

			@Override
			public void onAfter(int id) {
				showLoadingProgress(false);
				switch (urlQuesStatusCode) {
					case 1:
						onSuccessDoneTask();
						break;
					case 2:
					case 3:
						onFailedTask(urlQuesStatusCode);
						break;
					default:
						break;
				}
			}
		});
	}

	/**
	 * 配置URL
     * @param searchName
     * @param pageIndex
     * @return
     *
	 */
	private String configServerUrl(int pageIndex, String searchName) {
		if (AppController.getInstance().CONFIG_YNEDUT_V8_URL == null) {
			return null;
		}
		if (AppController.getInstance().CONFIG_YNEDUT_V8_URL.isEmpty()) {
			return null;
		}
		//.......
//		AppController.getInstance().CONFIG_YNEDUT_V8_URL = "http://10.6.23.137/ynedut";
//		AppController.getInstance().mAppTokenStr = "95decda2-9473-4178-b7a6-6161729cec20";
		//.......

		String url = AppController.getInstance().CONFIG_YNEDUT_V8_URL+EvaParam.APP_EVALUATE_LOAD_PERSONS_API;
		url = MessageFormat.format(url, mUserId,mPlanId,pageIndex,mPageSize,1,AppController.getInstance().mAppTokenStr);
		url = url+"&name="+searchName;
		L.e("search person == "+url);
//		//.......
//		url = "http://www.baidu.com";
		return url;
	}

    /**
     *
	 */
	protected void onSuccessDoneTask() {
		if (mPullToRefreshGridView.isRefreshing()) {
			mPullToRefreshGridView.onRefreshComplete();
		}
		if (mRefreshGridView.getAdapter() == null) {
			mRefreshGridView.setAdapter(mEvaPersonResultAdapter);
		}
		if (mEvaPersonResultList.size() > 0) {
			mEvaPersonResultAdapter.setEvaPersonList(mEvaPersonResultList);
		}
		mEvaPersonResultAdapter.notifyDataSetChanged();
		mAdapterCountTV.setText(mTotalEvaPersonCount+"个搜索结果");
//		mAdapterCountTV.setVisibility(View.VISIBLE);
	}

	protected void onFailedTask(int urlQuesStatusCode) {
		if (mPullToRefreshGridView.isRefreshing()) {
			mPullToRefreshGridView.onRefreshComplete();
		}
		switch (urlQuesStatusCode) {
		case 2:
			ToastUtil.toastAlerMessageBottom(mContext, "搜索失败", 800);
			break;
		case 3:
			ToastUtil.toastAlerMessageBottom(mContext, "网络错误，请检查重试", 800);
			break;
		default:
			break;
        }
	}

    /**
	 * @param visble
	 */
	protected void showLoadingProgress(boolean visble) {
		if (visble) {
			mLoadingProBar.setVisibility(VISIBLE);
		} else {
			mLoadingProBar.setVisibility(GONE);
		}
	}
	
	public void setSearchItemClickListener(OnSearchItemClickListener onSearchItemClickListener) {
		this.onSearchItemClickListener = onSearchItemClickListener;
	}

	/**
	 * 清除编辑框的焦点和文字
	 */
	public void clearSearchEditText() {
		mSearchPersonET.clearFocus();
		mSearchPersonET.setText("");
    }

    public interface OnSearchItemClickListener {
        void onItemClick(EvaPersonBean onClickPerson);
    }
}
