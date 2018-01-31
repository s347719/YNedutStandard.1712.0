package com.yineng.ynmessager.app.update;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.view.dialog.LoadingDialogWithDialog;
import com.yineng.ynmessager.view.dialog.UpdateDialog;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.MessageFormat;

import okhttp3.Call;

/**
 * Created by 胡毅 on 2015/12/10.
 */
public class UpdateCheckUtil {
    private static final String TAG = "UpdateCheckUtil";
    private static final int NOTIFY_ID = 0;
    public static String YNEDUT_APP_MISMATCH;
    private static UpdateCheckUtil mUpdateCheck;
    private boolean isShowDialog = true; //是否显示dialog
    private boolean from_xmpp = false; //是否来自自动登录
    // Application
    protected final AppController mApplication = AppController.getInstance();
    /**
     * 正在检测更新
     */
    public LoadingDialogWithDialog mCheckProgressDialog;
    /**
     * 产品信息
     */
    protected UpdateInfo mUpdateInfo;
    private UpdateDialog mUpdateDialog;
    private static Context mContext;
    private SyncHttpClient mSyncHttpClient = new SyncHttpClient();
    private AsyncTask<Void, Void, Integer> mCheckVerTask;
    private checkVersionListener onCheckVersionListener;
    /**
     * YNedut版本信息
     */
    private YnedutVersionInfo ynedutVerInfo;
    private NotificationManager mNotificationManager;
    private String downloadFilePath="";
    private String fileName="";
    private UpdateCheckUtil() {
        mSyncHttpClient.setMaxRetriesAndTimeout(1, 3000);
    }

    public static synchronized UpdateCheckUtil getInstance() {
        if (mUpdateCheck == null) {
            mUpdateCheck = new UpdateCheckUtil();
        }
        return mUpdateCheck;
    }

    /**
     * 是否显示dialog
     *
     * @param showDialog
     * @param fromXmpp   是否是来自自动登录
     */
    public void setShowDialog(boolean showDialog, boolean fromXmpp) {
        isShowDialog = showDialog;
        from_xmpp = fromXmpp;
    }

    /**
     * 格式化版本字符串，用于显示
     */
    public static String formatNewVersionToShow(String jsonVerStr) {
        String resultStr = "";
        if (TextUtils.isEmpty(jsonVerStr)) {
            jsonVerStr = "0000000000";
        }
        if (jsonVerStr.length() > 10) {
            jsonVerStr = jsonVerStr.substring(0, 9);
        } else if (jsonVerStr.length() < 10) {
            jsonVerStr = "0000000000".substring(0, 10 - jsonVerStr.length()) + jsonVerStr;
        }
        String firstVersion = jsonVerStr.substring(0, 2);
        if (firstVersion.startsWith("0")) {
            firstVersion = firstVersion.substring(1);
        }
        String secondVersion = jsonVerStr.substring(2, 3);
        String lastVersion = jsonVerStr.substring(3);
        L.e(firstVersion + " " + secondVersion + " " + lastVersion);
        resultStr = MessageFormat.format("{0}.{1}.{2}", firstVersion, secondVersion, lastVersion);
        return resultStr;
    }

    /**
     * 设置版本检测的监听器
     *
     * @param onCheckVersionListener
     */
    public void setOnCheckVersionListener(checkVersionListener onCheckVersionListener) {
        this.onCheckVersionListener = onCheckVersionListener;
    }

    public void setContext(Context context) {
        mContext=null;
        mContext = context;
    }

    public AsyncTask<Void, Void, Integer> getCheckVerTask() {
        return mCheckVerTask;
    }

    /**
     * 检测新版本
     *
     * @param tempContext
     */
    public void checkAppVersion(Context tempContext) {
        checkAppVersion(tempContext, false);
        mContext=null;
        mContext = tempContext;
    }

