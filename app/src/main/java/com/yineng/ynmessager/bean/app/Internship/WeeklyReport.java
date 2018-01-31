package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by 贺毅柳 on 2015/12/21 17:11.
 */
public class WeeklyReport extends InternshipReport
{
    public static final Creator<WeeklyReport> CREATOR = new Creator<WeeklyReport>() {
        @Override
        public WeeklyReport createFromParcel(Parcel source) {
            return new WeeklyReport(source);
        }

        @Override
        public WeeklyReport[] newArray(int size) {
            return new WeeklyReport[size];
        }
    };
    private int sequence;
    private Date rangeBegin;
    private Date rangeEnd;

    public WeeklyReport() {
        this.type = REPORT_TYPE_WEEKLY;
    }

    protected WeeklyReport(Parcel in) {
        super(in);
        this.sequence = in.readInt();
        long tmpRangeBegin = in.readLong();
        this.rangeBegin = tmpRangeBegin == -1 ? null : new Date(tmpRangeBegin);
        long tmpRangeEnd = in.readLong();
        this.rangeEnd = tmpRangeEnd == -1 ? null : new Date(tmpRangeEnd);
    }

    public Date getRangeBegin()
    {
        return rangeBegin;
    }

    public void setRangeBegin(Date rangeBegin)
    {
        this.rangeBegin = rangeBegin;
    }

    public Date getRangeEnd()
    {
        return rangeEnd;
    }

    public void setRangeEnd(Date rangeEnd)
    {
        this.rangeEnd = rangeEnd;
    }

    public int getSequence()
    {
        return sequence;
    }

    public void setSequence(int sequence)
    {
        this.sequence = sequence;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("status", status)
                .append("sequence", sequence)
                .append("rangeBegin", rangeBegin)
                .append("rangeEnd", rangeEnd)
                .toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeInt(sequence);
        dest.writeLong(rangeBegin != null ? rangeBegin.getTime() : -1);
        dest.writeLong(rangeEnd != null ? rangeEnd.getTime() : -1);
    }
}
