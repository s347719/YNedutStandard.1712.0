package com.yineng.ynmessager.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.HuaWeiRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.baidu.mapapi.SDKInitializer;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.marswin89.marsdaemon.DaemonApplication;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.tencent.smtt.sdk.QbSdk;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.okHttp.interceptor.LoggerInterceptor;
import com.yineng.ynmessager.service.LocateService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.util.address.V8ContextAddress;
import com.zhy.http.okhttp.OkHttpUtils;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 绑定的Application
 *
 * @author 贺毅柳
 * @see android.app.Application
 */
public class AppController extends DaemonApplication
        implements Thread.UncaughtExceptionHandler
 {

    final String DEFAULT_RES_PATH_FREFIX = "android.resource://";
    final String DEFAULT_RES_SOUND_TYPE = "raw";
    final String DEFAULT_RES_ICON_TYPE = "mipmap";
    private static final String TAG = "AppController";
    public int UnReedNoticeNum = 0;
    public static String UPDATE_CHECK_IP;
    private static AppController sInstance; // 当前Application的唯一实例
    public LoginUser mLoginUser; // 当前登陆的用户对象
    public User mSelfUser; // 我的详细信息
    public Setting mUserSetting; // 当前登陆用户的设置信息
    public boolean isShownOnScreen = false; // 当前是否有界面正在显示
    public boolean isAliTuisong = false;//是否点击的阿里推送消息
    public DisplayImageOptions mImageLoaderDisplayOptions = null;
    /**
     * 内网
     */
    public String CONFIG_INSIDE_IP = StringUtils.EMPTY;
    /**
     * 文件传输内网
     */
    public String CONFIG_INSIDE_FILE_TRANSLATE_IP = StringUtils.EMPTY;
    /**
     * V8服务地址
     */
    public String CONFIG_YNEDUT_V8_URL = StringUtils.EMPTY;
    /**
     * v8域名
     */
    public String CONFIG_YNEDUT_V8_SERVICE_HOST = StringUtils.EMPTY;
    /**
     * 调转到V8指定页面
     */
    public String CONFIG_YNEDUT_V8_APP_PAGE_URL = StringUtils.EMPTY;
    /**
     * 应用中心访问V8获取可用菜单
     */
    public String CONFIG_YNEDUT_V8_APP_MENUS_URL = StringUtils.EMPTY;

    /**
     * 获取OA待办的地址
     */
    public String CONFIG_YNEDUT_V8_EVENT_OA_URL = StringUtils.EMPTY;
    /**
     * 跳转OA详情的地址
     */
    public String CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL = StringUtils.EMPTY;

    public String CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL = StringUtils.EMPTY;

    /**
     * 应用模块的token
     */
    public String mAppTokenStr = StringUtils.EMPTY;

    /**
     * 人员定位数据批量提交的数据地址
     */
    public String CONFIG_LBS_URL_BATH_UPLOAD = StringUtils.EMPTY;

    /**
     * 人员定位配置参数获取的地址
     */
    public String CONFIG_LBS_URL_CONFIG = StringUtils.EMPTY;

    //当前Ynedut包图标的图片名称
    public static String icon_name = StringUtils.EMPTY;
    /**
     * 网络是否可用
     */
    public static boolean NET_IS_USEFUL = true;

    /**
     * 获取Application实例
     *
     * @return 唯一的实例
     */
    public static synchronized AppController getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //new LogcatWriter().write();
//        Thread.setDefaultUncaughtExceptionHandler(this); //设置异常捕捉操作
        int runtimeMemory = (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
        L.i(TAG, "分配的程序运行时内存大小：" + runtimeMemory + "MB");

        sInstance = this;
        //        百度地图的初始化
        SDKInitializer.initialize(getApplicationContext());
        initImageLoaderDisplayOptions();
        initOkhttp();
        initFileDownload();
        ApplicationInfo appInfo = null;
        try {
            appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            icon_name = appInfo.metaData.getString("app_icon");
            icon_name = icon_name.substring(icon_name.lastIndexOf("/") + 1, icon_name.lastIndexOf("."));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            icon_name = "ic_launcher";
        }
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //  Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
                //  Auto-generated method stub
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(getApplicationContext(), cb);
        initCloudChannel(this);
        //初始化V8 Url
        try {
            V8ContextAddress.bind(getApplicationContext(), URLs.class);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    //这个方法是解决打包的时候报64K这个错
    @Override
    public void attachBaseContextByDaemon(Context base) {
        super.attachBaseContextByDaemon(base);
        MultiDex.install(this);
    }
    /**
     * 初始化云推送通道
     *
     * @param applicationContext
     */
    private void initCloudChannel(final Context applicationContext) {
        PushServiceFactory.init(applicationContext);
        final CloudPushService cloudPushService = PushServiceFactory.getCloudPushService();//得到推送服务设置
        // 指定声音文件路径
        int assignSoundId = getResources().getIdentifier("office", DEFAULT_RES_SOUND_TYPE, getPackageName());
        if (assignSoundId != 0) {
            String defaultSoundPath = DEFAULT_RES_PATH_FREFIX + getPackageName() + "/" + assignSoundId;
            cloudPushService.setNotificationSoundFilePath(defaultSoundPath);
        }
        // 指定通知栏图标文件
        int assignLargeIconId = getResources().getIdentifier("icon_logo_about", DEFAULT_RES_ICON_TYPE, getPackageName());
        if (assignLargeIconId != 0) {
            Drawable drawable = getApplicationContext().getResources().getDrawable(assignLargeIconId);
            if (drawable != null) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                cloudPushService.setNotificationLargeIcon(bitmap);
            }
        }
        // 指定状态栏图标资源Id
        int assignSmallIconId = getResources().getIdentifier("icon_logo_about", DEFAULT_RES_ICON_TYPE, getPackageName());
        cloudPushService.setNotificationSmallIcon(assignSmallIconId);
        //此处必须绑定一个帐号，然后在登录过后再绑定详细的个人帐号，否则接收不到推送
        cloudPushService.register(applicationContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "init cloudchannel success");
                cloudPushService.bindAccount("test", new CommonCallback() {
                    @Override
                    public void onSuccess(String s) {
                        Log.i(TAG, "bind cloudchannel success");
                    }

                    @Override
                    public void onFailed(String s, String s1) {
                        Log.i(TAG, "bind cloudchannel false" + s1 + "  " + s);
                    }
                });

            }

            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.e(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });

        MiPushRegister.register(applicationContext, "2882303761517598601", "5311759832601");
        HuaWeiRegister.register(applicationContext);
//        GcmRegister.register(applicationContext, "send_id", "application_id");
    }

    /**
     * 当程序发生崩溃的时候捕捉异常
     * 在方法中做具体操作
     *
     * @param t
     * @param e
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
//         清空密码 在崩溃的时候点击APP会跳到登录页
        LastLoginUserSP lastUser = LastLoginUserSP.getInstance(this);
        lastUser.saveUserPassword("");
        BaseActivity.exit(true);
    }

    /**
     * 初始化并返回用于初始化ImageLoader的DisplayImageOptions对象，如果已初始化则直接返回
     */
    public DisplayImageOptions initImageLoaderDisplayOptions() {
        // 全局默认通用的DisplayImageOptions
        // 其他界面加载图片需要不一样参数的DisplayImageOptions对象时，
        // 可调用 new DisplayImageOptions.Builder().cloneFrom(mImageLoaderDisplayOptions)来克隆一个Builder，
        // 然后在此对象基础上更改需要的参数，最后build()
        if (mImageLoaderDisplayOptions == null) {
            mImageLoaderDisplayOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                    .resetViewBeforeLoading(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .build();// 构建完成
        }
        return mImageLoaderDisplayOptions;
    }

    /**
     * 初始化oKHttputils
     */
    public void initOkhttp(){
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("TAG",true))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
    }

    /**
     * 初始化filedownload
     * https://github.com/s347719/FileDownloader
     */
    public void initFileDownload(){

        FileDownloader.init(this, new FileDownloadHelper.OkHttpClientCustomMaker() {
            @Override
            public OkHttpClient customMake() {
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                return builder.connectTimeout(10000L, TimeUnit.MILLISECONDS)
                        .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                        .proxy(Proxy.NO_PROXY)
                        .build();
            }
        },10);

    }

    @Override
    protected DaemonConfigurations getDaemonConfigurations() {
        DaemonConfigurations.DaemonConfiguration configuration1 =
                new DaemonConfigurations.DaemonConfiguration("com.marswin89.marsdaemon.demo:process1",
                        LocateService.class.getCanonicalName(), KeepAliveReceiver1.class.getCanonicalName());

        DaemonConfigurations.DaemonConfiguration configuration2 =
                new DaemonConfigurations.DaemonConfiguration("com.marswin89.marsdaemon.demo:process2",
                        KeepAliveService.class.getCanonicalName(), KeepAliveReceiver2.class.getCanonicalName());

        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }
    public static class KeepAliveReceiver1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class KeepAliveReceiver2 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class KeepAliveService extends Service {

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            return super.onStartCommand(intent, flags, startId);
        }
    }
    class MyDaemonListener implements DaemonConfigurations.DaemonListener {
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }

}
