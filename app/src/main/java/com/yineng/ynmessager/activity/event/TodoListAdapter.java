package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * @author by 舒欢
 * @Created time: 2015/11/19 15:31
 * @Descreption：
 */
class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder>
{
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<TodoEvent> mData;
    private OnItemClickListener mOnItemClickListener;
    TodoListAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mLayoutInflater.inflate(R.layout.item_main_event_todo_listitem1, parent, false);
        View cardView = view.findViewById(R.id.cv_event_todo_cardview);
        final ViewHolder holder = new ViewHolder(view);
        cardView.setOnClickListener(new View.OnClickListener() {
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
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        TodoEvent todo = getItem(position);
        if (todo.isOnlyPcCheck()){
            holder.todo_alarm_image.setBackgroundResource(R.mipmap.event_todo_uncheck);
        }else {
            holder.todo_alarm_image.setBackgroundResource(R.mipmap.event_todo_check);
        }
        holder.cerate_name.setText(todo.getName());
        if (todo.isOnlyPcCheck()){
            holder.cerate_name.setTextColor(mContext.getResources().getColor(R.color.common_text_color));
        }else {
            holder.cerate_name.setTextColor(mContext.getResources().getColor(R.color.common_black_color));
        }

        holder.review_time.setText(mContext.getResources().getString(R.string.event_demand_review_reviewTime,todo.getCreateTime()));
        String preChecker = todo.getPreChecker();
        if (TextUtils.isEmpty(preChecker)||"null".equals(preChecker)){
            holder.review_person.setVisibility(View.GONE);
            holder.review_view.setVisibility(View.GONE);
            holder.review_show_view.setVisibility(View.GONE);
            if (todo.isOnlyPcCheck()){
                holder.has_review_person.setVisibility(View.VISIBLE);
                holder.only_hint_computer.setVisibility(View.VISIBLE);
            }else {
                holder.has_review_person.setVisibility(View.GONE);
            }
        }else {
            holder.has_review_person.setVisibility(View.VISIBLE);
            holder.review_person.setVisibility(View.VISIBLE);
            String time = "null".equals(todo.getReviewTime())|| todo.getReviewTime()==null  ? "无":todo.getReviewTime();
            holder.review_person.setText(mContext.getResources().getString(R.string.event_demand_review_preperson,preChecker)+"("+time+")");
            holder.review_image.setImageResource(R.mipmap.event_review_down);
            holder.review_view.setVisibility(View.GONE);
            holder.review_text.setText(todo.getReviewAdvice());
            holder.review_show_view.setVisibility(View.VISIBLE);
            if (todo.isOnlyPcCheck()){
                holder.only_hint_computer.setVisibility(View.VISIBLE);
            }else {
                holder.only_hint_computer.setVisibility(View.GONE);
            }
            holder.review_show_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.review_view.getVisibility()==View.VISIBLE){
                        holder.review_view.setVisibility(View.GONE);
                        holder.review_image.setImageResource(R.mipmap.event_review_down);
                    }else {
                        holder.review_view.setVisibility(View.VISIBLE);
                        holder.review_image.setImageResource(R.mipmap.event_review_up);
                    }
                }
            });
        }
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

    int getItemPostion(TodoEvent todoEvent) {
        if (mData == null || mData.size() <= 0) {
            return -1;
        }
        return mData.indexOf(todoEvent);
    }

    public void setItem(int pos,TodoEvent todoEvent) {
        mData.set(pos,todoEvent);
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
        View has_review_person;
        View only_hint_computer;
        View review_show_view;
        ImageView review_image;
        ImageView todo_alarm_image;
        View review_view;
        TextView review_text;
        TextView review_person;
        TextView review_time;
        TextView cerate_name;

        public ViewHolder(View itemView)
        {
            super(itemView);
            has_review_person = itemView.findViewById(R.id.has_review_person);
            only_hint_computer = itemView.findViewById(R.id.only_hint_computer);
            review_show_view = itemView.findViewById(R.id.review_show_view);
            review_image = (ImageView) itemView.findViewById(R.id.review_image);
            todo_alarm_image = (ImageView) itemView.findViewById(R.id.todo_alarm_image);
            review_view = itemView.findViewById(R.id.review_view);
            review_text = (TextView) itemView.findViewById(R.id.review_text);
            review_person = (TextView) itemView.findViewById(R.id.review_person);
            review_time = (TextView) itemView.findViewById(R.id.review_time);
            cerate_name = (TextView) itemView.findViewById(R.id.cerate_name);
        }
    }
}
