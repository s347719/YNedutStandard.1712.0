package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yineng.ynmessager.bean.app.DownLoadFile;
import com.yineng.ynmessager.db.dao.DownLoadFileDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import java.util.ArrayList;
import java.util.List;

/**
 * @author  by 舒欢
 * Created time: 2017/9/7
 * Descreption：下载文件信息表
 *
 *
 */

public class DownLoadFileTb implements DownLoadFileDao {
    public static final String TAG = "DownLoadFileTb";
    private SQLiteDatabase mDb;

    /**
     * 默认使用最后一次登录的帐号来初始化SettingsTb对象
     *
     * @param context
     */
    public DownLoadFileTb(Context context) {
        this(context, LastLoginUserSP.getInstance(context).getUserAccount());
    }

    public DownLoadFileTb(Context context, String account) {
        mDb = UserAccountDB.getInstance(context, account).getWritableDatabase();
    }


    public synchronized void saveOrUpdate(DownLoadFile downfile)
    {
        if(downfile == null)
        {
            return;
        }
        if(isExists(downfile.getPacketId()))
        {
            update(downfile);
        }else
        {
            insert(downfile);
        }
    }

    private void update(DownLoadFile downfile) {

        ContentValues values = new ContentValues();
        //因为名字是从数据中取出的。如果更新的时候有可能改变图片的后缀，所以在更新数据的时候图片名称不更改
//        values.put(COLUMN_NAME,downfile.getFileName());
        values.put(COLUMN_CHATID,downfile.getFileId());
        values.put(COLUMN_SOURCE,downfile.getFileSource());
        values.put(COLUMN_TIME,downfile.getDataTime());
        values.put(COLUMN_ISSEND,downfile.getIsSend());
        values.put(COLUMN_FILETYPE,downfile.getFileType());
        values.put(COLUMN_SENDUSER,downfile.getSendUserNo());
        values.put(COLUMN_PACKETID,downfile.getPacketId());
        values.put(COLUMN_SIZE,downfile.getSize());
        String[] args = {downfile.getPacketId()};
        mDb.update(TABLE_NAME,values,"packetId = ?",args);
    }

    public boolean isExists(String packetId) {
        boolean flag = false;
        Cursor cursor = mDb.query(TABLE_NAME,null,"packetId = ? ",new String[] {packetId},null,null,null);
        flag = cursor != null && cursor.getCount() > 0;
        if(cursor != null)
        {
            cursor.close();
        }
        return flag;
    }


