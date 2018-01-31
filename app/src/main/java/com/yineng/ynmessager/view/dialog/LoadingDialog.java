package com.yineng.ynmessager.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.yineng.ynmessager.R;


/**
 * 加载中Dialog
 * 
 */
public class LoadingDialog extends AlertDialog {


	public LoadingDialog(Context context) {
		super(context);
	}

	public LoadingDialog(Context context, String message, boolean cancleable) {
		super(context);
		this.setCancelable(cancleable);
	}

	public LoadingDialog(Context context, int theme, String message) {
		super(context, theme);
		this.setCancelable(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.view_tips_loading);

	}


}