    /**
     * 请求云中心获取版本信息
     */
    @SuppressLint("StaticFieldLeak")
    public void checkAppVersion(Context tempContext, final boolean isHandCheck) {
        mContext = tempContext;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mCheckProgressDialog != null) {
            mCheckProgressDialog = null;
        }
        mCheckProgressDialog = new LoadingDialogWithDialog(mContext, R.style.MyDialog, "");
        if (mCheckVerTask != null) {
            return;
        }
        mCheckVerTask = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                //判断是否获取到v8地址
                if (isYnedutUrlEmpty()) {
                    L.w(TAG, "ynedut urls is empty!!");
                    return CheckResult.INIT_URL_FAILED;
                }
                //判断是否成功获取到token
                if (isYnedutTokenEmpty()) {
                    return CheckResult.YNEDUT_TOKEN_FAILED;
                }
                //判断是否成功获取到v8version，以及异常处理
                ynedutVerInfo = getYnedutVersion();
                if (!isYnedutVersionAvailable(ynedutVerInfo)) {
                    return CheckResult.YNEDUT_VER_FAILED;
                }
                //请求云中心app版本信息
                getYnCenterProVersion();
                if (mUpdateInfo == null) {
                    return CheckResult.CENTER_APPINFO_FAILED;
                }
                return CheckResult.CHECK_VER_SUCCESS;
            }

            @Override
            protected void onPostExecute(Integer checkResultCode) {
                super.onPostExecute(checkResultCode);

                switch (checkResultCode) {
                    case CheckResult.CHECK_VER_SUCCESS:
                        //请求云中心成功，根据返回数据进行逻辑处理
                        checkResultCode = isShowUpdateDialog(mUpdateInfo, isHandCheck);
                        break;
                    case CheckResult.INIT_URL_FAILED:
                        ToastUtil.toastAlerMessageBottom(mContext, mContext.getString(R.string.request_failed), Toast.LENGTH_SHORT);
                        L.e("UpdateCheckUtil","V8地址为空");
                        break;
                    case CheckResult.YNEDUT_TOKEN_FAILED:
                        ToastUtil.toastAlerMessageBottom(mContext, mContext.getString(R.string.request_failed), Toast.LENGTH_SHORT);
                        L.e("UpdateCheckUtil","31003");
                        break;
                    case CheckResult.YNEDUT_VER_FAILED:
                        ToastUtil.toastAlerMessageBottom(mContext, mContext.getString(R.string.request_failed), Toast.LENGTH_SHORT);
                        L.e("UpdateCheckUtil","ynedut版本号获取异常");
                        break;
                    case CheckResult.CENTER_APPINFO_FAILED:
                        L.e("UpdateCheckUtil","获取升级信息异常");
                        break;
                    default:
                        break;
                }
                // TODO: 2016/4/28 检测更新值（取消注释即强制为最新版本，发布时应保持注释）
                //checkResultCode = CheckResult.CENTER_APP_IS_NEWEST;
                if (onCheckVersionListener != null) {
                    onCheckVersionListener.onCheckVerResult(checkResultCode, isHandCheck);
                }
                mCheckVerTask = null;
            }
        }.execute();
    }

    /**
     * 解析openfire的ynedut地址是否为空
     *
     * @return
     */
    private boolean isYnedutUrlEmpty() {
        return TextUtils.isEmpty(mApplication.CONFIG_YNEDUT_V8_URL);
    }

    /**
     * 获取ynedut的access_token<br>
     * <b>Note：</b>该方法会请求网络，需要在子线程中执行
     *
     * @return token 返回实际获取的token字符串，获取失败返回空字符串
     */
    private String getYnedutAccessToken() {
        if (!TextUtils.isEmpty(mApplication.mAppTokenStr)) {
            return mApplication.mAppTokenStr;
        }
        String url = URLs.GET_TOKEN;
        L.d(TAG, "token url : " + url);

        if (isYnedutUrlEmpty()) {
            L.w(TAG, "ynedut urls is empty!!");
            return "";
        }
        OKHttpCustomUtils.get(url, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                V8TokenManager.sToken = response.optString("access_token");
                //更新application里的token
                mApplication.mAppTokenStr = V8TokenManager.sToken;
                L.i(TAG, "new token : " + mApplication.mAppTokenStr);
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                V8TokenManager.sToken = "";
                mApplication.mAppTokenStr = "";
                L.w(TAG, "token http request failed and will return empty string");
            }
        });
        return mApplication.mAppTokenStr;
    }

    /**
     * 获取token后是否为空
     *
     * @return 测试结果是否是
     */
    private boolean isYnedutTokenEmpty() {
        return TextUtils.isEmpty(getYnedutAccessToken());
    }

    /**
     * 获取ynedut版本号
     *
     * @return ynedut版本号，即客户端的依赖版本号
     */
    private YnedutVersionInfo getYnedutVersion() {
        if (isYnedutUrlEmpty()) {
            L.w(TAG, "ynedut urls is empty!!");
            return null;
        }
        if (isYnedutTokenEmpty()) {
            return null;
        }
        String v8VersionUrl = MessageFormat.format(URLs.YNEDUT_GET_V8_VERSION_API, AppController.getInstance().mAppTokenStr);
        L.i("Ynedut ver url == " + v8VersionUrl);
        mSyncHttpClient.get(v8VersionUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                if (response == null) {
                    return;
                }
                String resultStr = new String(response);
                L.i(TAG, "getYnedutVersion : " + resultStr);
                try {
                    ynedutVerInfo = new YnedutVersionInfo();
                    org.json.JSONObject verInfo = new org.json.JSONObject(resultStr);
                    ynedutVerInfo.setMessage(verInfo.getString("message"));
                    ynedutVerInfo.setStatus(verInfo.getInt("status"));
                    ynedutVerInfo.setResult(verInfo.getString("result"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    ynedutVerInfo = null;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (responseBody == null) {
                    return;
                }
                L.e("xmppservice : statusCode == " + statusCode + " onFailure == " + new String(responseBody));
                ynedutVerInfo = null;
                String failStr = new String(responseBody);
                if (failStr.contains("invalid_token")) {
                    mApplication.mAppTokenStr = null;
                    if (isYnedutTokenEmpty()) {//重新获取token,如果token为空，则v8version不可用
                        return;
                    }
                    //拿到token后，重新获取v8version，并判断是否可用，如果不可用返回false，可用返回true
                    ynedutVerInfo = getYnedutVersion();
                }
            }
        });

        return ynedutVerInfo;
    }

    /**
     * 获取的v8版本信息是否可用
     *
     * @param tempYnedutVer
     * @return
     */
    private boolean isYnedutVersionAvailable(YnedutVersionInfo tempYnedutVer) {
        if (tempYnedutVer == null) {
            return false;
        } else {
            switch (tempYnedutVer.getStatus()) {
                case 0://0失败
                case 3://3无权限
                    return false;
                case 1://1成功
                    if (mContext != null) {
                        //保存v8版本号
                        LastLoginUserSP.setYnedutVerion(mContext, tempYnedutVer.getResult());
                    }
                    return true;
                case 2://2token过期
                    mApplication.mAppTokenStr = null;
                    if (isYnedutTokenEmpty()) {//重新获取token,如果token为空，则v8version不可用
                        return false;
                    }
                    //拿到token后，重新获取v8version，并判断是否可用，如果不可用返回false，可用返回true
                    ynedutVerInfo = getYnedutVersion();
                    return isYnedutVersionAvailable(ynedutVerInfo);
                default:
                    break;
            }
        }
        return false;
    }

    /**
     * 获取云中心的客户端产品版本号
     */
    private void getYnCenterProVersion() {

        String checkVersionUrl = MessageFormat.format(URLs.getMessengerUpdateURL(), AppUtils.getVersionName(mContext), ynedutVerInfo.getResult(), LastLoginUserSP.getServerInfoCode(mContext));
        L.i("Update center url == " + checkVersionUrl);
        mSyncHttpClient.get(checkVersionUrl, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] responseBody) {
                if (responseBody == null) {
                    return;
                }
                String responseStr = new String(responseBody);
                L.e(TAG, "获取云中心的客户端产品版本号 : statusCode == " + statusCode + " onSuccess == " + new String(responseBody));
                mUpdateInfo = parseUpdateInfoObject(responseStr);
                if (mUpdateInfo!=null&&mUpdateInfo.getResult()!=null && mUpdateInfo.getResult().getNewesProVersionCode()!=null) {
                    String newVersionToShow = formatNewVersionToShow(mUpdateInfo.getResult().getNewesProVersionCode());
                    //保存新版本信息
                    if (!TextUtils.isEmpty(newVersionToShow)) {
                        LastLoginUserSP.getInstance(mContext).setNewVersionCode(newVersionToShow);
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (responseBody == null) {
                    return;
                }
                L.e(TAG, "获取云中心的客户端产品版本号 : statusCode == " + statusCode + " onFailure == " + new String(responseBody));
                mUpdateInfo = null;
            }
        });

    }

    /**
     * 解析产品信息
     *
     * @param jsonStr
     * @return
     */
    private UpdateInfo parseUpdateInfoObject(String jsonStr) {
        UpdateInfo tempInfo = null;
        JSONObject resultObject = null;
        String status = "";
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            status = jsonObject.optString("status");
            if (status != null) {
                tempInfo = new UpdateInfo();
                tempInfo.setStatus(status);
                tempInfo.setMessage(jsonObject.optString("message"));
                resultObject = jsonObject.optJSONObject("result");
                if (resultObject != null) {
                    ProductInfo tempProInfo = new ProductInfo();
                    tempProInfo.setAttachmentId(resultObject.optString("attachmentId"));
                    tempProInfo.setFileSize(resultObject.optLong("fileSize"));
                    tempProInfo.setPublishTime(resultObject.optString("publishTime"));
                    tempProInfo.setChangeNote(resultObject.optString("changeNote"));
                    tempProInfo.setIsMustUpdate(resultObject.optInt("isMustUpdate"));
                    tempProInfo.setRemindUpdate(resultObject.optInt("remindUpdate"));
                    tempProInfo.setNewesProVersionCode(resultObject.optString("newesProVersionCode"));
                    tempProInfo.setUpdateTimes(resultObject.optString("updateTimes"));
                    tempInfo.setResult(tempProInfo);
                    JSONObject fileDataObject = resultObject.optJSONObject("fileDataMetaTempVO");
                    if (fileDataObject != null) {
                        FileDataInfo fileDataInfo = new FileDataInfo();
                        fileDataInfo.setUserCode(fileDataObject.optString("userCode"));
                        fileDataInfo.setBasePath(fileDataObject.optString("basePath"));
                        fileDataInfo.setServerGroupName(fileDataObject.optString("serverGroupName"));
                        fileDataInfo.setFileURLMappingId(fileDataObject.optString("fileURLMappingId"));
                        fileDataInfo.setSize(fileDataObject.optLong("size"));
                        tempProInfo.setFileDataMetaTempVO(fileDataInfo);
                    }
                    tempInfo.setResult(tempProInfo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return tempInfo;
    }

    /**
     * 根据升级信息确定是否显示更新对话框,以及处理fs返回的异常情况
     *
     * @param mUpdateInfo 升级信息
     * @param isHandCheck 是否是在关于界面手动检测升级
     * @return true 显示 false 不显示
     */
    private int isShowUpdateDialog(UpdateInfo mUpdateInfo, boolean isHandCheck) {
        int ret = 0;
        switch (mUpdateInfo.getStatus()) {
            case "0"://获取新版本成功
                if (mUpdateInfo.getResult() != null
                        && mUpdateInfo.getResult().getFileDataMetaTempVO() != null) {
                    String serverGroupName = mUpdateInfo.getResult().getFileDataMetaTempVO().getServerGroupName();
                    String fileMapId = mUpdateInfo.getResult().getFileDataMetaTempVO().getFileURLMappingId();
                    if (!TextUtils.isEmpty(serverGroupName) && !TextUtils.isEmpty(fileMapId)) {
                        int updateType = mUpdateInfo.getResult().getIsMustUpdate();
                        int remindType  = mUpdateInfo.getResult().getRemindUpdate();
                        if (updateType == 1) {
                            // 强制更新
                            if (from_xmpp) {
                                //如果是自动登录必须提示更新
                                showUpdateDialog(true, isHandCheck);
                            } else {
                                //不是自动登录根据是否显示弹框配置来显示
                                if (isShowDialog ||remindType==1) {
                                    showUpdateDialog(true, isHandCheck);
                                } else {
                                    onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_HAND, isHandCheck);
                                }
                            }
                        } else {
                            //非强制更新
                            if (isShowDialog) {
                                showUpdateDialog(false, isHandCheck);
                            } else {
                                onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_HAND, isHandCheck);
                            }
                        }
                    }
                }
                break;
            case "001"://fs死了的问题
                ret = CheckResult.CENTER_GET_APP_FILE_FAILED;
                break;
            case "002"://已是最新版本
                if (isShowDialog) {
                    ToastUtil.toastAlerMessageBottom(mContext, "当前已是最新版本", Toast.LENGTH_SHORT);
                }
                ret = CheckResult.CENTER_APP_IS_NEWEST;
                break;
            case "003"://Redis错误问题
                ret = CheckResult.CENTER_REDIS_ERROR;
                break;
            case "004":
                ret = CheckResult.CENTER_NOT_FOUNT_AVAIABLE_PRODUCT;
                break;
            case "005":
                YNEDUT_APP_MISMATCH = mContext.getString(R.string.update_ynedut_service_higher_client);
                if (mUpdateInfo.getResult() != null&&mUpdateInfo.getResult().getNewesProVersionCode()!=null) {
                    String avaiableVersion = formatNewVersionToShow(mUpdateInfo.getResult().getNewesProVersionCode());
                    YNEDUT_APP_MISMATCH = mContext.getString(R.string.update_ynedut_service_higher_client, avaiableVersion);
                    L.e("UpdateCheckUtil","12001");
                }
                ret = CheckResult.CENTER_APP_UNAVAIABLE;
                break;
            case "006":
                ret = CheckResult.CENTER_DEPEND_PRODUCE_VER_FAILED;
                break;
            default:
                ret = CheckResult.CENTER_APPINFO_FAILED;
                break;
        }
        return ret;
    }

    /**
     * 显示更新对话框
     *
     * @param isMustUpdate true：强制 false：非强制
     * @param isHandCheck  是否是关于界面检测升级
     */
    private void showUpdateDialog(final boolean isMustUpdate, final boolean isHandCheck) {
        if (mUpdateDialog!=null){
            mUpdateDialog.cancel();
            mUpdateDialog= null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("\n" + "最新版本：");
        String version = formatNewVersionToShow(mUpdateInfo.getResult().getNewesProVersionCode());
        sb.append(version);
        if (mUpdateInfo.getResult().getFileSize() != 0 || mUpdateInfo.getResult().getFileDataMetaTempVO().getSize() != 0) {
            String sizeStr = FileUtil.FormatFileSize(mUpdateInfo.getResult().getFileSize());
            sb.append("\u3000\u3000大小：" + sizeStr);
        }
        sb.append("\n当前版本：");
        sb.append(formatNewVersionToShow(AppUtils.getVersionName(mContext)));
        if (mUpdateInfo.getResult().getChangeNote() != null
                && !TextUtils.isEmpty(mUpdateInfo.getResult().getChangeNote())) {
            sb.append("\n更新内容：\n");
            sb.append(mUpdateInfo.getResult().getChangeNote() != null ? mUpdateInfo.getResult().getChangeNote() : "");
        }
        //更新提示框
        mUpdateDialog = new  UpdateDialog(mContext, R.style.MyDialog, new UpdateDialog.OnclickListener() {
            @Override
            public void clicklistener(View view) {
                switch (view.getId()) {
                    //点击提示框的取消按钮
                    case R.id.update_calcle:
                        if (mUpdateDialog != null) {
                            mUpdateDialog.cancel();
                        }
                        if (isMustUpdate) {
                            if (onCheckVersionListener != null) {
                                // /强制更新对话框点击退出程序，回调到Activity执行关闭应用程序的方法
                                onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_QUIT_MUST, isHandCheck);
                            }
                        } else {
                            if (onCheckVersionListener != null) {
                                //非强制更新对话框点取消，回调Activity执行进入主界面的方法
                                onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_HAND, isHandCheck);
                            }
                        }
                        break;

                    //点击提示框的更新按钮
                    case R.id.update_update:
                        if (mUpdateDialog != null) {
                            mUpdateDialog.cancel();
                        }
                        if (isMustUpdate)//强制更新时，跳转进入下载页面，并将升级流程的状态回调到Activity
                        {
                            if (onCheckVersionListener != null) {
                                onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_DO_MUST, isHandCheck);
                            }
                            Intent intent = new Intent(mContext, DownloadVersionActivity.class);
                            intent.putExtra(Const.UPDATE_INFO, mUpdateInfo.getResult());
                            mContext.startActivity(intent);
                        } else {//非强制更新时，开启下载apk线程，并弹出通知栏,并将升级流程的状态回调到Activity
                            startDownloadThread();
                            if (onCheckVersionListener != null) {
                                onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_HAND, isHandCheck);
                            }
                        }
                        break;

                    default:
                        break;
                }

            }
        }, sb.toString(), isMustUpdate);
        mUpdateDialog.setCancelable(false);

        if (((Activity) mContext).isFinishing()) {
            return;
        }
        if (mUpdateDialog.isShowing()) {
            mUpdateDialog.cancel();
        }
        //此时的弹框有可能为空，在此处加一个判断如果为空则进入不强制跟新流程
        if (mUpdateDialog != null) {
            mUpdateDialog.show();
        } else {
            onCheckVersionListener.onHandleUpdateVer(CheckResult.CENTER_APP_UPDATE_HAND, false);
        }
    }

    /**
     * 非强制更新下载下载
     */
    private void startDownloadThread() {
        if (mUpdateInfo == null) {
            return;
        }
        final FileDataInfo updateFileDataInfo = mUpdateInfo.getResult() != null ?
                mUpdateInfo.getResult().getFileDataMetaTempVO() : null;
        if (updateFileDataInfo == null) {
            return;
        }
        fileName = "Messenger" + AppUtils.getVersionName(mContext) + ".apk";
        String basePath = updateFileDataInfo.getBasePath();
        if (basePath != null && basePath.contains("/")) {
            int index = basePath.lastIndexOf("/");
            fileName = basePath.substring(index + 1);
        }
        L.i("下载APK文件名称：" + fileName);
        String downloadUrl = Const.UPDATE_DOWNLOAD_APP_URL+"?fastDFSId="+updateFileDataInfo.getFileURLMappingId();
        downloadFilePath = FileUtil.getUserSDPath(true, null) + File.separator + fileName;
        //开始下载
        FileDownloader.getImpl().create(downloadUrl)
                .setPath(downloadFilePath)
                .setListener(new NotificationListener(new FileDownloadNotificationHelper<NotificationItem>()))
                .start();

    }



    /**
     * 下载成功，解析apk安装包
     */
    private void doDownloadedSuccess(String filepath) {
        L.e("安装apk");
        if (filepath.contains(".apk")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filepath)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }


    private class NotificationListener extends FileDownloadNotificationListener {

        private static final String TAG = "NotificationListener";

        NotificationListener(FileDownloadNotificationHelper helper) {
            super(helper);
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(),
                    fileName, " ");
        }

        @Override
        public void destroyNotification(BaseDownloadTask task) {
            super.destroyNotification(task);
            //下载完成 去除notificat 安装下载包
            doDownloadedSuccess(downloadFilePath);

        }
    }

    public static class NotificationItem extends BaseNotificationItem {

        NotificationCompat.Builder builder;
        Notification mNotification;
        String progress = "";
        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
            builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());
            int icon_id = mContext.getResources().getIdentifier(AppController.icon_name, "mipmap", mContext.getPackageName());
            mNotification =  builder.mNotification;
            mNotification.icon = icon_id;
            mNotification.flags = Notification.FLAG_AUTO_CANCEL;
            mNotification.tickerText = desc;
            mNotification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notice_downloadfile_notification_layout);
            mNotification.contentView.setImageViewResource(R.id.notice_download_notification_logo,  icon_id);
        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " 加入中";
                    break;
                case FileDownloadStatus.started:
                    desc += " 开始";
                    break;
                case FileDownloadStatus.progress:
                    desc += " 正在下载...";
                    break;
                case FileDownloadStatus.retry:
                    desc += "重试中";
                    break;
                case FileDownloadStatus.error:
                    desc += " 连接异常，下载失败.";
                    break;
                case FileDownloadStatus.paused:
                    desc += " 下载暂停";
                    break;
                case FileDownloadStatus.completed:
                    desc += " 下载完成";
                    break;
                case FileDownloadStatus.warn:
                    desc += "连接异常，下载失败.";
                    break;
            }
            mNotification.contentView.setTextViewText(R.id.notice_download_notification_filename,getTitle());
            mNotification.contentView.setTextViewText(R.id.notice_download_notification_filestate,desc);
            if (statusChanged) {
                mNotification.contentView.setTextViewText(R.id.notice_download_notification_filestate,desc);
            }
            if (getTotal()>0) {
                progress = AppUtils.accuracy(getSofar(), getTotal(), 1);
            }
            mNotification.contentView.setTextViewText(R.id.notice_download_notification_filepercent, progress);
            mNotification.contentView.setProgressBar(R.id.notice_download_notification_progressbar,  getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }

    }

    public interface CheckResult {
        //云中心访问成功
        int CHECK_VER_SUCCESS = 700;

        //openfire初始化地址未获取到
        int INIT_URL_FAILED = 701;

        //token获取失败
        int YNEDUT_TOKEN_FAILED = 702;

        //YNedut版本号获取失败
        int YNEDUT_VER_FAILED = 703;

        //云中心访问失败
        int CENTER_APPINFO_FAILED = 704;

        //001云端判定当前版本可用，且有更新包（存在强制更新包），但获取更新包时出现异常
        int CENTER_GET_APP_FILE_FAILED = 713;

        //云中心访问成功，但redis错误（服务器的问题）
        int CENTER_REDIS_ERROR = 705;

        //此产品依赖版本号没有查到可用的产品版本！
        int CENTER_NOT_FOUNT_AVAIABLE_PRODUCT = 706;

        //您当前的版本与依赖的产品版本之间不对应，请卸载后重新安装
        int CENTER_APP_UNAVAIABLE = 707;

        //已是最新版本
        int CENTER_APP_IS_NEWEST = 708;

        //用户点了手动更新
        int CENTER_APP_UPDATE_HAND = 709;

        //强制更新时 用户点了退出程序
        int CENTER_APP_UPDATE_QUIT_MUST = 710;

        //用户点了强制更新
        int CENTER_APP_UPDATE_DO_MUST = 711;
        int CENTER_DEPEND_PRODUCE_VER_FAILED = 712;
    }

    public interface checkVersionListener {
        /**
         * 检测升级信息结果
         *
         * @param checkResult 检测结果
         * @param isHandCheck 是否是关于界面，手动更新
         */
        void onCheckVerResult(int checkResult, boolean isHandCheck);

        /**
         * 处理更新版本方法
         *
         * @param handleType  处理方式
         * @param isHandCheck 是否是关于界面，手动更新
         */
        void onHandleUpdateVer(int handleType, boolean isHandCheck);
    }

    public class YnedutVersionInfo {
        private String message;
        private String result;
        private int status;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
