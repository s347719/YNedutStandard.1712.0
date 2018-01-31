package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.SendRequestEvent;
import com.yineng.ynmessager.bean.event.TodoEvent;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.List;

/**
 * @author by 舒欢
 * @Created time: 2017/12/14
 * @Descreption：
 */

public class SendRequestAdapter extends RecyclerView.Adapter<SendRequestAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<SendRequestEvent> mData;

    private OnItemClickListener<ViewHolder> mOnItemClickListener;


    SendRequestAdapter(Context context)
    {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setData(List<SendRequestEvent> data)
    {
        mData = data;
    }
    public List<SendRequestEvent> getData()
    {
        return mData;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.item_main_event_sendrequest_listitem, parent, false);
        View send_request_view = view.findViewById(R.id.send_request_view);
        final SendRequestAdapter.ViewHolder holder = new SendRequestAdapter.ViewHolder(view);
        send_request_view.setOnClickListener(new View.OnClickListener() {
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
    public void onBindViewHolder(ViewHolder holder, int position) {

        SendRequestEvent item = getItem(position);
        holder.send_request_text.setText(item.getName());

    }
    public SendRequestEvent getItem(int position)
    {
        return mData.get(position);
    }
    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    public void setOnItemClickListener(OnItemClickListener<ViewHolder> onItemClickListener)
    {
        this.mOnItemClickListener = onItemClickListener;
    }


    static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView send_request_text;
        public ViewHolder(View itemView)
        {
            super(itemView);
            send_request_text = (TextView) itemView.findViewById(R.id.send_request_text);
        }
    }


}
