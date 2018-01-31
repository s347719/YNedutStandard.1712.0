package com.yineng.ynmessager.okHttp.model;

import org.json.JSONObject;

import okhttp3.Headers;

/**
 * @author by 舒欢
 *         Created time: 2017/11/8
 *         Descreption：
 */

public class ResponseModel {
    private JSONObject bady;
    private Headers header;

    public JSONObject getBady() {
        return bady;
    }

    public void setBady(JSONObject bady) {
        this.bady = bady;
    }

    public Headers getHeader() {
        return header;
    }

    public void setHeader(Headers header) {
        this.header = header;
    }

}
