package com.yineng.ynmessager.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.view.KeyEvent;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.view.dialog.LoadingDialog;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

//跟App相关的辅助类
public class AppUtils
{


	private static LoadingDialog mProgressDialog;

	private AppUtils()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 计算百分比
	 * @param num
	 * @param total
	 * @param scale
	 * @return
	 */
	public static String accuracy(double num, double total, int scale){
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		//可以设置精确几位小数
		df.setMaximumFractionDigits(scale);
		//模式 例如四舍五入
		df.setRoundingMode(RoundingMode.HALF_UP);
		double accuracy_num = num / total * 100;
		return df.format(accuracy_num)+"%";
	}


	/**
	 * 判断手机是否处于普通模式
	 * @return
	 */
	public static boolean isRiginNormalMode(){
		AudioManager audioManager = (AudioManager) AppController.getInstance().getSystemService(Context.AUDIO_SERVICE);
		int mode = audioManager.getRingerMode();
		boolean isModeNormal = false;
		switch (mode) {
			case AudioManager.RINGER_MODE_NORMAL:
				//普通模式
				isModeNormal = true;
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				//振动模式
				isModeNormal =  false;
				break;
			case AudioManager.RINGER_MODE_SILENT:
				//静音模式
				isModeNormal =  false;
				break;
		}
		return isModeNormal;
	}

	public static void  showProgressDialog(Context mcontext,String title)
	{
		if(mProgressDialog !=null)
		{
			mProgressDialog = null;
		}
		mProgressDialog = new LoadingDialog(mcontext, R.style.MyDialog,title);
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(true);
		mProgressDialog.setOnKeyListener(new android.content.DialogInterface.OnKeyListener()
		{
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
			{
				switch (keyCode)
				{
					case KeyEvent.KEYCODE_BACK:
						return true;
				}
				return false;
			}
		});
		mProgressDialog.show();
	}


	public static void  hideProgressDialog()
	{
		if(mProgressDialog!=null) {
            mProgressDialog.dismiss();
        }
		mProgressDialog=null;
	}
	/**
	 * 获取应用程序名称
	 */
	public static String getAppName(Context context)
	{
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			int labelRes = packageInfo.applicationInfo.labelRes;
			return context.getResources().getString(labelRes);
		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * [获取应用程序版本名称信息]
	 *
	 * @param context
	 * @return 当前应用的版本名称
	 */
	public static String getVersionName(Context context)
	{
		try
		{
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packageInfo = packageManager.getPackageInfo(
					context.getPackageName(), 0);
			return packageInfo.versionName;

		} catch (NameNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 判断某个服务是否正在运行的方法
	 *
	 * @param mContext
	 * @param serviceName
	 *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
	 * @return true代表正在运行，false代表服务没有正在运行
	 */
	public static boolean isServiceWork(Context mContext, String serviceName) {
		boolean isWork = false;
		ActivityManager myAM = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> myList = myAM.getRunningServices(100);
		if (myList.size() <= 0) {
			return false;
		}
		for (int i = 0; i < myList.size(); i++) {
			String mName = myList.get(i).service.getClassName().toString();
			if (mName.equals(serviceName)) {
				isWork = true;
				break;
			}
		}
		return isWork;
	}

	/**
	 * 如果服务还未启动，则根据类名启动对应的服务
	 * @param mContext 上下文
	 * @param cls 服务类名
	 */
	public static void startService(Context mContext,Class<?> cls) {
		L.e("isServiceWork == "+isServiceWork(mContext,cls.getName()));
		if (!isServiceWork(mContext,cls.getName())) {
			// 如果网络可用自动登陆
			Intent serviceIntent = new Intent(mContext,cls);
			mContext.startService(serviceIntent);// 开启服务，自动登陆
		}
	}
}
