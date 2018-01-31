//***************************************************************
//*    2015-9-24  下午5:58:12
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.app;

/**
 * @author 贺毅柳
 * 
 */
public class MyApp
{
	private String id;
	private String pid;
	private int icon;
	private String title;
	private String link;
	private int type;
    private int sourceSys;
    public static final int TYPE_WEB = 0;
	public static final int TYPE_NATIVE = 1;
	public static final int TYPE_THIRD_APP = 2;
	public static final int TYPE_THIRD_APP_V3 = 3;
	public static final int TYPE_THIRD_APP_V4 = 4;
    public static final int SOURCE_SYS_OA = 0;
    public static final int SOURCE_SYS_V8 = 1;
    public static final int SOURCE_SYS_SMESIS = 2;
	private boolean isV8web = false;
	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return the pid
	 */
	public String getPid()
	{
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid)
	{
		this.pid = pid;
	}

	/**
	 * @return the icon
	 */
	public int getIcon()
	{
		return icon;
	}

	/**
	 * @param icon
	 *            the icon to set
	 */
	public void setIcon(int icon)
	{
		this.icon = icon;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	/**
	 * @return the link
	 */
	public String getLink()
	{
		return link;
	}

	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(String link)
	{
		this.link = link;
	}

	/**
	 * @return the type
	 */
	public int getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(int type)
	{
		this.type = type;
	}

    public int getSourceSys()
    {
        return sourceSys;
    }

    public void setSourceSys(int sourceSys)
    {
        this.sourceSys = sourceSys;
    }

    /**
	 * 是否是v8的应用
	 * @return
	 */
	public boolean isV8web() {
		return isV8web;
	}

	public void setIsV8web(boolean isV8web) {
		this.isV8web = isV8web;
	}
}
