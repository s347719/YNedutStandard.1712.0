//***************************************************************
//*    2015-8-6  下午4:17:05
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.image;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.util.ViewHolder;

import java.util.ArrayList;

/**
 * @author 贺毅柳
 * 
 */
public class ImagePickContentAdapter extends BaseAdapter
{
	public static final String TAG = "ImgPickContentAdapter";
	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<ImageFile> mData;
	private ImageLoader mImageLoader;
	private ImageSelectedListener mImageSelectedListener;
	private DisplayImageOptions mDisplayImageOptions;
	
	ImagePickContentAdapter(Context context)
	{
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImageLoader = ImageLoader.getInstance();
		mDisplayImageOptions = new DisplayImageOptions.Builder().cloneFrom(AppController.getInstance().mImageLoaderDisplayOptions)
                                                        .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
														.displayer(new FadeInBitmapDisplayer(400))
														.build();
	}

	void setData(ArrayList<ImageFile> data)
	{
		mData = data;
	}

	@Override
	public int getCount()
	{
		return mData == null ? 0 : mData.size();
	}

	@Override
	public ImageFile getItem(int position)
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
			convertView = mInflater.inflate(R.layout.item_imgpick_griditem,parent,false);
		}

		final ImageView img = ViewHolder.get(convertView,R.id.imgPick_img_gridItem_img);
		final CheckBox selected = ViewHolder.get(convertView,R.id.imgPick_chk_gridItem_selected);

		final ImageFile item = getItem(position);

		mImageLoader.displayImage("file://" + item.getPath(),img,mDisplayImageOptions);
		selected.setChecked(item.isSelected());
		selected.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				boolean isChecked = selected.isChecked();
				item.setSelected(isChecked);
				if(mImageSelectedListener != null)
				{
					mImageSelectedListener.onSelected(selected,isChecked,item);
				}
			}
		});
		return convertView;
	}

	/**
	 * 给Adapter设置ImageSelectedListener的回调接口对象
	 * @param imageSelectedListener
	 */
	void setImageSelectedListener(ImageSelectedListener imageSelectedListener)
	{
		mImageSelectedListener = imageSelectedListener;
	}

	/**
	 * 有图片勾选或取消勾选时的监听器
	 * @author 贺毅柳
	 *
	 */
	interface ImageSelectedListener
	{
		/**
		 * 某个图片被勾选时的回调
		 * @param checkbox 勾选的CheckBox控件
		 * @param isSelected 勾选状态
		 * @param imageFile 对应的ImageFile对象
		 */
		void onSelected(CheckBox checkbox, boolean isSelected, ImageFile imageFile);
	}

}
