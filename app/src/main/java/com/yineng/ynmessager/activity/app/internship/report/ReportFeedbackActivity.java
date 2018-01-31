package com.yineng.ynmessager.activity.app.internship.report;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Internship.DailyReport;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.bean.app.Internship.MonthlyReport;
import com.yineng.ynmessager.bean.app.Internship.WeeklyReport;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReportFeedbackActivity extends BaseActivity implements View.OnClickListener {
    public static final int REQUEST_CODE = 100;
    private ImageView mImg_previous;
    private TextView mTxt_title;
    private TextView mTxt_redo;
    private InternshipReport mInternshipReport;
    private LinearLayout mLin_subtitleArea;
    private TextView mTxt_statusShow;
    private TextView mTxt_isSubmitDelayed;
    private TextView mTxt_feedback;
    private TextView mTxt_content;
    private TextView mTxt_writer;
    private TextView mTxt_requiredDate;
    private TextView mTxt_DateOfReport;
    private TextView mTxt_fristSubmitDate;
    private TextView mTxt_lastSubmitDate;
    private TextView mTxt_delayTag;
    private ProgressDialog mProgressDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_feedback);

        initView();
        initData();
    }

    private void initView() {
        mImg_previous = (ImageView) findViewById(R.id.reportFeedback_img_previous);
        mTxt_title = (TextView) findViewById(R.id.reportFeedback_txt_title);
        mTxt_redo = (TextView) findViewById(R.id.reportFeedback_txt_redo);
        mLin_subtitleArea = (LinearLayout) findViewById(R.id.reportFeedback_lin_subtitleArea);
        mTxt_statusShow = (TextView) findViewById(R.id.reportFeedback_txt_statusShow);
        mTxt_isSubmitDelayed = (TextView) findViewById(R.id.reportFeedback_txt_isSubmitDelayed);
        mTxt_feedback = (TextView) findViewById(R.id.reportFeedback_txt_feedback);
        mTxt_content = (TextView) findViewById(R.id.reportFeedback_txt_content);
        mTxt_writer = (TextView) findViewById(R.id.reportFeedback_txt_writer);
        mTxt_requiredDate = (TextView) findViewById(R.id.reportFeedback_txt_requiredDate);
        mTxt_DateOfReport = (TextView) findViewById(R.id.reportFeedback_txt_DateOfReport);
        mTxt_fristSubmitDate = (TextView) findViewById(R.id.reportFeedback_txt_firstSubmitDate);
        mTxt_lastSubmitDate = (TextView) findViewById(R.id.reportFeedback_txt_lastSubmitDate);
        mTxt_delayTag = (TextView) findViewById(R.id.reportFeedback_txt_delayTag);

        mImg_previous.setOnClickListener(this);
        mTxt_redo.setOnClickListener(this);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mInternshipReport = getIntent().getParcelableExtra("Report");

        switch (mInternshipReport.getType()) {
            case InternshipReport.REPORT_TYPE_DAILY:
                Date dailyDate = ((DailyReport) mInternshipReport).getDate();

                mTxt_title.setText(getString(R.string.reportContent_dailyTitle,
                    Integer.valueOf(DateFormatUtils.format(dailyDate, "MM")),
                    Integer.valueOf(DateFormatUtils.format(dailyDate, "dd"))));
                break;
            case InternshipReport.REPORT_TYPE_WEEKLY:
                WeeklyReport weeklyReport = (WeeklyReport) mInternshipReport;

                mTxt_title.setText(getString(R.string.reportContent_weeklyTitle, weeklyReport.getSequence(),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "MM")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "dd")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeEnd(), "MM")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeEnd(), "dd"))));
                break;
            case InternshipReport.REPORT_TYPE_MONTHLY:
                Date monthlyDate = ((MonthlyReport) mInternshipReport).getDate();

                mTxt_title.setText(getString(R.string.reportContent_monthlyTitle,
                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "yyyy")),
                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "MM"))));
                break;
        }
    }

    private void initData() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {

            @Override protected void onPreExecute() {
                mProgressDialog.show();
            }

            @Override protected String doInBackground(Void... params) {
                return V8TokenManager.obtain();
            }

            @Override protected void onPostExecute(String result) {
                InternshipReport report = mInternshipReport;

                String url =
                    AppController.getInstance().CONFIG_YNEDUT_V8_URL + "third/internship/dgQueryReportByDetail.htm";
                Map<String,String> params = new HashMap<>();
                params.put("reportId", report.getId());
                params.put("reportType", report.getType()+"");
                params.put("version", "V1.0");
                params.put("access_token", result);
                OKHttpCustomUtils.get(url, params, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject response, int id) {
                        try {
                            if (response.getInt("status") != 0) {
                                return;
                            }
                            JSONObject resultObj = response.getJSONObject("result");
                            String content = resultObj.getString("content");
                            String writer = resultObj.getString("writer");
                            String requireTime = resultObj.getString("requireTime");
                            String firstSubmitTime = resultObj.getString("firstSubmitTime");
                            String lastSubmitTime = resultObj.getString("lastSubmitTime");
                            boolean isDelayed = resultObj.getBoolean("late");
                            int status = resultObj.getInt("status");
                            String feedback = resultObj.getString("auditOpinion");

                            mLin_subtitleArea.setVisibility(View.VISIBLE);
                            if (status == InternshipReport.STATUS_PASSED) {
                                mLin_subtitleArea.setBackgroundResource(R.color.limegreen);

                                mTxt_statusShow.setText(R.string.reportFeedback_accept);
                                mTxt_statusShow.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.accept, 0, 0, 0);
                            } else if (status == InternshipReport.STATUS_REDO) {
                                mTxt_redo.setVisibility(View.VISIBLE);

                                mLin_subtitleArea.setBackgroundResource(R.color.orange);

                                mTxt_statusShow.setText(R.string.reportFeedback_reject);
                                mTxt_statusShow.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.reject, 0, 0, 0);

                                if (StringUtils.isNotBlank(feedback)) {
                                    mTxt_feedback.setText(feedback);
                                    mTxt_feedback.setVisibility(View.VISIBLE);
                                }
                            }

                            mTxt_isSubmitDelayed.setText(
                                    isDelayed ? R.string.reportFeedback_delayedYes : R.string.reportFeedback_delayedNo);
                            mTxt_content.setText(content);
                            mTxt_writer.setText(getString(R.string.reportFeedback_writer, writer));
                            mTxt_requiredDate.setText(getString(R.string.reportFeedback_requiredDate, requireTime));
                            mTxt_fristSubmitDate.setText(
                                    getString(R.string.reportFeedback_firstSubmitDate, firstSubmitTime));
                            mTxt_lastSubmitDate.setText(getString(R.string.reportFeedback_lastSubmitDate,
                                    StringUtils.isBlank(lastSubmitTime) ? firstSubmitTime : lastSubmitTime));
                            mTxt_delayTag.setVisibility(isDelayed ? View.VISIBLE : View.GONE);

                            switch (mInternshipReport.getType()) {
                                case InternshipReport.REPORT_TYPE_DAILY:
                                    Date dailyDate = ((DailyReport) mInternshipReport).getDate();

                                    mTxt_DateOfReport.setText(getString(R.string.reportFeedback_dailyDateOfReport,
                                            getString(R.string.reportContent_dailyTitle,
                                                    Integer.valueOf(DateFormatUtils.format(dailyDate, "MM")),
                                                    Integer.valueOf(DateFormatUtils.format(dailyDate, "dd")))));
                                    break;
                                case InternshipReport.REPORT_TYPE_WEEKLY:
                                    WeeklyReport weeklyReport = (WeeklyReport) mInternshipReport;

                                    mTxt_DateOfReport.setText(getString(R.string.reportFeedback_weeklyDateOfReport,
                                            getString(R.string.reportContent_weeklyTitle, weeklyReport.getSequence(),
                                                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "MM")),
                                                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "dd")),
                                                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeEnd(), "MM")),
                                                    Integer.valueOf(
                                                            DateFormatUtils.format(weeklyReport.getRangeEnd(), "dd")))));
                                    break;
                                case InternshipReport.REPORT_TYPE_MONTHLY:
                                    Date monthlyDate = ((MonthlyReport) mInternshipReport).getDate();

                                    mTxt_DateOfReport.setText(getString(R.string.reportFeedback_monthlyDateOfReport,
                                            getString(R.string.reportContent_monthlyTitle,
                                                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "yyyy")),
                                                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "MM")))));
                                    break;
                            }
                        } catch (JSONException e) {
                            L.e(mTag, e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        mProgressDialog.dismiss();
                    }
                });
            }
        };

        AsyncTaskCompat.executeParallel(task);
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        mProgressDialog.dismiss();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            setResult(resultCode);
            finish();
        }
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reportFeedback_img_previous:
                finish();
                break;
            case R.id.reportFeedback_txt_redo:
                Intent intent = new Intent(this, ReportContentActivity.class);
                intent.putExtra("Report", mInternshipReport);
                intent.putExtra("Content", mTxt_content.getText().toString());
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }
}
