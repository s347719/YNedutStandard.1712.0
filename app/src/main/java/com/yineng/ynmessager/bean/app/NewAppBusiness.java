package com.yineng.ynmessager.bean.app;

/**
 * Created by 舒欢
 * Created time: 2017/8/14
 * Descreption：APP应用中业务逻辑中返回的数据
 */

public class NewAppBusiness {

    /**
     * message : null
     * status : 0
     * result
     */

    private String message;
    private int status;
    private NewAppResult result;

    public NewAppResult getResult() {
        return result;
    }

    public void setResult(NewAppResult result) {
        this.result = result;
    }

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
}
