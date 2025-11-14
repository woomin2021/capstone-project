package com.example.jjb20;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.dto.HouseReviewDTO;

import java.util.ArrayList;

public class HostReviewActivity extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_review);

        RecyclerView recyclerView = findViewById(R.id.review_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<HouseReviewDTO> reviewList = new ArrayList<>();

        reviewList.add(new HouseReviewDTO(
                1, 10, 3, 5, 5,
                "정말 만족스러웠습니다. 다시 오고 싶습니다.",
                "오늘"
        ));

        reviewList.add(new HouseReviewDTO(
                2, 11, 7, 5, 4,
                "숙소 위치가 좋고 호스트가 친절했습니다.",
                "2주 전"
        ));

        reviewList.add(new HouseReviewDTO(
                3, 15, 12, 5, 3,
                "전반적으로 무난했습니다.",
                "1년 전"
        ));

        ReviewAdapter adapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(adapter);

    }
}
