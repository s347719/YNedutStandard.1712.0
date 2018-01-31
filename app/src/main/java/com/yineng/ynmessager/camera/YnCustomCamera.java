package com.yineng.ynmessager.camera;

import android.app.Activity;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by 舒欢
 * Created time: 2017/4/13
 */
public class YnCustomCamera extends CordovaPlugin {

    public static String TAG = "YnCustomCamera";
    private static final int REQUEST_CODE = 5;

    private CallbackContext callbackContext;
    private JSONObject params;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {


        this.callbackContext = callbackContext;
        this.params = args.getJSONObject(0);

        if (action.equals("startCamera")){

            String name = "";
            String time = "";
            String address = "";
            Intent intent  = new Intent(cordova.getActivity(),CameraActivity.class);

            if (this.params.has("name")){
                name = this.params.getString("name");
            }
            if (this.params.has("time")){
                time = this.params.getString("time");
            }
            if (this.params.has("address")){
                address = this.params.getString("address");
            }
            intent.putExtra("name",name);
            intent.putExtra("time",time);
            intent.putExtra("address",address);
            if (this.cordova!=null){
                this.cordova.startActivityForResult((CordovaPlugin) this,intent,REQUEST_CODE);
            }
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == REQUEST_CODE){
            //返回得到经过base64编码的图片字符串
            if (resultCode == Activity.RESULT_OK && intent!=null){
                String imageString = intent.getStringExtra("imageString");
                this.callbackContext.success(imageString);
            }
        }

    }
}
