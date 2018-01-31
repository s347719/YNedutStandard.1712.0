package com.yineng.ynmessager.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;

/**
 * Created by 贺毅柳 on 2015/11/12 10:18.<br>
 * 使原有的 {@link RecyclerView} 支持 EmptyView
 */
public class EmptySupportRecyclerView extends RecyclerView
{

    private View emptyView;
    private TextView empty_image,empty_text,empty_try;
    private OnClickListener onClickListener;
    private String hint="";//内容为空的时候提示词
    final private AdapterDataObserver observer = new AdapterDataObserver()
    {
        @Override
        public void onChanged()
        {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount)
        {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount)
        {
            checkIfEmpty();
        }
    };

    public EmptySupportRecyclerView(Context context)
    {
        super(context);
    }

    public EmptySupportRecyclerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public EmptySupportRecyclerView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    void checkIfEmpty()
    {
        if (emptyView != null && getAdapter() != null)
        {
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
            if (emptyViewVisible) {
                if (AppController.NET_IS_USEFUL) {
                    empty_image.setBackgroundResource(R.mipmap.no_data);
                    empty_text.setText(hint);
                    empty_try.setVisibility(GONE);
                } else {
                    empty_image.setBackgroundResource(R.mipmap.load_fail);
                    empty_text.setText(R.string.main_eventNoticeEmptyNetDown);
                    empty_try.setVisibility(VISIBLE);
                }
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter)
    {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null)
        {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null)
        {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyTextHint(String hint){
        this.hint = hint;
    }
    public void setEmptyView(View emptyView)
    {
        this.emptyView = emptyView;
        empty_image  = (TextView) emptyView.findViewById(R.id.empty_image);
        empty_text  = (TextView) emptyView.findViewById(R.id.empty_text);
        empty_try  = (TextView) emptyView.findViewById(R.id.empty_try);
        empty_try.setOnClickListener(onClickListener);
        checkIfEmpty();
    }
    public void setEmptyOnClickListener(OnClickListener clickListener){
        this.onClickListener = clickListener;
    }
}
