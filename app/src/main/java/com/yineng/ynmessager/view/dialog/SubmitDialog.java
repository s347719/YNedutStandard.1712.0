//***************************************************************
//*    2015-10-19  上午10:36:03
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yineng.ynmessager.R;

/**
 * @author 胡毅
 * 
 */
public class SubmitDialog extends AlertDialog {

	private ProgressBar mSubmitLoadingPB;
	private ImageView mSubmitSuccessIV;
	private TextView mSubmitStatusTV;
	private Context mContext;

	public SubmitDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public SubmitDialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	public SubmitDialog(Context context) {
		this(context, R.style.mydialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_submit_result_layout);
		findViews();
	}

	/**
	 * 
	 */
	private void findViews() {
		mSubmitLoadingPB = (ProgressBar) findViewById(R.id.pb_submit_result_progressbar);
		mSubmitSuccessIV = (ImageView) findViewById(R.id.iv_submit_result_success);
		mSubmitStatusTV = (TextView) findViewById(R.id.tv_submit_result_status);
	}
	
	public void showSubmitSuccessView() {
		mSubmitLoadingPB.setVisibility(View.GONE);
		mSubmitSuccessIV.setVisibility(View.VISIBLE);
		mSubmitStatusTV.setText(mContext.getString(R.string.submit_resut_success));
	}
	
	@Override
	public void show() {
		super.show();
		mSubmitLoadingPB.setVisibility(View.VISIBLE);
		mSubmitSuccessIV.setVisibility(View.GONE);
		mSubmitStatusTV.setText(mContext.getString(R.string.submit_resut_ing));
	}

}
