//***************************************************************
//*    2015-10-13  下午2:01:53
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.PopupWindowCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebStorage;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.bean.app.Submenu;
import com.yineng.ynmessager.db.NewMyAppsTb;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.view.agentwebx5.AgentWeb;
import com.yineng.ynmessager.view.agentwebx5.ChromeClientCallbackManager;
import com.yineng.ynmessager.view.agentwebx5.CommonIndicator;
import com.yineng.ynmessager.view.agentwebx5.interfae.DownLoadResultListener;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;


/**
 * 需要用 {@link Intent#putExtra(String, String)} 方法传入“Url”的参数字符串
 *
 * @author 贺毅柳
 *         修改cordiva插件 ： 舒欢
 *         第二次修改：yhu
 */
public class X5WebAppActivity extends BaseActivity implements OnClickListener {
    private String mTag = this.getClass().getSimpleName();
    public static String IS_OA = "IS_OA";
    private AppController mApplication = AppController.getInstance();
    private PopupWindow mSubMenuWindow;
    private Context mContext;
    private View app_webview_titleview_layout;
    private LinearLayout mAppTitleBackLL;
    private ImageView mAppTitleBackIV;
    private TextView mAppTitleNameTV;
    private LinearLayout mAppTitleRightLL;
    private ImageView mAppTitleRightIV;
    private ValueCallback<Uri[]> mFilePathCallback;
    private static final int REFRESH_COMPLETE = 0X110;
    private String mCameraPhotoPath;
    private RelativeLayout x5webcontain;
    private static AgentWeb mAgentWeb;
    private String titleName;
    //区别是否是宏天OA
    private boolean is_oa = false;
    private View hide_view, hide_view_back, hide_view_refresh;
    //判断是否启用back键
    private boolean isCanBack = true;

    private List<Submenu> mAppMenus = new ArrayList<>(); //菜单
    /**
     * 网页地址
     */
    private static String mUrl;

    private static class MyHandler extends Handler {
        private final WeakReference<X5WebAppActivity> mActivity;

