package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import java.util.Date;

/**
 * Created by 贺毅柳 on 2015/12/22 11:45.
 */
public class MonthlyReport extends InternshipReport {
    public static final Creator<MonthlyReport> CREATOR = new Creator<MonthlyReport>() {
        @Override public MonthlyReport createFromParcel(Parcel source) {
            return new MonthlyReport(source);
        }

        @Override public MonthlyReport[] newArray(int size) {
            return new MonthlyReport[size];
        }
    };
    private boolean isHead;
    private Date date;

    public MonthlyReport() {
        this.type = REPORT_TYPE_MONTHLY;
    }

    protected MonthlyReport(Parcel in) {
        super(in);
        this.isHead = in.readByte() != 0;
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isHead() {
        return isHead;
    }

    public void setHead(boolean head) {
        isHead = head;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.isHead ? (byte) 1 : (byte) 0);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
    }
}
