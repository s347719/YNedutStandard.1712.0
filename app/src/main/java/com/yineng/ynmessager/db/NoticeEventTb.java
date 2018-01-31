//***************************************************************
//*    2015-6-25  下午5:20:50
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.event.NoticeEvent;
import com.yineng.ynmessager.db.dao.NoticeEventTbDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户数据库事件表的操作类
 *
 * @author 贺毅柳
 */
public class NoticeEventTb implements NoticeEventTbDao
{
    private static final String TAG = "EventNoticeTb";
    private SQLiteDatabase mDb;

    public NoticeEventTb(Context context)
    {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public NoticeEventTb(Context context, String userNo)
    {
        mDb = UserAccountDB.getInstance(context, userNo).getWritableDatabase();
    }

    /**
     * 返回该表结构的SQL语句
     *
     * @return 建表语句 ("CREATE TABLE……")
     */
    public final static String getStructureSql()
    {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(TABLE_NAME).append("(")
                .append(COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(COL_PACKET_ID).append(" TEXT,")
                .append(COL_USER_NO).append(" TEXT,")
                .append(COL_USER_NAME).append(" TEXT,")
                .append(COL_RECEIVER).append(" TEXT,")
                .append(COL_TITLE).append(" TEXT,")
                .append(COL_MESSAGE).append(" TEXT,")
                .append(COL_CONTENT).append(" TEXT,")
                .append(COL_URL).append(" TEXT,")
                .append(COL_IS_READ).append(" INTEGER,")
                .append(COL_TIME_STAMP).append(" INTEGER,")
                .append(COL_SOURCE_TERMINAL).append(" INTEGER,")
                //增加通知数据库中的通知属性字段 huyi 2016.1.11
                .append(COL_NOTICE_ID).append(" TEXT,")
                .append(COL_NOTICE_TYPE).append(" TEXT,")
                .append(COL_HAS_ATTACHMENT).append(" TEXT,")
                .append(COL_HAS_PIC).append(" TEXT);");
        return sql.toString();
    }
    //在平台通知中更新数据库依据msgId来判断是否已经存在
    public synchronized void saveOrUpdate(NoticeEvent event){
        if(event == null)
        {
            return;
        }
        if(IsExists(event.getMsgId()))
        {
            update(event);
        }else
        {
            insert(event);
        }

    }

    private boolean IsExists(String id)
    {
        boolean flag = false;
        Cursor cursor = mDb.query(TABLE_NAME,null,"noticeId = ? ",new String[] {id + ""},null,null,null);
        flag = cursor != null && cursor.getCount() > 0;
        if(cursor != null)
        {
            cursor.close();
        }
        return flag;
    }
    @Override
    public long insert(NoticeEvent _event)
    {
        if (_event == null && !(_event instanceof NoticeEvent))
        {
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(COL_PACKET_ID, _event.getPacketId());
        values.put(COL_USER_NO, _event.getUserNo());
        values.put(COL_USER_NAME, _event.getUserName());
        values.put(COL_RECEIVER, _event.getReceiver());
        values.put(COL_TITLE, _event.getTitle());
        values.put(COL_MESSAGE, _event.getMessage());
        values.put(COL_CONTENT, _event.getContent());
        values.put(COL_URL, _event.getUrl());
        values.put(COL_IS_READ, _event.isRead());
        values.put(COL_TIME_STAMP, _event.getTimeStamp().getTime());
        values.put(COL_SOURCE_TERMINAL, _event.getSourceTerminal());
        // huyi 2016.1.11
        values.put(COL_NOTICE_ID, _event.getMsgId());
        values.put(COL_NOTICE_TYPE, _event.getMessageType());
        values.put(COL_HAS_ATTACHMENT, _event.getHasAttachment());
        values.put(COL_HAS_PIC, _event.getHasPic());//end
        long rowId = mDb.insert(TABLE_NAME, null, values);
        L.v(TAG, "插入了一条ID为" + rowId + "的记录");
        return rowId;
    }

    @Override
    public int delete(NoticeEvent event)
    {
        if (event == null)
        {
            return -1;
        }
        int rows = mDb.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(event.getId())});
        L.v(TAG, "删除了" + rows + "条记录");
        return rows;
    }

    //删除所有数据
    public int delete(){
//        int rows = mDb.delete(TABLE_NAME, "isRead=?", new String[]{1+""});
        int rows = mDb.delete(TABLE_NAME, null,null);
        L.v(TAG, "删除了" + rows + "条记录");
        return -1;
    }

    public void delete(List<NoticeEvent> eventList)
    {
        for (NoticeEvent event : eventList)
        {
            delete(event);
        }
    }

    @Override
    public int update(NoticeEvent event)
    {
        ContentValues values = new ContentValues();
        values.put(COL_PACKET_ID, event.getPacketId());
        values.put(COL_USER_NO, event.getUserNo());
        values.put(COL_USER_NAME, event.getUserName());
        values.put(COL_RECEIVER, event.getReceiver());
        values.put(COL_TITLE, event.getTitle());
        values.put(COL_MESSAGE, event.getMessage());
        values.put(COL_CONTENT, event.getContent());
        values.put(COL_URL, event.getUrl());
        values.put(COL_IS_READ, event.isRead());
        values.put(COL_TIME_STAMP, event.getTimeStamp().getTime());
        values.put(COL_SOURCE_TERMINAL, event.getSourceTerminal());
        // huyi 2016.1.11
        values.put(COL_NOTICE_ID, event.getMsgId());
        values.put(COL_NOTICE_TYPE, event.getMessageType());
        values.put(COL_HAS_ATTACHMENT, event.getHasAttachment());
        values.put(COL_HAS_PIC, event.getHasPic());//end
        return mDb.update(TABLE_NAME, values, COL_ID + "=?", new String[]{String.valueOf(event.getId())});
    }

    @Override
    public List<NoticeEvent> query()
    {
        ArrayList<NoticeEvent> list = new ArrayList<>();
        NoticeEvent row;
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, COL_TIME_STAMP + " desc");
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
        {
            row = new NoticeEvent();
            row.setId(cur.getInt(cur.getColumnIndex(COL_ID)));
            row.setPacketId(cur.getString(cur.getColumnIndex(COL_PACKET_ID)));
            row.setUserNo(cur.getString(cur.getColumnIndex(COL_USER_NO)));
            row.setUserName(cur.getString(cur.getColumnIndex(COL_USER_NAME)));
            row.setReceiver(cur.getString(cur.getColumnIndex(COL_RECEIVER)));
            row.setTitle(cur.getString(cur.getColumnIndex(COL_TITLE)));
            row.setMessage(cur.getString(cur.getColumnIndex(COL_MESSAGE)));
            row.setContent(cur.getString(cur.getColumnIndex(COL_CONTENT)));
            row.setUrl(cur.getString(cur.getColumnIndex(COL_URL)));
            row.setRead(cur.getInt(cur.getColumnIndex(COL_IS_READ)) != 0);
            row.setTimeStamp(new Date(cur.getLong(cur.getColumnIndex(COL_TIME_STAMP))));
            row.setSourceTerminal(cur.getInt(cur.getColumnIndex(COL_SOURCE_TERMINAL)));
            // huyi 2016.1.11
            row.setMsgId(cur.getString(cur.getColumnIndex(COL_NOTICE_ID)));
            row.setMessageType(cur.getString(cur.getColumnIndex(COL_NOTICE_TYPE)));
            row.setHasAttachment(cur.getString(cur.getColumnIndex(COL_HAS_ATTACHMENT)));
            row.setHasPic(cur.getString(cur.getColumnIndex(COL_HAS_PIC)));//end
            list.add(row);
        }
        cur.close();
        L.v(TAG, "查询出" + list.size() + "条记录");
        return list;
    }


