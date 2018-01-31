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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.app.CheckMyApps;
import com.yineng.ynmessager.activity.app.NewAppFragment;
import com.yineng.ynmessager.bean.app.MyApp;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.util.L;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/6/22.
 */

public class HomeAppGridAdapter extends BaseAdapter {

    private LinkedList<NewMyApps> apps = new LinkedList<>();
    private List<NewMyApps> deleteApps = new ArrayList<>();
    private LayoutInflater layoutInflater;
    private Context mContext;
    private boolean isEdit;
    private boolean isHome = false;
    private int hidePosition = AdapterView.INVALID_POSITION;
    private ViewHolder mViewHolder = null;

    public LinkedList<NewMyApps> getApps() {
        return apps;
    }

    public interface OnAdapterClickListener {
        void onDeleteListener(List<NewMyApps> apps, int position);

        void onDragListener(NewMyApps app);
    }

    private OnAdapterClickListener adapterClickListener;

    public void setOnDeleteClickListener(OnAdapterClickListener adapterClickListener) {
        this.adapterClickListener = adapterClickListener;
    }

    public HomeAppGridAdapter(LinkedList<NewMyApps> apps, Context mContext) {
        //只显示前11个
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getIsRecommend()) {
                this.apps.add(apps.get(i));
            }
        }
        //通过seq排序
        Collections.sort(this.apps, new CheckMyApps.AppComparatorByTopSeq());
        List<Integer> removeIndex = new ArrayList<>();
        for (int i = 0; i < this.apps.size(); i++) {
            if (i >= NewAppFragment.MAX_RECOMMEND_COUNT) {
                removeIndex.add(i);
            }
        }
        for (int i : removeIndex) {
            this.apps.removeLast();
        }
        layoutInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
    }


    /**
     * 首页显示时才会用到
     *
     * @param apps
     * @param mContext
     * @param isHome
     */
    public HomeAppGridAdapter(LinkedList<NewMyApps> apps, Context mContext, boolean isHome) {
        this(apps, mContext);
        this.isHome = isHome;
        boolean hasNew = false;
        //查找有无新应用
        for (NewMyApps app : apps) {
            if (app.getIsNew() == 1&&!app.getIsRecommend()) {
                hasNew = true;
            }
        }
        if (isHome) {
            NewMyApps app = new NewMyApps();
            app.setName("更多应用");
            app.setIcon("more");
            if (hasNew) {
                app.setIsNew(1);
            } else {
                app.setIsNew(0);
            }
            this.apps.add(app);
        }
        //通过seq排序
        Collections.sort(this.apps, new CheckMyApps.AppComparatorByTopSeq());
    }

    /**
     * 增加新的item
     *
     * @param app
     */
    public void addNewItem(NewMyApps app) {
        //判断存不存在这个app
        boolean isExisted = false;
        for (NewMyApps addApp : apps) {
            if (addApp.getId().equals(app.getId())) {
                isExisted = true;
            }
        }
        if (!isExisted) {
            apps.add(app);
            notifyDataSetChanged();
        }
    }

    /**
     * 删除这个app
     * @param app
     */
    public void deleteItem(NewMyApps app) {
        //判断存不存在这个app
        boolean isExisted = false;
        for (NewMyApps addApp : apps) {
            if (addApp.getId().equals(app.getId())) {
                isExisted = true;
            }
        }
        if(isExisted){
            apps.remove(app);
            notifyDataSetChanged();
        }
    }


    /**
     * 更新位置
     *
     * @param apps
     */
    public void setNewItem(LinkedList<NewMyApps> apps) {
        this.apps.clear();
        //只显示前11个
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).getIsRecommend()) {
                this.apps.add(apps.get(i));
            }
        }
        boolean hasNew = false;
        //查找有无新应用
        for (NewMyApps app : apps) {
            if (app.getIsNew() == 1&&!app.getIsRecommend()) {
                hasNew = true;
            }
        }
        //通过seq排序
        Collections.sort(this.apps, new CheckMyApps.AppComparatorByTopSeq());
        List<Integer> removeIndex = new ArrayList<>();
        for (int i = 0; i < this.apps.size(); i++) {
            if (i >= NewAppFragment.MAX_RECOMMEND_COUNT) {
                removeIndex.add(i);
            }
        }
        for (int i : removeIndex) {
            this.apps.removeLast();
        }

        //加入更多应用
        if (isHome) {
            NewMyApps app = new NewMyApps();
            app.setName("更多应用");
            app.setIcon("more");
            if (hasNew) {
                app.setIsNew(1);
            } else {
                app.setIsNew(0);
            }
            this.apps.add(app);
        }

        notifyDataSetChanged();
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
        private View converView;
        ImageView homeIcon;
        ImageButton deleteBtn;
        RelativeLayout dropRelative;
        TextView titleTxt;
        ImageView iconNewFlag;

        ViewHolder(View convertView) {
            this.converView = convertView;
            initView();
        }

        private void initView() {
            homeIcon = (ImageView) converView.findViewById(R.id.home_icon_img);
            deleteBtn = (ImageButton) converView.findViewById(R.id.home_delete_btn);
            dropRelative = (RelativeLayout) converView.findViewById(R.id.drop_relative);
            titleTxt = (TextView) converView.findViewById(R.id.app_title);
            iconNewFlag = (ImageView) converView.findViewById(R.id.app_new_flag);
        }


    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_home_app, parent, false);
            mViewHolder = new ViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        //根据菜单ID来查找对应的资源图片
        int drawAbleID = mContext.getResources().getIdentifier(apps.get(position).getIcon(), "mipmap", mContext.getPackageName());
        if (drawAbleID != 0) {
            try {
                mViewHolder.homeIcon.setImageResource(drawAbleID);
            } catch (Resources.NotFoundException e) {
                L.e(this.getClass(), e.getMessage());
                NewMyApps.randomIcon(apps.get(position), mViewHolder.homeIcon);
            }
        } else {
            NewMyApps.randomIcon(apps.get(position), mViewHolder.homeIcon);
        }

        //最后一个更多应用
        if (null != apps.get(position).getIcon() && apps.get(position).getIcon().equals("more")) {
            mViewHolder.homeIcon.setImageResource(R.mipmap.icon_mor_app);
        }
        mViewHolder.titleTxt.setText(apps.get(position).getName());
        //hide时隐藏Icon
