package com.yineng.ynmessager.bean.service;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by 贺毅柳 on 2016/3/14 16:00.
 */
public class LocateConfig
{
    private Date lastUpdate;
    private boolean enable;
    private long interval = 0;
    private ArrayList<LocateItem> list;

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public ArrayList<LocateItem> getList() {
        return list;
    }

    public void setList(ArrayList<LocateItem> list) {
        this.list = list;
    }


}
