package com.yineng.ynmessager.activity.app.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.util.L;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/6/23.
 */

public class GroupAppAdapter extends BaseAdapter {

    private LinkedList<NewMyApps> apps = new LinkedList<>();
    private Context mContext;
    private LayoutInflater layoutInflater;
    //是否编辑状态
    private boolean isEdit;
    //需要删除的appId
    private String deleteAppId = "";
    //需要添加的appId
    private String addAppId = "";

    public interface OnCheckedChangeListener {
        void checkedChangeListener(int position, View view, NewMyApps app);
    }

    private OnCheckedChangeListener onCheckedChangeListener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    /**
     * 设置删除后的delete Id
     *
     * @param appId
     */
    public void setDeleteAppId(String appId) {
        this.deleteAppId = appId;
        for (NewMyApps app:apps){
            if(app.getId().equals(appId)){
                app.setIsRecommend(false);
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 设置添加的Appid，让其Checked
     *
     * @param addAppId
     */
    public void setAddAppId(String addAppId) {
        this.addAppId = addAppId;
        for (NewMyApps app:apps){
            if(app.getId().equals(addAppId)){
                app.setIsRecommend(true);
            }
        }
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

    public GroupAppAdapter(Context context, List<NewMyApps> apps) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        this.apps.clear();
        //排序
        Collections.sort(apps,new CheckMyApps.AppComparatorBySeq());
        for (int i = 0; i < apps.size(); i++) {
            this.apps.add(apps.get(i));
        }

    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public NewMyApps getItem(int position) {
        return apps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        ImageView recentIcon;
        ImageView mCheckbox;
        ImageView iconNewFlag;
        TextView titleTxt;

        public ViewHolder(View v) {
            recentIcon = (ImageView) v.findViewById(R.id.recent_icon_img);
            mCheckbox = (ImageView) v.findViewById(R.id.recent_check);
            titleTxt = (TextView) v.findViewById(R.id.app_title);
            iconNewFlag = (ImageView) v.findViewById(R.id.icon_new_flag);
        }
    }

    private ViewHolder mViewHolder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int mPosition = position;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_recent_app, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        //根据菜单ID来查找对应的资源图片
        int drawAbleID = mContext.getResources().getIdentifier(apps.get(position).getIcon(), "mipmap", mContext.getPackageName());
        if (drawAbleID != 0) {
            try {
                mViewHolder.recentIcon.setImageResource(drawAbleID);
            } catch (Resources.NotFoundException e) {
                L.e(this.getClass(), e.getMessage());
                NewMyApps.randomIcon(apps.get(position),  mViewHolder.recentIcon);
            }
        } else {
            NewMyApps.randomIcon(apps.get(position),  mViewHolder.recentIcon);
        }
        mViewHolder.titleTxt.setText(apps.get(position).getName());
        //编辑状态
        if (isEdit) {
            mViewHolder.mCheckbox.setVisibility(View.VISIBLE);
            mViewHolder.iconNewFlag.setVisibility(View.GONE);
            //如果已经添加则显示灰色图标
            if (apps.get(position).getIsRecommend()) {
                mViewHolder.mCheckbox.setImageResource(R.mipmap.icon_app_checkbox_selected);
            }
        } else {
            mViewHolder.mCheckbox.setVisibility(View.GONE);
            //最新应用
            if(apps.get(position).getIsNew()==1){
                mViewHolder.iconNewFlag.setVisibility(View.VISIBLE);
            }else{
                mViewHolder.iconNewFlag.setVisibility(View.GONE);
            }
        }

        //修改check状态
       if(!apps.get(position).getIsRecommend()){
           mViewHolder.mCheckbox.setImageResource(R.mipmap.icon_app_checkbox_normal);
       }

        mViewHolder.mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCheckedChangeListener != null) {
                    onCheckedChangeListener.checkedChangeListener(mPosition, v, apps.get(mPosition));
                }
            }
        });

        return convertView;
    }
}
