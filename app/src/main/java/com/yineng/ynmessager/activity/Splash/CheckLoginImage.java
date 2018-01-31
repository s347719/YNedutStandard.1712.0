package com.yineng.ynmessager.activity.Splash;

import android.graphics.Bitmap;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.view.agentwebx5.interfae.IFileUploadChooser;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by yn on 2017/6/27.
 * 检查图片的工厂方法
 */

public class CheckLoginImage extends CheckPopImageFactory {
    public static String mTag = CheckLoginImage.class.getSimpleName();



    @Override
    public void downLoadPopImage() {
        final SaveImageAsyncTask saveImageAsyncTask = new SaveImageAsyncTask(getmContext(), getImageInfo(), LOGIN);
        String token = V8TokenManager.obtain();
        String url = URLs.THIRD_DOWNLOAD_FILE;
        Map<String,String> params = new HashMap<>();
        params.put("fileId", getImageInfo().get("filePath"));
        params.put("access_token", token);
        L.i(mTag, "CheckLoginImage 引导页图片下载地址：" + url);
        OKHttpCustomUtils.get(url, params, new BitmapCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.e(mTag, "图片下载失败，错误码:" + e.getMessage());
            }

            @Override
            public void onResponse(Bitmap response, int id) {
                if (response!=null){
                    saveImageAsyncTask.execute(response);
                }
            }
        });

    }

    @Override
    public boolean checkNewImage() {
        String url = URLs.POPULARIZE_PICTURE_PUSH;
        Map<String,String> params = new HashMap<>();
        String v8Version = "V1.0";
        String token = V8TokenManager.obtain();
        String number = "M0002"; //图片位置，表示在启动页面
        int total = 1;//获取多少张照片
        String personStart = "0";
        params.put("version", v8Version);
        params.put("access_token", token);
        params.put("number", number);
        params.put("personStart", personStart);
        params.put("total", total+"");
        Log.i(mTag, "检测登录图片：" + url + "?" + params.toString());
        OKHttpCustomUtils.get(url, params, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                try {

                    JSONArray resultArr = response.getJSONArray("result");
                    if (resultArr == null || resultArr.length() <= 0) {
                        SaveImageAsyncTask.deleteImage(SaveImageAsyncTask.SAVE_LOGIN);
                        getLastUser().deletePopImage(LastLoginUserSP.LOGIN_LOCAL_IMAGE_ID);
                        return;
                    }
                    JSONObject info = resultArr.getJSONObject(0);
                    String image_id = info.getString("id");
                    String filePath = info.getString("filePath");
                    String url = info.getString("url");

                    //判断是否是新的启动页或者本地要没有图片
                    if (getLastUser().hasImageId(info.toString(), LastLoginUserSP.LOGIN_LOCAL_IMAGE_ID) || !SaveImageAsyncTask.hasImage(SaveImageAsyncTask.SAVE_LOGIN)) {
                        HashMap<String, String> imageInfo = new HashMap<>();
                        imageInfo.put("id", image_id);
                        imageInfo.put("url", url);
                        imageInfo.put("filePath", filePath);
                        setImageInfo(imageInfo);
                        downLoadPopImage();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    //获取不到图片删除图片
                    SaveImageAsyncTask.deleteImage(SaveImageAsyncTask.SAVE_LOGIN);
                    getLastUser().deletePopImage(LastLoginUserSP.LOGIN_LOCAL_IMAGE_ID);
                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);

            }
        });

        return false;
    }
}