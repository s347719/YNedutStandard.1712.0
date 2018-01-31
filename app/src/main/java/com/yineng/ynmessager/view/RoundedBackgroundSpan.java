package com.yineng.ynmessager.view;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;

/**
 * Created by 贺毅柳 on 2015/11/13 14:47.
 */
public class RoundedBackgroundSpan extends ReplacementSpan
{
    private final int mPadding = 10;
    private int mBackgroundColor;
    private int mTextColor;

    public RoundedBackgroundSpan(int backgroundColor, int textColor)
    {
        super();
        mBackgroundColor = backgroundColor;
        mTextColor = textColor;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
    {
        return (int) (mPadding + paint.measureText(text.subSequence(start, end).toString()) + mPadding);
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {
        float width = paint.measureText(text.subSequence(start, end).toString());
        RectF rect = new RectF(x, top + mPadding, x + width + 2 * mPadding, bottom);
        paint.setColor(mBackgroundColor);
        canvas.drawRoundRect(rect, mPadding, mPadding, paint);
        paint.setColor(mTextColor);
        canvas.drawText(text, start, end, x + mPadding, y, paint);
    }
}
