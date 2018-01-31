//***************************************************************
//*    2015-9-21  下午2:08:51
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.activity.app;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.HttpUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.callback.StringCallback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author 胡毅
 * APP应用模块基类Activity
 */
public abstract class AppBaseActivity extends BaseActivity {

    public final int PULL_DOWN_TO_REFRESH = 0;
    public final int PULL_UP_TO_REFRESH = 1;
    public final int REFRESH_NO_MORE_DATA = 2;
    public final int REFRESH_COUNT_TIME = 3;
    public ImageView mAppTitleBackIV;
	public TextView mAppTitleNameTV;
	public LinearLayout mAppTitleRightLL;
    public ImageView mAppTitleRightIV;
    public LastLoginUserSP mLastUserSP;
    private RelativeLayout mAppContentLayoutRL;
	private ProgressBar mAppContentLoadingPB;
	private TextView mAppContentEmptyTV;
	private RelativeLayout mAppRootLayoutRL;
	private LinearLayout mAppTitleBackLL;
	/**
	 * 网络请求状态码:1 成功； 2 失败；  3 网络超时 
	 */
	private int urlQuesStatusCode = 0;
	/**
	 * 加载失败的视图
	 */
	private TextView mAppContentFailedTV;
	protected JSONObjectCallBack jsonObjectCallBack = new JSONObjectCallBack();

	class JSONObjectCallBack extends Callback<String> {

		@Override
		public void onBefore(Request request, int id) {
			super.onBefore(request, id);
			urlQuesStatusCode = 0;
			onPreTask();
		}

		@Override
		public void onAfter(int id) {
			dismissContentStatusView();
			switch (urlQuesStatusCode) {
				case 1:
					onDoneTask(null);
					break;
				case 2:
				case 3:
					onFailedTask(urlQuesStatusCode);
					break;
				default:
					break;
			}
		}

		@Override
		public String parseNetworkResponse(Response response, int id) throws Exception {
			return response.body().string();
		}

		@Override
		public void onError(Call call, Exception e, int id) {
			if (e.getMessage() == null) {
				if (e.getMessage() != null) {
					if (e.getMessage().contains("No address associated with hostname")) {
						urlQuesStatusCode = 3;
					} else {
						urlQuesStatusCode = 2;
					}
				} else {
					urlQuesStatusCode = 2;
				}
				return;
			}
			if (e.getMessage().contains("500")){
				ToastUtil.toastAlerMessageBottom(AppBaseActivity.this, "内部服务器错误", 700);
			}else if (e.getMessage().contains("401")&& "invalid_token".equals(e.getMessage())){
				initAppTokenData(true);
			}
			String failStr = e.getMessage();
			if (failStr.contains("time out")) {
				urlQuesStatusCode = 3;
			} else {
				urlQuesStatusCode = 2;
			}

		}

		@Override
		public void onResponse(String response, int id) {
			if (response == null) {
				return;
			}
			if ("error".equals(response)) {//请求成功，但是服务器返回error字符串，说明服务器有问题
				urlQuesStatusCode = 2;
				return;
			}
			urlQuesStatusCode = 1;
			onDoingTask(response);
		}
	}
    /**
     * 初始化获取、解析相关数据
     *
     * @param forceRefreshToken 是否强制重新请求刷新Token来访问应用菜单接口
     */
    @SuppressLint("StaticFieldLeak")
	public static void initAppTokenData(boolean forceRefreshToken) {
        new AsyncTask<Boolean, Integer, String>() {

            @Override protected String doInBackground(Boolean... params) {
                String token;
                if (params[0]) {
                    token = V8TokenManager.forceRefresh();
                } else {
                    token = V8TokenManager.obtain();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String result) {
                AppController.getInstance().mAppTokenStr = result;
            }
        }.execute(forceRefreshToken);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appcenter_common);
		initBaseView();
		initBaseViewListener();
		mLastUserSP = LastLoginUserSP.getInstance(AppBaseActivity.this);
	}

    /**
	 * 添加正文视图
	 * @param layoutResID
	 */
	public View setAppContentView(int layoutResID) {
		mAppContentLayoutRL = (RelativeLayout) findViewById(R.id.app_content_view);
		return getLayoutInflater().inflate(layoutResID, mAppContentLayoutRL, true);
	}
	
	/**
	 * 添加正文视图
	 * @param
	 */
	public View setAppContentView(View contentView) {
		mAppContentLayoutRL = (RelativeLayout) findViewById(R.id.app_content_view);
		mAppContentLayoutRL.addView(contentView);
		return mAppContentLayoutRL;
	}