//        if (position != hidePosition) {
//            mViewHolder.recentIcon.setVisibility(View.VISIBLE);
//        } else {
//            mViewHolder.recentIcon.setVisibility(View.GONE);
//        }
        if (isEdit) {
            mViewHolder.deleteBtn.setVisibility(View.VISIBLE);
            mViewHolder.deleteBtn.setFocusable(false);
            mViewHolder.iconNewFlag.setVisibility(View.GONE);
        } else {
            mViewHolder.deleteBtn.setVisibility(View.GONE);
            //最新应用
            if (apps.get(position).getIsNew() == 1) {
                mViewHolder.iconNewFlag.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.iconNewFlag.setVisibility(View.GONE);
            }
        }
        //重新设置app的seq
        apps.get(position).setTopSequence(position + 1);
        //删除
        mViewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteApps.clear();
                if (apps.size() > 0) {
                    deleteApps.add(apps.get(position));
                }
                itemAnimation(parent.getChildAt(position), position, 0);
            }
        });

        return convertView;
    }

    /**
     * item的动画
     *
     * @param v
     * @param type 0:删除；1：新增
     */
    public void itemAnimation(final View v, final int position, final int type) {
        v.setClickable(false);
        ObjectAnimator animator = null;
        if (type == 1) {
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
                if (type == 0) {
                    if (adapterClickListener != null) {
                        adapterClickListener.onDeleteListener(deleteApps, position);
                    }
                    v.setAlpha(1);
                    v.setClickable(true);
                    removeView(position);
                } else {

                }
            }
        });
    }

    /**
     * 设置编辑状态
     *
     * @param isEdit
     */
    public void setEditState(boolean isEdit) {
        this.isEdit = isEdit;
        this.notifyDataSetChanged();
    }

    public void hideView(int pos) {
        hidePosition = pos;
        notifyDataSetChanged();
    }

    public void showHideView() {
        hidePosition = AdapterView.INVALID_POSITION;
        notifyDataSetChanged();
    }


    public void removeView(int pos) {
        apps.remove(pos);
        notifyDataSetChanged();
    }

    /**
     * 更新拖动时的gridView
     */
    public void swapView(int draggedPos, int destPos) {
        NewMyApps app = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //从前向后拖动，其他item依次前移
        if (draggedPos < destPos) {
            app = getItem(draggedPos);
            app.setTopSequence(destPos + 1);
            app.setUpdateTime(df.format(new Date()));
            apps.add(destPos + 1, app);
            apps.remove(draggedPos);
        }
        //从后向前拖动，其他item依次后移
        else if (draggedPos > destPos) {
            app = getItem(draggedPos);
            app.setTopSequence(destPos + 1);
            app.setUpdateTime(df.format(new Date()));
            apps.add(destPos, getItem(draggedPos));
            apps.remove(draggedPos + 1);
        }
        if (adapterClickListener != null) {
            adapterClickListener.onDragListener(app);
        }
        hidePosition = destPos;
        notifyDataSetChanged();
    }

}
