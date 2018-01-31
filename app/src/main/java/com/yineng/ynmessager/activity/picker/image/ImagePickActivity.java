//***************************************************************
//*    2015-8-4  下午2:45:21
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.image;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;

import java.util.ArrayList;

/**
 * @author 贺毅柳
 * 
 */
@Deprecated
public class ImagePickActivity extends BaseActivity implements OnClickListener,OnItemClickListener,ImagePickContentAdapter.ImageSelectedListener
{
	private TextView mTxt_title;
	private TextView mTxt_cancel;
	private TextView mTxt_back;
	private GridView mGrd_content;
	private Button mBtn_send;
	private ArrayList<ImageFolder> mFolderList;
	private ImageFolder mCurrentFolder;
	private ImagePickContentAdapter mContentAdapter;
	private static final int SELECTED_MAX = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_img_pick);

		initViews();
	}

	private void initViews()
	{
		Intent intent = getIntent();
		int  folderIndex = intent.getIntExtra("folderIndex",0);
		mFolderList = (ArrayList<ImageFolder>)intent.getSerializableExtra("folderList");
		mCurrentFolder = mFolderList.get(folderIndex);
		
		mTxt_title = (TextView)findViewById(R.id.imgPick_txt_title);
		mTxt_cancel = (TextView)findViewById(R.id.imgPick_txt_cancel);
		mTxt_back = (TextView)findViewById(R.id.imgPick_txt_back);
		mGrd_content = (GridView)findViewById(R.id.imgPick_grd_content);
		mBtn_send = (Button)findViewById(R.id.imgPick_btn_send);

		mTxt_title.setText(mCurrentFolder.getDirectory().getName());
		mTxt_cancel.setOnClickListener(this);
		mTxt_back.setOnClickListener(this);
		mGrd_content.setOnItemClickListener(this);
		mBtn_send.setOnClickListener(this);
		mBtn_send.setText(getString(R.string.imgPick_send,getImageSelectedCount(),SELECTED_MAX));

		mContentAdapter = new ImagePickContentAdapter(this);
		mContentAdapter.setData(mCurrentFolder.getImages());
		mContentAdapter.setImageSelectedListener(this);
		mGrd_content.setAdapter(mContentAdapter);
	}
	
	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.imgPick_txt_cancel:
				operationBack(ImageFolderActivity.RESULT_USER_CANCELED,null);
				break;
			case R.id.imgPick_txt_back:
				Intent data = new Intent();
				data.putExtra("folderList",mFolderList);
				operationBack(RESULT_OK,data);
				break;
			case R.id.imgPick_btn_send:
				ArrayList<ImageFile> selectedImages = getImageSelected();
				if(selectedImages.size() > 0)
				{
					Intent intent = new Intent();
					intent.putExtra("selectedImages",selectedImages);
					operationBack(ImageFolderActivity.RESULT_DONE,intent);
				}
				break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent intent = new Intent(this,ImageViewerActivity.class);
		intent.putExtra("ImageFolder",mCurrentFolder);
		intent.putExtra("Position",position);
		startActivity(intent);
	}
	
	@Override
	public void onSelected(CheckBox checkbox, boolean isSelected, ImageFile imageFile)
	{
		int count = getImageSelectedCount();
		if(isSelected && count > SELECTED_MAX)
		{
			imageFile.setSelected(false);
			checkbox.setChecked(false);
			Toast.makeText(this,getString(R.string.imgPick_selectedCountMax,SELECTED_MAX),Toast.LENGTH_SHORT).show();
		}
		mBtn_send.setText(getString(R.string.imgPick_send,getImageSelectedCount(),SELECTED_MAX));
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			Intent data = new Intent();
			data.putExtra("folderList",mFolderList);
			operationBack(RESULT_OK,data);
		}
		return true;
	}
	
	/**
	 * 简化的返回Activity结果的方法
	 * @param resultCode 返回的结果标识
	 * @param data 可能需要回传给上个Activity的数据
	 */
	private void operationBack(int resultCode,Intent data)
	{
		setResult(resultCode,data);
		finish();
	}
	
	/**
	 * 获取所有已勾选的图片文件列表
	 * @return 文件列表 ArrayList
	 */
	private ArrayList<ImageFile> getImageSelected()
	{
		ArrayList<ImageFile> selected = new ArrayList<ImageFile>();
		for(ImageFolder folder : mFolderList)
		{
			ArrayList<ImageFile> images = folder.getImages();
			for(ImageFile img : images)
			{
				if(img.isSelected())
				{
					selected.add(img);
				}
			}
		}
		return selected;
	}
	
	/**
	 * 获取所有已勾选的图片文件数量
	 * @return
	 */
	private int getImageSelectedCount()
	{
		int count = 0;
		for(ImageFolder folder : mFolderList)
		{
			ArrayList<ImageFile> images = folder.getImages();
			for(ImageFile img : images)
			{
				if(img.isSelected())
				{
					++count;
				}
			}
		}
		return count;
	}
	
}
