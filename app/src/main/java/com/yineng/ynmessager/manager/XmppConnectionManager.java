package com.yineng.ynmessager.manager;

import android.content.Context;
import android.os.Handler;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.offline.MessageRevicerEntity;
import com.yineng.ynmessager.imageloader.FileDownLoader;
import com.yineng.ynmessager.smack.ReceiveBroadcastChatCallBack;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceivePresenceCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQProvider;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

/**
 * 整个xmpp链接的管理类，
 *
 * @author Yutang
 *
 */
public class XmppConnectionManager {
	private static XmppConnectionManager xmppConnectionManager;
	private XMPPConnection mConnection;// xmppconnection链接
	private ProviderManager mProviderManager;// IQ解析管理器
	private ConnectionConfiguration mConnectionConfig;// 连接配置
	/**
	 * UI层的IQ监听实例容器
	 */
	private Map<String, ReceiveReqIQCallBack> mRevIQCallBackMap = new Hashtable<String, ReceiveReqIQCallBack>();
	/**
	 * UI层的Message监听实例容器
	 */
	private Map<String, ReceiveMessageCallBack> mRevMsgCallBackMap = new Hashtable<String, ReceiveMessageCallBack>();
	/**
	 * UI层的Presence监听实例容器
	 */
	private HashSet<ReceivePresenceCallBack> mRevPresCallBackSet = new HashSet<ReceivePresenceCallBack>();
	/**
	 * 消息列表界面的连接状态监听实例容器
	 */
	private HashSet<StatusChangedCallBack> mStatusCallBackSet = new HashSet<StatusChangedCallBack>();
	private ReceiveBroadcastChatCallBack mReceiveBroadcastCallBack;
	private boolean mIsInit;// 链接初始状态标志
	private String mServiceName = null;
	private XmppConnectionManager() {

	}

	/**
	 * 懒汉式单例方法
	 *
	 * @return
	 */
	public static synchronized XmppConnectionManager getInstance() {
		if (xmppConnectionManager == null) {
			xmppConnectionManager = new XmppConnectionManager();
		}
		return xmppConnectionManager;
	}

	/**
	 * 如果密码与之前密码不同，则恢复默认状态，重新初始化
	 */
	public void setXmppConnectionConfigNull() {
		mConnectionConfig = null;
		mIsInit = false;
		mConnection = null;
		mProviderManager = null;
	}

	/**
	 * init ConnectionConfiguration and XMPPConnection listener
	 *
	 * @param host
	 * @param port
	 * @param
	 * @return
	 */
	public void init(String host, int port, String servicename) {
		//如果用户切换了服务器地址，需要重新初始化连接配置
		if (mConnectionConfig != null && !mConnectionConfig.getHost().equals(host)) {
			mIsInit = false;
		}
		if (!mIsInit) {
			clearUserCallbacks();// 清空用户注册的回调接口实例
			Connection.DEBUG_ENABLED = true;
			mProviderManager = ProviderManager.getInstance();
			configureProviders(mProviderManager);
			mConnectionConfig = new ConnectionConfiguration(host, port, servicename);
			// 是否压缩 不能设置为true 因为服务器不支持。会导致app崩溃，此处节省流量操作无法实现
			mConnectionConfig.setCompressionEnabled(false);
			// 开启调试模式
			mConnectionConfig.setDebuggerEnabled(true);
			// 是否SASL验证
			mConnectionConfig.setSASLAuthenticationEnabled(true);
			// 允许自动连接
			mConnectionConfig.setReconnectionAllowed(true);
			// 允许登陆成功后更新在线状态
			mConnectionConfig.setSendPresence(true);
			mConnection = new XMPPConnection(mConnectionConfig);
			mIsInit = true;

		}
	}

	public XMPPConnection getConnection() {
		return mConnection;
	}

