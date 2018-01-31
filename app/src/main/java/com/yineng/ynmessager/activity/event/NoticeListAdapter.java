package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 贺毅柳 on 2015/11/13 15:23.
 *
 */
class NoticeListAdapter extends RecyclerView.Adapter<NoticeListAdapter.ViewHolder>
{
    private List<NoticeEvent> mData;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private OnItemClickListener mOnItemClickListener;

    NoticeListAdapter(ArrayList<NoticeEvent> datas,Context context) {
        mData = datas;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }
    @Override
    public NoticeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mLayoutInflater.inflate(R.layout.item_main_event_notice_listitem, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(final NoticeListAdapter.ViewHolder holder, int position)
    {
        NoticeEvent item = getItem(position);
        int iconRes;
        String itemIitle;
        //当通知状态不是平台通知时，不跳转网页
        if ( !NoticeEvent.NOTICE_MSG.equals(item.getMessageType()))
        {
            iconRes = R.mipmap.event_notice_sys;
            holder.title.setMaxLines(Integer.MAX_VALUE);
            holder.content.setMaxLines(Integer.MAX_VALUE);
            holder.enterIcon.setVisibility(View.INVISIBLE);
            itemIitle = item.getTitle();
            holder.icon_text.setText("系统消息");
            holder.timeStamp.setText(TimeUtil.getDateByMillisecond(item.getTimeStamp().getTime(), TimeUtil.FORMAT_DATETIME_MM_DD));
        } else
        {
            iconRes = R.mipmap.event_notice_announcement;
            holder.title.setMaxLines(2);
            holder.content.setMaxLines(3);
            holder.enterIcon.setVisibility(View.VISIBLE);
            itemIitle =item.getTitle();
            holder.icon_text.setText("通知公告");
            holder.timeStamp.setText(TimeUtil.getDateByMillisecond(item.getTimeStamp().getTime(), TimeUtil.FORMAT_DATA_DAY));
        }
        holder.icon.setImageResource(iconRes);
        holder.title.setText(itemIitle);
        if (item.isRead())
        {
            holder.red_tip.setVisibility(View.INVISIBLE);
            holder.title.setTypeface(Typeface.DEFAULT);
        } else
        {
            holder.red_tip.setVisibility(View.VISIBLE);
            holder.title.setTypeface(Typeface.DEFAULT_BOLD);
        }

        holder.content.setText(item.getContent());



        //增加是否含有图片和文件的标志 huyi 2016.1.11
        if ("1".equals(item.getHasPic())) {
            holder.picIcon.setVisibility(View.VISIBLE);
        } else {
            holder.picIcon.setVisibility(View.GONE);
        }
        if ("1".equals(item.getHasAttachment())) {
            holder.fileIcon.setVisibility(View.VISIBLE);
        } else {
            holder.fileIcon.setVisibility(View.GONE);
        }//end
    }

    public void setData(List<NoticeEvent> data) {
        this.mData = new ArrayList<>();
        this.mData = data;
    }

    public List<NoticeEvent> getData() {return this.mData;}

    public NoticeEvent getItem(int position) {return this.mData.get(position);}


    @Override
    public int getItemCount()
    {
        return this.mData == null ? 0 : this.mData.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        mOnItemClickListener = onItemClickListener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView picIcon;
        ImageView fileIcon;
        ImageView red_tip;
        RelativeLayout leftArea;
        ImageView icon;
        TextView title;
        TextView content;
        TextView timeStamp;
        ImageView enterIcon;
        TextView icon_text;
        ViewHolder(View itemView)
        {
            super(itemView);
            leftArea = (RelativeLayout) itemView.findViewById(R.id.main_rel_eventNotice_listItem_leftArea);
            icon = (ImageView) itemView.findViewById(R.id.main_img_eventNotice_listItem_icon);
            red_tip = (ImageView) itemView.findViewById(R.id.red_tip);
            title = (TextView) itemView.findViewById(R.id.main_txt_eventNotice_listItem_title);
            content = (TextView) itemView.findViewById(R.id.main_txt_eventNotice_listItem_content);
            timeStamp = (TextView) itemView.findViewById(R.id.main_txt_eventNotice_listItem_timeStamp);
            enterIcon = (ImageView) itemView.findViewById(R.id.main_img_eventNotice_listItem_enterIcon);
            picIcon = (ImageView) itemView.findViewById(R.id.main_img_eventNotice_listItem_pic);
            fileIcon = (ImageView) itemView.findViewById(R.id.main_img_eventNotice_listItem_file);
            icon_text = (TextView)itemView.findViewById(R.id.main_text_event_listitem_text);
        }
    }

}
