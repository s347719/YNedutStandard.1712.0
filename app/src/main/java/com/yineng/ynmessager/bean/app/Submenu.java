package com.yineng.ynmessager.bean.app;

/**
 * Created by yn on 2017/7/19.
 * 应用子菜单
 */

public class Submenu {
    private String id;
    private String sortFieldMap;
    private String name;
    private String route;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }
}
