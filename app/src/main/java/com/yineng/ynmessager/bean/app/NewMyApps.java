package com.yineng.ynmessager.bean.app;


import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.yineng.ynmessager.R;

import java.util.List;

/**
 * Created by yn on 2017/6/21.
 */

public class NewMyApps implements Comparable {

    private String id;
    private String sortFieldMap;
    private String name;
    private int source;
    private String appTypeId;
    private String appTypeName;
    private String icon;
    private boolean isRecommend;
    private int sequence;
    private int topSequence;
    private int typeSequence;
    private String url;
    private int isNew;
    private String lastUseDate;
    private String submenu;
    private String updateTime;
    private int userDelete;


    public static final int TYPE_YNEDUT_APP = 1;
    public static final int TYPE_SMESIS_APP = 2;
    public static final int TYPE_THIRD_APP = 3;
    public static final int TYPE_REPORT_APP=4;
    public static final int TYPE_OA_APP = 5;

    public static void randomIcon(NewMyApps item, ImageView icon) {
        if (item.getIcon().equals("third_default")) {//第三方应用
            // 随机设置LOGO
            icon.setImageResource(R.mipmap.myapp_thirdapp);
        } else {
            icon.setImageResource(R.mipmap.myapp_icon4);
        }
    }

    public int getTypeSequence() {
        return typeSequence;
    }

    public void setTypeSequence(int typeSequence) {
        this.typeSequence = typeSequence;
    }

    public int getTopSequence() {
        return topSequence;
    }

    public void setTopSequence(int topSequence) {
        this.topSequence = topSequence;
    }

    public int getUserDelete() {
        return userDelete;
    }

    public void setUserDelete(int userDelete) {
        this.userDelete = userDelete;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSubmenu() {
        return submenu;
    }

    public void setSubmenu(String submenu) {
        this.submenu = submenu;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSortFieldMap() {
        return sortFieldMap;
    }

    public void setSortFieldMap(String sortFieldMap) {
        this.sortFieldMap = sortFieldMap;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getAppTypeId() {
        return appTypeId;
    }

    public void setAppTypeId(String appTypeId) {
        this.appTypeId = appTypeId;
    }

    public String getAppTypeName() {
        return appTypeName;
    }

    public void setAppTypeName(String appTypeName) {
        this.appTypeName = appTypeName;
    }

    public String getLastUseDate() {
        return lastUseDate;
    }

    public void setLastUseDate(String lastUseDate) {
        this.lastUseDate = lastUseDate;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public boolean getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(boolean isRecommend) {
        this.isRecommend = isRecommend;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 自定义排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NonNull Object o) {
        NewMyApps app = (NewMyApps) o;
        String id = app.getId();
        return this.id.compareTo(id);
    }

    /**
     * 重写equals 只有id相同就位一个元素
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NewMyApps) {
            NewMyApps app = (NewMyApps) obj;
            return (id.equals(app.id));
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        String id = getId();
        return id.hashCode();
    }
}
