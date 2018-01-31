package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.yineng.ynmessager.util.FileUtil;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;

/**
 * Created by huyi on 2015/12/29.
 */
public class AttachFile implements Parcelable {
    private String mFileId;
    private String mFileName;
    private String mFileSize;
    private int mDownloadProgress;
    private File mAttachFile;
    private int mStatus;

    public AttachFile() {

    }

    public AttachFile(String mFileId, String mFileName, String mFileSize) {
        this.mFileId = mFileId;
        this.mFileName = mFileName;
        this.mFileSize = mFileSize;
        mAttachFile = FileUtil.getFileByName(mFileName);
    }

    protected AttachFile(Parcel in) {
        mFileId = in.readString();
        mFileName = in.readString();
        mDownloadProgress = in.readInt();
    }

    public static final Creator<AttachFile> CREATOR = new Creator<AttachFile>() {
        @Override
        public AttachFile createFromParcel(Parcel in) {
            return new AttachFile(in);
        }

        @Override
        public AttachFile[] newArray(int size) {
            return new AttachFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mFileId);
        dest.writeString(mFileName);
        dest.writeInt(mDownloadProgress);
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof AttachFile)) {
            return false;
        }
        if(o == this) {
            return true;
        }

        AttachFile obj = (AttachFile)o;
        return new EqualsBuilder().append(this.mFileId,obj.getmFileId()).isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(95,27).append(this.mFileId).toHashCode();
    }
    public File getmAttachFile() {
        return mAttachFile;
    }

    public void setmAttachFile(File mAttachFile) {
        this.mAttachFile = mAttachFile;
    }

    public int getmDownloadProgress() {
        return mDownloadProgress;
    }

    public void setmDownloadProgress(int mDownloadProgress) {
        this.mDownloadProgress = mDownloadProgress;
    }

    public String getmFileId() {
        return mFileId;
    }

    public void setmFileId(String mFileId) {
        this.mFileId = mFileId;
    }

    public String getmFileName() {
        return mFileName;
    }

    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
        mAttachFile = FileUtil.getFileByName(mFileName);
    }

    public String getmFileSize() {
        return mFileSize;
    }

    public void setmFileSize(String mFileSize) {
        this.mFileSize = mFileSize;
    }

    public void setmStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public int getmStatus() {
        return mStatus;
    }

    public boolean isExist() {
        if (mAttachFile == null) {
            mAttachFile = FileUtil.getFileByName(mFileName);
            return mAttachFile.exists();
        }
        return mAttachFile.exists();
    }

}
