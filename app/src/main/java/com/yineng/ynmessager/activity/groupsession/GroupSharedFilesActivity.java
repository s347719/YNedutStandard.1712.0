//***************************************************************
//*    2015-9-7  下午3:53:56
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.groupsession;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.SharedFile;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.DownLoadFileTb;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.FileUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.TabLayout.CommonTabLayout;
import com.yineng.ynmessager.view.TabLayout.TabEntity;
import com.yineng.ynmessager.view.TabLayout.listener.CustomTabEntity;
import com.yineng.ynmessager.view.TabLayout.listener.OnTabSelectListener;
import com.yineng.ynmessager.view.xrecyclerview.XRecyclerView;

import org.jivesoftware.smack.packet.IQ;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;

/**
 * @author 贺毅柳 群共享文件界面
 *
 */
public class GroupSharedFilesActivity extends BaseActivity
		implements ReceiveReqIQCallBack,OnClickListener, GroupFilesBaseAdapter.OnItemLongClickListener {
	private TextView mTxt_previous;
	String mGroupId;
	private ContactOrgDao mContactDao;
	private CommonTabLayout tab_layout;
	private XRecyclerView list_view;
	private GroupFilesBaseAdapter adapter;
	private String[] mTitles ;
	private ArrayList<CustomTabEntity> mTabEntities = new ArrayList<>();
	private AlertDialog mDeleteConfirmDlg;
	private OnDelConfirmClickListener mOnDelConfirmClickListener = new OnDelConfirmClickListener();
	private ContactGroupUser mGroupPermission;
	/**
	 * 取下来的数据保存
	 */
	ArrayList<Object> data = new ArrayList<>();
	ArrayList<Object> mSharedFiles = new ArrayList<>();
	//下载文件工具类
	private DownLoadFileTb downLoadFileTb;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_shared_files);
		if (downLoadFileTb==null){
			downLoadFileTb = new DownLoadFileTb(this);
		}
		mTitles = getResources().getStringArray(R.array.groupSharedFiles_viewPagerTitle);

		for (int i = 0; i < mTitles.length; i++) {
			mTabEntities.add(new TabEntity(mTitles[i], R.mipmap.update_cancel, R.mipmap.update_cancel,""));
		}
		initViews();
		L.d(mTag , "free space : " + FileUtil.getAvailableInternalMemorySize());
		tab_layout.setTabData(mTabEntities);
		tab_layout.setOnTabSelectListener(new OnTabSelectListener() {
			@Override
			public void onTabSelect(int position) {
				mSharedFiles = new ArrayList<>();
				mSharedFiles.addAll(data);
				if (position==0){
					sortDataByTime(mSharedFiles);
				}else if (position==1){
					sortDataByType(mSharedFiles);
				}else if (position==2){
					sortDataByOwer(mSharedFiles);
				}
				TasksManager.getImpl().setData(mSharedFiles);
				adapter.notifyDataSetChanged();
			}

			@Override
			public void onTabReselect(int position) {
			}
		});
		tab_layout.setCurrentTab(0);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	private void initViews()
	{
		mTxt_previous = (TextView)findViewById(R.id.groupSharedFiles_txt_previous);


		mDeleteConfirmDlg = new AlertDialog.Builder(this)
				.setTitle(R.string.groupSharedFiles_deleteConfirm)
				.setPositiveButton(android.R.string.yes, mOnDelConfirmClickListener)
				.setNegativeButton(android.R.string.no, mOnDelConfirmClickListener)
				.setIcon(android.R.drawable.ic_delete)
				.create();

		tab_layout = (CommonTabLayout) findViewById(R.id.tab_layout);


		list_view = (XRecyclerView) findViewById(R.id.list_view);
		Intent incomingIntent = getIntent();
		mGroupId = incomingIntent.getStringExtra("GroupId");
		mContactDao = new ContactOrgDao(getApplicationContext(),mApplication.mLoginUser.getUserNo());
		mGroupPermission = mContactDao.getContactGroupUserById(mGroupId, mApplication.mLoginUser.getUserNo(), Const.CONTACT_GROUP_TYPE);
		mTxt_previous.setOnClickListener(this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		list_view.setLayoutManager(layoutManager);
		list_view.setLoadingListener(new XRecyclerView.LoadingListener() {
			@Override
			public void onRefresh() {
				requestFileList(mGroupId);
			}
			@Override
			public void onLoadMore() {
			}
		});
		adapter = new GroupFilesBaseAdapter(this,mGroupId);
		adapter.setOnItemLongClickListener(this);
		list_view.setAdapter(adapter);
		list_view.setNoMore(true);
		TasksManager.getImpl().onCreate(new WeakReference<>(this));

		requestFileList(mGroupId); // IQ请求功群共享文件数据
	}

	public void postNotifyDataChanged() {
		if (adapter != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (adapter != null) {
						adapter.notifyDataSetChanged();
					}
				}
			});
		}
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.groupSharedFiles_txt_previous:
				finish();
				break;
		}
	}

	/**
	 * IQ请求当前群的共享文件列表数据
	 *
	 * @param groupId
	 */
	void requestFileList(String groupId)
	{
		XmppConnectionManager xmppConnectionManager = XmppConnectionManager.getInstance();
		// 避免网络断开重连后不能回调
		xmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP, this);

		JSONObject paramJson = new JSONObject();
		try
		{
			paramJson.put("groupName",groupId);
		}catch(JSONException e)
		{
			L.e(mTag,e.getMessage(),e);
		}

		ReqIQ iq = new ReqIQ();
		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
		iq.setAction(10);
		iq.setType(IQ.Type.GET);
		iq.setFrom(JIDUtil.getJIDByAccount(mApplication.mLoginUser.getUserNo()));
		iq.setTo("admin@" + xmppConnectionManager.getServiceName());
		iq.setParamsJson(paramJson.toString());

		xmppConnectionManager.sendPacket(iq);
	}

	/**
	 * 处理返回的群共享文件列表IQ数据
	 */
	@Override
	public void receivedReqIQResult(ReqIQResult packet)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				list_view.refreshComplete();
			}
		});
		int code = packet.getCode();
		if(code != 200)
		{
			L.w(mTag,String.format("IQ Request received error (code : %d)",code));
			return;
		}
		switch(Integer.valueOf(packet.getAction()))
		{
			case 10: // 处理返回的群共享文件数据

				handleNewListData(packet);
				break;
			case 11: // 处理有文件被删除后的刷新等操作
				runOnUiThread(new Runnable() {

					@Override
					public void run()
					{
						requestFileList(mGroupId);
					}
				});
				break;
		}
	}

	/**
	 * 处理服务器传回的Packet所包含的JSON数据，转化成SharedFile对象，并更新UI
	 * @param packet
	 */
	private void handleNewListData(ReqIQResult packet)
	{

		mSharedFiles.clear(); // 添加数据之前清空
		data.clear();
		try
		{
			JSONObject response = new JSONObject(packet.getResp());
			JSONArray fileList = response.getJSONArray("fileList");
			for(int i = 0; i < fileList.length(); i++)
			{
				JSONObject fileJson = fileList.getJSONObject(i);
				SharedFile file = new SharedFile();
				file.setId(fileJson.getString("fileId"));
				file.setName(fileJson.getString("fileName"));
				file.setType(fileJson.getString("fileType"));
				file.setSize(fileJson.getLong("fileSize") * 1024);
				file.setOwner(fileJson.getString("owner"));
				file.setUploadTime(new Date(fileJson.getLong("uploadTime")));
				file.setUpTime(Long.valueOf(fileJson.getLong("uploadTime")).intValue());

				User owner = mContactDao.queryUserInfoByUserNo(file.getOwner());
				file.setOwnerName(owner == null ? getString(R.string.groupSharedFiles_ownerUnknown) : owner.getUserName());
				data.add(file);
				mSharedFiles.add(file);
			}
		}catch(JSONException e)
		{
			L.e(mTag,e.getMessage(),e);
			mSharedFiles.clear(); // 解析数据异常则清空数据
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run()
			{
				int tab = tab_layout.getCurrentTab();
				if (tab==0){
					sortDataByTime(mSharedFiles);
				}else if (tab==1){
					sortDataByType(mSharedFiles);
				}else if (tab==2){
					sortDataByOwer(mSharedFiles);
				}
				TasksManager.getImpl().releaseTask();
				TasksManager.getImpl().setData(mSharedFiles);
				adapter.notifyDataSetChanged();
//				System.gc();
			}
		});

	}

	@Override
	public boolean onLongClick(View v, int position) {

		SharedFile sharedFile = (SharedFile) TasksManager.getImpl().get(position);
		mOnDelConfirmClickListener.toDeleted = sharedFile;
		mDeleteConfirmDlg.setMessage(sharedFile.getName());
		mDeleteConfirmDlg.show();
		return true;
	}


	static class ListContentHead
	{
		private String title;

		/**
		 * @return the title
		 */
		public String getTitle()
		{
			return title;
		}

		/**
		 * @param title the title to set
		 */
		public void setTitle(String title)
		{
			this.title = title;
		}
	}


	private class OnDelConfirmClickListener implements DialogInterface.OnClickListener
	{
		SharedFile toDeleted;

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			if (which == DialogInterface.BUTTON_POSITIVE)
			{
				if (toDeleted == null) {
					return;
				}
				String currentUser = mApplication.mLoginUser.getUserNo();
				if (currentUser.equals(toDeleted.getOwner()) || mGroupPermission.getRole() != Const.GROUP_USER_TYPE)
				{
					// 如果当前要删除的文件正在被下载中，则不可以被删除
					String url =  Const.PROTOCOL + AppController.getInstance().CONFIG_INSIDE_FILE_TRANSLATE_IP
							+ Const.GROUP_FILE_DOWNLOAD_URL +"?fileId="+ toDeleted.getId()+"&token="+ FileUtil.getSendFileToken();
					BaseDownloadTask task = FileDownloader.getImpl().create(url)
							.setPath(FileUtil.getFilePath(toDeleted.getName()));
					int status = TasksManager.getImpl().getStatus(task.getId(), FileUtil.getFilePath(toDeleted.getName()));
					if (status == FileDownloadStatus.progress)
					{
						Toast.makeText(GroupSharedFilesActivity.this, R.string.downloadedFiles_del_failedWhenDownloading, Toast.LENGTH_SHORT).show();
					} else
					{
						downLoadFileTb.deleteByFileName(toDeleted.getName());
						new File(FileUtil.getFilePath(toDeleted.getName())).delete();
						requestDelete(toDeleted);
					}
				} else
				{
					Toast.makeText(GroupSharedFilesActivity.this, R.string.groupSharedFiles_hasNoPermission, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	/**
	 * 请求数据
	 */
	private void requestDelete(SharedFile sharedFile)
	{
		XmppConnectionManager xmppConnectionManager = XmppConnectionManager.getInstance();
		xmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP, this);

		JSONObject paramJson = new JSONObject();
		try
		{
			paramJson.put("fileId", sharedFile.getId());
		} catch (JSONException e)
		{
			L.e(mTag, e.getMessage(), e);
		}

		ReqIQ iq = new ReqIQ();
		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
		iq.setAction(11);
		iq.setType(IQ.Type.SET);
		iq.setFrom(JIDUtil.getJIDByAccount(mApplication.mLoginUser.getUserNo()));
		iq.setTo("admin@" + xmppConnectionManager.getServiceName());
		iq.setParamsJson(paramJson.toString());

		xmppConnectionManager.sendPacket(iq);
	}


	/**
	 * 按上传人来排序
	 */
	private Comparator<String> mComparatorByOwnerName = new Comparator<String>() {

		@Override
		public int compare(String lhs, String rhs)
		{
			return lhs.compareTo(rhs);
		}
	};

	 void sortDataByOwer(ArrayList<Object> data)
	{
		int size = data.size();
		if(size == 0) {
			return;
		}

		ArrayList<String> ownerListTmp = new ArrayList<>();
		for(Object sharedFile : data)
		{
			ownerListTmp.add(((SharedFile)sharedFile).getOwnerName());
		}
		Collections.sort(ownerListTmp,mComparatorByOwnerName);
		LinkedHashSet<String> ownerList = new LinkedHashSet<>(ownerListTmp);
		ownerListTmp = null;

		ArrayList<Object> sortedData = new ArrayList<>(size + ownerList.size());
		for(String owner : ownerList)
		{
			ListContentHead head = new ListContentHead();
			head.setTitle(owner);
			sortedData.add(head);

			ArrayList<Object> ownerFlies = new ArrayList<>();
			for(Object sharedFile : data)
			{
				if(owner.equals(((SharedFile)sharedFile).getOwnerName()))
				{
					ownerFlies.add(sharedFile);
				}
			}
			Collections.sort(ownerFlies,mComparatorByFileName);
			sortedData.addAll(ownerFlies);
		}

		data.clear();
		data.addAll(sortedData);
	}

	/**
	 * 按日期排序
	 */
	private Comparator<Object> mComparatorByUploadTime = new Comparator<Object>() {

		@Override
		public int compare(Object lhs, Object rhs)
		{
			return ((SharedFile)rhs).getUploadTime().compareTo(((SharedFile)lhs).getUploadTime());
		}
	};

	void sortDataByTime( ArrayList<Object> data){

		int size = data.size();

		if(size == 0) {
			return;
		}

		ArrayList<Object> todayList = new ArrayList<>();
		ArrayList<Object> inOneWeekList = new ArrayList<>();
		ArrayList<Object> earlier = new ArrayList<>();

		Collections.sort(data,mComparatorByUploadTime);

		for(int i = 0; i < size; ++i)
		{
			SharedFile sharedFile = (SharedFile)data.get(i);
			Date date = sharedFile.getUploadTime();

			if(DateUtils.isToday(date.getTime()))
			{
				todayList.add(sharedFile);
			}else if(TimeUtil.isInOneWeek(date))
			{
				inOneWeekList.add(sharedFile);
			}else
			{
				earlier.add(sharedFile);
			}
		}

		if(!todayList.isEmpty())
		{
			ListContentHead head = new ListContentHead();
			head.setTitle(getString(R.string.groupSharedFiles_today));
			todayList.add(0,head);
		}

		if(!inOneWeekList.isEmpty())
		{
			ListContentHead head = new ListContentHead();
			head.setTitle(getString(R.string.groupSharedFiles_inOneWeek));
			inOneWeekList.add(0,head);
		}

		if(!earlier.isEmpty())
		{
			ListContentHead head = new ListContentHead();
			head.setTitle(getString(R.string.groupSharedFiles_earlier));
			earlier.add(0,head);
		}

		data.clear();
		data.addAll(todayList);
		data.addAll(inOneWeekList);
		data.addAll(earlier);
	}

/**
 * 按类型排序
 */

 void sortDataByType(ArrayList<Object> data)
{
	int size = data.size();

	if(size == 0) {
		return;
	}

	ArrayList<Object> docList = new ArrayList<>();
	ArrayList<Object> imgList = new ArrayList<>();
	ArrayList<Object> videoList = new ArrayList<>();
	ArrayList<Object> othersList = new ArrayList<>();

	Collections.sort(data,mComparatorBySuffix);

	for(int i = 0; i < size; ++i)
	{
		SharedFile sharedFile = (SharedFile)data.get(i);

		String filename = sharedFile.getName();
		String suffix = getSuffixFromFileName(filename);

		boolean isContinue = false;
		for(String str : SUFFIX_DOC)
		{
			if(str.equalsIgnoreCase(suffix))
			{
				docList.add(sharedFile);
				isContinue = true;
				break;
			}
		}

		if(isContinue) {
			continue;
		}
		for(String str : SUFFIX_IMG)
		{
			if(str.equalsIgnoreCase(suffix))
			{
				imgList.add(sharedFile);
				isContinue = true;
				break;
			}
		}

		if(isContinue) {
			continue;
		}
		for(String str : SUFFIX_VIDEO)
		{
			if(str.equalsIgnoreCase(suffix))
			{
				videoList.add(sharedFile);
				isContinue = true;
				break;
			}
		}

		if(!isContinue) {
			othersList.add(sharedFile);
		}
	}

	if(!docList.isEmpty())
	{
		Collections.sort(docList,mComparatorByFileName);
		ListContentHead head = new ListContentHead();
		head.setTitle(getString(R.string.groupSharedFiles_doc));
		docList.add(0,head);
	}

	if(!imgList.isEmpty())
	{
		Collections.sort(imgList,mComparatorByFileName);
		ListContentHead head = new ListContentHead();
		head.setTitle(getString(R.string.groupSharedFiles_img));
		imgList.add(0,head);
	}

	if(!videoList.isEmpty())
	{
		Collections.sort(videoList,mComparatorByFileName);
		ListContentHead head = new ListContentHead();
		head.setTitle(getString(R.string.groupSharedFiles_video));
		videoList.add(0,head);
	}

	if(!othersList.isEmpty())
	{
		Collections.sort(othersList,mComparatorByFileName);
		ListContentHead head = new ListContentHead();
		head.setTitle(getString(R.string.groupSharedFiles_others));
		othersList.add(0,head);
	}

	data.clear();
	data.addAll(docList);
	data.addAll(imgList);
	data.addAll(videoList);
	data.addAll(othersList);
}
private static final String DOT = ".";
	private static final String[] SUFFIX_DOC = {".txt", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".wps", ".rtf", ".pdf"};
	private static final String[] SUFFIX_IMG = {".jpg", ".png", ".bmp", ".gif", ".tif", ".pic"};
	private static final String[] SUFFIX_VIDEO = {".mp4", ".avi", ".wmv", ".rmvb", ".mpg", ".swf", ".mov"};

	private Comparator<Object> mComparatorBySuffix = new Comparator<Object>() {

		@Override
		public int compare(Object lhs, Object rhs)
		{
			String lhsFilename = ((SharedFile)lhs).getName();
			String lhsSuffix = getSuffixFromFileName(lhsFilename);

			String rhsFilename = ((SharedFile)rhs).getName();
			String rhsSuffix = getSuffixFromFileName(rhsFilename);

			return lhsSuffix.compareToIgnoreCase(rhsSuffix);
		}
	};

	private String getSuffixFromFileName(String fileName)
	{
		int dotIndex = fileName.lastIndexOf(DOT);
		if(dotIndex < 0) {
			dotIndex = 0;
		}
		return fileName.substring(dotIndex);
	}
	private Comparator<Object> mComparatorByFileName = new Comparator<Object>()
	{

		@Override
		public int compare(Object lhs, Object rhs)
		{
			String lhsFilename = ((SharedFile) lhs).getName();
			String rhsFilename = ((SharedFile) rhs).getName();

			return lhsFilename.compareToIgnoreCase(rhsFilename);
		}
	};
}