    /**
     * 查找第一条记录
     * @return
     */
    public NoticeEvent queryFirstNotice(){
        NoticeEvent row = null;
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, COL_TIME_STAMP + " desc","1");
        if (cur != null && cur.getCount() > 0) {
            if (cur.moveToFirst()) {
                row = new NoticeEvent();
                row.setId(cur.getInt(cur.getColumnIndex(COL_ID)));
                row.setPacketId(cur.getString(cur.getColumnIndex(COL_PACKET_ID)));
                row.setUserNo(cur.getString(cur.getColumnIndex(COL_USER_NO)));
                row.setUserName(cur.getString(cur.getColumnIndex(COL_USER_NAME)));
                row.setReceiver(cur.getString(cur.getColumnIndex(COL_RECEIVER)));
                row.setTitle(cur.getString(cur.getColumnIndex(COL_TITLE)));
                row.setMessage(cur.getString(cur.getColumnIndex(COL_MESSAGE)));
                row.setContent(cur.getString(cur.getColumnIndex(COL_CONTENT)));
                row.setUrl(cur.getString(cur.getColumnIndex(COL_URL)));
                row.setRead(cur.getInt(cur.getColumnIndex(COL_IS_READ)) != 0);
                row.setTimeStamp(new Date(cur.getLong(cur.getColumnIndex(COL_TIME_STAMP))));
                row.setSourceTerminal(cur.getInt(cur.getColumnIndex(COL_SOURCE_TERMINAL)));
                // huyi 2016.1.11
                row.setMsgId(cur.getString(cur.getColumnIndex(COL_NOTICE_ID)));
                row.setMessageType(cur.getString(cur.getColumnIndex(COL_NOTICE_TYPE)));
                row.setHasAttachment(cur.getString(cur.getColumnIndex(COL_HAS_ATTACHMENT)));
                row.setHasPic(cur.getString(cur.getColumnIndex(COL_HAS_PIC)));//end
            }
            cur.close();
            L.v(TAG, "查询出" + cur.getCount() + "条记录");
        }
        return row;
    }

    /**
     * 返回所有事件的数量
     *
     * @return 返回指定type的事件数量，出错返回-1
     */
    public int count()
    {
        int count = -1;
        String sql = String.format("select count(%s) from %s", COL_ID, TABLE_NAME);
        Cursor cursor = mDb.rawQuery(sql, null);
        if (cursor.moveToFirst())
        {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 更改某条通知的已读状态
     * @param noticeId 通知的msgId
     * huyi
     */
    public void updateNoticeReadStatus(String noticeId) {
        ContentValues values = new ContentValues();
        values.put(COL_IS_READ, 1);
        if (mDb.isOpen()) {
            mDb.update(TABLE_NAME, values, COL_NOTICE_ID + "=?", new String[]{noticeId});
        }
    }
}
