//***************************************************************
//*    2015-4-23  下午4:23:44
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db.dao;

import android.content.ContentValues;
import android.database.Cursor;

import com.yineng.ynmessager.bean.settings.Setting;

/**
 * 用户数据库设置表的操作接口
 *
 * @author 贺毅柳
 */
public interface SettingsTbDao {
    /**
     * 表名
     */
    String TABLE_NAME = "Settings";
    // 列名
    String COLUMN_ID = "id";
    String COLUMN_DISTRACTION_FREE = "DistractionFree";
    String COLUMN_DISTRACTION_FREE_BEGIN_H = "DistractionFree_Begin_H";
    String COLUMN_DISTRACTION_FREE_BEGIN_M = "DistractionFree_Begin_M";
    String COLUMN_DISTRACTION_FREE_END_H = "DistractionFree_End_H";
    String COLUMN_DISTRACTION_FREE_END_M = "DistractionFree_End_M";
    String COLUMN_AUDIO = "Audio";
    String COLUMN_AUDIO_GROUP = "Audio_Group";
    String COLUMN_VIBRATE = "Vibrate";
    String COLUMN_VIBRATE_GROUP = "Vibrate_Group";
    String COLUMN_RECEIVE_WHEN_EXIT = "ReceiveWhenExit";
    String COLUMN_FONT_SIZE = "FontSize";
    String COLUMN_SKIN = "Skin";
    String COLUMN_DARK_MODE = "DarkMode";
    String COLUMN_ALWAYS_AUTO_RECEIVE_IMG = "AlwaysAutoReceiveImg";
    String COLUMN_IS_RECOMMEND_APP = "isRecommendApp";
    String COLUMN_IS_RECOMMEND_APP_DIALOG_SHOW = "isRecommendApp_DialogShow";
    // 每个列的默认值
    int VALUE_DISTRACTION_FREE = 0;
    int VALUE_DISTRACTION_FREE_BEGIN_H = 23;
    int VALUE_DISTRACTION_FREE_BEGIN_M = 0;
    int VALUE_DISTRACTION_FREE_END_H = 7;
    int VALUE_DISTRACTION_FREE_END_M = 0;
    int VALUE_AUDIO = 1;
    int VALUE_AUDIO_GROUP = 1;
    int VALUE_VIBRATE = 0;
    int VALUE_VIBRATE_GROUP = 0;
    int VALUE_RECEIVE_WHEN_EXIT = 1;
    int VALUE_FONT_SIZE = 1;
    int VALUE_SKIN = 0;
    int VALUE_DARK_MODE = 0;
    int VALUE_ALWAYS_AUTO_RECEIVE_IMG = 1;
    int VALUE_RECOMMEND_APP = 0;
    int VALUE_RECOMMEND_APP_DIALOG = 1;

    /**
     * 插入默认数据，也就是默认的设置。会先检查表是否为空，为空则执行。
     *
     * @return 最后插入的数据的行号。返回0表示为执行插入默认数据；-1表示出错
     */
    long insert();

    /**
     * 插入指定的数据到Settings表中
     *
     * @param values ContentValues对象，要插入的数据
     * @return 最后插入的数据的行号。返回-1表示出错
     */
    long insert(ContentValues values);

    /**
     * 更新Setting表中ID为1的行的数据
     *
     * @param setting 一个Setting对象
     * @return 更新操作后受影响的行数
     */
    int update(Setting setting);

    /**
     * 查询Setting表中的数据
     *
     * @param columns 要查询的列。传入null返回所有列
     * @return 返回ID为1的行所查询的列的数据（Settings表本来只需要一条数据）
     */
    Cursor query(String[] columns);

    /**
     * 检查表是否为空
     *
     * @return true表示为空，否则false
     */
    boolean isEmpty();

    /**
     * 通过查询数据库来生成对应的Setting实体类对象
     *
     * @return Setting VO
     */
    Setting obtainSettingFromDb();
}
