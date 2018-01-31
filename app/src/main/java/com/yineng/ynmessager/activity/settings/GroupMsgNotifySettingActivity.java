//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：heyiliu@ynedut.cn
//*    成都依能科技有限公司
//*    Copyright? 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.util.ViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 群讨论组消息提醒 设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class GroupMsgNotifySettingActivity extends BaseActivity implements OnClickListener
{
	private TextView mTxt_previous; // 左上角返回按钮
	private ListView mLst_content; // 内容列表（群、讨论组）
	private List<HashMap<String, Object>> mData = new ArrayList<HashMap<String, Object>>();
	private ContactOrgDao mDao = null;
	private ConfirmNotifyModeWindow mPopwin_confirm;
	private RelativeLayout mTxt_notify;
	private RelativeLayout mTxt_silence;
	private RelativeLayout mTxt_cancel;
	private ImageView mImg_notifyChecked;
	private ImageView mImg_silenceChecked;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_msg_notify_setting);

		initViews();
	}

	private void initViews()
	{
		mTxt_previous = (TextView)findViewById(R.id.groupMsgNotifySetting_txt_previous);
		mTxt_previous.setOnClickListener(this);
		mLst_content = (ListView)findViewById(R.id.groupMsgNotifySetting_lst_content);
		mDao = new ContactOrgDao(getApplicationContext());

		// 初始化PopupWindow
		mPopwin_confirm = new ConfirmNotifyModeWindow(this);
		View windowView = mPopwin_confirm.getContentView();
		mTxt_notify = (RelativeLayout)windowView.findViewById(R.id.groupMsgNotifySetting_rel_notify);
		mTxt_silence = (RelativeLayout)windowView.findViewById(R.id.groupMsgNotifySetting_rel_silence);
		mTxt_cancel = (RelativeLayout)windowView.findViewById(R.id.groupMsgNotifySetting_rel_cancel);
		mImg_notifyChecked = (ImageView)windowView.findViewById(R.id.groupMsgNotifySetting_img_notifyChecked);
		mImg_silenceChecked = (ImageView)windowView.findViewById(R.id.groupMsgNotifySetting_img_silenceChecked);
		mTxt_notify.setOnClickListener(this);
		mTxt_silence.setOnClickListener(this);
		mTxt_cancel.setOnClickListener(this);

		loadData();
		// 设置列表适配器、监听器
		ContentListAdapter adapter = new ContentListAdapter();
		ContentList_OnItemClickListener itemClickListener = new ContentList_OnItemClickListener();
		mLst_content.setAdapter(adapter);
		mLst_content.setOnItemClickListener(itemClickListener);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mPopwin_confirm.dismiss();
	}
	
	/**
	 * 从数据库加载群、讨论组相关数据到界面显示
	 */
	private void loadData()
	{
		mData.clear();
		// 查询出所有的群和讨论组
		List<ContactGroup> groupList = new ArrayList<ContactGroup>(mDao.queryGroupAndDiscussList("notifyMode"));
		// 整理mData
		// 添加 接收并提醒 标题
		HashMap<String, Object> map;
		map = new HashMap<String, Object>();
		map.put("data","接收并提醒");
		map.put("isTitle",true);
		mData.add(map);
		// 添加行（接收提醒的）
		for(ContactGroup group : groupList)
		{
			if(group.getNotifyMode() == ContactGroup.NOTIFYMODE_NOTIFY)
			{
				map = new HashMap<String, Object>();
				map.put("data",group);
				map.put("isTitle",false);
				mData.add(map);
			}
		}
		// 添加 接收但不提醒 标题
		map = new HashMap<String, Object>();
		map.put("data","接收但不提醒");
		map.put("isTitle",true);
		mData.add(map);
		// 最后添加接收不提醒的行
		for(ContactGroup group : groupList)
		{
			if(group.getNotifyMode() == ContactGroup.NOTIFYMODE_SILENCE)
			{
				map = new HashMap<String, Object>();
				map.put("data",group);
				map.put("isTitle",false);
				mData.add(map);
			}
		}
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.groupMsgNotifySetting_txt_previous:
				finish();
				break;
			case R.id.groupMsgNotifySetting_rel_notify: // Popupwindow中点击“接收并提醒”
				AdapterView parent = mPopwin_confirm.getListAdapterView();
				ContentListAdapter adapter = (ContentListAdapter)parent.getAdapter();
				HashMap<String, Object> item = adapter.getItem(mPopwin_confirm.getListPosition());
				ContactGroup group = (ContactGroup)item.get("data");
				group.setNotifyMode(ContactGroup.NOTIFYMODE_NOTIFY);
				// 更新到数据库
				mDao.updateGroupOrDiscuss(group);
				// 从数据库重新加载数据并刷新UI
				loadData();
				adapter.notifyDataSetChanged();
				mPopwin_confirm.dismiss();
				break;
			case R.id.groupMsgNotifySetting_rel_silence: // Popupwindow中点击“接收但不提醒”
				AdapterView _parent = mPopwin_confirm.getListAdapterView();
				ContentListAdapter _adapter = (ContentListAdapter)_parent.getAdapter();
				HashMap<String, Object> _item = _adapter.getItem(mPopwin_confirm.getListPosition());
				ContactGroup _group = (ContactGroup)_item.get("data");
				_group.setNotifyMode(ContactGroup.NOTIFYMODE_SILENCE);
				// 更新到数据库
				mDao.updateGroupOrDiscuss(_group);
				// 从数据库重新加载数据并刷新UI
				loadData();
				_adapter.notifyDataSetChanged();
				mPopwin_confirm.dismiss();
				break;
			case R.id.groupMsgNotifySetting_rel_cancel: // Popupwindow中点击“取消”
				mPopwin_confirm.setListAdapterView(null);
				mPopwin_confirm.setListPosition(0);
				mPopwin_confirm.dismiss();
				break;
		}
	}

	/**
	 * 列表Item点击的监听器
	 * 
	 * @author 贺毅柳
	 */
	private class ContentList_OnItemClickListener implements OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
		{
			ContentListAdapter adapter = (ContentListAdapter)parent.getAdapter(); // 得到ListView关联的Adapter
			HashMap<String, Object> item = adapter.getItem(position); // 获取当前点击的Item关联的数据
			boolean isTitle = (Boolean)item.get("isTitle"); // 判断是否点击的只是ListView中用来显示的标题
			if(!isTitle) // 如果不是标题
			{
				ContactGroup group = (ContactGroup)item.get("data");
				mPopwin_confirm.setListAdapterView(mLst_content);
				mPopwin_confirm.setListPosition(position);
				int notifyMode = group.getNotifyMode();
				if(notifyMode == ContactGroup.NOTIFYMODE_NOTIFY)
				{
					mImg_notifyChecked.setVisibility(View.VISIBLE);
					mImg_silenceChecked.setVisibility(View.INVISIBLE);
				}else if(notifyMode == ContactGroup.NOTIFYMODE_SILENCE)
				{
					mImg_notifyChecked.setVisibility(View.INVISIBLE);
					mImg_silenceChecked.setVisibility(View.VISIBLE);
				}
				mPopwin_confirm
						.showAtLocation(findViewById(R.id.groupMsgNotifySetting_rel_rootView),Gravity.BOTTOM,0,0);
			}
		}
	}

	/**
	 * 群、讨论组消息提醒设置列表的Adapter
	 * 
	 * @author 贺毅柳
	 * 
	 */
	private class ContentListAdapter extends BaseAdapter
	{
		private static final int VIEW_TYPE_TITLE = 0;
		private static final int VIEW_TYPE_NOT_TITLE = 1;

		@Override
		public int getCount()
		{
			return mData.size();
		}

		@Override
		public int getItemViewType(int position)
		{
			// 根据HashMap对象中的isTitle属性来判断是否为标题行
			HashMap<String, Object> item = getItem(position);
			boolean isTitle = (Boolean)item.get("isTitle");
			if(isTitle)
			{
				return VIEW_TYPE_TITLE;
			}else
			{
				return VIEW_TYPE_NOT_TITLE;
			}
		}

		@Override
		public int getViewTypeCount()
		{
			return 2;
		}

		@Override
		public HashMap<String, Object> getItem(int position)
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
			LayoutInflater inflater = getLayoutInflater();
			final int viewType = getItemViewType(position);
			switch(viewType)
			{
				case VIEW_TYPE_TITLE: // 如果是标题行
					if(convertView == null)
					{
						convertView = inflater.inflate(R.layout.group_notify_setting_listitem_title,parent,false);
					}

					TextView txt_title = ViewHolder.get(convertView,R.id.groupMsgNotifySetting_txt_listItem_title);

					HashMap<String, Object> titleItem = getItem(position);
					String title = (String)titleItem.get("data");
					txt_title.setText(title);
					break;
				case VIEW_TYPE_NOT_TITLE: // 如果不是标题行
					if(convertView == null)
					{
						convertView = inflater.inflate(R.layout.group_notify_setting_listitem,parent,false);
					}

					ImageView img_icon = ViewHolder.get(convertView,R.id.groupMsgNotifySetting_img_listItem_icon);
					TextView txt_groupName = ViewHolder.get(convertView,
							R.id.groupMsgNotifySetting_txt_listItem_groupName);

					HashMap<String, Object> item = getItem(position);
					ContactGroup group = (ContactGroup)item.get("data");

					img_icon.setImageResource(group.getGroupType() == Const.CONTACT_DISGROUP_TYPE ? R.mipmap.session_discuss
							: R.mipmap.session_group);
					// 因为有些群、讨论组有或者没有subject，如果有subject就优先显示，否则显示NaturalName
					String groupName = TextUtils.isEmpty(group.getSubject()) ? group.getNaturalName() : group
							.getSubject();
					txt_groupName.setText(groupName);
					break;
			}
			return convertView;
		}

	}

}
