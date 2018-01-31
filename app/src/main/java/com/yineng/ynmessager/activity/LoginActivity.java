package com.yineng.ynmessager.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.util.PatternsCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.Splash.SaveImageAsyncTask;
import com.yineng.ynmessager.activity.app.X5WebAppActivity;
import com.yineng.ynmessager.adapter.MyAdapter;
import com.yineng.ynmessager.adapter.ServiceUrlAdapter;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.update.UpdateCheckUtil;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.CommonEvent;
import com.yineng.ynmessager.bean.login.LoginConfig;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.bean.service.LocateConfig;
import com.yineng.ynmessager.bean.service.LocateItem;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.okHttp.OKHttpCustomUtils;
import com.yineng.ynmessager.okHttp.callback.JSONObjectCallBack;
import com.yineng.ynmessager.receiver.SmsReciver;
import com.yineng.ynmessager.service.LocateService;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.AppToastUtils;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.Base64PasswordUtil;
import com.yineng.ynmessager.util.CountDownTimerUtil;
import com.yineng.ynmessager.util.DensityUtil;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.IconBadgerHelper;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.PhoneFormatCheckUtils;
import com.yineng.ynmessager.util.PreferenceUtils;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TextUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.util.V8TokenManager;
import com.yineng.ynmessager.util.address.URLs;
import com.yineng.ynmessager.util.address.V8ContextAddress;
import com.yineng.ynmessager.view.dialog.VerificationDialog;
import com.zhy.http.okhttp.callback.StringCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;


/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */

/**
 * @author YINENG 用户登陆界面
 */
public class LoginActivity extends BaseActivity implements OnClickListener,
        TextWatcher, MyAdapter.OnClicklistener, ServiceUrlAdapter.OnClickListener {
    private final int START_MAINACTIVITY = 200;
    private final int START_INTENT = 201;
    /**
     * 登录过的帐号列表
     */
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private String tag = LoginActivity.class.getSimpleName();
    private Context mContext;// 全局上下文
    private String mUserAccount;// 用户帐号
    private String mUserNo;//用户唯一ID
    private String mUserPassword;// 用户密码
    private String mServiceAddress;// 用户地址
    private int loginType = LoginConfig.LOGIN_TYPE_ACCOUNT;//登录模式
    private String phoneNum = ""; //电话号码
    private String account = "";//用户名
    private String mobileCode = "";
    private boolean isGetUserInfo = true;

    //UI
    private LinearLayout ll_login_info, ll_login_setting_service_address, ll_login_useraccount, ll_login_servers, login_more_linear, login_more_choose;//隐藏显示
    private LinearLayout lin_change_login_type, lin_server_help;//切换登录的布局,服务器帮助
    private EditText login_et_username, login_et_password, login_service_address;//用户名，密码,服务器地址
    private ImageButton login_ib_select_account;//选择账户
    private TextView login_update_service_address, login_isSetting_service_address, history_service_address, login_sao_miao, loading_login, login_type_txt, login_get_verification, login_back_view;//历史记录,二维码扫描,修改服务器地址，是否设置服务器地址,修改登录类型帐号或手机,获取手机验证码
    private TextView login_phone_txt, txt_phone_help;//多帐号选择界面电话号，动态码帮助
    private ImageView login_iv_history, login_banner, login_img_user, login_img_phone;//历史记录下拉图标,登录方式不同的icon
    private Button login_btn_login, login_cancel, login_save;//登录,取消，保存服务器地址
    private FrameLayout login_progressbar;//登录progress


    private LoginUser mNowLoginuser = null;//当前登录用户,null:用户不存在在数据库，!= null: 存在在数据库
    private boolean mIsUserNoLoginSuccess = false;
    private XmppConnectionManager mXmppConnectionManager;
    private ClientInitConfig mClientInitConfig;//初始化配置信息
    private LastLoginUserSP mLastUserSP;// 最近一次登陆帐号信息
    private SmsReciver mSmsReciver;//短信验证码监听

    private LoginUser mDeleteLoginUser;//下拉框删除项
    private LoginThread mLoginRunable;//登录的runable对象
    private List<LoginUser> mUserList;//获取已经登录过的用户帐号
    private LoginUserDao mLoginUserDao;// 用户登陆帐号列表
    private com.yineng.ynmessager.adapter.MyAdapter mLoginUserAdapter;//用户帐号适配器
    private PopupWindow mUserAccountPop;//登录用户帐号的下拉列表
    private ServiceUrlAdapter mLoginServersAdapter;//服务器地址适配器
    private Set<String> mUserLoginedServiceUrls;//用户登录过的服务器地址
    private List<String> address;
    private PopupWindow mUserAddressPop;//登录用户服务器地址的下拉列表

    private Animation rightInAnimation;//右进
    private Animation rightOutAnimation;//右出
    private Animation leftInAnimation;//左进
    private Animation leftOutAnimation;//左出
    private View popView, addressView;
    private ListView popListView, addressListView;
    private AlertDialog dialog;
    private boolean isLoginLoading = false;//是否正在登录
    private boolean isDelete = true;//是否删除本地记录的标签
    private String saveOpenFireHost;
    //smesisId 用于应用跳转
    private String smesisUserId;
    private HashMap<String, String> imageInfo = new HashMap<>(); //图片信息
    AppController appController = AppController.getInstance();
    private boolean startGpsIndex = false;//标记点击dialog动作
    private String netInfo="";
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int x = msg.arg1;
            switch (x) {
                case LoginThread.LOGIN_START:
                    // 登陆中，登陆按钮不可用
                    L.i(tag, "LOGIN_START");
                    showProgressD();
                    break;
                case LoginThread.LOGIN_FAIL:
                    // 登陆失败
                    L.i(tag, "LOGIN_FAIL");
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "登录失败", 1000);
                    break;
                case LoginThread.LOGIN_USER_ACCOUNT_ERROR:
                    // 用户名或密码错误
                case LoginThread.LOGIN_USER_ACCOUNT_OUT:
                    // 用户名或密码错误
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "用户名或密码错误", 1000);
                    break;
                case LoginThread.LOGIN_SERVER_ERROR:
                    // 服务器或地址错误
                    L.i(tag, "LOGIN_SERVER_ERROR");
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "服务器或地址错误", 1000);
                    break;
                case LoginThread.LOGIN_SERVER_NOT_RESPON:
                    // 服务器没响应
                    L.i(tag, "LOGIN_SERVER_NOT_RESPON");
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "服务器连接异常", 1000);
                    break;
                case LoginThread.LOGIN_TIMEOUT:
                    // 超时
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "连接服务器超时，请检查", 1000);
                    break;
                case LoginThread.LOGIN_USER_NOT_EXIST:
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(), "用户不存在", 1000);
                    break;
                case LoginThread.LOGIN_SUCCESS:
                    // 登陆成功
                    L.d(tag, "登录成功-登录按钮设置为不可取消登录");
                    if (mNowLoginuser == null || isGetUserInfo) {
                        //用登录帐号登录成功
                        connectSendPacket();
                    } else {
                        //说明用真正的userNo登录成功了
                        L.d(tag, "LOGIN_SUCCESS -- init iq");
                        mIsUserNoLoginSuccess = true;
                        login_btn_login.setOnClickListener(null);
                        startMainUIAndService();
                    }

                    break;
                case LoginThread.LOGIN_CANCEL:
                    //取消登录
                    L.e("退出登录");
                    // 清空密码
                    LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
                    lastUser.saveUserPassword("");
                    // 下线，停止xmpp服务
                    closeXmppService();

                    //关闭用户数据库连接
                    UserAccountDB.setNullInstance();
                    break;

                default:
