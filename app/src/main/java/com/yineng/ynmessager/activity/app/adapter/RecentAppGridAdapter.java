package com.yineng.ynmessager.activity.app.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.util.L;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;

/**
 * Created by yn on 2017/6/23.
 */

public class RecentAppGridAdapter extends BaseAdapter {

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

    public LinkedList<NewMyApps> getApps() {
        return apps;
    }

    /**
     * 设置删除后的delete
     *
     * @param app
     */
    public void deleteApp(NewMyApps app) {
        boolean isDelete = false;
        int index = 0;
        for (int i = 0; i < this.apps.size(); i++) {
            if (apps.get(i).getId().equals(app.getId())) {
                isDelete = true;
                index = i;
            }
        }
        if (isDelete) {
            this.apps.remove(index);
            addAppId = "";
            notifyDataSetChanged();
        }
    }

    /**
     * 添加新的app
     *
     * @param app
     */
    public void addApp(NewMyApps app) {
        boolean hasApp = false;
        for (NewMyApps mApp : this.apps) {
            //首先要是没有这个app，不能是推荐应用，最后判断有没有使用时间
            if (mApp.getId().equals(app.getId()) || StringUtils.isEmpty(mApp.getLastUseDate())) {
                hasApp = true;
            }
        }
        if (!hasApp) {
            this.apps.add(app);
            notifyDataSetChanged();
        }
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
     * 设置编辑状态
     *
     * @param edit
     */
    public void setEdit(boolean edit) {
        isEdit = edit;
        notifyDataSetChanged();
    }


    /**
     * 设置添加的Appid，让其Checked
     *
     * @param addAppId
     */
    public void setAddAppId(String addAppId) {
        this.deleteAppId = "";
        this.addAppId = addAppId;
        notifyDataSetChanged();
    }

    public RecentAppGridAdapter(Context context, LinkedList<NewMyApps> apps) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
        //只添加前8个并且必须有使用时间
        this.apps.clear();
        for (int i = 0; i < apps.size(); i++) {
            if (i < 8 && apps.get(i).getLastUseDate() != null && !apps.get(i).getIsRecommend()) {
                this.apps.add(apps.get(i));
            }
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
        TextView titleTxt;

        public ViewHolder(View v) {
            recentIcon = (ImageView) v.findViewById(R.id.recent_icon_img);
            mCheckbox = (ImageView) v.findViewById(R.id.recent_check);
            titleTxt = (TextView) v.findViewById(R.id.app_title);
        }
    }

    private ViewHolder mViewHolder;

    /**
     * 更新位置
     *
     * @param apps
     */
    public void setNewItem(LinkedList<NewMyApps> apps) {
        this.apps.clear();
        for (int i = 0; i < apps.size(); i++) {
            if (i < 8 && !StringUtils.isEmpty(apps.get(i).getLastUseDate()) && !apps.get(i).getIsRecommend()) {
                this.apps.add(apps.get(i));
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final int myPosition = position;
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
        } else {
            mViewHolder.mCheckbox.setVisibility(View.GONE);
        }

        mViewHolder.mCheckbox.setImageResource(R.mipmap.icon_app_checkbox_normal);
        //取消删除的check
        if (apps.get(position).getId().equals(deleteAppId)) {
            mViewHolder.mCheckbox.setImageResource(R.mipmap.icon_app_checkbox_normal);
        }
//
//        //新增的App check
        if (apps.get(position).getId().equals(addAppId)) {
            mViewHolder.mCheckbox.setImageResource(R.mipmap.icon_app_checkbox_selected);
        }

        mViewHolder.mCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemAnimation(parent.getChildAt(position), position);
            }
        });

        return convertView;
    }


    /**
     * item的动画
     *
     * @param v
     */
    private void itemAnimation(final View v, final int position) {
        final float alpha = v.getAlpha();
        v.setClickable(false);
        ObjectAnimator animator;
        if (alpha == 0) {
            animator = ObjectAnimator.ofFloat(v, "alpha", 1f);
        } else {
            animator = ObjectAnimator.ofFloat(v, "alpha", 0f);
        }


        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(animator);
        animatorSet.setDuration(300);
        animatorSet.start();

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (alpha == 1) {
                    if (onCheckedChangeListener != null) {
                        onCheckedChangeListener.checkedChangeListener(position, v, apps.get(position));
                    }
                    v.setAlpha(1);
                    v.setClickable(true);
                } else {

                }
            }
        });
    }


}
