package com.yineng.ynmessager.okHttp;

import android.content.Context;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.PostFormBuilder;
import com.zhy.http.okhttp.callback.Callback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.File;
import java.util.Map;

/**
 * @author by 舒欢
 * Created time: 2017/11/8
 * Descreption：https://github.com/hongyangAndroid/okhttputils
 */

public class OKHttpCustomUtils {

    /**
     * 模拟GET表单（无参数，无header）
     *
     * @param url
     *            请求URL
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void get(String url, Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .build()//
                .execute(callback);
    }
    /**
     * 请求接口带有tag
     * @param url
     * @param tag
     * @param callback
     * @param <T>
     */
    public static <T> void get(String url,Context tag, Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .tag(tag)
                .build()//
                .execute(callback);
    }
    /**
     * 请求接口带有tag
     * @param url
     * @param tag
     * @param callback
     * @param <T>
     */
    public static <T> void get(String url,int tag, Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .tag(tag)
                .build()//
                .execute(callback);
    }
    /**
     * 模拟GET表单（有参数,无header）
     *
     * @param url
     *            请求URL
     * @param params
     *            参数
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void get(String url, Map<String, String> params,
                               Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .params(params)//
                .build()//
                .execute(callback);
    }


    public static <T> void get(String url, Map<String, String> params,Object tag,
                               Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .tag(tag)
                .params(params)//
                .build()//
                .execute(callback);
    }

    /**
     *
     * 模拟GET表单（有参数）
     *
     * @param url
     *            请求URL
     * @param headers
     *            请求header
     * @param params
     *            参数
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void get(String url, Map<String, String> headers,
                               Map<String, String> params, Callback<T> callback) {
        OkHttpUtils.get()//
                .url(url)//
                .headers(headers)//
                .params(params)//
                .build()//
                .execute(callback);
    }

    /**
     * 模拟POST表单（无参数,无header）
     *
     * @param url
     *            请求URL
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void post(String url, Callback<T> callback) {
        OkHttpUtils.post()//
                .url(url)//
                .build()//
                .execute(callback);
    }

    /**
     * 模拟POST表单（无参数,无header）
     *
     * @param url
     *            请求URL
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void post(String url,Context tag, Callback<T> callback) {
        OkHttpUtils.post()//
                .url(url)//
                .tag(tag)
                .build()//
                .execute(callback);
    }

    /**
     * 模拟POST表单（无参数）
     *
     * @param url
     *            请求URL
     *            请求Header
     * @param callback
     *            回调函数 （可根据自己需求自定义）
     */
    public static <T> void post(String url, Map<String, String> params,
                                Callback<T> callback) {
        OkHttpUtils.post()//
                .url(url)//
                .params(params)
                .build()//
                .execute(callback);
    }

    /**
     * post请求（无参数）
     *
     * @param context
     *            上下文
     * @param url
     *            请求URL
     * @param headers
     *            请求Header
     * @param callback
     *            回调函数 （可根据自己需求自定义）
     */
    public static <T> void post(Context context, String url,
                                Map<String, String> headers, Callback<T> callback) {
        OkHttpUtils.post()//
                .url(url)//
                .tag(context)//
                .headers(headers)//
                .build()//
                .execute(callback);
    }

    public static <T> void post(String url,
                                Map<String, String> params,Object context, Callback<T> callback){
        OkHttpUtils.post()//
                .url(url)//
                .tag(context)//
                .params(params)//
                .build()//
                .execute(callback);

    }
    /**
     * post表单形式上传文件
     * @param url
     * @param params
     * @param files
     * @param callback
     * @param <T>
     */
    public static <T> void post(String url,Map<String, String> params,String fileKey,Map<String, File> files, Callback<T> callback){
        OkHttpUtils.post()//
                .url(url)//
                .params(params)//
                .files(fileKey,files)
                .build()//
                .execute(callback);
    }


    /**
     * post请求
     *
     * @param context
     *            上下文
     * @param url
     *            请求URL
     * @param headers
     *            请求header
     * @param params
     *            参数
     * @param callback
     *            回调函数（可根据自己需求自定义）
     */
    public static <T> void post(Context context, String url,
                                Map<String, String> headers, Map<String, String> params,
                                Callback<T> callback) {
        OkHttpUtils.post()//
                .url(url)//
                .tag(context)//
                .headers(headers)//
                .params(params)//
                .build()//
                .execute(callback);
    }

    /**
     * post方式,单个文件上传（有参数）
     *
     * @param context
     *            上下文
     * @param url
     *            上传URL
     * @param headers
     *            请求header
     * @param keys
     *            上传文件key数组
     * @param file
     *            上传文件
     * @param params
     *            参数
     * @param callback
     *            回调函数
     */
    public static <T> void updateFile(Context context, String url,
                                      Map<String, String> headers, String keys, File file,
                                      Map<String, String> params, Callback<T> callback) {
        OkHttpUtils.post()//
                .addFile(keys, file.getName(), file)//
                .url(url)//
                .params(params)//
                .headers(headers)//
                .build()//
                .execute(callback);
    }

    /**
     * post方式,单个文件上传（无参数）
     *
     * @param context
     *            上下文
     * @param url
     *            上传URL
     * @param headers
     *            请求header
     * @param key
     *            上传文件key
     * @param file
     *            上传文件
     * @param callback
     *            回调函数
     */
    public static <T> void updateFile(Context context, String url,
                                      Map<String, String> headers, String key, File file,
                                      Callback<T> callback) {
        OkHttpUtils.post()//
                .addFile(key, file.getName(), file)//
                .url(url)//
                .headers(headers)//
                .build()//
                .execute(callback);
    }

    /**
     * post方式,单个文件上传（无参数,无header）
     *
     * @param context
     *            上下文
     * @param url
     *            上传URL
     * @param key
     *            上传文件key
     * @param file
     *            上传文件
     * @param callback
     *            回调函数
     */
    public static <T> void updateFile(Context context, String url, String key,
                                      File file, Callback<T> callback) {
        OkHttpUtils.post()//
                .addFile(key, file.getName(), file)//
                .url(url)//
                .build()//
                .execute(callback);
    }

    /**
     * post方式,多个文件上传
     *
     * @param context
     *            上下文
     * @param url
     *            上传URL
     * @param keys
     *            上传文件key数组
     * @param files
     *            上传文件数组
     * @param callback
     *            回调函数
     */

    public static <T> void updateFile(Context context, String url,Map<String, String> headers,
                                      String[] keys, File[] files, Callback<T> callback) {
        PostFormBuilder Builder = OkHttpUtils.post();
        for (int i = 0; i < files.length; i++) {
            Builder.addFile(keys[i], files[i].getName(), files[i]);
        }
        Builder.url(url)//
                .headers(headers)
                .build()//
                .execute(callback);
    }
}
