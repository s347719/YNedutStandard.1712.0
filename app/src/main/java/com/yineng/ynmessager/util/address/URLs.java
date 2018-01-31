
package com.yineng.ynmessager.util.address;

import android.content.Context;
import android.text.TextUtils;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.CryptUtil;
import com.yineng.ynmessager.util.HttpUtil;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 胡毅
 * @ClassName URLs
 * @Description 类描述
 */

public class URLs implements Const {
    /**
     * 保存用户输入的host
     */
    public static String HOST = "";
    /**
     * 在此处检验兼容性的字段
     * 判断是否是云版本
     */
    public static final String YUN_IP = "ynedut.com";
    public static final String YUN_IP_TEST = "ynedut.corpsys.cn";

    /**
     * 调转到V8指定页面
     */
    public static String V8_POSITON_PAGE = "/third/auth/forwardPage.htm?version=V1.0&access_token={0}&openId={1}&mobile={3}&urlStr={2}";
    /**
     * 应用中心访问V8获取可用菜单
     */
    public static String APP_CENTER_V8_MENU = "/third/msg/findMyShortcutAndAvailableResource.htm?access_token={0}&platformSysUserId={1}&reqPlatform={2}";
    /**
     * 用于OA待办验证_手机端
     */
    public static String OA_TODO_ANDROID = "/third/mobileProc/queryMobileToDoProcByUser.htm?version={0}&access_token={1}&userId={2}&pageNumber={3}&pageSize={4}";

    /**
     * 跳转OA详情的地址PC
     */
    public static String OA_TO_DETAIL = "/pages/platform/index.jsp?platform#/10000/995-YWLCGL-002-002";

    /**
     * 跳转到宏天OA指定页面
     */
    public static String JUMP_OA_POSITION_PAGE = "/oa/login.ht?username={0}&password={1}&succeedUrl={2}";

    /**
     * GPS规则获取
     */
    public static String GPS_GET_RULE = "/third/rydwForMsg/findCollectionRuleByUserId.htm";

    /**
     * GPS数据提交-批量（POST）
     */

    public static String GPS_UPLOAD_MULTIPTE = "/third/rydwForMsg/batchCollectLocation.htm";



    /**
     * 获取Token
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_TOKEN = "/oauth/token.htm?client_id=yn-message&client_secret=yn-message-yn88888888yn&grant_type=password";

    /**
     * 获取V8应用模块数据
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String APP_BUSINESS = "/third/mobilemodule/queryMobileModuleList.htm";

    /**
     * 根据查询条件获取该查询条件下的数据
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String APP_CONTENT_ITEM = "/third/mobilemodule/queryMobileModuleListByComponent.htm";
    /**
     * 根据搜索组件ID查询该组件下的搜索条件列表
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String APP_MODULE_SEARCH_LIST = "/third/mobilemodule/queryMobileModuleParamByComponent.htm";

    /**
     * 根据组件ID获取该组件的信息
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String APP_MODULE_LIST_ITEM = "/third/mobilemodule/queryMobileModuleComponentData.htm";
    /**
     * 参数：
     * fileId
     * access_token
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String THIRD_DOWNLOAD_FILE = "/third/file/download.htm";

    /**
     * 获取我的待办计数
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String TODO_COUNT_URL = "/third/mobileProc/getMobileProcCountByUser.htm?version=V1.0&access_token={0}&userId={1}&queryType={2}";


    /**
     * 跳转V8待办网页的相对地址
     */
    public static String TODO_PAGE_URL = "/pages/lc/common/flowProcess.html?lcBusinessProcDefId={0}&taskId={1}&formSource={2}";
    /**
     * 获取某条待办数据的处理状态接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String TODO_STATUS_URL = "/third/mobileProc/judgeProcHadDone.htm?version=V1.0&access_token={0}&userId={1}&taskId={2}&formSource={3}";
    /**
     * 获取申请数据的接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String DEMAND_DATA_URL = "/third/mobileProc/queryMobileApplyProcByUser.htm?version={0}&access_token={1}&userId={2}&pageNumber={3}&pageSize={4}";

    /**
     * 获取已办数据的接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String DONE_DATA_URL = "/third/mobileProc/queryMobileDoneProcByUser.htm?version={0}&access_token={1}&userId={2}&pageNumber={3}&pageSize={4}";
    /**
     * 待办中批量处理
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String BATCH_DATE_CHECK_URL = "/third/mobileProc/queryMobileToDoBatchCheckByUser.htm";
    /**
     * OA中发起申请接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String SEND_REQUEST = "/third/mobileProc/getMobileStartProcByUser.htm?version={0}&access_token={1}&userId={2}";

    /**
     * oa中获取ynedut的查看地址
     */
    public static String OA_YNEDUT_SCAN = "/pages/lc/common/flowProcess.html";
    /**
     * 客户端更新V8该条通知的状态
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String NOTICE_UPDATE_STATUS_URL = "/third/msg/changeMessageStatus.htm";
    /**
     * 推广图片推送接口（文字）
     * 参数：
     * v8Version
     * token
     * number
     * total
     * personStart
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String POPULARIZE_PICTURE_PUSH = "/third/pictureRotateAddress/findRotateByAddressNumber.htm";
    /**
     * 获取广播通知信息接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_NOTICE_URL_INFO = "/third/msg/queryMessageMobileInfoByCondition.htm";
    /**
     * 获取广播通知统计接口
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_NOTICE_URL_NUM = "/third/msg/queryMessageMobileCountInfoByCondition.htm";

    /**
     * 获取应用列表
     * 参数：
     * access_token
     * version
     * userId
     * userType
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_USER_APPS = "/third/app/queryAppsByUser.htm";

    /**
     * 获取待办列表
     * 参数：
     * access_token
     * version
     * userId
     * userType
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_USER_DEALT = "/third/app/queryTodoByUser.htm";

    /**
     * 获取服务器登录配置
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_SERVICE_LOGIN_CONFIG = "/third/version/getCurVersion.htm";
    /**
     * 获取手机登录动态码
     * 参数：
     * access_token:String
     * mobile:String 手机号
     * terminalType:int->2移动端
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String GET_PHONE_DYNAMIC_CODE = "/third/loginRule/genrateDynamicCode.htm";

    /**
     * 验证手机动态验证码是否可用
     * 参数：
     * access_token:String
     * mobile:String 手机号
     * code:String 验证码
     * terminalType:String 1 PC;2: 手机端
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String VERIFY_DYNAMIC_CODE_USEABLE = "/third/loginRule/judgeDynamicCodeUseable.htm";

    /**
     * 帐号密码登录验证
     * 参数:
     * version:String
     * access_token:String
     * userName:String
     * password:String-Base64
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String VALDATE_USER = "/third/loginRule/validateUser.htm";

    /**
     * 获取yendut版本号
     */

    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String YNEDUT_GET_V8_VERSION_API = "/third/version/getCurrentVersion.htm?access_token={0}";

