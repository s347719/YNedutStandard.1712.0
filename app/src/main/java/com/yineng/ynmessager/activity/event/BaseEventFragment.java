package com.yineng.ynmessager.activity.event;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseFragment;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.activity.app.CordovaWebActivity;
import com.yineng.ynmessager.activity.app.X5WebAppActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Request;

public abstract class BaseEventFragment extends BaseFragment implements XRecyclerView.LoadingListener, XRecyclerView.OnRetryListener {

    public static final int EVENT_TODO_TYPE = 0;

    public static final int EVENT_DEMAND_TYPE = 1;

    public static final int EVENT_DONE_TYPE = 2;
    /**
     * 发起申请
     */
    public static final int EVENT_SEND_TYPE = 3;

    private static final int PAGE_ITEMS_LOAD = 20;
    public XRecyclerView mRcy_list;
    public View mEmptyView;
    protected String token;
    public View view_delete;
    public View event_send_hint;
    public View event_hole_view;
    public View send_request_image_close;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_event_todo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        event_hole_view =view.findViewById(R.id.event_hole_view);
        mEmptyView = view.findViewById(R.id.text_empty);
        mRcy_list = (XRecyclerView) view.findViewById(R.id.main_rcy_todoEventList);
        view_delete =view.findViewById(R.id.view_delete);
        event_send_hint = view.findViewById(R.id.event_send_hint);
        send_request_image_close =  view.findViewById(R.id.send_request_image_close);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRcy_list.setLayoutManager(layoutManager);
        mRcy_list.setLoadingListener(this);
        mRcy_list.setOnRetryListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventMainThread(CommonEvent event) {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    /**
     * 根据事件类型，页码请求获取数据
     *
     * @param eventType
     * @param isForceRefresh
     * @param page
     */
    public void loadEventDataByPageIndex(final boolean aotoRefresh, final boolean isTop, final int eventType, final boolean isForceRefresh, final int page) {
        @SuppressLint("StaticFieldLeak")
        final AsyncTask<Boolean, Integer, String> task = new AsyncTask<Boolean, Integer, String>() {

            @Override
            protected String doInBackground(Boolean... params) {

                if (params[0]) {
                    token = V8TokenManager.forceRefresh();
                } else {
                    token = V8TokenManager.obtain();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                if (mApplication == null) {
                    mApplication = AppController.getInstance();
                    if (mApplication == null) {
                        return;
                    }
                }
                String url = null;
                switch (eventType) {
                    case EVENT_TODO_TYPE:
                        url = mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL;
                        break;
                    case EVENT_DEMAND_TYPE:
                        url = URLs.DEMAND_DATA_URL;
                        break;
                    case EVENT_DONE_TYPE:
                        url = URLs.DONE_DATA_URL;
                        break;
                    case EVENT_SEND_TYPE:
                        url = URLs.SEND_REQUEST;
                        break;
                    default:
                        break;
                }
                if (TextUtils.isEmpty(url)) {
                    L.w(mTag, "todo events url is empty so data won't come");
                    return;
                }

                url = MessageFormat.format(url, "V1.0", token, LastLoginUserSP.getInstance(getContext()).getUserAccount(), page, PAGE_ITEMS_LOAD);
                L.d(mTag, "todo events url : " + url);
                OKHttpCustomUtils.get(url, new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                            // 如果是因为token失效导致的请求出错，则重新刷新token来请求
                            if (e.getMessage()!=null && e.getMessage().contains("invalid_token")) {
                                loadEventDataByPageIndex(true,true,eventType, true, 0);
                            }
                    }
                    @Override
                    public void onResponse(String response, int id) {
                        if (TextUtils.isEmpty(response)) {
                            return;
                        }

                        onTaskGetJson(response, page);
                    }

                    @Override
                    public void onBefore(Request request, int id) {
                        // TODO: 2016/6/21 异常：mSwp_refresher -> NullPointerException
                        if (mRcy_list != null&&aotoRefresh) {
                            mRcy_list.refresh();
                        }
                        if (mRcy_list != null&&isTop){
                            mRcy_list.setNoMore(false);
                        }
                    }

                    @Override
                    public void onAfter(int id) {
                        if (mRcy_list != null&&aotoRefresh) {
                            mRcy_list.refreshComplete();
                        }
                        onTaskFinish(isTop);
                    }
                });
            }
        };
        AsyncTaskCompat.executeParallel(task, isForceRefresh);
    }

