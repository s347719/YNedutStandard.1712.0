//***************************************************************
//*    2015-8-5  下午2:22:39
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.image;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.util.ViewHolder;

import java.util.List;

/**
 * @author 贺毅柳
 * 
 */
class ImageFolderAdapter extends BaseAdapter
{
	public static final String TAG = "ImageFolderAdapter";
	private Context mContext;
	private LayoutInflater mInflater;
	private List<ImageFolder> mData;
	private ImageLoader mImageLoader;
	private DisplayImageOptions mDisplayImageOptions;
	private Resources mResources;
	
	ImageFolderAdapter(Context context)
	{
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
		mImageLoader = ImageLoader.getInstance();
		mDisplayImageOptions = new DisplayImageOptions.Builder().cloneFrom(AppController.getInstance().mImageLoaderDisplayOptions)
														.displayer(new FadeInBitmapDisplayer(400))
														.build();
	}

	void setData(List<ImageFolder> data)
	{
		mData = data;
	}

	@Override
	public int getCount()
	{
		return mData == null ? 0 : mData.size();
	}

	@Override
	public ImageFolder getItem(int position)
	{
		return mData.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.item_imgfolder_folderlist,parent,false);
		}
		
		final ImageView cover = ViewHolder.get(convertView,R.id.imgFolder_img_listItem_cover);
		final TextView title = ViewHolder.get(convertView,R.id.imgFolder_img_listItem_title);
		
		ImageFolder folder = getItem(position);
		List<ImageFile> child = folder.getImages();
		mImageLoader.displayImage("file://" + child.get(0).getPath(),cover,mDisplayImageOptions);
		title.setText(mResources.getString(R.string.imgFolder_folderName,folder.getDirectory().getName(),child.size()));
		
		return convertView;
	}

}
