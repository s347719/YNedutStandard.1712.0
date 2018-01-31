package com.yineng.ynmessager.activity.app.internship;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.Internship.MonthHeadIndicator;
import com.yineng.ynmessager.bean.app.Internship.MonthIndicator;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by 贺毅柳 on 2015/12/19 14:47.
 */
public class DailyIndicatorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_MONTH = 0;
    public static final int VIEW_TYPE_YEARHEAD = 1;
    private Context mContext;
    private LayoutInflater mInflater;
    private Resources mResources;
    private List<MonthIndicator> mData;
    private OnItemClickListener<GeneralViewHolder> mOnItemClickListener;

    DailyIndicatorAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        final RecyclerView.ViewHolder holder;
        if (viewType == VIEW_TYPE_MONTH) {
            itemView = mInflater.inflate(R.layout.item_reportcalendar_daily_indicator, parent, false);
            holder = new GeneralViewHolder(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        int position = holder.getLayoutPosition();
                        mOnItemClickListener.onItemClick(position, (GeneralViewHolder) holder);
                    }
                }
            });
        } else {
            itemView = mInflater.inflate(R.layout.item_reportcalendar_daily_indicatorhead, parent, false);
            holder = new HeadViewHolder(itemView);
        }
        return holder;
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder _holder, int position) {
        int viewType = getItemViewType(position);
        MonthIndicator item = getItem(position);

        if (viewType == VIEW_TYPE_MONTH) {
            DailyIndicatorAdapter.GeneralViewHolder holder = (DailyIndicatorAdapter.GeneralViewHolder) _holder;
            holder.date.setText(DateFormatUtils.format(item.getDate(), "MM"));
            if (item.isSelected()) {
                holder.date.setBackgroundColor(ResourcesCompat.getColor(mResources, R.color.white, null));
                holder.highLight.setVisibility(View.VISIBLE);
            } else {
                holder.date.setBackgroundColor(ResourcesCompat.getColor(mResources, R.color.lightgray, null));
                holder.highLight.setVisibility(View.INVISIBLE);
            }
        } else {
            DailyIndicatorAdapter.HeadViewHolder holder = (DailyIndicatorAdapter.HeadViewHolder) _holder;
            holder.head.setText(DateFormatUtils.format(item.getDate(), "yyyy"));
        }
    }

    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override public int getItemViewType(int position) {
        return getItem(position) instanceof MonthHeadIndicator ? VIEW_TYPE_YEARHEAD : VIEW_TYPE_MONTH;
    }

    List<MonthIndicator> getData() {
        return mData;
    }

    void setData(List<MonthIndicator> data) {
        mData = data;
    }

    MonthIndicator getItem(int position) {
        return mData.get(position);
    }

    void setOnItemClickListener(OnItemClickListener<GeneralViewHolder> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    static class GeneralViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        View highLight;

        GeneralViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.reportCalendarDaily_txt_indicatorItem_date);
            highLight = itemView.findViewById(R.id.reportCalendarDaily_view_indicatorItem_highLight);
        }
    }

    static class HeadViewHolder extends RecyclerView.ViewHolder {
        TextView head;

        public HeadViewHolder(View itemView) {
            super(itemView);

            head = (TextView) itemView.findViewById(R.id.reportCalendarDaily_txt_indicatorHeadItem_year);
        }
    }
}