//				hideProgessD();
                    break;
            }
            switch (msg.what) {

                case START_MAINACTIVITY:
                    initConfigInfo();
                    break;
                case START_INTENT:
                    LastLoginUserSP.getInstance(LoginActivity.this).saveIsLogin(true);
                    L.e("MSG", "--是否登录--" + LastLoginUserSP.getInstance(LoginActivity.this).isLogin());
//                    startService(new Intent(LoginActivity.this, LocateService.class));
                    Intent mainActivityIntent = new Intent(mContext, MainActivity.class);
                    mainActivityIntent.putExtra("loginType", loginType);
                    LoginActivity.this.startActivity(mainActivityIntent);
                    finishActivity();
                    break;
                default:
                    break;
            }
        }
    };

    private PacketListener mUserInfoIQListener = new PacketListener() {

        @Override
        public void processPacket(Packet paramPacket) {
            readResultPacket((ReqIQResult) paramPacket);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.layout_login_update);
        initViews();
        initUserInfo();
        initUserAccountListView();
        initUserServiceUrls();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ConnectivityManager.CONNECTIVITY_ACTION));
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (startGpsIndex) {
            mHandler.sendEmptyMessage(START_INTENT);
        }
    }

    /**
     * 初始化views
     */
    private void initViews() {
        mContext = this;
        mLastUserSP = LastLoginUserSP.getInstance(mContext);
        mLoginUserDao = new LoginUserDao(mContext);
        popView = LayoutInflater.from(this).inflate(R.layout.layout_pop_account_list, null);
        addressView = LayoutInflater.from(this).inflate(R.layout.layout_pop_address_list, null);
        addressListView = (ListView) addressView.findViewById(R.id.pop_listview);
        popListView = (ListView) popView.findViewById(R.id.pop_listview);
        login_progressbar = (FrameLayout) findViewById(R.id.login_progressbar);
        lin_change_login_type = (LinearLayout) findViewById(R.id.lin_change_login_type);
        loading_login = (TextView) findViewById(R.id.loading_login);
        ll_login_info = (LinearLayout) findViewById(R.id.ll_login_info);
        ll_login_setting_service_address = (LinearLayout) findViewById(R.id.ll_login_setting_service_address);
        ll_login_useraccount = (LinearLayout) findViewById(R.id.ll_login_useraccount);
        ll_login_servers = (LinearLayout) findViewById(R.id.ll_login_servers);
        login_more_linear = (LinearLayout) findViewById(R.id.login_more_linear);
        login_more_choose = (LinearLayout) findViewById(R.id.login_more_choose);
        login_et_username = (EditText) findViewById(R.id.login_et_username);
        login_et_password = (EditText) findViewById(R.id.login_et_password);
        login_service_address = (EditText) findViewById(R.id.login_service_address);
        login_ib_select_account = (ImageButton) findViewById(R.id.login_ib_select_account);
        login_update_service_address = (TextView) findViewById(R.id.login_update_service_address);
        login_isSetting_service_address = (TextView) findViewById(R.id.login_isSetting_service_address);
        history_service_address = (TextView) findViewById(R.id.history_service_address);
        login_sao_miao = (TextView) findViewById(R.id.login_sao_miao);
        login_type_txt = (TextView) findViewById(R.id.login_type_txt);
        txt_phone_help = (TextView) findViewById(R.id.txt_phone_help);
        login_get_verification = (TextView) findViewById(R.id.login_get_verification);
        login_iv_history = (ImageView) findViewById(R.id.login_iv_history);
        lin_server_help = (LinearLayout) findViewById(R.id.lin_server_help);
        login_banner = (ImageView) findViewById(R.id.login_banner);
        login_img_user = (ImageView) findViewById(R.id.login_img_user);
        login_img_phone = (ImageView) findViewById(R.id.login_img_phone);
        login_btn_login = (Button) findViewById(R.id.login_btn_login);
        login_cancel = (Button) findViewById(R.id.login_cancel);
        login_save = (Button) findViewById(R.id.login_save);
        login_phone_txt = (TextView) findViewById(R.id.login_phone_txt);
        login_back_view = (TextView) findViewById(R.id.login_back_view);
        login_ib_select_account.setOnClickListener(this);
        login_update_service_address.setOnClickListener(this);
        login_btn_login.setOnClickListener(this);
        history_service_address.setOnClickListener(this);
        login_sao_miao.setOnClickListener(this);
        login_cancel.setOnClickListener(this);
        login_save.setOnClickListener(this);
        login_banner.setOnClickListener(this);
        login_type_txt.setOnClickListener(this);
        login_get_verification.setOnClickListener(this);
        login_back_view.setOnClickListener(this);
        login_service_address.addTextChangedListener(this);
        lin_server_help.setOnClickListener(this);
        txt_phone_help.setOnClickListener(this);
        if (PreferenceUtils.getPrefBoolean(this, Const.IS_FIRST_LAUNCH, true)) {//是否为第一次启动
            ll_login_info.setVisibility(View.GONE);
            ll_login_setting_service_address.setVisibility(View.VISIBLE);
            PreferenceUtils.setPrefBoolean(this, Const.IS_FIRST_LAUNCH, false);
        }

        dialog = new AlertDialog.Builder(this).create();
        //输入焦点放到后面
        login_service_address.setSelection(login_service_address.getText().toString().length());

        ((TextView) findViewById(R.id.login_txt_versionText)).setText(
                getString(R.string.about_currentVersionName,
                        UpdateCheckUtil.formatNewVersionToShow(AppUtils.getVersionName(this))));
//        UpdateCheckUtil.getInstance().setShowDialog(true);
        popListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LoginUser loginUser = mLoginUserDao.getLoginUserByAccount(mUserList.get(i).getAccount());
                login_et_username.setText(mUserList.get(i).getAccount());
                login_et_username.setSelection(mUserList.get(i).getAccount().length());
                //手机登录不允许显示密码
                if (loginUser.getIsUserAccountType() == 1) {
                    login_et_password.setText(mUserList.get(i).getPassWord());
                } else {
                    login_et_password.setText("");
                }
                mUserAccountPop.dismiss();
            }
        });

        addressListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                login_service_address.setText(address.get(i));
                mUserAddressPop.dismiss();
            }
        });

        //手机号如果为空，则获取验证码框为灰色不可用
        login_et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (login_et_username.getText().length() > 0) {
                    if (!isGetVerificationing) {
                        login_get_verification.setBackgroundResource(R.drawable.login_activity_verification_nor_background);
                        login_get_verification.setTextColor(getResources().getColor(R.color.actionBar_bg));
                    }
                } else {
                    login_get_verification.setBackgroundResource(R.drawable.login_activity_verification_press_background);
                    login_get_verification.setTextColor(getResources().getColor(R.color.common_text_line));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //设置默认图片
        setLocalImage();
        initAnimation();
    }


    /**
     * 设置默认图片
     */
    private void setLocalImage() {

        //如果本地有图片加载本地图片
        if (SaveImageAsyncTask.hasImage(SaveImageAsyncTask.SAVE_LOGIN)) {
            //启动页未更新
            String localPath = SaveImageAsyncTask.localImagePath(SaveImageAsyncTask.SAVE_LOGIN);
            Bitmap splashBitmap = BitmapFactory.decodeFile(localPath);
            login_banner.setImageBitmap(splashBitmap);
            //获取本地图片信息
            try {
                imageInfo = mLastUserSP.getImageInfo(LastLoginUserSP.LOGIN_LOCAL_IMAGE_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                login_banner.setImageBitmap(splashBitmap);
            }
        } else {
            login_banner.setImageResource(R.mipmap.icon_login_banner);
            imageInfo = null;
        }
    }


    /**
     * 初始化动画
     */
    private void initAnimation() {
        rightInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_in);
        rightOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_right_out);
        leftInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_in);
        leftOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.slide_left_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 清除桌面图标的未读提醒数字
        IconBadgerHelper.showIconBadger(getApplicationContext());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText && SystemUtil.isTouchOutside(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    /**
     * 重置user信息
     */
    protected void resetUserInfo() {
        mNowLoginuser = null;
        mUserNo = "";
        mIsUserNoLoginSuccess = false;
        mClientInitConfig = null;
    }

    /**
     * 初始化用户界面
     */
    private boolean isUpdate = false;

    private void initUserInfo() {
        account = mLastUserSP.getUserLoginAccount();
        String address = mLastUserSP.getUserServicesAddress();
        login_et_password.setText(mLastUserSP.getUserPassword());

        mSmsReciver = new SmsReciver();
        isUpdate = false;
        LoginConfig mLoginConfig = mLastUserSP.getServiceLoginConfig();
        //判断系统配置文件推荐的登录方式,如果存在手机登录帐号则显示手机登录方式
        boolean supportPhone = false;
        boolean supportAccount = true;
        try {
            loginType = mLoginConfig.getFirstLoginWay();
            //获取服务器配置，看是否支持帐号登录or手机登录，如果不支持就隐藏
            supportPhone = mLoginConfig.isLoginByMobile();
            supportAccount = mLoginConfig.isLoginByAccount();

        } catch (NullPointerException e) {
            e.printStackTrace();
            //如果拿不到配置文件
            isUpdate = true;
            login_iv_history.setVisibility(View.GONE);
            history_service_address.setVisibility(View.GONE);
            ll_login_info.setVisibility(View.GONE);
            login_cancel.setVisibility(View.GONE);
            ll_login_setting_service_address.setVisibility(View.VISIBLE);
        }


        if (!supportPhone) {
            //不支持就隐藏切换登录的按钮
            loginType = LoginConfig.LOGIN_TYPE_ACCOUNT;
        }

        if (!supportAccount) {
            loginType = LoginConfig.LOGIN_TYPE_PHONE;
        }

        //如果都支持才可以切换
        if (supportAccount && supportPhone) {
            lin_change_login_type.setVisibility(View.VISIBLE);
            //如果有最后一次登录方式就切换成手机登录
            if (!StringUtils.isEmpty(account)) {
                LoginUser loginUser = mLoginUserDao.getLoginUserByAccount(account);
                if (loginUser != null) {
                    if (loginUser.getLastLoginType() == LoginConfig.LOGIN_TYPE_PHONE) {
                        phoneNum = mLoginUserDao.getLoginUserByAccount(account).getLastLoginPhoneNum();
                        loginType = LoginConfig.LOGIN_TYPE_PHONE;
                    } else {
                        loginType = LoginConfig.LOGIN_TYPE_ACCOUNT;
                    }
                } else {
                    loginType = mLoginConfig.getFirstLoginWay();
                }
            }
        } else {
            lin_change_login_type.setVisibility(View.GONE);
        }

        changeLoginTypeViewAnim(false);

        if (address != null && !"".equals(address)) {
            mServiceAddress = TextUtil.replaceSlash(TextUtil.replaceBlank(address));
            login_isSetting_service_address.setText(address);
            mApplication.CONFIG_YNEDUT_V8_URL = mServiceAddress + "/";
            login_service_address.setText(address);
            if (!isUpdate) {
                login_cancel.setVisibility(View.VISIBLE);
            }
        } else {
            login_cancel.setVisibility(View.GONE);
        }
    }


    /**
     * 登录
     */
    private void Login() {
        // 帐号转成小写
        mUserAccount = login_et_username.getText().toString().trim();
        mUserPassword = login_et_password.getText().toString().trim();
        mServiceAddress = TextUtil.replaceSlash(TextUtil.replaceBlank(login_isSetting_service_address.getText().toString()));
        String toastFirst = getString(R.string.login_errortext_emptyusername);
        String toastSecond = getString(R.string.login_errortext_emptypassowrd);
        if (loginType == LoginConfig.LOGIN_TYPE_PHONE) {
            mobileCode = mUserPassword;
            phoneNum = mUserAccount;
            toastFirst = getResources().getString(R.string.login_errortext_emptyphone);
            toastSecond = getResources().getString(R.string.login_errortext_emptyverification);
            //判断电话号是否正确
            if (!TextUtils.isEmpty(mUserAccount) && !PhoneFormatCheckUtils.isChinaPhoneLegal(mUserAccount)) {
                login_et_username.requestFocus();
                ToastUtil.toastAlerMessageiconTop(mContext,
                        LoginActivity.this.getLayoutInflater(),
                        getString(R.string.login_errortext_emptyphone_check),
                        2000);
                return;
            }
        }
        // 验证用户输入及提示用输入内容
        if (TextUtils.isEmpty(mUserAccount)) {
            login_et_username.requestFocus();
            ToastUtil.toastAlerMessageiconTop(mContext,
                    LoginActivity.this.getLayoutInflater(),
                    toastFirst,
                    2000);
            return;
        }


        if (TextUtils.isEmpty(mUserPassword)) {
            login_et_password.requestFocus();
            ToastUtil.toastAlerMessageiconTop(mContext,
                    LoginActivity.this.getLayoutInflater(),
                    toastSecond,
                    2000);
            return;
        }

        if (TextUtils.isEmpty(mServiceAddress)) {
            login_isSetting_service_address.requestFocus();
            ToastUtil
                    .toastAlerMessageiconTop(
                            mContext,
                            LoginActivity.this.getLayoutInflater(),
                            getString(R.string.login_errortext_emptyserviceaddress),
                            2000);
            return;
        } else {
            if (!checkServerAddressIsURL(mServiceAddress)) {// 如果用户输入格式不正确
                login_isSetting_service_address.requestFocus();
                ToastUtil
                        .toastAlerMessageiconTop(
                                mContext,
                                LoginActivity.this.getLayoutInflater(),
                                getString(R.string.login_errorformat_serviceaddress),
                                2000);
                return;
            }
        }

        // 如果网络不可用，建议用户开启网络连接
        if (!NetWorkUtil.isNetworkAvailable(mContext)) {
            if (mLastUserSP.isExistsUser()) {// 如果存在本地账户，进入主页
                Intent mainActivityIntent = new Intent(mContext,
                        MainActivity.class);
                LoginActivity.this.startActivity(mainActivityIntent);
            } else {// 无网络无登录记录
                ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getString(R.string.login_networkDisconnected), 2000);
                return;
            }
        } else {// 如果网络可用，开启登陆线程
            mLastUserSP.saveUserAccount("");//先存一次空密码,为了在第一次登录的时候进入匿名登录流程
            login_btn_login.setText("取消");
            isLoginLoading = true;
            login_et_username.setEnabled(false);
            login_et_password.setEnabled(false);
            login_update_service_address.setEnabled(false);
            login_ib_select_account.setEnabled(false);
            login_type_txt.setEnabled(false);
            login_progressbar.setVisibility(View.VISIBLE);

            @SuppressLint("StaticFieldLeak")
            AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {


                @Override
                protected void onPreExecute() {
                    mApplication.CONFIG_YNEDUT_V8_URL = mServiceAddress + "/";
                }

                @Override
                protected String doInBackground(Void... params) {
                    return V8TokenManager.forceRefresh();
                }

                @Override
                protected void onPostExecute(final String token) {
                    if (StringUtils.isBlank(token)) {
                        ToastUtil.toastAlerMessageiconTop(LoginActivity.this, getLayoutInflater(), getString(R.string.request_failed), 2000);
                        hidProgess();
                        return;
                    }
                    Map<String, String> params = new HashMap<>();
                    params.put("access_token", token);
                    OKHttpCustomUtils.get(URLs.GET_SERVICE_LOGIN_CONFIG, params, new StringCallback() {
                        boolean isSuccess;

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            ToastUtil.toastAlerMessageiconTop(LoginActivity.this, getLayoutInflater(), getString(R.string.request_failed), 2000);
                            isSuccess = false;
                            hidProgess();
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            final Context context = mContext;
                            try {
                                JSONObject resObj = new JSONObject(response);
                                JSONObject resultObj = resObj.getJSONObject("result");

                                String centerHost = resultObj.getString("centerHostAndPost");
                                String cloudHost = resultObj.getString("cloudHostAndPost");
                                String companyName = resultObj.getString("companyName");
                                String companyCode = resultObj.getString("companycode");
                                String code = resultObj.optString("code");
                                String landSysVersion = resultObj.getString("landSystemVersion");
                                String openFireHost = resultObj.getString("openFireHostAndPost");
                                //获取服务器登录配置
                                String loginRuleStr = resultObj.getString("loginRule");
                                Log.i(mTag, "获取服务器登录配置:" + loginRuleStr);
                                LoginConfig loginConfig = JSON.parseObject(resultObj.getString("loginRule"), LoginConfig.class);
                                int lastGetVerNum = mLastUserSP.getServiceLoginConfig().getLastGetVerNum();
                                loginConfig.setLastGetVerNum(lastGetVerNum);
                                mLastUserSP.saveServiceLoginConfig(loginConfig);

                                if (resultObj.optBoolean("cloud", false)) {
                                    saveOpenFireHost = openFireHost.startsWith("http://") ? openFireHost.replace("http://", "") : openFireHost;
                                } else {
                                    String openFirePort = resultObj.getString("openFirePort");
                                    String addRess = mServiceAddress.split("/")[2];
                                    saveOpenFireHost = addRess.contains(":") ? addRess.substring(0, addRess.indexOf(":")) + ":" + openFirePort : mServiceAddress.split("/")[2] + ":" + openFirePort;
                                    L.i(mTag, "云地端整合拼接生成的openfirehost地址： " + saveOpenFireHost);
                                }
                                L.i(mTag, "登录成功拼接生成的地址：" + saveOpenFireHost);
                                LastLoginUserSP.saveServerInfoOpenFireHost(context, saveOpenFireHost);

                                AppController.UPDATE_CHECK_IP = centerHost;//获取计费系统地址
                                //登录优化的时候去除第三个接口，采用第二个接口返回的版本号来作为判断在首页是否提示版本不可用，需要下载等功能 BaseActivity 238行
                                LastLoginUserSP.setYnedutVerion(context, landSysVersion);
                                LastLoginUserSP.saveServerInfoCenterHost(context, centerHost);
                                LastLoginUserSP.saveServerInfoCloudHost(context, cloudHost);
                                LastLoginUserSP.saveServerInfoCompanyName(context, companyName);
                                LastLoginUserSP.saveServerInfoCompanyCode(context, companyCode);
                                LastLoginUserSP.saveServerInfoCode(context, code);
                                LastLoginUserSP.saveServerInfoLandSysVersion(context, landSysVersion);

                                isSuccess = true;
                            } catch (JSONException e) {
                                L.e(mTag, e.getMessage(), e);
                                isSuccess = false;
                            }
                        }

                        @Override
                        public void onAfter(int id) {
                            if (isSuccess) {
                                //EventBus通知BaseActivity执行检测升级的方法
                                mXmppConnectionManager = XmppConnectionManager.getInstance();
                                //如果密码不同，则恢复状态，重新初始化
                                if (!mUserPassword.equals(mLastUserSP.getUserPassword())
                                        || !mUserAccount.equals(mLastUserSP.getUserLoginAccount())) {
                                    mXmppConnectionManager.setXmppConnectionConfigNull();
                                }
                                loading_login.setText("正在连接...");
                                //初始化链接地址
                                configConstantUrl();
                                //EventBus通知BaseActivity执行检测升级的方法
                                EventBus.getDefault().post(new CommonEvent(Const.IS_NOT_AUTO_LOGIN_XMPP, UpdateCheckUtil.getInstance()));

                            }
                        }
                    });
                }
            };
            AsyncTaskCompat.executeParallel(tokenTask);
        }

    }


    /**
     * 手机验证码登录
     */
    private void loginPhone(String token) {
        resetUserInfo();
        //判断手机验证码是否可用并返回帐号
        String url = URLs.VERIFY_DYNAMIC_CODE_USEABLE;
        Map<String, String> params = new HashMap<>();
        params.put("access_token", token);
        params.put("mobile", phoneNum);
        params.put("code", mobileCode);
        params.put("terminalType", 2 + "");
        Log.i(mTag, "动态码验证地址:" + url + "?" + params.toString());
        //验证手机号是否正确，并且返回登录信息
        OKHttpCustomUtils.get(url, params, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                Log.i(mTag, "动态码验证错误:" + e.getMessage() + " " + getResources().getString(R.string.request_error_login));
                ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getResources().getString(R.string.request_failed), 1000);
            }

            @Override
            public void onResponse(String jsonStr, int id) {
                List<HashMap<String, String>> accounts = new ArrayList<>();
                Log.i(mTag, "动态码验证结果:" + jsonStr);
                try {
                    JSONObject resultObj = new JSONObject(jsonStr);
                    JSONObject listObj = resultObj.getJSONObject("result");
                    String resultCode = listObj.getString("errcode");
                    String errMsg = listObj.getString("errmsg");
                    if ("0".equals(resultCode)) {
                        JSONArray userArr = listObj.getJSONArray("accountList");
                        HashMap<String, String> userMap = null;
                        for (int i = 0; i < userArr.length(); i++) {
                            JSONObject infoObj = userArr.getJSONObject(i);
                            userMap = new HashMap<>();
                            System.out.println(infoObj.getString("userId"));
                            userMap.put("userNo", infoObj.getString("userId"));
                            userMap.put("account", infoObj.getString("account"));
                            userMap.put("accountShow", infoObj.getString("showInfo"));
                            userMap.put("password", infoObj.getString("passId"));
                            userMap.put("smeUserId", infoObj.optString("smeUserId"));
                            accounts.add(userMap);
                        }
                        //如果是多帐号登录，则进入选择界面
                        if (accounts.size() > 1) {
                            //动画显示多帐号界面
                            showMoreAccountView(true);
                            //动态添加布局,以及点击事件
                            for (final LinearLayout view : setMoreAccountButton(accounts)) {
                                login_more_choose.addView(view);
                                view.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        login_progressbar.setVisibility(View.VISIBLE);
                                        HashMap<String, String> map = (HashMap<String, String>) v.getTag();
                                        multiOrSingleAccountLogin(map);
                                    }
                                });
                            }
                        } else {
                            multiOrSingleAccountLogin(userMap);
                        }
                    } else {
//                        AppToastUtils.getInstance(mContext).toastForPhoneTop(resultCode);
                        ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), errMsg, 500);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                    if (accounts.size() <= 0) {
                        ToastUtil.toastAlerMessageiconTop(LoginActivity.this, getLayoutInflater(), getString(R.string.login_verification_error), 500);
                    }
                }
            }
        });
    }

    /**
     * 手机动态码登录,多帐号和单帐号
     *
     * @param map
     */
    private void multiOrSingleAccountLogin(HashMap<String, String> map) {
        login_btn_login.setBackgroundResource(R.drawable.login_activity_login_bt_bg);
        login_btn_login.setEnabled(false);

        String account = map.get("account");
        String password = map.get("password");
        String userNo = map.get("userNo");
        this.smesisUserId = map.get("smeUserId");
        mUserAccount = account;
        mUserNo = userNo;

        mLastUserSP.saveUserAccount(userNo);
        if (mNowLoginuser == null) {
            mNowLoginuser = new LoginUser(account);
            mNowLoginuser.setUserNo(userNo);
            if (loginType == LoginConfig.LOGIN_TYPE_ACCOUNT) {
                mNowLoginuser.setPassWord(mUserPassword);
            }
            mNowLoginuser.setFirstLoginDate(TimeUtil
                    .getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24));
            mLoginUserDao.saveLoginUser(mNowLoginuser);
        }
        mUserPassword = password;
        //如果用户在登录帐号登录成功，在获取该帐号详细信息过程中，用户取消登录，就不执行userno登录的操作
        if (mLoginRunable != null && mLoginRunable.getCancelLogin()) {
            return;
        }
        //改用mUserNo登录，给mLoginRunable赋新的变量地址
        mLoginRunable = mXmppConnectionManager.doLoginThread(userNo, password, Const.RESOURSE_NAME, mHandler, mContext, true);
        isGetUserInfo = true;
    }

    /**
     * 显示多帐号登录界面
     */
    private void showMoreAccountView(boolean inOrOut) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float w_screen = dm.widthPixels;
        if (inOrOut) {
            //in
            login_more_linear.setVisibility(View.VISIBLE);
            login_more_linear.setTranslationX(w_screen);
            ObjectAnimator objAnim = ObjectAnimator.ofFloat(login_more_linear, "translationX", w_screen, 0);
            objAnim.setDuration(300);
            objAnim.start();
            objAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    login_more_linear.setTranslationX(0);
                    ll_login_info.setVisibility(View.GONE);
                }
            });
        } else {
            //out
            ll_login_info.setVisibility(View.VISIBLE);
            ObjectAnimator objAnim = ObjectAnimator.ofFloat(login_more_linear, "translationX", 0, w_screen);
            objAnim.setDuration(300);
            objAnim.start();
            objAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    login_more_linear.setVisibility(View.GONE);
                    login_more_choose.removeAllViews();
                }
            });
        }
    }

    /**
     * 动态添加多帐号布局并显示
     */
    private List<LinearLayout> setMoreAccountButton(List<HashMap<String, String>> accounts) {
        List<LinearLayout> accountViews = new ArrayList<>();
        String msg = "您的手机号" + phoneNum.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2") + "关联了多个登录帐号，请选择进入";
        SpannableStringBuilder builder = new SpannableStringBuilder(msg);
        ForegroundColorSpan phoneSpan = new ForegroundColorSpan(getResources().getColor(R.color.login_phone_num_color));
        builder.setSpan(phoneSpan, 5, 16, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        login_phone_txt.setText(builder);

        for (HashMap<String, String> map : accounts) {
            LinearLayout moreLoginBtn = new LinearLayout(this);
            LinearLayout.LayoutParams linParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            linParams.setMargins(0, DensityUtil.dip2px(this, 10), 0, 0);
            moreLoginBtn.setOrientation(LinearLayout.VERTICAL);
            moreLoginBtn.setGravity(Gravity.CENTER);
            moreLoginBtn.setPadding(0, DensityUtil.dip2px(this, 5), 0, DensityUtil.dip2px(this, 5));
            moreLoginBtn.setBackgroundResource(R.drawable.select_login_choose_account);
            moreLoginBtn.setClickable(true);
            moreLoginBtn.setLayoutParams(linParams);
            moreLoginBtn.setTag(map);

            LinearLayout.LayoutParams txtParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView userNameTxt = new TextView(this);
            userNameTxt.setText(map.get("account"));
            userNameTxt.setTextSize(18);
            userNameTxt.setTextColor(getResources().getColorStateList(R.color.selector_login_txt));
            userNameTxt.setLayoutParams(txtParams);

            TextView departTxt = new TextView(this);
            departTxt.setText(map.get("accountShow"));
            departTxt.setTextColor(getResources().getColorStateList(R.color.selector_login_txt));
            departTxt.setTextSize(13);
            departTxt.setLayoutParams(txtParams);

            moreLoginBtn.addView(userNameTxt);
            moreLoginBtn.addView(departTxt);
            accountViews.add(moreLoginBtn);
        }
        return accountViews;
    }

    /**
     * 帐号密码登录
     */
    private void loginAccount(String token) {
        resetUserInfo();
        //获取加密后的密码
        Map<String, String> params = new HashMap<>();
        params.put("version", "V1.0");
        params.put("access_token", token);
        params.put("userName", mUserAccount);
        params.put("password", Base64PasswordUtil.encode(mUserPassword));
        OKHttpCustomUtils.get(URLs.VALDATE_USER, params, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                try {
                    int status = response.getInt("status");
                    if (status == 0) {
                        JSONObject resultObj = response.getJSONObject("result");
                        String userNo = resultObj.getString("userId");
                        String password = resultObj.getString("passId");
                        String smeUserId = resultObj.optString("smeUserId");
                        mUserNo = userNo;
                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("userNo", userNo);
                        userMap.put("account", mUserAccount);
                        userMap.put("password", password);
                        userMap.put("smeUserId", smeUserId);
                        multiOrSingleAccountLogin(userMap);

                    } else {
                        ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getString(R.string.login_username_password_error), 2000);
                        hidProgess();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                Log.e(mTag,getResources().getString(R.string.request_error_login));
                ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getResources().getString(R.string.request_failed) , 1000);
                hidProgess();
            }
        });
    }


    /**
     * 初始化用户登录过的帐号视图
     */
    private void initUserAccountListView() {
        // 如果有两个以上帐号，则显示帐号选择框
        mUserList = mLoginUserDao.getLoginUsers();
        int userSize = mUserList.size();

        //过滤掉手机登录的用户
        for (LoginUser user : mUserList) {
            Log.e("yhu", user.getAccount() + ";" + user.getIsUserAccountType());
            if (user.getIsUserAccountType() == 0) {
                userSize--;
            }
        }
        if (mUserList != null && userSize > 0) {
            login_ib_select_account.setImageResource(R.mipmap.icon_login_xiala);
            login_ib_select_account.setEnabled(true);
        } else {
            login_ib_select_account.setImageDrawable(null);
            login_ib_select_account.setEnabled(false);
        }
    }

    /**
     * 初始化用户登录过的服务器地址
     */
    private void initUserServiceUrls() {
        mUserLoginedServiceUrls = LastLoginUserSP.getUserServerUrls(mContext);
        if (mUserLoginedServiceUrls == null || mUserLoginedServiceUrls.size() == 0) {
            login_iv_history.setVisibility(View.GONE);
            history_service_address.setVisibility(View.GONE);
            ll_login_info.setVisibility(View.GONE);
            ll_login_setting_service_address.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 保存用户登陆信息
     */
    private void saveLastUser() {

        // save to sharedpreference
        L.d("saveLastUser : mUserNo == " + mUserNo);
        mLastUserSP.saveUserAccount(mUserNo);
        mLastUserSP.saveUserLoginAccount(mUserAccount);
        mLastUserSP.saveUserPassword(mUserPassword);
        mLastUserSP.saveUserServicesAddress(TextUtil.replaceSlash(TextUtil.replaceBlank(mServiceAddress)));
        mNowLoginuser.setLastLoginDate(TimeUtil
                .getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24));
        mNowLoginuser.setLastLoginType(loginType);
        if (loginType == LoginConfig.LOGIN_TYPE_PHONE) {
            mNowLoginuser.setLastLoginPhoneNum(phoneNum);
        } else {
            //用帐号密码方式登陆过
            mNowLoginuser.setIsUserAccountType(1);
        }
        mLoginUserDao.saveLoginUser(mNowLoginuser);
        mApplication.mLoginUser = mNowLoginuser;
        //保存smesisUserId 用于smesis应用跳转
        mLastUserSP.saveSmesisUserId(this.smesisUserId);
    }

    /**
     * 开启主界面和服务
     */
    private void startMainUIAndService() {
        saveLastUser();// 保存登陆信息
        saveClientInitInfo();//保存客户端初始化信息到数据库
        NoticesManager.getInstance(getApplicationContext()).destoryInstance();

        Intent serviceIntent = new Intent(mContext, XmppConnService.class);
        serviceIntent.putExtra("isLoginActvity", true);
        LoginActivity.this.startService(serviceIntent);

        if (!mHandler.hasMessages(START_MAINACTIVITY)) {
            mHandler.sendEmptyMessageDelayed(START_MAINACTIVITY, 1000);
        }
    }


    /**
     * 保存openfire addressOf的初始化信息
     */
    public void saveClientInitInfo() {
        if (mClientInitConfig != null) {
            ContactOrgDao initDao = new ContactOrgDao(LoginActivity.this);
            initDao.saveClientInitInfo(mClientInitConfig);
        }
    }

    /**
     * 显示正在登录的界面
     */
    private void showProgressD() {
        isLoginLoading = true;
        login_btn_login.setText("取消");
    }

    /**
     * 隐藏正在登录的界面
     */
    public void hidProgess() {
        login_btn_login.setText("登录");
        isLoginLoading = false;
        login_et_username.setEnabled(true);
        login_et_password.setEnabled(true);
        login_update_service_address.setEnabled(true);
        login_ib_select_account.setEnabled(true);
        login_type_txt.setEnabled(true);
        login_progressbar.setVisibility(View.GONE);
        login_btn_login.setBackgroundResource(R.drawable.login_activity_login_bt_background);
        login_btn_login.setEnabled(true);
        login_btn_login.setOnClickListener(this);
        loading_login.setText("正在登录...");
    }

    /**
     * 关闭界面
     */
    private void finishActivity() {
        this.finish();
    }

    /**
     * 检测服务器地址是否合法
     *
     * @return
     */
    private boolean checkServerAddressIsURL(String address) {
        return address.matches(PatternsCompat.WEB_URL.toString());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUserAccountPop != null) {
            mUserAccountPop.dismiss();
        }
        if (mUserAddressPop != null) {
            mUserAddressPop.dismiss();
        }
        mHandler.removeCallbacksAndMessages(null);
        if (mXmppConnectionManager != null) {
            mXmppConnectionManager.removePacketListener(mUserInfoIQListener);
        }
    }


    @Override
    protected void onNetWorkChanged(String info) {

        if (!TextUtils.isEmpty(netInfo))
        {
            if (!netInfo.equals(info))
            {
                netInfo = info;
                cancelLogin();
            }
        }else {
            netInfo = info;
        }
        L.e(mTag,"网络信息 info:"+info +"保存网络信息：netinfo"+netInfo);

    }

    /**
     * 发送IQ请求获取该联系人详细信息
     */
    private void connectSendPacket() {
        mXmppConnectionManager.addPacketListener(mUserInfoIQListener, new PacketTypeFilter(IQ.class));
        ReqIQ iq = new ReqIQ();
        iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL);
        JSONObject paramJson = new JSONObject();
        try {
            paramJson.put("userNoList", new JSONArray().put(mUserAccount));
            paramJson.put("isUserNo", false);
            paramJson.put("code", LastLoginUserSP.getServerInfoCompanyCode(getApplicationContext()));
        } catch (JSONException e) {
            L.e(mTag, e.getMessage(), e);
        }

        iq.setParamsJson(paramJson.toString());
        L.e("LoginActivity", "iq xml ->" + iq.toXML());
        sendPersonInfoIqPacket(iq);
    }

    /**
     * 发送packet
     *
     * @param packet
     * @throws IOException
     */
    protected boolean sendPersonInfoIqPacket(Packet packet) {
        boolean sendSuccess = false;
        if (mXmppConnectionManager.getConnection() != null) {
            if (NetWorkUtil.isNetworkAvailable(mContext)) {
                if (mXmppConnectionManager.getConnection().isConnected()) {
                    try {
                        mXmppConnectionManager.getConnection().sendPacket(packet);
                        sendSuccess = true;
                    } catch (Exception e) {
//						if (e.getMessage().equals("Not connected to server.")) {}
                        sendHandlerMessage(LoginThread.LOGIN_TIMEOUT, 500);
                    }
                } else {
                    sendHandlerMessage(LoginThread.LOGIN_TIMEOUT, 500);
                }
            } else {
                sendHandlerMessage(LoginThread.LOGIN_TIMEOUT, 500);
            }
        }
        return sendSuccess;
    }

    /**
     * 发送到Handler消息队列
     *
     * @param arg
     * @param delaymillis
     */
    private void sendHandlerMessage(int arg, long delaymillis) {
        Message msg = mHandler.obtainMessage();
        msg.arg1 = arg;
        mHandler.sendMessageDelayed(msg, delaymillis);
    }

    /**
     * 解析用户信息
     *
     * @param packet
     */
    protected void readResultPacket(ReqIQResult packet) {
        String nameSpace = packet.getNameSpace();
        Log.i(mTag, "解析packet" + packet.toXML());
        switch (nameSpace) {
            case Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL:
                L.d("LoginActivity", "iq xml ->" + packet.toXML());
                switch (packet.getCode()) {
                    case Const.IQ_RESPONSE_CODE_SUCCESS:
                        //{"createTime":1448003882000,"email":"","gender":2,"headUrlDate":0,"joinTime":"","loginName":"刘平","mainOrg":"英语组","schoolId":"","signature":"","userName":"刘平","userNo":"8e1ab776-8d18-4d84-b79e-b5abd1cc33a5","userType":1}
                        try {
                            JSONArray tempJsonArray = new JSONArray(packet.getResp());
                            if (tempJsonArray != null && tempJsonArray.length() > 0) {
                                for (int i = 0; i < tempJsonArray.length(); i++) {
                                    JSONObject mUserObject = tempJsonArray.optJSONObject(i);
                                    L.i(mTag, "个人信息：" + mUserObject.toString());
                                    mUserNo = mUserObject.optString("userNo");
                                    Settings.System.putString(LoginActivity.this.getContentResolver(), "lastLoginNo", mUserNo);
                                    if (!TextUtils.isEmpty(mUserNo)) {
                                        mLastUserSP.saveUserAccount(mUserNo);
                                    }
                                    LastLoginUserSP.getInstance(this).saveUserType(mUserObject.optInt("userType"));
                                    LastLoginUserSP.getInstance(this).saveUserName(mUserObject.optString("userName"));
                                    SharedPreferences share = getSharedPreferences("userPost", 0);
                                    SharedPreferences.Editor editor = share.edit();
                                    editor.putString("post", mUserObject.optString("post"));
                                    editor.putString("email", mUserObject.optString("email"));
                                    editor.putString("orgName", mUserObject.optString("mainOrg"));
                                    editor.putString("userNo", mUserNo);
                                    editor.putInt("gender", mUserObject.optInt("gender"));
                                    try {
                                        editor.putString("telephone", mUserObject.getString("telephone"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        editor.putString("telephone", "");
                                    }
                                    editor.apply();
                                }

                                //如果登录名和用户id一致,并且如果是手机登录直接成功
                                isGetUserInfo = false;//获取个人信息成功
                                sendHandlerMessage(LoginThread.LOGIN_SUCCESS, 0);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Const.USER_NOT_EXISTS:
                        sendHandlerMessage(LoginThread.LOGIN_USER_NOT_EXIST, 200);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化定位信息
     */
    private LocateConfig mConfig = null;

    private void initConfigInfo() {
        final Context context = getApplicationContext();
        Map<String, String> params = new HashMap<>();
        params.put("platformSysUserId", StringUtils.substringBefore(Settings.System.getString(context.getContentResolver(), "lastLoginNo"), "_"));
        params.put("access_token", mApplication.mAppTokenStr);
        L.e("msg", "params to config url:\n" + params.toString() + "----" + Settings.System.getString(context.getContentResolver(), "lastLoginNo"));

        String configUrl = Settings.System.getString(context.getContentResolver(), "lastLoginGpsRuleUrl");
        L.e("msg", "定位配置URL：" + configUrl);
        //当地址配置错误的时候直接进入主页面,如果获取失败也进入主页面，如果发生崩溃也进入主页面
        //如果获取配置成功且配置信息不管有无都进入主页面（在信息不为空的情况下加入判断是否开启定位功能，如果开启则进入主页面如果没有弹框提示开启然后进入主页面）
        if (StringUtils.isEmpty(configUrl)) {
            L.e("msg", "CONFIG_LBS_URL_CONFIG url is empty!");
            mHandler.sendEmptyMessage(START_INTENT);
            return;
        }
        OKHttpCustomUtils.get(configUrl, params, new JSONObjectCallBack() {
            @Override
            public void onResponse(JSONObject response, int id) {
                try {
                    int status = response.getInt("status");
                    String msg = response.getString("message");

                    if (status != 0) {
                        L.w("msg", msg);
                        mHandler.sendEmptyMessage(START_INTENT);
                        return;
                    }
                    JSONObject resultObj = response.getJSONObject("result");
                    int interval;
                    interval = resultObj.getInt("gatherIntervalTime");
                    ArrayList<LocateItem> list = new ArrayList<>();
                    boolean enable = resultObj.getBoolean("collectStatus");
                    String start, end;
                    if (enable) {
                        JSONArray timeVoList = resultObj.getJSONArray("timeVOList");
                        for (int i = 0; i < timeVoList.length(); i++) {
                            JSONObject object = (JSONObject) timeVoList.get(i);
                            LocateItem item = new LocateItem();
                            start = TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATE1) + " " + object.getString("gatherStartTime");
                            end = TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATE1) + " " + object.getString("gatherEndTime");
                            item.setStart(start);
                            item.setEnd(end);
                            list.add(item);
                        }
                    } else {
                        L.e(mTag, "配置中没有启用定位");
                        L.i(mTag, "Location is disabled for current user");
                        mHandler.sendEmptyMessage(START_INTENT);
                        return;
                    }
                    L.i(mTag, "更新的个人信息：" + resultObj.toString());
                    mConfig = new LocateConfig();
                    // TODO: 2016/3/25 发布的时候需要修改这个interval
                    mConfig.setInterval(interval);
                    mConfig.setList(list);
                    mConfig.setEnable(enable);
                    mConfig.setLastUpdate(new Date());
                    startService(new Intent(LoginActivity.this, LocateService.class));
                    if (mConfig != null) {
                        textGPS();
                    } else {
                        mHandler.sendEmptyMessage(START_INTENT);
                    }
                } catch (JSONException e) {
                    mConfig = null;
                    mHandler.sendEmptyMessage(START_INTENT);
                }

            }

            @Override
            public void onError(Call call, Exception e, int i) {
                super.onError(call, e, i);
                mHandler.sendEmptyMessage(START_INTENT);
            }
        });
    }

    protected void textGPS() {
        if (!SystemUtil.getGpsState(mContext)) {//是否打开GPS
            openGPSdialog();
        } else {
            mHandler.sendEmptyMessage(START_INTENT);
        }
    }

    protected AlertDialog mOpenGpsAlertDialog;

    private void openGPSdialog() {
        //初始化提示打开GPS的对话框
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog_Alert);
        dialogBuilder.setTitle(R.string.gpsAlert);
        dialogBuilder.setMessage(R.string.gpsAlertContent);
        dialogBuilder.setIcon(R.mipmap.ic_launcher);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mHandler.sendEmptyMessage(START_INTENT);
            }
        });
        dialogBuilder.setPositiveButton(R.string.gpsAlertGoSettings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 转到手机设置界面，用户设置GPS
                startGpsIndex = true;
                dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        mOpenGpsAlertDialog = dialogBuilder.create();
        mOpenGpsAlertDialog.show();
    }

    /**
     * 取消登录
     */
    public void cancelLogin() {
        if (mLoginRunable != null) {
            L.i(mTag, "取消登录");
            mLoginRunable.setCancelLogin(true);
            hidProgess();
            mHandler.removeMessages(START_MAINACTIVITY);
        }
        if (mIsUserNoLoginSuccess) {
            disconOpenfire(true);
        }
    }


    @Override
    public void onBackPressed() {
        cancelLogin();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginUser login) {
        if (login == null || login.getAccount() == null) {
            return;
        }
        switch (login.getAccount()) {
            case "0"://隐藏进度圈并回到登录界面
                hidProgess();
                break;
            case "1"://进入登录流程

                mXmppConnectionManager.init(LoginThread.getHostFromAddress(saveOpenFireHost),
                        LoginThread.getPortFromAddress(saveOpenFireHost), Const.SERVICENAME);
                L.i(mTag, "初始化的信息：" + LoginThread.getHostFromAddress(saveOpenFireHost) + " -- " + LoginThread.getPortFromAddress(saveOpenFireHost));

                if (loginType == LoginConfig.LOGIN_TYPE_PHONE) {
                    hidProgess();
                    //验证手机验证码是否正确
                    loginPhone(appController.mAppTokenStr);
                } else {
                    loginAccount(appController.mAppTokenStr);
                }

                break;
            default:
                break;
        }
    }

    /**
     * 初始化配置app链接
     */
    private void configConstantUrl() {

        String landSysUrl = StringUtils.removeEnd(login_isSetting_service_address.getText().toString(), "/");
        String openfireAddr = LastLoginUserSP.getServerInfoOpenFireHost(mContext);

        mApplication.CONFIG_INSIDE_IP = openfireAddr;
        L.d(mTag, "CONFIG_INSIDE_IP -> " + mApplication.CONFIG_INSIDE_IP);

        mApplication.CONFIG_YNEDUT_V8_URL = StringUtils.endsWith(landSysUrl, "/") ? landSysUrl : landSysUrl + "/";
        L.d(mTag, "CONFIG_YNEDUT_V8_URL -> " + mApplication.CONFIG_YNEDUT_V8_URL);

        mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST = mApplication.CONFIG_YNEDUT_V8_URL;
        L.d(mTag, "CONFIG_YNEDUT_V8_SERVICE_HOST -> " + mApplication.CONFIG_YNEDUT_V8_SERVICE_HOST);

        //文件传输
        String split[] = StringUtils.split(openfireAddr, ':');
        String ip = split.length > 0 ? split[0] : StringUtils.EMPTY;
        String port = "80";
        if (landSysUrl.split("/")[2].contains(":")) {
            port = landSysUrl.split("/")[2].split(":")[1];
        }
        mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP = ip + ":" + port;
        L.d(mTag, "CONFIG_INSIDE_FILE_TRANSLATE_IP -> " + mApplication.CONFIG_INSIDE_FILE_TRANSLATE_IP);
        //调转到V8指定页面
        mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL = landSysUrl + URLs.V8_POSITON_PAGE;
        L.d(mTag, "CONFIG_YNEDUT_V8_APP_PAGE_URL -> " + mApplication.CONFIG_YNEDUT_V8_APP_PAGE_URL);
        //应用中心访问V8获取可用菜单
        mApplication.CONFIG_YNEDUT_V8_APP_MENUS_URL = landSysUrl + URLs.APP_CENTER_V8_MENU;
        L.d(mTag, "CONFIG_YNEDUT_V8_APP_MENUS_URL -> " + mApplication.CONFIG_YNEDUT_V8_APP_MENUS_URL);
        // 获取OA待办的地址
        mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL = landSysUrl + URLs.OA_TODO_ANDROID;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_OA_URL -> " + mApplication.CONFIG_YNEDUT_V8_EVENT_OA_URL);
        //跳转OA详情的地址,pc端使用的
        mApplication.CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL = landSysUrl + URLs.OA_TO_DETAIL;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL -> "
                + mApplication.CONFIG_YNEDUT_V8_EVENT_OA_DETAIL_URL);
        mApplication.CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL = landSysUrl + URLs.JUMP_OA_POSITION_PAGE;
        L.d(mTag, "CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL -> "
                + mApplication.CONFIG_YNEDUT_V8_EVENT_V7OA_DETAIL_URL);
        mApplication.CONFIG_LBS_URL_CONFIG = landSysUrl + URLs.GPS_GET_RULE;
        LastLoginUserSP.saveGpsRuleUrl(mContext, mApplication.CONFIG_LBS_URL_CONFIG);
        Settings.System.putString(LoginActivity.this.getContentResolver(), "lastLoginGpsRuleUrl", landSysUrl + URLs.GPS_GET_RULE);
        L.d(mTag, "CONFIG_LBS_URL_CONFIG -> " + mApplication.CONFIG_LBS_URL_CONFIG);
        mApplication.CONFIG_LBS_URL_BATH_UPLOAD = landSysUrl + URLs.GPS_UPLOAD_MULTIPTE;
        LastLoginUserSP.saveBathGpsSubmitUrl(mContext, mApplication.CONFIG_LBS_URL_BATH_UPLOAD);
        Settings.System.putString(LoginActivity.this.getContentResolver(), "lastLoginBathGpsSubmitUrl", landSysUrl + URLs.GPS_UPLOAD_MULTIPTE);
        L.d(mTag, "CONFIG_LBS_URL_BATH_UPLOAD -> " + mApplication.CONFIG_LBS_URL_BATH_UPLOAD);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_ib_select_account://选择账户
                secletAccount();
                break;
            case R.id.login_update_service_address://修改服务器地址
                ll_login_info.setVisibility(View.GONE);
                ll_login_setting_service_address.setVisibility(View.VISIBLE);
                if (mUserLoginedServiceUrls.size() > 0) {
                    history_service_address.setVisibility(View.VISIBLE);
                    login_iv_history.setVisibility(View.VISIBLE);
                } else {
                    history_service_address.setVisibility(View.INVISIBLE);
                    login_iv_history.setVisibility(View.INVISIBLE);
                }
                ll_login_setting_service_address.startAnimation(rightInAnimation);
                ll_login_info.startAnimation(leftOutAnimation);
                if (!login_isSetting_service_address.getText().toString().trim().equals("未设置")) {
                    login_service_address.setText(login_isSetting_service_address.getText().toString().trim());
                    login_service_address.setSelection(login_isSetting_service_address.getText().toString().trim().length());
                }
                break;
            case R.id.login_btn_login:
                //登录
                if (isLoginLoading) {
                    cancelLogin();
                } else {
                    Login();
                }
                break;
            case R.id.history_service_address:
                //历史记录
                secletService();
                break;
            case R.id.login_sao_miao:
                //二维码扫描
                Intent intent = new Intent();
                intent.setClass(this, MipcaActivityCapture.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
//                requestCameraPermissions();
                break;
            case R.id.login_cancel:
                //取消
                ll_login_info.setVisibility(View.VISIBLE);
                ll_login_setting_service_address.setVisibility(View.GONE);
                ll_login_info.startAnimation(leftInAnimation);
                ll_login_setting_service_address.startAnimation(rightOutAnimation);
                break;
            case R.id.login_save:
                //保存设置
                if (TextUtils.isEmpty(login_service_address.getText().toString().trim())) {
                    ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getString(R.string.please_input_server_address2), 2000);
                    return;
                }
                if (!StringUtils.startsWithIgnoreCase(login_service_address.getText().toString().trim(), "http://") &&
                        !StringUtils.startsWithIgnoreCase(login_service_address.getText().toString().trim(), "https://")) {
                    ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), "地址必须以http://或https://开头", 2000);
                    return;
                }
                //保存服务器地址
                String url = TextUtil.replaceSlash(TextUtil.replaceBlank(login_service_address.getText().toString()));
                login_isSetting_service_address.setText(url);
                LastLoginUserSP.getInstance(mContext).saveUserServicesAddress(url);
                mUserLoginedServiceUrls = LastLoginUserSP.getUserServerUrls(mContext);
                if (mUserLoginedServiceUrls == null || mUserLoginedServiceUrls.size() == 0) {
                    mUserLoginedServiceUrls = new HashSet<>();
                    mUserLoginedServiceUrls.add(url);
                } else {
                    //遍历已经保存的服务器地址
                    for (Iterator it = mUserLoginedServiceUrls.iterator(); it.hasNext(); ) {
                        if (!it.next().toString().equals(url)) {
                            mUserLoginedServiceUrls.add(url);
                            break;
                        }
                    }
                }
                LastLoginUserSP.setUserServerUrls(mContext, mUserLoginedServiceUrls);
                //重新刷新一次url
                V8ContextAddress.v8Address = null;
                try {
                    V8ContextAddress.bind(getApplicationContext(), URLs.class);
                } catch (MalformedURLException e) {
                    ToastUtil.toastAlerMessageCenter(this, "服务器地址格式错误", 300);
                    return;
                }
                //获取服务器登录配置
                syncServerLoginConfig();
                break;
            case R.id.login_banner:
                //如果图片信息是空，或者获取不到url则不跳转
                if (imageInfo == null) {
                    return;
                }
                if (StringUtils.isEmpty(imageInfo.get("url"))) {
                    return;
                }
                //广告跳转
                Intent it = new Intent(LoginActivity.this, X5WebAppActivity.class);
                it.putExtra("Url", imageInfo.get("url"));
                startActivity(it);
                break;
            case R.id.login_type_txt:
                //切换登录方式
                if (loginType == LoginConfig.LOGIN_TYPE_ACCOUNT) {
                    loginType = LoginConfig.LOGIN_TYPE_PHONE;
                } else {
                    loginType = LoginConfig.LOGIN_TYPE_ACCOUNT;
                }
                changeLoginTypeViewAnim(true);
                break;
            case R.id.login_get_verification:
                String mPhoneNum = login_et_username.getText().toString().trim();
                if (TextUtils.isEmpty(mPhoneNum)) {
                    login_et_username.requestFocus();
                    ToastUtil.toastAlerMessageiconTop(mContext,
                            LoginActivity.this.getLayoutInflater(),
                            getString(R.string.login_errortext_emptyphone_check),
                            2000);
                    return;
                }
                //判断电话号是否正确
                if (!TextUtils.isEmpty(mPhoneNum) && !PhoneFormatCheckUtils.isChinaPhoneLegal(mPhoneNum)) {
                    if (!TextUtils.isEmpty(mPhoneNum) && !PhoneFormatCheckUtils.isChinaPhoneLegal(mPhoneNum)) {
                        login_et_username.requestFocus();
                        ToastUtil.toastAlerMessageiconTop(mContext,
                                LoginActivity.this.getLayoutInflater(),
                                getString(R.string.login_errortext_emptyphone_check),
                                2000);
                        return;
                    }
                }
                phoneNum = mPhoneNum;
                //获取验证码
                getVerification();
                break;
            case R.id.login_back_view:
                //返回登录界面
                showMoreAccountView(false);
                break;
            case R.id.lin_server_help:
                //服务器地址帮助文档
                Intent helpIntent = new Intent(this, HelpActivity.class);
                helpIntent.putExtra("helpType", HelpActivity.HELP_TYPE_SERVER);
                startActivity(helpIntent);
                break;
            case R.id.txt_phone_help:
                //手机动态码帮助文档
                Intent phoneIntent = new Intent(this, HelpActivity.class);
                phoneIntent.putExtra("helpType", HelpActivity.HELP_TYPE_PHONE);
                startActivity(phoneIntent);
                break;

            default:
                break;
        }
    }

    /**
     * 获取服务器登录配置
     */
    private void syncServerLoginConfig() {
        mServiceAddress = TextUtil.replaceSlash(TextUtil.replaceBlank(login_service_address.getText().toString()));
        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                isLoginLoading = true;
                loading_login.setText("检测中...");
                login_progressbar.setVisibility(View.VISIBLE);
                mApplication.CONFIG_YNEDUT_V8_URL = mServiceAddress + "/";
            }

            @Override
            protected String doInBackground(Void... params) {
                return V8TokenManager.forceRefresh();
            }

            @Override
            protected void onPostExecute(String token) {
                if (StringUtils.isEmpty(token)) {
                    hidProgess();
                    ToastUtil.toastAlerMessageiconTop(LoginActivity.this, getLayoutInflater(), getString(R.string.server_connection_fail), 2000);
                    return;
                }
                //获取服务器登录配置
                String version = "V1.0";
                Map<String, String> params = new HashMap<>();
                params.put("version", version);
                params.put("access_token", token);
                OKHttpCustomUtils.get(URLs.GET_SERVICE_LOGIN_CONFIG, params, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject response, int id) {
                        JSONObject resultObj = null;
                        try {
                            resultObj = response.getJSONObject("result");

                            LoginConfig loginConfig = JSON.parseObject(resultObj.getString("loginRule"), LoginConfig.class);
                            int lastGetVerNum;
                            try {
                                lastGetVerNum = mLastUserSP.getServiceLoginConfig().getLastGetVerNum();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                                lastGetVerNum = 0;
                            }
                            loginConfig.setLastGetVerNum(lastGetVerNum);
                            mLastUserSP.saveServiceLoginConfig(loginConfig);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            ToastUtil.toastAlerMessageCenter(LoginActivity.this, "无法获取服务器配置", 300);
                        }
                        ll_login_info.setVisibility(View.VISIBLE);
                        ll_login_setting_service_address.setVisibility(View.GONE);
                        ll_login_info.startAnimation(leftInAnimation);
                        ll_login_setting_service_address.startAnimation(rightOutAnimation);
                        initUserInfo();
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        Log.i(mTag, "获取服务器配置返回配置错误:" + e.getMessage()+" "+ getResources().getString(R.string.request_error_config));
                    }

                    @Override
                    public void onAfter(int id) {
                        hidProgess();
                    }
                });
            }
        };
        AsyncTaskCompat.executeParallel(tokenTask);
    }

    /**
     * 获取验证码
     */
    private boolean isGetVerificationing = false;
    private LoginConfig loginConfig = null;
    private int getPhoneCodeNum = 0;
    private CountDownTimerUtil mCountDownTimerUtil;

    private void getVerification() {

        //获取短信验证码
        mSmsReciver.setOnSmsPhoneCodeListener(new SmsReciver.OnSmsPhoneCodeListener() {
            @Override
            public void onReciverPhoneCode(String code) {
                mobileCode = code;
                login_et_password.requestFocus();
                login_et_password.setText(code);
                login_et_password.setSelection(code.length());
            }
        });

        @SuppressLint("StaticFieldLeak")
        AsyncTask<Void, Void, String> tokenTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                mApplication.CONFIG_YNEDUT_V8_URL = mServiceAddress + "/";
            }

            @Override
            protected String doInBackground(Void... params) {
                return V8TokenManager.forceRefresh();
            }

            @Override
            protected void onPostExecute(String token) {
                if (StringUtils.isBlank(token)) {
                    ToastUtil.toastAlerMessageiconTop(LoginActivity.this, getLayoutInflater(), getString(R.string.request_failed), 2000);
                    L.e(mTag, "500");
                    return;
                }

                loginConfig = mLastUserSP.getServiceLoginConfig();
                //增加获取短信的次数
                getPhoneCodeNum = loginConfig.getLastGetVerNum();
                getPhoneCodeNum++;
                //判断短信验证
                final int configCodeNum = loginConfig.getShowValidateCodeNumber();
                if (getPhoneCodeNum > configCodeNum) {
                    VerificationDialog dialog = new VerificationDialog(mContext, R.style.MyDialogStyleBottom);
                    //成功
                    dialog.setmVerificationListener(new VerificationDialog.VerificationListener() {
                        @Override
                        public void verificationSuccess() {
                            getPhoneCodeNum = configCodeNum - 1;
                            loginConfig.setLastGetVerNum(getPhoneCodeNum);
                            mLastUserSP.saveServiceLoginConfig(loginConfig);
                            getVerification();
                        }
                    });
                    dialog.show();
                    return;
                }

                login_get_verification.setBackgroundResource(R.drawable.login_activity_verification_press_background);
                login_get_verification.setTextColor(getResources().getColor(R.color.common_text_line));
                login_get_verification.setClickable(false);

                if (mCountDownTimerUtil != null) {
                    mCountDownTimerUtil.cancel();
                    //防止new出多个导致时间跳动加速
                    mCountDownTimerUtil = null;
                }
                mCountDownTimerUtil = new CountDownTimerUtil(60000, 1000, login_get_verification);
                mCountDownTimerUtil.start();
                mCountDownTimerUtil.setShowTxt("秒重发");
                isGetVerificationing = true;
                mCountDownTimerUtil.setOnCountDownTimerFinish(new CountDownTimerUtil.OnCountDownTimerFinish() {
                    @Override
                    public void onTimeFinish() {
                        isGetVerificationing = false;
                        login_get_verification.setClickable(true);
                        login_get_verification.setText(R.string.login_get_verification);
                        login_get_verification.setBackgroundResource(R.drawable.login_activity_verification_nor_background);
                        login_get_verification.setTextColor(getResources().getColor(R.color.actionBar_bg));
                    }
                });
                txt_phone_help.setVisibility(View.VISIBLE);

                //获取短信码
                Map<String, String> params = new HashMap<>();
                params.put("access_token", token);
                params.put("mobile", phoneNum);
                params.put("terminalType", 2 + "");//移动端是2
                OKHttpCustomUtils.get(URLs.GET_PHONE_DYNAMIC_CODE, params, new JSONObjectCallBack() {
                    @Override
                    public void onResponse(JSONObject response, int id) {
                        Log.i(mTag, "验证码获取信息:" + response.toString());
                        try {
                            JSONObject resultObj = response.getJSONObject("result");
                            String resultCode = resultObj.getString("errcode");
                            String errMsg = resultObj.getString("errmsg");
                            //获取手机号错误
                            if (!"0".equals(resultCode)) {
                                mCountDownTimerUtil.onFinish();
                                mCountDownTimerUtil.cancel();
                                ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), errMsg, 500);
                            } else {
                                ToastUtil.toastAlerMessageiconTop(mContext, getLayoutInflater(), getString(R.string.phone_send_success), 500);
                                //增加获取短信的次数
                                loginConfig.setLastGetVerNum(getPhoneCodeNum);
                                mLastUserSP.saveServiceLoginConfig(loginConfig);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Call call, Exception e, int i) {
                        super.onError(call, e, i);
                        Log.i(mTag, "验证码获取错误信息:" + e.getMessage());
                        AppToastUtils.getInstance(mContext).toastForPhoneTop(Const.PHONE_SEND_CODE_ERROR);
                        mCountDownTimerUtil.onFinish();
                        mCountDownTimerUtil.cancel();

                    }
                });
            }
        };
        AsyncTaskCompat.executeParallel(tokenTask);
    }

    /**
     * 修改不同登录方式的ui
     */
    private void changeLoginTypeViewAnim(boolean isAnim) {
        //如果是手机验验证码登录
        int duration = 0;
        if (isAnim) {
            duration = 300;
        }
        if (loginType == LoginConfig.LOGIN_TYPE_PHONE) {
            login_et_username.setHint(R.string.login_evtext_phonenum);
            login_et_username.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_CLASS_NUMBER);
            login_et_password.setInputType(InputType.TYPE_CLASS_NUMBER);
            login_et_password.setHint(R.string.login_evtext_verification);
            login_et_username.setText("");
            login_et_password.setText("");
            //如果动态码和手机号不为空就不用清空
            if (!StringUtils.isEmpty(phoneNum)) {
                login_et_username.setText(phoneNum);
                login_et_username.setSelection(phoneNum.length());
            }
            login_type_txt.setText(R.string.login_account_type);
            ObjectAnimator phoneIconAnim = ObjectAnimator.ofFloat(login_img_phone, "translationY", 0, DensityUtil.dip2px(this, -23));
            ObjectAnimator userIconAnim = ObjectAnimator.ofFloat(login_img_user, "translationY", 0, DensityUtil.dip2px(this, -23));
            ObjectAnimator selectIconAnim = ObjectAnimator.ofFloat(login_ib_select_account, "translationY", 0, DensityUtil.dip2px(this, -30));
            ObjectAnimator verifiIconAnim = ObjectAnimator.ofFloat(login_get_verification, "translationY", 0, DensityUtil.dip2px(this, -30) - 1);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(duration);
            set.play(phoneIconAnim).with(userIconAnim).with(verifiIconAnim).with(selectIconAnim);
            set.start();
        } else {
            login_et_username.setHint(R.string.login_evtext_username);
            login_et_password.setHint(R.string.login_evtext_passowrd);
            login_et_username.setInputType(InputType.TYPE_CLASS_TEXT);
            login_et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            login_et_username.setText("");
            login_et_password.setText("");
            if (!StringUtils.isEmpty(account)) {
                LoginUser loginUser = mLoginUserDao.getLoginUserByAccount(account);
                if (loginUser != null) {
                    if (loginUser.getIsUserAccountType() == 1) {
                        login_et_username.setText(account);
                        login_et_username.setSelection(account.length());
                    }
                }
            }

            txt_phone_help.setVisibility(View.GONE);
            login_type_txt.setText(R.string.login_phonenum_type);
            ObjectAnimator phoneIconAnim = ObjectAnimator.ofFloat(login_img_phone, "translationY", DensityUtil.dip2px(this, -23), 0);
            ObjectAnimator userIconAnim = ObjectAnimator.ofFloat(login_img_user, "translationY", DensityUtil.dip2px(this, -23), 0);
            ObjectAnimator selectIconAnim = ObjectAnimator.ofFloat(login_ib_select_account, "translationY", DensityUtil.dip2px(this, -30), 0);
            ObjectAnimator verifiIconAnim = ObjectAnimator.ofFloat(login_get_verification, "translationY", DensityUtil.dip2px(this, -30) - 1, 0);
            AnimatorSet set = new AnimatorSet();
            set.setDuration(duration);
            set.play(phoneIconAnim).with(userIconAnim).with(selectIconAnim).with(verifiIconAnim);
            set.start();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //把中文符号替换成英文符号
        String editStr = login_service_address.getText().toString();
        if (editStr.contains("。") || editStr.contains("：")) {
            String newStr = editStr.replaceAll("：", ":").replaceAll("。", ".");
            login_service_address.setText(newStr);
            login_service_address.setSelection(editStr.length());
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (LastLoginUserSP.getUserServerUrls(mContext) != null) {
            if (LastLoginUserSP.getUserServerUrls(mContext).size() <= 0) {
                if (!TextUtils.isEmpty(login_service_address.getText().toString().trim())) {
                    if (!isUpdate) {
                        login_cancel.setVisibility(View.VISIBLE);
                    }
                } else {
                    login_cancel.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * 选择账户
     */
    private void secletAccount() {
        if (mUserList.size() <= 0) {
            return;
        }
        login_ib_select_account.setImageResource(R.mipmap.icon_login_shangla);
        popListView.setVerticalScrollBarEnabled(false);
        if (mLoginUserAdapter == null) {
            mLoginUserAdapter = new MyAdapter(this, mUserList, this);
            popListView.setDividerHeight(0);
            popListView.setAdapter(mLoginUserAdapter);
        } else {
            mLoginUserAdapter.notifyDataSetChanged();
        }
        int margin = getResources().getDimensionPixelSize(R.dimen.login_usericon_padding);
        int popWidht = ll_login_useraccount.getWidth() - 2 * margin;
        setSelectAccountHeight(popListView, mLoginUserAdapter);
        if (mUserAccountPop == null) {
            mUserAccountPop = new PopupWindow(popView, popWidht, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mUserAccountPop.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5f5f5")));
            mUserAccountPop.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    login_ib_select_account.setImageResource(R.mipmap.icon_login_xiala);
                }
            });
        }
        mUserAccountPop.showAsDropDown(ll_login_useraccount, margin, 0);
    }

    /**
     * 选择登录服务器
     */
    private void secletService() {
        login_iv_history.setImageResource(R.mipmap.icon_server_dropup);
        address = new ArrayList<String>(mUserLoginedServiceUrls);
        if (addressListView.getAdapter() == null) {
            mLoginServersAdapter = new ServiceUrlAdapter(this, this);
            mLoginServersAdapter.setData(address);
            addressListView.setDividerHeight(0);
            addressListView.setAdapter(mLoginServersAdapter);
        } else {
            mLoginServersAdapter.setData(address);
            mLoginServersAdapter.notifyDataSetChanged();
        }
        int margin = getResources().getDimensionPixelSize(R.dimen.login_usericon_padding);
        int popWidht = ll_login_servers.getWidth() - 2 * margin;
        setSelectAccountHeight(addressListView, mLoginServersAdapter);
        if (mUserAddressPop == null) {
            mUserAddressPop = new PopupWindow(addressView, popWidht, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            mUserAddressPop.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f5f5f5")));
            mUserAddressPop.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    login_iv_history.setImageResource(R.mipmap.icon_server_dropdwon);
                }
            });
        }
        mUserAddressPop.showAsDropDown(ll_login_servers, margin, 0);
    }


    /**
     * 动态设置LISTVEIW高度
     */
    private void setSelectAccountHeight(ListView listView, ListAdapter adapter) {
        if (login_ib_select_account.getBackground() == null) {
            return;
        }
        int totalHeight = 0;
        if (adapter.getCount() > 0 && adapter.getCount() < 3) {//item小于3大于0时，高度自适应
            totalHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            for (int i = 0, len = adapter.getCount(); i < len; i++) { // listAdapter.getCount()返回数据项的数目
                View listItem = adapter.getView(i, null, listView);
                listItem.measure(0, 0); // 计算子项View 的宽高
                totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
            }
            totalHeight = totalHeight / adapter.getCount() * 3;//默认显示3行
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
    }

    /**
     * 删除帐号信息的dialog
     */
    private void showAccountDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_login_dialog, null);
        LinearLayout cancel = (LinearLayout) view.findViewById(R.id.cancel);
        LinearLayout sure = (LinearLayout) view.findViewById(R.id.sure);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.delete_local_info);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isDelete = isChecked;
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        sure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                deleteSpAndDao(isDelete);
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);
        dialog.setView(view);
        dialog.show();
    }

    @Override
    public void OnClicklistener(String account, LoginUser user) {
        mDeleteLoginUser = user;
        showAccountDialog();
        mUserAccountPop.dismiss();
        mLoginUserAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String qrCodeResult = bundle.getString("result");
                    if (qrCodeResult != null && !"null".equals(qrCodeResult)) {
                        String qrCodeSign = getString(R.string.qeCodeSignName);
                        if (qrCodeResult.startsWith(qrCodeSign)) {
                            login_service_address.setText(qrCodeResult.split(qrCodeSign)[1]);
                        } else {
                            ToastUtil.toastAlerMessageiconTop(this, LoginActivity.this.getLayoutInflater(), getString(R.string.qrCodeError), 2000);
                        }
                    }
                }
                break;

            default:
                break;
        }
    }

    /**
     * 删除数据
     *
     * @param deleteAll 是否全部删除
     */
    private void deleteSpAndDao(boolean deleteAll) {
        //清除webview缓存
        clearWebCacheData();
        if (mUserList != null && mDeleteLoginUser != null && mUserList.contains(mDeleteLoginUser)) {

            //删除loginuser表中的该条记录
            mLoginUserDao.deleteByAccount(mDeleteLoginUser.getAccount());

            //删除手机中该帐号的文件
            if (deleteAll) {
                String dbPath = (UserAccountDB.getInstance(this, mDeleteLoginUser.getUserNo()))
                        .getWritableDatabase().getPath();
                UserAccountDB.setNullInstance();// 释放数据库文件句柄
                //删除所属帐号的下载文件夹
                FileUtil.delFolder(FileUtil.getFilePathByUserAccount(mDeleteLoginUser.getAccount()));
                mLoginUserDao.deleteByAccount(mDeleteLoginUser.getAccount());
                if (dbPath != null) {// 删除数据库文件
                    new File(dbPath).delete();
                }
            }

            LastLoginUserSP sp = LastLoginUserSP.getInstance(this);
            if (sp.getUserAccount().equals(mDeleteLoginUser.getUserNo())) {// 如果删除的是当前帐号，清空当前帐号
                sp.saveUserLoginAccount("");
                sp.saveUserAccount("");
                sp.saveUserPassword("");
            }
            //删除最近会话列表
            RecentChatDao chatDao = new RecentChatDao(this);
            chatDao.deleteRecentChatByUserNo(mDeleteLoginUser.getUserNo());
            mUserList.remove(mDeleteLoginUser);
            mLoginUserAdapter.notifyDataSetChanged();
            if (mUserList.size() == 0) {
                login_ib_select_account.setImageDrawable(null);
                login_et_username.setText("");
                login_et_password.setText("");
            } else {
                LoginUser tempLoginUser = mUserList.get(0);
                login_et_username.setText(tempLoginUser.getAccount());
                login_et_username.setSelection(tempLoginUser.getAccount().length());
                LoginUser loginUser = mLoginUserDao.getLoginUserByAccount(tempLoginUser.getAccount());
                //手机登录不允许显示密码
                if (loginUser.getIsUserAccountType() == 1) {
                    login_et_password.setText(tempLoginUser.getPassWord());
                } else {
                    login_et_password.setText("");
                }
            }
        }
    }

    @Override
    public void OnClickListener(String url) {
        String deleteServer = url;
        if (mUserLoginedServiceUrls == null) {
            return;
        }
        address.remove(deleteServer);
        mUserLoginedServiceUrls.remove(deleteServer);
        LastLoginUserSP.setUserServerUrls(mContext, mUserLoginedServiceUrls);
        if (address.size() == 0) {
//            login_service_address.setText("");
            mLastUserSP.saveUserServicesAddress("");
            mUserAddressPop.dismiss();
            login_iv_history.setVisibility(View.GONE);
            history_service_address.setVisibility(View.GONE);
        } else {
            //如果删除的地址和编辑框里的地址相同，则更改编辑框内容
            if (login_service_address.getEditableText().toString().equals(deleteServer)) {
//                login_service_address.setText((String) address.get(0));
                mLastUserSP.saveUserServicesAddress((String) address.get(0));
            }
            mLoginServersAdapter.notifyDataSetChanged();
        }
        if (url == null) {
            address.remove(url);
            mUserLoginedServiceUrls.remove(url);
            LastLoginUserSP.setUserServerUrls(mContext, mUserLoginedServiceUrls);
            mLoginServersAdapter.notifyDataSetChanged();
        }
        mUserAddressPop.dismiss();
        mLoginServersAdapter.notifyDataSetChanged();
    }


}
