package com.example.jjb20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class ReserveConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);

        // 툴바 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        // 진행 바(2단계까지 채움) — 레이아웃에 seg1~seg4 가 없으면 그냥 넘어감 그리고 이거 오류 뜨는거 아무 문제 없음 실행 오류랑 상관 없으니까 걍 하셈
        View seg1 = findViewById(R.id.seg1);
        View seg2 = findViewById(R.id.seg2);
        View seg3 = findViewById(R.id.seg3);
        View seg4 = findViewById(R.id.seg4);
        int GREEN = Color.parseColor("#14D8B4");
        int GRAY  = getColor(R.color.basic2);

        if (seg1 != null) seg1.setBackgroundColor(GREEN);
        if (seg2 != null) seg2.setBackgroundColor(GREEN);
        if (seg3 != null) seg3.setBackgroundColor(GRAY);
        if (seg4 != null) seg4.setBackgroundColor(GRAY);

        // 이전 화면에서 전달된 값 반영
        String date   = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_DATE);
        String guests = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_GUESTS);
        String price  = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_PRICE);

        TextView tvDate   = findViewById(R.id.textDate);
        TextView tvGuest  = findViewById(R.id.textGuest);
        TextView tvPrice  = findViewById(R.id.priceValue);

        if (tvDate  != null && date  != null) tvDate.setText(date);
        if (tvGuest != null && guests!= null) tvGuest.setText(guests);
        if (tvPrice != null && price != null) tvPrice.setText(price);

        // 결제 페이지로 이동 (PaymentActivity 만들기 전까지 임시 Toast라 토스페이가 되야지 될듯 일단 보류 )
        findViewById(R.id.btnNext).setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class))
        );
        // 나중에 PaymentActivity 만들면 아래로 교체:
        // startActivity(new Intent(this, PaymentActivity.class));
    }
}