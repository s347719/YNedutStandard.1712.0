package com.yineng.ynmessager.bean.app;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：
 */

public class NewAppModuleItem {


    /**
     * id : 1
     * name : 个人成长
     * active : true
     */

    private String id;
    private String name;
    private boolean active;

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
