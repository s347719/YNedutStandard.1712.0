//***************************************************************
//*    2015-6-24  下午3:49:11
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.event;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


/**
 * 异常事件的JavaBean
 *
 * @author 贺毅柳
 */
public class TodoEvent implements Parcelable
{
    private String id;
    private String name;
    private String createTime;
    private String preChecker;
    private String reviewAdvice;
    private String reviewTime;
    private String url;
    private String formSource;
    private String viewUrl;
    private boolean isOnlyPcCheck;
    private boolean isOnlyPcView;

    private int status;

    public static final Parcelable.Creator<TodoEvent> CREATOR = new Parcelable.Creator<TodoEvent>()
    {
        @Override
        public TodoEvent createFromParcel(Parcel in)
        {
            return new TodoEvent(in);
        }

        @Override
        public TodoEvent[] newArray(int size)
        {
            return new TodoEvent[size];
        }
    };
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.id);
        dest.writeInt(this.status);
        dest.writeString(this.name);
        dest.writeString(this.createTime);
        dest.writeString(this.preChecker);
        dest.writeString(this.reviewAdvice);
        dest.writeString(this.reviewTime);
        dest.writeString(this.url);
        dest.writeString(this.formSource);
        dest.writeString(this.viewUrl);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof TodoEvent)) {
            return false;
        }
        TodoEvent tempTodoEvent = (TodoEvent) o;
        if (!TextUtils.isEmpty(tempTodoEvent.getId())) {
            return tempTodoEvent.getId().equals(getId());
        }

        return false;
    }

    private TodoEvent(Parcel in)
    {
        this.id = in.readString();
        this.status = in.readInt();
        this.name = in.readString();
        this.createTime = in.readString();
        this.preChecker = in.readString();
        this.reviewAdvice = in.readString();
        this.reviewTime = in.readString();
        this.url = in.readString();
        this.viewUrl = in.readString();
        this.formSource = in.readString();
        this.isOnlyPcCheck = in.readByte() != 0;
        this.isOnlyPcView = in.readByte() != 0;

    }

    public TodoEvent()
    {
    }

    public boolean isOnlyPcView() {
        return isOnlyPcView;
    }

    public void setOnlyPcView(boolean onlyPcView) {
        isOnlyPcView = onlyPcView;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getPreChecker() {
        return preChecker;
    }

    public void setPreChecker(String preChecker) {
        this.preChecker = preChecker;
    }

    public String getReviewAdvice() {
        return reviewAdvice;
    }

    public void setReviewAdvice(String reviewAdvice) {
        this.reviewAdvice = reviewAdvice;
    }

    public String getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(String reviewTime) {
        this.reviewTime = reviewTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFormSource() {
        return formSource;
    }

    public void setFormSource(String formSource) {
        this.formSource = formSource;
    }

    public boolean isOnlyPcCheck() {
        return isOnlyPcCheck;
    }

    public void setOnlyPcCheck(boolean onlyPcCheck) {
        isOnlyPcCheck = onlyPcCheck;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }


}
