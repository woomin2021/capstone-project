// 파일: app/src/main/java/com/example/jjb20/PaymentActivity.java
package com.example.jjb20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class PaymentActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
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

        // 3) 이전 화면에서 값 전달받아 표시(있으면)
        String date   = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_DATE);
        String guests = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_GUESTS);
        String price  = getIntent().getStringExtra(RentHouseDetailActivity.EXTRA_PRICE);

        setTextIfExists("textDate",   date);
        setTextIfExists("textGuest",  guests);
        setTextIfExists("priceValue", price);

        // 4) 결제 버튼(여러 후보 id 중 있는 것 사용)
        View btnPay = findFirst("btnPay", "btnNext", "payButton", "buttonPay", "btnConfirm");
        if (btnPay != null) {
            btnPay.setOnClickListener(v ->
                    startActivity(new Intent(this, PaymentCompleteActivity.class))
            );
        }
    }

    private void setTextIfExists(String idName, String value) {
        if (value == null) return;
        int id = getResources().getIdentifier(idName, "id", getPackageName());
        if (id != 0) {
            TextView tv = findViewById(id);
            if (tv != null) tv.setText(value);
        }
    }

    /** 여러 후보 id 중 먼저 발견되는 뷰를 반환 */
    private View findFirst(String... ids) {
        for (String name : ids) {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v != null) return v;
            }
        }
        return null;
    }
}