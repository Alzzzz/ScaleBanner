package com.alzzz.detail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RoomTypeAdapter roomTypeAdapter;
    RoomTypeItemDecoration itemDecoration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv_main);

        setupRecyclerView();
        setupData();
    }

    private void setupRecyclerView() {
        ScaleLayoutManager layoutManager = new ScaleLayoutManager(this, 20);
        layoutManager.setOrientation(HouseBannerLayoutManager.HORIZONTAL);
        layoutManager.setInfinite(false);
        recyclerView.setLayoutManager(layoutManager);

//        itemDecoration = new RoomTypeItemDecoration(40);
//        recyclerView.addItemDecoration(itemDecoration);

//        CenterSnapHelper pagerSnapHelper = new CenterSnapHelper();
//        pagerSnapHelper.attachToRecyclerView(recyclerView);

        roomTypeAdapter = new RoomTypeAdapter(this);
        recyclerView.setAdapter(roomTypeAdapter);
    }

    private void setupData() {
        List<RoomType> roomTypes = new ArrayList<>();

        for (int i=0; i<10; i++){
            RoomType roomType = new RoomType();
            roomType.setCoverUrl("");
            roomType.setHouseTypeName("ROOM TYPE "+i);
            roomTypes.add(roomType);
        }
        roomTypeAdapter.setRoomTypeList(roomTypes);
    }
}
