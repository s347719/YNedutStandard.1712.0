package com.yineng.ynmessager.activity.app.internship;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.internship.report.ReportContentActivity;
import com.yineng.ynmessager.activity.app.internship.report.ReportFeedbackActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Internship.DailyReport;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.bean.app.Internship.MonthHeadIndicator;
import com.yineng.ynmessager.bean.app.Internship.MonthIndicator;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.recyclerview.decoration.GridSpacingItemDecoration;
import com.yineng.ynmessager.view.recyclerview.decoration.LinearVerSpacingItemDecoration;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;

/**
 * Created by 贺毅柳 on 2015/12/18 13:54.
 */
public class DailyFragment extends ReportCalendarBaseFragment implements OnItemClickListener {
    private static final int CALENDAR_SPAN_COUNT = 4;
    private RecyclerView mRcy_indicator;
    private RecyclerView mRcy_calendar;
    private TextView mTxt_tip;
    private DailyIndicatorAdapter mIndicatorAdapter;
    private DailyCalendarAdapter mCalendarAdapter;
    private Handler mHandler = new Handler();

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reportcalendar_daily, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRcy_indicator = (RecyclerView) view.findViewById(R.id.reportCalendarDaily_rcy_indicator);
        mRcy_calendar = (RecyclerView) view.findViewById(R.id.reportCalendarDaily_rcy_calendar);
        mTxt_tip = (TextView) view.findViewById(R.id.reportCalendarDaily_txt_tip);

        Context context = getContext();
        Resources res = context.getResources();

        mIndicatorAdapter = new DailyIndicatorAdapter(context);
        mIndicatorAdapter.setOnItemClickListener(this);
        mRcy_indicator.setHasFixedSize(true);
        mRcy_indicator.setLayoutManager(new LinearLayoutManager(context));
        mRcy_indicator.addItemDecoration(new LinearVerSpacingItemDecoration(
            res.getDimensionPixelSize(R.dimen.reportCalendarDaily_indicatorItem_spacing), true));
        mRcy_indicator.setAdapter(mIndicatorAdapter);

        mCalendarAdapter = new DailyCalendarAdapter(context);
        mCalendarAdapter.setOnItemClickListener(this);
        mRcy_calendar.setLayoutManager(new GridLayoutManager(context, CALENDAR_SPAN_COUNT));
        mRcy_calendar.addItemDecoration(new GridSpacingItemDecoration(CALENDAR_SPAN_COUNT,
            res.getDimensionPixelSize(R.dimen.reportCalendarDaily_calenderItem_spacing), true));
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
                params.put("reportType", InternshipReport.REPORT_TYPE_DAILY+"");
                params.put("version", "V1.0");
                params.put("access_token", token);
                L.i(mTag, "Daily Report Calendar Url : \n" + url);
                OKHttpCustomUtils.get(url, params,new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        showEmptyView(true);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        List<DailyReport> dailyReportList = assembleObjects(response);
                        List<MonthIndicator> monthIndicatorList = assembleMonthIndicators(dailyReportList);

                        mIndicatorAdapter.setData(monthIndicatorList);
                        mIndicatorAdapter.notifyDataSetChanged();

                        Date today = new Date();
                        //设置是否显示“今天无需填报”
                        if (dailyReportList != null) {
                            boolean hasToday = false;
                            for (DailyReport report : dailyReportList) {
                                if (DateUtils.isSameDay(today, report.getDate())) {
                                    hasToday = true;
                                    break;
                                }
                            }
                            if (!hasToday) {
                                mTxt_tip.setVisibility(View.VISIBLE);
                                mTxt_tip.setText(
                                        getString(R.string.reportCalendar_tip, DateFormatUtils.format(today, "M月d日")));
                            }
                        }

                        //滚动到默认位置
                        int position = 0;
                        boolean isExist = false;
                        for (MonthIndicator indicator : monthIndicatorList) {
                            if (DateUtils.truncatedEquals(today, indicator.getDate(), Calendar.MONTH)) {
                                isExist = true;
                                break;
                            }
                            ++position;
                        }

                        if (!isExist) {
                            position = 1;
                        }

                        L.d(mTag, "scroll to default month indicator -> " + position);
                        selectIndicator(position);
                        mRcy_indicator.smoothScrollToPosition(position);

                        // 自动滚动到指定位置
                        final int _position = position;
                        mHandler.postDelayed(new Runnable() {
                            @Override public void run() {
                                RecyclerView.ViewHolder viewHolder =
                                        mRcy_indicator.findViewHolderForAdapterPosition(_position);
                                if (viewHolder != null) {
                                    viewHolder.itemView.performClick();
                                }
                            }
                        }, 200);
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

    @Nullable private List<DailyReport> assembleObjects(String responseString) {
        List<DailyReport> dataList;
        Date now = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);

        try {
            JSONObject responseJson = new JSONObject(responseString);
            int responseStatus = responseJson.getInt("status");
            if (responseStatus < 0) {
                return null;
            }
            dataList = new ArrayList<>();

            JSONArray resultArray = responseJson.getJSONArray("result");
            int _len_y = resultArray.length();
            for (int y = 0; y < _len_y; ++y) {
                JSONObject yearObj = resultArray.getJSONObject(y);

                JSONArray monthArr = yearObj.getJSONArray("month");
                int _len_m = monthArr.length();
                for (int m = 0; m < _len_m; ++m) {
                    JSONObject monthObj = monthArr.getJSONObject(m);

                    JSONArray dayArr = monthObj.getJSONArray("dayReport");
                    int _len_d = dayArr.length();
                    for (int d = 0; d < _len_d; ++d) {
                        JSONObject dayObj = dayArr.getJSONObject(d);

                        String id = dayObj.getString("id");
                        Date date = DateUtils.parseDate(dayObj.getString("date"), "yyyy-MM-dd");
                        int status = dayObj.getInt("status");
                        String require = dayObj.isNull("require") ? null : dayObj.getString("require");

                        DailyReport report = new DailyReport();
                        report.setId(id);
                        report.setDate(date);
                        report.setRequire(require);

                        if (hasLocalDraft(getApplicationContext(), report)) {
                            report.setStatus(InternshipReport.STATUS_DRAFT);
                        } else if (status == InternshipReport.STATUS_EXPIRED) {
                            if (date.compareTo(now) < 0) {
                                report.setStatus(InternshipReport.STATUS_EXPIRED);
                            }
                        } else {
                            report.setStatus(status);
                        }

                        L.v(mTag, report.toString());
                        dataList.add(report);
                    }
                }
            }
        } catch (JSONException | ParseException e) {
            L.e(mTag, e.getMessage(), e);
            return null;
        }

        //顺序可能不是自然顺序，所以重新排序
        Collections.sort(dataList, new Comparator<DailyReport>() {
            @Override public int compare(DailyReport lhs, DailyReport rhs) {
                Date l_date = lhs.getDate();
                Date r_date = rhs.getDate();
                return l_date.compareTo(r_date);
            }
        });

        return dataList;
    }

