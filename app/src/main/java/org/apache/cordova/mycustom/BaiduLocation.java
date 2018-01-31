package org.apache.cordova.mycustom;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.yineng.ynmessager.util.L;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 百度云推送插件
 *
 * @author mrwutong
 */
public class BaiduLocation extends CordovaPlugin {

    /** LOG TAG */
    private static final String TAG = BaiduLocation.class.getSimpleName();

    /** JS回调接口对象 */
    public static CallbackContext cbCtx = null;

    /** 百度定位客户端 */
    public LocationClient mLocationClient = null;

    /** 百度定位监听 */
    public BDLocationListener myListener = new BDLocationListener() {
        @Override public void onReceiveLocation(BDLocation location) {
            try {
                JSONObject resultObj = new JSONObject();
                JSONObject coords = new JSONObject();
                coords.put("latitude", location.getLatitude());
                coords.put("longitude", location.getLongitude());
                coords.put("altitude", location.getAltitude());
                coords.put("accuracy", location.getRadius());
                coords.put("heading", location.getDirection());
                coords.put("speed", location.getSpeed());
                coords.put("address", location.getAddrStr() + "," + location.getLocationDescribe());
                resultObj.put("coords", coords);
                resultObj.put("timestamp", location.getTime());
                L.i(TAG, resultObj.toString());

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                String errMsg = e.getMessage();
                LOG.e(TAG, errMsg, e);

                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, errMsg);
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            } finally {
                mLocationClient.stop();
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };

    /**
     * 插件主入口
     */
    @Override public boolean execute(String action, final JSONArray args, CallbackContext callbackContext)
        throws JSONException {
        LOG.d(TAG, "BaiduPush#execute");

        boolean ret = false;

        if ("getCurrentPosition".equalsIgnoreCase(action)) {
            cbCtx = callbackContext;

            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            cbCtx.sendPluginResult(pluginResult);

            if (mLocationClient == null) {
                mLocationClient = new LocationClient(this.webView.getContext());
                mLocationClient.registerLocationListener(myListener);

                // 配置定位SDK参数
                initLocation();
            }

            if (!mLocationClient.isStarted()) mLocationClient.start();
            ret = true;
        }

        return ret;
    }

    /**
     * 配置定位SDK参数
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(BDLocation.BDLOCATION_GCJ02_TO_BD09LL); //可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(0); //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true); //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); //可选，默认false,设置是否使用gps
        option.setLocationNotify(false); //可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(
            true); //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false); //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(true); //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(true); //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false); //可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }
}
