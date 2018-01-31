package com.yineng.ynmessager.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.Splash.SplashActivity;
import com.yineng.ynmessager.activity.TransmitActivity.TransmitActivity;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.activity.contact.ContactChildOrgActivity;
import com.yineng.ynmessager.activity.contact.ContactGroupOrgActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.update.DownloadVersionActivity;
import com.yineng.ynmessager.app.update.UpdateCheckUtil;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.contact.ContactOrg;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.camera.CameraActivity;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.netWorkChangedListener;
import com.yineng.ynmessager.receiver.CommonReceiver.onLoginedOtherListener;
import com.yineng.ynmessager.service.DownloadService;
import com.yineng.ynmessager.service.LocateService;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.dialog.LoadingDialogWithDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * 所有Activity都应从此类继承
 *
 * @author 贺毅柳
 *
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static final int RESTARTING = 1;
	public static final int PAUSE = 2;
	public static final int START = 3;
	public static final int CANCEL = 4;
	// BaseActivity的自己TAG
	private static final String BASE_TAG = "BaseActivity";
	// 用来储存当前运行中的Activity实例的List
	private static final LinkedList<Activity> M_ACTIVITY_LIST = new LinkedList<Activity>();
	public static int mCurrentStatus;// 0；没有下载任务，1：正在下载，2，暂停，3，下载完成 4取消下载
	/**
	 * 存放发送的消息数据，用于处理消息回执,确保该变量里面存放的是聊天窗口未处理消息回执的消息
	 * key为packetId
	 */
	public static HashMap<String, Object> mChatMsgBeanMap = new HashMap<String, Object>();
	/**
	 * 下载APP过程中链接超时
	 */
	public static final int UPDATE_CON_TIMEOUT = 101;
	/**
	 * 下载过程中IO异常
	 */
	public static final int UPDATE_IO_EXCEP = 102;
	// 每个Activity的TAG
	protected final String mTag = this.getClass().getSimpleName();
	// Application
	protected final AppController mApplication = AppController.getInstance();
	/**
	 * 网络状态监听器
	 */
	public CommonReceiver mNetWorkChangedReceiver;
	/**
	 * 身份验证过期对话框
	 */
	public AlertDialog mIdPastDialog;
	/**
	 * 正在检测更新
	 */
	public LoadingDialogWithDialog mCheckProgressDialog;
	/**
	 * 检测升级工具
	 */
	public UpdateCheckUtil mCheckAppUtil;
	private XMPPConnection mConnection = XmppConnectionManager.getInstance().getConnection();
	private  Context mContext = this;
	/**
	 * 下线通知对话框
	 */
	private AlertDialog mOfflineNoticeDialog;
	/**
	 * 加载联系人的广播
	 */
	private LoadContactReceiver mLoadContactReceiver;

	/**
	 * 升级检测结果的回调接口
	 */
	public UpdateCheckUtil.checkVersionListener mCheckAppListener = new UpdateCheckUtil.checkVersionListener() {

		private void handleUpdateChecked(boolean isHandCheck) {
			//如果是登录界面，则进入主界面;其他界面不响应
			if (BaseActivity.this instanceof LoginActivity) {
				EventBus.getDefault().post(new LoginUser("1"));//进入登录流程
			} else {
				//如果是关于界面点击更新，则不更新联系人信息
				if (isHandCheck) {
                    return;
                }
				EventBus.getDefault().post(new ContactOrg("1"));//更新联系人信息
			}
		}

		@Override
		public void onCheckVerResult(int checkResult, boolean isHandCheck) {

			switch (checkResult) {
				case UpdateCheckUtil.CheckResult.INIT_URL_FAILED:
				case UpdateCheckUtil.CheckResult.YNEDUT_TOKEN_FAILED:
				case UpdateCheckUtil.CheckResult.YNEDUT_VER_FAILED:
					if (isHandCheck) {
                        return;//如果是关于界面手动更新，则保持xmpp连接，即return
                    }
					//如果是登录界面，则关闭连接；如果是主界面或者其他，则断开连接，在消息列表给出提示
					if (BaseActivity.this instanceof LoginActivity || BaseActivity.this instanceof SplashActivity) {
						disconOpenfire(true);
						EventBus.getDefault().post(new LoginUser("0"));//取消登录界面的正在登录动画
					} else {
						disconOpenfire(false);
						EventBus.getDefault().post(new ContactOrg("0"));//更新消息列表提示
					}
					break;
				case UpdateCheckUtil.CheckResult.CENTER_GET_APP_FILE_FAILED:
					showNoMsg();
					break;
				case UpdateCheckUtil.CheckResult.CENTER_REDIS_ERROR:
					showNoMsg();
					break;
				case UpdateCheckUtil.CheckResult.CENTER_APPINFO_FAILED:
					//将获取到的版本号与预置的版本号进行比较，判断客户端可用不可用；如果不可用，提示不可用，如果不是在登录界面，则要返回登录界面
					String v8Version = LastLoginUserSP.getYnedutVersion(mContext);
					L.e(v8Version+" "+Const.YNEDUT_LOCAL_VERSION);
					try {
						if (Integer.parseInt(v8Version) >= Integer.parseInt(Const.YNEDUT_LOCAL_VERSION)) {//可用
							L.i("当前版本可用");
							handleUpdateChecked(isHandCheck);
						} else {//不可用
							showAppUnavailableView(mContext.getString(R.string.update_ynedut_app_info_failed), isHandCheck);
							L.e(mTag,"11001");
						}
					} catch (Exception e) {}

					break;
				case UpdateCheckUtil.CheckResult.CENTER_NOT_FOUNT_AVAIABLE_PRODUCT:
					ToastUtil.toastAlerMessageBottom(mContext, "当前已是最新版本", Toast.LENGTH_SHORT);
					handleUpdateChecked(isHandCheck);
					break;
				case UpdateCheckUtil.CheckResult.CENTER_APP_UNAVAIABLE:
					//Center检测结果是客户端不可用,提示不可用，如果不是在登录界面，则要返回登录界面
					showAppUnavailableView(UpdateCheckUtil.YNEDUT_APP_MISMATCH, isHandCheck);
					break;
				case UpdateCheckUtil.CheckResult.CENTER_APP_IS_NEWEST://最新版
					if (isHandCheck) {
						ToastUtil.toastAlerMessageBottom(mContext, "当前已是最新版本", Toast.LENGTH_SHORT);
					}
					handleUpdateChecked(isHandCheck);
					break;
				case UpdateCheckUtil.CheckResult.CENTER_DEPEND_PRODUCE_VER_FAILED:
					ToastUtil.toastAlerMessageBottom(mContext, "当前已是最新版本", Toast.LENGTH_SHORT);
					handleUpdateChecked(isHandCheck);
					break;
				default:
					break;
			}
			hideProgessD();
		}


		@Override
		public void onHandleUpdateVer(int handleType, boolean isHandCheck) {
			switch (handleType) {
				case UpdateCheckUtil.CheckResult.CENTER_APP_UPDATE_HAND:
					//有新版,非强制更新
					handleUpdateChecked(isHandCheck);
					break;
				case UpdateCheckUtil.CheckResult.CENTER_APP_UPDATE_QUIT_MUST:
					//有新版,退出强制更新
//					exit(true);
					logout(true);
					break;
				case UpdateCheckUtil.CheckResult.CENTER_APP_UPDATE_DO_MUST:
					//有新版,且强制更新
					disconOpenfire(false);
					break;
				default:
					break;
			}
			hideProgessD();
		}
	};
	/**
	 * Home键按下的广播接收器
	 */
	private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
		String systemReason = "reason";
		String systemHomeKey = "homekey";
		String systemHomeKeyLong = "recentapps";

		@Override public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(systemReason);
				if (TextUtils.equals(reason, systemHomeKey)) {
					// 表示按了home键,程序到了后台
					L.i(BASE_TAG, "home键返回桌面");
					mApplication.isShownOnScreen = false;
				} else if (TextUtils.equals(reason, systemHomeKeyLong)) {
					// 表示长按home键,显示最近使用的程序列表
					L.i(BASE_TAG, "home键显示最近程序列表");
				}
			}
		}
	};

	/**
	 * 退出时调用此方法（关闭所有的Activity，并根据参数判断是否关闭系统进程）
	 *
	 * @param isShutDownProcess 是否彻底退出并关闭进程
	 */
	public static void exit(boolean isShutDownProcess) {
		// 清理Activity
		Iterator<Activity> iterator = M_ACTIVITY_LIST.iterator();
		Activity activity;
		while (iterator.hasNext()) {
			activity = iterator.next();
			if (activity != null) {
				activity.finish();
			}
			L.d(BASE_TAG, "正在销毁" + activity.getClass().getSimpleName());
		}
		L.d(BASE_TAG, "完成销毁所有的Activity");
		// 彻底关闭
		if (isShutDownProcess) {
			L.i(BASE_TAG, "彻底关闭了应用进程");

			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
		}
	}

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 *
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful.
	 * If a deletion fails, the method stops attempting to
	 * delete and returns "false".
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			//递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * 显示客户端不能用的视图
	 */
	private void showAppUnavailableView(String noitceMsg, boolean isHandCheck) {
		L.e("当前版本不可用");
		if (BaseActivity.this instanceof LoginActivity) {
			ToastUtil.toastAlerMessageBottom(BaseActivity.this,noitceMsg,Toast.LENGTH_SHORT);
			EventBus.getDefault().post(new LoginUser("0"));//取消登录界面的正在登录动画
		} else {
			ToastUtil.toastAlerMessageBottom(BaseActivity.this, noitceMsg, Toast.LENGTH_SHORT);
			initIdPastDialog(noitceMsg);
		}
	}

	/**
	 * 不提示任何信息并判断是否在登陆页面
	 * 由 {@Link com.yineng.ynmessager.activity.BaseActicity.showAppUnavailableView} 分化而来
	 */
	public void showNoMsg() {
		if (BaseActivity.this instanceof LoginActivity) {
			//正常进入首页
			EventBus.getDefault().post(new LoginUser("1"));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		M_ACTIVITY_LIST.add(this);
		L.v(BASE_TAG,"启动并添加了Activity：" + mTag);

		initSettingsTb();
		if(savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
        }
		initView();
		initImageLoader();
		initListener();
		initOfflineDialog();
//		initOfflineDetect();
	}

	private void restoreSavedInstanceState(Bundle savedInstanceState)
	{
		L.d(mTag, "savedInstanceState 已经执行恢复");
		mApplication.mLoginUser = savedInstanceState.getParcelable("AppController.mLoginUser");
		mApplication.mSelfUser = savedInstanceState.getParcelable("AppController.mSelfUser");
		mApplication.mUserSetting = savedInstanceState.getParcelable("AppController.mUserSetting");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putParcelable("AppController.mLoginUser", mApplication.mLoginUser);
		outState.putParcelable("AppController.mSelfUser", mApplication.mSelfUser);
		outState.putParcelable("AppController.mUserSetting", mApplication.mUserSetting);
		L.d(mTag, "onSaveInstanceState 执行了");
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		mApplication.isShownOnScreen = true; // 每个Activity显示的时候，都标示当前程序界面处于显示状态
		mCheckAppUtil.setContext(BaseActivity.this);
		// 清除通知栏的消息
		NoticesManager.getInstance(getApplicationContext()).clearMessageTypeNotice();
		// 注册Home键按下的广播接收器
		registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

		if("SplashActivity".equals(mTag) ) {
            return;
        }

		initLoadingContactBroadcast(); // 注册加载联系人信息的广播
		registerNetworkListener();//注册网络状态监听器
	}

	@Override protected void onRestart() {
		super.onRestart();

		L.d(mTag, "onRestart");
		if(this instanceof SplashActivity || this instanceof LoginActivity) {
            return;
        }

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//注册eventbus
		if (!EventBus.getDefault().isRegistered(this)){
			EventBus.getDefault().register(this);
		}
		StatService.onResume(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		StatService.onPause(this);
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		/***** 删除用户状态监听器 *****/
		// XmppConnectionManager.getInstance().removeStatusChangedCallBack(this);
		if(mIdPastDialog != null && mIdPastDialog.isShowing())
		{
			mIdPastDialog.dismiss();
		}
		if(mLoadContactReceiver != null)
		{
			L.i(mTag + "反注册加载联系人信息的广播");
			unregisterReceiverSafe(mLoadContactReceiver);
		}
		if(mHomeKeyEventReceiver != null)
		{
			unregisterReceiver(mHomeKeyEventReceiver);
		}
		if(mNetWorkChangedReceiver != null)
		{
			unregisterReceiverSafe(mNetWorkChangedReceiver);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		// Unregister
		EventBus.getDefault().unregister(this);
		M_ACTIVITY_LIST.remove(this);
		L.v(BASE_TAG,"关闭并移除了Activity：" + mTag);

//		if(mNetWorkChangedReceiver != null)
//		{
//			unregisterReceiverSafe(mNetWorkChangedReceiver);
//		}

		// 取消所有dialog显示，避免造成Activity内存泄露
		if(mOfflineNoticeDialog != null) {
            mOfflineNoticeDialog.dismiss();
        }
		if(mIdPastDialog != null) {
            mIdPastDialog.dismiss();
        }
		if(mCheckProgressDialog != null) {
            mCheckProgressDialog.dismiss();
        }
	}

	/**
	 * 初始化界面
	 */
	private void initView()
	{
	}

	private void initListener() {

		mCheckAppUtil = UpdateCheckUtil.getInstance();
		mCheckAppUtil.setOnCheckVersionListener(mCheckAppListener);
	}

	private void initImageLoader()
	{
		ImageLoader imageLoader = ImageLoader.getInstance();
		if(!(this instanceof SplashActivity) && !(this instanceof LoginActivity) && !imageLoader.isInited())
		{
			L.v(mTag,"Universal Image Loader 正在初始化...");
			ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());
			builder.defaultDisplayImageOptions(mApplication.initImageLoaderDisplayOptions());
			builder.diskCache(new UnlimitedDiskCache(new File(FileUtil.getImgPath(null))));
			imageLoader.init(builder.build());
		}
	}

	private void initSettingsTb()
	{
		if(this instanceof SplashActivity || this instanceof LoginActivity) {
            return;
        }

		SettingsTb settingsTb = new SettingsTb(getApplicationContext());
		settingsTb.insert(); // 尝试插入默认设置数据
		// 将设置数据库数据读到Application->Setting中
		mApplication.mUserSetting = settingsTb.obtainSettingFromDb();
	}

	@Override
	public void setContentView(int layoutResID)
	{
		initThisTheme();
		super.setContentView(layoutResID);
	}

	@Override
	public void setContentView(View view)
	{
		initThisTheme();
		super.setContentView(view);
	}

	@Override
	public void setContentView(View view, LayoutParams params)
	{
		initThisTheme();
		super.setContentView(view, params);
	}

	/**
	 * 根据Setting来设置当前Activity主题
	 */
	private void initThisTheme()
	{
		// 不能设置主题的Activity
		if(this instanceof LoginActivity || this instanceof SplashActivity || this instanceof DownloadVersionActivity)
		{
			return;
		}

		// 设置主题
		int theme;
		if(mApplication.mUserSetting.getDarkMode() == 0)
		{
			theme = R.style.MyAppTheme_Light;
		}else
		{
			theme = R.style.MyAppTheme_Dark;
		}
		setTheme(theme);
	}

	/**
	 * 注册加载联系人信息的广播
	 */
	protected void initLoadingContactBroadcast()
	{
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Const.BROADCAST_ACTION_LOADING_CONTACT);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_LOADED_CONTACT);
		mLoadContactReceiver = new LoadContactReceiver();
		registerReceiver(mLoadContactReceiver,mIntentFilter);
		L.i(mTag + "注册加载联系人信息的广播");
	}

	/**
	 * 注册网络状态监听器
	 */
	private void registerNetworkListener(){
		//注册广播接收器
		IntentFilter intentFilter;
		if("SplashActivity".equals(mTag))
		{
			return;
		}else
		{
			mNetWorkChangedReceiver = new CommonReceiver();
			intentFilter = new IntentFilter(Const.BROADCAST_ACTION_ID_PAST);
			intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			intentFilter.addAction(Const.BROADCAST_ACTION_LOGINED_OTHER);
			//网络状态监听
			mNetWorkChangedReceiver.setNetWorkChangedListener(new netWorkChangedListener() {

				@Override
				public void netWorkChanged(String netWorkInfo)
				{
					L.e("netWorkChanged == "+mTag);
					onNetWorkChanged(netWorkInfo);
					if (!AppUtils.isServiceWork(mContext, LocateService.class.getName())&&AppController.getInstance().mAppTokenStr.length()>0) {
						startService(new Intent(mContext, LocateService.class));
					}
				}
			});
			//下线通知监听
			mNetWorkChangedReceiver.setOnLoginedOtherListener(new onLoginedOtherListener() {

				@Override
				public void onLoginedOther() {
					//发送用户退出/注销的广播（！！！注意是LocalBroadcastManager发送的！！！）
					//Logout() 方法中也会发送一遍！！
					LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
					broadcastManager.sendBroadcast(new Intent(Const.BROADCAST_ACTION_USER_LOGOUT));
					stopService(new Intent(BaseActivity.this, LocateService.class));

					if (mOfflineNoticeDialog != null && !mOfflineNoticeDialog.isShowing()) {
						mOfflineNoticeDialog.show();
					}
				}
			});
		}
		registerReceiver(mNetWorkChangedReceiver, intentFilter);
	}

	/**
	 * 断开连接
	 */
	public void disconOpenfire(boolean isLogOff) {
		// 清空密码
		if (isLogOff) {
			LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
			lastUser.saveUserPassword("");
			//关闭用户数据库连接
			UserAccountDB.setNullInstance();
		}
		//停止聊天窗口里下载文件的服务
		stopDownLoadService();
		// 下线，停止xmpp服务
		closeXmppService();

	}

	/**
	 * 身份验证过期对话框
	 */
	public void initIdPastDialog(String content)
	{
		String con;
		if (TextUtils.isEmpty(content)){
			con = getResources().getString(R.string.common_id_past_msg);
		}else {
			con = content;
		}
		Builder tempIdPastDialog = new AlertDialog.Builder(this);
		tempIdPastDialog.setTitle(R.string.common_id_past_title);
		tempIdPastDialog.setMessage(con);
		tempIdPastDialog.setCancelable(false);
		tempIdPastDialog.setPositiveButton("确定",new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(android.content.DialogInterface dialog, int which)
			{
				logout(false);
			}
		});
		if (mIdPastDialog!=null){
			mIdPastDialog.dismiss();
		}
		mIdPastDialog = tempIdPastDialog.create();
		mIdPastDialog.show();
	}



	/**
	 * 下线通知对话框
	 */
	private void initOfflineDialog()
	{
		// 初始化对话框
		Builder mOfflineNoticeBuilder = new Builder(this);
		mOfflineNoticeBuilder.setTitle(R.string.common_offline_notice_title);
		mOfflineNoticeBuilder.setMessage(R.string.common_offline_notice_msg);
		mOfflineNoticeBuilder.setCancelable(false);
		mOfflineNoticeBuilder.setPositiveButton(getString(R.string.common_offline_retry),
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(android.content.DialogInterface dialog, int which)
					{
						if(NetWorkUtil.isNetworkAvailable(mContext))
						{
							// 取消每个activity中的下线对话框，避免重新登录后返回上一级界面还会出现该对话框
							for(Activity activity : M_ACTIVITY_LIST)
							{
								((BaseActivity)activity).mOfflineNoticeDialog.dismiss();
							}
							// 如果网络可用自动登陆
							Intent serviceIntent = new Intent(mContext,XmppConnService.class);
							startService(serviceIntent);// 开启服务，自动登陆

							new Handler().postDelayed(new Runnable() {//延时开启定位服务
								@Override
								public void run() {
									if (LastLoginUserSP.getInstance(BaseActivity.this).isLogin()) {
										BaseActivity.this.startService(new Intent(BaseActivity.this, LocateService.class));
									}
								}
							}, 3000);
						}
					}
				}).setNegativeButton(getString(R.string.common_offline_exit),
				new android.content.DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(android.content.DialogInterface dialog, int which)
					{
						logout(false);
					}
				});
		mOfflineNoticeDialog = mOfflineNoticeBuilder.create();

	}

	/**
	 * 网络状态变化时回调该方法。默认实现只是显示一个Toast提示
	 * @param info 没有网络情况下为none
	 */
	protected void onNetWorkChanged(String info)
	{
		if("none".equals(info))
		{
			AppController.NET_IS_USEFUL = false;
			ToastUtil.toastAlerMessageCenter(mContext,"当前无网络",1000);
		}else
		{
			AppController.NET_IS_USEFUL = true;
			ToastUtil.toastAlerMessageCenter(mContext,"您当前在使用" + info + "网络",1000);
		}
	}

	/**
	 * 清除栈中转发页面的activity实例
	 */
	public static void deleteTransmitActivity() {
		try {
			for (Activity activity : M_ACTIVITY_LIST) {
				if(activity instanceof TransmitActivity){
					activity.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//在联系人界面点击进去之后进入讨论组个人或群会话中不仅要消除联系人群讨论组信息界面还要消除联系人中组织机构的列表Activity
	public static void deleteContactActivitys(){
		try {
			for (Activity activity : M_ACTIVITY_LIST) {
				if (activity instanceof ContactChildOrgActivity || activity instanceof ContactGroupOrgActivity) {
					activity.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//删除自定义相机栈
	public static void deleteCanmeraActivity() {
		try {
			for (Activity activity : M_ACTIVITY_LIST) {
				if (activity instanceof CameraActivity) {
					activity.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//在出现异常的时候消除所有的Activity
	public static void deleteAllActivities(){
		try {
			for (Activity activity : M_ACTIVITY_LIST) {
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	//转发进入机构的时候因为activity是默认模式，不清楚逻辑所以在这里进行删除而不是改变activity的启动模式
	public static void deleteContactChildActivity() {
		try {
			for (Activity activity : M_ACTIVITY_LIST) {
				if (activity instanceof  TransmitActivity || activity instanceof ContactChildOrgActivity) {
					activity.finish();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 注销账户，停止服务，关闭应用
	 *
	 * @param isShutDown 是否彻底退出并关闭进程
	 *            是否关闭app; true 关闭;false不关
	 */
	public void logout(boolean isShutDown)
	{
		//发送用户退出/注销的广播（！！！注意是LocalBroadcastManager发送的！！！）
		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		broadcastManager.sendBroadcast(new Intent(Const.BROADCAST_ACTION_USER_LOGOUT));

		// 清空密码
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(this);
		lastUser.saveUserPassword("");
		//停止聊天窗口里下载文件的服务
		stopDownLoadService();
		// 下线，停止xmpp服务
		closeXmppService();

		//退出的时候同时解绑阿里推送帐号
		PushServiceFactory.getCloudPushService().unbindAccount(
				new CommonCallback() {
					@Override
					public void onSuccess(String response) {
						L.i(mTag,"解绑阿里帐号成功");
					}

					@Override
					public void onFailed(String errorCode, String errorMessage) {
						L.i(mTag,"解绑阿里帐号失败");
					}
				}
		);
		CheckMyApps.checkMyApps = null;
		//关闭用户数据库连接
		UserAccountDB.setNullInstance();

		//是否彻底退出并关闭进程
		exit(isShutDown);

		if(!isShutDown)
		{
			Intent intent = new Intent(this,LoginActivity.class);
			startActivity(intent);
		}
	}


	/**
	 * 停止xmpp的服务
	 */
	protected void closeXmppService()
	{
		XmppConnectionManager.getInstance().doExitThread();

		Intent serviceIntent = new Intent(this,XmppConnService.class);
		stopService(serviceIntent);
	}

	/**
	 * 停止聊天窗口里下载文件的服务
	 */
	protected void stopDownLoadService() {
		Intent downLoadIntent = new Intent(mContext, DownloadService.class);
		L.v("DownloadService.mDownloadMsgBeans == "+DownloadService.mDownloadMsgBeans);
		if (DownloadService.mDownloadMsgBeans != null && DownloadService.mDownloadMsgBeans.size() > 0) {
			List<BaseChatMsgEntity> msgBeans = new ArrayList<BaseChatMsgEntity>(DownloadService.mDownloadMsgBeans);
			DownloadService.mDownloadMsgBeans.clear();
			for (BaseChatMsgEntity failedMsg :msgBeans) {
				DownloadService.updateDatabaseMsgStatus(failedMsg, BaseChatMsgEntity.DOWNLOAD_FAILED);
			}
		}
		stopService(downLoadIntent);
	}

	/**
	 * openfire初始化地址加载完成，去检测升级信息
	 */
	@Subscribe(threadMode  = ThreadMode.MAIN)
	public void onEventMainThread(CommonEvent event) {
		switch (event.getWhat()){
			//自动登录进入的更新检测
			case Const.IS_AUTO_LOGIN_XMPP:
				mCheckAppUtil.checkAppVersion(BaseActivity.this,false);
				mCheckAppUtil.setShowDialog(false,true);
				break;
			//登陆页进入的更新检测
			case Const.IS_NOT_AUTO_LOGIN_XMPP:
				mCheckAppUtil.checkAppVersion(BaseActivity.this,true);
				mCheckAppUtil.setShowDialog(true,false);
				break;
			default:
				break;

		}
	}

	/**
	 * 显示Toast
	 *
	 * @param text
	 *            要显示的字符串
	 */
	protected void showToast(CharSequence text)
	{
		Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
	}

	/**
	 * 根据格式化的字符串来显示Toast
	 *
	 * @param text
	 *            要显示的格式化字符串
	 * @param args
	 *            格式化参数
	 */
	protected void showToast(String text, Object... args)
	{
		showToast(String.format(text, args));
	}

	/**
	 * 从R.string中显示字符串
	 *
	 * @param resId
	 *            字符串的资源ID
	 */
	protected void showToast(int resId)
	{
		Toast.makeText(this,resId,Toast.LENGTH_SHORT).show();
	}

	/**
	 * 从R.string中显示字符串
	 *
	 * @param resId
	 *            字符串的资源ID
	 * @param args
	 *            格式化参数
	 */
	protected void showToast(int resId, Object... args)
	{
		Toast.makeText(this,getString(resId,args),Toast.LENGTH_SHORT).show();
	}

	protected void unregisterReceiverSafe(BroadcastReceiver receiver)
	{
		try
		{
			unregisterReceiver(receiver);
		}catch(IllegalArgumentException e)
		{
			L.d(BASE_TAG,e.getMessage(),e);
		}
	}

	/**
	 * 清除webview缓存
	 */
	public void clearWebCacheData() {
		File file = getCacheDir();
		if (file != null && file.exists()) {
			deleteDir(file);
		}
		File dbfile = getDatabasePath(Const.WEBVIEW_DB_PATH);
		if (dbfile != null && dbfile.exists()) {
			deleteDir(dbfile);
		}
//		deleteDatabase("webview.db");
//		deleteDatabase("webviewCache.db");
	}




	public void showProgressD(String str,boolean cancelable)
	{
		if (mCheckProgressDialog != null && !mCheckProgressDialog.isShowing()) {
			mCheckProgressDialog = null;
		}
		mCheckProgressDialog = new LoadingDialogWithDialog(this,R.style.MyDialog,str);
		mCheckProgressDialog.setCanceledOnTouchOutside(cancelable);
		mCheckProgressDialog.setCancelable(cancelable);
		mCheckProgressDialog.show();
	}

	public void hideProgessD()
	{
		if (mCheckProgressDialog != null && mCheckProgressDialog.isShowing()) {
			mCheckProgressDialog.cancel();
		}
	}

	public void hideProgressDialog() {
		AppUtils.hideProgressDialog();
	}

	public void showProgressDialog(String title) {
		AppUtils.showProgressDialog(this, title+" ");
	}



	/**
	 * 下载成功，解析apk安装包
	 */
	protected void doDownloadedSuccess(String filepath)
	{
		L.i("安装apk");
		if(filepath.contains(".apk"))
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(filepath)),"application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
		}
	}

	/**
	 * 发送packet
	 *
	 * @param packet
	 * @throws IOException
	 */
	protected boolean sendIqPacketCommon(Packet packet){
		boolean sendSuccess = false;
		if (mConnection  != null) {
			if (NetWorkUtil.isNetworkAvailable(mContext)) {
				if (mConnection.isConnected()) {
					try {
						mConnection.sendPacket(packet);
						sendSuccess = true;
					} catch (Exception e) {
						String notConnectServer = "Not connected to server.";
						if (notConnectServer.equals(e.getMessage())) {
							ToastUtil.toastAlerMessage(mContext, "Not connected to server.",Toast.LENGTH_SHORT);
						}
					}
				} else {
					ToastUtil.toastAlerMessage(mContext, "正连接服务器，请稍候",Toast.LENGTH_SHORT);
				}
			} else {
				ToastUtil.toastAlerMessage(mContext, "网络异常，请检查网络",Toast.LENGTH_SHORT);
			}
		}
		return sendSuccess;
	}

	/**
	 * 广播接收器
	 *
	 * @author huyi
	 */
	protected class LoadContactReceiver extends BroadcastReceiver {
		@Override public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Const.BROADCAST_ACTION_LOADING_CONTACT)) {
				//				ShowDialog("");
				showProgressD("正在加载数据...", true);
			} else if (action.equals(Const.BROADCAST_ACTION_LOADED_CONTACT)) {
				hideProgessD();
			}
		}
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}
}
