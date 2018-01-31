package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.app.Dealt;
import com.yineng.ynmessager.db.dao.DealtDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/27.
 */

public class DealtTb implements DealtDao {
    public static final String TAG = "DealtTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public DealtTb(Context context) {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public DealtTb(Context context, String account) {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }

    @Override
    public void insert(Dealt dealt) {
        if (dealt == null) {
            return;
        }
        Dealt dealt1 = queryById(dealt);
        if (dealt1 != null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, dealt.getId());
        values.put(COLUMN_NAME, dealt.getName());
        values.put(COLUMN_COUNT, dealt.getCount());
        values.put(COLUMN_SEQ, dealt.getSequence());
        mDb.insert(TABLE_NAME, null, values);
    }

    @Override
    public void insert(List<Dealt> dealts) {
        if (dealts == null) {
            return;
        }
        if (!mDb.isOpen()) {
            return;
        }
        for (Dealt dealt : dealts) {
            insert(dealt);
        }
    }

    @Override
    public LinkedList<Dealt> queryOrderBySequence() {
        if (!mDb.isOpen()) {
            return null;
        }
        LinkedList<Dealt> dealts = new LinkedList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, "sequence ASC");
        while (cur.moveToNext()) {
            Dealt dealt1 = new Dealt();
            dealt1.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            dealt1.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            dealt1.setCount(cur.getString(cur.getColumnIndex(COLUMN_COUNT)));
            dealt1.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQ)));
            dealts.add(dealt1);
        }
        cur.close();
        return dealts;
    }

    @Override
    public Dealt queryById(Dealt dealt) {
        if (!mDb.isOpen() || dealt == null) {
            return null;
        }
        Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{dealt.getId()}, null, null, null);
        Dealt dealt1 = new Dealt();
        if (!cur.moveToNext()) {
            return null;
        }
        dealt1.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
        dealt1.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
        dealt1.setCount(cur.getString(cur.getColumnIndex(COLUMN_COUNT)));
        dealt1.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQ)));
        cur.close();
        return dealt1;
    }

    @Override
    public void update(Dealt dealt) {
        if (!mDb.isOpen() || dealt == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, dealt.getId());
        values.put(COLUMN_NAME, dealt.getName());
        values.put(COLUMN_COUNT, dealt.getCount());
        values.put(COLUMN_SEQ, dealt.getSequence());
        mDb.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{dealt.getId()});
    }

    @Override
    public void update(List<Dealt> dealts) {
        if (!mDb.isOpen() || dealts.size() == 0) {
            return;
        }
        for (Dealt dealt : dealts) {
            update(dealt);
        }
    }

    @Override
    public void delete(Dealt dealt) {
        if (!mDb.isOpen()) {
            return;
        }
        mDb.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{dealt.getId()});
    }

    @Override
    public void closeDB() {
        if(mDb!=null){
            mDb.close();
        }
    }
}
