package com.example.jjb20;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.ReviewRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HouseReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit;

    private ApiService apiService;

    private long reservationId;
    private long userId;

    private static final String TAG = "HouseReviewActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_review);

        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmit);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        reservationId = getIntent().getLongExtra("reservationId", -1);
        userId = PrefManager.getInt("userId", -1);

        btnSubmit.setOnClickListener(v -> sendReview());
    }

    private void sendReview(){

        if (reservationId == -1){
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
                
                if (!response.isSuccessful()){
                    Toast.makeText(HouseReviewActivity.this, "리뷰 등록 실패", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(HouseReviewActivity.this, "리뷰가 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(HouseReviewActivity.this, "네트워크 오류" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