	/**
	 * 为不同的IQ设置相应的解析器，
	 *
	 * @param pm
	 */
	private void configureProviders(ProviderManager pm) {
		// add ReqIQProvider
		ReqIQProvider reqIQProvider = new ReqIQProvider();
		//客户端初始化
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_CLIENT_INIT, reqIQProvider);
		//组织机构
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_ORG, reqIQProvider);
		//用户状态
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_STATUS, reqIQProvider);
		//群与讨论组
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_GROUP, reqIQProvider);
		//查看某用户详细信息
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL,
				reqIQProvider);
		//离线会话列表
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_OFFLINE_USER_LIST, reqIQProvider);
		pm.addIQProvider("notice", Const.REQ_IQ_XMLNS_NOTICE, reqIQProvider);
		// add delivery receipts
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_MSG_RESULT, reqIQProvider);
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT, Const.MESSAGE_READED_RECEIVER, new MessageRevicerEntity.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
	}

	/**
	 * 注册msg消息回调器
	 *
	 * @param account
	 *            回话对方的 account 或讨论组、群名称
	 * @param callback
	 */
	public void addReceiveMessageCallBack(String account,
										  ReceiveMessageCallBack callback) {
		mRevMsgCallBackMap.put(account, callback);
	}

	/**
	 * 通过Key获取msg消息回调器
	 *
	 * @param account
	 *            回话对方的 account 或讨论组、群名称
	 * @return
	 */
	public ReceiveMessageCallBack getReceiveMessageCallBack(String account) {
		return mRevMsgCallBackMap.get(account);
	}

	/**
	 * 删除msg消息回调器
	 *
	 * @param account
	 *            回话对方的 account或讨论组、群名称
	 */
	public void removeReceiveMessageCallBack(String account) {
		mRevMsgCallBackMap.remove(account);
	}

	/**
	 * 获取IQ消息回调器
	 *
	 * @param namespace
	 * @return
	 */
	public ReceiveReqIQCallBack getReceiveReqIQCallBack(String namespace) {
		return mRevIQCallBackMap.get(namespace);
	}

	/**
	 * 删除IQ消息回调器
	 *
	 * @param namespace
	 */
	public void removeReceiveReqIQCallBack(String namespace) {
		mRevIQCallBackMap.remove(namespace);
	}

	/**
	 * 注册IQ消息回调器
	 *
	 * @param namespace
	 * @param callback
	 */
	public void addReceiveReqIQCallBack(String namespace,
										ReceiveReqIQCallBack callback) {
		mRevIQCallBackMap.put(namespace, callback);
	}

	/**
	 * 注册Presence消息回调器
	 *
	 * @param callback
	 */
	public void addReceivePresCallBack(ReceivePresenceCallBack callback) {

		mRevPresCallBackSet.add(callback);
	}

	/**
	 * 删除Presence消息回调器
	 *
	 * @param callback
	 */
	public void removeReceivePresCallBack(ReceivePresenceCallBack callback) {

		mRevPresCallBackSet.remove(callback);
	}

	/**
	 * 分发presence 消息
	 *
	 * @param packet
	 */
	public void dispatchPresence(Presence packet) {
		ReceivePresenceCallBack callback = null;
		Iterator<ReceivePresenceCallBack> iterator = mRevPresCallBackSet
				.iterator();
		while (iterator.hasNext()) {
			callback = iterator.next();
			callback.receivedPresence(packet);
		}
	}

	/**
	 * 注册用户状态变动回调器
	 *
	 * @param callback
	 */
	public void addStatusChangedCallBack(StatusChangedCallBack callback) {

		mStatusCallBackSet.add(callback);
	}

	/**
	 * 删除用户状态变动回调器
	 *
	 * @param callback
	 */
	public void removeStatusChangedCallBack(StatusChangedCallBack callback) {

		mStatusCallBackSet.remove(callback);
	}

	/**
	 * 回调用户状态变动的所有监听
	 *
	 * @return
	 */
	public void doStatusChangedCallBack(int status) {
		StatusChangedCallBack callback = null;
		Iterator<StatusChangedCallBack> iterator = mStatusCallBackSet
				.iterator();
		while (iterator.hasNext()) {
			callback = iterator.next();
			callback.onStatusChanged(status);
		}
	}

	/**
	 * 发送packet
	 *
	 * @param packet
	 * @return 已发送返回true；未发送返回false
	 */
	public boolean sendPacket(Packet packet)
	{
		if(mConnection != null && mConnection.isConnected() && mConnection.isAuthenticated())
		{
			mConnection.sendPacket(packet);
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * 注册packet监听器
	 *
	 * @param listener
	 * @param filter
	 */
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		if (mConnection != null) {
			mConnection.addPacketListener(listener, filter);
		}
	}

	/**
	 * 删除packet监听器
	 *
	 * @param listener
	 */
	public void removePacketListener(PacketListener listener) {
		if (mConnection != null) {
			mConnection.removePacketListener(listener);
		}
	}

	/**
	 * 判断链接是否登陆
	 *
	 * @return
	 */
	public boolean isAuthenticated() {
		if (mConnection != null) {
			return mConnection.isAuthenticated();
		} else {
			return false;
		}
	}

	/**
	 * 判断链接是否断开
	 *
	 * @return
	 */
	public boolean isConnected() {
		if (mConnection != null) {
			return mConnection.isConnected();
		} else {
			return false;
		}
	}

	/**
	 * 获取服务器域名 如：m.com
	 *
	 * @return
	 */
	public String getServiceName() {
		if (mConnection != null && mConnection.isConnected()) {
			mServiceName = mConnection.getServiceName();
			return mConnection.getServiceName();
		} else {
			if (mServiceName != null && !mServiceName.isEmpty()) {
				return mServiceName;
			}
			return null;
		}
	}

	/**
	 * 获取当前链接的帐号名
	 *
	 * @return
	 */
	public String getUser() {
		if (mConnection != null) {
			return mConnection.getUser();
		} else {
			return "";
		}
	}

	/**
	 * @return 获取用户状态：
	 */
	public int getUserCurrentStatus() {
		if (mConnection != null) {
			//如果服务器已关闭
			if (isServerShutDown()) {
				return LoginThread.USER_STATUS_SERVER_SHUTDOWN;
			}
			//如果已在其他设备上登录
			if (isLoginedOther()) {
				return LoginThread.USER_STATUS_LOGINED_OTHER;
			}
			if (mConnection.isConnected()) {
				if (mConnection.isAuthenticated()) {
					if (AppController.NET_IS_USEFUL) {
						return LoginThread.USER_STATUS_ONLINE;
					}else {
						return LoginThread.USER_STATUS_NETOFF;
					}
				} else {
					return LoginThread.USER_STATUS_OFFLINE;
				}
			} else {
				return LoginThread.USER_STATUS_CONNECT_OFF;
			}
		} else {
			return LoginThread.USER_STATUS_CONNECT_OFF;
		}
	}

	/**
	 * 开启登陆请求线程/登录界面时调用
	 *  @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param resourcename
	 *            资源名称
	 * @param mHandler
	 */
	public LoginThread doLoginThread(String username, String password,
									 String resourcename, Handler mHandler,Context context) {
		LoginThread thread = new LoginThread(username, password, resourcename,
				mHandler, mConnection,context);
		new Thread(thread).start();
		return thread;
	}

	/**
	 * 手机登录
	 * @param username
	 * @param password
	 * @param resourcename
	 * @param mHandler
	 * @param context
	 * @param isPlain
	 * @return
	 */
	public LoginThread doLoginThread(String username, String password,
									 String resourcename, Handler mHandler,Context context,boolean isPlain) {
		LoginThread thread = new LoginThread(username, password, resourcename,
				mHandler, mConnection,context,isPlain);
		new Thread(thread).start();
		return thread;
	}

	/**
	 * 后台重新登录时调用
	 *
	 * @param username
	 * @param password
	 * @param resourcename
	 * @param mHandler
	 */
	public void doReLoginThread(String username, String password,
								String resourcename, Handler mHandler,Context context,boolean isPlain) {
		LoginThread thread = new LoginThread(username, password, resourcename,
				mHandler, mConnection,context,isPlain);
		new Thread(thread).start();
	}

	/**
	 * 下线的runable
	 */
	private Runnable mExitRunnable = new Runnable() {
		@Override
		public void run() {
			if (mConnection != null && mConnection.isConnected()) {
				Presence presence = new Presence(Presence.Type.unavailable);
				presence.setStatus("offline");
				mConnection.disconnect(presence);
			}
		}
	};

	/**
	 * 开启下线请求线程
	 */
	public void doExitThread() {
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				if (mConnection != null && mConnection.isConnected()) {
//					Presence presence = new Presence(Presence.Type.unavailable);
//					presence.setStatus("offline");
//					mConnection.disconnect(presence);
//				}
//			}
//		}).start();
		FileDownLoader.getInstance().getThreadPool().execute(mExitRunnable);
	}

	/**
	 * 判断XmppConnectionManager是否初始化
	 *
	 * @return
	 */
	public boolean isInit() {
		return mIsInit;
	}

	/**
	 * 获取广播消息回调接口实例
	 *
	 * @return
	 */
	public ReceiveBroadcastChatCallBack getReceiveBroadcastChatCallBack() {
		return mReceiveBroadcastCallBack;
	}

	/**
	 * 设置广播消息回调函数
	 *
	 * @param receiveBroadcastCallBack
	 */
	public void setReceiveBroadcastChatCallBack(
			ReceiveBroadcastChatCallBack receiveBroadcastCallBack) {
		this.mReceiveBroadcastCallBack = receiveBroadcastCallBack;
	}

	/**
	 * 释放广播消息回调接口实例
	 */
	public void clearReceiveBroadcastChatCallBack() {
		this.mReceiveBroadcastCallBack = null;
	}

	/**
	 * 清空用户注册的回调接口实例
	 */
	public void clearUserCallbacks() {
		mRevIQCallBackMap.clear();// 清空监听器
		mRevMsgCallBackMap.clear();// 清空监听器
		mRevPresCallBackSet.clear();// 清空监听器
		//huyi 2016.1.22注释，用以解决消息列表界面状态更新不正确的问题。当调用
		//doStatusChangedCallBack方法时，mStatusCallBackSet已经被clear了，故无法回调到界面去执行界面更新
		//的方法,所以注释掉clear代码
//		mStatusCallBackSet.clear();// 清空监听器
		mReceiveBroadcastCallBack = null;
	}

	/**
	 * @return 得到当前登录的服务器的ip
	 */
	public String getServiceHost() {
		if (mConnectionConfig != null) {
			return mConnectionConfig.getHost().isEmpty() ? null : mConnectionConfig.getHost();
		}
		return null;
	}

	/**
	 * @return 得到当前登录的服务器的端口
	 */
	public int getServicePort() {
		if (mConnectionConfig != null) {
			return mConnectionConfig.getPort() == 0 ? 0 : mConnectionConfig.getPort();
		}
		return 0;
	}

	/**
	 * 服务器是否关闭
	 */
	private boolean isServerShutDown = false;

	public boolean isServerShutDown() {
		return isServerShutDown;
	}

	public void setIsServerShutDown(boolean isServerShutDown) {
		this.isServerShutDown = isServerShutDown;
	}

	/**
	 * 是否已在其他设备上登录
	 */
	private boolean isLoginedOther = false;

	public boolean isLoginedOther() {
		return isLoginedOther;
	}

	public void setLoginedOther(boolean isLoginedOther) {
		this.isLoginedOther = isLoginedOther;
	}
}
