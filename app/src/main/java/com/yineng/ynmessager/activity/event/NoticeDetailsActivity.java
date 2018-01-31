package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.db.NoticeEventTb;
import com.yineng.ynmessager.util.TimeUtil;

public class NoticeDetailsActivity extends BaseActivity implements View.OnClickListener
{
    private NoticeEvent mCurrentNotice;
    private ImageView mImg_close;
    private TextView mTxt_title;
    private TextView mTxt_info;
    private TextView mTxt_content;
    private TextView mTxt_delete;
    private NoticeEventTb mNoticeEventTb;
    private AlertDialog mDelConfirmDlg;
    private LocalBroadcastManager mBroadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_details);

        mCurrentNotice = getIntent().getParcelableExtra("NoticeEvent");
        if (mCurrentNotice == null) {
            return;
        }

        initViews();
    }

    private void initViews()
    {
        Context context = getApplicationContext();
        mImg_close = (ImageView) findViewById(R.id.noticeDetails_txt_close);
        mTxt_title = (TextView) findViewById(R.id.noticeDetails_txt_content_title);
        mTxt_info = (TextView) findViewById(R.id.noticeDetails_txt_content_info);
        mTxt_content = (TextView) findViewById(R.id.noticeDetails_txt_content_text);
        mTxt_delete = (TextView) findViewById(R.id.noticeDetails_txt_delete);

        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        mNoticeEventTb = new NoticeEventTb(context);

        mImg_close.setOnClickListener(this);
        mTxt_title.setText(mCurrentNotice.getTitle());
        String time = TimeUtil.getTimeRelationFromNow2(context, mCurrentNotice.getTimeStamp());
        mTxt_info.setText(getString(R.string.noticeDetails_infoText, time, mCurrentNotice.getReceiver()));
        mTxt_content.setText(mCurrentNotice.getContent());
        mTxt_delete.setOnClickListener(this);

        mDelConfirmDlg = new AlertDialog.Builder(this)
                .setMessage(R.string.noticeDetails_deleteConfirm)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mNoticeEventTb.delete(mCurrentNotice);
                        mBroadcastManager.sendBroadcast(new Intent(NoticeEvent.ACTION_REFRESH));
                        mDelConfirmDlg.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mDelConfirmDlg.dismiss();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.noticeDetails_txt_delete:
                mDelConfirmDlg.show();
                break;
            case R.id.noticeDetails_txt_close:
                finish();
                break;
        }
    }

}
