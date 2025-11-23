package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

public class PaymentCompleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_complete);

        //  1) Toolbar 뒤로가기 =====
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(null);
        }

        // 2) Intent 데이터 받기 =====
        String houseName  = getIntent().getStringExtra("houseName");
        String address    = getIntent().getStringExtra("address");
        String imageUrl   = getIntent().getStringExtra("image");
        String dateRange  = getIntent().getStringExtra("dateRange");
        String nights     = getIntent().getStringExtra("nights");
        String price      = getIntent().getStringExtra("price");

        //  3) View 연결 =====
        ImageView imgHotel      = findViewById(R.id.imageHotel);
        TextView tvName         = findViewById(R.id.textHotelName);
        TextView tvDateRange    = findViewById(R.id.textDateRange);
        TextView tvAddress      = findViewById(R.id.textAddress);
        TextView tvPrice        = findViewById(R.id.textPrice);
        View btnDone            = findViewById(R.id.btnDone);

        // 4) UI 반영 =====
        if (tvName != null && houseName != null)   tvName.setText(houseName);
        if (tvDateRange != null && dateRange != null) tvDateRange.setText(dateRange);
        if (tvAddress != null && address != null) tvAddress.setText(address);
        if (tvPrice != null && price != null)     tvPrice.setText(price);

        //5) 이미지 표시 =====
        if (imageUrl != null && imgHotel != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.sample1)        // 로딩 전 기본 이미지
                    .error(R.drawable.sample1)              // 실패 시 이미지
                    .into(imgHotel);
        }

        btnDone.setOnClickListener(v -> {
            Intent i = new Intent(PaymentCompleteActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });
    }
}