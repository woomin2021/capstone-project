package com.example.jjb20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.jjb20.dto.ReservationDTO;
import com.example.jjb20.dto.ReservationCreateRequestDto;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // 1) 툴바 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        // 2) 진행바 3/4단계 채우기(있으면)
        View seg1 = findViewById(R.id.seg1);
        View seg2 = findViewById(R.id.seg2);
        View seg3 = findViewById(R.id.seg3);
        View seg4 = findViewById(R.id.seg4);
        int GREEN = Color.parseColor("#14D8B4");
        int GRAY  = getColor(R.color.basic2);

        if (seg1 != null) seg1.setBackgroundColor(GREEN);
        if (seg2 != null) seg2.setBackgroundColor(GREEN);
        if (seg3 != null) seg3.setBackgroundColor(GREEN);
        if (seg4 != null) seg4.setBackgroundColor(GRAY);

        // 3) 이전 화면에서 값 전달받아 표시
        Intent in = getIntent();
        String date    = in.getStringExtra(RentHouseDetailActivity.EXTRA_DATE);
        String guests  = in.getStringExtra(RentHouseDetailActivity.EXTRA_GUESTS);   // "성인 2명"
        String price   = in.getStringExtra(RentHouseDetailActivity.EXTRA_PRICE);    // "360000원 · 2박"
        String name    = in.getStringExtra(RentHouseDetailActivity.EXTRA_HOUSE_NAME);
        String address = in.getStringExtra(RentHouseDetailActivity.EXTRA_HOUSE_ADDR);
        String image   = in.getStringExtra(RentHouseDetailActivity.EXTRA_HOUSE_IMAGE);

        setTextIfExists("textDate",   date);
        setTextIfExists("textGuest",  guests);
        setTextIfExists("priceValue", price);

        // 4) 결제 버튼
        AppCompatButton btnPay = findViewById(R.id.btnNext);
        btnPay.setOnClickListener(v -> {


            // 전달 값 복구
            long houseId      = getIntent().getLongExtra("houseId", -1);
            String checkinDate  = getIntent().getStringExtra("checkinDate");   // "2025-12-15" 이런 형식이라고 가정
            String checkoutDate = getIntent().getStringExtra("checkoutDate");


            int guestCount = Integer.parseInt(guests.replaceAll("[^0-9]", ""));


            int totalPrice = Integer.parseInt(price.replaceAll("[^0-9]", ""));

            // (2) 요청 DTO 생성 서버의 ReservationsCreateRequestDto 에 맞는 DTO
            ReservationCreateRequestDto dto = new ReservationCreateRequestDto();
            dto.houseId      = houseId;
            dto.guestUserId  = PrefManager.getLong("userId");
            dto.checkinDate  = checkinDate;
            dto.checkoutDate = checkoutDate;
            dto.guestCount   = guestCount;
            dto.totalPrice   = totalPrice;

            ApiService api = RetrofitClient.getInstance().create(ApiService.class);

            api.createReservation(dto).enqueue(new Callback<ReservationDTO>() {
                @Override
                public void onResponse(Call<ReservationDTO> call, Response<ReservationDTO> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        ReservationDTO result = res.body();

                        Intent i = new Intent(PaymentActivity.this, PaymentCompleteActivity.class);
                        i.putExtra("reservationId", result.id);
                        i.putExtra("houseName", name);
                        i.putExtra("address", address);
                        i.putExtra("image", image);
                        i.putExtra("dateRange", date);
                        i.putExtra("nights", guests);
                        i.putExtra("price", price);
                        startActivity(i);
                        Log.d("예약요청",
                                "houseId=" + houseId +
                                        ", checkin=" + checkinDate +
                                        ", checkout=" + checkoutDate +
                                        ", guestCount=" + guestCount +
                                        ", totalPrice=" + totalPrice);
                    } else {
                        Log.e("예약 실패", "code=" + res.code());
                    }
                }

                @Override
                public void onFailure(Call<ReservationDTO> call, Throwable t) {
                    Log.e("예약 실패", t.getMessage(), t);
                }
            });
        });
    }

    private void setTextIfExists(String idName, String value) {
        if (value == null) return;
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        if (id != 0) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setText(value);
        }
    }
}