package com.example.jjb20;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.adapter.ReviewAdapter;
import com.example.jjb20.chat.ChatMsgFragment;
import com.example.jjb20.dto.HostProfileDto;
import com.example.jjb20.dto.HouseReviewDTO;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostReviewActivity extends AppCompatActivity {
    private AppCompatButton chatBtn;
    private TextView tempTextView, hostName, rating;   // 온도 표시용

    private long hostId;             // 이 화면에 들어온 호스트의 id

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_review);

        chatBtn = findViewById(R.id.chatBtn);

        tempTextView = findViewById(R.id.host_temperature); // 추가
        hostName = findViewById(R.id.user_name);
        rating = findViewById(R.id.rating);

        // 이 액티비티를 띄울 때 putExtra("hostId", 호스트아이디)로 넘겨준다고 가정
        hostId = getIntent().getLongExtra("hostId", -1L);

        // 온도 / 프로필 불러오기
        fetchHostProfile();  // 이 줄이 핵심

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
        View container = findViewById(R.id.chat_container);
        if (container == null) {
            // chat_container 못 찾으면 XML id 문제
            return;
        }

        container.setVisibility(View.VISIBLE);

        String chatroomName = "testRoom"; // 나중에 실제 방 키로 교체

        ChatMsgFragment chatFragment = ChatMsgFragment.newInstance(chatroomName);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void fetchHostProfile() {
        if (hostId <= 0) {
            tempTextView.setText("--.-°");
            if (hostName != null) hostName.setText("호스트");
            if (rating != null) rating.setText("평점 없음");
            return;
        }

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.getHostProfile(hostId)
                .enqueue(new Callback<HostProfileDto>() {
                    @Override
                    public void onResponse(Call<HostProfileDto> call,
                                           Response<HostProfileDto> response) {

                        android.util.Log.d("TEMP_API",
                                "code=" + response.code() + ", body=" + response.body());

                        if (response.isSuccessful() && response.body() != null) {
                            HostProfileDto body = response.body();

                            // 온도
                            String temp = String.format(
                                    java.util.Locale.getDefault(),
                                    "%.1f°",
                                    body.temperature
                            );
                            tempTextView.setText(temp);

                            // 호스트 이름
                            if (hostName != null) {
                                if (body.realName != null && !body.realName.isEmpty()) {
                                    hostName.setText(body.realName);
                                } else {
                                    hostName.setText("호스트");
                                }
                            }

                            // 평점
                            if (rating != null) {
                                if (body.ratingCount > 0) {
                                    String ratingText = String.format(
                                            java.util.Locale.getDefault(),
                                            "★ %.1f (%d명)",
                                            body.ratingAvg,
                                            body.ratingCount
                                    );
                                    rating.setText(ratingText);
                                } else {
                                    rating.setText("평점 없음");
                                }
                            }

                        } else {
                            tempTextView.setText("--.-°");
                            if (rating != null) rating.setText("평점 없음");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<HostProfileDto> call,
                                          @NonNull Throwable t) {
                        android.util.Log.e("TEMP_API", "fail", t);
                        tempTextView.setText("--.-°");
                        if (rating != null) rating.setText("평점 없음");
                    }
                });
    }
}
