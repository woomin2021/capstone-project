package com.example.jjb20;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.HouseDetailResponseDto;
import com.example.jjb20.dto.ReservationDTO;
import com.example.jjb20.dto.ReviewRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit;
    private ImageView houseImg;
    private ImageView btnBack;
    private String coverphotourl;
    private String houseName;

    private ApiService apiService;

    private long reservationId;
    private long userId;
    private long houseId;

    private ReservationDTO reservation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_review);

        // 🔥 반드시 먼저 findViewById!
        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmit);
        houseImg = findViewById(R.id.imgHouse);
        btnBack = findViewById(R.id.btnBack);   // XML에 back 버튼 있다면 id 맞춰줘야 함
        TextView txtTitle = findViewById(R.id.txtHouseName);
        TextView txtDate = findViewById(R.id.txtDate);

        // Retrofit 초기화
        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // Intent 값 로드
        reservationId = getIntent().getLongExtra("reservationId", -1);
        userId = PrefManager.getInt("userId", -1);

        reservation = (ReservationDTO) getIntent().getSerializableExtra("reservation");

        if (reservation == null) {
            Toast.makeText(this, "예약 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        houseId = reservation.houseId;

        // 체크인/아웃 날짜 UI
        txtDate.setText(reservation.checkinDate + " ~ " + reservation.checkoutDate);

        // 🔙 뒤로가기 버튼
        btnBack.setOnClickListener(v -> finish());

        // 📌 하우스 상세 정보 요청 (사진, 이름)
        apiService.getHouseFullDetail(houseId).enqueue(new Callback<HouseDetailResponseDto>() {
            @Override
            public void onResponse(Call<HouseDetailResponseDto> call, Response<HouseDetailResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("HouseReview", "서버 응답 오류");
                    return;
                }

                HouseDetailResponseDto dto = response.body();
                coverphotourl = dto.getCoverPhotoUrl();
                houseName = dto.getTitle();

                txtTitle.setText(houseName);

                // 이미지 로드
                Glide.with(HouseReviewActivity.this)
                        .load(coverphotourl)
                        .into(houseImg);

                Log.d("HouseReviewActivity", "coverPhotoUrl = " + coverphotourl);
                Log.d("HouseReviewActivity", "houseName = " + houseName);
            }

            @Override
            public void onFailure(Call<HouseDetailResponseDto> call, Throwable t) {
                Log.e("HouseReview", "네트워크 오류: " + t.getMessage());
            }
        });

        // 리뷰 제출 버튼
        btnSubmit.setOnClickListener(v -> sendReview());
    }

    private void sendReview() {

        if (reservationId == -1) {
            Toast.makeText(this, "예약 정보가 없습니다. 오류", Toast.LENGTH_SHORT).show();
            return;
        }

        short ratingValue = (short) ratingBar.getRating();
        String comment = edtComment.getText().toString().trim();

        ReviewRequestDto dto = new ReviewRequestDto(
                reservationId,
                ratingValue,
                comment
        );

        apiService.createHouseReview(dto).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(HouseReviewActivity.this, "리뷰 등록 실패", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(HouseReviewActivity.this, "리뷰가 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(HouseReviewActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
