package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.app.Publicize;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/28.
 */

public interface PublicizeDao {

    String TABLE_NAME = "Publicize";
    String COLUMN_ID = "id";
    String COLUMN_TITLE = "title";
    String COLUMN_METHOD = "method";
    String COLUMN_REMARK = "remark";
    String COLUMN_ORDER_BY = "sequence";
    String COLUMN_URL = "url";
    String COLUMN_FILE_NAME = "fileName";
    String COLUMN_FILE_PATH = "filePath";
    String COLUMN_SORT_FIELD_MAP = "sortFieldMap";
    String COLUMN_IS_READ = "isRead";

    /**
     * 插入一条推广信息
     *
     * @param publicize
     */
    void insert(Publicize publicize);

    /**
     * 插入一组数据
     *
     * @param publicizes
     */
    void insert(List<Publicize> publicizes);

    /**
     * 查询推广信息
     *
     * @return
     */
    LinkedList<Publicize> queryOrderBy();

    /**
     * 通过id查询
     *
     * @param publicize
     * @return
     */
    Publicize queryById(Publicize publicize);

    /**
     * 修改一条推广数据
     *
     * @param publicize
     */
    void update(Publicize publicize);

    /**
     * 修改一组推广信息
     *
     * @param publicizes
     */
    void update(List<Publicize> publicizes);

    /**
     * 删除待办
     *
     * @param publicizes
     */
    void delete(Publicize publicizes);

    /**
     * 关闭数据库
     */
    void closeDB();
}
