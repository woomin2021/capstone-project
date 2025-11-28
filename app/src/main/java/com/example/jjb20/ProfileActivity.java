package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.MyPageSummaryDto;
import com.example.jjb20.dto.UserProfileDto; // Import the new DTO
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.Locale; // Import Locale

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout houseListBtn, reservationListBtn, reservationRequestBtn;
    TextView name;
    private TextView tvHouseCount, tvReserveCount;
    Button profileEditbtn;
    Button logoutBtn;
    ImageView btnBack;
    CircleImageView profile_image;
    private FirebaseAuth mAuth;
    ApiService apiService;
    private MyPageSummaryDto myPageSummaryDto;

    // New UI elements for user profile stats
    private TextView userTemperature;
    private RatingBar guestRatingBar;
    private TextView guestRatingText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // 각종 버튼
        houseListBtn = findViewById(R.id.houseList);
        reservationListBtn = findViewById(R.id.reservationList);
        reservationRequestBtn = findViewById(R.id.reservationRequest);
        profileEditbtn = findViewById(R.id.profileEditbtn);
        btnBack = findViewById(R.id.btnBack);

        tvHouseCount = findViewById(R.id.tvHouseCount);
        tvReserveCount = findViewById(R.id.tvReserveCount);

        // Find new views
        userTemperature = findViewById(R.id.user_temperature);
        guestRatingBar = findViewById(R.id.guest_rating_bar);
        guestRatingText = findViewById(R.id.guest_rating_text);


        int myUserId = PrefManager.getInt("userId", -1);
        if (myUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getSummary(myUserId).enqueue(new Callback<MyPageSummaryDto>() {
            @Override
            public void onResponse(Call<MyPageSummaryDto> call, Response<MyPageSummaryDto> response) {
                if (response.isSuccessful() && response.body() != null) {

                    MyPageSummaryDto dto = response.body();

                    String fullHouseCount = String.valueOf(dto.getHouseCount()) + "개";
                    String fullReserveCount = String.valueOf(dto.getReservationCount()) + "개";
                    tvHouseCount.setText(fullHouseCount);
                    tvReserveCount.setText(fullReserveCount);

                } else {
                    Toast.makeText(ProfileActivity.this, "요약 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyPageSummaryDto> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });


        profile_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);

        name.setText(PrefManager.get("userName"));

        String imageUrl = PrefManager.get("profile_image");
        //프로필 이미지 불러오기
        Glide.with(this)
                        .load(imageUrl)
                                .into(profile_image);

        houseListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileHouseListActivity.class);
            startActivity(intent);
        });

        reservationListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationListActivity.class);
            startActivity(intent);
        });

        // TODO: Implement logic to show/hide this button based on whether the user is a host.
        // E.g., if (PrefManager.getBoolean("isHost", false)) { reservationRequestBtn.setVisibility(View.VISIBLE); } else { reservationRequestBtn.setVisibility(View.GONE); }
        reservationRequestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationRequestActivity.class);
            startActivity(intent);
        });

        profileEditbtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });


        //로그아웃
        logoutBtn = findViewById(R.id.logoutbtn);

        logoutBtn.setOnClickListener( v -> {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Fetch and display user profile stats
        fetchUserProfileStats();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String newImageUrl = PrefManager.get("profile_image");

        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(newImageUrl)
                    .into(profile_image);
        }
    }

    private void fetchUserProfileStats() {
        String idToken = PrefManager.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            userTemperature.setText("--.-°C");
            guestRatingText.setText("로그인이 필요합니다.");
            guestRatingBar.setRating(0);
            return;
        }

        String bearerToken = "Bearer " + idToken;

        apiService.getUserProfile(bearerToken)
                .enqueue(new Callback<UserProfileDto>() {
                    @Override
                    public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserProfileDto dto = response.body();

                            // 온도
                            String temp = String.format(Locale.getDefault(), "%.1f°C", dto.getTemperature());
                            userTemperature.setText(temp);
                            userTemperature.setTextColor(
                                    ContextCompat.getColor(
                                            ProfileActivity.this,
                                            getTemperatureColor(dto.getTemperature())
                                    )
                            );

                            // 게스트 평점
                            if (dto.getGuestReviewCount() > 0) {
                                guestRatingBar.setRating((float) dto.getGuestRatingAvg());
                                String ratingText = String.format(
                                        Locale.getDefault(),
                                        "%.1f / 5.0 (%d개)",
                                        dto.getGuestRatingAvg(),
                                        dto.getGuestReviewCount()
                                );
                                guestRatingText.setText(ratingText);
                            } else {
                                guestRatingBar.setRating(0);
                                guestRatingText.setText("게스트 평점 없음");
                            }

                        } else {
                            userTemperature.setText("--.-°C");
                            guestRatingText.setText("정보를 불러올 수 없습니다.");
                            guestRatingBar.setRating(0);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserProfileDto> call, Throwable t) {
                        userTemperature.setText("--.-°C");
                        guestRatingText.setText("서버 연결 실패");
                        guestRatingBar.setRating(0);
                    }
                });
    }

    private int getTemperatureColor(double temp) {
        if (temp >= 80) {
            return R.color.temp_very_high;
        } else if (temp >= 70) {
            return R.color.temp_high;
        } else if (temp < 30) {
            return R.color.temp_very_low;
        } else if (temp < 40) {
            return R.color.temp_low;
        } else {
            return R.color.temp_normal;
        }
    }
}
