package com.yineng.ynmessager.view;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yineng on 2016/3/3.
 */
public class X5Webview4Msg extends WebView {
    private Context mContext;
    /**
     * 文件下载器
     */
    private DownloadManager downloadManager;

    /**
     * 长按图片的下载地址
     */
    public String mDownloadImgurl = "";

    private String mDownloadFileReg = "(filename=\"(.*?)\")";

    public X5Webview4Msg(Context context) {
        super(context);
    }

    public X5Webview4Msg(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mContext = context;
        init(context);
    }

    public X5Webview4Msg(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public X5Webview4Msg(Context context, AttributeSet attributeSet, int i, boolean b) {
        super(context, attributeSet, i, b);
    }

    public X5Webview4Msg(Context context, AttributeSet attributeSet, int i, Map<String, Object> map, boolean b) {
        super(context, attributeSet, i, map, b);
    }

    private void init(Context context) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (getX5WebViewExtension() != null) {//x5内核
            L.i("x5内核");
        } else { //os内核
            L.i("os内核");
        }
        WebSettings settings = this.getSettings();
        //设置此属性，可任意比例缩放
        settings.setUseWideViewPort(true);
        //设置webview可运行JavaScript方法
        settings.setJavaScriptEnabled(true);
        //设置JavaScript自动打开窗口.This applies to the JavaScript function window.open()
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //开启 DOM storage API 功能,设置可以使用localStorage
        settings.setDomStorageEnabled(true);
        //设置可以有缓存
        settings.setAppCacheEnabled(true);
        //设置缓存目录
        String appCachePath = mContext.getCacheDir().getAbsolutePath();
        L.v("appCachePath == " + appCachePath);
        settings.setAppCachePath(appCachePath);
        //设置缓存大小
        settings.setAppCacheMaxSize(1024 * 1024 * 15);//8M
        // 应用可以有数据库
        settings.setDatabaseEnabled(true);

        String dbPath = mContext.getDatabasePath(Const.WEBVIEW_DB_PATH).getAbsolutePath();
        settings.setDatabasePath(dbPath);
        settings.setAllowFileAccess(true);
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        //设置 缓存模式
        if (NetWorkUtil.isNetworkAvailable(mContext)) {
            settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        } else {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }

        settings.setPluginsEnabled(true);
        setDownloadListener(new MyWebViewDownLoadListener());
        //创建长按图片保存到手机时的界面显示
        setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//                if (v instanceof WebView) {
                    WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                    if (result != null) {
                        int type = result.getType();
                        if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                            mDownloadImgurl = result.getExtra();
                            menu.setHeaderTitle("提示");
                            menu.add(0, 0, 0, "保存图片");
                        }
                    }
//                }
            }
        });
    }


    private class MyWebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            int systemApiLevel = SystemUtil.getSystemVersion();
            L.i("url == "+url);
            L.i("userAgent == "+userAgent);
            L.i("contentDisposition == "+contentDisposition);
            L.i("mimetype == " + mimetype);
            L.i("contentLength == " + contentLength);
            if (systemApiLevel > 10) {
                // 2.3以上版本调用DownloadManager下载
                try {
                    String fileName = null;
                    if (TextUtils.isEmpty(contentDisposition)) {
                        if (url.contains("/")) {
                            fileName = url.substring(url.lastIndexOf("/")+1).trim();
                        }
                    } else {
                        Pattern p = Pattern.compile(mDownloadFileReg);
                        Matcher m = p.matcher(contentDisposition);
                        if (m.find()) {
                            String tempFileName = m.group();
                            fileName = tempFileName.substring("filename=\"".length(),tempFileName.length()-"\"".length());
                            fileName = URLDecoder.decode(fileName,"UTF-8");
                            L.e("Match fileName == "+fileName);
                        } else if (contentDisposition.contains("filename=")) {
                            fileName = contentDisposition.substring(contentDisposition.lastIndexOf("filename=")+"filename=".length()).trim();
                            L.i("fileName == " + fileName + " getEncoding == " + getEncoding(fileName));
                            String fileNameOld = fileName;

                            fileName = new String(fileNameOld.getBytes("UTF-8"), "ISO-8859-1");
                            L.v("fileName == " + fileName + " getEncoding == " + getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("utf-8"), "gbk");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("utf-8"), "gb2312");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("ISO-8859-1"), "utf-8");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("GB2312"), "utf-8");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("GB2312"), "ISO-8859-1");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = new String(fileNameOld.getBytes("ISO-8859-1"), "GB2312");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));

                            fileName = URLDecoder.decode(fileNameOld,"UTF-8");
                            L.v("fileName == "+fileName+" getEncoding == "+getEncoding(fileName));
                        }
                    }
                    startDownloadWebFile(url,fileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {// 2.3及其以下版本调用浏览器下载
                Uri uri = Uri.parse(url);
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(it);
            }
        }
    }

    public static String getEncoding(String str) {
        String encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GB2312
                String s = encode;
                return s; //是的话，返回“GB2312“，以下代码同理
            }
        } catch (Exception exception) {
        }
        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是ISO-8859-1
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是UTF-8
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) { //判断是不是GBK
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        return ""; //如果都不是，说明输入的内容不属于常见的编码格式。
    }

    /**
     * 下载网页的文件
     * @param url
     * @param fileName
     */
    public void startDownloadWebFile(String url, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            int idx = url.lastIndexOf(".");
            String ext = url.substring(idx);
            fileName = TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24) + ext;
        }
        L.i("url == "+url);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        if (fileName != null) {
            request.setDestinationInExternalFilesDir(mContext, null, fileName);
            request.setTitle(fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setMimeType("application/com.yineng.ynmessager.download.file");
            request.setDestinationInExternalPublicDir(FileUtil.getUserSDPath(false,""), fileName);
        }
        downloadManager.enqueue(request);
    }
}
