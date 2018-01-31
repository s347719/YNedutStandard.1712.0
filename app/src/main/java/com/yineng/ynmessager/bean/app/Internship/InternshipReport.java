package com.yineng.ynmessager.bean.app.Internship;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by 贺毅柳 on 2015/12/19 17:32.
 */
public abstract class InternshipReport implements Parcelable {
    public static final int REPORT_TYPE_DAILY = 1;
    public static final int REPORT_TYPE_WEEKLY = 2;
    public static final int REPORT_TYPE_MONTHLY = 3;
    public static final int REPORT_TYPE_DEFAULT = 0;
    public static final String LOCAL_DRAFT_KEY_PREFIX = "InternShipReportDraft_";

    /**
     * <p>草稿状态</p>
     * 本地状态，服务器不会返回
     */
    public static final int STATUS_DRAFT = 5;
    /**
     * <p>偷懒/未提交状态</p>
     */
    public static final int STATUS_EXPIRED = 1;
    /**
     * <p>待审核状态</p>
     */
    public static final int STATUS_PENDING = 2;
    /**
     * <p>重做状态</p>
     */
    public static final int STATUS_REDO = 3;
    /**
     * <p>通过状态</p>
     */
    public static final int STATUS_PASSED = 4;

    /**
     * <p>默认状态</p>
     */
    public static final int STATUS_DEFAULT = 0;

    protected int type = REPORT_TYPE_DEFAULT;
    protected String id;
    protected int status = STATUS_DEFAULT;
    protected String require;
    protected boolean isSameDate;

    public InternshipReport() {
    }

    protected InternshipReport(Parcel in) {
        this.type = in.readInt();
        this.id = in.readString();
        this.status = in.readInt();
        this.require = in.readString();
        this.isSameDate = in.readByte() != 0;
    }

    @NonNull public static String getTypeString(int type) {
        String str = "REPORT_TYPE_DEFAULT";

        switch (type) {
            case REPORT_TYPE_DAILY:
                str = "REPORT_TYPE_DAILY";
                break;
            case REPORT_TYPE_WEEKLY:
                str = "REPORT_TYPE_WEEKLY";
                break;
            case REPORT_TYPE_MONTHLY:
                str = "REPORT_TYPE_MONTHLY";
                break;
        }

        return str;
    }

    public int getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRequire() {
        return require;
    }

    public void setRequire(String require) {
        this.require = require;
    }

    public boolean isSameDate() {
        return isSameDate;
    }

    public void setSameDate(boolean isSameDate) {
        this.isSameDate = isSameDate;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.id);
        dest.writeInt(this.status);
        dest.writeString(this.require);
        dest.writeByte(this.isSameDate ? (byte) 1 : (byte) 0);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof InternshipReport)) {
            return false;
        }

        InternshipReport that = (InternshipReport) o;

        return new EqualsBuilder().append(id, that.id).isEquals();
    }

    @Override public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    @Override public String toString() {
        return new ToStringBuilder(this).append("id", id).toString();
    }
}
