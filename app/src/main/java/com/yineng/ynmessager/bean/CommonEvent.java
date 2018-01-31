package com.yineng.ynmessager.bean;

/**
 * Created by yineng
 * Created time: 2017/3/30
 */
public class CommonEvent {

    private int what;
    private Object obj;
    public CommonEvent(int what, Object obj) {
        super();
        this.what = what;
        this.obj = obj;
    }

    public int getWhat() {
        return what;
    }

    public void setWhat(int what) {
        this.what = what;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

}
