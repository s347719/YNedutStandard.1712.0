package com.yineng.ynmessager.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.service.LocateConfig;
import com.yineng.ynmessager.bean.service.LocateItem;
import com.yineng.ynmessager.bean.service.Location;
import com.yineng.ynmessager.db.LocationsTb;
import com.yineng.ynmessager.db.dao.LocationsTbDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.AppUtils;
import com.yineng.ynmessager.util.Base64PasswordUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.SystemUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//  2016/3/25 发布时需要在百度控制台里面添加用于正式发布的keystore对应的API KEY

/**
 * 定位后台服务
 * <p>Created by 贺毅柳 on 2016/3/8 17:03.</p>
 */
public class LocateService extends Service {
    public static final String TAG = "LocateService";
    private LocateConfig mConfig = null;
    private LocationClient mLocationClient = null;
    private MyLocationListener mMyLocationListener;
    private LocationRequestHandler mLocationRequestHandler;
    private AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private LocationsTbDao mLocationsTb;
    LocationClientOption option;
    private HomeKeyListener mHomeKeyListener;
    private boolean mIsFirstInitConfig = false;
    private List<Location> updateList = new ArrayList<>();//上传地址的list
    private List<Location> addressList = new ArrayList<>();//数据库空地址的list
    private List<Location> locations;//获取数据库地址的list
    private boolean isLogout = false;//用户是否注销
    //定时器
    private Timer timer = new Timer();
    static TimerTask task;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                final Context context = getApplicationContext();
                //初始化上传配置
                String rsglSysUserId = StringUtils.substringBefore(Settings.System.getString(context.getContentResolver(), "lastLoginNo"), "_");
                String longAndLatVal;
                //批量上传
                final JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject;
                try {
                    for (Location mLocation : updateList) {
                        Log.e(TAG, "上传地址：" + mLocation.getAddress() + ",ID  = " + mLocation.getId() + ",时间：" + mLocation.getTimestamp());
                        jsonObject = new JSONObject();
                        jsonObject.put("rsglSysUserId", rsglSysUserId);
                        double longitude = mLocation.getLongitude();
                        double latitude = mLocation.getLatitude();
                        if (longitude == 0 || latitude == 0) {
                            longAndLatVal = StringUtils.EMPTY;
                        } else {
                            longAndLatVal = longitude + "," + latitude;
                        }
                        jsonObject.put("longAndLat", longAndLatVal);
                        jsonObject.put("gpsIsOpen", mLocation.isGpsOpen() ? 1 : 0);
                        jsonObject.put("lastCollectTime", DateFormatUtils.format(mLocation.getTimestamp(), "yyyy/MM/dd HH:mm:ss"));
                        jsonObject.put("address", mLocation.getAddress());
                        jsonObject.put("errorRange", mLocation.getRadius());
                        jsonObject.put("phoneNumber", "");
                        jsonArray.put(jsonObject);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String uploadUrl = Settings.System.getString(context.getContentResolver(), "lastLoginBathGpsSubmitUrl");
                if (StringUtils.isEmpty(uploadUrl)) {
                    L.w(TAG, "CONFIG_LBS_URL_UPLOAD url is empty!");
                    return;
                }
                L.e("MSG", "批量提交地址：" + uploadUrl);
                RequestParams params = new RequestParams();
                params.put("encodeStr", Base64PasswordUtil.encode(jsonArray.toString()));
                params.put("access_token", AppController.getInstance().mAppTokenStr);
                mHttpClient.post(uploadUrl, params, new TextHttpResponseHandler() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        L.i(TAG, "Start uploading");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        L.w(TAG, "upload failed\n" + responseString);
                        //清空查询数据库得到的数据数组
                        locations.clear();
                        //清空保存上传数据的数组
                        updateList.clear();
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String responseString) {
                        L.e(TAG, "批量提交结果：" + responseString);
                        //如果服务器返回状态正常，则从本地数据库中删除这条记录
                        try {
                            JSONObject respObj = new JSONObject(responseString);
                            int status = Integer.parseInt(respObj.getString("status"));
                            if (status == 0) {
                                L.e(TAG, "批量上传成功");
                                //服务器返回上传成功，就本地删除掉数据库中存储的数据
                                for (Location ll : updateList) {
                                    int index = mLocationsTb.delete(ll);
                                    L.e(TAG, "删除数据：----" + index);
                                }
                                //清空查询数据库得到的数据数组
                                locations.clear();
                                //清空保存上传数据的数组
                                updateList.clear();
                            }
                        } catch (JSONException e) {
                            L.e(TAG, e.getMessage(), e);
                        }
                    }
                });
            }
        }
    };

    /**
     * 守护服务死亡广播接收器
     */
    private BroadcastReceiver mGuardDeathReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (StringUtils.equals(Const.GUARD_ACTION_DEATH, action)) {
                startService(new Intent(context, GuardService.class));//启动守护服务
            }
        }
    };

    /**
     * 用户注销广播接收器
     */
    private BroadcastReceiver mUserLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            L.v(TAG, "UserLogoutReceiver onReceive");
            String action = intent.getAction();
            if (StringUtils.equals(Const.BROADCAST_ACTION_USER_LOGOUT, action)) {
                L.i(TAG, "Locating is going to stop(Handler stop sending message)");
                isLogout = true;//用户注销
                //清空相关内存对象
                mConfig = null;
                mLocationsTb = null;
                //从Handler中移除请求定位的Message
                mLocationRequestHandler.removeMessages(LocationRequestHandler.WHAT);
                mMyLocationListener.tmpLocations.clear();
                if (mLocationClient.isStarted()) mLocationClient.stop();
                LocateService.this.stopSelf();
            }
        }
    };

    /**
     * 控制台打印输出 {@link BDLocation} 对象的相关信息
     */
    private static void debugPrintLocation(@NonNull BDLocation bdLocation) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("time : ");
        sb.append(DateFormatUtils.format(System.currentTimeMillis(), "HH:mm:ss"));
        sb.append("\nerror code : ");
        sb.append(bdLocation.getLocType());
        sb.append("\nlatitude : ");
        sb.append(bdLocation.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(bdLocation.getLongitude());
        sb.append("\nradius : ");
        sb.append(bdLocation.getRadius());
        if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
            sb.append("\nspeed : ");
            sb.append(bdLocation.getSpeed());// 单位：公里每小时
            sb.append("\nsatellite : ");
            sb.append(bdLocation.getSatelliteNumber());
            sb.append("\nheight : ");
            sb.append(bdLocation.getAltitude());// 单位：米
            sb.append("\ndirection : ");
            sb.append(bdLocation.getDirection());// 单位度
            sb.append("\naddr : ");
            sb.append(bdLocation.getAddrStr() + StringUtils.SPACE + StringUtils.defaultString(
                    bdLocation.getLocationDescribe()));
            sb.append("\ndescribe : ");
            sb.append("gps定位成功");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
            sb.append("\naddr : ");
            sb.append(bdLocation.getAddrStr() + StringUtils.SPACE + StringUtils.defaultString(
                    bdLocation.getLocationDescribe()));
            //运营商信息
            sb.append("\noperationers : ");
            sb.append(bdLocation.getOperators());
            sb.append("\ndescribe : ");
            sb.append("网络定位成功");
        } else if (bdLocation.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
        } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        sb.append("\nlocationdescribe : ");
        sb.append(bdLocation.getLocationDescribe() + "--" + bdLocation.getAddress() + "-" + bdLocation.getAddrStr());// 位置语义化信息
        List<Poi> list = bdLocation.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        Log.e(TAG, "定位结果：" + sb.toString());
    }

    /**
     * 初始化获取服务器上的定位采集配置
     */
    private void initConfInfo() {
        final Context context = getApplicationContext();
        RequestParams params = new RequestParams();
        String userId = StringUtils.substringBefore(Settings.System.getString(context.getContentResolver(), "lastLoginNo"), "_");
        params.put("platformSysUserId", userId);
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        String configUrl = Settings.System.getString(context.getContentResolver(), "lastLoginGpsRuleUrl");
        L.e("msg", "定位配置URL：" + configUrl);
        if (StringUtils.isEmpty(configUrl)) {
            L.w(TAG, "CONFIG_LBS_URL_CONFIG url is empty!");
            return;
        }
        mHttpClient.get(configUrl, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                L.w(TAG, "Getting config info failed\n" + responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                L.e(TAG, "Getting config info successfully\n" + responseString);
                try {
                    JSONObject json = new JSONObject(responseString);
                    int status = json.getInt("status");
                    String msg = json.getString("message");

                    if (status != 0) {
                        L.w(TAG, msg);
                        return;
                    }

                    JSONObject resultObj = json.getJSONObject("result");
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
                        L.e(TAG, "配置中没有启用定位");
                        L.i(TAG, "Location is disabled for current user");
                        return;
                    }
                    L.i(TAG, "更新的个人信息：" + resultObj.toString());
                    mConfig = new LocateConfig();
                    mConfig.setInterval(interval);
                    mConfig.setList(list);
                    mConfig.setEnable(enable);
                    mConfig.setLastUpdate(new Date());

                    //标示现在是第一次初始化配置
                    mIsFirstInitConfig = true;

                    //开始定位了
                    if (!mLocationClient.isStarted()) mLocationClient.start();
                    boolean containMsg = mLocationRequestHandler.hasMessages(LocationRequestHandler.WHAT);//如果没有Message已发送到Handler队列
                    L.d(TAG, String.format("LocationRequestMessage? -> %s", containMsg) + LocationRequestHandler.WHAT);
                    if (!containMsg) {//发送Message到Handler，开始根据时间间隔配置请求定位
                        L.e(TAG, "请求定位");
                        if (task != null) {
                            task.cancel();
                        }
                        startTimer();
                        if (mConfig.getInterval() == 0) {
                            mConfig.setInterval(100);
                        }
                        timer.schedule(task, 0, mConfig.getInterval() * 60 * 1000);
                        L.i(TAG, "Locating is going to start(Handler has send the message)");
                    }
                } catch (JSONException e) {
                    L.e(TAG, e.getMessage(), e);
                    mConfig = null;
                }
            }
        });
    }

    /**
     * 更新数据库中没有Address信息的记录，并提交数据库中所有的记录
     *
     * @param location 位置信息
     */
    @SuppressLint("DefaultLocale")
    private void updateAddressAndUpload(final Location location) {

        //判断Address是否为空
        String address = location.getAddress();
        Log.e(TAG, "获取定位结果地址：" + address);
        if (StringUtils.isEmpty(address)) {
            L.d(TAG, "Location(id %d) doesn't contain address field,Reverse Geo Coding");
            //不包含Address，则根据经纬度数据重新获取一下
            final GeoCoder geoCoder = GeoCoder.newInstance();
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(new LatLng(location.getLatitude(), location.getLongitude())));
            geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                @Override
                public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                    L.v(TAG, "onGetGeoCodeResult");
                    //释放GeoCoder对象
                    geoCoder.destroy();
                }

                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                    //先释放GeoCoder对象
                    geoCoder.destroy();

                    if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                        L.d(TAG, "Location(id %d) onGetReverseGeoCodeResult() returned NULL or ERROR");
                        return;
                    }

                    //得到address
                    String _address = reverseGeoCodeResult.getAddress();

                    if (!StringUtils.isEmpty(_address)) {
                        L.v(TAG, String.format("Location(id %d) got a new address: %s", location.getId(), _address));

                        //把得到的Address设置进Location
                        location.setAddress(_address);

                    } else {
                        L.d(TAG, "Location(id %d) still got no address after ReverseGeoCode");
                    }
                }
            });

        } else {
            L.v(TAG, String.format("Location(id %d) already got address field and going to be uploaded", location.getId()));
        }

        //上传单次定位得到的数据
        uploadLocation(location);

    }

    /**
     * 上传单次获取到的位置数据
     *
     * @param location 位置信息
     */
    private void uploadLocation(final Location location) {

        final Context context = getApplicationContext();
        //初始化提交参数
        RequestParams params = new RequestParams();
        String rsglSysUserId = StringUtils.substringBefore(Settings.System.getString(context.getContentResolver(), "lastLoginNo"), "_");
        String longAndLatVal;
        final JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            Log.e(TAG, "上传地址：" + location.getAddress() + ",ID  = " + location.getId() + ",时间：" + location.getTimestamp());
            jsonObject = new JSONObject();
            jsonObject.put("rsglSysUserId", rsglSysUserId);
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            if (longitude == 0 || latitude == 0) {
                longAndLatVal = StringUtils.EMPTY;
            } else {
                longAndLatVal = longitude + "," + latitude;
            }
            jsonObject.put("longAndLat", longAndLatVal);
            jsonObject.put("gpsIsOpen", location.isGpsOpen() ? 1 : 0);
            jsonObject.put("lastCollectTime", DateFormatUtils.format(location.getTimestamp(), "yyyy/MM/dd HH:mm:ss"));
            jsonObject.put("address", location.getAddress());
            jsonObject.put("errorRange", location.getRadius());
            jsonObject.put("phoneNumber", "");
            jsonArray.put(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put("access_token", AppController.getInstance().mAppTokenStr);
        params.put("encodeStr", Base64PasswordUtil.encode(jsonArray.toString()));
        String uploadUrl = Settings.System.getString(context.getContentResolver(), "lastLoginBathGpsSubmitUrl");
        if (StringUtils.isEmpty(uploadUrl)) {
            L.w(TAG, "CONFIG_LBS_URL_UPLOAD url is empty!");
            return;
        }

        L.e(TAG, "提交定位地址：" + uploadUrl + "\n 参数：" + params.toString() + "\n 内容：" + jsonArray.toString());
        //提交
        mHttpClient.get(uploadUrl, params, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                L.e(TAG, "单次提交失败");
                mLocationsTb.insert(location);
                showToast(false);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                L.e(TAG, "单次提交结果：" + responseString);
                //如果服务器返回状态正常，则从本地数据库中删除这条记录
                try {
                    JSONObject respObj = new JSONObject(responseString);
                    int status = Integer.parseInt(respObj.getString("status"));
                    if (status == 0) {
                        L.e(TAG, "单次上传成功");
                        //单个数据上传成功，去数据库查询有没有保存的数据
                        uploadBatchLocation();
                        showToast(true);
                    } else {
                        //服务器返回上传失败
                        mLocationsTb.insert(location);
                        showToast(false);
                    }
                } catch (JSONException e) {
                    L.e(TAG, e.getMessage(), e);
                }
            }

            private void showToast(boolean isSuccess) {
                //如果是第一次配置规则初始化后提交，则弹出Toast提醒成功或失败
                if (mIsFirstInitConfig) {
                    String toastStr;
                    if (isSuccess) {
                        toastStr = "定位功能已正常启用";
                    } else {
                        toastStr = "定位功能异常";
                    }
                    ToastUtil.toastAlerMessageCenter(context, toastStr, 1000);

                    mIsFirstInitConfig = false;
                }
            }
        });
    }


    /**
     * 从本地数据库中检查有没有需要上传的数据
     */
    private void uploadBatchLocation() {
        //去数据库查询所有数据
        locations = mLocationsTb.limitQuery(20);
        L.e("msg", "数据库中定位结果条数：" + mLocationsTb.query().size() + "----" + locations.size());
        if (locations.size() > 0) {
            //检查地址address是否为空
            for (final Location location : locations) {
                String address = location.getAddress();
                if (address.equals("null") || address.equals("")) {
                    addressList.add(location);
                } else {
                    updateList.add(location);
                }
            }

            if (addressList.size() == 0) {//数据库里面的定位数据没有存在地址为空的数据
                Message msg = new Message();
                handler.sendMessage(msg);
                msg.what = 0;
            }

            for (final Location location : addressList) {
                //不包含Address，则根据经纬度数据重新获取一下
                final GeoCoder geoCoder = GeoCoder.newInstance();
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(new LatLng(location.getLatitude(), location.getLongitude())));
                geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                    @Override
                    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
                        L.v(TAG, "onGetGeoCodeResult");
                        geoCoder.destroy();//释放GeoCoder对象
                    }

                    @Override
                    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                        geoCoder.destroy();//先释放GeoCoder对象
                        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                            return;
                        }
                        String _address = reverseGeoCodeResult.getAddress();//得到address
                        L.e(TAG, "定位失败后存库重新获取的地址：" + _address);
                        if (!StringUtils.isEmpty(_address)) {
                            location.setAddress(_address);//把得到的Address设置进Location
                            updateList.add(location);
                            addressList.remove(location);
                            if (addressList.size() == 0) {
                                Message msg = new Message();
                                handler.sendMessage(msg);
                                msg.what = 0;
                            }
                        } else {
                            L.e(TAG, "获取地址失败！");
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.e(TAG, "LocateService created");
        Context context = getApplicationContext();
        //初始化Handler
        mLocationRequestHandler = new LocationRequestHandler(this);

        //注册广播接收器
//        /**守护死亡*/
        IntentFilter guardDeathFilter = new IntentFilter();
        guardDeathFilter.addAction(Const.GUARD_ACTION_DEATH);
        registerReceiver(mGuardDeathReceiver, guardDeathFilter);
//        /**用户注销*/
        IntentFilter stopIntentFilter = new IntentFilter();
        stopIntentFilter.addAction(Const.BROADCAST_ACTION_USER_LOGOUT);
        registerReceiver(mUserLogoutReceiver, stopIntentFilter);
//        /**注册Home键监听的广播接收器*/
        mHomeKeyListener = new HomeKeyListener();
        registerReceiver(mHomeKeyListener, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));

        //HttpClient网络请求对象初始化
        //设置重连和超时
        mHttpClient.setMaxRetriesAndTimeout(0, 8000);

        option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(BDLocation.BDLOCATION_GCJ02_TO_BD09LL); //可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0); //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true); //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); //可选，默认false,设置是否使用gps
        option.setLocationNotify(false); //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true); //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false); //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(true); //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(true); //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false); //可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient = new LocationClient(context); //声明LocationClient类
        mMyLocationListener = new MyLocationListener();
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mMyLocationListener); //注册监听函数
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.e(TAG, "开启定位服务：onStartCommand");
        if (intent == null) {
            L.e(TAG, "已经存在创建过定位服务，才调用此方法：onStartCommand");
        }
        L.e(TAG, "重新获取个人信息配置");
        initInfo();
        return Service.START_STICKY;
    }

    private void initInfo() {
        if (mLocationsTb == null) mLocationsTb = new LocationsTb(this);//此时用户已经登录，所以这里才初始化DB操作对象
        //如果还没有初始化服务器采集配置，就获取配置并返回
        if (mConfig == null) {
            L.e(TAG, "配置服务器");
            L.i(TAG, "Received User Login Broadcast but Location config is null");
            initConfInfo();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.e(TAG, "LocateService destroyed");
        mLocationRequestHandler.removeMessages(LocationRequestHandler.WHAT);
        mMyLocationListener.tmpLocations.clear();
        mLocationClient.stop();
        mLocationClient.unRegisterLocationListener(mMyLocationListener);
        unregisterReceiver(mGuardDeathReceiver);
        unregisterReceiver(mUserLogoutReceiver);
        unregisterReceiver(mHomeKeyListener);
        if (task != null) {
            task.cancel();
        }
        if (!isLogout) {
            sendBroadcast(new Intent(Const.LOCATE_ACTION_DEATH));//发送定位死亡广播
        } else {
            L.e(TAG, "用户注销了---定位服务销毁了");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 定时器请求定位
     */
    private void startTimer() {
        task = new TimerTask() {
            @Override
            public void run() {
                L.e(TAG, "发送定位msg");
                mLocationRequestHandler.sendEmptyMessage(LocationRequestHandler.WHAT);
                if (!Build.MANUFACTURER.equals("HUAWEI")) {
                    if (!AppUtils.isServiceWork(LocateService.this, GuardService.class.getName())) {//不存在该服务启动定位服务
                        L.e(TAG, "守护服务不存在！！");
                        startService(new Intent(LocateService.this, GuardService.class));
                    } else {
                        L.e(TAG, "守护服务还在！！");
                    }
                } else {
                    if (Integer.parseInt(Build.VERSION.RELEASE.substring(0, 1)) != 6) {
                        if (!AppUtils.isServiceWork(LocateService.this, GuardService.class.getName())) {//不存在该服务启动定位服务
                            L.e(TAG, "守护服务不存在！！");
                            startService(new Intent(LocateService.this, GuardService.class));
                        } else {
                            L.e(TAG, "守护服务还在！！");
                        }
                    }
                }
            }
        };
    }

    /**
     * 定时发送定位请求的Handler,此时mConfig不能为null
     */
    static class LocationRequestHandler extends Handler {
        static final int WHAT = 12010077;
        private final WeakReference<LocateService> host;

        LocationRequestHandler(LocateService locateService) {
            host = new WeakReference<>(locateService);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == WHAT) {
                LocateService _host = host.get();

                if (_host != null && _host.mConfig != null) {
                    //如果在时间范围内，才发起百度请求定位
                    if (_host.mConfig.isEnable()) {
                        //需要采集才去定位
                        for (int i = 0; i < _host.mConfig.getList().size(); i++) {
                            if (TimeUtil.isInTimeScope(_host.mConfig.getList().get(i).getStart(), _host.mConfig.getList().get(i).getEnd(), TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24))) {
                                L.e(TAG, "开始请求定位！！！");
                                _host.mLocationClient.requestLocation();
                                break;
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 百度地图定位SDK的定位回调/监听
     */
    private class MyLocationListener implements BDLocationListener {
        static final int UPDATE_TIMES = 1;
        //有时候定位失败获取到的坐标就是这个值，为百度定位SDK中坐标的默认值
        static final double ERROR_COORDINATE = 4.9E-324D;
        List<Location> tmpLocations = new ArrayList<>(UPDATE_TIMES);

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //百度定位SDK刚刚调用start()方法时，会自动发起一次定位并回调结果到这里，
            // 所以这里需要验证是否已经登录初始化
            if (mConfig == null || mLocationsTb == null) {
                return;
            }
            L.i(TAG, "location data received in MyLocationListener");

            //控制台打印
            debugPrintLocation(bdLocation);

            //解析成Location对象
            Date timeStamp = new Date();
            double longitude = bdLocation.getLongitude();
            double latitude = bdLocation.getLatitude();
            if (ERROR_COORDINATE == longitude || ERROR_COORDINATE == latitude) {
                L.w(TAG, "ERROR_COORDINATE!");

                longitude = 0.0;
                latitude = 0.0;
            }

            int radius = Math.round(bdLocation.getRadius());
            String address = bdLocation.getAddrStr() + StringUtils.defaultString(
                    bdLocation.getLocationDescribe());
            L.i(TAG, "合成的address:" + address);
            boolean gpsOpen = SystemUtil.getGpsState(getApplicationContext());
            Location location = new Location();
            location.setRsglSysUserId(StringUtils.substringBefore(Settings.System.getString(getApplication().getContentResolver(), "lastLoginNo"), "_"));
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            location.setRadius(radius);
            location.setAddress(address);
            location.setGpsOpen(gpsOpen);
            location.setTimestamp(timeStamp);
            Log.e(TAG, "测试定位服务返回的准确地址：" + address);
            tmpLocations.add(location);
            //插入数据库记录
            L.v(TAG, "inserting the location into db:\n" + location.toString());
            //如果当前还在更新定位次数内
            Log.e(TAG, "当前位置收集的次数：" + tmpLocations.size());
            if (mConfig != null && mConfig.isEnable()) {
                for (int i = 0; i < mConfig.getList().size(); i++) {
                    if (TimeUtil.isInTimeScope(mConfig.getList().get(i).getStart(), mConfig.getList().get(i).getEnd(), TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24))) {
                        if (NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
                            //单次上传数据
                            updateAddressAndUpload(location);
                        } else {
                            mLocationsTb.insert(location);
                        }
                        break;
                    }
                }
            }
            //清空临时Location List
            tmpLocations.clear();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * 用于监听Home操作并执行更新mConfig（服务器采集配置）的广播接收器
     */
    private class HomeKeyListener extends BroadcastReceiver {
        private static final String TAG = "LocateService.HomeKey";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: action: " + action);
            if (StringUtils.equals(action, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i(TAG, "reason: " + reason);

                //如果用户还没有登录，则不响应
                if (mLocationsTb == null) {
                    return;
                }

                //如果还没有获取过配置，则先获取
                if (mConfig == null) {
                    initConfInfo();
                    return;
                }

                updateConfig();

                //下面是业务逻辑无关的打印输出，仅供调试参考
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    Log.e(TAG, "homekey");
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    Log.e(TAG, "long press home key or activity switch");
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    Log.e(TAG, "lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    Log.e(TAG, "assist");
                }
            }
        }

        void updateConfig() {
            //如果现在不是上次更新那天，则更新
            Date last = mConfig.getLastUpdate();
            if (!DateUtils.isSameDay(last, new Date())) {
                initConfInfo();
            }
        }

    }
}
