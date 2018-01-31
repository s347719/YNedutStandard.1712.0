//***************************************************************
//*    2015-8-7  上午9:05:09
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * @author 胡毅
 * 
 */
public abstract class ClickableImageSpan extends ImageSpan {

	public ClickableImageSpan(Context context, Bitmap b, int verticalAlignment) {
		super(context, b, verticalAlignment);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Context context, Bitmap b) {
		super(context, b);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Context context, int resourceId,
			int verticalAlignment) {
		super(context, resourceId, verticalAlignment);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Context context, int resourceId) {
		super(context, resourceId);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Context context, Uri uri, int verticalAlignment) {
		super(context, uri, verticalAlignment);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Context context, Uri uri) {
		super(context, uri);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Drawable d, int verticalAlignment) {
		super(d, verticalAlignment);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Drawable d, String source, int verticalAlignment) {
		super(d, source, verticalAlignment);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Drawable d, String source) {
		super(d, source);
		//  Auto-generated constructor stub
	}

	public ClickableImageSpan(Drawable d) {
		super(d);
		//  Auto-generated constructor stub
	}

	public abstract void onClick(View view);
}