    /**
     * 刷新待办模块中选项卡计数
     *
     * @param count     待办数量
     * @param eventType 事件类型
     */
    public void refreshEventCount(String count, int eventType) {
        if (!TextUtils.isEmpty(count)) {
            String countStr;
            switch (eventType) {
                case EVENT_TODO_TYPE:
                    //更改事件中我的待办选项卡中的计数
                    if (Integer.parseInt(count) > 0) {
                        countStr = getString(R.string.main_event_my_todo) + "(" + count + ")";
                    } else {
                        countStr = getString(R.string.main_event_my_todo);
                    }
                    EventBus.getDefault().post(new CommonEvent(EVENT_TODO_TYPE, countStr));
                    break;
                case EVENT_DEMAND_TYPE:
                    //更改事件中我的申请选项卡中的计数
                    if (Integer.parseInt(count) > 0) {
                        countStr = getString(R.string.main_event_my_demand) + "(" + count + ")";
                    } else {
                        countStr = getString(R.string.main_event_my_demand);
                    }
                    EventBus.getDefault().post(new CommonEvent(EVENT_DEMAND_TYPE, countStr));
                    break;
                case EVENT_DONE_TYPE:
                    //更改事件中我的申请选项卡中的计数
                    if (Integer.parseInt(count) > 0) {
                        countStr = getString(R.string.main_event_my_done) + "(" + count + ")";
                    } else {
                        countStr = getString(R.string.main_event_my_done);
                    }
                    EventBus.getDefault().post(new CommonEvent(EVENT_DONE_TYPE, countStr));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *
     * @param formSource
     * @param id
     * @param smesisUrl
     * @param type   0-待办  1-已办
     */
    public void startWebActivity(String formSource, String id,String smesisUrl,int type,String smesisName) {
        String url = null;
        Intent intent = new Intent();
        if (TextUtils.isEmpty(formSource)) {
            url = URLs.getOAUrl(getApplicationContext(), "", id ,type);
            intent.setClass(mParentActivity, X5WebAppActivity.class);
            intent.putExtra("Name", "OA");
            intent.putExtra(X5WebAppActivity.IS_OA, true);
        } else {
            switch (formSource) {
                case "0"://宏天OA
                    url = URLs.getOAUrl(getApplicationContext(), "", id,type);
                    intent.setClass(mParentActivity, X5WebAppActivity.class);
                    intent.putExtra("Name", "OA");
                    intent.putExtra(X5WebAppActivity.IS_OA, true);
                    break;
                case "1":
                    intent.setClass(mParentActivity, X5WebAppActivity.class);
                    intent.putExtra("Name", "详情");
                    String ynedutUrl = URLs.getYnedutUrl(getActivity(), token, id);
                    url = MessageFormat.format(mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL,mApplication.mAppTokenStr,LastLoginUserSP.getInstance(getActivity()).getUserAccount(),ynedutUrl,"1");
                    break;
                case "2":
                    intent.setClass(mParentActivity, CordovaWebActivity.class);
                    intent.putExtra("title", smesisName);
                    String smesisUserId= LastLoginUserSP.getInstance(getActivity()).getSmesisUserId();

                    if (StringUtils.isEmpty(smesisUserId)) {
                        EventActivity eventActivity= (EventActivity) getActivity();
                        eventActivity.initIdPastDialog("");
                        return;
                    }

                    url = URLs.getSmesisUrl(smesisUrl, token,smesisUserId);
                    break;
                default:
                    break;
            }
        }
        if (!TextUtils.isEmpty(url)) {
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }
    public Comparator<TodoEvent> comparator = new Comparator<TodoEvent>() {
        @Override
        public int compare(TodoEvent o1, TodoEvent o2) {
            long time1;
            long time2;
            if (TextUtils.isEmpty(o1.getReviewTime())){
                time1 = TimeUtil.getMillisecondByDate(o1.getCreateTime(),TimeUtil.FORMAT_DATETIME_24);
            }else {
                time1 = TimeUtil.getMillisecondByDate(o1.getReviewTime(),TimeUtil.FORMAT_DATETIME_24);
            }

            if (TextUtils.isEmpty(o2.getReviewTime())){
                time2 = TimeUtil.getMillisecondByDate(o2.getCreateTime(),TimeUtil.FORMAT_DATETIME_24);
            }else {
                time2 = TimeUtil.getMillisecondByDate(o2.getReviewTime(),TimeUtil.FORMAT_DATETIME_24);
            }

            return (int)(time1- time2);
        }
    } ;

    /**
     * 得到JSON
     *
     * @param responseString json字符串
     * @param page           页码
     */
    public abstract void onTaskGetJson(String responseString, int page);

    /**
     * 获取数据的线程结束
     */
    public abstract void onTaskFinish(boolean isTop);

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRetry(XRecyclerView view) {

    }
}
