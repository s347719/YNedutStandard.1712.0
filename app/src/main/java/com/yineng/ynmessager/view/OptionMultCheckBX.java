//***************************************************************
//*    2015-9-25  上午10:15:07
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yineng.ynmessager.R;

/**
 * @author 胡毅
 *
 */
public class OptionMultCheckBX extends RelativeLayout {

	public CheckBox mCheckCB;
	private ImageView mCheckIV;

	public OptionMultCheckBX(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_mult_choice, this);
		initView();
	}

	public OptionMultCheckBX(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_mult_choice, this);
		initView();
	}

	public OptionMultCheckBX(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_mult_choice, this);
		initView();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mCheckCB = (CheckBox) findViewById(R.id.item_app_evaluate_option_mult_cb);
		mCheckIV = (ImageView) findViewById(R.id.item_app_evaluate_option_mult_iv);

		ViewTreeObserver vto = mCheckCB.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				int margeLeft = mCheckIV.getWidth()/2;
				if (mCheckCB.getLeft() == margeLeft) {
					return;
				}
				mCheckCB.setLeft(mCheckCB.getLeft()+margeLeft);
			}
		});
		mCheckCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCheckIV.setSelected(true);
				} else {
					mCheckIV.setSelected(false);
				}
				if (onOptionCheckListener != null) {
					onOptionCheckListener.onChecked(buttonView,isChecked);
				}
			}
		});
	}
	
	public interface optionCheckListener {
		void onChecked(CompoundButton buttonView, boolean isChecked);
	}
	
	public optionCheckListener onOptionCheckListener;

	public void setOnOptionCheckListener(optionCheckListener onOptionCheckListener) {
		this.onOptionCheckListener = onOptionCheckListener;
	}
	
	public void setChecked(boolean isCheck) {
		mCheckCB.setChecked(isCheck);
	}
}
