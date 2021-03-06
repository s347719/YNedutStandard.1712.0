package com.yineng.ynmessager.view.face.gif;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.yineng.ynmessager.view.face.gif.GifDecoder.GifFrame;

import java.io.InputStream;

public class AnimatedGifDrawable extends AnimationDrawable {

	public static final String TAG = "AnimatedGifDrawable";
	private int mCurrentIndex = 0;
	private UpdateListener mListener;
	private GifDecoder decoder;

	public AnimatedGifDrawable(InputStream source, UpdateListener listener, Rect rect) {
		mListener = listener;
		decoder = new GifDecoder();
		decoder.read(source);
		// Iterate through the gif frames, add each as animation frame
		for (int i = 0; i < decoder.getFrameCount(); i++) {
			Bitmap bitmap = decoder.getFrame(i);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			// Explicitly set the bounds in order for the frames to display
			drawable.setBounds(rect.left, rect.top, rect.right, rect.bottom);
			addFrame(drawable, decoder.getDelay(i));
			if (i == 0) {
				// Also set the bounds for this container drawable
				setBounds(rect.left, rect.top, rect.right, rect.bottom);
			}
		}
	}

	/**
	 * Naive method to proceed to next frame. Also notifies listener.
	 */
	public void nextFrame() {
		mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
		if (mListener != null) {
            mListener.update();
        }
	}

	/**
	 * Return display duration for current frame
	 */
	public int getFrameDuration() {
		return getDuration(mCurrentIndex);
	}

	/**
	 * Return drawable for current frame
	 */
	public Drawable getDrawable() {
		return getFrame(mCurrentIndex);
	}
	
	/**
	 * Interface to notify listener to update/redraw Can't figure out how to
	 * invalidate the drawable (or span in which it sits) itself to force redraw
	 */
	public interface UpdateListener {
		void update();
	}

	public void recycleBitmaps() {
		if (decoder.frames != null) {
			for (GifFrame frame : decoder.frames) {
				if (!frame.image.isRecycled()) {
					frame.image.recycle();
				}
			}
//			decoder.frames.clear();
//			decoder.frames = null;
//			decoder = null;
		}
//		if (mListener != null) {
//			mListener = null;
//		}
	}
}