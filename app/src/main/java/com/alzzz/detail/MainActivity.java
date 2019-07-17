package com.alzzz.detail;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.alzzz.scalemanagerlib.CardScaleLayoutManager;
import com.alzzz.scalemanagerlib.HouseBannerSnapHelper;
import com.alzzz.scalemanagerlib.HouseScaleLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RoomTypeAdapter roomTypeAdapter;
    RoomTypeItemDecoration itemDecoration;
    EditText numEleEt;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv_main);
        numEleEt = findViewById(R.id.et_num_element);
        submitBtn = findViewById(R.id.btn_submit);

        setupRecyclerView();
        setupData();
    }

    private void setupRecyclerView() {
        HouseScaleLayoutManager layoutManager = new HouseScaleLayoutManager
                .Builder(this, 0)
                .build();
        layoutManager.setOrientation(CardScaleLayoutManager.HORIZONTAL);
        layoutManager.setInfinite(false);
        recyclerView.setLayoutManager(layoutManager);

//        itemDecoration = new RoomTypeItemDecoration(40);
//        recyclerView.addItemDecoration(itemDecoration);

        HouseBannerSnapHelper pagerSnapHelper = new HouseBannerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(recyclerView);

        roomTypeAdapter = new RoomTypeAdapter(this);
        recyclerView.setAdapter(roomTypeAdapter);
    }

    private void setupData() {

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = numEleEt.getText().toString();
                int contentSize = 0;
                if (!TextUtils.isEmpty(content)){
                    contentSize = Integer.parseInt(content);
                }
                resetRecyclerContent(contentSize);
            }
        });
        resetRecyclerContent(10);
    }

    private void resetRecyclerContent(int contentSize) {
        List<RoomType> roomTypes = new ArrayList<>();

        for (int i=0; i<contentSize; i++){
            RoomType roomType = new RoomType();
            roomType.setCoverUrl("");
            roomType.setHouseTypeName("ROOM TYPE "+i);
            roomTypes.add(roomType);
        }
        roomTypeAdapter.setRoomTypeList(roomTypes);
    }
}
