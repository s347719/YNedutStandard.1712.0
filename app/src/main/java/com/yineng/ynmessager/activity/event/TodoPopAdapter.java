package com.yineng.ynmessager.activity.event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.event.BatchTodoItem;

import java.util.List;

/**
 * @author by 舒欢
 * @Created time: 2017/12/21
 * @Descreption：
 */

public class TodoPopAdapter extends BaseAdapter {

    private List<BatchTodoItem> mList;
    Context mContext;
    private LayoutInflater inflater ;
    public TodoPopAdapter(Context context, List<BatchTodoItem> data) {
        this.mContext = context;
        this.mList = data;
        this.inflater = LayoutInflater.from(context);
    }

    public void setData(List<BatchTodoItem> data){
        this.mList = data;
    }
    @Override
    public int getCount() {
         return mList.size()>0 ? mList.size():0;
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        BatchTodoItem batchTodoItem = mList.get(position);
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(
                    R.layout.todo_pop_view, parent, false);
            viewHolder.todo_pop_text = (TextView) convertView.findViewById(R.id.todo_pop_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String num  = batchTodoItem.getTodonum()+"";
        viewHolder.todo_pop_text.setText(batchTodoItem.getName()+"("+num+")");
        return convertView;
    }
    private final class ViewHolder {
        TextView todo_pop_text;
    }
}
