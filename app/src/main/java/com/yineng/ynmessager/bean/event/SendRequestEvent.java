package com.yineng.ynmessager.bean.event;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * @author by 舒欢
 * @Created time: 2017/12/14
 * @Descreption：
 */

public class SendRequestEvent implements Parcelable {


    /**
     *  "id": "YNBackLeave:7:22635",
     "name": "销假申请",
     "url": "路由信息",
     "formSource":"0"
     */

    private String id;
    private String name;
    private String url;
    private String formSource;
    private String defKey;
    private String actDefId;

    public static final Parcelable.Creator<SendRequestEvent> CREATOR = new Parcelable.Creator<SendRequestEvent>()
    {
        @Override
        public SendRequestEvent createFromParcel(Parcel in)
        {
            return new SendRequestEvent(in);
        }

        @Override
        public SendRequestEvent[] newArray(int size)
        {
            return new SendRequestEvent[size];
        }
    };


    private SendRequestEvent(Parcel in)
    {
        this.id = in.readString();
    }

    public SendRequestEvent(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeString(this.formSource);
        dest.writeString(this.defKey);
        dest.writeString(this.actDefId);


    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof SendRequestEvent)) {
            return false;
        }
        SendRequestEvent sendRequestEvent = (SendRequestEvent) o;
        if (!TextUtils.isEmpty(sendRequestEvent.getId())) {
            return sendRequestEvent.getId().equals(getId());
        }
        if (!TextUtils.isEmpty(sendRequestEvent.getName())) {
            return sendRequestEvent.getName().equals(getName());
        }
        if (!TextUtils.isEmpty(sendRequestEvent.getUrl())) {
            return sendRequestEvent.getUrl().equals(getUrl());
        }
        if (!TextUtils.isEmpty(sendRequestEvent.getFormSource())) {
            return sendRequestEvent.getFormSource().equals(getFormSource());
        }


        return false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getDefKey() {
        return defKey;
    }

    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    public String getActDefId() {
        return actDefId;
    }

    public void setActDefId(String actDefId) {
        this.actDefId = actDefId;
    }
}
