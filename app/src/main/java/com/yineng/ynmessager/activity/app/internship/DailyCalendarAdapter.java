package com.yineng.ynmessager.activity.app.internship;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.app.Internship.DailyReport;
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Created by 贺毅柳 on 2015/12/19 17:24.
 */
public class DailyCalendarAdapter extends RecyclerView.Adapter<DailyCalendarAdapter.ViewHolder> {
    private Context mContext;
    private Resources mResources;
    private LayoutInflater mInflater;
    private List<DailyReport> mData;
    private OnItemClickListener<ViewHolder> mOnItemClickListener;
    private Date mToDay = new Date();

    DailyCalendarAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_reportcalendar_daily_calendar, parent, false);
        final ViewHolder holder = new ViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                }
            }
        });

        return holder;
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        DailyReport report = getItem(position);

        Date date = report.getDate();
        if (null == date) {
            L.e(getClass(), "the date field of DailyReport is NULL!");
        }

        holder.date.setText(DateFormatUtils.format(date, "d"));
        if (DateUtils.isSameDay(mToDay, date)) {
            ((CardView) holder.itemView).setCardBackgroundColor(
                ResourcesCompat.getColor(mResources, R.color.button_bg_purple, null));

            holder.note.setText(R.string.internshipList_today);
            holder.note.setVisibility(View.VISIBLE);
            holder.date.setTextColor(ResourcesCompat.getColor(mResources, R.color.white, null));
        } else {
            ((CardView) holder.itemView).setCardBackgroundColor(
                ResourcesCompat.getColor(mResources, R.color.white, null));

            holder.note.setText("");
            holder.note.setVisibility(View.INVISIBLE);
            holder.date.setTextColor(ResourcesCompat.getColor(mResources, R.color.gray, null));
        }

        int tagImage;
        switch (report.getStatus()) {
            case InternshipReport.STATUS_DRAFT:
                tagImage = R.mipmap.report_draft;
                break;
            case InternshipReport.STATUS_EXPIRED:
                tagImage = R.mipmap.report_expired;
                break;
            case InternshipReport.STATUS_PENDING:
                tagImage = R.mipmap.report_pending;
                break;
            case InternshipReport.STATUS_REDO:
                tagImage = R.mipmap.report_redo;
                break;
            case InternshipReport.STATUS_PASSED:
                tagImage = R.mipmap.report_passed;
                break;
            default:
                tagImage = 0;
                break;
        }
        holder.tag.setImageResource(tagImage);
    }

    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    void setData(@Nullable List<DailyReport> data) {
        mData = data;
    }

    void setOnItemClickListener(OnItemClickListener<ViewHolder> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Nullable DailyReport getItem(int position) {
        return mData == null ? null : mData.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView note;
        ImageView tag;

        ViewHolder(View itemView) {
            super(itemView);

            date = (TextView) itemView.findViewById(R.id.reportCalendarDaily_img_calendarItem_date);
            note = (TextView) itemView.findViewById(R.id.reportCalendarDaily_img_calendarItem_note);
            tag = (ImageView) itemView.findViewById(R.id.reportCalendarDaily_img_calenderItem_tag);
        }
    }
}
