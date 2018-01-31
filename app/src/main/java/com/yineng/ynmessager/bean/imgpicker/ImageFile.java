//***************************************************************
//*    2015-8-7  下午4:37:51
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.imgpicker;

import java.io.File;


/**
 * @author 贺毅柳
 * 
 */
public class ImageFile extends File
{
	private static final long serialVersionUID = -8834885102177938933L;
	private boolean mIsSelected = false;

	public ImageFile(String path)
	{
		super(path);
	}

	public boolean isSelected()
	{
		return mIsSelected;
	}

	public void setSelected(boolean isSelected)
	{
		mIsSelected = isSelected;
	}

	public static ImageFile fromFile(File file)
	{
		return new ImageFile(file.getPath());
	}

}
