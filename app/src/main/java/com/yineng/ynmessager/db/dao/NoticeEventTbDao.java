//***************************************************************
//*    2015-6-25  下午4:17:47
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.event.NoticeEvent;

import java.util.List;

/**
 * 用户数据库事件表的操作接口
 * 
 * @author 贺毅柳
 * 
 */
public interface NoticeEventTbDao
{
	String TABLE_NAME = "NoticeEvent";

	String COL_ID = "id";
	String COL_PACKET_ID = "packetId";
	String COL_USER_NO = "userNo";
	String COL_USER_NAME = "userName";
	String COL_RECEIVER = "receiver";
	String COL_TITLE = "title";
	String COL_MESSAGE = "message";
	String COL_CONTENT = "content";
	String COL_URL = "url";
	String COL_IS_READ = "isRead";
	String COL_TIME_STAMP = "timeStamp";
	String COL_SOURCE_TERMINAL = "sourceTerminal";
	//增加平台通知的msgid，通知类型，是否有附件和图片的字段  huyi 2016.1.11
	String COL_NOTICE_ID = "noticeId";
	String COL_NOTICE_TYPE = "noticeType";
	String COL_HAS_ATTACHMENT = "hasAttachment";
	String COL_HAS_PIC = "hasPic";
	/**
	 * 往表中插入一行数据
	 * 
	 * @param event
	 *            一个要插入数据库表的event对象
	 * @return 最新插入的行的ID，如果出错则返回-1
	 */
	long insert(NoticeEvent event);

	/**
	 * 根据ID来删除表中指定的数据行
	 * @param event 要删除的event对象
	 * @return 被删除的行总数；如果传入的event参数为null则返回-1
	 */
	int delete(NoticeEvent event);

	int update(NoticeEvent event);

	/**
	 * 查询该表的所有行所有列的数据
	 * 
	 * @return 没有进行排序(orderBy)，直接返回表中所有数据
	 */
	List<NoticeEvent> query();

}
