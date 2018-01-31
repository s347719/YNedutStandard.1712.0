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
import com.yineng.ynmessager.bean.app.Internship.InternshipReport;
import com.yineng.ynmessager.bean.app.Internship.WeeklyReport;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by 贺毅柳 on 2015/12/21 17:02.
 */
public class WeeklyCalendarAdapter extends RecyclerView.Adapter<WeeklyCalendarAdapter.ViewHolder> {
    private List<WeeklyReport> mData;
    private Context mContext;
    private LayoutInflater mInflater;
    private Resources mResources;
    private OnItemClickListener<ViewHolder> mOnItemClickListener;
    private Date mCurrent = new Date();

    WeeklyCalendarAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    static boolean isInRangeOfWeeks(Date now, Date start, Date end) {
        long _now = now.getTime();
        return _now >= start.getTime() && _now < end.getTime();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_reportcalendar_weekly_calendar, parent, false);
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
        WeeklyReport report = getItem(position);

        if (isInRangeOfWeeks(mCurrent, report.getRangeBegin(), report.getRangeEnd())) {
            ((CardView) holder.itemView).setCardBackgroundColor(
                ResourcesCompat.getColor(mResources, R.color.button_bg_purple, null));

            holder.note.setText(R.string.reportCalendar_thisWeek);
            holder.note.setVisibility(View.VISIBLE);
            holder.week.setTextColor(ResourcesCompat.getColor(mResources, R.color.white, null));
            holder.range.setTextColor(ResourcesCompat.getColor(mResources, R.color.white, null));
        } else {
            ((CardView) holder.itemView).setCardBackgroundColor(
                ResourcesCompat.getColor(mResources, R.color.white, null));

            holder.note.setText("");
            holder.note.setVisibility(View.INVISIBLE);
            holder.week.setTextColor(ResourcesCompat.getColor(mResources, R.color.gray, null));
            holder.range.setTextColor(ResourcesCompat.getColor(mResources, R.color.gray, null));
        }

        holder.week.setText(mContext.getString(R.string.reportCalendar_weekOfYear, report.getSequence()));

        String rangeBegin = DateFormatUtils.format(report.getRangeBegin(), "MM/d");
        String rangeEnd = DateFormatUtils.format(report.getRangeEnd(), "MM/d");
        holder.range.setText(mContext.getString(R.string.reportCalendar_rangeOfWeek, rangeBegin, rangeEnd));

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

    WeeklyReport getItem(int position) {
        return mData.get(position);
    }

    void setData(@Nullable List<WeeklyReport> data) {
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener<ViewHolder> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * @return 找不到返回-1
     */
    int getCurrentWeekPosition() {
        List<WeeklyReport> data = mData;

        if (data != null) {
            for (WeeklyReport report : data) {
                if (report.isSameDate()) {
                    return data.indexOf(report);
                }
            }
        }

        return -1;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView note;
        TextView week;
        TextView range;
        ImageView tag;

        public ViewHolder(View itemView) {
            super(itemView);

            note = (TextView) itemView.findViewById(R.id.reportCalendarWeekly_img_calendarItem_note);
            week = (TextView) itemView.findViewById(R.id.reportCalendarWeekly_img_calendarItem_week);
            range = (TextView) itemView.findViewById(R.id.reportCalendarWeekly_img_calendarItem_range);
            tag = (ImageView) itemView.findViewById(R.id.reportCalendarWeekly_img_calenderItem_tag);
        }
    }
}
