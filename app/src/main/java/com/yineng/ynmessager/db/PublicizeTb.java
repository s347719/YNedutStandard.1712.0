package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.app.Publicize;
import com.yineng.ynmessager.db.dao.PublicizeDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/28.
 */

public class PublicizeTb implements PublicizeDao {
    public static final String TAG = "DealtTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public PublicizeTb(Context context) {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public PublicizeTb(Context context, String account) {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }


    @Override
    public void insert(Publicize publicize) {
        if (publicize == null) {
            return;
        }
        Publicize publicize1 = queryById(publicize);
        if (publicize1 != null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, publicize.getId());
        values.put(COLUMN_TITLE, publicize.getTitle());
        values.put(COLUMN_METHOD, publicize.getMethod());
        values.put(COLUMN_REMARK, publicize.getRemark());
        values.put(COLUMN_ORDER_BY, publicize.getOrderBy());
        values.put(COLUMN_URL, publicize.getUrl());
        values.put(COLUMN_FILE_NAME, publicize.getFileName());
        values.put(COLUMN_FILE_PATH, publicize.getFilePath());
        values.put(COLUMN_SORT_FIELD_MAP, publicize.getSortFieldMap());
        values.put(COLUMN_IS_READ, publicize.getIsRead());
        mDb.insert(TABLE_NAME, null, values);
    }

    @Override
    public void insert(List<Publicize> publicizes) {
        if (publicizes == null) {
            return;
        }
        if (!mDb.isOpen()) {
            return;
        }
        for (Publicize publicize : publicizes) {
            insert(publicize);
        }
    }

    @Override
    public LinkedList<Publicize> queryOrderBy() {
        if (!mDb.isOpen()) {
            return null;
        }
        LinkedList<Publicize> publicizes = new LinkedList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, COLUMN_ORDER_BY + " ASC");
        while (cur.moveToNext()) {
            Publicize publicize = new Publicize();
            publicize.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            publicize.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
            publicize.setMethod(cur.getInt(cur.getColumnIndex(COLUMN_METHOD)));
            publicize.setRemark(cur.getString(cur.getColumnIndex(COLUMN_REMARK)));
            publicize.setOrderBy(cur.getInt(cur.getColumnIndex(COLUMN_ORDER_BY)));
            publicize.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            publicize.setFileName(cur.getString(cur.getColumnIndex(COLUMN_FILE_NAME)));
            publicize.setFilePath(cur.getString(cur.getColumnIndex(COLUMN_FILE_PATH)));
            publicize.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FIELD_MAP)));
            publicize.setIsRead(cur.getInt(cur.getColumnIndex(COLUMN_IS_READ)));
            publicizes.add(publicize);
        }
        cur.close();
        return publicizes;
    }

    @Override
    public Publicize queryById(Publicize publicize) {
        if (!mDb.isOpen() || publicize == null) {
            return null;
        }
        Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{publicize.getId()}, null, null, null);
        Publicize publicize1 = new Publicize();
        if (!cur.moveToNext()) {
            return null;
        }
        publicize1.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
        publicize1.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
        publicize1.setMethod(cur.getInt(cur.getColumnIndex(COLUMN_METHOD)));
        publicize1.setRemark(cur.getString(cur.getColumnIndex(COLUMN_REMARK)));
        publicize1.setOrderBy(cur.getInt(cur.getColumnIndex(COLUMN_ORDER_BY)));
        publicize1.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
        publicize1.setFileName(cur.getString(cur.getColumnIndex(COLUMN_FILE_NAME)));
        publicize1.setFilePath(cur.getString(cur.getColumnIndex(COLUMN_FILE_PATH)));
        publicize1.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FIELD_MAP)));
        publicize1.setIsRead(cur.getInt(cur.getColumnIndex(COLUMN_IS_READ)));
        cur.close();
        return publicize1;
    }

    @Override
    public void update(Publicize publicize) {
        if (!mDb.isOpen() || publicize == null) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, publicize.getId());
        values.put(COLUMN_TITLE, publicize.getTitle());
        values.put(COLUMN_METHOD, publicize.getMethod());
        values.put(COLUMN_REMARK, publicize.getRemark());
        values.put(COLUMN_ORDER_BY, publicize.getOrderBy());
        values.put(COLUMN_URL, publicize.getUrl());
        values.put(COLUMN_FILE_NAME, publicize.getFileName());
        values.put(COLUMN_FILE_PATH, publicize.getFilePath());
        values.put(COLUMN_SORT_FIELD_MAP, publicize.getSortFieldMap());
        values.put(COLUMN_IS_READ, publicize.getIsRead());
        mDb.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{publicize.getId()});
    }

    @Override
    public void update(List<Publicize> publicizes) {
        if (!mDb.isOpen() || publicizes.size() == 0) {
            return;
        }
        for (Publicize publicize : publicizes) {
            update(publicize);
        }
    }

    @Override
    public void delete(Publicize publicize) {
        if (!mDb.isOpen()) {
            return;
        }
        mDb.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{publicize.getId()});
    }

    @Override
    public void closeDB() {
        if (mDb != null) {
            mDb.close();
        }
    }
}
