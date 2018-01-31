//***************************************************************
//*    2015-10-16  上午9:49:35
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.yineng.ynmessager.R;

/**
 * @author 胡毅
 *
 */
public class EvaluateSubmitScoreDialog extends AlertDialog {

	public ImageView mCloseDialogIV;
	public TextView mMyScoreTV;
	public TextView mQuesTotalScoreTV;
	public TextView mAnsweredCountTV;
	public TextView mQuesTotalCountTV;
	public Button mSubmitScore;
	
	private float mMyAnswerScore = 0;
	
	private int mTotalScore = 0;
	
	private int mMyAnswerCount = 0;
	
	private int mTotalQuesCount = 0;
    private OnSubmitScoreListener onSubmitScoreListener;

	public EvaluateSubmitScoreDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public EvaluateSubmitScoreDialog(Context context, int themeResId) {
		super(context, themeResId);
	}

	public EvaluateSubmitScoreDialog(Context context) {
//		super(context);
		this(context, R.style.mydialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_evaluate_submit_score_dialog);
		findViews();
		initListener();
	}

    /**
     *
     */
	private void findViews() {
		mCloseDialogIV = (ImageView) findViewById(R.id.app_eva_submit_close_dialog_iv);
		mMyScoreTV = (TextView) findViewById(R.id.app_eva_my_score);
		mQuesTotalScoreTV = (TextView) findViewById(R.id.app_eva_question_sum_score);
		mAnsweredCountTV = (TextView) findViewById(R.id.app_eva_answered_count);
		mQuesTotalCountTV = (TextView) findViewById(R.id.app_eva_question_sum);
		mSubmitScore = (Button) findViewById(R.id.app_eva_dialog_submit_score);
	}
	
	/**
	 * 初始化监听器
	 */
	private void initListener() {
		mCloseDialogIV.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mSubmitScore.setOnClickListener(new View.OnClickListener() {

            @Override
			public void onClick(View v) {
				if (onSubmitScoreListener != null) {
					onSubmitScoreListener.onSubmitScore(v);
				}
			}
		});
	}
	
	/**
	 * 设置对话框要显示的数据
	 * @param tempMyAnswerScore
	 * @param tempMyAnswerCount
	 * @param tempTotalQuesCount
	 */
	public void setDialogData(float tempMyAnswerScore,int tempMyAnswerCount,int tempTotalQuesCount) {
		mMyAnswerScore = tempMyAnswerScore;
		mMyAnswerCount = tempMyAnswerCount;
		mTotalQuesCount = tempTotalQuesCount;
	}
	
	@Override
	public void show() {
		super.show();
		mMyScoreTV.setText(mMyAnswerScore+"");
//		mQuesTotalScoreTV.setText("100分");
		mAnsweredCountTV.setText(mMyAnswerCount+"");
		mQuesTotalCountTV.setText(mTotalQuesCount+"题");
	}
	
	public void setOnSubmitScoreListener(OnSubmitScoreListener onSubmitScoreListener) {
		this.onSubmitScoreListener = onSubmitScoreListener;
    }

    public interface OnSubmitScoreListener {
        void onSubmitScore(View v);
    }
	
}
