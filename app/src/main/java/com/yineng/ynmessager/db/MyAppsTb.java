//***************************************************************
//*    2015-10-14  下午3:11:59
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
import android.support.annotation.Nullable;

import com.yineng.ynmessager.bean.app.MyApp;
import com.yineng.ynmessager.db.dao.MyAppsTbDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 贺毅柳
 */
public class MyAppsTb implements MyAppsTbDao
{

    public static final String TAG = "MyAppsTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public MyAppsTb(Context context)
    {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public MyAppsTb(Context context, String account)
    {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }

    public static String getStructureSql()
    {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(TABLE_NAME).append("(")
                .append(COLUMN_ID).append(" TEXT PRIMARY KEY,")
                .append(COLUMN_PID).append(" TEXT,")
                .append(COLUMN_ICON).append(" INTEGER,")
                .append(COLUMN_TITLE).append(" TEXT,")
                .append(COLUMN_LINK).append(" TEXT,")
                .append(COLUMN_TYPE).append(" INTEGER,")
                .append(COLUMN_SOURCE_SYS).append(" INTEGER);");
        return sql.toString();
    }

    @Override
    public long insert(@Nullable MyApp app)
    {
        if (app == null) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, app.getId());
        values.put(COLUMN_PID, app.getPid());
        values.put(COLUMN_ICON, app.getIcon());
        values.put(COLUMN_TITLE, app.getTitle());
        values.put(COLUMN_LINK, app.getLink());
        values.put(COLUMN_TYPE, app.getType());
        values.put(COLUMN_SOURCE_SYS, app.getSourceSys());
        return mDb.insert(TABLE_NAME, null, values);
    }

    @Override
    public void insert(@Nullable List<MyApp> myApps)
    {
        if (myApps == null) {
            return;
        }
        if (!mDb.isOpen())
        {
            return;
        }
        for (MyApp app : myApps)
        {
            insert(app);
        }
    }

    @Override
    public int clear()
    {
        if (!mDb.isOpen()) {
            return 0;
        }
        return mDb.delete(TABLE_NAME, null, null);
    }

    public boolean isEmpty()
    {
        return count() == 0;
    }

    public int count()
    {
        int count = 0;
        StringBuilder sql = new StringBuilder();
        sql.append("select count(").append(COLUMN_ID).append(") from ").append(TABLE_NAME);
        if (mDb.isOpen())
        {
            Cursor cur = mDb.rawQuery(sql.toString(), null);
            if (cur.moveToFirst())
            {
                count = cur.getInt(0);
            }
            cur.close();
        }
        return count;
    }

    @Override
    public ArrayList<MyApp> query()
    {
        ArrayList<MyApp> result = new ArrayList<>();
        if (mDb.isOpen())
        {
            Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                MyApp app = new MyApp();
                app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
                app.setPid(cur.getString(cur.getColumnIndex(COLUMN_PID)));
                app.setIcon(cur.getInt(cur.getColumnIndex(COLUMN_ICON)));
                app.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
                app.setLink(cur.getString(cur.getColumnIndex(COLUMN_LINK)));
                app.setType(cur.getInt(cur.getColumnIndex(COLUMN_TYPE)));
                app.setSourceSys(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE_SYS)));
                result.add(app);
            }
            cur.close();
        }
        return result;
    }

    public ArrayList<MyApp> queryByPid(String pid)
    {
        ArrayList<MyApp> result = new ArrayList<>();
        if (mDb.isOpen())
        {
            Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_PID + "=?", new String[]{pid}, null, null, null);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                MyApp app = new MyApp();
                app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
                app.setPid(cur.getString(cur.getColumnIndex(COLUMN_PID)));
                app.setIcon(cur.getInt(cur.getColumnIndex(COLUMN_ICON)));
                app.setTitle(cur.getString(cur.getColumnIndex(COLUMN_TITLE)));
                app.setLink(cur.getString(cur.getColumnIndex(COLUMN_LINK)));
                app.setType(cur.getInt(cur.getColumnIndex(COLUMN_TYPE)));
                app.setSourceSys(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE_SYS)));
                result.add(app);
            }
            cur.close();
        }
        return result;
    }
}
