package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Created by 贺毅柳 on 2015/12/15 19:26.
 */
public class InternshipAct implements Parcelable
{
    private String id;
    private String title;
    private String teacher;
    private String company;
    private Date startDate;
    private Date endDate;
    private boolean hasDaily;
    private boolean hasWeekly;
    private boolean hasMonthly;

    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public boolean isHasDaily()
    {
        return hasDaily;
    }

    public void setHasDaily(boolean hasDaily)
    {
        this.hasDaily = hasDaily;
    }

    public boolean isHasMonthly()
    {
        return hasMonthly;
    }

    public void setHasMonthly(boolean hasMonthly)
    {
        this.hasMonthly = hasMonthly;
    }

    public boolean isHasWeekly()
    {
        return hasWeekly;
    }

    public void setHasWeekly(boolean hasWeekly)
    {
        this.hasWeekly = hasWeekly;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public String getTeacher()
    {
        return teacher;
    }

    public void setTeacher(String teacher)
    {
        this.teacher = teacher;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.teacher);
        dest.writeString(this.company);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeLong(endDate != null ? endDate.getTime() : -1);
        dest.writeByte(hasDaily ? (byte) 1 : (byte) 0);
        dest.writeByte(hasWeekly ? (byte) 1 : (byte) 0);
        dest.writeByte(hasMonthly ? (byte) 1 : (byte) 0);
    }

    public InternshipAct() {}

    protected InternshipAct(Parcel in)
    {
        this.id = in.readString();
        this.title = in.readString();
        this.teacher = in.readString();
        this.company = in.readString();
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        long tmpEndDate = in.readLong();
        this.endDate = tmpEndDate == -1 ? null : new Date(tmpEndDate);
        this.hasDaily = in.readByte() != 0;
        this.hasWeekly = in.readByte() != 0;
        this.hasMonthly = in.readByte() != 0;
    }

    public static final Parcelable.Creator<InternshipAct> CREATOR = new Parcelable.Creator<InternshipAct>()
    {
        @Override
        public InternshipAct createFromParcel(Parcel source) {return new InternshipAct(source);}

        @Override
        public InternshipAct[] newArray(int size) {return new InternshipAct[size];}
    };

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof InternshipAct)) {
            return false;
        }

        InternshipAct that = (InternshipAct) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("teacher", teacher)
                .append("company", company)
                .append("startDate", startDate)
                .append("endDate", endDate)
                .append("hasDaily", hasDaily)
                .append("hasWeekly", hasWeekly)
                .append("hasMonthly", hasMonthly)
                .toString();
    }
}
