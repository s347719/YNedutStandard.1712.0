package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.app.Dealt;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/27.
 */

public interface DealtDao {
    String TABLE_NAME="Dealt";
    String COLUMN_ID = "id";
    String COLUMN_NAME = "name";
    String COLUMN_COUNT = "count";
    String COLUMN_SEQ = "sequence";

    /**
     * 插入一条待办
     * @param dealt
     */
    void insert(Dealt dealt);

    /**
     * 插入一组数据
     * @param dealts
     */
    void insert(List<Dealt> dealts);

    /**
     * 查询dealt
     * @return
     */
    LinkedList<Dealt> queryOrderBySequence();

    /**
     * 通过id查询
     * @param dealt
     * @return
     */
    Dealt queryById(Dealt dealt);

    /**
     * 修改一条待办数据
     * @param dealt
     */
    void update(Dealt dealt);

    /**
     * 修改一组待办事项
     * @param dealts
     */
    void update(List<Dealt> dealts);

    /**
     * 删除待办
     * @param dealt
     */
    void delete(Dealt dealt);

    /**
     * 关闭数据库
     */
    void closeDB();
}
