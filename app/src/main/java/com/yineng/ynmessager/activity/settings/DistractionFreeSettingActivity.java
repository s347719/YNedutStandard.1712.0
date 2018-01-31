//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.TimeUtil;

/**
 * 免打扰设置界面
 * 
 * @category Activity
 * @author 贺毅柳
 * 
 */
public class DistractionFreeSettingActivity extends BaseActivity implements OnClickListener
{

	private TextView mTxt_previous; // 左上角[返回]按钮
	private RelativeLayout mRel_beginTime; // [开始时间]设置按钮
	private TextView mTxt_beginTimeDisplay; // 用于显示已设置的开始时间的TextView
	private RelativeLayout mRel_endTime; // [结束时间]设置按钮
	private TextView mTxt_endTimeDisplay; // 用于显示已设置的开始时间的TextView
	private TimePickerDialog mBeginTimeDialog; // 设置开始时间的TimePickerDialog对话框
	private TimePickerDialog mEndTimeDialog; // 设置结束时间的TimePickerDialog对话框

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_distraction_free_setting);

		initViews();
	}

	/**
	 * 初始化界面组件
	 */
	private void initViews()
	{
		mTxt_previous = (TextView)findViewById(R.id.distractionFreeSetting_imgb_previous);
		mTxt_previous.setOnClickListener(this);
		mRel_beginTime = (RelativeLayout)findViewById(R.id.distractionFreeSetting_rel_beginTime);
		mRel_beginTime.setOnClickListener(this);
		mTxt_beginTimeDisplay = (TextView)findViewById(R.id.distractionFreeSetting_txt_beginTimeDisplay);
		mRel_endTime = (RelativeLayout)findViewById(R.id.distractionFreeSetting_rel_endTime);
		mRel_endTime.setOnClickListener(this);
		mTxt_endTimeDisplay = (TextView)findViewById(R.id.distractionFreeSetting_txt_endTimeDisplay);

		Setting setting = AppController.getInstance().mUserSetting;

		// 初始化TimePickerDialog
		mBeginTimeDialog = TimePickerDialog.newInstance(new BeginTimeDialog_OnTimeSetListener(),
				setting.getDistractionFree_begin_h(),setting.getDistractionFree_begin_m(),true);
		// 初始化TimePickerDialog
		mEndTimeDialog = TimePickerDialog.newInstance(new EndTimeDialog_OnTimeSetListener(),
				setting.getDistractionFree_end_h(),setting.getDistractionFree_end_m(),true);

		// 初始化UI显示
		String[] begin = TimeUtil.betterTimeDisplay(setting.getDistractionFree_begin_h(),
				setting.getDistractionFree_begin_m());
		String[] end = TimeUtil
				.betterTimeDisplay(setting.getDistractionFree_end_h(),setting.getDistractionFree_end_m());
		mTxt_beginTimeDisplay.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay,begin[0],begin[1]));
		mTxt_endTimeDisplay.setText(getString(R.string.distractionFreeSetting_endTimeDisplay,end[0],end[1]));
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.distractionFreeSetting_imgb_previous:
				finish();
				break;
			case R.id.distractionFreeSetting_rel_beginTime:
				mBeginTimeDialog.show(getFragmentManager(),"tag");
				break;
			case R.id.distractionFreeSetting_rel_endTime:
				mEndTimeDialog.show(getFragmentManager(),"tag");
				break;
		}

	}

	/**
	 * 设置开始时间的TimePickerDialog对话框的监听器
	 * 
	 * @category Listener
	 * @author 贺毅柳
	 * 
	 */
	class BeginTimeDialog_OnTimeSetListener implements OnTimeSetListener
	{

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int sec)
		{
			// 修改Application中的Setting
			Setting setting = AppController.getInstance().mUserSetting;
			setting.setDistractionFree_begin_h(hourOfDay);
			setting.setDistractionFree_begin_m(minute);
			// 更新到数据库
			SettingsTbDao dao = new SettingsTb(getApplicationContext());
			dao.update(setting);
			// 从数据库更新Application中的Setting
			AppController.getInstance().mUserSetting = dao.obtainSettingFromDb();
			// 更新UI
			String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay,minute);
			mTxt_beginTimeDisplay.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay,betterTime[0],
					betterTime[1]));
		}
	}

	/**
	 * 设置结束时间的TimePickerDialog对话框的监听器
	 * 
	 * @category Listener
	 * @author 贺毅柳
	 * 
	 */
	class EndTimeDialog_OnTimeSetListener implements OnTimeSetListener
	{

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int sec)
		{
			// 修改Application中的Setting
			Setting setting = AppController.getInstance().mUserSetting;
			setting.setDistractionFree_end_h(hourOfDay);
			setting.setDistractionFree_end_m(minute);
			// 更新到数据库
			SettingsTbDao dao = new SettingsTb(getApplicationContext());
			dao.update(setting);
			// 从数据库更新Application中的Setting
			AppController.getInstance().mUserSetting = dao.obtainSettingFromDb();
			// 更新UI
			String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay,minute);
			mTxt_endTimeDisplay.setText(getString(R.string.distractionFreeSetting_endTimeDisplay,betterTime[0],
					betterTime[1]));
		}

	}

}
