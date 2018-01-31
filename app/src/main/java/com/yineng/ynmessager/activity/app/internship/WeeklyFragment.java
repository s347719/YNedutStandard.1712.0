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
import com.yineng.ynmessager.bean.app.Internship.WeeklyReport;
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
 * Created by 贺毅柳 on 2015/12/21 16:21.
 */
public class WeeklyFragment extends ReportCalendarBaseFragment
    implements OnItemClickListener<WeeklyCalendarAdapter.ViewHolder> {
    private static final int SPAN_COUNT = 4;
    private RecyclerView mRcy_calendar;
    private WeeklyCalendarAdapter mCalendarAdapter;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reportcalendar_weekly, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRcy_calendar = (RecyclerView) view.findViewById(android.R.id.list);

        Context context = getContext();

        mRcy_calendar.setHasFixedSize(true);
        mRcy_calendar.setLayoutManager(new GridLayoutManager(context, SPAN_COUNT));
        mRcy_calendar.addItemDecoration(new GridSpacingItemDecoration(SPAN_COUNT,
            context.getResources().getDimensionPixelSize(R.dimen.reportCalendarWeekly_calenderItem_spacing), true));
        mCalendarAdapter = new WeeklyCalendarAdapter(context);
        mCalendarAdapter.setOnItemClickListener(this);
        mRcy_calendar.setAdapter(mCalendarAdapter);

        initData();
    }

    @Override protected void initData() {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {
            @Override protected void onPreExecute() {
                ((ReportCalendarActivity) mParentActivity).mProgressDialog.show();
            }

            @Override protected String doInBackground(Void... params) {
                return V8TokenManager.obtain();
            }

            @Override protected void onPostExecute(String token) {
                super.onPostExecute(token);
                String url =
                    AppController.getInstance().CONFIG_YNEDUT_V8_URL + "third/internship/dgQueryReportByUser.htm";
                Map<String,String> params = new HashMap<>();
                params.put("activityId", mThisInternshipAct.getId());
                params.put("userId", LastLoginUserSP.getLoginUserNo(getContext()));
                params.put("reportType", 2+"");
                params.put("version", "V1.0");
                params.put("access_token", token);
                OKHttpCustomUtils.get(url, params, getActivity(), new StringCallback() {
                    @Override
                    public void onResponse(String response, int id) {
                        List<WeeklyReport> weeklyReportList = assembleObjects(response);

                        mCalendarAdapter.setData(weeklyReportList);
                        mCalendarAdapter.notifyDataSetChanged();

                        //滚动到当前周
                        int scrollPosition = mCalendarAdapter.getCurrentWeekPosition();
                        if (scrollPosition >= 0) {
                            mRcy_calendar.smoothScrollToPosition(scrollPosition);
                        }
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

    @Nullable private List<WeeklyReport> assembleObjects(String responseString) {
        List<WeeklyReport> dataList = null;

        try {
            JSONObject responseObj = new JSONObject(responseString);
            int _status = responseObj.getInt("status");
            if (_status < 0) {
                return null;
            }

            JSONArray resultArr = responseObj.getJSONArray("result");
            int _len = resultArr.length();

            Date now = new Date();
            Date truncatedNow = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
            dataList = new ArrayList<>(_len);
            for (int i = 0; i < _len; ++i) {
                JSONObject result = resultArr.getJSONObject(i);
                String id = result.getString("id");
                int sequence = result.getInt("week");
                Date rangeBegin = DateUtils.parseDate(result.getString("startDate"), "yyyy-MM-dd");
                Date rangeEnd = DateUtils.parseDate(result.getString("endDate"), "yyyy-MM-dd");
                int status = result.getInt("status");
                String require = result.isNull("require") ? null : result.getString("require");

                WeeklyReport report = new WeeklyReport();
                report.setId(id);
                report.setRangeBegin(rangeBegin);
                report.setRangeEnd(rangeEnd);
                report.setSequence(sequence);
                report.setRequire(require);
                boolean isSameWeek = WeeklyCalendarAdapter.isInRangeOfWeeks(now, rangeBegin, rangeEnd);
                report.setSameDate(isSameWeek);

                if (hasLocalDraft(getApplicationContext(), report)) {
                    report.setStatus(InternshipReport.STATUS_DRAFT);
                } else if (status == InternshipReport.STATUS_EXPIRED) {
                    if (rangeBegin.compareTo(truncatedNow) < 0 && !isSameWeek) {
                        report.setStatus(InternshipReport.STATUS_EXPIRED);
                    }
                } else {
                    report.setStatus(status);
                }

                dataList.add(report);
            }
        } catch (JSONException | ParseException e) {
            L.e(getClass(), e.getMessage(), e);
        }

        return dataList;
    }

    @Override public void onItemClick(int position, WeeklyCalendarAdapter.ViewHolder viewHolder) {
        WeeklyReport report = mCalendarAdapter.getItem(position);
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
        startActivityForResult(intent, InternshipReport.REPORT_TYPE_WEEKLY);
    }
}
