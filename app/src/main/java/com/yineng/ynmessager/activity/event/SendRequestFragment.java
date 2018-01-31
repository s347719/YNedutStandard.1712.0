package com.yineng.ynmessager.activity.event;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.CordovaWebActivity;
import com.yineng.ynmessager.activity.app.X5WebAppActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.event.SendRequestEvent;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.CryptUtil;
import com.yineng.ynmessager.util.HttpUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
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
import java.util.Map;

/**
 * @author by 舒欢
 * @Created time: 2017/12/12
 * @Descreption：
 */

public class SendRequestFragment extends BaseEventFragment implements OnItemClickListener<SendRequestAdapter.ViewHolder> {


    private int mCurrentPage = 0;
    private int hint_send = R.string.empty_hint_send_request;
    private SendRequestAdapter mAdapter;


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new SendRequestAdapter(getApplicationContext());
        mAdapter.setOnItemClickListener(this);
        mRcy_list.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        refreshSendList(false, true);
    }

    @Override
    public void onLoadMore() {
        refreshSendList(false, false, false, mCurrentPage + 1);
    }

    @Override
    public void onRetry(XRecyclerView view) {
        refreshSendList(true, true);
    }


    public void refreshSendList(boolean aotuRfresh, boolean isTop) {
        refreshSendList(aotuRfresh, isTop, false, 0);
    }

    private void refreshSendList(boolean aotuRfresh, boolean isTop, boolean isForceRefresh, final int page) {
        loadEventDataByPageIndex(aotuRfresh, isTop, EVENT_SEND_TYPE, isForceRefresh, page);
    }

    @Override
    public void onTaskGetJson(String responseString, int page) {
        ArrayList<SendRequestEvent> dataList = null;
        try {
            JSONObject jsonResp = new JSONObject(responseString);
            JSONArray content = jsonResp.getJSONArray("result");
            //解析待办总数量
            int size = content.length();
            if (size > 0) {
                dataList = new ArrayList<>(size);
            }
            for (int i = 0; i < size; ++i) {
                JSONObject sendContent = content.getJSONObject(i);
                SendRequestEvent sendRequestEvent = new SendRequestEvent();
                sendRequestEvent.setId(sendContent.getString("id"));
                sendRequestEvent.setName(sendContent.getString("name"));
                sendRequestEvent.setUrl(sendContent.getString("url"));
                sendRequestEvent.setFormSource(sendContent.getString("formSource"));
                sendRequestEvent.setDefKey(sendContent.getString("defKey"));
                sendRequestEvent.setActDefId(sendContent.getString("actDefId"));

                dataList.add(sendRequestEvent);
            }
        } catch (JSONException e) {
            L.w(mTag, e.getMessage(), e);
            if (dataList != null) {
                dataList.clear();
            }
        }
        if (dataList != null) {
            Collections.sort(dataList, sendRequestEventComparator);
        }
        mAdapter.setData(dataList);
        mAdapter.notifyDataSetChanged();
        mCurrentPage = 0;
        mRcy_list.setNoMore(true);
    }

    /**
     * 将数据按来源排序
     */
    private Comparator<SendRequestEvent> sendRequestEventComparator = new Comparator<SendRequestEvent>() {
        @Override
        public int compare(SendRequestEvent o1, SendRequestEvent o2) {
            return o1.getFormSource().compareTo(o2.getFormSource());
        }
    };

    @Override
    public void onTaskFinish(boolean isTop) {
        if (mAdapter.getItemCount() > 0) {
            if (!LastLoginUserSP.getInstance(getActivity()).getEventSendRequestHint()) {
                event_send_hint.setVisibility(View.VISIBLE);
                send_request_image_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 删除提示
                        LastLoginUserSP.getInstance(getActivity()).saveEventSendRequestHint(true);
                        event_send_hint.setVisibility(View.GONE);
                    }
                });
            } else {
                event_send_hint.setVisibility(View.GONE);
            }
            mRcy_list.refreshComplete();
        } else {
            mRcy_list.refreshComplete();
            mRcy_list.setEmptyView(mEmptyView, hint_send);
        }
    }

    @Override
    public void onItemClick(int position, SendRequestAdapter.ViewHolder viewHolder) {
        // 点击发起申请
        SendRequestEvent item = mAdapter.getItem(position - 1);
        if (!TextUtils.isEmpty(item.getUrl())) {
            //   跳转
            startSendActivity(item.getFormSource(), item.getId(), item.getUrl(), item.getDefKey(),item.getActDefId(),item.getName());
        } else {
            ToastUtil.toastAlerMessageCenter(getActivity(), "数据错误", 500);
        }
    }

    private void startSendActivity(String formSource, String id, String smesisUrl, String defKey,String actDefid,String smesisName) {
        String url = null;
        Intent intent = new Intent();
        String V8_HOST_SERVICE_HOST = AppController.getInstance().CONFIG_YNEDUT_V8_SERVICE_HOST;
        switch (formSource) {
            case "0"://宏天OA
                LastLoginUserSP sp = LastLoginUserSP.getInstance(getActivity());
                String host = V8_HOST_SERVICE_HOST.substring(0, StringUtils.removeEnd(V8_HOST_SERVICE_HOST, "/").lastIndexOf("/")) + "/bpmx/weixin/login.ht";
                Map<String, String> params = new HashMap<>();
                // 宏天OA
                params.put("username", CryptUtil.encode(sp.getUserLoginAccount()));
                params.put("password", sp.getUserPassword());
                params.put("version", "V2.0");
                params.put("method", "getStartForm");
                params.put("defId", id);
                params.put("defKey", defKey);
                params.put("actDefId", actDefid);
                params.put("linked", "ms");
                url = HttpUtil.createGetUrl(host, params);

                intent.setClass(mParentActivity, X5WebAppActivity.class);
                intent.putExtra("Name", "OA");
                intent.putExtra(X5WebAppActivity.IS_OA, true);
                break;
            case "1":
                intent.setClass(mParentActivity, X5WebAppActivity.class);
                intent.putExtra("Name", "详情");
                String ynedutUrl = URLs.getYnedutUrl(getActivity(), token, id);
                url = MessageFormat.format(mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL, mApplication.mAppTokenStr, LastLoginUserSP.getInstance(getActivity()).getUserAccount(), ynedutUrl, "1");
                break;
            case "2":
                intent.setClass(mParentActivity, CordovaWebActivity.class);
                intent.putExtra("title",smesisName);
                String smesisUserId= LastLoginUserSP.getInstance(getActivity()).getSmesisUserId();
                if (StringUtils.isEmpty(smesisUserId)) {
                    EventActivity eventActivity= (EventActivity) getActivity();
                    eventActivity. initIdPastDialog("");
                    return;
                }

                url = URLs.getSmesisUrl(smesisUrl, token,smesisUserId);
                break;
            default:
                break;
        }
        if (!TextUtils.isEmpty(url)) {
            intent.putExtra("url", url);
            startActivity(intent);
        }
    }
}
