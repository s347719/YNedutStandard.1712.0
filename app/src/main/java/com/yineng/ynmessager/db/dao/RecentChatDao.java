package com.yineng.ynmessager.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 最近会话实体
 *
 * @author Yutang
 */
public class RecentChatDao {
    private final String mTable = "RecentChat";
    private SQLiteDatabase mDB;
    private String TAG = RecentChatDao.class.getSimpleName();
    public String userNo = null;

    public RecentChatDao(Context context) {
        mDB = UserAccountDB.getInstance(context, LastLoginUserSP.getInstance(context).getUserAccount())
                .getWritableDatabase();
        userNo = LastLoginUserSP.getInstance(context).getUserAccount();
//		L.e(mTAG +"(Context mContext) "+mDB.getPath());
    }

    /**
     * 插入一条用户登录新记录
     *
     * @param chat 登录用户
     */
    private void insertRecentChat(RecentChat chat) {
        L.v(TAG, TAG + ":insertRecentChat-> " + chat.getContent());
        ContentValues contentValues = new ContentValues();
        contentValues.put("userNo", chat.getUserNo());
        contentValues.put("title", chat.getTitle());
        contentValues.put("content", chat.getContent());
        contentValues.put("chatType", chat.getChatType());
        contentValues.put("senderName", chat.getSenderName());
        contentValues.put("senderNo", chat.getSenderNo());
        contentValues.put("dateTime", chat.getDateTime());
        contentValues.put("headUrl", chat.getHeadUrl());
        contentValues.put("headLocalPath", chat.getHeadLocalPath());
        contentValues.put("isTop", chat.getIsTop());
        contentValues.put("topTime", "");
        contentValues.put("unReadCount", chat.getUnReadCount());
        contentValues.put("draft", chat.getDraft());
        mDB.insert(mTable, null, contentValues);
    }

    /**
     * 更新或新加一条记录
     *
     * @param chat
     */
    public synchronized void saveRecentChatSetUnreadNum(RecentChat chat) {
        RecentChat temp = isChatExist(chat.getUserNo(), chat.getChatType());
        if (temp != null) {// 如果已经存在 update
            int unReadCount = chat.getUnReadCount();
            chat.setUnReadCount(unReadCount);
            chat.setId(temp.getId());
            chat.setDraft(temp.getDraft());
            updateRecentChat(chat);
        } else {// 如果不存在，insert
            insertRecentChat(chat);
        }
    }

    /**
     * @param chat
     */
    public void updateRecentChat(RecentChat chat) {
        L.v(TAG, TAG + ":updateRecentChat-> " + chat.getContent());
        ContentValues contentValues = new ContentValues();
        contentValues.put("title", chat.getTitle());
        contentValues.put("content", chat.getContent());
        contentValues.put("senderName", chat.getSenderName());
        contentValues.put("senderNo", chat.getSenderNo());
        contentValues.put("dateTime", chat.getDateTime());
        contentValues.put("unReadCount", chat.getUnReadCount());
        contentValues.put("draft", chat.getDraft());
        mDB.update(mTable, contentValues, "id = ?", new String[]{chat.getId() + ""});
    }

    public int updateDraft(String userNo, String draft) {
        ContentValues values = new ContentValues();
        values.put("draft", draft);
        return mDB.update(mTable, values, "userNo=?", new String[]{userNo});
    }

    /**
     * 根据UserNo查询其对应的draft
     *
     * @param userNo
     * @return draft，（草稿）；如果未查询到结果则返回长度为0的字符串
     */
    public String queryDraftByUserNo(String userNo) {
        String draft = null;
        Cursor cur = mDB.query(mTable, new String[]{"draft"}, "userNo=?", new String[]{userNo}, null, null, null);
        if (cur.moveToFirst()) {
            draft = cur.getString(cur.getColumnIndex("draft"));
        }
        cur.close();
        if (draft == null) {
            draft = "";
        }
        return draft;
    }

    /**
     * 修改置顶参数
     *
     * @param id
     * @param istop
     */
    public void updateIsTop(int id, int istop) {
        L.v(TAG, TAG + ":updateRecentChatIsTop->");
        ContentValues contentValues = new ContentValues();
        contentValues.put("isTop", istop);
        if (istop == 1) {
            contentValues.put("topTime", TimeUtil.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24_mic));
        } else {
            contentValues.put("topTime", "");
        }

