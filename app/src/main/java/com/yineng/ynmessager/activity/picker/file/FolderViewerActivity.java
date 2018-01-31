//***************************************************************
//*    2015-8-11  上午10:12:13
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.file;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.picker.file.FolderViewerContentAdapter.OnFileSelectListener;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author 贺毅柳
 * 
 */
public class FolderViewerActivity extends BaseActivity implements OnClickListener,OnItemClickListener,OnFileSelectListener
{
	private TextView mTxt_cancel;
	private ListView mLst_folderContent;
	private TextView mTxt_fullPath;
	private Button mBtn_send;
	private File mCurrentPath;
	private FolderViewerContentAdapter mContentAdapter;
	static final int SELECTED_FILES_MAX = 10;
	public static final int ACTIVITY_REQUEST_CODE = 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_folder_viewer);

		initViews();
	}

	private void initViews()
	{
		mTxt_cancel= (TextView)findViewById(R.id.filePicker_txt_cancel);
		mLst_folderContent = (ListView)findViewById(R.id.filePicker_lst_folderContent);
		mTxt_fullPath = (TextView)findViewById(R.id.filePicker_txt_fullPath);
		mBtn_send = (Button)findViewById(R.id.filePicker_btn_send);
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			mCurrentPath = Environment.getExternalStorageDirectory();
		}else
		{
			mCurrentPath = new File("/mnt");
		}
		
		List<File> fileList = listDirecotry(mCurrentPath);
		mContentAdapter = new FolderViewerContentAdapter(this);
		mContentAdapter.setOnFileSelectListener(this);
		mContentAdapter.setData(fileList);
		mLst_folderContent.setAdapter(mContentAdapter);
		mLst_folderContent.setOnItemClickListener(this);
		
		mTxt_cancel.setOnClickListener(this);
		mTxt_fullPath.setText(mCurrentPath.getPath());
		mBtn_send.setOnClickListener(this);
		mBtn_send.setText(getString(R.string.filePicker_send,mContentAdapter.getSelectedFiles().size(),SELECTED_FILES_MAX));
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		FolderViewerContentAdapter adapter = (FolderViewerContentAdapter)parent.getAdapter();
		
		File file = (File)parent.getItemAtPosition(position);
		if(file.isDirectory())
		{
			adapter.setData(listDirecotry(file));
			adapter.notifyDataSetChanged();
			
			mCurrentPath = file;
			mTxt_fullPath.setText(mCurrentPath.getPath());
		}
	}
	
	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.filePicker_txt_cancel:
				finish();
				break;
			case R.id.filePicker_btn_send:
				HashSet<File> selectedFiles = mContentAdapter.getSelectedFiles();
				if(selectedFiles.size() > 0)
				{
					Intent intent = new Intent();
					intent.putExtra("SelectedFiles",selectedFiles);
					setResult(RESULT_OK,intent);
					finish();
				}
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			mCurrentPath = mCurrentPath.getParentFile();
			if(mCurrentPath != null)
			{
				mContentAdapter.setData(listDirecotry(mCurrentPath));
				mContentAdapter.notifyDataSetChanged();
				mTxt_fullPath.setText(mCurrentPath.getPath());
				return true;
			}
		}
		return super.onKeyDown(keyCode,event);
	}

	private static List<File> listDirecotry(File dir)
	{
		if(dir == null || !dir.exists() || !dir.canRead() || dir.isFile()) {
            return null;
        }
		
		List<File> fileList = null;
		
		File[] files = dir.listFiles();
		if(files != null)
		{
			Arrays.sort(files);
			fileList = Arrays.asList(files);
		}
		return fileList;
	}

	@Override
	public void onSelect(CheckBox checkbox,boolean isChecked,File thisFile,Set<File> selectedFiles)
	{
		if(isChecked)
		{
			if((selectedFiles.size() + 1) > SELECTED_FILES_MAX)
			{
				checkbox.setChecked(false);
				Toast.makeText(this,getString(R.string.filePicker_selectedCountMax,SELECTED_FILES_MAX),Toast.LENGTH_SHORT).show();
			}else
			{
				selectedFiles.add(thisFile);
			}
		}else
		{
			selectedFiles.remove(thisFile);
		}
		mBtn_send.setText(getString(R.string.filePicker_send,selectedFiles.size(),SELECTED_FILES_MAX));
	}

}
