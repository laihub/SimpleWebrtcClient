package com.closeli.demo.clbidirectionrenderdemo;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by piaovalentin on 2017/4/6.
 */

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    //懒的判断是否有DPAD
    private int mSelectedIndex = -2;

    public interface delegateCallback {
        void onClick(int position);
        String onDisplayView(int position);
        int itemCount();
    }

    private Context mContext;
    private int mLayoutId;
    private delegateCallback mListener;

    public UsersAdapter(Context context, int layoutID) {
        mContext = context;
        mLayoutId = layoutID;
    }

    public void setDelegate(delegateCallback listener) {
        mListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View layout = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
        return new CellViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        CellViewHolder cellHolder = (CellViewHolder) holder;

        String title = mListener.onDisplayView(position);
        cellHolder.title.setText(title);

        if (mSelectedIndex == position) {
            cellHolder.layout.setBackgroundColor(Color.LTGRAY);
        }
        else {
            cellHolder.layout.setBackgroundColor(Color.TRANSPARENT);
        }

        cellHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListener.itemCount();
    }

    static class CellViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cellTitle)
        TextView title;

        @BindView(R.id.container)
        ConstraintLayout layout;

        public CellViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public void notifySelectedIndexForDirection(int direction) {

        switch (direction) {

            case KeyEvent.KEYCODE_DPAD_CENTER:
                mListener.onClick(mSelectedIndex);
                break;

            case KeyEvent.KEYCODE_DPAD_UP:
                mSelectedIndex--;
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                mSelectedIndex++;
                break;

            default: return;
        }

        //只有dpad 可以激活 mSelectedIndex
        if (-2 == mSelectedIndex) {
            mSelectedIndex = -1;
        }

        updateRecycleView();
    }

    public void updateRecycleView() {
        updateSelectedIndex();
        notifyDataSetChanged();
//        notifyItemChanged(mSelectedIndex);
    }

    private void updateSelectedIndex() {
        if (mSelectedIndex == -1) {
            mSelectedIndex = 0;
        }
        else {
            int count = getItemCount();
            if (mSelectedIndex == count){
                mSelectedIndex = count - 1;
            }
        }
    }
}
