package com.yineng.ynmessager.activity.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.AppBusinessDialogInterface;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.app.AppContentParamsItem;
import com.yineng.ynmessager.bean.app.NewAppContentItem;
import com.yineng.ynmessager.bean.app.NewAppSearchDataitem;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.tagCloudView.TagCloudView;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * Created by 舒欢
 * Created time: 2017/8/16
 * Descreption：应用逻辑的适配
 */

public class AppBusinessAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 1;//平常布局
    private static final int TYPE_SEARCH_MODULR = 2;//带有滑动的布局
    private Context mcontext;
    private LayoutInflater mLayoutInflater;
    private List<NewAppContentItem> list;
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

    public AppBusinessAdapter(Context context) {
        this.mcontext = context;
        mLayoutInflater = LayoutInflater.from(context);
        appDao = new NewMyAppsTb(context);

    }

    public void setData(List<NewAppContentItem> list) {
        this.list = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_NORMAL:
                return new Normal_Holder(mLayoutInflater.inflate(R.layout.app_business_normal_item, parent, false));
            case TYPE_SEARCH_MODULR:
                return new Module_Holder(mLayoutInflater.inflate(R.layout.app_business_module_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        NewAppContentItem contentItem = list.get(position);
        if (holder instanceof Normal_Holder) {
            ((Normal_Holder) holder).normal_title.setText(contentItem.getTitle());
            if (StringUtils.isEmpty(contentItem.getSubTitle())) {
                //判断是否有标题
                ((Normal_Holder) holder).normal_subtitle.setVisibility(View.VISIBLE);
                ((Normal_Holder) holder).normal_subtitle.setText(contentItem.getSubTitle());
            } else {
                ((Normal_Holder) holder).normal_subtitle.setVisibility(View.GONE);
            }
            if (contentItem.isHasData() && !contentItem.isTimeOut()) {
                //判断返回是否有数据和超时
                ((Normal_Holder) holder).tag.setVisibility(View.VISIBLE);
                ((Normal_Holder) holder).tag.setTags(contentItem.getDataList());
                ((Normal_Holder) holder).normal_fail.setVisibility(View.GONE);
                if (contentItem.isHasJumpApp()) {
                    //是否可以点击跳转并且根据跳转信息设定图标否则随机产生一个
                    ((Normal_Holder) holder).normal_jump.setVisibility(View.VISIBLE);
                    int drawAbleID = mcontext.getResources().getIdentifier(contentItem.getJumpAppInfo().getAppIconId(), "mipmap", mcontext.getPackageName());
                    ((Normal_Holder) holder).normal_icon.setBackgroundResource(drawAbleID);
                } else {
                    ((Normal_Holder) holder).normal_jump.setVisibility(View.GONE);
                    int logoBackground = TITLE_ICON[(int) (Math.random() * 7)];
                    ((Normal_Holder) holder).normal_icon.setBackgroundResource(logoBackground);
                }
                ((Normal_Holder) holder).normal_view.setTag(position);
                ((Normal_Holder) holder).normal_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  在可以点击跳转到的时候点击事件
                        NewMyApps app = appDao.queryById(list.get((Integer) v.getTag()).getJumpAppInfo().getAppId());
                        if (app == null) {
                            return;
                        }
                        boolean isMenu = !StringUtils.isEmpty(app.getSubmenu());
                        CheckMyApps.getInstance(mcontext).JumpApp(app, isMenu);
                    }
                });
            } else {
                ((Normal_Holder) holder).tag.setVisibility(View.GONE);
                ((Normal_Holder) holder).normal_fail.setVisibility(View.VISIBLE);
                ((Normal_Holder) holder).normal_fail.setTag(position);
                ((Normal_Holder) holder).fail_title.setText(contentItem.getNoContentMsg());
                ((Normal_Holder) holder).normal_fail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //  失败的时候点击重新获取
                        int pos = (int) v.getTag();
                        loadNormalData(list.get(pos));
                    }
                });
            }

        } else if (holder instanceof Module_Holder) {
            //搜索模式
            if (contentItem.isTimeOut() && !contentItem.isHasData()) {
                ((Module_Holder) holder).module_fail.setVisibility(View.VISIBLE);
                ((Module_Holder) holder).fail_title.setText(list.get(position).getNoContentMsg());
                ((Module_Holder) holder).module_params.setVisibility(View.GONE);
                ((Module_Holder) holder).module_fail.setTag(position);
                ((Module_Holder) holder).module_fail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 获取搜索列表
                        loadModuleParams(list.get(position));
                    }
                });

            } else {
                final List<AppContentParamsItem> params = contentItem.getParams();
                ((Module_Holder) holder).module_fail.setVisibility(View.GONE);
                ((Module_Holder) holder).module_params.setVisibility(View.VISIBLE);
                ((Module_Holder) holder).module_text_title.setText(params.get(0).getName());
                if (params.size() == 1) {
                    ((Module_Holder) holder).module_left.setVisibility(View.GONE);
                    ((Module_Holder) holder).module_right.setVisibility(View.GONE);
                } else if (params.size() == 2) {
                    ((Module_Holder) holder).module_left.setVisibility(View.GONE);
                    ((Module_Holder) holder).module_right.setVisibility(View.VISIBLE);

                } else {
                    ((Module_Holder) holder).module_left.setVisibility(View.VISIBLE);
                    ((Module_Holder) holder).module_right.setVisibility(View.VISIBLE);
                }
                int currem = 0;//params 开始的位置从0开始
                ((Module_Holder) holder).module_left.setTag(currem);
                ((Module_Holder) holder).module_right.setTag(currem);
                //左边的点击事件
                ((Module_Holder) holder).module_left.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tag = (int) v.getTag();
                        tag -= 1;
                        ((Module_Holder) holder).module_text_title.setText(params.get(tag).getName());
                        ((Module_Holder) holder).module_left.setTag(tag);
                        loadModuleData(list.get(position), tag);
                    }
                });
                //右边的点击事件
                ((Module_Holder) holder).module_right.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int tag = (int) v.getTag();
                        tag += 1;
                        ((Module_Holder) holder).module_text_title.setText(params.get(tag).getName());
                        ((Module_Holder) holder).module_left.setTag(tag);
                        loadModuleData(list.get(position), tag);
                    }
                });
                AppModuleAdapter moduleAdapter = new AppModuleAdapter(mcontext);
                moduleAdapter.setData(contentItem.getSearchDataList());
                moduleAdapter.setLoadingListener(new AppBusinessDialogInterface() {
                    @Override
                    public void start() {
                        if (appBusinessDialogInterface != null) {
                            appBusinessDialogInterface.start();
                        }
                    }

                    @Override
                    public void finish() {
                        if (appBusinessDialogInterface != null) {
                            appBusinessDialogInterface.finish();
                        }
                    }
                });
                ((Module_Holder) holder).recycle_list.setAdapter(moduleAdapter);

            }

        }

    }

    /**
     * 获取module中的搜索组件信息
     *
     * @param newAppContentItem
     */
    private void loadModuleParams(final NewAppContentItem newAppContentItem) {
        Map<String, String> params = new HashMap<>();
        params.put("componentId", newAppContentItem.getId() + "");
        params.put("userId", LastLoginUserSP.getLoginUserNo(mcontext));
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        String url = AppController.getInstance().CONFIG_YNEDUT_V8_URL + URLs.APP_MODULE_SEARCH_LIST;
        if (appBusinessDialogInterface != null) {
            appBusinessDialogInterface.start();
        }
        OKHttpCustomUtils.get(url, params, new JSONObjectCallBack() {

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                ToastUtil.toastAlerMessageCenter(mcontext, "重新获取失败", 500);
            }

            @Override
            public void onResponse(JSONObject jsonResp, int id) {
                if (jsonResp != null) {
                    L.e(jsonResp.toString());
                    String status = jsonResp.optString("status");
                    if ("0".equals(status)) {
                        //成功
                        String result = jsonResp.optString("result");
                        if (TextUtils.isEmpty(result)) {
                            return;
                        }
                        List<AppContentParamsItem> contentItems = JSONArray.parseArray(result, AppContentParamsItem.class);
                        if (contentItems != null) {
                            newAppContentItem.setParams(contentItems);
                            refreshListItem(newAppContentItem);
                        }
                        if (appBusinessDialogInterface != null) {
                            appBusinessDialogInterface.finish();
                        }
                    }
                }
            }
        });
    }

    /**
     * module组件中 更新内容
     *
     * @param newAppContentItem
     */
    private void loadModuleData(final NewAppContentItem newAppContentItem, int pos) {
        Map<String, String> params = new HashMap<>();
        params.put("componentId", newAppContentItem.getId() + "");
        params.put("userId", LastLoginUserSP.getLoginUserNo(mcontext));
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        params.put("param", newAppContentItem.getParams().get(pos).getId());
        String url = URLs.APP_CONTENT_ITEM;
        if (appBusinessDialogInterface != null) {
            appBusinessDialogInterface.start();
        }
        OKHttpCustomUtils.get(url, params, new JSONObjectCallBack() {

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                ToastUtil.toastAlerMessageCenter(mcontext, "重新获取失败", 500);
            }

            @Override
            public void onResponse(JSONObject jsonResp2, int id) {
                if (jsonResp2 != null) {
                    L.e(jsonResp2.toString());
                    String status = jsonResp2.optString("status");
                    if ("0".equals(status)) {
                        //成功
                        String result = jsonResp2.optString("result");
                        if (TextUtils.isEmpty(result)) {
                            return;
                        }
                        List<NewAppSearchDataitem> contentItems = JSONArray.parseArray(result, NewAppSearchDataitem.class);
                        if (contentItems != null) {
                            newAppContentItem.setSearchDataList(contentItems);
                            refreshListItem(newAppContentItem);
                        }
                        if (appBusinessDialogInterface != null) {
                            appBusinessDialogInterface.finish();
                        }
                    }
                }
            }
        });


    }

    /**
     * 重新加载返回失败的数据
     */
    private void loadNormalData(final NewAppContentItem newAppContentItem) {
        Map<String, String> params = new HashMap<>();
        params.put("componentId", newAppContentItem.getId() + "");
        params.put("userId", LastLoginUserSP.getLoginUserNo(mcontext));
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        String url = AppController.getInstance().CONFIG_YNEDUT_V8_URL + URLs.APP_MODULE_LIST_ITEM;
        if (appBusinessDialogInterface != null) {
            appBusinessDialogInterface.start();
        }
        OKHttpCustomUtils.get(url, params, new JSONObjectCallBack() {

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                ToastUtil.toastAlerMessageCenter(mcontext, "重新获取失败", 500);
            }

            @Override
            public void onResponse(JSONObject jsonResp3, int id) {
                if (jsonResp3 != null) {
                    L.e(jsonResp3.toString());
                    String status = jsonResp3.optString("status");
                    if ("0".equals(status)) {//成功
                        String result = jsonResp3.optString("result");
                        if (TextUtils.isEmpty(result)) {
                            return;
                        }
                        List<NewAppContentItem> contentItems = JSONArray.parseArray(result, NewAppContentItem.class);
                        if (contentItems != null) {
                            newAppContentItem.setDataList(contentItems.get(0).getDataList());
                            refreshListItem(newAppContentItem);
                        }
                        if (appBusinessDialogInterface != null) {
                            appBusinessDialogInterface.finish();
                        }
                    }
                }
            }
        });
    }

    public void setLoadingListener(AppBusinessDialogInterface appBusinessDialogInterface) {
        this.appBusinessDialogInterface = appBusinessDialogInterface;
    }

    /**
     * 刷新当前点击失败的数据
     *
     * @param newAppContentItem
     */
    private void refreshListItem(NewAppContentItem newAppContentItem) {
        int pos = getItemPostion(newAppContentItem);
        if (pos < 0) {
            return;
        }
        setItem(pos, newAppContentItem);
        notifyItemChanged(pos);
    }

    /**
     * 获取当前数据再list中位置
     *
     * @param item
     * @return
     */
    private int getItemPostion(NewAppContentItem item) {
        if (list == null || list.size() <= 0) {
            return -1;
        }
        return list.indexOf(item);
    }

    /**
     * 重置位置中的数据内容
     *
     * @param pos
     * @param item
     */
    public void setItem(int pos, NewAppContentItem item) {
        list.set(pos, item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        NewAppContentItem newAppContentItem = list.get(position);
        if (newAppContentItem.isSearchModule()) {
            return TYPE_SEARCH_MODULR;
        } else {
            return TYPE_NORMAL;
        }
    }

    /**
     * 普通类型
     */
    class Normal_Holder extends RecyclerView.ViewHolder {

        private TextView normal_icon, normal_title, normal_subtitle, fail_title;
        private View normal_jump, normal_fail, normal_view;
        private TagCloudView tag;

        public Normal_Holder(View itemView) {
            super(itemView);
            normal_icon = (TextView) itemView.findViewById(R.id.normal_icon);
            normal_title = (TextView) itemView.findViewById(R.id.normal_title);
            normal_subtitle = (TextView) itemView.findViewById(R.id.normal_subtitle);
            fail_title = (TextView) itemView.findViewById(R.id.fail_title);
            normal_jump = itemView.findViewById(R.id.normal_jump);
            normal_fail = itemView.findViewById(R.id.normal_fail);
            normal_view = itemView.findViewById(R.id.normal_view);
            tag = (TagCloudView) itemView.findViewById(R.id.tag);
        }
    }

    /**
     * 搜索组件类型
     */
    class Module_Holder extends RecyclerView.ViewHolder {

        private View module_params, module_fail;
        private TextView module_text_title, fail_title;
        private ImageButton module_left, module_right;
        private RecyclerView recycle_list;

        public Module_Holder(View itemView) {
            super(itemView);
            module_params = itemView.findViewById(R.id.module_params);
            module_fail = itemView.findViewById(R.id.module_fail);
            module_text_title = (TextView) itemView.findViewById(R.id.module_text_title);
            fail_title = (TextView) itemView.findViewById(R.id.fail_title);
            module_left = (ImageButton) itemView.findViewById(R.id.module_left);
            module_right = (ImageButton) itemView.findViewById(R.id.module_right);
            recycle_list = (RecyclerView) itemView.findViewById(R.id.recycle_list);

        }
    }

}
