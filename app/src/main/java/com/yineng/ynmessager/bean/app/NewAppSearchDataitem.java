package com.yineng.ynmessager.bean.app;

import com.yineng.ynmessager.view.tagCloudView.TagItem;

import java.util.ArrayList;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：
 */

public class NewAppSearchDataitem {


    /**
     * id : 5
     * parentComponentId : 4
     * hasData : true
     * sort : 1
     * level : 1
     * title : 本学期最近一次课堂考勤
     * subTitle : null
     * noContentMsg : null
     * templateId : null
     * jumpAppInfo : null
     * hasJumpApp : false
     * isSearchModule  : false
     * isTimeOut : false
     * searchDataList:[]
     */

    private String id;
    private String parentComponentId;
    private boolean hasData;
    private int sort;
    private int level;
    private String title;
    private String subTitle;
    private String noContentMsg;
    private String templateId;
    private AppJumpInfo jumpAppInfo;
    private boolean hasJumpApp;
    private boolean isSearchModule;
    private boolean isTimeOut;
    private ArrayList<TagItem> dataList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentComponentId() {
        return parentComponentId;
    }

    public void setParentComponentId(String parentComponentId) {
        this.parentComponentId = parentComponentId;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getNoContentMsg() {
        return noContentMsg;
    }

    public void setNoContentMsg(String noContentMsg) {
        this.noContentMsg = noContentMsg;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public AppJumpInfo getJumpAppInfo() {
        return jumpAppInfo;
    }

    public void setJumpAppInfo(AppJumpInfo jumpAppInfo) {
        this.jumpAppInfo = jumpAppInfo;
    }

    public boolean isHasJumpApp() {
        return hasJumpApp;
    }

    public void setHasJumpApp(boolean hasJumpApp) {
        this.hasJumpApp = hasJumpApp;
    }

    public boolean isSearchModule() {
        return isSearchModule;
    }

    public void setSearchModule(boolean searchModule) {
        isSearchModule = searchModule;
    }

    public boolean isTimeOut() {
        return isTimeOut;
    }

    public void setTimeOut(boolean timeOut) {
        isTimeOut = timeOut;
    }

    public ArrayList<TagItem> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<TagItem> dataList) {
        this.dataList = dataList;
    }
}