    @Override
    public long insert(@Nullable DownLoadFile downLoadFile) {

        if(downLoadFile ==null) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME,downLoadFile.getFileName());
        values.put(COLUMN_CHATID,downLoadFile.getFileId());
        values.put(COLUMN_SOURCE,downLoadFile.getFileSource());
        values.put(COLUMN_TIME,downLoadFile.getDataTime());
        values.put(COLUMN_ISSEND,downLoadFile.getIsSend());
        values.put(COLUMN_FILETYPE,downLoadFile.getFileType());
        values.put(COLUMN_SENDUSER,downLoadFile.getSendUserNo());
        values.put(COLUMN_PACKETID,downLoadFile.getPacketId());
        values.put(COLUMN_SIZE,downLoadFile.getSize());
        return mDb.insert(TABLE_NAME, null, values);
    }

    @Override
    public void deleteByFileName(String fileName) {
        mDb.delete(TABLE_NAME, "fileName = ?", new String[]{fileName});
    }

    @Override
    public void insert(List<DownLoadFile> list) {
            if (list == null) {
                return;
            }
        if (!mDb.isOpen()){
            return;
        }
        for (DownLoadFile downloadfile : list) {
            insert(downloadfile);
        }
    }

    @Override
    public ArrayList<DownLoadFile> query(String order) {
        ArrayList<DownLoadFile> result = new ArrayList<>();
        if (mDb.isOpen())
        {
            Cursor cur = mDb.query(TABLE_NAME, null, null, null, null, null, "dataTime "+order);
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                DownLoadFile download = new DownLoadFile();
                download.setFileName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
                download.setFileId(cur.getString(cur.getColumnIndex(COLUMN_CHATID)));
                download.setFileSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
                download.setDataTime(cur.getString(cur.getColumnIndex(COLUMN_TIME)));
                download.setIsSend(cur.getInt(cur.getColumnIndex(COLUMN_ISSEND)));
                download.setFileType(cur.getInt(cur.getColumnIndex(COLUMN_FILETYPE)));
                download.setSendUserNo(cur.getString(cur.getColumnIndex(COLUMN_SENDUSER)));
                download.setPacketId(cur.getString(cur.getColumnIndex(COLUMN_PACKETID)));
                download.setSize(cur.getString(cur.getColumnIndex(COLUMN_SIZE)));
                result.add(download);
            }
            cur.close();
        }
        return result;
    }

    public List<DownLoadFile> queryByKeyString(String keyStr){
        ArrayList<DownLoadFile> result = new ArrayList<>();

        keyStr = keyStr.replaceAll("_","/_");
        keyStr = keyStr.replaceAll("%","/%");

        if (mDb.isOpen())
        {
            Cursor cur = mDb.query(TABLE_NAME, null, "fileName like ? ESCAPE '/'", new String[] {"%" + keyStr + "%"}, null, null, "dataTime DESC");
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
            {
                DownLoadFile download = new DownLoadFile();
                download.setFileName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
                download.setFileId(cur.getString(cur.getColumnIndex(COLUMN_CHATID)));
                download.setFileSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
                download.setDataTime(cur.getString(cur.getColumnIndex(COLUMN_TIME)));
                download.setIsSend(cur.getInt(cur.getColumnIndex(COLUMN_ISSEND)));
                download.setFileType(cur.getInt(cur.getColumnIndex(COLUMN_FILETYPE)));
                download.setSendUserNo(cur.getString(cur.getColumnIndex(COLUMN_SENDUSER)));
                download.setPacketId(cur.getString(cur.getColumnIndex(COLUMN_PACKETID)));
                download.setSize(cur.getString(cur.getColumnIndex(COLUMN_SIZE)));
                result.add(download);
            }
            cur.close();
        }
        return result;
    }

    @Override
    public DownLoadFile queryByPacketId(String packetId) {
        if (!mDb.isOpen() || TextUtils.isEmpty(packetId)) {
            return null;
        }
        Cursor cur = mDb.query(TABLE_NAME, null, COLUMN_PACKETID + "=?", new String[]{packetId}, null, null, null);
        DownLoadFile item = new DownLoadFile();
        if (!cur.moveToNext()) {
            return null;
        }
        item.setFileName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
        item.setFileId(cur.getString(cur.getColumnIndex(COLUMN_CHATID)));
        item.setFileSource(cur.getInt(cur.getColumnIndex(COLUMN_SOURCE)));
        item.setDataTime(cur.getString(cur.getColumnIndex(COLUMN_TIME)));
        item.setIsSend(cur.getInt(cur.getColumnIndex(COLUMN_ISSEND)));
        item.setFileType(cur.getInt(cur.getColumnIndex(COLUMN_FILETYPE)));
        item.setSendUserNo(cur.getString(cur.getColumnIndex(COLUMN_SENDUSER)));
        item.setPacketId(cur.getString(cur.getColumnIndex(COLUMN_PACKETID)));
        item.setSize(cur.getString(cur.getColumnIndex(COLUMN_SIZE)));
        cur.close();
        return item;
    }

    @Override
    public int clear() {
        if (!mDb.isOpen()) {
            return 0;
        }
        return mDb.delete(TABLE_NAME, null, null);
    }

    @Override
    public void closeDB() {
        if(mDb!=null){
            mDb.close();
        }
    }
}
