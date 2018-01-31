//***************************************************************
//*    2015-9-8  下午2:17:29
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.groupsession;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * @author 贺毅柳
 */
public class SharedFile implements Parcelable
{
    private String id;
    private String name;
    private String type;
    private long size;
    private String owner; // UserNo
    private String ownerName; // UserName
    private Date uploadTime;
    private int downloadProgress;
    private int upTime;
    public static final Parcelable.Creator<SharedFile> CREATOR = new Parcelable.Creator<SharedFile>()
    {
        @Override
        public SharedFile createFromParcel(Parcel in)
        {
            return new SharedFile(in);
        }

        @Override
        public SharedFile[] newArray(int size)
        {
            return new SharedFile[size];
        }
    };

    private SharedFile(Parcel in)
    {
        this.id = in.readString();
        this.name = in.readString();
        this.type = in.readString();
        this.size = in.readLong();
        this.owner = in.readString();
        this.ownerName = in.readString();
        long tmpUploadTime = in.readLong();
        this.uploadTime = (tmpUploadTime == -1 ? null : new Date(tmpUploadTime));
        this.downloadProgress = in.readInt();
        this.upTime = in.readInt();
    }

    public SharedFile()
    {

    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.type);
        dest.writeLong(this.size);
        dest.writeString(this.owner);
        dest.writeString(this.ownerName);
        dest.writeLong(this.uploadTime == null ? -1 : this.uploadTime.getTime());
        dest.writeInt(this.downloadProgress);
        dest.writeInt(this.upTime);
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof SharedFile)) {
            return false;
        }
        if (o == this) {
            return true;
        }

        SharedFile obj = (SharedFile) o;
        return new EqualsBuilder().append(this.id, obj.getId()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder()
                .append(this.id)
                .toHashCode();
    }

    public int getUpTime() {
        return upTime;
    }

    public void setUpTime(int upTime) {
        this.upTime = upTime;
    }

    /**
     * @return the id
     */
    public String getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @return the size
     */
    public long getSize()
    {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size)
    {
        this.size = size;
    }

    /**
     * @return the owner
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * @param owner the owner to set
     */
    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    /**
     * @return the uploadTime
     */
    public Date getUploadTime()
    {
        return uploadTime;
    }

    /**
     * @param uploadTime the uploadTime to set
     */
    public void setUploadTime(Date uploadTime)
    {
        this.uploadTime = uploadTime;
    }

    /**
     * @return the ownerName
     */
    public String getOwnerName()
    {
        return ownerName;
    }

    /**
     * @param ownerName the ownerName to set
     */
    public void setOwnerName(String ownerName)
    {
        this.ownerName = ownerName;
    }

    /**
     * @return the downloadProgress
     */
    public int getDownloadProgress()
    {
        return downloadProgress;
    }

    /**
     * @param downloadProgress the downloadProgress to set
     */
    public void setDownloadProgress(int downloadProgress)
    {
        this.downloadProgress = downloadProgress;
    }

}
