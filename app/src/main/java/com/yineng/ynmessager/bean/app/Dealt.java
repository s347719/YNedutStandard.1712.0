package com.yineng.ynmessager.bean.app;

import android.support.annotation.NonNull;

/**
 * Created by yn on 2017/7/27.
 */

public class Dealt implements Comparable{

    private String id; //待办id
    private String name; //待办名称
    private String count; //待办数量
    private int sequence; //待办排序

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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int seq) {
        this.sequence = seq;
    }

    /**
     * 自定义排序
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(@NonNull Object o) {
        Dealt dealt = (Dealt) o;
        String id = dealt.getId();
        return this.id.compareTo(id);
    }

    /**
     * 重写equals 只有id相同就位一个元素
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Dealt) {
            Dealt dealt = (Dealt) obj;
            return (id.equals(dealt.id));
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
