package com.example.jjb20;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.entity.HotelItem;

import java.util.ArrayList;
import java.util.List;

public class HouseReserveActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommended;
    private RecyclerView recyclerHot;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_reserve);

        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        recyclerHot = findViewById(R.id.recyclerHot);

        // 추천: 가로 스크롤
        LinearLayoutManager horizontalManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerRecommended.setLayoutManager(horizontalManager);
        recyclerRecommended.setAdapter(new RecommendedAdapter(getRecommendedData()));

        // HOT: 2열 Grid
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerHot.setLayoutManager(gridLayoutManager);
        recyclerHot.setAdapter(new HotAdapter(getHotData()));
    }

    private List<HotelItem> getRecommendedData() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem("마운틴뷰",
                "2025-06-05 ~ 2025-07-31",
                "제주 노형동",
                "30,000원 · 1박",
                R.drawable.sample1));
        list.add(new HotelItem("부산의 아침",
                "2025-06-05 ~ 2025-07-31",
                "부산 해운대",
                "60,000원 · 1박",
                R.drawable.sample2));
        // 필요 시 더 추가
        return list;
    }

    private List<HotelItem> getHotData() {
        List<HotelItem> list = new ArrayList<>();
        list.add(new HotelItem("인천 주택", "", "인천 연수구 송도동", "30,000원", R.drawable.sample_room));
        list.add(new HotelItem("강남 펜트하우스", "", "서울 강남 논현동", "300,000원", R.drawable.sample_pool));
        list.add(new HotelItem("인천 주택", "", "인천 연수구 송도동", "30,000원", R.drawable.sample_room));
        list.add(new HotelItem("강남 펜트하우스", "", "서울 강남 논현동", "300,000원", R.drawable.sample_pool));
        return list;
    }
}
