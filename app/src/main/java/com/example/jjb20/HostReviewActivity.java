package com.example.jjb20;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.adapter.ReviewAdapter;
import com.example.jjb20.chat.ChatMsgFragment;
import com.example.jjb20.dto.HouseReviewDTO;

import java.util.ArrayList;

public class HostReviewActivity extends AppCompatActivity {
    private AppCompatButton chatBtn;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_review);

        chatBtn = findViewById(R.id.chatBtn);

        chatBtn.setOnClickListener(v -> {
            openChatFragment();
        });




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

    private void openChatFragment() {
        // 컨테이너를 보이게
        findViewById(R.id.chat_container).setVisibility(View.VISIBLE);

        // 일단은 임시 방 이름 (나중에 상대 uid나 예약 id로 바꾸면 됨)
        String chatroomName = "testRoom";

        ChatMsgFragment chatFragment = ChatMsgFragment.newInstance(chatroomName);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.chat_container, chatFragment);
        ft.addToBackStack(null);   // 뒤로가기 누르면 원래 화면으로 돌아가게
        ft.commit();
    }
}
