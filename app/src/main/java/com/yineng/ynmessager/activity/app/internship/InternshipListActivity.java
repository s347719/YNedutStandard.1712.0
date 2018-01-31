package com.yineng.ynmessager.activity.app.internship;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.Internship.InternshipAct;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.view.EmptySupportRecyclerView;
import com.yineng.ynmessager.view.recyclerview.decoration.LinearVerSpacingItemDecoration;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

public class InternshipListActivity extends BaseActivity
    implements InternshipListAdapter.ItemViewHolder.onChildClickListener, InternshipListAdapter.OnLoadMoreListener,
    SwipeRefreshLayout.OnRefreshListener {
    private static final int PAGE_SIZE = 10;
    private SwipeRefreshLayout mSwpRef_refresher;
    private EmptySupportRecyclerView mRcy_list;
    private InternshipListAdapter mContentAdapter;
    private int mCurrentPage = -1;
    private ThisHandler mThisHandler = new ThisHandler(this);
    private ImageView closeBtn;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internship_list);

        initViews();
        initData(0, PAGE_SIZE);
    }

    private void initViews() {
        mSwpRef_refresher = (SwipeRefreshLayout) findViewById(R.id.internshipList_swpRef_refresher);
        mRcy_list = (EmptySupportRecyclerView) findViewById(android.R.id.list);
        closeBtn = (ImageView) findViewById(R.id.img_close);

        mSwpRef_refresher.setColorSchemeResources(R.color.app_actionBar_bg);
        mSwpRef_refresher.setEnabled(false);
        mSwpRef_refresher.setOnRefreshListener(this);

        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.internshipList_item_dividerSpacing);
        mRcy_list.addItemDecoration(new LinearVerSpacingItemDecoration(itemSpacing));
        mRcy_list.setLayoutManager(new LinearLayoutManager(this));
        View emptyView = findViewById(R.id.text_empty);
        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                initData(0, PAGE_SIZE);
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRcy_list.setEmptyTextHint("没有通知消息");
        mRcy_list.setEmptyView(emptyView);

        mContentAdapter = new InternshipListAdapter(this, mRcy_list);
        mContentAdapter.setOnItemChildClickListener(this);
        mContentAdapter.setOnLoadMoreListener(this);
        mRcy_list.setAdapter(mContentAdapter);
    }

    private void initData(final int page, final int pageSize) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {
            @Override protected void onPreExecute() {
                if (page == 0) {
                    mSwpRef_refresher.setRefreshing(true);
                }
            }

            @Override protected String doInBackground(Void... params) {
                return V8TokenManager.obtain();
            }

            @Override protected void onPostExecute(String token) {
                Context context = getApplicationContext();

                String url = AppController.getInstance().CONFIG_YNEDUT_V8_URL + "third/internship/dgQueryActivity.htm";

                Map<String,String> params = new HashMap<>();
                params.put("userId", LastLoginUserSP.getLoginUserNo(context));
                params.put("userType", LastLoginUserSP.getUserType(context)+"");
                params.put("version", "V1.0");
                params.put("access_token", token);
                params.put("pageSize", pageSize+"");
                params.put("pageNumber", page+"");

                L.i(mTag, "Internship list url : \n" + url);
                OKHttpCustomUtils.get(url, params, new StringCallback() {
                    List<InternshipAct> resultList = null;
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        this.resultList = null;
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        this.resultList = assembleObjects(response);
                        if (resultList != null) {
                            mCurrentPage = page;
                        }
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        mSwpRef_refresher.setEnabled(false);
                    }

                    @Override
                    public void onAfter(int id) {
                        L.v(mTag, "Internship data onFinish");

                        final List<InternshipAct> dataList = mContentAdapter.getData();

                        //remove progress item
                        if (page > 0) {
                            if (dataList != null && !dataList.isEmpty()) {
                                dataList.remove(dataList.size() - 1);
                                mContentAdapter.notifyItemRemoved(dataList.size());

                                if (this.resultList != null) {
                                    dataList.addAll(this.resultList);
                                }
                            }
                            mContentAdapter.setLoaded();
                            mContentAdapter.setData(dataList);
                        } else {
                            mContentAdapter.setData(this.resultList);
                            mSwpRef_refresher.setRefreshing(false);
                        }

                        mContentAdapter.notifyDataSetChanged();

                        mSwpRef_refresher.setEnabled(true);
                    }
                });
            }
        };

        AsyncTaskCompat.executeParallel(tokenTask);
    }

    /**
     * 请求数据成功后重新封装对象
     *
     * @return 返回JSON中，status不为0，或者content结果集长度为0，都返回Null
     */
    @Nullable private List<InternshipAct> assembleObjects(String responseString) {
        List<InternshipAct> internshipActList = null;
        try {
            JSONObject responseJson = new JSONObject(responseString);
            int status = responseJson.getInt("status");
            if (status != 0) {
                return null;
            }

            JSONArray contentArr = responseJson.getJSONObject("result").getJSONArray("content");
            int length = contentArr.length();
            if (length == 0) {
                return null;
            }

            internshipActList = new ArrayList<>(length);
            for (int i = 0; i < length; ++i) {
                InternshipAct act = new InternshipAct();
                JSONObject obj = contentArr.getJSONObject(i);

                String id = obj.getString("id");
                String title = obj.getString("activityName");
                String teacher = obj.isNull("teacher") ? StringUtils.EMPTY : obj.getString("teacher");
                String company = obj.isNull("company") ? StringUtils.EMPTY : obj.getString("company");

                Date startDate = DateUtils.parseDate(obj.getString("startDate"), "yyyy/MM/dd");
                Date endDate = DateUtils.parseDate(obj.getString("begionDate"), "yyyy/MM/dd");
                boolean hasDaily = obj.getBoolean("isDayEnabled");
                boolean hasWeekly = obj.getBoolean("isWeekEnabled");
                boolean hasMonthly = obj.getBoolean("isMonthEnabled");

                act.setId(id);
                act.setTitle(title);
                act.setTeacher(teacher);
                act.setCompany(company);
                act.setStartDate(startDate);
                act.setEndDate(endDate);
                act.setHasDaily(hasDaily);
                act.setHasWeekly(hasWeekly);
                act.setHasMonthly(hasMonthly);

                internshipActList.add(act);
            }
        } catch (JSONException | ParseException e) {
            L.e(mTag, e.getMessage(), e);
            if (internshipActList != null) {
                internshipActList.clear();
            }
        }
        return internshipActList;
    }

    @Override public void onLoadMore() {
        final List<InternshipAct> dataList = mContentAdapter.getData();
        //add progress item
        dataList.add(null);
        mContentAdapter.notifyItemInserted(dataList.size() - 1);

        mThisHandler.postDelayed(new Runnable() {
            @Override public void run() {
                initData(mCurrentPage + 1, PAGE_SIZE);
            }
        }, 500);
    }

    @Override public void onRefresh() {
        L.d(mTag, "refreshing");

        mCurrentPage = 0;
        initData(mCurrentPage, PAGE_SIZE);
    }

    @Override public void onDaily(int position, InternshipAct internshipAct) {
        Intent intent = new Intent(this, ReportCalendarActivity.class);
        intent.putExtra("InternShipAct", internshipAct);
        intent.putExtra("Which", InternshipReport.REPORT_TYPE_DAILY);
        startActivity(intent);
    }

    @Override public void onWeekly(int position, InternshipAct internshipAct) {
        Intent intent = new Intent(this, ReportCalendarActivity.class);
        intent.putExtra("InternShipAct", internshipAct);
        intent.putExtra("Which", InternshipReport.REPORT_TYPE_WEEKLY);
        startActivity(intent);
    }

    @Override public void onMonthly(int position, InternshipAct internshipAct) {
        Intent intent = new Intent(this, ReportCalendarActivity.class);
        intent.putExtra("InternShipAct", internshipAct);
        intent.putExtra("Which", InternshipReport.REPORT_TYPE_MONTHLY);
        startActivity(intent);
    }

    static class ThisHandler extends Handler {
        WeakReference<Activity> activityWeakReference;

        ThisHandler(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }
    }
}
