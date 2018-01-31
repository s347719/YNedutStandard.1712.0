package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by 贺毅柳 on 2015/12/21 11:16.
 */
public class MonthIndicator implements Parcelable
{
    public static final Creator<MonthIndicator> CREATOR = new Creator<MonthIndicator>()
    {
        @Override
        public MonthIndicator createFromParcel(Parcel source) {return new MonthIndicator(source);}

        @Override
        public MonthIndicator[] newArray(int size) {return new MonthIndicator[size];}
    };

    protected Date date;
    private List<DailyReport> dailyReportList;
    private boolean selected;

    public MonthIndicator() {}

    protected MonthIndicator(Parcel in)
    {
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        this.dailyReportList = in.createTypedArrayList(DailyReport.CREATOR);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(date != null ? date.getTime() : -1);
        dest.writeTypedList(dailyReportList);
    }

    public Date getDate()
    {
        return date;
    }

    @NonNull
    public void setDate(Date date)
    {
        this.date = date;
    }

    public List<DailyReport> getDailyReportList()
    {
        return dailyReportList;
    }

    public void setDailyReportList(List<DailyReport> dailyReportList)
    {
        this.dailyReportList = dailyReportList;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("date", DateFormatUtils.format(this.date, "yyyy/MM"))
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MonthIndicator that = (MonthIndicator) o;

        return new EqualsBuilder()
                .append(date, that.date)
                .append(dailyReportList, that.dailyReportList)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(date)
                .append(dailyReportList)
                .toHashCode();
    }
}
