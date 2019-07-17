package com.alzzz.detail;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.alzzz.scalemanagerlib.CardScaleLayoutManager;
import com.alzzz.scalemanagerlib.HouseBannerSnapHelper;
import com.alzzz.scalemanagerlib.HouseScaleLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description SecondActivity
 * @Date 2019-07-17
 * @Author sz
 */
public class SecondActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;
    RoomTypeAdapter roomTypeAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mRecyclerView = findViewById(R.id.rv_main);
        setupRecyclerView();
        resetRecyclerContent(10);
    }

    private void setupRecyclerView() {
        HouseScaleLayoutManager layoutManager = new HouseScaleLayoutManager
                .Builder(this, 0)
                .setMinScale(1)
                .build();
        layoutManager.setOrientation(CardScaleLayoutManager.HORIZONTAL);
        layoutManager.setInfinite(false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addItemDecoration(new RoomTypeItemDecoration(10));

        HouseBannerSnapHelper pagerSnapHelper = new HouseBannerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(mRecyclerView);

        roomTypeAdapter = new RoomTypeAdapter(this);
        mRecyclerView.setAdapter(roomTypeAdapter);
    }

    private void resetRecyclerContent(int contentSize) {
        List<RoomType> roomTypes = new ArrayList<>();

        for (int i=0; i<contentSize; i++){
            RoomType roomType = new RoomType();
            roomType.setCoverUrl("");
            roomType.setCoverRes(R.drawable.demo2);
            roomType.setHouseTypeName("ROOM TYPE "+i);
            roomTypes.add(roomType);
        }
        roomTypeAdapter.setRoomTypeList(roomTypes);
    }
}
