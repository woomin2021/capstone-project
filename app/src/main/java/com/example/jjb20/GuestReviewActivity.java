package com.example.jjb20;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.ReservationDTO;
import com.example.jjb20.dto.ReviewRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GuestReviewActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText edtComment;
    private Button btnSubmit;

    private ApiService apiService;

    private long reservationId;
    private long userId;

    private ReservationDTO reservation;
    private static final String TAG = "GuestReviewActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_review);

        ratingBar = findViewById(R.id.ratingBar);
        edtComment = findViewById(R.id.edtReview);
        btnSubmit = findViewById(R.id.btnSubmit);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        reservationId = getIntent().getLongExtra("reservationId", -1);
        userId = PrefManager.getInt("userId", -1);

        reservation = (ReservationDTO) getIntent().getSerializableExtra("reservation");

        if (reservation == null) {
            Toast.makeText(this, "예약 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView txtGuestName = findViewById(R.id.txtHouseName); // XML ID는 재사용
        TextView txtDate = findViewById(R.id.txtDate);
        ImageView img = findViewById(R.id.imgHouse);

        txtGuestName.setText(
                (reservation.guestName != null && !reservation.guestName.isEmpty())
                        ? reservation.guestName
                        : "게스트 #" + reservation.guestUserId
        );

        if (reservation.guestProfileImageUrl != null && !reservation.guestProfileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(reservation.guestProfileImageUrl)
                    .placeholder(R.drawable.profile_image) // Assuming this drawable exists
                    .into(img);
        } else {
            img.setImageResource(R.drawable.profile_image); // Assuming this drawable exists
        }

        txtDate.setText(reservation.checkinDate + " ~ " + reservation.checkoutDate);

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
        
        // TODO: ApiService에 createGuestReview 메소드가 정의되어 있어야 합니다.
        apiService.createGuestReview(dto).enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                
                if (!response.isSuccessful()){
                    Toast.makeText(GuestReviewActivity.this, "리뷰 등록 실패", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(GuestReviewActivity.this, "게스트 리뷰가 등록 되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Toast.makeText(GuestReviewActivity.this, "네트워크 오류" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}