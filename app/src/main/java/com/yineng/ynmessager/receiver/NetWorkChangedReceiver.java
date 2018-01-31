package com.yineng.ynmessager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;

/**
 * @author YuTang
 */
public class NetWorkChangedReceiver extends BroadcastReceiver {
	public final String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"; //
	private final String TAG = "NetWorkChangedReceiver";
	private static boolean isServiceUp = false;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		// 网络变更
		if (TextUtils.equals(action, ConnectivityManager.CONNECTIVITY_ACTION)) {
			L.v(TAG,
					"NetWorkUtil.isNetworkAvailable :"
							+ NetWorkUtil.isNetworkAvailable(context)+" "+isServiceUp);
			//网络可用且有登录帐号，且XmppConnect是连接及认证状态
			if (NetWorkUtil.isNetworkAvailable(context)) {// 如果网络可用自动登陆
////				isServiceUp = true;
//				Intent serviceIntent = new Intent(context,
//						XmppConnService.class);
//				context.startService(serviceIntent);// 开启服务，自动登陆
			} else {
//				isServiceUp = false;
			}
		}
	}
}
