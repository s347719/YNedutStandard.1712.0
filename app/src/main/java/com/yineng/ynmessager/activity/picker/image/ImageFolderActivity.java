//***************************************************************
//*    2015-8-4  下午3:41:46
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.picker.image;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.os.AsyncTaskCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.picker.picture.GalleryActivity;
import com.yineng.ynmessager.bean.imgpicker.ImageFile;
import com.yineng.ynmessager.bean.imgpicker.ImageFolder;
import com.yineng.ynmessager.util.L;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>该Activity已过时。应该用 {@link GalleryActivity}</p>
 * @author 贺毅柳
 */
@Deprecated
public class ImageFolderActivity extends BaseActivity implements OnClickListener, OnItemClickListener
{
	private TextView mTxt_cancel;
	private ListView mLst_Folders;
	private ImageFolderAdapter mAdapter;
	private ProgressDialog mLoadingDialog;
	private ArrayList<ImageFolder> mImageFolderList;
	public static final String[] IMAGE_SUPPORTED_TYPE = {".jpg", ".png", ".gif", ".bmp"}; // 要搜索的图片后缀名类型
	public static final int ACTIVITY_REQUEST_CODE = 1;
	
	/** 用户取消发送图片操作，返回聊天界面 */
	public static final int RESULT_USER_CANCELED = 2;
	/** 用户完成选择图片操作，返回聊天界面 */
	public static final int RESULT_DONE = 3;

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_img_folder);

		initViews();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		mLoadingDialog.dismiss();
	}

	private void initViews()
	{
		mTxt_cancel = (TextView)findViewById(R.id.imgFolder_txt_cancel);
		mLst_Folders = (ListView)findViewById(R.id.imgFolder_lst_folders);

		mTxt_cancel.setOnClickListener(this);

		mLoadingDialog = new ProgressDialog(this);
		mLoadingDialog.setMessage(getString(R.string.imgFolder_loading));
		mLoadingDialog.setCanceledOnTouchOutside(false);

		mAdapter = new ImageFolderAdapter(this);
		mLst_Folders.setAdapter(mAdapter);
		mLst_Folders.setOnItemClickListener(this);

		AsyncTaskCompat.executeParallel(mSearchImageFilesTask,(Void[])null);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode,resultCode,data);
		if(resultCode == RESULT_USER_CANCELED)
		{
			operationBack(resultCode,null);
		}else if(resultCode == RESULT_OK)
		{
			mImageFolderList = (ArrayList<ImageFolder>)data.getSerializableExtra("folderList");
			mAdapter.setData(mImageFolderList);
			mAdapter.notifyDataSetChanged();
		}else if(resultCode == RESULT_DONE)
		{
			operationBack(resultCode,data);
		}
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.imgFolder_txt_cancel:
				operationBack(RESULT_USER_CANCELED,null);
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent intent = new Intent(this,ImagePickActivity.class);
		intent.putExtra("folderIndex",position);
		intent.putExtra("folderList",mImageFolderList);
		startActivityForResult(intent,0);
	}
	
	private void operationBack(int resultCode,Intent data)
	{
		setResult(resultCode,data);
		finish();
	}

	/**
	 * 用于搜索所有图片文件的AsyncTask
	 */
	private AsyncTask<Void, Integer, ArrayList<ImageFolder>> mSearchImageFilesTask = new AsyncTask<Void, Integer, ArrayList<ImageFolder>>() {

		@Override
		protected ArrayList<ImageFolder> doInBackground(Void... params)
		{
			ArrayList<ImageFolder> imageFolders = new ArrayList<>();
			
			ArrayList<ImageFile> allImageFiles = ScanAllImageFiles();
			ArrayList<String> paths = new ArrayList<>(allImageFiles.size());
			for(ImageFile _if : allImageFiles)
			{
				paths.add(_if.getParent());
			}
			paths = new ArrayList<>(Arrays.asList((new LinkedHashSet<String>(paths).toArray(new String[]{}))));
			
			for(String path : paths)
			{
				ImageFolder folder = new ImageFolder();
				folder.setDirectory(new File(path));
				
				ArrayList<ImageFile> subFiles = new ArrayList<>();
				for(ImageFile _if : allImageFiles)
				{
					if(path.equals(_if.getParent()))
					{
						subFiles.add(_if);
					}
				}
				folder.setImages(subFiles);
				imageFolders.add(folder);
			}
			return imageFolders;
		}

		@Override
        protected void onPostExecute(ArrayList<ImageFolder> result)
		{
			mAdapter.setData(result);
			mAdapter.notifyDataSetChanged();
			mImageFolderList = result;
			mLoadingDialog.dismiss();
		}

		@Override
        protected void onPreExecute()
		{
			mLoadingDialog.show();
		}
	};

	public ArrayList<ImageFile> ScanAllImageFiles()
	{
		Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = {MediaStore.Images.ImageColumns.DATA};
		Cursor c = null;
		SortedSet<String> dirList = new TreeSet<String>();
		ArrayList<ImageFile> resultIAV = new ArrayList<ImageFile>();

		String[] directories = null;
		if(u != null)
		{
			c = managedQuery(u,projection,null,null,null);
		}

		if((c != null) && (c.moveToFirst()))
		{
			do
			{
				String tempDir = c.getString(0);
				tempDir = tempDir.substring(0,tempDir.lastIndexOf("/"));
				try
				{
					dirList.add(tempDir);
				}catch(Exception e)
				{
					L.e(mTag,e.getMessage(),e);
				}
			}while(c.moveToNext());
			directories = new String[dirList.size()];
			dirList.toArray(directories);
		}

		for(int i = 0; i < dirList.size(); i++)
		{
			File imageDir = new File(directories[i]);
			File[] imageList = imageDir.listFiles();
			if(imageList == null) {
                continue;
            }
			for(File imagePath : imageList)
			{
				try
				{
					if(imagePath.isDirectory())
					{
						imageList = imagePath.listFiles();
					}
					
					String imageFilename = imagePath.getName();
					for(String suffix : IMAGE_SUPPORTED_TYPE)
					{
						if(imageFilename.endsWith(suffix))
						{
							String path = imagePath.getAbsolutePath();
							ImageFile imageFile = new ImageFile(path);
							resultIAV.add(imageFile);
							break;
						}
					}
				}
				catch(Exception e)
				{
					L.e(mTag,e.getMessage(),e);
				}
			}
		}

		return resultIAV;
	}

}
