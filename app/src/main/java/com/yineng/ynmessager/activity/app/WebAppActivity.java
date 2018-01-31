//***************************************************************
//*    2015-10-13  下午2:01:53
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.MyApp;
import com.yineng.ynmessager.db.MyAppsTb;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.V8TokenManager;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 需要用 {@link Intent#putExtra(String, String)} 方法传入“Url”的参数字符串
 *
 * @author 贺毅柳
 */
public class WebAppActivity extends BaseActivity implements OnClickListener
{
	//	private RelativeLayout mRel_actionBar;
	private WebView mWeb_content;
	//	private TextView mTxt_title;
	private PopupWindow mSubMenuWindow;
	private Context mContext;
	private LinearLayout mAppTitleBackLL;
	private ImageView mAppTitleBackIV;
	private TextView mAppTitleNameTV;
	private LinearLayout mAppTitleRightLL;
	private ImageView mAppTitleRightIV;
	private ArrayList<MyApp> mAppMenus = new ArrayList<>();
	private ProgressBar mWeb_loading;
	private DownloadManager downloadManager;
	private FrameLayout mFullVideoView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mContext = WebAppActivity.this;
		setContentView(R.layout.activity_web_app);
		initViews();
	}

	private void initViews()
	{
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		findViews();

		Intent intent = getIntent();
		String url = intent.getStringExtra("Url");
		String titleName = intent.getStringExtra("Name");
		String parentId = intent.getStringExtra("AppId");

		mAppTitleNameTV.setText(titleName);
		L.i(mTag,"loading url : " + url);

		initAppMenus(parentId);

		mAppTitleNameTV.setOnClickListener(this);
		mAppTitleBackLL.setOnClickListener(this);
		initWebviewSetting();

		mWeb_content.requestFocusFromTouch();
		mWeb_content.setOnCreateContextMenuListener(this);
		mWeb_content.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				mWeb_loading.setVisibility(View.GONE);
			}
//
//			@Override
//			public boolean shouldOverrideUrlLoading(WebView webView, String url) {
//				webView.loadUrl(url);
//				return true;
//			}
		});
		mWeb_content.setWebChromeClient(webChromeClient);

		mWeb_content.setDownloadListener(new MyWebViewDownLoadListener());
		if(!TextUtils.isEmpty(url)) {
			mWeb_content.loadUrl(url);
			mWeb_loading.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置webview
	 */
	private void initWebviewSetting() {
//		mWeb_content.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		WebSettings settings = mWeb_content.getSettings();
		//设置此属性，可任意比例缩放
		settings.setUseWideViewPort(true);
		//设置webview可运行JavaScript方法
		settings.setJavaScriptEnabled(true);
		//设置JavaScript自动打开窗口.This applies to the JavaScript function window.open()
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		//开启 DOM storage API 功能,设置可以使用localStorage
		settings.setDomStorageEnabled(true);
		//设置可以有缓存
		settings.setAppCacheEnabled(true);
		//设置缓存目录
		String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
		L.e("appCachePath == "+ appCachePath);
		settings.setAppCachePath(appCachePath);
		//设置缓存大小
		settings.setAppCacheMaxSize(1024 * 1024 * 8);//8M
		// 应用可以有数据库
		settings.setDatabaseEnabled(true);
		String dbPath = getApplicationContext().getDatabasePath("databases").getAbsolutePath();
		settings.setDatabasePath(dbPath);
		settings.setAllowFileAccess(true);
		settings.setSavePassword(false);
		settings.setSaveFormData(false);
		//设置 缓存模式
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//		settings.setPluginsEnabled(true);
		settings.setPluginState(WebSettings.PluginState.ON);
	}

	/**
	 *
	 */
	private void findViews() {
		View view = findViewById(R.id.app_webview_titleview_layout);
		mAppTitleBackLL = (LinearLayout) view.findViewById(R.id.app_common_titleview_left);
		mAppTitleBackIV = (ImageView) view.findViewById(R.id.app_common_title_view_back);
		mAppTitleNameTV = (TextView) view.findViewById(R.id.app_common_title_view_name);
		mAppTitleRightLL = (LinearLayout) view.findViewById(R.id.app_common_titleview_right);
		mAppTitleRightIV = (ImageView) view.findViewById(R.id.app_common_title_view_infomation);
		mWeb_content = (WebView)findViewById(R.id.webApp_web_content);
		mWeb_loading = (ProgressBar) findViewById(R.id.web_view_loading);
		// 声明video，把之后的视频放到这里面去
		mFullVideoView = (FrameLayout) findViewById(R.id.video);
	}

	/**
	 * 如果当前模块有子菜单，加载子菜单并显示
	 * @param parentId 主页被点击的模块ID
	 *
	 */
	private void initAppMenus(String parentId) {
		if (parentId != null && !parentId.isEmpty()) {
			MyAppsTb mMyAppsTb = new MyAppsTb(mContext);
			mAppMenus  = mMyAppsTb.queryByPid(parentId);
			if (mAppMenus.size() > 0) {
				ArrayList<HashMap<String, String>> subMenuData = new ArrayList<>();
				HashMap<String, String> subMenu;
				for (MyApp menuApp : mAppMenus) {
					subMenu = new HashMap<>();
					subMenu.put("menu",menuApp.getTitle());
					subMenuData.add(subMenu);
				}

				SimpleAdapter mSubMenuListAdapter = new SimpleAdapter(this,subMenuData,R.layout.item_web_app_sub_menu,new String[] {"menu"},
						new int[] {R.id.webApp_txt_subMenu_listItem_content});
				View subMenuView = getLayoutInflater().inflate(R.layout.view_web_app_sub_menu,null);
				ListView subMenuList = (ListView)subMenuView.findViewById(R.id.webApp_lst_subMenuList);
				subMenuList.setAdapter(mSubMenuListAdapter);
				subMenuList.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						if (mSubMenuWindow != null) {
							mSubMenuWindow.dismiss();
						}
						MyApp menuApp = mAppMenus.get(position);
						mAppTitleNameTV.setText(menuApp.getTitle());
						if(!TextUtils.isEmpty(menuApp.getLink())) {
							String url = MessageFormat.format(mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL,V8TokenManager.obtain(),
									mApplication.mLoginUser.getUserNo(),menuApp.getLink());
							mWeb_content.loadUrl(url);
							mWeb_loading.setVisibility(View.VISIBLE);
							L.i(mTag,"loading url : " + url);
						}
					}
				});

				//初始化弹出框
				mSubMenuWindow = new PopupWindow(subMenuView,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
				mSubMenuWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),R.color.main_content_bg,getTheme()));
				mSubMenuWindow.setOutsideTouchable(true);
				mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
						getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
				//popwin消失时改变箭头方向
				mSubMenuWindow.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
								getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
					}
				});
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		if (mSubMenuWindow != null) {
			mSubMenuWindow.dismiss();
		}

		mWeb_content.stopLoading();
		//避免java.lang.Throwable: Error: WebView.destroy() called while still attached!
		RelativeLayout webParentView = (RelativeLayout) mWeb_content.getParent();
		if (webParentView != null) {
			webParentView.removeView(mWeb_content);
			mWeb_content.destroy();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			// 如果是全屏状态 按返回键则变成非全屏状态，否则执行返回操作
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				quitFullScreen();
			} else {
				if(mWeb_content.canGoBack())
				{
					mWeb_content.goBack();// 返回上一页面
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode,event);
	}

	@Override
	public void onClick(View v)
	{
		switch(v.getId())
		{
			case R.id.app_common_title_view_name:
				if (mSubMenuWindow == null) {
					return;
				}
				mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,ResourcesCompat.getDrawable(getResources(),R.mipmap.contact_fast_jump_arrow_up,getTheme()), null);
				PopupWindowCompat.showAsDropDown(mSubMenuWindow,mAppTitleNameTV,0,0,Gravity.NO_GRAVITY);
				break;
			case R.id.app_common_titleview_left:
				finish();
				break;
		}
	}

	private class MyWebViewDownLoadListener implements DownloadListener {
		@Override
		public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
									long contentLength) {
			int systemApiLevel = SystemUtil.getSystemVersion();
			L.i("url == "+url);
			L.i("userAgent == "+userAgent);
			L.i("contentDisposition == "+contentDisposition);
			L.i("mimetype == " + mimetype);
			L.i("contentLength == " + contentLength);
			if (systemApiLevel > 10) {// 2.3以上版本调用DownloadManager下载
				try {
					String fileName = null;
					if (TextUtils.isEmpty(contentDisposition)) {
						if (url.contains("/")) {
							fileName = url.substring(url.lastIndexOf("/")+1).trim();
						}
					} else {
						if (contentDisposition.contains("filename=")) {
							fileName = contentDisposition.substring(contentDisposition.lastIndexOf("filename=")+"filename=".length()).trim();
							fileName = new String(fileName.getBytes("iso-8859-1"), "gb2312");
						}
					}
					startDownloadWebFile(url,fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {// 2.3及其以下版本调用浏览器下载
				Uri uri = Uri.parse(url);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(it);
			}
		}
	}

	/**
	 * 下载网页的文件
	 * @param url
	 * @param fileName
	 */
	private void startDownloadWebFile(String url, String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			int idx = imgurl.lastIndexOf(".");
			String ext = imgurl.substring(idx);
			fileName = TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24) + ext;
		}
		L.i("url == "+url);
		Uri uri = Uri.parse(url);
		DownloadManager.Request request = new DownloadManager.Request(uri);
		if (fileName != null) {
			request.setDestinationInExternalFilesDir(mContext, null, fileName);
			request.setTitle(fileName);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			request.setMimeType("application/com.yineng.ynmessager.download.file");
			request.setDestinationInExternalPublicDir(FileUtil.getUserSDPath(false,""),fileName);
		}
		downloadManager.enqueue(request);
	}


	private String mCameraFilePath = null;
	private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
	private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>

	private WebChromeClient webChromeClient = new WebChromeClient() {
		WebChromeClient.CustomViewCallback customViewCallback;

		// 一个回调接口使用的主机应用程序通知当前页面的自定义视图已被撤职
		// 进入全屏的时候
		@Override
		public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
			// 赋值给callback
			customViewCallback = callback;
			// 设置webView隐藏
			mWeb_content.setVisibility(View.GONE);
//			back_btn.setVisibility(View.VISIBLE);
			// 将video放到当前视图中
			mFullVideoView.addView(view);
			// 横屏显示
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			// 设置全屏
			setFullScreen();
			mFullVideoView.setVisibility(View.VISIBLE);
		}

		@Override
		public void onHideCustomView() {
			if (customViewCallback != null) {
				// 隐藏掉
				customViewCallback.onCustomViewHidden();
			}
			// 用户当前的首选方向
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// 退出全屏
			quitFullScreen();
			// 设置WebView可见
			mWeb_content.setVisibility(View.VISIBLE);
//			back_btn.setVisibility(View.GONE);
		}

		// For Android 3.0+
		public void openFileChooser(ValueCallback<Uri> uploadMsg) {
			if (uploadMsg == null) {
				return;
			}

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("image/*");
			startActivityForResult(
					Intent.createChooser(i, "File Chooser"),
					FILECHOOSER_RESULTCODE);

		}

		// For Android 3.0+
		public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
			if (uploadMsg == null) {
				return;
			}

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			startActivityForResult(
					Intent.createChooser(i, "File Browser"),
					FILECHOOSER_RESULTCODE);
		}

		// For Android 4.1
		public void openFileChooser(ValueCallback<Uri> uploadMsg,
									String acceptType, String capture) {
			if (uploadMsg == null) {
				return;
			}

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("image/*");
			startActivityForResult(
					Intent.createChooser(i, "File Chooser"),
					FILECHOOSER_RESULTCODE);

		}

		private Intent createDefaultOpenableIntent() {
			// Create and return a chooser with the default OPENABLE
			// actions including the camera, camcorder and sound
			// recorder where available.
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");

			Intent chooser = createChooserIntent(createCameraIntent(),
					createCamcorderIntent(), createSoundRecorderIntent());
			chooser.putExtra(Intent.EXTRA_INTENT, i);
			return chooser;
		}

		private Intent createChooserIntent(Intent... intents) {
			Intent chooser = new Intent(Intent.ACTION_CHOOSER);
			chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
			chooser.putExtra(Intent.EXTRA_TITLE, "File Chooser");
			return chooser;
		}

		private Intent createCameraIntent() {
			Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			File externalDataDir = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
			System.out.println("externalDataDir:" + externalDataDir);
			File cameraDataDir = new File(externalDataDir.getAbsolutePath()
					+ File.separator + "browser-photo");
			cameraDataDir.mkdirs();
			mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
					+ System.currentTimeMillis() + ".jpg";
			System.out.println("mcamerafilepath:" + mCameraFilePath);
			cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(mCameraFilePath)));

			return cameraIntent;
		}

		private Intent createCamcorderIntent() {
			return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		}

		private Intent createSoundRecorderIntent() {
			return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
		}
	};

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage) {
                return;
            }
			L.e("onActivityResult == ");
			Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
					: intent.getData();
			L.e("mCameraFilePath == "+mCameraFilePath+" result == "+result);
			if (result == null && intent == null
					&& resultCode == Activity.RESULT_OK) {
				L.e("upload == ");
				File cameraFile = new File(mCameraFilePath);

				if (cameraFile.exists()) {
					result = Uri.fromFile(cameraFile);
					sendBroadcast(new Intent(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
				}
			}

			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
		}
	}

	private String imgurl = "";

	/***
	 * 创建长按图片保存到手机时的界面显示
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v instanceof WebView) {
			WebView.HitTestResult result = ((WebView) v).getHitTestResult();
			if (result != null) {
				int type = result.getType();
				if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
					imgurl = result.getExtra();
					menu.setHeaderTitle("提示");
					menu.add(0, 0, 0, "保存图片");
				}
			}
		}
	}

	/**
	 * 点击菜单中的某项的实现功能
	 * @param item 菜单项
	 * @return
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getTitle() == "保存图片") {
			startDownloadWebFile(imgurl,null);
		}
		return true;
	}

	/**
	 * 设置全屏
	 */
	private void setFullScreen() {
		// 设置全屏的相关属性，获取当前的屏幕状态，然后设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 全屏下的状态码：1098974464
		// 窗口下的状态吗：1098973440
	}

	/**
	 * 退出全屏
	 */
	private void quitFullScreen() {
		// 声明当前屏幕状态的参数并获取
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		mFullVideoView.setVisibility(View.GONE);
	}
}
