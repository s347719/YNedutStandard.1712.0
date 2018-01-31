package com.yineng.ynmessager.app;

import android.os.Environment;

import java.io.File;

/**
 * @author 贺毅柳
 */
public interface Const {
    boolean DEBUG = true; // true

    /***********会话信息**************/
    int SERVER_PORT = 6000;
    String SERVICENAME = "messenger.yineng.com.cn";
    String RESOURSE_NAME = "Msg_Phone";
    String DES_KEY = "learning";
    int REQUEST_CODE = 0;
    int RESULT_CODE = 1;
    int CHAT_TYPE_P2P = 1;// 两人会话
    int CHAT_TYPE_GROUP = 2;// 群会话
    int CHAT_TYPE_DIS = 3;// 讨论组会话
    int CHAT_TYPE_BROADCAST_RECEIVER = 3;// 广播消息回执
    int CHAT_TYPE_BROADCAST = 4;// 广播
    int CHAT_TYPE_EVENT = 5;// 事件
    int CHAT_TYPE_NOTICE = 7;// 通知
    int CHAT_TYPE_PLATFORM_NOTICE = 8;//平台通知
    int CHAT_TYPE_MOVE = 11;// 抖动
    int CHAT_TYPE_FILE = 12;// 文件接收回执
    int CHAT_TYPE_AUTO_SEND = 13;// 自动回复
    int CHAT_VOICE_TYPE_P2P = 15;// 二人会话语音
    int CHAT_TYPE_P2P_FILE = 16;//二人会话在线文件发送
    int CHAT_VOICE_TYPE_GROUP = 25;// 群会话语音
    int CHAT_VOICE_TYPE_DISGROUP = 35;// 讨论组会话语音
    String BROADCAST_NAME = "广播";// 消息列表显示的名称
    String BROADCAST_ID = "com:yineng:broadcast";// 消息列表广播存放的uerno

    int IS_AUTO_LOGIN_XMPP = 101;//是自动登录更新检测
    int IS_NOT_AUTO_LOGIN_XMPP = 102;//是登录页进入之后的更新检测

    int ORG_UPDATE_ALL = 0;// 全量更新
    int ORG_UPDATE_SOME = 1;// 增量更新
    int GET_OFFLINE_MSG = 2;// 离线消息
    int CONTACT_GROUP_TYPE = 8;// 联系人-群组
    int CONTACT_DISGROUP_TYPE = 9;// 联系人-讨论组
    int GROUP_CREATER_TYPE = 10;// 群组、讨论组-创建人
    int GROUP_MANAGER_TYPE = 20;// 群组、讨论组-管理员
    int GROUP_USER_TYPE = 50;// 群组、讨论组-一般用户
    int GET_OFFLINE_MSG_NUM = 100;// 获取离线消息条数

    int DOWN_UP_1MB = 1024 * 1024;
    int DOWN_UP_512K = 512 * 1024;
    int REQUEST_TAKE_PHOTO = 4;
    int SCAN_NOTICEEVENT = 17;
    int MESSENGERFILE = 0;
    int APPDGSXFILE = 1;
    int VOICE_MAX_TIME = 59;//聊天中的语音录制的最大录音时长

    /**
     * 通过查询AppOpsManager 得到照相机权限为 26
     */
    int PERMISSION_OP_CAMERA = 26;

    /**
     * 存储到Preference中，标识是否是第一次启动程序
     */
    String IS_FIRST_LAUNCH = "isFirstLaunch";
    /**
     * 用户下线
     */
    int USER_OFF_LINE = 0;
    /**
     * 手机上线
     */
    int USER_ON_LINE_PHONE = 1;
    /**
     * PC上线
     */
    int USER_ON_LINE_PC = 2;
    /**
     * PC和手机都在线
     */
    int USER_ON_LINE_ALL = 3;
    /**
     * 用户状态上下线状态改变
     */
    int USER_STATUS_CHANGED = 4;
    /**
     * webview数据库名称
     */
    String WEBVIEW_DB_PATH = "webviewdb";
    /**********************聊天窗口中的文件上传下载地址*****************************/
    String UPLOAD_FILE_URL = "/plugins/orgmanager/ynmessenger/upload";
    String DOWNLOAD_FILE_URL = "/plugins/orgmanager/ynmessenger/download";
    String GROUP_FILE_DOWNLOAD_URL = "/plugins/orgmanager/ynmessenger/download";
    /**
     * 用户头像下载地址
     */
    String DOWNLOAD_AVATAR_URL = "/plugins/orgmanager/ynmessenger/headdownload";


    /**
     * 会话中图片缓存图片保存文件夹
     */
    String DOWNLOAD_FILE_CACHE = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/ynmessager/ynCache";

    /**
     * 更新主界面未读消息条数的广播
     */
    String ACTION_UPDATE_UNREAD_COUNT = "UPDATE_UNREAD_COUNT_UI";

    // update info 升级版本时服务器返回的数据字典
    String PROTOCOL = "http://";

