package com.yineng.ynmessager.view.recyclerview;

import android.support.v7.widget.RecyclerView;

/**
 * Created by 贺毅柳 on 2015/11/13 16:37.
 */
public interface OnItemClickListener<VH extends RecyclerView.ViewHolder>
{
    /**
     * {@link android.support.v7.widget.RecyclerView} 的Item项点击事件
     *
     * @param position   点击所在位置
     * @param viewHolder 当前点击item所对应的 {@link android.support.v7.widget.RecyclerView.ViewHolder}
     */
    void onItemClick(int position, VH viewHolder);
}
