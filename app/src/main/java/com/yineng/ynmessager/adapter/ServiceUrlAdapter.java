package com.yineng.ynmessager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;

import java.util.List;

/**
 * Created by yn on 2017/4/19.
 */
public class ServiceUrlAdapter extends BaseAdapter {

    private Context context;
    private List<String> data;
    private ViewHolder holder;
    private OnClickListener clicklistener;

    public ServiceUrlAdapter(Context context,  OnClickListener clicklistener) {
        this.context = context;
        this.clicklistener=clicklistener;
    }

    public  void setData(List<String> data){
        this.data = data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view==null){
            view= LayoutInflater.from(context).inflate(R.layout.item_login_seclet_account,null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        holder.item_username.setText(data.get(position));
        holder.item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicklistener.OnClickListener(data.get(position));
            }
        });
        return view;
    }

    class ViewHolder {
        LinearLayout item_bg;
        TextView item_username;
        ImageView item_delete;

        public ViewHolder(View view) {
            item_bg = (LinearLayout) view.findViewById(R.id.item_bg);
            item_username = (TextView) view.findViewById(R.id.item_user_name);
            item_delete = (ImageView) view.findViewById(R.id.item_delete_history);
        }
    }

    public interface OnClickListener {
        void OnClickListener(String url);
    }
}
