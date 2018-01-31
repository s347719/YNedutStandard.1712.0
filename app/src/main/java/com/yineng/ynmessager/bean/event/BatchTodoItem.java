package com.yineng.ynmessager.bean.event;

/**
 * @author by 舒欢
 * @Created time: 2017/12/21
 * @Descreption：
 */

public class BatchTodoItem {

    private String id;
    private String name;
    private String formSource;
    private int todonum;

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

    public String getFormSource() {
        return formSource;
    }

    public void setFormSource(String formSource) {
        this.formSource = formSource;
    }

    public int getTodonum() {
        return todonum;
    }

    public void setTodonum(int todonum) {
        this.todonum = todonum;
    }
}
