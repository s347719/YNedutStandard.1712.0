//***************************************************************
//*    2015-6-24  上午10:21:08
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.event;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.CordovaWebActivity;
import com.yineng.ynmessager.bean.event.BatchTodoItem;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;


/**
 * @author 贺毅柳
 */
public class TodoFragment extends BaseEventFragment implements OnItemClickListener<TodoListAdapter.ViewHolder> {
    private TodoListAdapter mAdapter;
    private int mCurrentPage = 0;
    private boolean isRestart = false;

    /**
     * 被点击的待办对象
     */
    private TodoEvent mClickevent = null;
    /**
     * 待办总计数
     */
    private String totalElements;
    private int hint_todo = R.string.empty_hint_todo;

    /**
     * 弹出框
     */
    private View popView;
    private PopupWindow popWindows;
    String originUrl;
    private List<BatchTodoItem> batchTodoItems = new ArrayList<>();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        originUrl = StringUtils.chop(mApplication.CONFIG_YNEDUT_V8_URL);
        mAdapter = new TodoListAdapter(getApplicationContext());
        mAdapter.setOnItemClickListener(this);
        mRcy_list.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isRestart && mClickevent != null) {
            //获取该条待办的状态
            getTodoEventStatus(mClickevent);
//            refreshTodoList();
        }
        isRestart = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRestart = true;
    }

    @Override
    public void onDestroy() {
        isRestart = false;
        super.onDestroy();
    }

    public void refreshTodoList(boolean aotuRfresh,boolean isTop) {
        refreshTodoList(aotuRfresh,isTop,false, 0);
    }

    private void refreshTodoList(boolean aotuRfresh,boolean isTop,boolean isForceRefresh, final int page) {
        loadEventDataByPageIndex(aotuRfresh,isTop,EVENT_TODO_TYPE, isForceRefresh, page);
    }

    @Override
    public void onRefresh() {
        refreshTodoList(false,true);
    }

    @Override
    public void onLoadMore() {
        refreshTodoList(false,false,false, mCurrentPage + 1);
    }

    @Override
    public void onRetry(XRecyclerView view) {
        refreshTodoList(true,true);
    }

    @Override
    public void onTaskGetJson(String responseString, int page) {
        ArrayList<TodoEvent> dataList = null;
        try {
            JSONObject jsonResp = new JSONObject(responseString);
            JSONObject result = jsonResp.getJSONObject("result");
            JSONArray content = result.getJSONArray("content");
            //解析待办总数量
            totalElements = result.optString("totalElements");
            //刷新待办总数量
            if (isAdded()) {
                refreshEventCount(totalElements, EVENT_TODO_TYPE);
            }
            int size = content.length();
            if (size > 0) {

                dataList = new ArrayList<>(size);
            }
            for (int i = 0; i < size; ++i) {
                JSONObject todoContent = content.getJSONObject(i);
                TodoEvent todoEvent = new TodoEvent();
                todoEvent.setId(todoContent.getString("id"));
                todoEvent.setName(todoContent.getString("name"));
                todoEvent.setCreateTime(todoContent.getString("createTime"));
                todoEvent.setPreChecker(todoContent.getString("preChecker"));
                todoEvent.setReviewAdvice(todoContent.getString("reviewAdvice"));
                todoEvent.setReviewTime(todoContent.getString("reviewTime"));
                todoEvent.setOnlyPcCheck(todoContent.getBoolean("isOnlyPcCheck"));
                todoEvent.setUrl(todoContent.getString("url"));
                todoEvent.setFormSource(todoContent.getString("formSource"));
                dataList.add(todoEvent);
            }
        } catch (JSONException e) {
            L.w(mTag, e.getMessage(), e);
            if (dataList != null) {
                dataList.clear();
            }
        }
        if (page > 0)
        {
            if (dataList != null) {
             mAdapter.getData().addAll(dataList);
            }
        } else
        {
            mAdapter.setData(dataList);
        }
        mAdapter.notifyDataSetChanged();

        if (dataList != null && dataList.size() > 0) {
            mCurrentPage = page;
            mRcy_list.setNoMore(false);
        }else {
            mRcy_list.setNoMore(true);
        }
    }

    @Override
    public void onTaskFinish(boolean isTop) {
        if (mAdapter.getItemCount() > 0) {
            if (view_delete.getVisibility()!=View.VISIBLE){
//                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(
//                        LinearLayout.LayoutParams.MATCH_PARENT,
//                        LinearLayout.LayoutParams.WRAP_CONTENT
//                );
//                p.setMargins(0,0,0, SystemUtil.dp2px(getActivity(),70));
//                mRcy_list.setLayoutParams(p);
                view_delete.setVisibility(View.VISIBLE);
                view_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  批量处理
                        showPopWindow();
                    }
                });
            }
            if (isTop){
                mRcy_list.refreshComplete();
            }else {
                mRcy_list.loadMoreComplete();
            }
        } else {
            if (isTop){
                mRcy_list.refreshComplete();
            }else {
                mRcy_list.loadMoreComplete();
            }
            mRcy_list.setEmptyView(mEmptyView, hint_todo);
        }
    }

    @Override
    public void onItemClick(int position, TodoListAdapter.ViewHolder viewHolder) {
        mClickevent = mAdapter.getItem(position-1);
        if (!mClickevent.isOnlyPcCheck()) {

            startWebActivity(mClickevent.getFormSource(),mClickevent.getId(),mClickevent.getUrl(),0,mClickevent.getName());
        } else {
            ToastUtil.toastAlerMessageCenter(getActivity(), "此申请只能在电脑上审批", 500);
        }
    }
    /**
     *将数据按来源排序
     */
    private Comparator<BatchTodoItem> formsource = new Comparator<BatchTodoItem>() {
        @Override
        public int compare(BatchTodoItem o1, BatchTodoItem o2) {
            return o1.getFormSource().compareTo(o2.getFormSource());
        }
    };
    /**
     * 获取某条待办的待办状态
     *
     * @param clickEvent 点击信息
     */
    public void getTodoEventStatus(TodoEvent clickEvent) {
        String url = URLs.TODO_STATUS_URL;
        //给url赋上参数
        url = MessageFormat.format(url, V8TokenManager.obtain(),
                LastLoginUserSP.getInstance(getActivity()).getUserAccount(), clickEvent.getId(), clickEvent.getFormSource());
        L.e(url);
        OKHttpCustomUtils.get(url, new JSONObjectCallBack() {
            private TodoEvent mTodoEvent;

            private JSONObjectCallBack setEventObj(TodoEvent todoEvent) {
                mTodoEvent = todoEvent;
                return this;
            }

            @Override
            public void onResponse(JSONObject response, int id) {
                int result = response.optInt("result");
                int status = response.optInt("status");
                if (0==status && 1==result) {//该条待办已经变为已办
                    int pos = mAdapter.getItemPostion(mTodoEvent);
                    if (pos < 0) {
                        return;
                    }
                    mAdapter.getData().remove(pos);
                    mAdapter.notifyDataSetChanged();
                    if (!TextUtils.isEmpty(totalElements)) {
                        int count = Integer.parseInt(totalElements);
                        //刷新待办总数量
                        refreshEventCount((count - 1) + "", EVENT_TODO_TYPE);
                    }
                }
            }

            @Override
            public void onAfter(int id) {
                super.onAfter(id);
                mTodoEvent = null;
            }
        }.setEventObj(clickEvent));
    }

    /**
     * 弹框选择
     *
     */
    private void showPopWindow() {
        batchTodoItems.clear();
        popView = LayoutInflater.from(getActivity()).inflate(
                R.layout.event_todo_batch_view, null);
        final View empty_data = popView.findViewById(R.id.empty_data);
        View event_todo_batch_dis_view = popView.findViewById(R.id.event_todo_batch_dis_view);
        event_todo_batch_dis_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindows.dismiss();
            }
        });
        View todo_pop_cancel = popView.findViewById(R.id.todo_pop_cancel);
        todo_pop_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindows.dismiss();
            }
        });

        final ListView todo_batch_list = (ListView) popView.findViewById(R.id.todo_batch_list);
        final TodoPopAdapter adapter = new TodoPopAdapter(getActivity(),batchTodoItems);
        todo_batch_list.setAdapter(adapter);
        todo_batch_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO  跳转
                BatchTodoItem item = batchTodoItems.get(position);
                StringBuilder urlArg = new StringBuilder("file:///android_asset/www/index.html#/batchApproval?")
                        .append("userId=" + LastLoginUserSP.getLoginUserNo(getActivity(), false))
                        .append("&originUrl="+ originUrl)
                        .append("&userType="+LastLoginUserSP.getInstance(getActivity()).getUserType())
                        .append("&access_token="+token)
                        .append("&formSource=" + item.getFormSource())
                        .append("&proId=" + item.getId())
                        .append("&procDefName="+item.getName());

                L.d(mTag, "生成的web地址：" + urlArg.toString());
                final String _urlArg = urlArg.toString();
                Intent intent = new Intent(getActivity(), CordovaWebActivity.class);
                intent.putExtra("url", _urlArg);
                intent.putExtra("title", item.getName());
                startActivityForResult(intent,200);
                popWindows.dismiss();
            }
        });
        popWindows = new PopupWindow(popView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        popWindows.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x00000000);
        popWindows.setBackgroundDrawable(dw);
        popWindows.showAtLocation(event_hole_view, Gravity.BOTTOM,0,0);
        popWindows.setAnimationStyle(R.style.new_popwindow_anim_style);
        popWindows.setClippingEnabled(false);
        popWindows.setOutsideTouchable(true);
        popWindows.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                popWindows.dismiss();
            }
        });

        Map<String,String> map = new HashMap<>();
        map.put("access_token",V8TokenManager.obtain());
        map.put("userId",LastLoginUserSP.getInstance(getContext()).getUserAccount());
        map.put("version","V1.0");
        OKHttpCustomUtils.get(URLs.BATCH_DATE_CHECK_URL,map, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                if (response.optInt("status")==0){
                    batchTodoItems = JSON.parseArray(response.optString("result"), BatchTodoItem.class);
                    if (batchTodoItems.size()>0){
                        todo_batch_list.setVisibility(View.VISIBLE);
                        empty_data.setVisibility(View.GONE);
                    }else {
                        todo_batch_list.setVisibility(View.GONE);
                        empty_data.setVisibility(View.VISIBLE);
                    }
                    Collections.sort(batchTodoItems,formsource);
                    adapter.setData(batchTodoItems);
                    adapter.notifyDataSetChanged();
                }else {
                    ToastUtil.toastAlerMessage(getActivity(),"数据错误！",500);
                }
            }

            @Override
            public void onBefore(Request request, int id) {
                view_delete.setClickable(false);
            }

            @Override
            public void onAfter(int id) {
                view_delete.setClickable(true);
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                view_delete.setClickable(true);
                ToastUtil.toastAlerMessage(getActivity(),"数据错误！",500);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==200){
            refreshTodoList(true,true);
        }
    }
}
