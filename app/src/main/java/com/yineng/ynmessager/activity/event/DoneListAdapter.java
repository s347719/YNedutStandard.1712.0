package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.view.ExpandableTextView;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * Created by 胡毅 on 2016/03/18 15:31.
 */
class DoneListAdapter extends RecyclerView.Adapter<DoneListAdapter.ViewHolder>
{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<TodoEvent> mData;
    private OnItemClickListener mOnItemClickListener;
    DoneListAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mLayoutInflater.inflate(R.layout.item_main_event_done_listitem, parent, false);
        View done_hole_view = view.findViewById(R.id.done_hole_view);
        final ViewHolder holder = new ViewHolder(view);
        done_hole_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null ) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position)
    {
        TodoEvent doneEventObj = getItem(position);
        if (doneEventObj.isOnlyPcCheck()){
            holder.done_only_computer.setVisibility(View.VISIBLE);
            holder.done_only_computer.setBackgroundColor(mContext.getResources().getColor(R.color.whitesmoke));
            holder.done_hole_view.setBackgroundColor(mContext.getResources().getColor(R.color.whitesmoke));

        }else {
            holder.done_only_computer.setVisibility(View.GONE);
            holder.done_only_computer.setBackgroundColor(mContext.getResources().getColor(R.color.white));
            holder.done_hole_view.setBackgroundColor(mContext.getResources().getColor(R.color.white));

        }

        holder.done_name.setText(doneEventObj.getName());
        holder.done_time.setText(mContext.getResources().getString(R.string.event_demand_review_reviewTime,doneEventObj.getCreateTime()));
        int color;
        int string;
        switch (doneEventObj.getStatus()){
            case -1:
                color = R.color.orange;
                string = R.string.event_done_refuse;
                break;
            case 0:
                color = R.color.orange;
                string = R.string.event_done_back;
                break;
            case 1:
                color = R.color.actionBar_bg;
                string = R.string.event_done_checking;
                break;
            case 2:
                color = R.color.green;
                string = R.string.event_done_over;
                break;
            default:
                color = R.color.actionBar_bg;
                string = R.string.event_done_back;
                break;

        }
        holder.done_status.setTextColor(mContext.getResources().getColor(color));
        holder.done_status.setText(mContext.getResources().getString(string));
        String reviewAdvice = doneEventObj.getReviewAdvice();
        if (TextUtils.isEmpty(reviewAdvice)|| "null".equals(reviewAdvice) || reviewAdvice==null){
            reviewAdvice = "无";
        }
        holder.expandable_text.setText(reviewAdvice+"("+doneEventObj.getReviewTime()+")");
    }


    public TodoEvent getItem(int position)
    {
        return mData.get(position);
    }

    @Override
    public int getItemCount()
    {
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<TodoEvent> data)
    {
        mData = data;
    }

    public List<TodoEvent> getData()
    {
        return mData;
    }

    public OnItemClickListener getOnItemClickListener()
    {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this.mOnItemClickListener = onItemClickListener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder
    {

        View done_only_computer;
        View done_hole_view;
        TextView expandable_text;
        TextView done_status;
        TextView done_name;
        TextView done_time;
        public ViewHolder(View itemView)
        {
            super(itemView);
            done_only_computer = itemView.findViewById(R.id.done_only_computer);
            done_hole_view = itemView.findViewById(R.id.done_hole_view);
            expandable_text = (TextView) itemView.findViewById(R.id.expandable_text);
            done_status  = (TextView) itemView.findViewById(R.id.done_status);
            done_name  = (TextView) itemView.findViewById(R.id.done_name);
            done_time  = (TextView) itemView.findViewById(R.id.done_time);
        }
    }
}
