package com.yineng.ynmessager.view.tipView;

import android.graphics.Color;

/**
 * Created by 舒欢
 * Created time: 2017/5/4
 */

public class TipViewItem {

    private String title;

    private int textColor = Color.WHITE;

    public TipViewItem(String title) {
        this.title = title;
    }

    public TipViewItem(String title, int textColor) {
        this.title = title;

        this.textColor = textColor;
    }

    public String getTitle() {
        return title;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
