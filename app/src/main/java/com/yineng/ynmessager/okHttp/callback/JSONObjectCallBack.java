package com.yineng.ynmessager.okHttp.callback;

import okhttp3.Call;
import okhttp3.Response;

import org.json.JSONObject;

import com.zhy.http.okhttp.callback.Callback;

public abstract class JSONObjectCallBack extends Callback<JSONObject> {

	@Override
	public JSONObject parseNetworkResponse(Response arg0, int arg1)
			throws Exception {
		return new JSONObject(arg0.body().string());
	}

	@Override
	public void onError(Call call, Exception e, int i) {
		if (call.isCanceled()){
			return;
		}
	}

}
