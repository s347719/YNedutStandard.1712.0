package com.yineng.ynmessager.bean.app;

import java.util.ArrayList;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：具体内容请看DOC中V8应用模板接口文档
 */

public class NewAppResult {

    private ArrayList<NewAppModuleItem> moduleInfoList;

    private NewAppPageContent pageContent;

    public NewAppPageContent getPageContent() {
        return pageContent;
    }

    public void setPageContent(NewAppPageContent pageContent) {
        this.pageContent = pageContent;
    }

    public ArrayList<NewAppModuleItem> getModuleInfoList() {
        return moduleInfoList;
    }

    public void setModuleInfoList(ArrayList<NewAppModuleItem> moduleInfoList) {
        this.moduleInfoList = moduleInfoList;
    }
}
