package com.yineng.ynmessager.view.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by 贺毅柳 on 2015/11/13 16:45.
 */
public interface OnItemLongClickListener<VH extends RecyclerView.ViewHolder>
{
    /**
     * {@link android.support.v7.widget.RecyclerView} 的Item项长按事件
     *
     * @param position   点击所在位置
     * @param viewHolder 当前点击item所对应的 {@link android.support.v7.widget.RecyclerView.ViewHolder}
     * @return 参考 {@link android.view.View.OnLongClickListener#onLongClick(View)}
     */
    boolean onItemLongClick(int position, VH viewHolder);
}