    /**
     * 第三方应用跳转
     * 参数：
     * id:String appId
     * client:1
     * userId:String 用户真实账号
     * access_token:String
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String YNEDUT_THIRD_APP = "/pages/third/anonymous/thirdSystemOpen.html";

    /**
     * OA跳转地址
     * 参数：
     * version=V2.0 版本号2.0
     * username :String 加密的用户名CryptUtil.encode(account:登录名)
     * password:String 服务器返回加密后的密码
     * linked:ms
     * method:getMyTodoTask
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.BPMX)
    public static String YNEDUT_OA_APP = "/weixin/login.ht";

    /**
     * 支付页面
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String YNEDUT_PAY_APP = "/third/auth/forwardPage.htm";

    /**
     * 报表跳转地址
     * 参数：
     * fr_username:String 用户登录名
     * fr_password:String 加密后的密码
     * userId:String 用户真实账户
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.WR)
    public static String YNEDUT_REPORT_APP = "/appReport/view.html";

    /**
     * 分享下载地址
     */
    @V8AddressAnnotation(v8ContextType = V8AddressAnnotation.ContextType.YNEDUT)
    public static String YNEDUT_DOWNLOAD_SHARE = "/pages/platform/download/index.html";

    public static String getMessengerUpdateURL() {
        String url = AppController.UPDATE_CHECK_IP + "/openservices/productversion/findProVersionCodeNew?" +
                "productCode=" + Const.PRODUCT_CODE + "&proVersionCode={0}&dependProductCode=" + Const.DEPEND_PRODUCT_CODE +
                "&dependProVersionCode={1}" + "&companycode={2}";
        return url;
    }


    /**
     * @return 获取OA系统URL
     */
    public static String getOAUrl(Context context, String oaLink, String id,int type) {
        if (TextUtils.isEmpty(oaLink)) {
            oaLink = "/bpmx/weixin/login.ht";//备用地址
        }

        if (context == null) {
            return StringUtils.EMPTY;
        }

        LastLoginUserSP sp = LastLoginUserSP.getInstance(context);
        String V8_HOST_SERVICE_HOST = AppController.getInstance().CONFIG_YNEDUT_V8_SERVICE_HOST;
        String url = V8_HOST_SERVICE_HOST.substring(0, StringUtils.removeEnd(V8_HOST_SERVICE_HOST, "/").lastIndexOf("/")) + oaLink;
        Map<String, String> params = new HashMap<>();
        // 宏天OA
        params.put("username", CryptUtil.encode(sp.getUserLoginAccount()));
        params.put("password", sp.getUserPassword());
        params.put("version", "V2.0");
        if (type==0){
            params.put("method", "getTasks");
            params.put("taskId", id);
        }else if (type==1){
            params.put("method", "getProcessRun");
            params.put("runId", id);
        }
        params.put("linked", "ms");
        return HttpUtil.createGetUrl(url, params);
    }

    /**
     * 应用中获取smesisUrl跳转地址
     *
     * @param smesisUrl
     * @param token
     * @param smesisUserId
     * @return
     */
    public static String getSmesisUrl(String smesisUrl,String token,String smesisUserId){

        String url;
        AppController instance1 = AppController.getInstance();
        String V8_HOST_SERVICE_HOST = instance1.CONFIG_YNEDUT_V8_SERVICE_HOST;
        String smesisServer = V8_HOST_SERVICE_HOST.substring(0, StringUtils.removeEnd(V8_HOST_SERVICE_HOST, "/").lastIndexOf("/")) + "/smesis";
        if (smesisUrl.contains("?")){
            url =  "file:///android_asset/smesis/index.html#/" + smesisUrl+ "&platformSysUserId="+ smesisUserId
                    +"&originUrl="+ smesisServer
                    +"&access_token="+token;
        }else {
            url = "file:///android_asset/smesis/index.html#/" + smesisUrl+ "?platformSysUserId="+ smesisUserId
                    +"&originUrl="+smesisServer
                    +"&access_token="+token;
        }
        return url;
    }

    /**
     * 获取ynedut 地址
     * @return
     */
    public static String getYnedutUrl(Context context, String token,String id){
        if (context == null) {
            return StringUtils.EMPTY;
        }
        LastLoginUserSP sp = LastLoginUserSP.getInstance(context);
        return OA_YNEDUT_SCAN +
                "?" +
                "lcBusinessProcDefId=" + id +
                "&taskId=" +
                "&access_token=" + token +
                "&userId=" + sp.getUserAccount();
    }
}
