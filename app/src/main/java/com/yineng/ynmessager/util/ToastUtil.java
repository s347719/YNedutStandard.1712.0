package com.yineng.ynmessager.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.contact.UserStatus;

public class ToastUtil {
	// private static final int NETWORK_SHOW_TIME = 2000;
	private static Toast mToast = null;

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessage(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageTop(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.TOP, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageCenter(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param stringId
	 *
	 * @param duration
	 *
	 */
	public static void toastAlerMessageCenter(Context context, int stringId,
											  int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(stringId);
			toast.setDuration(duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * toast的资源文件id 通过去系统查找文件的方法拿到布局id进行修改
	 */
	private static int textview_id;
	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageBottom(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			if (textview_id==0)
				textview_id = Resources.getSystem().getIdentifier("message","id","android");
			((TextView)toast.getView().findViewById(textview_id)).setGravity(Gravity.CENTER);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param inflater
	 * @param message
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageiconTop(Context context,
			LayoutInflater inflater, String message, int duration) {

		try {
			View layout = inflater.inflate(R.layout.view_toast, null);
			TextView text = (TextView) layout.findViewById(R.id.tv_toast_text);
			text.setText(message);
			Toast toast = getInstanceToast(context);

			toast.setGravity(Gravity.TOP, 0, 8);
			// toast.setMargin(0.0F, 0.83F);
			toast.setDuration(duration);
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param inflater
	 * @param message
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageiconCenter(Context context,
			LayoutInflater inflater, String message, int duration) {

		try {
			View layout = inflater.inflate(R.layout.view_toast, null);
			TextView text = (TextView) layout.findViewById(R.id.tv_toast_text);
			text.setText(message);
			Toast toast = getInstanceToast(context);
			//
			toast.setGravity(Gravity.CENTER, 0, 0);
			// toast.setMargin(0.0F, 0.83F);
			toast.setDuration(duration);
			//
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @return
	 */
	@SuppressLint("ShowToast")
	public static Toast getInstanceToast(Context context) {
		if (mToast == null) {
			mToast = Toast.makeText(context.getApplicationContext(), "warning",
					Toast.LENGTH_SHORT);
		} else {
			mToast.cancel();
			mToast = Toast.makeText(context.getApplicationContext(), "warning",
					Toast.LENGTH_SHORT);
		}
		return mToast;
	}
}
