//***************************************************************
//*    2015-8-7  上午9:22:57
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.view;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author 胡毅
 *
 */
public class ClickableMovementMethod extends LinkMovementMethod {

	private static ClickableMovementMethod sInstance;
	//解决listview中的长按点击事件
	private long lastClickTime;

	private static final long CLICK_DELAY = 500L;

	public static ClickableMovementMethod getInstance() {
		if (sInstance == null) {
			sInstance = new ClickableMovementMethod();
		}
		return sInstance;
	}

	@Override
	public boolean onTouchEvent(TextView widget, Spannable buffer,
								MotionEvent event) {
		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
			int x = (int) event.getX();
			int y = (int) event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			ClickableSpan[] link = buffer.getSpans(off, off,
					ClickableSpan.class);
			ClickableImageSpan[] imageSpans = buffer.getSpans(off, off,
					ClickableImageSpan.class);

			if (link.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					if(System.currentTimeMillis() - lastClickTime < CLICK_DELAY){
						link[0].onClick(widget);
					}
				} else if (action == MotionEvent.ACTION_DOWN) {
					Selection.setSelection(buffer,
							buffer.getSpanStart(link[0]),
							buffer.getSpanEnd(link[0]));
					lastClickTime = System.currentTimeMillis();
				}

				return true;
			} else if (imageSpans.length != 0) {
				if (action == MotionEvent.ACTION_UP) {
					if(System.currentTimeMillis() - lastClickTime < CLICK_DELAY){
						int[] index = {buffer.getSpanStart(imageSpans[0]),buffer.getSpanEnd(imageSpans[0])};
						widget.setTag(index);
						imageSpans[0].onClick(widget);
					}
				} else if (action == MotionEvent.ACTION_DOWN) {
//					Selection.setSelection(buffer,
//							buffer.getSpanStart(imageSpans[0]),
//							buffer.getSpanEnd(imageSpans[0]));
//					imageSpans[0].onClick(widget);
					lastClickTime = System.currentTimeMillis();
				}

				return true;
			} else {
				Selection.removeSelection(buffer);
			}
		}

		return false;
	}

}
