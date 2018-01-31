package com.yineng.ynmessager.activity.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.util.DensityUtil;
import com.yineng.ynmessager.view.DragGridView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/6/23.
 */

public class GroupListAdapter extends BaseAdapter {
    private List<HashMap<String, List<NewMyApps>>> groupApp = new ArrayList<>();
    private List<HashMap<String, Object>> groupList;
    private Context mContext;
    private LayoutInflater layoutInflater;
    private GroupAppAdapter recentAppGridAdapter;
    //是否编辑状态
    private boolean isEdit;
    //需要删除的appId
    private String deleteAppId;
    //需要添加的appId
    private String addAppId;
    private GroupAppAdapter.OnCheckedChangeListener onCheckedChangeListener;
    private AdapterView.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnCheckedChangeListener(GroupAppAdapter.OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnUpdateGroupDateApp {
        void onUpdateGroupDate(NewMyApps app);
    }

    private OnUpdateGroupDateApp onUpdateGroupDateApp;

    public void setOnUpdateGroupDateApp(OnUpdateGroupDateApp onUpdateGroupDateApp) {
        this.onUpdateGroupDateApp = onUpdateGroupDateApp;
    }


    /**
     * 设置删除后的delete Id
     *
     * @param appId
     */
    public void setDeleteAppId(String appId) {
        this.deleteAppId = appId;
        this.addAppId = "";
        notifyDataSetChanged();
    }

    /**
     * 设置添加的Appid，让其Checked
     *
     * @param addAppId
     */
    public void setAddAppId(String addAppId) {
        this.addAppId = addAppId;
        this.deleteAppId = "";
        notifyDataSetChanged();
    }

    /**
     * 设置编辑状态
     *
     * @param edit
     */
    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }

    public GroupListAdapter(List<HashMap<String, Object>> groupList, List<HashMap<String, List<NewMyApps>>> groupApp, Context mContext) {
        this.groupApp = groupApp;
        this.groupList = groupList;
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return groupList.size();
    }

    @Override
    public Object getItem(int position) {
        return groupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        TextView titleTxt;
        GridView appGrid;

        public ViewHolder(View v) {
            titleTxt = (TextView) v.findViewById(R.id.group_title_txt);
            appGrid = (DragGridView) v.findViewById(R.id.group_app_grid);
        }
    }

    private ViewHolder mViewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_group_app, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, Object> groupTitle = groupList.get(position);
        String titleName = String.valueOf(groupTitle.get("typeName"));
        if (titleName.equals("未分类") && groupList.size() <= 1) {
            mViewHolder.titleTxt.setText("");
        } else {
            mViewHolder.titleTxt.setText(titleName);
        }
        //设置gridview
        final List<NewMyApps> apps = this.groupApp.get(position).get("apps");
        recentAppGridAdapter = new GroupAppAdapter(mContext, apps);
        mViewHolder.appGrid.setAdapter(recentAppGridAdapter);
        mViewHolder.appGrid.setPadding(0, DensityUtil.dip2px(mContext, 5), 0, DensityUtil.dip2px(mContext, 20));
        mViewHolder.appGrid.setVerticalSpacing(DensityUtil.dip2px(mContext, 15));
        recentAppGridAdapter.setEdit(isEdit); //设置是否是编辑状态
        recentAppGridAdapter.setAddAppId(this.addAppId);//设置添加的appId
        recentAppGridAdapter.setDeleteAppId(this.deleteAppId);//设置删除的appId
        //设置item点击事件
        if (onItemClickListener != null) {
            mViewHolder.appGrid.setOnItemClickListener(onItemClickListener);
        }
        if (onCheckedChangeListener != null) {
            recentAppGridAdapter.setOnCheckedChangeListener(onCheckedChangeListener);
        }

        mViewHolder.appGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NewMyApps app = apps.get(position);
                if (!isEdit) {
                    if (onUpdateGroupDateApp != null) {
                        onUpdateGroupDateApp.onUpdateGroupDate(app);
                    }
                }
            }
        });

        return convertView;
    }
}
