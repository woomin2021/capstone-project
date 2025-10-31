package com.example.jjb20;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.chat.ChatRoomFragment;
import com.example.jjb20.entity.HotelItem;

import java.util.ArrayList;
import java.util.List;

public class RentHouseActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommended;
    private RecyclerView recyclerHot;
    private Button chattingButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house);

        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        recyclerHot = findViewById(R.id.recyclerHot);

        //채팅 구현
        chattingButton = findViewById(R.id.chattingButton);

        chattingButton.setOnClickListener(v -> {
            ChatRoomFragment fragment = new ChatRoomFragment();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //R.id.main을 fragment로 교체하겠다.
            transaction.replace(R.id.main, fragment);
            //뒤로가기 버튼을 눌렀을 때 이전 fragment로 돌아가기
            transaction.addToBackStack(null);
            //실행
            transaction.commit();
        });


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
