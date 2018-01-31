package com.yineng.ynmessager.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.TimeUtil;

/**
 * Created by yn on 2017/6/12.
 */

public class PerferSettingActivity extends BaseActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    private CheckBox perfer_load_img, perfer_voice, perfer_shock, perfer_not_disturb;
    private LinearLayout perfer_not_disturb_time;
    private TextView perfer_txt_timeSetting, perfer_rel_beginTime, perfer_rel_endTime,about_txt_previous;
    private Setting setting = mApplication.mUserSetting;
    private TimePickerDialog mBeginTimeDialog; // 设置开始时间的TimePickerDialog对话框
    private TimePickerDialog mEndTimeDialog; // 设置结束时间的TimePickerDialog对话框
    private SettingsTbDao mDao; // Setting表DAO实例

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfer_setting);

        initviews();
    }

    private void initviews() {
        mDao = new SettingsTb(this);
        perfer_not_disturb_time = (LinearLayout) findViewById(R.id.perfer_not_disturb_time);
        perfer_txt_timeSetting = (TextView) findViewById(R.id.perfer_txt_timeSetting);
        perfer_rel_beginTime = (TextView) findViewById(R.id.perfer_rel_beginTime);
        perfer_rel_endTime = (TextView) findViewById(R.id.perfer_rel_endTime);
        about_txt_previous= (TextView) findViewById(R.id.about_txt_previous);
        about_txt_previous.setOnClickListener(this);
        perfer_rel_beginTime.setOnClickListener(this);
        perfer_rel_endTime.setOnClickListener(this);

        perfer_load_img = (CheckBox) findViewById(R.id.perfer_load_img);
        perfer_voice = (CheckBox) findViewById(R.id.perfer_voice);
        perfer_shock = (CheckBox) findViewById(R.id.perfer_shock);
        perfer_not_disturb = (CheckBox) findViewById(R.id.perfer_not_disturb);
        perfer_load_img.setOnCheckedChangeListener(this);
        perfer_voice.setOnCheckedChangeListener(this);
        perfer_shock.setOnCheckedChangeListener(this);
        perfer_not_disturb.setOnCheckedChangeListener(this);

        // 设置勾选
        perfer_load_img.setChecked(setting.getAlwaysAutoReceiveImg() != 0);
        perfer_voice.setChecked(setting.getAudio() != 0);
        perfer_voice.setChecked(setting.getAudio_group() != 0);
        perfer_shock.setChecked(setting.getVibrate() != 0);
        perfer_shock.setChecked(setting.getVibrate_group() != 0);

        // 用户是否已经开启免打扰
        boolean isDistractionFree = setting.getDistractionFree() != 0;
        perfer_not_disturb.setChecked(isDistractionFree);
        perfer_txt_timeSetting.setClickable(isDistractionFree);
        perfer_txt_timeSetting.setEnabled(isDistractionFree);
        isShowTime(isDistractionFree);

        // 初始化TimePickerDialog
        mBeginTimeDialog = TimePickerDialog.newInstance(new BeginTimeDialog_OnTimeSetListener(),
                setting.getDistractionFree_begin_h(), setting.getDistractionFree_begin_m(), true);
        // 初始化TimePickerDialog
        mEndTimeDialog = TimePickerDialog.newInstance(new EndTimeDialog_OnTimeSetListener(),
                setting.getDistractionFree_end_h(), setting.getDistractionFree_end_m(), true);

        // 初始化UI显示
        String[] begin = TimeUtil.betterTimeDisplay(setting.getDistractionFree_begin_h(),setting.getDistractionFree_begin_m());
        String[] end = TimeUtil.betterTimeDisplay(setting.getDistractionFree_end_h(), setting.getDistractionFree_end_m());
        perfer_rel_beginTime.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay, begin[0], begin[1]));
        perfer_rel_endTime.setText(getString(R.string.distractionFreeSetting_endTimeDisplay, end[0], end[1]));
    }

    private void isShowTime(boolean flag) {
        if (flag) {
            perfer_not_disturb_time.setVisibility(View.VISIBLE);
        } else {
            perfer_not_disturb_time.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.perfer_rel_beginTime://免打扰开始时间
                mBeginTimeDialog.show(getFragmentManager(),"tag");
                break;
            case R.id.perfer_rel_endTime://免打扰结束时间
                mEndTimeDialog.show(getFragmentManager(),"tag");
                break;
            case R.id.about_txt_previous:
                finish();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.perfer_load_img://非WiFi状态下加载图片
                setting.setAlwaysAutoReceiveImg(isChecked ? 1 : 0);
                break;
            case R.id.perfer_voice://声音提醒
                setting.setAudio(isChecked ? 1 : 0);
                setting.setAudio_group(isChecked ? 1 : 0);
                break;
            case R.id.perfer_shock://震动提醒
                setting.setVibrate(isChecked ? 1 : 0);
                setting.setVibrate_group(isChecked ? 1 : 0);
                break;
            case R.id.perfer_not_disturb://免打扰
                setting.setDistractionFree(isChecked ? 1 : 0);
                perfer_txt_timeSetting.setClickable(isChecked);
                perfer_txt_timeSetting.setEnabled(isChecked);
                isShowTime(isChecked);
                break;
        }
        mDao.update(setting);
        mApplication.mUserSetting = mDao.obtainSettingFromDb();
    }

    /**
     * 设置开始时间的TimePickerDialog对话框的监听器
     * @author 贺毅柳
     * @category Listener
     */
    class BeginTimeDialog_OnTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int sec) {
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
            String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay, minute);
            perfer_rel_beginTime.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay, betterTime[0],
                    betterTime[1]));
        }
    }

    /**
     * 设置结束时间的TimePickerDialog对话框的监听器
     * @author 贺毅柳
     * @category Listener
     */
    class EndTimeDialog_OnTimeSetListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int sec) {
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
            String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay, minute);
            perfer_rel_endTime.setText(getString(R.string.distractionFreeSetting_endTimeDisplay, betterTime[0],
                    betterTime[1]));
        }
    }
}
