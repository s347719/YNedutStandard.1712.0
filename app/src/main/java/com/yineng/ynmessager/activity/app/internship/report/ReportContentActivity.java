package com.yineng.ynmessager.activity.app.internship.report;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Internship.DailyReport;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.bean.app.Internship.MonthlyReport;
import com.yineng.ynmessager.bean.app.Internship.WeeklyReport;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

public class ReportContentActivity extends BaseActivity
    implements View.OnClickListener, DialogInterface.OnClickListener {
    private InternshipReport mInternshipReport;
    private TextView mTxt_title;
    private ImageButton mImgb_previous;
    private ImageButton mImgb_submit;
    private EditText mEdt_content;
    private TextView mTxt_textCount;
    private ImageButton mImgb_requirementText;
    private ProgressDialog mProgressDialog;
    private AlertDialog mSaveAsDraftDialog;
    private AlertDialog mSubmitConfirmDialog;
    private String mDefaultContentText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_content);

        initViews();
    }

    private void initViews() {
        mTxt_title = (TextView) findViewById(R.id.reportContent_txt_title);
        mImgb_previous = (ImageButton) findViewById(R.id.reportContent_imgb_previous);
        mImgb_submit = (ImageButton) findViewById(R.id.reportContent_imgb_submit);
        mEdt_content = (EditText) findViewById(R.id.reportContent_edt_content);
        mTxt_textCount = (TextView) findViewById(R.id.reportContent_txt_textCount);
        mImgb_requirementText = (ImageButton) findViewById(R.id.reportContent_imgb_requirementText);

        Context context = getApplicationContext();


        mSaveAsDraftDialog = new AlertDialog.Builder(this).setTitle(R.string.reportContent_confirmExit)
            .setMessage(R.string.reportContent_confirmSaveAsDraft)
            .setPositiveButton(R.string.reportContent_SaveAsDraft, this)
            .setNegativeButton(R.string.reportContent_giveUp, this)
            .setNeutralButton(R.string.reportContent_cancel, this)
            .create();

        mSubmitConfirmDialog = new AlertDialog.Builder(this).setTitle(R.string.reportContent_confirmExit)
            .setMessage(R.string.reportContent_confirmSubmit)
            .setPositiveButton(R.string.reportContent_submitAndBack, this)
            .setNegativeButton(R.string.reportContent_giveUp, this)
            .setNeutralButton(R.string.reportContent_cancel, this)
            .create();

        mImgb_previous.setOnClickListener(this);
        mImgb_submit.setOnClickListener(this);
        mImgb_requirementText.setOnClickListener(this);

        mEdt_content.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override public void afterTextChanged(Editable s) {
                mTxt_textCount.setText(getString(R.string.reportContent_textCount, s.length()));
            }
        });
        Intent startedIntent = getIntent();
        mInternshipReport = startedIntent.getParcelableExtra("Report");
        mDefaultContentText = startedIntent.getStringExtra("Content");

        if (StringUtils.isNotBlank(mDefaultContentText)) {
            mEdt_content.setText(mDefaultContentText);
        }

        switch (mInternshipReport.getType()) {
            case InternshipReport.REPORT_TYPE_DAILY:
                Date dailyDate = ((DailyReport) mInternshipReport).getDate();

                mTxt_title.setText(getString(R.string.reportContent_dailyTitle,
                    Integer.valueOf(DateFormatUtils.format(dailyDate, "MM")),
                    Integer.valueOf(DateFormatUtils.format(dailyDate, "d"))));
                break;
            case InternshipReport.REPORT_TYPE_WEEKLY:
                WeeklyReport weeklyReport = (WeeklyReport) mInternshipReport;

                mTxt_title.setText(getString(R.string.reportContent_weeklyTitle, weeklyReport.getSequence(),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "MM")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeBegin(), "d")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeEnd(), "MM")),
                    Integer.valueOf(DateFormatUtils.format(weeklyReport.getRangeEnd(), "d"))));
                break;
            case InternshipReport.REPORT_TYPE_MONTHLY:
                Date monthlyDate = ((MonthlyReport) mInternshipReport).getDate();

                mTxt_title.setText(getString(R.string.reportContent_monthlyTitle,
                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "yyyy")),
                    Integer.valueOf(DateFormatUtils.format(monthlyDate, "MM"))));
                break;
        }

        String draft = loadDraft(context);
        if (StringUtils.isNotBlank(draft)) {
            mEdt_content.setText(draft);
        }
    }

    /**
     * 加载保存的草稿
     *
     * @return 如果未找到草稿返回null
     */
    private String loadDraft(Context context) {
        String str = LastLoginUserSP.getInstance(context)
            .getSharedPreferences()
            .getString(InternshipReport.LOCAL_DRAFT_KEY_PREFIX + mInternshipReport.getId(), null);

        L.d(mTag, "loading draft:" + str);
        return str;
    }

    @Override public void onClick(View v) {
        String contentText = mEdt_content.getText().toString();

        int id = v.getId();
        switch (id) {
            case R.id.reportContent_imgb_submit:
                if (StringUtils.isBlank(contentText)) {
                    Toast.makeText(this, R.string.reportContent_contentHint, Toast.LENGTH_SHORT).show();
                } else {
                    submitContent(contentText);
                }
                break;
            case R.id.reportContent_imgb_previous:
                if (shouldExit(contentText)) {
                    finish();
                }
                break;
            case R.id.reportContent_imgb_requirementText:
                String text = mInternshipReport.getRequire();
                if (StringUtils.isEmpty(text)) {
                    Toast.makeText(this, R.string.reportContent_noRequirementText, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, text, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override public void onBackPressed() {
        if (shouldExit(mEdt_content.getText().toString())) {
            super.onBackPressed();
        }
    }

    private boolean shouldExit(String content) {
        int status = mInternshipReport.getStatus();
        switch (status) {
            case InternshipReport.STATUS_REDO:
                if (!StringUtils.equals(mDefaultContentText, content)) {
                    mSubmitConfirmDialog.show();
                    return false;
                }
                break;
            default:
                String draft = loadDraft(getApplicationContext());

                if (!StringUtils.isEmpty(content) && !StringUtils.equals(draft, content)) {
                    mSaveAsDraftDialog.show();
                    return false;
                }
        }

        return true;
/*        if (StringUtils.isBlank(content)) {
            if (ReportCalendarBaseFragment.hasLocalDraft(getApplicationContext(), mInternshipReport)) {
                mSaveAsDraftDialog.show();
            } else {
                setResult(RESULT_CANCELED);
                return true;
            }
        } else {
            if (!StringUtils.equals(loadDraft(getApplicationContext()), content)){
                mSaveAsDraftDialog.show();
                return true;
            }
        }

        return false;*/
    }

    @Override protected void onDestroy() {
        super.onDestroy();

        mSaveAsDraftDialog.dismiss();
    }

    private void submitContent(String content) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<String, Integer, String> submitTask = new AsyncTask<String, Integer, String>() {
            boolean result;
            String token;

            @Override protected void onPreExecute() {
                showProgressDialog("");
            }

            @Override protected String doInBackground(String... params) {
                token = V8TokenManager.obtain();
                if (StringUtils.isEmpty(token)) {
                    return null;
                }
                return params[0];
            }
            @Override protected void onPostExecute(String reportContext) {

                String url = String.format(AppController.getInstance().CONFIG_YNEDUT_V8_URL
                        + "third/internship/dgSubmitReportByUser.htm?access_token=%s&version=V1.0", token);
                 Map<String,String> urlParams = new HashMap<>();
                urlParams.put("reportContext", reportContext);
                OKHttpCustomUtils.get(url, urlParams, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject response, int id) {

                        int status = -1;
                        try {
                            status = response.getInt("status");
                        } catch (JSONException e) {
                            L.e(mTag, e.getMessage(), e);
                        }

                        result = status == 0;
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);

                        result = false;
                    }
                    @Override
                    public void onAfter(int id) {
                        hideProgressDialog();
                        if (result) {
                            Toast.makeText(ReportContentActivity.this, R.string.reportContent_submitSucceed, Toast.LENGTH_SHORT)
                                    .show();

                            //移除之前的草稿
                            Context context = getApplicationContext();
                            SharedPreferences.Editor editor = LastLoginUserSP.getInstance(context).getEditor();
                            editor.remove(InternshipReport.LOCAL_DRAFT_KEY_PREFIX + mInternshipReport.getId()).apply();

                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(ReportContentActivity.this, R.string.reportContent_submitFailed, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });

            }
        };

        JSONObject jsonContent = new JSONObject();
        try {
            jsonContent.put("id", mInternshipReport.getId());
            jsonContent.put("type", mInternshipReport.getType());
            jsonContent.put("content", content);
            jsonContent.put("status", 2);
            jsonContent.put("files", new JSONArray());
        } catch (JSONException e) {
            L.e(mTag, e.getMessage(), e);
        }

        AsyncTaskCompat.executeParallel(submitTask, jsonContent.toString());
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        Context context = getApplicationContext();
        SharedPreferences.Editor editor = LastLoginUserSP.getInstance(context).getEditor();

        if (dialog == mSaveAsDraftDialog) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:   //保存
                    String content = mEdt_content.getText().toString();
                    String key = InternshipReport.LOCAL_DRAFT_KEY_PREFIX + mInternshipReport.getId();

                    if (StringUtils.isEmpty(content)) {
                        editor.remove(key).apply();
                    } else {
                        editor.putString(key, content).apply();
                        L.i(mTag, "draft saved!");
                    }

                    setResult(RESULT_OK);
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:   //放弃
                    finish();
                    break;
                case AlertDialog.BUTTON_NEUTRAL:    //取消
                    dialog.dismiss();
                    break;
            }
        } else if (dialog == mSubmitConfirmDialog) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:   //提交
                    mImgb_submit.performClick();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:   //放弃
                    finish();
                    break;
                case AlertDialog.BUTTON_NEUTRAL:    //取消
                    dialog.dismiss();
                    break;
            }
        }
    }
}
