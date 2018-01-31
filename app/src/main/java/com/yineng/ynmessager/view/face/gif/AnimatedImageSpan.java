package com.yineng.ynmessager.view.face.gif;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.style.DynamicDrawableSpan;

public class AnimatedImageSpan extends DynamicDrawableSpan {

    private Drawable mDrawable;
	private Handler mHandler;
	private Runnable mRunnable;
	private boolean isBitmapRun = true;
    public AnimatedImageSpan(Drawable d) {
        super();
        mDrawable = d;
        // Use handler for 'ticks' to proceed to next frame 
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
//            	if (isBitmapRun) {
//                    ((AnimatedGifDrawable)mDrawable).nextFrame();
//                    mHandler.postDelayed(this, ((AnimatedGifDrawable)mDrawable).getFrameDuration());
////                    L.e("Runnable Runnable");
//				}
            	 ((AnimatedGifDrawable)mDrawable).nextFrame();
                 // Set next with a delay depending on the duration for this frame 
                 mHandler.postDelayed(this, ((AnimatedGifDrawable)mDrawable).getFrameDuration());
            }
        };
        mHandler.post(mRunnable);
    }

    /*
     * Return current frame from animated drawable. Also acts as replacement for super.getCachedDrawable(),
     * since we can't cache the 'image' of an animated image.
     */
    @Override
    public Drawable getDrawable() {
        return ((AnimatedGifDrawable)mDrawable).getDrawable();
    }

    /*
     * Copy-paste of super.getSize(...) but use getDrawable() to get the image/frame to calculate the size,
     * in stead of the cached drawable.
     */
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();

        if (fm != null) {
            fm.ascent = -rect.bottom; 
            fm.descent = 0; 

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right;
    }

    /*
     * Copy-paste of super.draw(...) but use getDrawable() to get the image/frame to draw, in stead of
     * the cached drawable.
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
    	try {
            Drawable b = getDrawable();
            canvas.save();

            int transY = bottom - b.getBounds().bottom;
            if (mVerticalAlignment == ALIGN_BASELINE) {
                transY -= paint.getFontMetricsInt().descent;
            }

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
		} catch (Exception e) {
			// : handle exception
			System.out.println("trying to use a recycled bitmap"); 
		}
    }
    
	public void recycleBitmaps(boolean recycleBmp) {
		mHandler.removeCallbacksAndMessages(null);
		if (recycleBmp) {
			((AnimatedGifDrawable)mDrawable).recycleBitmaps();
//			/**
//			 * 删除变量的引用地址
//			 */
//			mDrawable = null;
//			mRunnable = null;
//			mHandler = null;
		}
	}

	/**
	 * 让gif动起来
	 */
	public void runGifImg(){
		if (!isBitmapRun) {
			isBitmapRun = true;
			mHandler.post(mRunnable);
		}
	}
	
	/**
	 * 让gif暂停，防止后台线程一直执行
	 */
	public void pauseGifImg(){
		isBitmapRun = false;
	}
}