    private List<MonthIndicator> assembleMonthIndicators(List<DailyReport> dailyReportList) {
        //先根据所有的DailyReport生成MonthIndicator列表数据，包括MonthHeadIndicator
        Set<MonthIndicator> monthsSet = new LinkedHashSet<>();
        Date prev = null;
        for (DailyReport report : dailyReportList) {
            MonthIndicator monthIndicator = new MonthIndicator();
            Date reportDate = report.getDate();
            monthIndicator.setDate(DateUtils.truncate(reportDate, Calendar.MONTH));

            //如果上个日报的Date为空 或者 与当前遍历的Date年份不同，则需要生成并添加一个YearHead
            if (prev == null || !DateUtils.truncatedEquals(prev, reportDate, Calendar.YEAR)) {
                MonthHeadIndicator yearHead = new MonthHeadIndicator();
                yearHead.setDate(DateUtils.truncate(reportDate, Calendar.YEAR));
                monthsSet.add(yearHead);
            }

            monthsSet.add(monthIndicator);
            prev = reportDate;
        }

        //把MonthIndicatorSet转换成List重新排序一下
        List<MonthIndicator> monthIndicatorList = new ArrayList<>(monthsSet);
        Collections.sort(monthIndicatorList, new Comparator<MonthIndicator>() {
            @Override public int compare(MonthIndicator lhs, MonthIndicator rhs) {
                Date l_date = lhs.getDate();
                Date r_date = rhs.getDate();
                return l_date.compareTo(r_date);
            }
        });

        //把各个DailyReport加入其对应的MonthIndicator中
        for (MonthIndicator indicator : monthIndicatorList) {
            //如果是MonthHeadIndicator则直接跳过
            if (indicator instanceof MonthHeadIndicator) {
                continue;
            }

            List<DailyReport> dailyReports = new ArrayList<>(1);
            Date indicatorMonth = indicator.getDate();

            for (DailyReport report : dailyReportList) {
                if (DateUtils.truncatedEquals(indicatorMonth, report.getDate(), Calendar.MONTH)) {
                    dailyReports.add(report);
                }
            }
            indicator.setDailyReportList(dailyReports);
        }

        return monthIndicatorList;
    }

    private void selectIndicator(int position) {
        List<MonthIndicator> dataList = mIndicatorAdapter.getData();
        for (MonthIndicator temp : dataList) {
            temp.setSelected(false);
        }

        dataList.get(position).setSelected(true);
        mIndicatorAdapter.notifyDataSetChanged();
    }

    @Override public void onItemClick(int position, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof DailyIndicatorAdapter.GeneralViewHolder) {
            selectIndicator(position);

            MonthIndicator indicator = mIndicatorAdapter.getItem(position);
            List<DailyReport> reportList = indicator.getDailyReportList();
            mCalendarAdapter.setData(reportList);
            mCalendarAdapter.notifyDataSetChanged();
        } else if (viewHolder instanceof DailyCalendarAdapter.ViewHolder) {
            DailyReport report = mCalendarAdapter.getItem(position);
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
            startActivityForResult(intent, InternshipReport.REPORT_TYPE_DAILY);
        }
    }
}