	/**
	 * 初始化基础界面
	 */
	private void initBaseView() {
		mAppRootLayoutRL = (RelativeLayout) findViewById(R.id.app_root_view);
		View titleView = findViewById(R.id.app_common_titleview_layout);
		mAppTitleBackLL = (LinearLayout) titleView.findViewById(R.id.app_common_titleview_left);
		mAppTitleBackIV = (ImageView) titleView.findViewById(R.id.app_common_title_view_back);
		mAppTitleNameTV = (TextView) titleView.findViewById(R.id.app_common_title_view_name);
		mAppTitleRightLL = (LinearLayout) titleView.findViewById(R.id.app_common_titleview_right);
		mAppTitleRightIV = (ImageView) titleView.findViewById(R.id.app_common_title_view_infomation);
		mAppContentLayoutRL = (RelativeLayout) findViewById(R.id.app_content_view);
		mAppContentLoadingPB = (ProgressBar) findViewById(R.id.app_content_loading_data);
		mAppContentEmptyTV = (TextView) findViewById(R.id.app_content_empty_data);
		mAppContentFailedTV = (TextView) findViewById(R.id.app_content_load_failed_data);
	}

	/**
	 * 初始化基础界面的监听器
	 */
	private void initBaseViewListener() {
		mAppTitleBackLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backView();
			}
		});
		mAppTitleRightLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				titleRightClick(v);
			}
		});
		mAppRootLayoutRL.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mAppRootLayoutRL.setFocusable(true);
				mAppRootLayoutRL.setFocusableInTouchMode(true);
				mAppRootLayoutRL.requestFocus();
				return false;
			}
		});
		mAppContentFailedTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onReloadClick(v);
			}
		});
	}

	/**
	 * 设置标题名称
     *
     * @param name
	 */
	public void setTitleName(String name) {
		mAppTitleNameTV.setText(name);
	}

    public void setTitleRightVisible() {
		mAppTitleRightLL.setVisibility(View.VISIBLE);
	}

    public void setTitleRightInvisible() {
		mAppTitleRightLL.setVisibility(View.INVISIBLE);
	}

	public boolean isTitleRightVisible() {
		return mAppTitleRightLL.isShown();
	}
	
	/**
	 * 设置标题栏左边的图标
	 * @param ResId 资源ID
	 */
	public void setTitleLeftIcon(int ResId) {
		mAppTitleBackIV.setImageResource(ResId);
	}

	/**
	 * 设置标题栏左边的图标
	 * @param ResId 资源ID
	 */
	public void setTitleRightIcon(int ResId) {
		mAppTitleRightIV.setImageResource(ResId);
	}
	
	/**
	 * 显示正在加载进度条
	 */
	public void showLoadingView() {
		mAppContentLoadingPB.setVisibility(View.VISIBLE);
		mAppContentEmptyTV.setVisibility(View.GONE);
		mAppContentFailedTV.setVisibility(View.GONE);
	}

    /**
	 * 隐藏内容状态的界面
	 */
	public void dismissContentStatusView() {
		mAppContentLoadingPB.setVisibility(View.GONE);
		mAppContentEmptyTV.setVisibility(View.GONE);
		mAppContentFailedTV.setVisibility(View.GONE);
	}
	
	/**
	 * 显示没有数据的界面
	 */
	public void showEmptyView() {
		showEmptyView("暂无数据!");
	}
	
	/**
	 * 显示没有数据的界面
	 */
	public void showEmptyView(String str) {
		mAppContentEmptyTV.setText(str);
		mAppContentEmptyTV.setVisibility(View.VISIBLE);
		mAppContentLoadingPB.setVisibility(View.GONE);
		mAppContentFailedTV.setVisibility(View.GONE);
	}
	
	/**
	 * 显示加载失败的视图
	 */
	public void showContentFailedView() {
		showContentFailedView("加载失败，点击重试!");
	}
	
	/**
	 * 显示加载失败的视图
	 * @param str 要显示的字符串
	 */
	public void showContentFailedView(String str) {
		mAppContentFailedTV.setText(str);
		mAppContentFailedTV.setVisibility(View.VISIBLE);
		mAppContentLoadingPB.setVisibility(View.GONE);
		mAppContentEmptyTV.setVisibility(View.GONE);
	}
	


	/**
	 * 一般通用网络请求方法
	 * @param url 网络地址
	 */
	public void loadServiceData(String url) {

		OKHttpCustomUtils.get(url, jsonObjectCallBack);
	}
	
	/**
	 * 评教选人界面的网络请求方法
	 * @param url 网络地址
	 * @param type 搜索类型
	 */
	public void loadServiceData(String url,int type) {
		final int[] tag = {-1};
		OKHttpCustomUtils.get(url, type, new StringCallback() {
			@Override
			public void onResponse(String response, int id) {
				if ("error".equals(response)) {
					//请求成功，但是服务器返回error字符串，说明服务器有问题
					urlQuesStatusCode = 2;
					return;
				}
				urlQuesStatusCode = 1;
				onDoingTask(response, tag[0]);
			}

			@Override
			public String parseNetworkResponse(Response arg0, int arg1) throws IOException {
				tag[0] = (int) arg0.request().tag();
				return arg0.body ().string();
			}

			@Override
			public void onError(Call call, Exception e, int i) {
				if ( e.getMessage()!= null) {
					if (e.getMessage().contains("No address associated with hostname")) {
						urlQuesStatusCode = 3;
					} else {
						urlQuesStatusCode = 2;
					}
				} else {
					urlQuesStatusCode = 2;
				}
				if (e.getMessage().contains("500")){
					ToastUtil.toastAlerMessageBottom(AppBaseActivity.this, "内部服务器错误", 700);
				}else if (e.getMessage().contains("401")&& "invalid_token".equals(e.getMessage())){
					initAppTokenData(true);
				}
				if (e.getMessage().contains("time out")) {
					urlQuesStatusCode = 3;
				} else {
					urlQuesStatusCode = 2;
				}
			}


			@Override
			public void onBefore(Request request, int id) {
				urlQuesStatusCode = 0;
				onPreTask();
			}

			@Override
			public void onAfter(int id) {
				dismissContentStatusView();
				switch (urlQuesStatusCode) {
					case 1:
						onDoneTask(null);
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
	 * @param resultJson
	 * @param type
	 */
	protected void onDoingTask(String resultJson, int type) {}
	
	/**
	 * 获取服务器数据
	 * @param url 接口请求地址
	 * @param loadingVisible 是否显示加载视图
	 */
	@SuppressLint("StaticFieldLeak")
	@Deprecated
	public void loadDataTask(String url,final boolean loadingVisible) {

        new AsyncTask<String, Integer, Object>() {
			private boolean isFailed = false;
			@Override
            protected void onPreExecute() {
				isFailed = false;
				if (loadingVisible) {
					showLoadingView();
				}
				onPreTask();
			}

			@Override
			protected Object doInBackground(String... params) {
				Object resultObject = null;
				try {
					String url = params[0];
					String resultJson = HttpUtil.getHttpURLConnRequestWithTimeout(
							AppBaseActivity.this, url, null, false, 6000);
//					String resultJson ="";
					resultObject = onDoingTask(resultJson);
				} catch (Exception e) {
					e.printStackTrace();
					isFailed = true;
				}
				return resultObject;
			}

            @Override
            protected void onPostExecute(Object result) {
				if (isFailed && result == null) {
//					onFailedTask();
				} else {
//					dismissLoadingView();
					onDoneTask(result);
				}
			}

		}.executeOnExecutor(FileDownLoader.getInstance().getThreadPool(), url);
	}
	
	@Override
	public void onBackPressed() {
		backView();
	}
	
	/**
	 * 标题栏右侧点击事件，可重写自定义方法
	 * @param v 标题栏右侧视图
	 */
	public void titleRightClick(View v) {}
	
//	/**
//	 * 初始化当前界面除基础元素的元素视图
//	 */
//	public abstract void initView();
//	/**
//	 * 初始化当前界面除基础元素的监听器
//	 */
//	public abstract void initViewListener();

	/**
     * 加载失败，点击重试
     * @param v 屏幕中央视图
     */
    public void onReloadClick(View v) {
    }

    /**
     * 物理返回键和标题栏返回按钮的返回时间
	 */
	public abstract void backView();
	
	/**
	 * 线程执行前的方法
	 */
	public abstract void onPreTask();
	
	/**
	 * 子线程执行过程中，获取到了json
	 * @param resultJson 服务器返回的json数据
	 * @return 解析json后的数据接口对象
	 */
	public abstract Object onDoingTask(String resultJson);
	
	/**
	 * 子线程执行完后的方法
	 * @param result 封装好的对象
	 */
	public abstract void onDoneTask(Object result);
	
	/**
	 * 请求失败
     * @param urlQuesStatusCode2
     */
	public abstract void onFailedTask(int urlQuesStatusCode2);
}