        public MyHandler(X5WebAppActivity activity) {
            mActivity = new WeakReference<X5WebAppActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            X5WebAppActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case REFRESH_COMPLETE:
                        // 要清除缓存
                        mAgentWeb.clearWebCache();
                        mAgentWeb.getLoader().loadUrl(mUrl);
                        break;
                }
            }
        }
    }

    MyHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = X5WebAppActivity.this;
        mHandler = new MyHandler(this);
        setContentView(R.layout.activity_x5web_app);
        initViews();
    }

    @SuppressLint("NewApi")
    private void initViews() {
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        x5webcontain = (RelativeLayout) findViewById(R.id.x5webcontain);

        Intent intent = getIntent();
        //判断是否是OA
        is_oa = getIntent().getBooleanExtra(IS_OA, false);

        mUrl = intent.getStringExtra("url");
        titleName = intent.getStringExtra("Name");
        String parentId = intent.getStringExtra("AppId");
        app_webview_titleview_layout = findViewById(R.id.app_webview_titleview_layout);
        hide_view = findViewById(R.id.hide_view);

        if (is_oa) {
            app_webview_titleview_layout.setVisibility(View.GONE);
            hide_view.setVisibility(View.VISIBLE);
        } else {
            app_webview_titleview_layout.setVisibility(View.VISIBLE);
            hide_view.setVisibility(View.GONE);
        }
        app_webview_titleview_layout = findViewById(R.id.app_webview_titleview_layout);
        mAppTitleBackLL = (LinearLayout) app_webview_titleview_layout.findViewById(R.id.app_common_titleview_left);
        mAppTitleBackIV = (ImageView) app_webview_titleview_layout.findViewById(R.id.app_common_title_view_back);
        mAppTitleNameTV = (TextView) app_webview_titleview_layout.findViewById(R.id.app_common_title_view_name);
        mAppTitleRightLL = (LinearLayout) app_webview_titleview_layout.findViewById(R.id.app_common_titleview_right);
        mAppTitleRightLL.setVisibility(View.VISIBLE);
        mAppTitleRightIV = (ImageView) app_webview_titleview_layout.findViewById(R.id.app_common_title_view_infomation);
        mAppTitleRightIV.setImageResource(R.mipmap.refresh);
        mAppTitleRightIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(REFRESH_COMPLETE);
            }
        });
        hide_view_back = findViewById(R.id.hide_view_back);
        hide_view_refresh = findViewById(R.id.hide_view_refresh);
        hide_view_back.setOnClickListener(this);
        hide_view_refresh.setOnClickListener(this);
        if (!StringUtils.isEmpty(titleName)) {
            mAppTitleNameTV.setText(titleName);
        }
        L.i(mTag, "loading url : " + mUrl);
        mAppTitleNameTV.setOnClickListener(this);
        mAppTitleBackLL.setOnClickListener(this);
        initAppMenus(parentId);

        //自定义ProgressBar
        CommonIndicator mCommonIndicator = new CommonIndicator(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-2, -2);
        lp.gravity = Gravity.CENTER;
        ProgressBar mProgressBar = new ProgressBar(this);
        mProgressBar.setBackground(getResources().getDrawable(R.drawable.indicator_shape));
        mCommonIndicator.addView(mProgressBar, lp);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(x5webcontain, new LinearLayout.LayoutParams(-1, -1))
                .customProgress(mCommonIndicator)
                .setReceivedTitleCallback(mCallback)
                .setWebChromeClient(webChromeClient)
                .setWebViewClient(mWebViewClient)
                .setSecutityType(AgentWeb.SecurityType.strict)
                .addDownLoadResultListener(mDownLoadResultListener)
                .createAgentWeb()
                .ready()
                .go(mUrl);

        //微信调用
        mAgentWeb.getJsInterfaceHolder().addJavaObject("native", new Object() {

            /**
             * 禁用back键
             */
            @JavascriptInterface
            public void disableBackButton() {
                isCanBack = false;
            }

            /**
             * 关闭当前页面
             */
            @JavascriptInterface
            public void closeWindow() {
                finish();
            }

            /**
             * 微信支付获取ip
             */
            @JavascriptInterface
            public void getMyIp() {
                String url2 = "http://ip.aliyun.com/service/getIpInfo2.php";
                Map<String, String> params = new HashMap<>();
                params.put("ip", "myip");
                OKHttpCustomUtils.post(url2, params, this, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject response, int id) {
                        try {
                            String dataStr = response.getString("data");
                            JSONObject ipJson = new JSONObject(dataStr);
                            String ip = ipJson.getString("ip");
                            mAgentWeb.getJsEntraceAccess().quickCallJs("window.payByWx", ip);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        mAgentWeb.getJsEntraceAccess().quickCallJs("window.payByWx", "");
                    }
                });
            }

        });

    }


    private WebViewClient mWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
            L.v(mTag, "WebViewClient -- onPageFinished");
        }

        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            L.v(mTag, "onReceivedSslError **************");
            sslErrorHandler.proceed();
        }

    };

    protected DownLoadResultListener mDownLoadResultListener = new DownLoadResultListener() {
        @Override
        public void success(String path) {
            Log.i("Info", "path:" + path);
        }

        @Override
        public void error(String path, String resUrl, String cause, Throwable e) {

            Log.i("Info", "path:" + path + "  url:" + resUrl + "  couse:" + cause + "  Throwable:" + e);
        }
    };
    /**
     * 接收来自H5的标题
     */
    private ChromeClientCallbackManager.ReceivedTitleCallback mCallback = new ChromeClientCallbackManager.ReceivedTitleCallback() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (StringUtils.isEmpty(titleName) && mAppTitleNameTV != null) {
                mAppTitleNameTV.setText(title);
            }
        }
    };

    /**
     * 如果当前模块有子菜单，加载子菜单并显示
     *
     * @param parentId 主页被点击的模块ID
     */
    private void initAppMenus(String parentId) {
        if (!StringUtils.isEmpty(parentId)) {
            //获取submenu
            NewMyAppsDao dao = new NewMyAppsTb(this);
            NewMyApps app = dao.queryById(parentId);
            this.mAppMenus = JSON.parseArray(app.getSubmenu(), Submenu.class);
            //判断是否有菜单
            if (mAppMenus == null) {
                return;
            }
            int size = mAppMenus.size();
            if (size > 0) {
                ArrayList<HashMap<String, String>> subMenuData = new ArrayList<>();
                HashMap<String, String> subMenu;
                for (Submenu submenu : mAppMenus) {
                    subMenu = new HashMap<>();
                    subMenu.put("menu", submenu.getName());
                    subMenuData.add(subMenu);
                }

                SimpleAdapter mSubMenuListAdapter = new SimpleAdapter(this, subMenuData, R.layout.item_web_app_sub_menu, new String[]{"menu"},
                        new int[]{R.id.webApp_txt_subMenu_listItem_content});
                View subMenuView = getLayoutInflater().inflate(R.layout.view_web_app_sub_menu, null);
                ListView subMenuList = (ListView) subMenuView.findViewById(R.id.webApp_lst_subMenuList);
                subMenuList.setAdapter(mSubMenuListAdapter);
                subMenuList.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        if (mSubMenuWindow != null) {
                            mSubMenuWindow.dismiss();
                        }
                        Submenu menuApp = mAppMenus.get(position);
                        mAppTitleNameTV.setText(menuApp.getName());
                        if (!StringUtils.isEmpty(menuApp.getRoute())) {
                            mUrl = MessageFormat.format(mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL, V8TokenManager.obtain(),
                                    mApplication.mLoginUser.getUserNo(), menuApp.getRoute());
                            mAgentWeb.getLoader().loadUrl(mUrl);
                            L.i(mTag, "loading url : " + mUrl);
                        }
                    }
                });

                //初始化弹出框
                mSubMenuWindow = new PopupWindow(subMenuView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
                mSubMenuWindow.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.color.main_content_bg, getTheme()));
                mSubMenuWindow.setOutsideTouchable(true);
                mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
                //popwin消失时改变箭头方向
                mSubMenuWindow.setOnDismissListener(new OnDismissListener() {

                    @Override
                    public void onDismiss() {
                        mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null,
                                getResources().getDrawable(R.mipmap.contact_fast_jump_arrow_down), null);
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubMenuWindow != null) {
            mSubMenuWindow.dismiss();
        }
        mAgentWeb.getWebLifeCycle().onDestroy();
    }

    @Override
    public void onResume() {
        mAgentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mAgentWeb.getWebLifeCycle().onPause();
        super.onPause();
    }


    /**
     * 清除webview缓存
     */
    public void clearWebCacheData() {
        File file = getCacheDir();
        if (file != null && file.exists()) {
            deleteDir(file);
        }
        File dbfile = getDatabasePath(Const.WEBVIEW_DB_PATH);
        if (dbfile != null && dbfile.exists()) {
            deleteDir(dbfile);
        }
    }


    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isCanBack) {
            return true;
        }
        if (mAgentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_common_title_view_name:
                if (mSubMenuWindow == null) {
                    return;
                }
                mAppTitleNameTV.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.mipmap.contact_fast_jump_arrow_up, getTheme()), null);
                PopupWindowCompat.showAsDropDown(mSubMenuWindow, mAppTitleNameTV, 0, 0, Gravity.NO_GRAVITY);
                break;
            case R.id.app_common_titleview_left:
                finish();
                break;
            case R.id.hide_view_back:
                finish();
                break;
            case R.id.hide_view_refresh:
                mHandler.sendEmptyMessage(REFRESH_COMPLETE);
                break;
        }
    }


    private String mCameraFilePath = null;
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>
    //OA 中点击文件选择 相关参数
    public static final int INPUT_FILE_REQUEST_CODE = 2;

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(com.tencent.smtt.sdk.WebView webView, int i) {
            super.onProgressChanged(webView, i);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = valueCallback;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    //设置MediaStore.EXTRA_OUTPUT路径,相机拍照写入的全路径
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    Log.e("WebViewSetting", "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }


        // For Android 4.1
        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType, String capture) {
            if (uploadMsg == null) {
                return;
            }

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILECHOOSER_RESULTCODE);

        }

        // For Android 3.0-
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            if (uploadMsg == null) {
                return;
            }

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILECHOOSER_RESULTCODE);

        }

        // For Android 3.0+=
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            if (uploadMsg == null) {
                return;
            }

            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(
                    Intent.createChooser(i, "File Browser"),
                    FILECHOOSER_RESULTCODE);
        }

        @Override
        public void onReachedMaxAppCacheSize(long l, long l1, WebStorage.QuotaUpdater quotaUpdater) {
            //清除webview缓存
            mAgentWeb.clearWebCache();
            clearWebCacheData();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) {
                return;
            }
            L.v(mTag, "onActivityResult == ");
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null
                    : intent.getData();
            L.v(mTag, "mCameraFilePath == " + mCameraFilePath + " result == " + result);
            if (result == null && intent == null
                    && resultCode == Activity.RESULT_OK) {
                L.v(mTag, "upload == ");
                File cameraFile = new File(mCameraFilePath);

                if (cameraFile.exists()) {
                    result = Uri.fromFile(cameraFile);
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, result));
                }
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == INPUT_FILE_REQUEST_CODE && mFilePathCallback != null) {
            // 5.0的回调
            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (intent == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        Log.d("camera_photo_path", mCameraPhotoPath);
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {
                    String dataString = intent.getDataString();
                    Log.d("camera_dataString", dataString);
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }

    }

    //在sdcard卡创建缩略图
    //createImageFileInSdcard
    @SuppressLint("SdCardPath")
    private File createImageFile() {
        //mCameraPhotoPath="/mnt/sdcard/tmp.png";
        File file = new File(Environment.getExternalStorageDirectory() + "/", TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATE1) + ".png");
        mCameraPhotoPath = file.getAbsolutePath();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }
}
