package com.yineng.plugins;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by fanjiaben on 2017/5/11.
 */

public class YNCordova extends CordovaPlugin {
    @Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        // 关闭cordova 贺毅柳写法 后面需要更改
        if ("close".equals(action)) {
            cordova.getActivity().finish();
            callbackContext.success();
            return true;
        }

        return false;
    }
}