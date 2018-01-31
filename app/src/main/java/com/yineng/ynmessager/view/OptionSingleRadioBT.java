//***************************************************************
//*    2015-9-24  下午4:52:54
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.yineng.ynmessager.R;

/**
 * @author 胡毅
 *
 */
public class OptionSingleRadioBT extends RelativeLayout {

	public RadioButton mSelectRB;
	public ImageView mSelectIV;

	public OptionSingleRadioBT(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_single_choice, this);
		initView();
	}

	public OptionSingleRadioBT(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_single_choice, this);
		initView();
	}

	public OptionSingleRadioBT(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.item_evaluate_single_choice, this);
		initView();
	}

	/**
	 * 初始化界面
	 */
	private void initView() {
		mSelectRB = (RadioButton) findViewById(R.id.item_app_evaluate_option_rb);
		mSelectIV = (ImageView) findViewById(R.id.item_app_evaluate_option_bt);

		ViewTreeObserver vto = mSelectRB.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				int margeLeft = mSelectIV.getWidth()/2;
				if (mSelectRB.getLeft() == margeLeft) {
					return;
				}
				mSelectRB.setLeft(mSelectRB.getLeft()+margeLeft);
			}
		});
//		mSelectRB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			
//			@Override
//			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//				if (isChecked) {
//					mSelectIV.setSelected(true);
//				} else {
//					mSelectIV.setSelected(false);
//				}
//			}
//		});
//		mSelectRB.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (mSelectRB.isChecked()) {
//					mSelectRB.setChecked(false);
//				} else {
//					mSelectRB.setChecked(true);
//				}
//			}
//		});
	}


}
