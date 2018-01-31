package com.yineng.ynmessager.bean.event;

/**
 * Created by yineng on 2016/3/29.
 */
/**
 * 审批信息
 */
public class EventReviewInfo {
    //id
    private String id;
    //审批人
    private String reviewPerson;
    //审批时间 MM-dd HH:mm
    private String reviewTime;
    //审批意见
    private String reviewAdvice;
    //审批节点
    private String reviewNode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReviewAdvice() {
        return reviewAdvice;
    }

    public void setReviewAdvice(String reviewAdvice) {
        this.reviewAdvice = reviewAdvice;
    }

    public String getReviewNode() {
        return reviewNode;
    }

    public void setReviewNode(String reviewNode) {
        this.reviewNode = reviewNode;
    }

    public String getReviewPerson() {
        return reviewPerson;
    }

    public void setReviewPerson(String reviewPerson) {
        this.reviewPerson = reviewPerson;
    }

    public String getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(String reviewTime) {
        this.reviewTime = reviewTime;
    }
}
