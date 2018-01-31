//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.service.LocateService;

/**
 * 我的设置 界面
 *
 * @author 贺毅柳
 * @category Activity
 */
public class SettingActivity extends BaseActivity implements OnClickListener
{
    private TextView mImgb_previous; // 左上角返回按钮
    private RelativeLayout mRel_msgNotifySetting; // 打开[消息提醒]设置界面的按钮
    private RelativeLayout mRel_viewSetting; // 打开[浏览设置]界面的按钮
    private RelativeLayout mRel_additionalFunction; // 打开[辅助功能]设置界面的按钮
    private RelativeLayout mRel_about; // 打开[关于]的按钮
    private TextView mTxt_logout; // [退出登陆]按钮
    private AlertDialog mLogoutConfirmDialog; // 退出登陆确认对话框

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initViews();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        mLogoutConfirmDialog.dismiss();
    }

    /**
     * 初始化界面控件
     */
    private void initViews()
    {
        mImgb_previous = (TextView) findViewById(R.id.setting_txt_previous);
        mImgb_previous.setOnClickListener(this);
        mRel_msgNotifySetting = (RelativeLayout) findViewById(R.id.setting_rel_msgNotifySetting);
        mRel_msgNotifySetting.setOnClickListener(this);
        mRel_viewSetting = (RelativeLayout) findViewById(R.id.setting_rel_viewSetting);
        mRel_viewSetting.setOnClickListener(this);
        mRel_additionalFunction = (RelativeLayout) findViewById(R.id.setting_rel_additionalFunction);
        mRel_additionalFunction.setOnClickListener(this);
        mRel_about = (RelativeLayout) findViewById(R.id.setting_rel_about);
        mRel_about.setOnClickListener(this);
        mTxt_logout = (TextView) findViewById(R.id.setting_txt_logout);
        mTxt_logout.setOnClickListener(this);

        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle(R.string.setting_logout);
        builder.setPositiveButton(R.string.setting_logoutConfirmed, new Dialog.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                stopService(new Intent(SettingActivity.this, LocateService.class));//退出登录后停止定位服务
                logout();
            }
        });
        builder.setNegativeButton(R.string.setting_cancel, null);
        mLogoutConfirmDialog = builder.create();
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        switch (id)
        {
            case R.id.setting_txt_previous:
                finish();
                break;
            case R.id.setting_rel_msgNotifySetting:
                startActivity(new Intent(this, MsgNotifySettingActivity.class));
                break;
            case R.id.setting_rel_viewSetting:
                startActivity(new Intent(this, ViewSettingActivity.class));
                break;
            case R.id.setting_rel_additionalFunction:
                startActivity(new Intent(this, AdditionalFunctionActivity.class));
                break;
            case R.id.setting_rel_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.setting_txt_logout:
                mLogoutConfirmDialog.setMessage(getString(R.string.setting_logoutConfirm));
                mLogoutConfirmDialog.show();
                break;
        }
    }

    /**
     * 注销帐号
     */
    private void logout()
    {
        AppController.getInstance().mLoginUser = null;
        AppController.getInstance().mSelfUser = null;
        super.logout(false);
    }

}
