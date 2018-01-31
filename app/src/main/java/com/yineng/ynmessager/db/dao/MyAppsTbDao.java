//***************************************************************
//*    2015-10-14  下午3:03:47
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.app.MyApp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 贺毅柳
 * 
 */
public interface MyAppsTbDao
{
	String TABLE_NAME = "MyApps";
	String COLUMN_ID = "id";
	String COLUMN_PID = "pid";
	String COLUMN_ICON = "icon";
	String COLUMN_TITLE = "title";
	String COLUMN_LINK = "link";
	String COLUMN_TYPE = "type";
    String COLUMN_SOURCE_SYS = "sourceSys";

	long insert(MyApp app);

	void insert(List<MyApp> myApps);
	
	ArrayList<MyApp> query();
	
	int clear();
}
