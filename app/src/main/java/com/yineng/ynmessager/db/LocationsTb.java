package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.yineng.ynmessager.bean.service.Location;
import com.yineng.ynmessager.db.dao.LocationsTbDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by 贺毅柳 on 2016/3/23 10:22.
 */
public class LocationsTb implements LocationsTbDao
{
    public static final String TAG = "LocationsTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public LocationsTb(Context context)
    {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public LocationsTb(Context context, String account)
    {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }

    public static String getStructureSql()
    {
        return new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(TABLE_NAME).append("(")
                .append(COLUMN_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(COLUMN_LONGITUDE).append(" REAL,")
                .append(COLUMN_LATITUDE).append(" REAL,")
                .append(COLUMN_RADIUS).append(" INTEGER,")
                .append(COLUMN_ADDRESS).append(" TEXT,")
                .append(COLUMN_GPS_OPEN).append(" INTEGER,")
                .append(COLUMN_RSGSYSUSERID).append(" TEXT,")
                .append(COLUMN_TIMESTAMP).append(" INTEGER);")
                .toString();
    }

    @Override
    public long insert(@NonNull Location location)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_RADIUS, location.getRadius());
        values.put(COLUMN_ADDRESS, location.getAddress());
        values.put(COLUMN_GPS_OPEN, location.isGpsOpen());
        values.put(COLUMN_TIMESTAMP, location.getTimestamp().getTime());
        values.put(COLUMN_RSGSYSUSERID, location.getRsglSysUserId());

        return mDb.insert(TABLE_NAME, null, values);
    }

    @NonNull
    @Override
    public List<Location> query()
    {
        List<Location> results;
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, null);
        results = new ArrayList<>(cur.getCount());
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
        {
            int id = cur.getInt(cur.getColumnIndex(COLUMN_ID));
            double longitude = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));
            double latitude = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
            int radius = cur.getInt(cur.getColumnIndex(COLUMN_RADIUS));
            String address = cur.getString(cur.getColumnIndex(COLUMN_ADDRESS));
            boolean gpsOpen = cur.getInt(cur.getColumnIndex(COLUMN_GPS_OPEN)) != 0;
            long timestamp = cur.getLong(cur.getColumnIndex(COLUMN_TIMESTAMP));
            String rsgSysUserId = cur.getString(cur.getColumnIndex(COLUMN_RSGSYSUSERID));

            Location l = new Location();
            l.setId(id);
            l.setLongitude(longitude);
            l.setLatitude(latitude);
            l.setRadius(radius);
            l.setAddress(address);
            l.setGpsOpen(gpsOpen);
            l.setTimestamp(new Date(timestamp));
            l.setRsglSysUserId(rsgSysUserId);

            results.add(l);
        }
        cur.close();

        return results;
    }

    @Override
    public List<Location> limitQuery(int num) {
        List<Location> results;
        Cursor cur = mDb.query(TABLE_NAME, null, null, null, "id", null, COLUMN_TIMESTAMP+" DESC", num + "");
        results = new ArrayList<>(cur.getCount());
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            int id = cur.getInt(cur.getColumnIndex(COLUMN_ID));
            double longitude = cur.getDouble(cur.getColumnIndex(COLUMN_LONGITUDE));
            double latitude = cur.getDouble(cur.getColumnIndex(COLUMN_LATITUDE));
            int radius = cur.getInt(cur.getColumnIndex(COLUMN_RADIUS));
            String address = cur.getString(cur.getColumnIndex(COLUMN_ADDRESS));
            boolean gpsOpen = cur.getInt(cur.getColumnIndex(COLUMN_GPS_OPEN)) != 0;
            long timestamp = cur.getLong(cur.getColumnIndex(COLUMN_TIMESTAMP));
            String rsgSysUserId = cur.getString(cur.getColumnIndex(COLUMN_RSGSYSUSERID));

            Location l = new Location();
            l.setId(id);
            l.setLongitude(longitude);
            l.setLatitude(latitude);
            l.setRadius(radius);
            l.setAddress(address);
            l.setGpsOpen(gpsOpen);
            l.setTimestamp(new Date(timestamp));
            l.setRsglSysUserId(rsgSysUserId);

            results.add(l);
        }
        cur.close();

        return results;

    }

    @Override
    public int delete(@NonNull Location location)
    {
        int id = location.getId();
        return mDb.delete(TABLE_NAME, COLUMN_ID.concat("=?"), new String[]{String.valueOf(id)});
    }

    @Override
    public int update(@NonNull Location location)
    {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LONGITUDE, location.getLongitude());
        values.put(COLUMN_LATITUDE, location.getLatitude());
        values.put(COLUMN_RADIUS, location.getRadius());
        values.put(COLUMN_ADDRESS, location.getAddress());
        values.put(COLUMN_GPS_OPEN, location.isGpsOpen());
        values.put(COLUMN_TIMESTAMP, location.getTimestamp().getTime());
        values.put(COLUMN_RSGSYSUSERID, location.getRsglSysUserId());

        return mDb.update(TABLE_NAME, values, COLUMN_ID.concat("=?"), new String[]{String.valueOf(location.getId())});
    }
}
