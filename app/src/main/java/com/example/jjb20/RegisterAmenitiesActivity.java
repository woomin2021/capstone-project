package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

public class RegisterAmenitiesActivity extends AppCompatActivity {

    // 뷰 변수
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;
    private LinearLayout amenitiesContainer;

    // 체크박스 목록 관리 리스트
    private List<MaterialCheckBox> checkBoxes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_amenities);

        // 2. 뷰 초기화
        initViews();

        // 3. 이벤트 리스너 설정
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nextButton = findViewById(R.id.next_button);
        amenitiesContainer = findViewById(R.id.amenities_container);

        // 체크박스 리스트 초기화
        checkBoxes = new ArrayList<>();

        // amenitiesContainer 내부를 순회하며 MaterialCheckBox만 찾기
        if (amenitiesContainer != null) {
            for (int i = 0; i < amenitiesContainer.getChildCount(); i++) {
                View child = amenitiesContainer.getChildAt(i);
                // 자식 뷰가 MaterialCheckBox인 경우
                if (child instanceof MaterialCheckBox) {
                    MaterialCheckBox checkBox = (MaterialCheckBox) child;
                    checkBoxes.add(checkBox); // 리스트에 추가
                }
            }
        }
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '다음' 버튼
        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                // TODO: 선택된 편의시설 목록을 다음 액티비티로 전달
                // ArrayList<String> selectedAmenities = new ArrayList<>();
                // for (MaterialCheckBox checkBox : checkBoxes) {
                //     if (checkBox.isChecked()) {
                //         selectedAmenities.add(checkBox.getText().toString());
                //     }
                // }

                Intent intent = new Intent(RegisterAmenitiesActivity.this, RegisterDetailsActivity.class);
                // intent.putStringArrayListExtra("AMENITIES", selectedAmenities);
                startActivity(intent);
            }
        });
    }
}
