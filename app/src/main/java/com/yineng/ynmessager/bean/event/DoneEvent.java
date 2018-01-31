package com.yineng.ynmessager.bean.event;

/**
 * Created by yineng on 2016/3/17.
 * 我的已办数据
 */
public class DoneEvent {

    private String id;
    private String name;
    private String createTime;
    private String curChecker;
    private String preChecker;
    private String reviewAdvice;
    private String reviewTime;
    private int status;
    private boolean isOnlyPcView;
    private boolean isOnlyPcModify;
    private String modifyUrl;
    private String viewUrl;
    private String formSource;

    private boolean isExpand = false;

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCurChecker() {
        return curChecker;
    }

    public void setCurChecker(String curChecker) {
        this.curChecker = curChecker;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean getIsOnlyPcView() {
        return isOnlyPcView;
    }

    public void setIsOnlyPcView(boolean onlyPcView) {
        isOnlyPcView = onlyPcView;
    }

    public boolean getIsOnlyPcModify() {
        return isOnlyPcModify;
    }

    public void setIsOnlyPcModify(boolean onlyPcModify) {
        isOnlyPcModify = onlyPcModify;
    }

    public String getModifyUrl() {
        return modifyUrl;
    }

    public void setModifyUrl(String modifyUrl) {
        this.modifyUrl = modifyUrl;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String getFormSource() {
        return formSource;
    }

    public void setFormSource(String formSource) {
        this.formSource = formSource;
    }
}
