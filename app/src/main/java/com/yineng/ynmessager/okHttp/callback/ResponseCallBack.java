package com.yineng.ynmessager.okHttp.callback;


import com.yineng.ynmessager.okHttp.model.ResponseModel;
import com.zhy.http.okhttp.callback.Callback;

import org.json.JSONObject;

import okhttp3.Response;

public abstract class ResponseCallBack extends Callback<ResponseModel> {
	@Override
	public ResponseModel parseNetworkResponse(Response arg0, int arg1)
			throws Exception {
		ResponseModel response=new ResponseModel();
		response.setBady(new JSONObject(arg0.body().string()));
		response.setHeader(arg0.headers());
		return response;
	}
}