        mDB.update(mTable, contentValues, "id = ?", new String[]{id + ""});
    }

    /**
     * @param userNo
     * @param chatType
     * @param unReadCount
     */
    public void updateUnreadCount(String userNo, int chatType, int unReadCount) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("unReadCount", unReadCount);
        mDB.update(mTable, contentValues, "userNo = ? AND chatType = ?", new String[]{userNo, chatType + ""});
    }

    /**
     * 更新或新加一条记录
     *
     * @param chat
     */
    public void saveRecentChat(RecentChat chat) {
        RecentChat temp = isChatExist(chat.getUserNo(), chat.getChatType());
        if (temp != null) {// 如果已经存在 update
            int unReadCount = temp.getUnReadCount() + chat.getUnReadCount();

            chat.setUnReadCount(unReadCount);
            chat.setId(temp.getId());

            updateRecentChat(chat);
        } else {// 如果不存在，insert
            insertRecentChat(chat);
        }
    }

    /**
     * 读取n条数据(根据消息时间排序)
     *
     * @param pageindex
     * @return
     */
    public List<RecentChat> queryRecentChatPage(int pageindex, int count) {
        List<RecentChat> list = new ArrayList<RecentChat>();
        String sql = "select * from RecentChat ORDER BY isTop desc,topTime desc ,dateTime desc limit " + pageindex
                * count + "," + count;
        Cursor cursor = mDB.rawQuery(sql, null);

        RecentChat chat = null;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                chat = new RecentChat();
                chat.setId(cursor.getInt(cursor.getColumnIndex("id")));
                chat.setUserNo(cursor.getString(cursor.getColumnIndex("userNo")));
                chat.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                chat.setContent(cursor.getString(cursor.getColumnIndex("content")));
                chat.setChatType(cursor.getInt(cursor.getColumnIndex("chatType")));
                chat.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
                chat.setSenderNo(cursor.getString(cursor.getColumnIndex("senderNo")));
//				chat.setSenderName(cursor.getString(cursor.getColumnIndex("senderNo")));
                chat.setDateTime(cursor.getString(cursor.getColumnIndex("dateTime")));
                chat.setHeadUrl(cursor.getString(cursor.getColumnIndex("headUrl")));
                chat.setHeadLocalPath(cursor.getString(cursor.getColumnIndex("headLocalPath")));
                chat.setIsTop(cursor.getInt(cursor.getColumnIndex("isTop")));
                chat.setUnReadCount(cursor.getInt(cursor.getColumnIndex("unReadCount")));
                chat.setDraft(cursor.getString(cursor.getColumnIndex("draft")));
                list.add(chat);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        L.v(TAG, TAG + ":queryRecentChatPage:list.size->" + list.size());
        return list;
    }

    public List<RecentChat> queryByChatType(int chatType) {
        List<RecentChat> resultSet = new ArrayList<RecentChat>();
        Cursor cursor = mDB.query(mTable, null, "chatType=?", new String[]{String.valueOf(chatType)}, null, null, null);
        RecentChat result = null;
        while (cursor.moveToNext()) {
            result = new RecentChat();
            result.setId(cursor.getInt(cursor.getColumnIndex("id")));
            result.setUserNo(cursor.getString(cursor.getColumnIndex("userNo")));
            result.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            result.setContent(cursor.getString(cursor.getColumnIndex("content")));
            result.setChatType(cursor.getInt(cursor.getColumnIndex("chatType")));
            result.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
            result.setSenderNo(cursor.getString(cursor.getColumnIndex("senderNo")));
            result.setDateTime(cursor.getString(cursor.getColumnIndex("dateTime")));
            result.setHeadUrl(cursor.getString(cursor.getColumnIndex("headUrl")));
            result.setHeadLocalPath(cursor.getString(cursor.getColumnIndex("headLocalPath")));
            result.setIsTop(cursor.getInt(cursor.getColumnIndex("isTop")));
            result.setUnReadCount(cursor.getInt(cursor.getColumnIndex("unReadCount")));
            result.setDraft(cursor.getString(cursor.getColumnIndex("draft")));
            resultSet.add(result);
        }
        if (cursor != null) {
            cursor.close();
        }
        return resultSet;
    }

    /**
     * @param userNo
     * @return
     */
    public RecentChat isChatExist(String userNo, int type) {
        if (userNo == null) {
            return null;
        }
        RecentChat chat = null;
        Cursor cursor = mDB.query(mTable, null, "userNo = ? AND chatType = ?", new String[]{userNo, type + ""}, null, null,
                null);
        if (cursor != null && cursor.getCount() > 0) {
            L.v(TAG, TAG + ":isChatExist->cursor.getCount() > 0");

            if (cursor.moveToNext()) {
                chat = new RecentChat();
                chat.setId(cursor.getInt(cursor.getColumnIndex("id")));
                chat.setUserNo(cursor.getString(cursor.getColumnIndex("userNo")));
                chat.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                chat.setContent(cursor.getString(cursor.getColumnIndex("content")));
                chat.setChatType(cursor.getInt(cursor.getColumnIndex("chatType")));
                chat.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
                chat.setSenderNo(cursor.getString(cursor.getColumnIndex("senderNo")));
                chat.setDateTime(cursor.getString(cursor.getColumnIndex("dateTime")));
                chat.setHeadUrl(cursor.getString(cursor.getColumnIndex("headUrl")));
                chat.setHeadLocalPath(cursor.getString(cursor.getColumnIndex("headLocalPath")));
                chat.setIsTop(cursor.getInt(cursor.getColumnIndex("isTop")));
                chat.setUnReadCount(cursor.getInt(cursor.getColumnIndex("unReadCount")));
                chat.setDraft(cursor.getString(cursor.getColumnIndex("draft")));
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return chat;
    }

    /**
     * 删除会话
     *
     * @param id
     */
    public void deleteRecentChatById(int id) {
        String sql = "delete from RecentChat where id = " + id;
        mDB.execSQL(sql);
    }

    public void deleteRecentChatByUserNo(String num) {
        // String sql = "delete from RecentChat where userNo = " + num;
        // mDB.execSQL(sql);
        mDB.delete("RecentChat", "userNo = ?", new String[]{num});
    }

    /**
     * 清空整个表
     */
    public void deleteAll() {
        mDB.delete("RecentChat", null, null);
    }

    /**
     * 统计未读联系人总数
     *
     * @return
     */
    public int getUnReadUserCount() {
        String sql = "SELECT COUNT(*) as unRead from RecentChat where unReadCount > 0";
        Cursor cursor = mDB.rawQuery(sql, null);
        int count = 0;
        if (cursor != null) {

            if (cursor.getCount() > 0 && cursor.moveToNext()) {

                count = cursor.getInt(cursor.getColumnIndex("unRead"));
            }
            cursor.close();
        }
        return count;
    }

    /**
     * 统计未读消息总数
     *
     * @return
     */
    public int getUnReadMsgCount() {
//		Cursor cur = mDB.query(mTable,null,null,null,null,null,null);
//		while(cur.moveToNext())
//		{
//			L.d(mTAG,cur.getString(cur.getColumnIndex("title")) + "-->" + cur.getInt(cur.getColumnIndex("unReadCount")));
//		}
//		cur.close();

        String sql = "SELECT SUM(unReadCount) as unRead from RecentChat where unReadCount > 0";
        L.d("getUnReadMsgCount db == " + mDB.getPath());
        if (!mDB.isOpen()) {
            return 0;
        }
        Cursor cursor = mDB.rawQuery(sql, null);
        int count = 0;
        if (cursor != null) {

            if (cursor.getCount() > 0 && cursor.moveToNext()) {

                count = cursor.getInt(cursor.getColumnIndex("unRead"));
            }
            cursor.close();
        }
        return count;
    }

    public String getUserNameByUserId(String userId, int type) {
        String name = null;
        String sql = "";
        switch (type) {
            case Const.CHAT_TYPE_P2P:
                name = "error";
                sql = "select userName from User where userNo ='" + userId.trim() + "'";
                break;
            case Const.CHAT_TYPE_DIS:
                name = "error";
                sql = "select * from DiscussGroup where groupName ='" + userId.trim() + "'";
                break;
            case Const.CHAT_TYPE_GROUP:
                name = "error";
                sql = "select * from UserGroup where groupName ='" + userId.trim() + "'";
                break;
        }
        Cursor cursor = mDB.rawQuery(sql, null);

        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToNext()) {
                switch (type) {
                    case Const.CHAT_TYPE_P2P:
                        name = cursor.getString(cursor.getColumnIndex("userName"));
                        break;
                    case Const.CHAT_TYPE_DIS:
                        name = cursor.getString(cursor.getColumnIndex("subject"));
                        if (name == null || (name != null && name.isEmpty())) {
                            name = cursor.getString(cursor.getColumnIndex("naturalName"));
                        }
                        break;
                    case Const.CHAT_TYPE_GROUP:
                        name = cursor.getString(cursor.getColumnIndex("naturalName"));
                        break;
                }
            }
            cursor.close();
        }
        return name;
    }

    /**
     * 清空除了广播以外的整个表
     */
    public void deleteAllNotNotifiy() {
        mDB.delete("RecentChat", "chatType = ?", new String[]{String.valueOf(Const.CHAT_TYPE_P2P)});
        mDB.delete("RecentChat", "chatType = ?", new String[]{String.valueOf(Const.CHAT_TYPE_DIS)});
        mDB.delete("RecentChat", "chatType = ?", new String[]{String.valueOf(Const.CHAT_TYPE_GROUP)});
    }

    /**
     * @param sendTime
     * @param chatType
     * @param userNo
     */
    public void updateRecentChatTime(String sendTime, String userNo, int chatType) {
        RecentChat temp = isChatExist(userNo, chatType);
        if (temp != null) {
            temp.setDateTime(sendTime);
            updateRecentChat(temp);
        }
    }
}
