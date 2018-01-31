package com.yineng.ynmessager.view.face;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.p2psession.ViewPagerAdapter;
import com.yineng.ynmessager.bean.ChatEmoji;
import com.yineng.ynmessager.view.ViewPagerCompat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 自定义表情选择组件
 */
public class FaceRelativeLayout extends RelativeLayout implements
		OnItemClickListener, OnClickListener, TextWatcher{

	private Context mContext;

	/** 表情页的监听事件 */
	private OnCorpusSelectedListener mListener;

	/** 显示表情页的viewpager */
	private ViewPager mFaceVP;

	/** 表情页界面集合 */
	private ArrayList<View> mPageViewList;

	/** 游标显示布局 */
	private LinearLayout mLayoutPoint;

	/** 游标点集合 */
	private ArrayList<ImageView> mPointViewList;

	/** 表情集合 */
	private List<List<ChatEmoji>> mEmojiLists;

	/** 表情区域 */
	private View mView;

	/** 输入框 */
	private EditText mSendMessageET;

	/** 表情数据填充器 */
	private List<FaceAdapter> mFaceAdapters;

	/** 当前表情页 */
	private int current = 0;
	
	/**
	 * 发送图片布局
	 */
	private RelativeLayout mUtilLayout;
	
	private GridView mUtilGridLayout;
	
	private OtherButtonsClickListener mOtherButtonsClickListener;
	private ImageView mVoiceEntryIV;
	private Button mSendTextMsgBT;

	private RelativeLayout mVoiceLayout;
	private ViewPagerCompat mVoicePager;
	private PageIndicator mIndicator;
	public Fragment[] mVoiceContent;
	protected final String[] mTabTitle = new String[] { "对讲", "录音"};
	private VoicePagerAdapter mVoicePagerAdapter;

	public FaceRelativeLayout(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	public FaceRelativeLayout(Context mContext, AttributeSet attrs) {
		super(mContext, attrs);
		this.mContext = mContext;
	}

	public FaceRelativeLayout(Context mContext, AttributeSet attrs, int defStyle) {
		super(mContext, attrs, defStyle);
		this.mContext = mContext;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	public void setOtherButtonsClickListener(OtherButtonsClickListener otherButtonsClickListener)
	{
		mOtherButtonsClickListener = otherButtonsClickListener;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() > 0) {
			mVoiceEntryIV.setVisibility(GONE);
			mSendTextMsgBT.setVisibility(VISIBLE);
		} else {
			mVoiceEntryIV.setVisibility(VISIBLE);
			mSendTextMsgBT.setVisibility(INVISIBLE);
		}
	}

	/**
	 * 表情选择监听
	 * 
	 * @时间： 2013-1-15下午04:32:54
	 */
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	/**
	 * {@link FaceRelativeLayout} 中其他辅助功能按钮的回调接口
	 */
	public interface OtherButtonsClickListener
	{
		/**
		 * 发送图片按钮点击事件
		 */
		void onSendImageClick();

		/**
		 * 发送文件按钮点击事件
		 */
		void onSendFileClick();

		/**
		 * 拍照发送按钮点击事件
		 */
		void onSendCameraImageClick();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEmojiLists = FaceConversionUtil.getInstace().mEmojiLists;
		onCreate();
	}

	private void onCreate() {
		initView();
		initViewPager();
		initPicSelector();
		initVoicePager();
		initPoint();
		initData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_face:
			hideSoftInputView();//隐藏软键盘
			//隐藏图片选择框
			if (mUtilLayout.isShown()) {
				mUtilLayout.setVisibility(View.GONE);
			}
			//隐藏语音选择框
			if (mVoiceLayout.isShown()) {
				mVoiceLayout.setVisibility(View.GONE);
			}
			// 隐藏表情选择框
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
			} else {
				mView.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.et_sendmessage:
			// 隐藏表情选择框
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
			}
			//隐藏图片选择框
			if (mUtilLayout.isShown()) {
				mUtilLayout.setVisibility(View.GONE);
			}
			//隐藏语音选择框
			if (mVoiceLayout.isShown()) {
				mVoiceLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.btn_select:
			hideSoftInputView();//隐藏软键盘
			// 隐藏表情选择框
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
			}
			//隐藏语音选择框
			if (mVoiceLayout.isShown()) {
				mVoiceLayout.setVisibility(View.GONE);
			}
			//隐藏图片选择框
			if (mUtilLayout.isShown()) {
				mUtilLayout.setVisibility(View.GONE);
			} else {
				mUtilLayout.setVisibility(View.VISIBLE);
			}
			break;
			case R.id.iv_send_voice:
				hideSoftInputView();//隐藏软键盘
				// 隐藏表情选择框
				if (mView.isShown()) {
                    mView.setVisibility(View.GONE);
                }
				//隐藏图片选择框
				if (mUtilLayout.isShown()) {
                    mUtilLayout.setVisibility(View.GONE);
                }
				//隐藏语音选择框
				if (mVoiceLayout.isShown()) {
					mVoiceLayout.setVisibility(View.GONE);
				} else {
					mVoiceLayout.setVisibility(View.VISIBLE);
				}
				break;
		}
	}

	/**
	 * 隐藏表情选择框
	 */
	public boolean hideFaceView() {
		hideSoftInputView();//隐藏软键盘
		// 隐藏表情选择框图片选择框、图片选择框
		if (mView.getVisibility() == View.VISIBLE
				|| mUtilLayout.isShown() || mVoiceLayout.isShown()) {
			mView.setVisibility(View.GONE);
			mUtilLayout.setVisibility(View.GONE);
			mVoiceLayout.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		mFaceVP = (ViewPager) findViewById(R.id.vp_contains);
		mSendMessageET = (EditText) findViewById(R.id.et_sendmessage);
		mLayoutPoint = (LinearLayout) findViewById(R.id.iv_image);
		mSendMessageET.setOnClickListener(this);
		mSendMessageET.addTextChangedListener(this);
		findViewById(R.id.btn_face).setOnClickListener(this);
		findViewById(R.id.btn_select).setOnClickListener(this);
		mView = findViewById(R.id.ll_facechoose);
		mUtilLayout = (RelativeLayout) findViewById(R.id.ll_utilchoose);
		mUtilGridLayout = (GridView) findViewById(R.id.gv_choose_grid_layout);
		mVoiceEntryIV = (ImageView) findViewById(R.id.iv_send_voice);
		mSendTextMsgBT = (Button) findViewById(R.id.btn_send);
		mVoiceEntryIV.setOnClickListener(this);
	}

	/**
	 * 初始化显示表情的viewpager
	 */
	private void initViewPager() {
		mPageViewList = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(mContext);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		mPageViewList.add(nullView1);

		// 中间添加表情页

		mFaceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < mEmojiLists.size(); i++) {
			GridView view = new GridView(mContext);
			FaceAdapter adapter = new FaceAdapter(mContext, mEmojiLists.get(i));
			view.setAdapter(adapter);
			mFaceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			mPageViewList.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(mContext);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		mPageViewList.add(nullView2);
	}

	/**
	 * 初始化图片选择器按钮
	 */
	private void initPicSelector() {
		final int[] otherButtonsImage = new int[] {R.mipmap.send_image_file,
                R.mipmap.send_file,
                R.mipmap.send_camera};
		final String[] otherButtonsTitle = mContext.getResources().getStringArray(R.array.chatFooter_otherButtons);
		ArrayList<HashMap<String, Object>> otherButtonsData = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < otherButtonsImage.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", otherButtonsImage[i]);
			map.put("itemText", otherButtonsTitle[i]);
			map.put("tag", i);
			otherButtonsData.add(map);
		}
		// 群讨论组不需要发送图片
		if(mContext instanceof GroupChatActivity) {
            otherButtonsData.remove(1);
        }
		
		SimpleAdapter mOtherButtonsAdapter = new SimpleAdapter(mContext, otherButtonsData,// 数据源
				R.layout.chat_bottom_grid_item,// 显示布局
				new String[] { "itemImage", "itemText" }, new int[] {
						R.id.select_Image_item, R.id.select_text_item });
		
		mUtilGridLayout.setAdapter(mOtherButtonsAdapter);
		mUtilGridLayout.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				if(mOtherButtonsClickListener != null)
				{
					int tag = (Integer)((HashMap<String, Object>)arg0.getItemAtPosition(arg2)).get("tag");
					switch(tag)
					{
						case 0:
							mOtherButtonsClickListener.onSendImageClick();
							break;
						case 1:
							mOtherButtonsClickListener.onSendFileClick();
							break;
						case 2:
							mOtherButtonsClickListener.onSendCameraImageClick();
							break;
					}
				}
			}
		});
	}
	ChatTalkFragment chatTalkFragment = new ChatTalkFragment();
	ChatRecordFragment chatRecordFragment = new ChatRecordFragment();
	private void initVoicePager() {
		mVoiceLayout = (RelativeLayout) findViewById(R.id.rl_voice_record_layout);
		mVoiceContent = new Fragment[]{chatTalkFragment,chatRecordFragment};
		chatTalkFragment.setViewPagerScrollListener(new BaseVoiceFragment.viewPagerScrollable() {
			@Override
			public void onViewPagerScroll(boolean scrollable) {
				mVoicePager.setScrollable(scrollable);
			}
		});
		chatRecordFragment.setViewPagerScrollListener(new BaseVoiceFragment.viewPagerScrollable() {
			@Override
			public void onViewPagerScroll(boolean scrollable) {
				mVoicePager.setScrollable(scrollable);
			}
		});
		mVoicePagerAdapter = new VoicePagerAdapter(((BaseActivity)mContext).getSupportFragmentManager());
		mVoicePager = (ViewPagerCompat)findViewById(R.id.vp_voice_record);
		mVoicePager.setAdapter(mVoicePagerAdapter);
		mIndicator = (CirclePageIndicator)findViewById(R.id.voice_indicator);
		mIndicator.setViewPager(mVoicePager);
	}

	/**
	 * 是否正在录音
	 * @return
	 */
	public boolean isVoiceFragmentRecording() {
		if (mVoicePager == null || mVoicePagerAdapter == null) {
            return false;
        }
		int curPos = mVoicePager.getCurrentItem();
		BaseVoiceFragment selectedVoiceFragment = (BaseVoiceFragment) mVoicePagerAdapter.getItem(curPos);
		if (selectedVoiceFragment.mVoiceRecorder == null) {
            return false;
        }
		return selectedVoiceFragment.mVoiceRecorder.isStart();
	}

	/**
	 * 初始化游标
	 */
	private void initPoint() {
		mPointViewList = new ArrayList<ImageView>();
		ImageView imageView;
		Resources res = getResources();
		float size = res.getDimension(R.dimen.chatFooter_popupBar_indicator_size);
		float spacing = res.getDimension(R.dimen.chatFooter_popupBar_indicator_horSpacing);
		for (int i = 0; i < mPageViewList.size(); i++) {
			imageView = new ImageView(mContext);
			imageView.setBackgroundResource(R.mipmap.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = (int)spacing;
			layoutParams.rightMargin = (int)spacing;
			layoutParams.width = (int)size;
			layoutParams.height = (int)size;
			mLayoutPoint.addView(imageView, layoutParams);
			if (i == 0 || i == mPageViewList.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.mipmap.d2);
			}
			mPointViewList.add(imageView);
		}
	}

	/**
	 * 填充数据
	 */
	private void initData() {
		mFaceVP.setAdapter(new ViewPagerAdapter(mPageViewList));

		mFaceVP.setCurrentItem(1);
		current = 0;
		mFaceVP.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				mDrawPoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == mPointViewList.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mFaceVP.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
						mPointViewList.get(1).setBackgroundResource(R.mipmap.d2);
					} else {
						mFaceVP.setCurrentItem(arg0 - 1);// 倒数第二屏
						mPointViewList.get(arg0 - 1).setBackgroundResource(
								R.mipmap.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	/**
	 * 绘制游标背景
	 */
	public void mDrawPoint(int index) {
		for (int i = 1; i < mPointViewList.size(); i++) {
			if (index == i) {
				mPointViewList.get(i).setBackgroundResource(R.mipmap.d2);
			} else {
				mPointViewList.get(i).setBackgroundResource(R.mipmap.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) mFaceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon) {
			int selection = mSendMessageET.getSelectionStart();
			String text = mSendMessageET.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					mSendMessageET.getText().delete(start, end);
					return;
				}
				mSendMessageET.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null) {
                mListener.onCorpusSelected(emoji);
            }
			SpannableString spannableString = FaceConversionUtil.getInstace()
					.addFace(getContext(), emoji.getId(), emoji.getCharacter());
			mSendMessageET.append(spannableString);
		}

	}
	
	//隐藏键盘
	public void hideSoftInputView() {
		InputMethodManager manager = ((InputMethodManager) mContext
				.getSystemService(Activity.INPUT_METHOD_SERVICE));
		if (((Activity) mContext).getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
			if (((Activity) mContext).getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(((Activity) mContext)
                                .getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
		}
	}


	private class VoicePagerAdapter extends FragmentPagerAdapter
	{
		VoicePagerAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0)
		{
			return mVoiceContent[arg0];
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return mTabTitle[position];
		}

		@Override
		public int getCount()
		{
			return mVoiceContent.length;
		}
	}

}
