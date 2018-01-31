//***************************************************************
//*    2015-8-11  上午11:14:09
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.file;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.util.ViewHolder;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 贺毅柳
 * 
 */
public class FolderViewerContentAdapter extends BaseAdapter
{
	private Context mContext;
	private LayoutInflater mInflater;
	private List<File> mData;
	private HashSet<File> mSelectedFiles = new HashSet<File>();
	private OnFileSelectListener mOnFileSelectListener;

	FolderViewerContentAdapter(Context context)
	{
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	void setData(List<File> data)
	{
		mData = data;
	}

	void setOnFileSelectListener(OnFileSelectListener onFileSelectListener)
	{
		mOnFileSelectListener = onFileSelectListener;
	}
	
	@Override
	public int getCount()
	{
		return mData == null ? 0 : mData.size();
	}

	@Override
	public File getItem(int position)
	{
		return mData.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public int getItemViewType(int position)
	{
		int viewType = 1;
		File file = getItem(position);
		if(file.isDirectory())
		{
			viewType = 0;
		}
		return viewType;
	}

	public HashSet<File> getSelectedFiles()
	{
		return mSelectedFiles;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		switch(getItemViewType(position))
		{
			case 0:
				convertView = getFolderView(position,convertView,parent);
				break;
			case 1:
				convertView = getFileView(position,convertView,parent);
				break;
		}

		return convertView;
	}

	private View getFolderView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.item_folder_viewer_contentlist_folder,parent,false);
		}

		final ImageView icon = ViewHolder.get(convertView,R.id.folderViewer_img_listItem_icon);
		final TextView foldername = ViewHolder.get(convertView,R.id.folderViewer_txt_foldername);

		File folder = getItem(position);

		icon.setImageResource(R.mipmap.folder);
		foldername.setText(folder.getName());

		return convertView;
	}

	private View getFileView(int position, View convertView, ViewGroup parent)
	{
		if(convertView == null)
		{
			convertView = mInflater.inflate(R.layout.item_folder_viewer_contentlist_file,parent,false);
		}

		final ImageView icon = ViewHolder.get(convertView,R.id.folderViewer_img_listItem_icon);
		final TextView foldername = ViewHolder.get(convertView,R.id.folderViewer_txt_filename);
		final CheckBox select = ViewHolder.get(convertView,R.id.folderViewer_chk_select);
		
		final File file = getItem(position);

		icon.setImageResource(R.mipmap.file);
		foldername.setText(file.getName());
		select.setChecked(mSelectedFiles.contains(file));
		select.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				if(mOnFileSelectListener != null)
				{
					mOnFileSelectListener.onSelect(select,select.isChecked(),file,mSelectedFiles);
				}
			}
		});
		return convertView;
	}
	
	interface OnFileSelectListener
	{
		void onSelect(CheckBox checkbox,boolean isChecked,File thisFile,Set<File> selectedFiles);
	}

}
