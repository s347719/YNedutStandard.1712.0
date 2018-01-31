//***************************************************************
//*    2015-8-20  下午5:05:26
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker;

import org.jivesoftware.smack.packet.Message;

import java.io.File;

/**
 * @author 贺毅柳
 */
public interface SendingListener
{
	/**
	 * 在准备发送一个文件之前调用（发送前，每个文件发送都会回调）
	 * @param type 当前准备发送的文件类型
	 * @param msg 当前信息
	 * @param chatType 当前所属类型
	 */
	void onBeforeEachSend(int type, Message msg,int chatType);

	/**
	 * 完成发送一个文件消息后执行该回调方法（发送后，每个文件发送都会回调）
	 *
	 * @param type
	 *            已发送出去的Flie对象类型
	 *
	 * @param msg
	 *            已执行发送的org.jivesoftware.smack.packet.Message对象
	 * @param chatType 当前所属类型
	 */
	void onEachDone(int type, Message msg,int chatType);

	/**
	 * 已完成发送该Task中所有的文件发送后回调
	 */
	void onAllDone();

	/**
	 * 发送失败
	 * @param type 发送失败的Flie对象
	 * @param message 发送的消息
	 * @param chatType 发送的消息所属的类型
	 */
	void onFailedSend(int type, Message message,int chatType);

//	/**
//	 * @param message
//	 * @param messageBodyEntity
//	 * @param progress
//	 */
//	void onProgressUpdateSend(Message message,
//			MessageBodyEntity messageBodyEntity, int progress);
}
