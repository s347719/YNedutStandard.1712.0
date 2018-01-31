package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.app.DownLoadFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 舒欢
 * Created time: 2017/9/7
 * Descreption：
 */

public interface DownLoadFileDao  {
    String TABLE_NAME="DownLoadFile"; //表名
    String COLUMN_ID = "id";
    String COLUMN_NAME = "fileName"; // 图片的文件名
    String COLUMN_CHATID = "fileId";//当前会话的ID
    String COLUMN_SOURCE = "fileSource";  //当前保存图片会话类型   0-个人会话  1-讨论组   2-群组
    String COLUMN_TIME = "dataTime";  //dataTime  图片保存时间（取数据保存的时间）
    String COLUMN_ISSEND = "isSend";  // 图片是发送的还是接收的    0:是发送 1:不是发送（即接收）
    String COLUMN_FILETYPE = "fileType";  //fileType   下载文件类型在这里都是图片为 1    1-图片  2-文件
    String COLUMN_SENDUSER = "sendUserNo";  //发送者帐号：userNo
    String COLUMN_PACKETID = "packetId";  // 下载文件在聊天记录中的packetId 唯一标记
    String COLUMN_SIZE ="size" ;   // 文件大小
    /**
     * 插入一条下载数据
     * @param
     */
    long insert(DownLoadFile downLoadFile);

    /**
     * 插入一组下载数据
     * @param
     */
    void insert(List<DownLoadFile> list);

    /**
     * 查询所有的数据
     * @return
     */
    ArrayList<DownLoadFile> query(String order );

    /**
     * 通过packetId查询相关下载信息
     * @return
     */
    DownLoadFile queryByPacketId(String packetId);

    /**
     * 根据packetId删除数据
     * @param fileName
     */
    void deleteByFileName(String fileName);

    /**
     * 清除表内所有数据
     * @return
     */
    int clear();

    /**
     * 关闭数据库
     */
    void closeDB();

}
