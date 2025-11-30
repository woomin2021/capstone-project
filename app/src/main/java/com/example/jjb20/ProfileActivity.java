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
import com.example.jjb20.dto.UserProfileDto;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout houseListBtn, reservationListBtn, reservationRequestBtn;
    private TextView name;
    private TextView tvHouseCount, tvReserveCount, tvReserveRequestCount;

    // 🔹 통합 온도 / 평점 / 리뷰 텍스트
    private TextView tvTemperature;       // 매너온도 텍스트 (--.-°C)
    private RatingBar guestRatingBar;     // 별점
    private TextView guestRatingText;     // "평점 없음" / "4.5점 (3명)"

    private Button profileEditbtn;
    private Button logoutBtn;
    private ImageView btnBack;
    private CircleImageView profile_image;

    private FirebaseAuth mAuth;
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager.init(this);
        setContentView(R.layout.activity_profile);

        // 디버그용 로그
        android.util.Log.d(
                "PROFILE_DEBUG",
                "userId=" + PrefManager.getInt("userId", -1)
                        + ", userName=" + PrefManager.get("userName")
                        + ", profile_image=" + PrefManager.get("profile_image")
        );

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // ====== View 바인딩 ======
        houseListBtn = findViewById(R.id.houseList);
        reservationListBtn = findViewById(R.id.reservationList);
        reservationRequestBtn = findViewById(R.id.reservationRequest);
        profileEditbtn = findViewById(R.id.profileEditbtn);
        btnBack = findViewById(R.id.btnBack);
        logoutBtn = findViewById(R.id.logoutbtn);

        tvHouseCount = findViewById(R.id.tvHouseCount);
        tvReserveCount = findViewById(R.id.tvReserveCount);
        tvReserveRequestCount = findViewById(R.id.tvReserveRequestCount);

        // 🔹 XML의 id랑 매칭
        tvTemperature = findViewById(R.id.user_temperature);
        guestRatingBar = findViewById(R.id.guest_rating_bar);
        guestRatingText = findViewById(R.id.guest_rating_text);

        profile_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);

        // ====== 이름 / 프로필 이미지 세팅 ======
        name.setText(PrefManager.get("userName"));

        String imageUrl = PrefManager.get("profile_image");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(profile_image);
        }

        // ====== 서버 데이터 로드 ======
        loadSummary();      // 집 개수 / 예약 개수 / 요청 개수
        loadUserProfile();  // 통합 온도 / 평점 / 리뷰 텍스트

        // ====== 버튼 리스너 ======
        houseListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileHouseListActivity.class);
            startActivity(intent);
        });

        reservationListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationListActivity.class);
            startActivity(intent);
        });

        reservationRequestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationRequestActivity.class);
            startActivity(intent);
        });

        profileEditbtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());

        // 로그아웃
        logoutBtn.setOnClickListener(v -> {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * 집 개수 / 예약 개수 / 요청 개수 로드
     */
    private void loadSummary() {
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

                    long houseCount = dto.getHouseCount() != null ? dto.getHouseCount() : 0;
                    long reserveCount = dto.getReservationCount() != null ? dto.getReservationCount() : 0;
                    Long reqCountObj = dto.getReceivedRequestCount();
                    long reqCount = reqCountObj != null ? reqCountObj : 0;

                    tvHouseCount.setText(houseCount + "개");
                    tvReserveCount.setText(reserveCount + "개");
                    if (tvReserveRequestCount != null) {
                        tvReserveRequestCount.setText(reqCount + "개");
                    }

                } else {
                    Toast.makeText(ProfileActivity.this, "요약 정보 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MyPageSummaryDto> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "서버 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 통합 온도 + 게스트 리뷰 정보 로드
     * GET /api/users/me/profile
     */
    private void loadUserProfile() {
        // 로그인 시 저장해둔 Firebase ID 토큰 (키 이름은 프로젝트에서 실제 사용하는 걸로 맞추기)
        String idToken = PrefManager.get("idToken");

        if (idToken == null || idToken.isEmpty()) {
            android.util.Log.e("PROFILE_DEBUG", "idToken 없음, /me/profile 호출 불가");
            setProfileStatsFallback();
            return;
        }

        String bearer = "Bearer " + idToken;

        apiService.getUserProfile(bearer).enqueue(new Callback<UserProfileDto>() {
            @Override
            public void onResponse(Call<UserProfileDto> call, Response<UserProfileDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileDto dto = response.body();

                    double temp = dto.getTemperature();
                    long reviewCount = dto.getGuestReviewCount();
                    double ratingAvg = dto.getGuestRatingAvg();

                    // 온도 텍스트
                    if (tvTemperature != null) {
                        String tempText = String.format(Locale.getDefault(), "%.1f°C", temp);
                        tvTemperature.setText(tempText);

                        // 🔹 온도에 따라 색상 변경 (HostReviewActivity와 동일 로직)
                        int colorRes = getTemperatureColor(temp);
                        tvTemperature.setTextColor(
                                ContextCompat.getColor(ProfileActivity.this, colorRes)
                        );
                    }

                    // 평점 / 별
                    if (guestRatingBar != null) {
                        if (reviewCount > 0) {
                            guestRatingBar.setRating((float) ratingAvg);
                        } else {
                            guestRatingBar.setRating(0f);
                        }
                    }

                    // 텍스트 ("평점 없음" or "4.5점 (3명)")
                    if (guestRatingText != null) {
                        if (reviewCount > 0) {
                            String ratingText = String.format(
                                    Locale.getDefault(),
                                    "%.1f점 (%d명)",
                                    ratingAvg,
                                    reviewCount
                            );
                            guestRatingText.setText(ratingText);
                        } else {
                            guestRatingText.setText("평점 없음");
                        }
                    }

                    android.util.Log.d(
                            "PROFILE_DEBUG",
                            "temp=" + temp + ", avg=" + ratingAvg + ", cnt=" + reviewCount
                    );

                } else {
                    android.util.Log.e("PROFILE_DEBUG", "getUserProfile 실패, code=" + response.code());
                    setProfileStatsFallback();
                }
            }

            @Override
            public void onFailure(Call<UserProfileDto> call, Throwable t) {
                android.util.Log.e("PROFILE_DEBUG", "getUserProfile 실패", t);
                setProfileStatsFallback();
            }
        });
    }

    /**
     * 온도/평점 영역 기본값으로 되돌리기
     */
    private void setProfileStatsFallback() {
        if (tvTemperature != null) {
            tvTemperature.setText("--.-°C");
            tvTemperature.setTextColor(
                    ContextCompat.getColor(this, R.color.temp_normal)
            );
        }
        if (guestRatingBar != null) {
            guestRatingBar.setRating(0f);
        }
        if (guestRatingText != null) {
            guestRatingText.setText("평점 없음");
        }
    }

    /**
     * 🔥 HostReviewActivity와 동일한 온도 색상 로직
     */
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

    @Override
    protected void onResume() {
        super.onResume();

        // 프로필 이미지 새로고침
        String newImageUrl = PrefManager.get("profile_image");

        if (newImageUrl != null && !newImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(newImageUrl)
                    .into(profile_image);
        }

        // 요약 정보 / 프로필 통계 새로고침
        loadSummary();
        loadUserProfile();
    }
}
