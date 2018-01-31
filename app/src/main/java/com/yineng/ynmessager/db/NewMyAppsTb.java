package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.app.NewMyApps;
import com.yineng.ynmessager.db.dao.NewMyAppsDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/18.
 */

public class NewMyAppsTb implements NewMyAppsDao {

    public static final String TAG = "MyAppsTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public NewMyAppsTb(Context context) {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public NewMyAppsTb(Context context, String account) {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }

    @Override
    public long insert(NewMyApps app) {
        if (app == null) {
            return -1;
        }
        NewMyApps apps = queryById(app.getId());
        if (apps != null) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, app.getId());
        values.put(COLUMN_ICON_NAME, app.getIcon());
        values.put(COLUMN_NAME, app.getName());
        values.put(COLUMN_IS_NEW, app.getIsNew());
        values.put(COLUMN_APP_TYPE_ID, app.getAppTypeId());
        values.put(COLUMN_APP_TYPE_NAME, app.getAppTypeName());
        if (app.getIsRecommend()) {
            values.put(COLUMN_iS_RECOMMEND, NewMyAppsDao.RECOMMEND);
        } else {
            values.put(COLUMN_iS_RECOMMEND, NewMyAppsDao.UNRECOMMEND);
        }
        values.put(COLUMN_URL, app.getUrl());
        values.put(COLUMN_SEQUENCE, app.getSequence());
        values.put(COLUMN_SORT_FILELD_MAP, app.getSortFieldMap());
        values.put(COLUMN_SOURCE, app.getSource());
        values.put(COLUMN_LAST_USE_DATE, app.getLastUseDate());
        values.put(COLUMN_SUBMENU,app.getSubmenu());
        values.put(COLUMN_UPDATE_TIME,app.getUpdateTime());
        values.put(COLUMN_USER_DELETE,app.getUserDelete());
        values.put(COLUMN_TOP_SEQUENCE,app.getTopSequence());
        values.put(COLUMN_TYPE_SEQUENCE,app.getTypeSequence());
        return mDb.insert(TABLE_NAME, null, values);
    }

    @Override
    public void insert(List<NewMyApps> apps) {
        if (apps == null) {
            return;
        }
        if (!mDb.isOpen()) {
            return;
        }
        for (NewMyApps app : apps) {
            insert(app);
        }
    }

    @Override
    public void update(List<NewMyApps> apps) {
        if (!mDb.isOpen() || apps.size() == 0) {
            return;
        }
        for (NewMyApps app : apps) {
            update(app);
        }
    }

    @Override
    public void update(NewMyApps app) {
        if (!mDb.isOpen()) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, app.getId());
        values.put(COLUMN_ICON_NAME, app.getIcon());
        values.put(COLUMN_NAME, app.getName());
        values.put(COLUMN_IS_NEW, app.getIsNew());
        values.put(COLUMN_APP_TYPE_ID, app.getAppTypeId());
        values.put(COLUMN_APP_TYPE_NAME, app.getAppTypeName());
        if (app.getIsRecommend()) {
            values.put(COLUMN_iS_RECOMMEND, NewMyAppsDao.RECOMMEND);
        } else {
            values.put(COLUMN_iS_RECOMMEND, NewMyAppsDao.UNRECOMMEND);
        }
        values.put(COLUMN_URL, app.getUrl());
        values.put(COLUMN_SEQUENCE, app.getSequence());
        values.put(COLUMN_SORT_FILELD_MAP, app.getSortFieldMap());
        values.put(COLUMN_SOURCE, app.getSource());
        values.put(COLUMN_LAST_USE_DATE, app.getLastUseDate());
        values.put(COLUMN_SUBMENU,app.getSubmenu());
        values.put(COLUMN_UPDATE_TIME,app.getUpdateTime());
        values.put(COLUMN_USER_DELETE,app.getUserDelete());
        values.put(COLUMN_TOP_SEQUENCE,app.getTopSequence());
        values.put(COLUMN_TYPE_SEQUENCE,app.getTypeSequence());
        mDb.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(app.getId())});
    }

    @Override
    public void delete(String id) {
        if (!mDb.isOpen()) {
            return;
        }
        mDb.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{id});
    }

    @Override
    public NewMyApps queryById(String id) {
        if (!mDb.isOpen()) {
            return null;
        }
        Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        NewMyApps app = new NewMyApps();
        if (!cur.moveToNext()) {
            return null;
        }
        app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
        app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
        app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
        app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
        int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
        if (isRecommend == NewMyAppsDao.RECOMMEND) {
            app.setIsRecommend(true);
        } else {
            app.setIsRecommend(false);
        }
        app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
        app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
        app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
        app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
        app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
        app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
        app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
        app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
        app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
        app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
        app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
        app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
        cur.close();
        return app;
    }

    @Override
    public ArrayList<NewMyApps> query() {
        if (!mDb.isOpen()) {
            return null;
        }

        ArrayList<NewMyApps> myApps = new ArrayList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, null);
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            myApps.add(app);
        }
        cur.close();
        return myApps;
    }

    @Override
    public LinkedList<NewMyApps> queryOrderBySeq() {
        if (!mDb.isOpen()) {
            return null;
        }

        LinkedList<NewMyApps> myApps = new LinkedList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, "sequence ASC");
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            myApps.add(app);
        }
        cur.close();
        return myApps;
    }

    @Override
    public LinkedList<NewMyApps> queryOrderByTopSeq() {
        if (!mDb.isOpen()) {
            return null;
        }

        LinkedList<NewMyApps> myApps = new LinkedList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, "topSequence ASC");
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            myApps.add(app);
        }
        cur.close();
        return myApps;
    }

    /**
     * 根据时间排序
     *
     * @return
     */
    @Override
    public LinkedList<NewMyApps> queryOrderByDate() {
        if (!mDb.isOpen()) {
            return null;
        }

        LinkedList<NewMyApps> myApps = new LinkedList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, "last_use_date DESC");
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            myApps.add(app);
        }
        cur.close();
        return myApps;
    }

    @Override
    public List<HashMap<String, Object>> queryGroupByGroup() {
        if (!mDb.isOpen()) {
            return null;
        }
        List<HashMap<String, Object>> result = new ArrayList<>();
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, "appTypeId", null, "typeSequence DESC");
        while (cur.moveToNext()) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("typeId", cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            map.put("typeName", cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            result.add(map);
        }
        cur.close();
        return result;
    }

    @Override
    public List<NewMyApps> queryByGroupId(String typeId) {
        if (!mDb.isOpen()) {
            return null;
        }
        Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_APP_TYPE_ID + "=?", new String[]{typeId}, null, null, "sequence ASC");
        List<NewMyApps> apps = new ArrayList<>();
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            apps.add(app);
        }
        cur.close();
        return apps;
    }

    @Override
    public List<NewMyApps> queryByLike(String name) {
        if (!mDb.isOpen()) {
            return null;
        }
        Cursor cur = mDb.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_NAME+" LIKE '%"+name+"%'",null);
        List<NewMyApps> apps = new ArrayList<>();
        while (cur.moveToNext()) {
            NewMyApps app = new NewMyApps();
            app.setId(cur.getString(cur.getColumnIndex(COLUMN_ID)));
            app.setIcon(cur.getString(cur.getColumnIndex(COLUMN_ICON_NAME)));
            app.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            app.setIsNew(cur.getInt(cur.getColumnIndex(COLUMN_IS_NEW)));
            int isRecommend = cur.getInt(cur.getColumnIndex(COLUMN_iS_RECOMMEND));
            if (isRecommend == NewMyAppsDao.RECOMMEND) {
                app.setIsRecommend(true);
            } else {
                app.setIsRecommend(false);
            }
            app.setUrl(cur.getString(cur.getColumnIndex(COLUMN_URL)));
            app.setAppTypeId(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_ID)));
            app.setAppTypeName(cur.getString(cur.getColumnIndex(COLUMN_APP_TYPE_NAME)));
            app.setSequence(cur.getInt(cur.getColumnIndex(COLUMN_SEQUENCE)));
            app.setTopSequence(cur.getInt(cur.getColumnIndex(COLUMN_TOP_SEQUENCE)));
            app.setSortFieldMap(cur.getString(cur.getColumnIndex(COLUMN_SORT_FILELD_MAP)));
            app.setSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
            app.setLastUseDate(cur.getString(cur.getColumnIndex(COLUMN_LAST_USE_DATE)));
            app.setSubmenu(cur.getString(cur.getColumnIndex(COLUMN_SUBMENU)));
            app.setUpdateTime(cur.getString(cur.getColumnIndex(COLUMN_UPDATE_TIME)));
            app.setUserDelete(cur.getInt(cur.getColumnIndex(COLUMN_USER_DELETE)));
            app.setTypeSequence(cur.getInt(cur.getColumnIndex(COLUMN_TYPE_SEQUENCE)));
            apps.add(app);
        }
        cur.close();
        return apps;
    }

    @Override
    public void cleanMyApps() {
        mDb.execSQL("DELETE FROM " + NewMyAppsDao.TABLE_NAME + " ;");
    }

    @Override
    public void closeDB() {
        if (mDb.isOpen()) {
            mDb.close();
        }
    }
}
