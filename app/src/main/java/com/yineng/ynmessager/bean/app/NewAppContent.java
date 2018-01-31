package com.yineng.ynmessager.bean.app;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/19.
 */

public class NewAppContent<T> {
    private String message;
    private int status;
    private List<T> result = new LinkedList<>();

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }
}
