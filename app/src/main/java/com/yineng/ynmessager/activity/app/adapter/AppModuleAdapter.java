package com.yineng.ynmessager.activity.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBusinessDialogInterface;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.NewAppContentItem;
import com.yineng.ynmessager.bean.app.NewAppSearchDataitem;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.tagCloudView.TagCloudView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;

/**
 * Created by 舒欢
 * Created time: 2017/8/21
 * Descreption：
 */

public class AppModuleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mcontext;
    private LayoutInflater mLayoutInflater;
    private List<NewAppSearchDataitem> list;
    private NewMyAppsDao appDao;
    private AppBusinessDialogInterface appBusinessDialogInterface;
    //随机的颜色
    public static final int[] TITLE_ICON = {
            R.drawable.app_business_color_one,
            R.drawable.app_business_color_two,
            R.drawable.app_business_color_three,
            R.drawable.app_business_color_four,
            R.drawable.app_business_color_five,
            R.drawable.app_business_color_seven,
            R.drawable.app_business_color_six
    };


    public void setData( List<NewAppSearchDataitem> list){
        this.list = list;
    }

    AppModuleAdapter(Context context){
        this.mcontext = context;
        mLayoutInflater = LayoutInflater.from(context);
        appDao = new NewMyAppsTb(context);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppModuleAdapter.Normal_Holder(mLayoutInflater.inflate(R.layout.app_business_normal_item,parent,false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NewAppSearchDataitem newAppContentItem = list.get(position);
        ((AppModuleAdapter.Normal_Holder) holder).normal_title.setText(newAppContentItem.getTitle());
        if (StringUtils.isEmpty(newAppContentItem.getSubTitle())){
            ((AppModuleAdapter.Normal_Holder) holder).normal_subtitle.setVisibility(View.VISIBLE);
            ((AppModuleAdapter.Normal_Holder) holder).normal_subtitle.setText(newAppContentItem.getSubTitle());
        }else {
            ((AppModuleAdapter.Normal_Holder) holder).normal_subtitle.setVisibility(View.GONE);
        }
        if (newAppContentItem.isHasData()&& !newAppContentItem.isTimeOut()){
            ((AppModuleAdapter.Normal_Holder) holder).tag.setVisibility(View.VISIBLE);
            ((AppModuleAdapter.Normal_Holder) holder).tag.setTags(newAppContentItem.getDataList());
            ((AppModuleAdapter.Normal_Holder) holder).normal_fail.setVisibility(View.GONE);
            if (newAppContentItem.isHasJumpApp()){
                ((AppModuleAdapter.Normal_Holder) holder).normal_jump.setVisibility(View.VISIBLE);
                int drawAbleID = mcontext.getResources().getIdentifier(newAppContentItem.getJumpAppInfo().getAppIconId(), "mipmap", mcontext.getPackageName());
                ((AppModuleAdapter.Normal_Holder) holder).normal_icon.setBackgroundResource(drawAbleID);
            }else {
                ((AppModuleAdapter.Normal_Holder) holder).normal_jump.setVisibility(View.GONE);
                int logoBackground = TITLE_ICON[(int) (Math.random() * 7)];
                ((AppModuleAdapter.Normal_Holder) holder).normal_icon.setBackgroundResource(logoBackground);
            }
            ((AppModuleAdapter.Normal_Holder) holder).normal_view.setTag(position);
            ((AppModuleAdapter.Normal_Holder) holder).normal_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  在可以点击跳转到的时候点击事件
                    NewMyApps app = appDao.queryById(list.get((Integer) v.getTag()).getJumpAppInfo().getAppId());
                    if (app==null) {
                        return;
                    }
                    boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
                    CheckMyApps.getInstance(mcontext).JumpApp(app, isMenu);
                }
            });
        }else {
            ((AppModuleAdapter.Normal_Holder) holder).tag.setVisibility(View.GONE);
            ((AppModuleAdapter.Normal_Holder) holder).normal_fail.setVisibility(View.VISIBLE);
            ((AppModuleAdapter.Normal_Holder) holder).normal_fail.setTag(position);
            ((AppModuleAdapter.Normal_Holder) holder).fail_title.setText(newAppContentItem.getNoContentMsg());
            ((AppModuleAdapter.Normal_Holder) holder).normal_fail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  失败的时候点击重新获取
                    int pos  = (int) v.getTag();
                    loadNormalData(list.get(pos));
                }
            });
        }
    }
    /**
     * 重新加载返回失败的数据
     */
    private void loadNormalData(final NewAppSearchDataitem newAppContentItem) {
        Map<String,String> params = new HashMap<>();
        params.put("componentId", newAppContentItem.getId()+"");
        params.put("userId", LastLoginUserSP.getLoginUserNo(mcontext));
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        OKHttpCustomUtils.get(URLs.APP_MODULE_LIST_ITEM, params, new JSONObjectCallBack() {

            @Override
            public void onBefore(Request request, int id) {
                if (appBusinessDialogInterface !=null) {
                    appBusinessDialogInterface.start();
                }
            }

            @Override
            public void onResponse(JSONObject response, int id) {
                String status = response.optString("status");
                if ("0".equals(status)) {//成功
                    String result = response.optString("result");
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }
                    List<NewAppContentItem> contentItems = JSONArray.parseArray(result, NewAppContentItem.class);
                    if (contentItems != null) {
                        newAppContentItem.setDataList(contentItems.get(0).getDataList());
                        refreshListItem(newAppContentItem);
                    }
                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                ToastUtil.toastAlerMessageCenter(mcontext,"重新获取失败",500);
            }

            @Override
            public void onAfter(int id) {
                if (appBusinessDialogInterface !=null) {
                    appBusinessDialogInterface.finish();
                }
            }
        });
    }

    public void setLoadingListener(AppBusinessDialogInterface appBusinessDialogInterface){
        this.appBusinessDialogInterface = appBusinessDialogInterface;
    }

    /**
     * 刷新当前点击失败的数据
     * @param newAppContentItem
     */
    private void refreshListItem(NewAppSearchDataitem newAppContentItem) {
        int pos = getItemPostion(newAppContentItem);
        if (pos < 0) {
            return;
        }
        setItem(pos,newAppContentItem);
        notifyItemChanged(pos);
    }

    /**
     * 获取当前数据再list中位置
     * @param item
     * @return
     */
    private int getItemPostion(NewAppSearchDataitem item) {
        if (list == null || list.size() <= 0) {
            return -1;
        }
        return list.indexOf(item);
    }

    /**
     * 重置位置中的数据内容
     * @param pos
     * @param item
     */
    public void setItem(int pos,NewAppSearchDataitem item) {
        list.set(pos,item);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    class Normal_Holder extends RecyclerView.ViewHolder{

        private TextView normal_icon,normal_title,normal_subtitle,fail_title;
        private View normal_jump,normal_fail,normal_view;
        private TagCloudView tag;

        public Normal_Holder(View itemView) {
            super(itemView);
            normal_icon = (TextView) itemView.findViewById(R.id.normal_icon);
            normal_title = (TextView) itemView.findViewById(R.id.normal_title);
            normal_subtitle = (TextView) itemView.findViewById(R.id.normal_subtitle);
            fail_title = (TextView) itemView.findViewById(R.id.fail_title);
            normal_jump = itemView.findViewById(R.id.normal_jump);
            normal_fail =itemView.findViewById(R.id.normal_fail);
            normal_view =itemView.findViewById(R.id.normal_view);
            tag = (TagCloudView) itemView.findViewById(R.id.tag);
        }
    }


}
