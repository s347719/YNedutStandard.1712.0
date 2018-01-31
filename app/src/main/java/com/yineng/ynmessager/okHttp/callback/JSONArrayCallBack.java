package com.yineng.ynmessager.okHttp.callback;

import com.zhy.http.okhttp.callback.Callback;


import org.json.JSONArray;

import okhttp3.Call;
import okhttp3.Response;

/**
 * @author by 舒欢
 *         Created time: 2017/11/8
 *         Descreption：
 */

public abstract class JSONArrayCallBack extends Callback<JSONArray> {


    @Override
    public JSONArray parseNetworkResponse(Response response, int id) throws Exception {
        return new JSONArray(response.body().string());
    }

    @Override
    public void onError(Call call, Exception e, int id) {
        if (call.isCanceled()){
            return;
        }
    }

}
