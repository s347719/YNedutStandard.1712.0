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
import com.yineng.ynmessager.bean.app.Internship.MonthlyReport;
import com.yineng.ynmessager.view.recyclerview.OnItemClickListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Created by 贺毅柳 on 2015/12/22 11:17.
 */
public class MonthlyCalendarAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int VIEW_TYPE_HEAD = 0;
    public static final int VIEW_TYPE_ITEM = 1;
    private Context mContext;
    private LayoutInflater mInflater;
    private Resources mResources;
    private List<MonthlyReport> mData;
    private Date mCurrent = new Date();
    private OnItemClickListener<ViewHolderItem> mOnItemClickListener;

    MonthlyCalendarAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    static boolean isSameMonth(Date date1, Date date2) {
        return DateUtils.truncatedEquals(date1, date2, Calendar.MONTH);
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEAD:
                View headView = mInflater.inflate(R.layout.item_reportcalendar_monthly_calendarhead, parent, false);
                return new ViewHolderHead(headView);
            case VIEW_TYPE_ITEM:
                View itemView = mInflater.inflate(R.layout.item_reportcalendar_monthly_calendar, parent, false);
                final ViewHolderItem holder = new ViewHolderItem(itemView);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        if (mOnItemClickListener != null) {
                            mOnItemClickListener.onItemClick(holder.getLayoutPosition(), holder);
                        }
                    }
                });
                return holder;
            default:
                return null;
        }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MonthlyReport report = getItem(position);

        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEAD:
                ViewHolderHead headHolder = (ViewHolderHead) holder;
                headHolder.year.setText(DateFormatUtils.format(report.getDate(), "yyyy"));
                break;
            case VIEW_TYPE_ITEM:
                ViewHolderItem itemHolder = (ViewHolderItem) holder;

                Date date = report.getDate();
                if (isSameMonth(mCurrent, date)) {
                    ((CardView) itemHolder.itemView).setCardBackgroundColor(
                        ResourcesCompat.getColor(mResources, R.color.button_bg_purple, null));

                    itemHolder.note.setText(R.string.reportCalendar_thisMonth);
                    itemHolder.note.setVisibility(View.VISIBLE);
                    itemHolder.date.setTextColor(ResourcesCompat.getColor(mResources, R.color.white, null));
                } else {
                    ((CardView) itemHolder.itemView).setCardBackgroundColor(
                        ResourcesCompat.getColor(mResources, R.color.white, null));

                    itemHolder.note.setText(R.string.reportCalendar_thisMonth);
                    itemHolder.note.setVisibility(View.INVISIBLE);
                    itemHolder.date.setTextColor(ResourcesCompat.getColor(mResources, R.color.gray, null));
                }

                int month = DateUtils.toCalendar(date).get(Calendar.MONTH);
                itemHolder.date.setText(mContext.getString(R.string.reportCalendar_monthOfYear, month + 1));

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
                itemHolder.tag.setImageResource(tagImage);
                break;
        }
    }

    @Override public int getItemViewType(int position) {
        return getItem(position).isHead() ? VIEW_TYPE_HEAD : VIEW_TYPE_ITEM;
    }

    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    MonthlyReport getItem(int position) {
        return mData.get(position);
    }

    void setData(@Nullable List<MonthlyReport> data) {
        mData = data;
    }

    public void setOnItemClickListener(OnItemClickListener<ViewHolderItem> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    /**
     * @return 未找到返回-1
     */
    int getCurrentMonthPosition() {
        List<MonthlyReport> data = mData;

        if (data != null) {
            for (MonthlyReport report : data) {
                if (report.isSameDate()) {
                    return data.indexOf(report);
                }
            }
        }

        return -1;
    }

    static class ViewHolderItem extends RecyclerView.ViewHolder {
        TextView note;
        TextView date;
        ImageView tag;

        ViewHolderItem(View itemView) {
            super(itemView);

            note = (TextView) itemView.findViewById(R.id.reportCalendarMonthly_txt_indicatorItem_note);
            date = (TextView) itemView.findViewById(R.id.reportCalendarMonthly_txt_indicatorItem_date);
            tag = (ImageView) itemView.findViewById(R.id.reportCalendarMonthly_img_indicatorItem_tag);
        }
    }

    static class ViewHolderHead extends RecyclerView.ViewHolder {
        TextView year;

        ViewHolderHead(View itemView) {
            super(itemView);

            year = (TextView) itemView.findViewById(R.id.reportCalendarMonthly_img_indicatorHead);
        }
    }
}
