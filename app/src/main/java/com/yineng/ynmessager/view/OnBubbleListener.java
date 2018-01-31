package com.yineng.ynmessager.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;


//实现气泡管理器类（WindowManager类）
public class OnBubbleListener implements OnTouchListener {

	private Context context;
	private View pointView;
	private BubbleView bubbleView;
	private WindowManager windowManager;
	private LayoutParams params;

	public OnBubbleListener(Context context, View pointView) {
		super();
		this.context = context;
		this.pointView = pointView;
		this.bubbleView = new BubbleView(context);
		initWindowManager();
	}

	private void initWindowManager() {
		windowManager = (WindowManager) this.context
				.getSystemService(Context.WINDOW_SERVICE);
		params = new LayoutParams();
		params.format = PixelFormat.TRANSLUCENT;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int actionMasked = MotionEventCompat.getActionMasked(event);
		switch (actionMasked) {
		case MotionEvent.ACTION_DOWN:
			ViewParent parent = v.getParent();
			parent.requestDisallowInterceptTouchEvent(true);

			// 将我们的默认的红点隐藏
			this.pointView.setVisibility(View.INVISIBLE);

			this.bubbleView.setBubbleText("20");
			this.bubbleView.setCenterPoint(event.getRawX(), event.getRawY());

			// 绑定气泡
			this.windowManager.addView(bubbleView, params);
			break;
		}
		this.bubbleView.onTouchEvent(event);
		return true;
	}

}