    /******************************************* 升级配置 *********************************************/
    String YNEDUT_LOCAL_VERSION = "0861711251";
    String PRODUCT_CODE = "ynmessenger_mb_v8";
    String DEPEND_PRODUCT_CODE = "ynedut8";

    /**
     * 下载APP的地址
     */
    String UPDATE_DOWNLOAD_APP_URL = AppController.UPDATE_CHECK_IP + "/file/download.htm";//"http://10.6.0.129/center/file/download.htm";
    String UPDATE_INFO = "info";
    /**
     * 处理成功
     */
    int IQ_RESPONSE_CODE_SUCCESS = 200;


    /******************* IQ请求返回码 *******************/
    // 处理成功
    String SUCESS = "200";
    // 非法操作
    String ILLEGAL_OPERATION = "202";
    // 参数错误
    String PARAM_ERROR = "203";
    // 未授权、鉴权失败
    String UNAUTHORIZED = "204";
    // 密码错误
    String PASSWORD_ERROR = "205";
    // 数据不可修改
    String READ_ONLY = "206";
    // 不支持的操作
    String UNSUPORTED_OPERATE = "207";
    // 根节点不存在
    String ROOT_ORG_NOT_EXISTS = "301";
    // 根节点已存在
    String ROOT_ORG_EXISTED = "302";
    // 父级节点不存在
    String PARENT_ORG_NOT_EXISTS = "303";
    // 组织机构已存在
    String ORG_EXISTED = "304";
    // 组织机构不存在
    String ORG_NOT_EXISTS = "305";
    // 组织机构不为空,表示其下还有子部门或者员工
    String ORG_NOT_NULL = "306";
    // 用户已存在
    String USER_EXISTED = "401";
    // 用户不存在
    int USER_NOT_EXISTS = 402;
    // 超出群用户数上限
    String EXCEED_MAX_USERS = "601";
    // 群或讨论组已存在
    String GROUP_EXISTED = "602";
    // 群公告为空
    String EMPTY_GROUP_SUBJECT = "603";
    // 权限不够
    String NO_PERMISSION = "604";
    // 群或讨论组不存在
    String GROUP_NOT_EXISTS = "605";
    // 群创建个数达到上限
    String EXCEED_MAX_GROUPS = "606";
    // 文件是空的
    String FILE_IS_EMPTY = "701";
    // 文件不存在
    String FILE_NOT_FOUND = "702";
    // 更新的项目不存在
    String PROJECT_NOT_EXISTS = "802";

    /*********************短信登录错误码*************************/


    /**
     * 动态码发送成功
     */
    String PHONE_SEND_SUCCESS = "0";
    /**
     * 手机号+短信动态码登录方式未开启,不能获取短信动态码
     */
    String PHONE_TYPE_NO_OPEN = "0000";

    /**
     * 该手机号在系统中不存在
     */
    String PHONE_NO_FOUND = "0002";

    /**
     * 该手机号对应的登录帐号不存在
     */
    String PHONE_NO_ACCOUNT = "0003";

    /**
     * 该手机号对应的登录帐号已锁定，请联系管理员
     */
    String PHONE_ACCOUNT_LOCK = "0004";

    /**
     * 该手机号对应的登录帐号不在使用期限范围内，不能登录
     */
    String PHONE_ACCOUNT_OUT_TIME = "0005";

    /**
     * 该手机号对应的帐号不存在、或帐号被锁定或帐号不在使用期限范围内，请联系管理员
     */
    String PHONE_ACCOUNT_NO_FOUND_OR_OUT_TIME = "0006";

    /**
     * 发送动态码失败，请稍后重试
     */
    String PHONE_SEND_CODE_ERROR = "0007";

    /**
     * 学校充值的短信条数可能已用完，请联系管理员！
     */
    String PHONE_NO_PREPAID = "0008";

    /**
     * 发送动态码过于频繁，同一手机号每天最多发送${n}次，请您明天再试
     */
    String PHONE_GET_CODE_OFTEN = "0009";

    /**
     * 发送动态码过于频繁，同一手机号每天最多发送${n}次，请切换登录方式
     */
    String PHONE_GET_CODE_OFTEN_2 = "0010";

    /**
     * 手机号和动态码不能为空
     */
    String PHONE_NO_EMPTY = "0011";

    /**
     * 动态码错误或已过期,请重新输入
     */
    String PHONE_CODE_ERROR_OR_OUT_TIME = "0012";

    /**
     * 动态码已失效,请重新获取！
     */
    String PHONE_CODE_LOSE = "0013";

    /**
     * 守护服务死亡广播
     */
    String GUARD_ACTION_DEATH = "ynmsg.guard_death";
    /**
     * 定位服务死亡广播
     */
    String LOCATE_ACTION_DEATH = "ynmsg.locate_death";

