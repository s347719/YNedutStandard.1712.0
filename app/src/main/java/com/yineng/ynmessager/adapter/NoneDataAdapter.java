package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yineng.ynmessager.R;

/**
 * Created by 舒欢
 * Created time: 2017/7/7
 */

public class NoneDataAdapter extends BaseAdapter{

    private Context mContext;

    public NoneDataAdapter(Context mContext)
    {
        this.mContext = mContext;
    }

    @Override
    public int getCount()
    {
        return 1;
    }

    @Override
    public Object getItem(int arg0)
    {
        return null;
    }

    @Override
    public long getItemId(int arg0)
    {
        return arg0;
    }

    @Override
    public View getView(int position, View conView, ViewGroup arg2)
    {
        conView = LayoutInflater.from(mContext).inflate(R.layout.nodata_layout, null);
        return conView;
    }


    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
