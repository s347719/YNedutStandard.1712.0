package com.yineng.ynmessager.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.BaseChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.bean.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SearchChatRecordEditText extends Dialog implements
		OnFocusChangeListener, TextWatcher {

	private Context mContext;
	/**
	 * 搜索结果列表
	 */
	private ListView mContactSearchListView;
	/**
	 * 搜索框
	 */
	private EditText mSearchEditText;
	private ContactOrgDao mContactOrgDao;
	private InputMethodManager inputMM;
	/**
	 * 搜索结果
	 */
	private List<Object> mSearchResultObjects = new ArrayList<Object>();
	private ChatRecordAdapter mChatRecordAdapter;
	/**
	 * 取消搜索按钮
	 */
	private Button mContactCancelSearchBt;
	/**
	 * 1.两人  2.群组  3.讨论组
	 */
	private int mChatType = 0;
	private P2PChatMsgDao mP2PChatMsgDao;
	private GroupChatDao mGroupChatDao;
	private DisGroupChatDao mDisGroupChatDao;
	private String mChatId;
	
	/**
	 * 结果为空的提示
	 */
	private TextView mContactSearchResultEmptyTV;
	
	public SearchChatRecordEditText(Context context,String chatId,int chatType) {
		// super(mContext);
		super(context, R.style.mydialog);
		this.mContext = context;
		mChatId = chatId;
		mChatType = chatType;
		initData();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//  Auto-generated method stub
		super.onCreate(savedInstanceState);
		mContactOrgDao = new ContactOrgDao(mContext);
		initView();
		initSearchEditViewListener();

	}

	/**
	 * 初始化信息
	 */
	private void initData() {
		switch (mChatType) {
		case Const.CHAT_TYPE_P2P:
			mP2PChatMsgDao = new P2PChatMsgDao(mContext);
			break;
		case Const.CHAT_TYPE_GROUP:
			mGroupChatDao = new GroupChatDao(mContext);
			break;
		case Const.CHAT_TYPE_DIS:
			mDisGroupChatDao = new DisGroupChatDao(mContext);
			break;
		default:
			break;
		}
		inputMM = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);		
	}
	
	public void initView() {
		Window win = getWindow();
		win.getDecorView().setPadding(0, 0, 0, 0);
		win.setWindowAnimations(0);
		win.setGravity(Gravity.TOP);
		WindowManager.LayoutParams lp = win.getAttributes();
		lp.width = WindowManager.LayoutParams.FILL_PARENT;
		lp.height = WindowManager.LayoutParams.FILL_PARENT;
		win.setAttributes(lp);

		this.setContentView(R.layout.contact_search_view);
		mSearchEditText = (EditText) findViewById(R.id.se_contact_org_search);
		mContactSearchListView = (ListView) findViewById(R.id.lv_search_contact_org_listview);
		mContactCancelSearchBt = (Button) findViewById(R.id.bt_cancel_search);
		mContactSearchResultEmptyTV = (TextView) findViewById(R.id.tv_search_contact_result_list_no_data);
	}

	/**
	 * 删除按钮的引用
	 */
	private Drawable mClearDrawable;

	private void initSearchEditViewListener() {
		// 获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
		mClearDrawable = mSearchEditText.getCompoundDrawables()[2];
		if (mClearDrawable == null) {
			mClearDrawable = mContext.getResources().getDrawable(
					R.mipmap.emotionstore_progresscancelbtn);
		}
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());
		setClearIconVisible(false);

		mContactCancelSearchBt
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mOnCancelSearchAnimationListener != null) {
							mOnCancelSearchAnimationListener
									.cancelSearchContactAnimation();
						}
					}
				});

		setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface dialog) {
				if (inputMM.isActive()) {
					inputMM.toggleSoftInput(0,
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		});

		mSearchEditText.setOnFocusChangeListener(this);
		mSearchEditText.addTextChangedListener(this);
		mSearchEditText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent event) {
				if (mSearchEditText.getCompoundDrawables()[2] != null) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						boolean touchable = event.getX() > (mSearchEditText
								.getWidth() - mSearchEditText.getPaddingRight() - mClearDrawable
									.getIntrinsicWidth())
								&& (event.getX() < ((mSearchEditText.getWidth() - mSearchEditText
										.getPaddingRight())));
						if (touchable) {
							mSearchEditText.setText("");
						}
					}
				}
				return false;
			}
		});

		mContactSearchListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
				if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
					if (mContactSearchListView.getCount() <= 0) {
						mOnCancelSearchAnimationListener
								.cancelSearchContactAnimation();
						return true;
					} else {
						HideKeyBoardAndClearFocus();
					}
				}
				return false;
			}
		});
		mContactSearchListView
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int pos, long arg3) {
						Object mObject = mChatRecordAdapter.getItem(pos);
//						if (mObject instanceof User) {
//							User mUser = (User) mObject;
//							startPersonInfoActivity(mUser);
//						} else if (mObject instanceof OrganizationTree) {
//							OrganizationTree tempOrg = (OrganizationTree) mObject;
////							startChildOrgActivity(tempOrg);
//							resetOrgPathList(tempOrg);
//							startChildOrgActivity(tempOrg,0);
//							dismiss();
//						} else if (mObject instanceof ContactGroup) {
//
//						}
						mOnResultListItemCLickListener.scrollToClickItem(mObject);
					}
				});
	}

	/**
	 * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
	 * 
	 * @param visible
	 */
	protected void setClearIconVisible(boolean visible) {
		Drawable right = visible ? mClearDrawable : null;
		mSearchEditText.setCompoundDrawables(
				mSearchEditText.getCompoundDrawables()[0],
				mSearchEditText.getCompoundDrawables()[1], right,
				mSearchEditText.getCompoundDrawables()[3]);
	}

	/**
	 * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
	 */
	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(mSearchEditText.getText().length() > 0);
		} else {
			setClearIconVisible(false);
		}
	}

	/**
	 * 当输入框里面内容发生变化的时候回调的方法
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int count, int after) {
		setClearIconVisible(s.length() > 0);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {
			switch (mChatType) {
			case Const.CHAT_TYPE_P2P:
				mSearchResultObjects = mP2PChatMsgDao.querySearchResultByKeyWords(mChatId,s.toString());
				break;
			case Const.CHAT_TYPE_GROUP:
				mSearchResultObjects = mGroupChatDao.querySearchResultByKeyWords(mChatId,s.toString());
				break;
			case Const.CHAT_TYPE_DIS:
				mSearchResultObjects = mDisGroupChatDao.querySearchResultByKeyWords(mChatId,s.toString());
				break;
			default:
				break;
			}
		} else {
			if (mSearchResultObjects != null) {
				mSearchResultObjects.clear();
			}
		}
		
		// 去掉纯表情的消息记录匹配
		//先找出需要移除的纯表情消息
		List<Object> removeList = new ArrayList<Object>();
		String msg;
		for(Object result : mSearchResultObjects)
		{
			if(result instanceof P2PChatMsgEntity)
			{
				msg = ((P2PChatMsgEntity)result).getMessage();
			}else
			{
				msg = ((GroupChatMsgEntity)result).getMessage();
			}
			MessageBodyEntity contentEntity = JSON.parseObject(msg,MessageBodyEntity.class);
			msg = contentEntity.getContent().replaceAll(Const.Regex.FACE + "+","");
			msg = msg.replaceAll(Const.Regex.IMG + "+","");
			msg = msg.replaceAll(Const.Regex.FILE + "+","");
			if(TextUtils.isEmpty(msg) || !msg.contains(s))
			{
				removeList.add(result);  //添加到待删除
			}
		}
		//然后依次从列表中移除
		for(Object remove : removeList)
		{
			mSearchResultObjects.remove(remove);
		}
		
		showPopupWindow(mSearchResultObjects);
	}

	/** 去掉搜索框焦点和隐藏软键盘 **/
	public void HideKeyBoardAndClearFocus() {
		inputMM.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
	}

	public void showPopupWindow(List<Object> mSearchResultObjects) {
		// if (mSearchResultObjects != null && mSearchResultObjects.size() > 0)
		// {
		// mContactSearchListView.setAlpha(1);
		// } else {
		// mContactSearchListView.setAlpha((float) 0.6);
		// }
		
		if (mChatRecordAdapter == null) {
			mChatRecordAdapter = new ChatRecordAdapter(mContext,
					mSearchResultObjects);
			mContactSearchListView.setAdapter(mChatRecordAdapter);
		} else {
			mChatRecordAdapter.setnChatMsgEntities(mSearchResultObjects);
			mChatRecordAdapter.notifyDataSetChanged();
		}
		//判断是否显示空数据界面
		if (mSearchEditText.getText().length()>0 && mChatRecordAdapter.getCount() <= 0) {
			mContactSearchListView.setEmptyView(mContactSearchResultEmptyTV);
		} else {
			mContactSearchResultEmptyTV.setVisibility(View.GONE);
			mContactSearchListView.setEmptyView(null);
		}
	}

	private onCancelSearchAnimationListener mOnCancelSearchAnimationListener;

	/**
	 * 设置取消动画监听
	 * @param tempOnCancelSearchAnimationListener
	 */
	public void setOnCancelSearchAnimationListener(
			onCancelSearchAnimationListener tempOnCancelSearchAnimationListener) {
		this.mOnCancelSearchAnimationListener = tempOnCancelSearchAnimationListener;
	}

	public interface onCancelSearchAnimationListener {
		void cancelSearchContactAnimation();
	}
	
	private onResultListItemCLickListener mOnResultListItemCLickListener;

	/**
	 * 设置点击跳转监听
	 * @param mOnResultListItemCLickListener
	 */
	public void setOnResultListItemCLickListener(
			onResultListItemCLickListener mOnResultListItemCLickListener) {
		this.mOnResultListItemCLickListener = mOnResultListItemCLickListener;
	}

	public interface onResultListItemCLickListener {
		void scrollToClickItem(Object clickObject);
	}

	public void clearSearchEditText() {
		mSearchEditText.clearFocus();
		mSearchEditText.setText("");
	}

	@Override
	public void dismiss() {
		HideKeyBoardAndClearFocus();
		super.dismiss();
		clearSearchEditText();
	}

	@Override
	public void onBackPressed() {
		mOnCancelSearchAnimationListener.cancelSearchContactAnimation();
		super.onBackPressed();
	}
	
	public class ChatRecordAdapter extends BaseAdapter {
		
		private Context nContext;
		private List<Object> nChatMsgEntities;

		public ChatRecordAdapter(Context context,List<Object> tempMsgEntities) {
			nContext = context;
			nChatMsgEntities = tempMsgEntities;
		}

		public void setnChatMsgEntities(List<Object> nChatMsgEntities) {
			this.nChatMsgEntities = nChatMsgEntities;
		}

		@Override
        public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder mViewHolder = null;
			String mHeadUrl = null;
			String mHeadLocalPath = null;
			String mChatName = null;
			String mChatDate = null;
			String mChatContent = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(R.layout.chat_record_find_result, null);
				mViewHolder = new ViewHolder();
				mViewHolder.mFindChatRecordUserIconIV= (ImageView) convertView.findViewById(R.id.iv_find_chat_record_user_icon);
				mViewHolder.mFindChatRecordUserNameTV = (TextView) convertView.findViewById(R.id.tv_find_chat_record_user_name);
				mViewHolder.mFindChatRecordMsgDateTV = (TextView) convertView.findViewById(R.id.tv_find_chat_record_msg_date);
				mViewHolder.mFindChatRecordMsgContentTV = (TextView) convertView.findViewById(R.id.tv_find_chat_record_msg_content);
				convertView.setTag(mViewHolder);
			} else {
				mViewHolder = (ViewHolder) convertView.getTag();
			}
			Object mChatMsgItem = nChatMsgEntities.get(position);
			int avatar=-1;
			if (mChatMsgItem instanceof P2PChatMsgEntity) {
				P2PChatMsgEntity mP2PChatMsgEntity = (P2PChatMsgEntity) mChatMsgItem;
				User mUser = mContactOrgDao.queryUserInfoByUserNo(mP2PChatMsgEntity.getChatUserNo());
				if (mUser.getGender() == 1) {//是男性
					avatar = R.mipmap.session_p2p_men;
				} else if (mUser.getGender() == 2) {//是女性
					avatar = R.mipmap.session_p2p_woman;
				} else {
					avatar = R.mipmap.session_no_sex;
				}
				if (mP2PChatMsgEntity.getIsSend() == 0) {
					mChatName = "我:";
				} else {
					mChatName = mUser.getUserName();
				}
				mChatDate = mP2PChatMsgEntity.getTime();
				mChatContent = mP2PChatMsgEntity.getMessage();
			} else {
				GroupChatMsgEntity mGroupChatMsgEntity = (GroupChatMsgEntity) mChatMsgItem;
				User mUser = mContactOrgDao.queryUserInfoByUserNo(mGroupChatMsgEntity.getChatUserNo());
				if (mUser!=null) {
					if (mUser.getGender() == 1) {//是男性
						avatar = R.mipmap.session_p2p_men;
					} else if (mUser.getGender() == 2) {//是女性
						avatar = R.mipmap.session_p2p_woman;
					} else {
						avatar = R.mipmap.session_no_sex;
					}
				}
				if (mGroupChatMsgEntity.getIsSend() == 0) {
					mChatName = "我:";
				} else {
					if (mUser != null) {
						mChatName = mUser.getUserName();
					} else {
						mChatName = mGroupChatMsgEntity.getSenderName();
					}
				}
				mChatDate = mGroupChatMsgEntity.getTime();
				mChatContent = mGroupChatMsgEntity.getMessage();
			}
			mViewHolder.mFindChatRecordUserNameTV.setText(mChatName != null ? mChatName:"");
			mViewHolder.mFindChatRecordUserIconIV.setImageResource(avatar);
			Date chatRecordMsgDate = new Date(Long.valueOf(mChatDate));
			mViewHolder.mFindChatRecordMsgDateTV.setText(TimeUtil.getTimeRelationFromNow2(mContext,chatRecordMsgDate));
			if (mChatContent != null) { 
				MessageBodyEntity body = JSON.parseObject(mChatContent,
						MessageBodyEntity.class);
				SpannableString spannableString = FaceConversionUtil.getInstace()
						.getExpressionString(nContext, body.getContent());
				// 对内容做处理
//				SpannableString spannableString = FaceConversionUtil
//						.getInstace().handlerContent(nContext,mViewHolder.mFindChatRecordMsgContentTV,
//						body.getContent());
				mViewHolder.mFindChatRecordMsgContentTV.setText(spannableString);
			}
			
			return convertView;
		}

		@Override
		public int getCount() {
			if (nChatMsgEntities == null) {
				return 0;
			}
			return nChatMsgEntities.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (nChatMsgEntities == null) {
				return 0;
			}
			return nChatMsgEntities.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			//  Auto-generated method stub
			return 0;
		}
		
	}
	
	class ViewHolder {
        public ImageView mFindChatRecordUserIconIV;
		public TextView mFindChatRecordUserNameTV;
		public TextView mFindChatRecordMsgDateTV;
		public TextView mFindChatRecordMsgContentTV;
	}

}
