package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.app.NewMyApps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yn on 2017/7/18.
 */

public interface NewMyAppsDao {
    String TABLE_NAME = "NewMyApps";
    String COLUMN_ID = "id";
    String COLUMN_ICON_NAME = "icon";
    String COLUMN_NAME = "name";
    String COLUMN_IS_NEW = "is_new";
    String COLUMN_APP_TYPE_ID = "appTypeId";
    String COLUMN_APP_TYPE_NAME = "appTypeName";
    String COLUMN_SEQUENCE = "sequence";
    String COLUMN_TOP_SEQUENCE = "topSequence";
    String COLUMN_TYPE_SEQUENCE = "typeSequence";
    String COLUMN_LAST_USE_DATE = "last_use_date";
    String COLUMN_iS_RECOMMEND = "isRecommend";
    String COLUMN_SORT_FILELD_MAP = "sortFieldMap";
    String COLUMN_SOURCE = "source";
    String COLUMN_URL = "url";
    String COLUMN_SUBMENU = "submenu";
    String COLUMN_UPDATE_TIME = "updateTime";
    String COLUMN_USER_DELETE = "user_delete";

    public static int RECOMMEND = 0;//推荐
    public static int UNRECOMMEND = 1;//不推荐

    public static int USER_DELETE_TRUE = 0;//用户删除
    public static int USER_DELETE_FALSE = 1;//用户未删除

    /**
     * 插入一条App信息
     *
     * @param app
     * @return
     */
    long insert(NewMyApps app);

    /**
     * 插入一组数据
     *
     * @param apps
     */
    void insert(List<NewMyApps> apps);

    /**
     * 更新一组app信息
     *
     * @param apps
     */
    void update(List<NewMyApps> apps);

    /**
     * app信息更新
     *
     * @param apps
     */
    void update(NewMyApps apps);

    /**
     * 删除一个app
     *
     * @param id
     */
    void delete(String id);

    /**
     * 通过id查询app
     *
     * @param id
     * @return
     */
    NewMyApps queryById(String id);

    /**
     * 查询app列表
     *
     * @return
     */
    ArrayList<NewMyApps> query();

    /**
     * 通过seq排序app列表
     *
     * @return
     */
    LinkedList<NewMyApps> queryOrderBySeq();

    /**
     * 通过top-seq排序app列表
     *
     * @return
     */
    LinkedList<NewMyApps> queryOrderByTopSeq();

    /**
     * 通过最后使用日期排序app列表
     *
     * @return
     */
    LinkedList<NewMyApps> queryOrderByDate();

    /**
     * 通过分组查询app列表
     *
     * @return
     */
    List<HashMap<String, Object>> queryGroupByGroup();

    /**
     * 通过类型id查询app列表
     *
     * @param group_id
     * @return
     */
    List<NewMyApps> queryByGroupId(String group_id);

    /**
     * 模糊查询应用名称
     * @param name
     * @return
     */
    List<NewMyApps> queryByLike(String name);

    /**
     * 清除数据
     */
    void cleanMyApps();

    /**
     * 关闭数据库
     */
    void closeDB();
}