package com.alzzz.detail;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description RoomTypeAdapter
 * @Date 2019-07-12
 * @Author sz
 */
public class RoomTypeAdapter extends RecyclerView.Adapter<RoomTypeAdapter.ViewHolder> {
    private List<RoomType> mRoomTypeList;
    private Context mContext;

    public RoomTypeAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setRoomTypeList(List<RoomType> mRoomTypeList) {
        this.mRoomTypeList = mRoomTypeList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomTypeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_room_type, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RoomTypeAdapter.ViewHolder viewHolder, int position) {
        if (position == 0){
            viewHolder.itemView.setTransitionName("sharedView");
        }
        viewHolder.bindView(mRoomTypeList.get(position));
    }

    @Override
    public int getItemCount() {
        if (mRoomTypeList == null){
            return 0;
        }
        return mRoomTypeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView coverImg;
        public TextView roomTypeName;
        private Context mContext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImg = itemView.findViewById(R.id.iv_cover);
            roomTypeName = itemView.findViewById(R.id.tv_room_type_name);
            this.mContext = itemView.getContext();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void bindView(RoomType roomType){
            if (roomType.getCoverRes()>0){
                coverImg.setImageResource(roomType.getCoverRes());
            } else {
                coverImg.setImageResource(R.drawable.demo1);
            }
            roomTypeName.setText(roomType.getHouseTypeName());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTransitionName("sharedView");
                    Intent intent = new Intent(mContext, SecondActivity.class);
                    Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, v, "sharedView").toBundle();
                    mContext.startActivity(intent, bundle);
                }
            });
        }
    }
}
