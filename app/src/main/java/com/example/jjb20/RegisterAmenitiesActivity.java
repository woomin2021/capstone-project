package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterAmenitiesActivity extends AppCompatActivity {

    private static final String TAG = "RegisterAmenities";

    // 뷰 변수
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;
    private LinearLayout amenitiesContainer;

    // 체크박스 목록 관리 리스트
    private List<MaterialCheckBox> checkBoxes;

    // 편의시설 텍스트를 코드로 매핑하는 Map
    private static final Map<String, String> AMENITY_CODE_MAP = new HashMap<String, String>() {{
        put("주차장", "PARKING");
        put("와이파이", "WIFI");
        put("에어컨", "AIR_COND");
        put("난방", "HEATING");
        put("주방", "KITCHEN");
        put("세탁기", "WASHER");
        put("건조기", "DRYER");
        put("욕조", "BATHTUB");
        put("식탁", "DINING_TABLE");
        put("전자레인지", "MICROWAVE");
        put("냉장고", "REFRIGERATOR");
        put("TV", "TV");
    }};
    //프로그레스바 2단게
    private ProgressBar progressBarStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager.init(getApplicationContext());
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
        progressBarStep = findViewById(R.id.progressBarStep);

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
        //진행바
        progressBarStep.setMax(6);
        progressBarStep.setProgress(3);
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '다음' 버튼
        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                saveAmenities();
            }
        });
    }

    /**
     * 선택된 편의시설을 PrefManager에 저장하고 다음 단계로 이동
     * (실제 서버 저장은 RegisterFinalActivity에서 집 생성 후 수행됨)
     */
    private void saveAmenities() {
        // 1. 선택된 편의시설 코드 목록 생성
        List<String> selectedAmenityCodes = new ArrayList<>();
        for (MaterialCheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                String amenityText = checkBox.getText().toString();
                String amenityCode = AMENITY_CODE_MAP.get(amenityText);
                if (amenityCode != null) {
                    selectedAmenityCodes.add(amenityCode);
                } else {
                    Log.w(TAG, "Unknown amenity: " + amenityText);
                }
            }
        }

        // 2. 선택한 편의시설을 PrefManager에 저장하여 RegisterFinalActivity에서 사용
        PrefManager.putStringList("house_amenity_codes", selectedAmenityCodes);
        
        Log.d(TAG, "Selected amenities saved to PrefManager: " + selectedAmenityCodes.size() + " items");

        // 3. 다음 액티비티로 이동
        // (편의시설 저장 API는 RegisterFinalActivity에서 집 생성 후 호출됨)
        Intent intent = new Intent(RegisterAmenitiesActivity.this, RegisterDetailsActivity.class);
        startActivity(intent);
    }
}
