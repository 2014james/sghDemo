package com.android.sgh.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.android.sgh.R;


/**
 * 时间刻度显示界面
 *
 * @author howie
 */
public class TimeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "TimeAdapter";

    public TimeAdapter() {
    }

    /**
     * 第一种布局
     *
     * @author afnasdf
     */
    public final static class ItemViewHolder extends RecyclerView.ViewHolder {
        public View lineLeft;
        public View lineRight;
        public TextView time;

        public ItemViewHolder(View itemView) {
            super(itemView);
            lineLeft = (View) itemView.findViewById(R.id.line_left);
            lineRight = (View) itemView.findViewById(R.id.line_right);
            time = (TextView) itemView.findViewById(R.id.tv_time_recorder);
        }
    }


    @Override
    public int getItemCount() {
        return 1 * 60 * 60 * 5;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        String timeStr = "";
        int min = (position - 1) / 60;
        int sec = (position - 1) % 60;
        if (min < 10) {
            timeStr = "0" + min;
        } else {
            timeStr = min + "";
        }
        if (sec < 10) {
            timeStr = timeStr + ":0" + sec;
        } else {
            timeStr = timeStr + ":" + sec;
        }
        itemViewHolder.time.setText(timeStr);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_time_counter, viewGroup, false);
        return new ItemViewHolder(view);
    }


    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1;
    }


}
