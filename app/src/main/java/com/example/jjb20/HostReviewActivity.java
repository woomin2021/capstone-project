package com.example.jjb20;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jjb20.adapter.ReviewAdapter;
import com.example.jjb20.chat.ChatMsgFragment;
import com.example.jjb20.dto.HostProfileDto;
import com.example.jjb20.dto.HouseReviewDTO;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostReviewActivity extends AppCompatActivity {
    private AppCompatButton chatBtn;
    private TextView tempTextView, hostName, rating;   // 온도 표시용
    private TextView tvReviewCountTop, tvReviewCountSection;
    private ImageView profileImageView;

    private long hostId;             // 이 화면에 들어온 호스트의 id
    private long myUserId;
    private String chatroomName;

    private RecyclerView recyclerView;
    private ArrayList<HouseReviewDTO> reviewList = new ArrayList<>();
    private ReviewAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_review);

        chatBtn = findViewById(R.id.chatBtn);


        tempTextView = findViewById(R.id.host_temperature);
        hostName     = findViewById(R.id.user_name);
        rating       = findViewById(R.id.rating);
        tvReviewCountTop = findViewById(R.id.review_count1);
        tvReviewCountSection = findViewById(R.id.review_count2);
        profileImageView = findViewById(R.id.profile_img);

        hostId = getIntent().getLongExtra("hostId", -1L);
        myUserId = PrefManager.getInt("userId", -1);

        // 리사이클러뷰 초기화
        recyclerView = findViewById(R.id.review_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReviewAdapter(reviewList);
        recyclerView.setAdapter(adapter);


        // 온도 / 프로필 불러오기
        fetchHostProfile();
        // 리뷰 목록 불러오기
        loadHostReviews();

        chatBtn.setOnClickListener(v -> openChatFragment());
        // 툴바
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void openChatFragment() {
        View container = findViewById(R.id.chat_container);
        if (container == null) {
            return;
        }

        // 내 userId 또는 hostId가 이상하면 방 못 열게
        if (myUserId <= 0 || hostId <= 0) {
            android.widget.Toast.makeText(
                    this,
                    "채팅 정보를 불러올 수 없습니다.",
                    android.widget.Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 두 사람의 ID를 이용해서 항상 같은 방 이름이 나오도록 정렬
        long a = Math.min(myUserId, hostId);
        long b = Math.max(myUserId, hostId);
        String roomName = "dm_" + a + "_" + b;   // 예: dm_1_5

        container.setVisibility(View.VISIBLE);

        ChatMsgFragment chatFragment = ChatMsgFragment.newInstance(roomName);

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

                            // 프로필 이미지
                            if (body.profileImageUrl != null && !body.profileImageUrl.isEmpty()) {
                                Glide.with(HostReviewActivity.this)
                                        .load(body.profileImageUrl)
                                        .into(profileImageView);
                            }

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

                            // 여기서 채팅방 이름 확정 (host_호스트ID)
                            chatroomName = "host_" + hostId;


                            // 후기
                            if (tvReviewCountTop != null || tvReviewCountSection != null) {
                                if (body.ratingCount > 0) {
                                    // 리뷰가 1개 이상 있을 때만 숫자 표시
                                    String topText = String.format(Locale.getDefault(), "+%,d개", body.ratingCount);
                                    String sectionText = String.format(Locale.getDefault(), "%,d개", body.ratingCount);

                                    if (tvReviewCountTop != null) {
                                        tvReviewCountTop.setText(topText);
                                    }
                                    if (tvReviewCountSection != null) {
                                        tvReviewCountSection.setText(sectionText);
                                    }
                                } else {
                                    // 리뷰가 한 개도 없으면 "-" 로 표시
                                    if (tvReviewCountTop != null) {
                                        tvReviewCountTop.setText("-");
                                    }
                                    if (tvReviewCountSection != null) {
                                        tvReviewCountSection.setText("-");
                                    }
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


    private void loadHostReviews() {
        if (hostId <= 0) return;

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getHostReviews(hostId)
                .enqueue(new Callback<List<HouseReviewDTO>>() {
                    @Override
                    public void onResponse(Call<List<HouseReviewDTO>> call,
                                           Response<List<HouseReviewDTO>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            reviewList.clear();
                            reviewList.addAll(response.body());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<HouseReviewDTO>> call, Throwable t) { }
                });
    }
}
