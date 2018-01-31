package com.yineng.ynmessager.activity.app.internship;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.app.Internship.InternshipAct;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.zhy.http.okhttp.OkHttpUtils;

public class ReportCalendarActivity extends BaseActivity {
    ProgressDialog mProgressDialog;
    private TextView mTxt_title;
    private TextView mTxt_subtitle;
    private ImageView mImg_previous;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_calendar);

        initViews();
    }

    private void initViews() {
        Intent incoming = getIntent();
        InternshipAct internshipAct = incoming.getParcelableExtra("InternShipAct");
        int which = incoming.getIntExtra("Which", InternshipReport.REPORT_TYPE_DEFAULT);

        mTxt_title = (TextView) findViewById(R.id.reportCalendar_txt_title);
        mTxt_subtitle = (TextView) findViewById(R.id.reportCalendar_txt_subtitle);
        mImg_previous = (ImageView) findViewById(R.id.reportCalendar_img_previous);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mTxt_subtitle.setText(internshipAct.getTitle());
        mImg_previous.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                finish();
            }
        });

        Fragment initFragment = null;
        //初始化要加载的Fragment
        if (InternshipReport.REPORT_TYPE_DAILY == which) {
            initFragment = new DailyFragment();
            mTxt_title.setText(R.string.reportCalendar_dailyReport);
        } else if (InternshipReport.REPORT_TYPE_WEEKLY == which) {
            initFragment = new WeeklyFragment();
            mTxt_title.setText(R.string.reportCalendar_weeklyReport);
        } else if (InternshipReport.REPORT_TYPE_MONTHLY == which) {
            initFragment = new MonthlyFragment();
            mTxt_title.setText(R.string.reportCalendar_monthlyReport);
        }

        if (initFragment != null) {
            //给要加载的Fragment设置传进来的参数
            Bundle args = new Bundle();
            args.putParcelable("InternShipAct", internshipAct);
            initFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                .add(R.id.reportCalendar_frm_calendarContent, initFragment)
                .commit();
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag(this);
        mProgressDialog.dismiss();
    }
}
