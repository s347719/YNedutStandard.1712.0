package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by 贺毅柳 on 2015/12/30 17:26.
 */
public class MonthHeadIndicator extends MonthIndicator
{
    public static final Creator<MonthHeadIndicator> CREATOR = new Creator<MonthHeadIndicator>()
    {
        @Override
        public MonthHeadIndicator createFromParcel(Parcel source) {return new MonthHeadIndicator(source);}

        @Override
        public MonthHeadIndicator[] newArray(int size) {return new MonthHeadIndicator[size];}
    };

    public MonthHeadIndicator() {}

    protected MonthHeadIndicator(Parcel in)
    {
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(date != null ? date.getTime() : -1);
    }

    /**
     * 只会返回null
     *
     * @return
     */
    @Override
    public List<DailyReport> getDailyReportList()
    {
        return null;
    }

    /**
     * 什么也不会做
     *
     * @param dailyReportList
     */
    @Override
    public void setDailyReportList(List<DailyReport> dailyReportList)
    {
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("date", DateFormatUtils.format(this.date, "yyyy"))
                .toString();
    }
}
