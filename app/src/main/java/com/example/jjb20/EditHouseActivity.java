package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class EditHouseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        RelativeLayout editNameButton = findViewById(R.id.edit_name_button);
        RelativeLayout editAddressButton = findViewById(R.id.edit_address_button);
        RelativeLayout editDescriptionButton = findViewById(R.id.edit_description_button);
        RelativeLayout editPeriodButton = findViewById(R.id.edit_period_button);
        RelativeLayout editPriceButton = findViewById(R.id.edit_price_button);
        RelativeLayout editPhotosButton = findViewById(R.id.edit_photos_button);

        // 이름 수정 → RegisterHouseActivity
        editNameButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterTitleActivity.class);
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });

        // 주소 & 설명 수정 → RegisterDetailsActivity
        editAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterDetailsActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "address");  // 주소 수정을 구별
            startActivity(intent);
        });

        editDescriptionButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterDetailsActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "description"); // 설명 수정을 구별
            startActivity(intent);
        });

        // 임대 기간 수정 → RegisterCalendarActivity
        editPeriodButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterCalendarActivity.class);
            intent.putExtra("mode", "edit");
            startActivity(intent);
        });

        // 가격, 사진 수정 → RegisterFinalActivity
        editPriceButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterFinalActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "price");
            startActivity(intent);
        });

        editPhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterFinalActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "photos");
            startActivity(intent);
        });

        // 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
}
