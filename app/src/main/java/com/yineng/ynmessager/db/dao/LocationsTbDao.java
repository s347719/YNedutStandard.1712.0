package com.yineng.ynmessager.db.dao;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.yineng.ynmessager.bean.service.Location;

import java.util.List;

/**
 * Created by 贺毅柳 on 2016/3/23 10:11.
 */
public interface LocationsTbDao
{
    /**
     * 表名
     */
    String TABLE_NAME = "Locations";
    /**
     * ID
     */
    String COLUMN_ID = "id";
    /**
     * 经度
     */
    String COLUMN_LONGITUDE = "longitude";
    /**
     * 纬度
     */
    String COLUMN_LATITUDE = "latitude";
    /**
     * 误差（米）
     */
    String COLUMN_RADIUS = "radius";
    /**
     * 经纬度对应的地址
     */
    String COLUMN_ADDRESS = "address";
    /**
     * GPS开关状态
     */
    String COLUMN_GPS_OPEN = "gps_open";
    /**
     * 时间戳
     */
    String COLUMN_TIMESTAMP = "timestamp";

    /**
     * 用户ID
     */
    String COLUMN_RSGSYSUSERID = "rsglSysUserId";
    /**
     * 插入一条定位记录
     *
     * @param location
     * @return 参见 {@link android.database.sqlite.SQLiteDatabase#insert(String, String, ContentValues)}
     */
    long insert(@NonNull Location location);

    /**
     * 查询数据库中的所有记录，记录包含所有列
     *
     * @return
     */
    @NonNull
    List<Location> query();

    /**
     * 写入原生语言查找数据
     * @return
     */
    List<Location> limitQuery(int limit);

    /**
     * 根据Location对象的ID，来删除数据库中指定的记录
     *
     * @param location
     * @return 参见 {@link android.database.sqlite.SQLiteDatabase#delete(String, String, String[])}
     */
    int delete(@NonNull Location location);

    /**
     * 根据Location对象的ID，来更新数据库中指定的记录
     *
     * @param location
     * @return 参见 {@link android.database.sqlite.SQLiteDatabase#update(String, ContentValues, String, String[])}
     */
    int update(@NonNull Location location);
}
