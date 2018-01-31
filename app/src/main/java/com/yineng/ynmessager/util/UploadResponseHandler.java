//***************************************************************
//*    2015-9-11  上午9:40:49
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.util;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author 胡毅
 *
 */
public class UploadResponseHandler extends AsyncHttpResponseHandler {

	/**
	 * 上传失败的标识
	 */
	public final static String mFailedSend = "upload_failed";
	String[] returnStrings = new String[3];

	public UploadResponseHandler() {
		super();
	}


	@Override
	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		L.v("上传成功 == " + statusCode);
		String responseData = new String(responseBody);
		if(StringUtils.isEmpty(responseData)){
			return;
		}
		try {
			JSONArray response;
			response = new JSONArray(responseData);
			JSONObject obj = response.getJSONObject(0);
			returnStrings[0] = obj.getString("fileId");
			returnStrings[1] = obj.getString("fileName");
			returnStrings[2] = obj.getString("fileType");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFailure(int statusCode, Header[] headers,
						  byte[] responseBody, Throwable error) {
		L.v("上传失败 == "+statusCode);
		if (responseBody == null) {
			return;
		}
		returnStrings[0] = mFailedSend;
	}

	@Override
	public void onProgress(long bytesWritten, long totalSize) {
		super.onProgress(bytesWritten, totalSize);
	}

}
