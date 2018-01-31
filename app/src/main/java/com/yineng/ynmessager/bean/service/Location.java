package com.yineng.ynmessager.bean.service;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

/**
 * Created by 贺毅柳 on 2016/3/23 15:32.
 */
public class Location implements Parcelable
{
    private int id;
    private String rsglSysUserId;
    private double longitude;
    private double latitude;
    private int radius;
    private String address;
    private boolean gpsOpen;
    private Date timestamp;

    public String getRsglSysUserId() {
        return rsglSysUserId;
    }

    public void setRsglSysUserId(String rsglSysUserId) {
        this.rsglSysUserId = rsglSysUserId;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public int getRadius()
    {
        return radius;
    }

    public void setRadius(int radius)
    {
        this.radius = radius;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public boolean isGpsOpen()
    {
        return gpsOpen;
    }

    public void setGpsOpen(boolean gpsOpen)
    {
        this.gpsOpen = gpsOpen;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }


    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.id);
        dest.writeString(this.rsglSysUserId);
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.latitude);
        dest.writeInt(this.radius);
        dest.writeString(this.address);
        dest.writeByte(gpsOpen ? (byte) 1 : (byte) 0);
        dest.writeLong(timestamp != null ? timestamp.getTime() : -1);
    }

    public Location() {}

    protected Location(Parcel in)
    {
        this.rsglSysUserId= in.readString();
        this.id = in.readInt();
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
        this.radius = in.readInt();
        this.address = in.readString();
        this.gpsOpen = in.readByte() != 0;
        long tmpTimestamp = in.readLong();
        this.timestamp = tmpTimestamp == -1 ? null : new Date(tmpTimestamp);
    }

    public static final Creator<Location> CREATOR = new Creator<Location>()
    {
        @Override
        public Location createFromParcel(Parcel source) {return new Location(source);}

        @Override
        public Location[] newArray(int size) {return new Location[size];}
    };

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("rsglSysUserId",rsglSysUserId)
                .append("longitude", longitude)
                .append("latitude", latitude)
                .append("radius", radius)
                .append("address", address)
                .append("gpsOpen", gpsOpen)
                .append("timestamp", timestamp)
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Location)) {
            return false;
        }

        Location location = (Location) o;

        return new EqualsBuilder()
                .append(id, location.id)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
