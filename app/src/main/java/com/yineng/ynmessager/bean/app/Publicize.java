package com.yineng.ynmessager.bean.app;

import android.support.annotation.NonNull;

public class Publicize implements Comparable{

    private String id;
    private String filePath;
    private String fileName;
    private String url;
    private String title;
    private int method;
    private String remark;
    private int orderBy;
    private String sortFieldMap;
    private int isRead;

    public int getIsRead() {
        return isRead;
    }

    public void setIsRead(int isRead) {
        this.isRead = isRead;
    }

    public String getSortFieldMap() {
        return sortFieldMap;
    }

    public void setSortFieldMap(String sortFieldMap) {
        this.sortFieldMap = sortFieldMap;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFilePath() {
        return filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return fileName;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }

    public void setMethod(int method) {
        this.method = method;
    }
    public int getMethod() {
        return method;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    public String getRemark() {
        return remark;
    }

    public void setOrderBy(int orderBy) {
        this.orderBy = orderBy;
    }
    public int getOrderBy() {
        return orderBy;
    }

    /**
     * 自定义排序
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NonNull Object o) {
        Publicize publicize = (Publicize) o;
        String id = publicize.getId();
        return this.id.compareTo(id);
    }

    /**
     * 重写equals 只有id相同就位一个元素
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Publicize) {
            Publicize publicize = (Publicize) obj;
            return (id.equals(publicize.id));
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