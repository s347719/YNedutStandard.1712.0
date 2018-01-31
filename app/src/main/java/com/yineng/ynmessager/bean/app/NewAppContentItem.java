package com.yineng.ynmessager.bean.app;

import com.yineng.ynmessager.view.tagCloudView.TagItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：具体内容请看DOC中V8应用模板接口文档
 */

public class NewAppContentItem {
    private String id ;
    private String parentComponentId;
    private boolean hasData;
    private int sort;
    private int level;
    private String title;
    private String subTitle;
    private String noContentMsg;
    private String templateId;
    private boolean hasJumpApp;
    private boolean isSearchModule;
    private boolean isTimeOut;
    private AppJumpInfo jumpAppInfo;
    private ArrayList<TagItem> dataList;
    private List<NewAppSearchDataitem> searchDataList;
    private List<AppContentParamsItem> params;

    public List<NewAppSearchDataitem> getSearchDataList() {
        return searchDataList;
    }

    public void setSearchDataList(List<NewAppSearchDataitem> searchDataList) {
        this.searchDataList = searchDataList;
    }

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



    public List<AppContentParamsItem> getParams() {
        return params;
    }

    public void setParams(List<AppContentParamsItem> params) {
        this.params = params;
    }
}
