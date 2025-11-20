package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class RegisterBasicsActivity extends AppCompatActivity {

    // 뷰 변수
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;

    // 카운터 뷰
    private ImageButton bedroomMinus, bedroomPlus;
    private TextView bedroomCount;
    private ImageButton bedMinus, bedPlus;
    private TextView bedCount;
    private ImageButton bathroomMinus, bathroomPlus;
    private TextView bathroomCount;

    // 개수를 저장할 변수
    private int intBedroom = 0;
    private int intBed = 0;
    private int intBathroom = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_basics);

        // 2. 뷰 초기화
        initViews();

        // 3. 이벤트 리스너 설정
        setupListeners();

        // 4. 초기 UI 상태 업데이트 (0으로 설정 및 버튼 비활성화)
        updateUi();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nextButton = findViewById(R.id.next_button);

        // 침실
        bedroomMinus = findViewById(R.id.bedroom_minus);
        bedroomPlus = findViewById(R.id.bedroom_plus);
        bedroomCount = findViewById(R.id.bedroom_count);

        // 침대
        bedMinus = findViewById(R.id.bed_minus);
        bedPlus = findViewById(R.id.bed_plus);
        bedCount = findViewById(R.id.bed_count);

        // 욕실
        bathroomMinus = findViewById(R.id.bathroom_minus);
        bathroomPlus = findViewById(R.id.bathroom_plus);
        bathroomCount = findViewById(R.id.bathroom_count);
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '다음' 버튼
        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                PrefManager.put("house_bedroom_count", intBedroom);
                PrefManager.put("house_bed_count", intBed);
                PrefManager.put("house_bathroom_count", intBathroom);

                // 다음 액티비티로 데이터 전달 및 이동
                Intent intent = new Intent(RegisterBasicsActivity.this, RegisterAmenitiesActivity.class);
                // intent.putExtra("BEDROOM_COUNT", intBedroom);
                // intent.putExtra("BED_COUNT", intBed);
                // intent.putExtra("BATHROOM_COUNT", intBathroom);
                startActivity(intent);
            }
        });

        // --- 카운터 리스너 ---

        // 침실
        bedroomMinus.setOnClickListener(v -> {
            if (intBedroom > 0) {
                intBedroom--;
                updateUi();
            }
        });
        bedroomPlus.setOnClickListener(v -> {
            intBedroom++;
            updateUi();
        });

        // 침대
        bedMinus.setOnClickListener(v -> {
            if (intBed > 0) {
                intBed--;
                updateUi();
            }
        });
        bedPlus.setOnClickListener(v -> {
            intBed++;
            updateUi();
        });

        // 욕실
        bathroomMinus.setOnClickListener(v -> {
            if (intBathroom > 0) {
                intBathroom--;
                updateUi();
            }
        });
        bathroomPlus.setOnClickListener(v -> {
            intBathroom++;
            updateUi();
        });
    }

    /**
     * 현재 개수를 기준으로 UI 업데이트
     */
    private void updateUi() {
        // 1. TextView 텍스트 업데이트
        bedroomCount.setText(String.valueOf(intBedroom));
        bedCount.setText(String.valueOf(intBed));
        bathroomCount.setText(String.valueOf(intBathroom));

        // 2. Minus(-) 버튼 활성화/비활성화
        // (개수가 0이면 비활성화, 0보다 크면 활성화)
        bedroomMinus.setEnabled(intBedroom > 0);
        bedMinus.setEnabled(intBed > 0);
        bathroomMinus.setEnabled(intBathroom > 0);

        // 3. '다음' 버튼 활성화/비활성화
        // (하나라도 1 이상이면 활성화)
        boolean isEnabled = (intBedroom > 0) || (intBed > 0) || (intBathroom > 0);
        nextButton.setEnabled(isEnabled);

        // 4. 색상 변경 로직 추가
        if (isEnabled) {
            // 활성화 상태: #00CBA8
            int color = Color.parseColor("#00CBA8");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            // 비활성화 상태: XML에 지정된 색상 유지
            int color = Color.parseColor("#BDBDBD");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}