package com.yineng.ynmessager.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.yineng.ynmessager.R;


/**
 * Dialog
 * 
 */
public class LoadingDialogWithDialog extends AlertDialog {

	private TextView tips_loading_msg;

	private String mess ;

	public LoadingDialogWithDialog(Context context, String message) {
		super(context);
		this.mess = message;
	}

	public LoadingDialogWithDialog(Context context, String message, boolean cancleable) {
		super(context);
		this.mess = message;
	}

	public LoadingDialogWithDialog(Context context, int theme, String message) {
		super(context, theme);
		this.mess = message;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.view_tips_load_title);
		tips_loading_msg = (TextView) findViewById(R.id.tips_loading_msg);
		tips_loading_msg.setText(this.mess);
	}



}