    /**
     * 账户已经成功登陆上的广播
     */
    String BROADCAST_ACTION_USER_LOGIN = "ynmsg.account.login";
    /**
     * 注销/退出 账户的广播
     */
    String BROADCAST_ACTION_USER_LOGOUT = "ynmsg.account.logout";
    /**
     * 从登录界面进入主界面的广播
     */
    String BROADCAST_ACTION_START_MAINACTIVITY = "ynmsg.start.mainact";
    /**
     * 广播-服务器推送消息的action
     */
    String BROADCAST_ACTION_SERVICE_NOTICE = "ynmsg.service.notice";// 服务器推送消息
    /**
     * 广播-更新群/讨论组信息的action
     */
    String BROADCAST_ACTION_UPDATE_GROUP = "ynmsg.group.update";// 更新群/讨论组信息
    /**
     * 广播-创建讨论组的action
     */
    String BROADCAST_ACTION_CREATE_GROUP = "ynmsg.group.create";// 创建讨论组
    /**
     * 广播-退出某讨论组的action
     */
    String BROADCAST_ACTION_QUIT_GROUP = "ynmsg.group.quit";// 退出讨论组
    /**
     * 广播-登录用户退出某讨论组的action
     */
    String BROADCAST_ACTION_I_QUIT_GROUP = "ynmsg.group.I.quit";// 我退出讨论组
    /**
     * 正在加载联系人信息的广播的action
     */
    String BROADCAST_ACTION_LOADING_CONTACT = "ynmsg.loading.contact";// 正在加载联系人信息的广播
    /**
     * 联系人信息加载完毕的广播的action
     */
    String BROADCAST_ACTION_LOADED_CONTACT = "ynmsg.loaded.contact";//加载完成联系人信息的广播
    /**
     * 密码错误，身份验证过期
     */
    String BROADCAST_ACTION_ID_PAST = "ysmsg.id.past";
    /**
     * 下线通知
     */
    String BROADCAST_ACTION_LOGINED_OTHER = "ynmsg.logined.other";
    /**
     * 广播 - 清空会话列表
     */
    String BROADCAST_ACTION_CLEAR_SESSION_LIST = "ynmsg.setting.clearSessionList";
    /**
     * 广播 - 清除所有聊天记录
     */
    String BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG = "ynmsg.setting.clearAllChatMsg";
    /**
     * 广播 - 清除缓存
     */
    String BROADCAST_ACTION_CLEAR_CACHE = "ynmsg.setting.clearCache";
    /**
     * 服务器接口-重命名群/讨论组的action
     */
    int INTERFACE_ACTION_GROUP_RENAME = 2;// 修改群/讨论组名称
    /**
     * 添加人员到群组、讨论组
     **/
    String GROUP_ADD_USER = "group_add_user";
    /**
     * 联系人进入群或讨论组时传一个群/讨论组列表的Extra的key
     **/
    String INTENT_GROUP_LIST_EXTRA_NAME = "contactGroupList";
    /**
     * 单人会话Intent传一个用户对象的Extra的key
     **/
    String INTENT_USER_EXTRA_NAME = "userObject";
    /**
     * 群组Intent传一个群对象的Extra的key
     **/
    String INTENT_GROUP_EXTRA_NAME = "groupObject";
    /**
     * 群组Intent传一个群ID的Extra的key
     **/
    String INTENT_GROUPID_EXTRA_NAME = "discussion_group_id";
    /**
     * 群组Intent传一个群/讨论组类型的Extra的key
     **/
    String INTENT_GROUPTYPE_EXTRA_NAME = "GROUP_TYPE";
    /**
     * 客户端初始化命名空间
     */
    String REQ_IQ_XMLNS_CLIENT_INIT = "com:yineng:clientinit";
    /**
     * 获取组织机构的命名空间
     */
    String REQ_IQ_XMLNS_GET_ORG = "com:yineng:orgget";
    /**
     * 获取用户状态的命名空间
     */
    String REQ_IQ_XMLNS_GET_STATUS = "com:yineng:status";
    /**
     * 获取群、讨论组的命名空间
     */
    String REQ_IQ_XMLNS_GET_GROUP = "com:yineng:group";
    /**
     * 获取某个用户的详细信息的命名空间
     */
    String REQ_IQ_XMLNS_GET_PERSON_DETAIL = "com:yineng:querydetail";
    /**
     * 获取某个用户的离线消息
     */
    String REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST = "com:yineng:chathistory";
    /**
     * 服务器主动推送的字段
     */
    String REQ_IQ_XMLNS_NOTICE = "com:yineng:notice";
    /**
     * message消息回执监听
     */
    String REQ_IQ_XMLNS_MSG_RESULT = "com:yineng:receipt";
    /**
     * 消息已读回执
     */
    String MESSAGE_READED_RECEIVER = "urn:xmpp:receipts";
    /**
     * 广播-自定义未读消息条数
     */
    String BROADCAST_ACTION_UNREADER_MSG_NUM="ynmsg.unreader.num";

    interface Regex {
        String FACE = "(\\[\\/\\d{1,4}\\])";
        String IMG = "(\\<img key=\"(.*?)\"\\>)";
        String FILE = "(\\<file key=\"(.*?)\"\\>)";
    }

}
