package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by 贺毅柳 on 2015/12/19 17:32.
 */
public class DailyReport extends InternshipReport {
    public static final Parcelable.Creator<DailyReport> CREATOR = new Parcelable.Creator<DailyReport>() {
        @Override
        public DailyReport createFromParcel(Parcel source) {
            return new DailyReport(source);
        }

        @Override
        public DailyReport[] newArray(int size) {
            return new DailyReport[size];
        }
    };
    private Date date;

    public DailyReport() {
        this.type = REPORT_TYPE_DAILY;
    }

    public DailyReport(Parcel source) {
        super(source);
        long tmpDate = source.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(date != null ? date.getTime() : -1);
    }

    @Override public String toString() {
        return new ToStringBuilder(this).append("id", this.id)
            .append("date", DateFormatUtils.format(this.date, "yyyy/MM/dd"))
            .append("status", this.status)
            .append("require", this.require)
            .toString();
    }
}
