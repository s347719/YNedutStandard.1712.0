package com.yineng.ynmessager.activity.app.internship;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.internship.report.ReportContentActivity;
import com.yineng.ynmessager.activity.app.internship.report.ReportFeedbackActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.bean.app.Internship.MonthlyReport;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.decoration.GridSpacingItemDecoration;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by 贺毅柳 on 2015/12/22 11:02.
 */
public class MonthlyFragment extends ReportCalendarBaseFragment
        implements OnItemClickListener<MonthlyCalendarAdapter.ViewHolderItem> {
    private static final int SPAN_COUNT = 4;
    private RecyclerView mRcy_calendar;
    private MonthlyCalendarAdapter mCalendarAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reportcalendar_monthly, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRcy_calendar = (RecyclerView) view.findViewById(android.R.id.list);

        Context context = getContext();

        mRcy_calendar.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, SPAN_COUNT);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mCalendarAdapter.getItemViewType(position) == MonthlyCalendarAdapter.VIEW_TYPE_HEAD ? SPAN_COUNT
                        : 1;
            }
        });
        mRcy_calendar.setLayoutManager(gridLayoutManager);
        mRcy_calendar.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT,
                context.getResources().getDimensionPixelSize(R.dimen.reportCalendarMonthly_calenderItem_spacing), true));
        mCalendarAdapter = new MonthlyCalendarAdapter(context);
        mCalendarAdapter.setOnItemClickListener(this);
        mRcy_calendar.setAdapter(mCalendarAdapter);
        initData();
    }

    @Override
    protected void initData() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                ((ReportCalendarActivity) mParentActivity).mProgressDialog.show();
            }

            @Override
            protected String doInBackground(Void... params) {
                return V8TokenManager.obtain();
            }

            @Override
            protected void onPostExecute(String token) {
                super.onPostExecute(token);
                String url =
                        AppController.getInstance().CONFIG_YNEDUT_V8_URL + "third/internship/dgQueryReportByUser.htm";
                Map<String, String> params = new HashMap<>();
                params.put("activityId", mThisInternshipAct.getId());
                params.put("userId", LastLoginUserSP.getLoginUserNo(getContext()));
                params.put("reportType", 3 + "");
                params.put("version", "V1.0");
                params.put("access_token", token);
                OKHttpCustomUtils.get(url, params, new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        List<MonthlyReport> weeklyReportList;
                        try {
                            weeklyReportList = assembleObjects(response);
                        } catch (JSONException | ParseException e) {
                            L.e(mTag, e.getMessage(), e);
                            return;
                        }
                        mCalendarAdapter.setData(weeklyReportList);
                        mCalendarAdapter.notifyDataSetChanged();

                        //滚动到当前月
                        int scrollPosition = mCalendarAdapter.getCurrentMonthPosition();
                        if (scrollPosition >= 0) {
                            mRcy_calendar.smoothScrollToPosition(scrollPosition);
                        }
                        L.v(mTag, "scroll to position " + scrollPosition);
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        showEmptyView(true);
                    }

                    @Override
                    public void onAfter(int id) {
                        ((ReportCalendarActivity) mParentActivity).mProgressDialog.dismiss();
                    }
                });

            }
        };
        AsyncTaskCompat.executeParallel(tokenTask);
    }

    @Nullable
    private List<MonthlyReport> assembleObjects(String responseString) throws JSONException, ParseException {
        JSONObject responseObj = new JSONObject(responseString);
        //JSON数据status返回不为0直接返回null
        if (responseObj.getInt("status") != 0) {
            return null;
        }

        Date now = DateUtils.truncate(new Date(), Calendar.MONTH);
        List<MonthlyReport> monthlyReportList = new ArrayList<>();
        JSONArray yearArr = responseObj.getJSONArray("result");
        int _yearLen = yearArr.length();
        for (int yIndex = 0; yIndex < _yearLen; ++yIndex) {
            JSONObject yearObj = yearArr.getJSONObject(yIndex);
            final int year = yearObj.getInt("year");

            MonthlyReport head = new MonthlyReport();
            head.setDate(DateUtils.parseDate(String.valueOf(year), "yyyy"));
            head.setHead(true);
            monthlyReportList.add(head);

            JSONArray monthArr = yearObj.getJSONArray("month");
            int _monthLen = monthArr.length();
            for (int mIndex = 0; mIndex < _monthLen; ++mIndex) {
                JSONObject monthObj = monthArr.getJSONObject(mIndex);

                MonthlyReport report = new MonthlyReport();
                report.setId(monthObj.getString("id"));
                report.setRequire(monthObj.isNull("require") ? null : monthObj.getString("require"));
                Date date = DateUtils.parseDate(year + "/" + monthObj.getInt("value"), "yyyy/MM");
                report.setDate(date);
                report.setHead(false);
                report.setSameDate(MonthlyCalendarAdapter.isSameMonth(now, date));

                int status = monthObj.getInt("status");
                if (hasLocalDraft(getApplicationContext(), report)) {
                    report.setStatus(InternshipReport.STATUS_DRAFT);
                } else if (status == InternshipReport.STATUS_EXPIRED) {
                    if (date.compareTo(now) < 0) {
                        report.setStatus(InternshipReport.STATUS_EXPIRED);
                    }
                } else {
                    report.setStatus(status);
                }

                monthlyReportList.add(report);
            }
        }

        return monthlyReportList;
    }

    @Override
    public void onItemClick(int position, MonthlyCalendarAdapter.ViewHolderItem viewHolder) {
        MonthlyReport report = mCalendarAdapter.getItem(position);
        Intent intent;
        int status = report.getStatus();
        if (status == InternshipReport.STATUS_PASSED || status == InternshipReport.STATUS_REDO) {
            intent = new Intent(mParentActivity, ReportFeedbackActivity.class);
        } else if (status == InternshipReport.STATUS_PENDING) {
            //待审核状态不执行操作
            return;
        } else {
            intent = new Intent(mParentActivity, ReportContentActivity.class);
        }

        intent.putExtra("Report", report);
        startActivityForResult(intent, InternshipReport.REPORT_TYPE_MONTHLY);
    }
}
