package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterFinalActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private TextView subtitleText;
    private MaterialButton registerButton;
    private MaterialCardView addPhotoButton;

    private TextInputEditText priceEditText;
    private TextView photoCounterText;

    // 갤러리에서 이미지를 선택하기 위한 최신 방식 (ActivityResultLauncher)
    // "registerForActivityResult"는 onCreate 또는 클래스 멤버 변수 초기화 시에 호출되어야 합니다.
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // 사용자가 이미지를 선택한 경우
                    // TODO: 1. 선택된 이미지를 (Glide/Coil 등으로) ImageView에 로드
                    // TODO: 2. HorizontalScrollView 내부의 LinearLayout에 동적으로 ImageView 추가
                    // TODO: 3. photoCounterText 업데이트 (예: "1/5")
                } else {
                    // 사용자가 선택을 취소한 경우
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_final);

        // 2. 뷰 초기화
        initViews();

        // 3. 이벤트 리스너 설정
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        subtitleText = findViewById(R.id.subtitle_text);
        registerButton = findViewById(R.id.register_button);
        addPhotoButton = findViewById(R.id.add_photo_button);
        priceEditText = findViewById(R.id.price_edit_text);
        photoCounterText = findViewById(R.id.photoCounterText);
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '사진 추가' 버튼 클릭 리스너
        addPhotoButton.setOnClickListener(v -> {
            openGallery();
        });

        // '등록하기' 버튼 클릭 리스너
        registerButton.setOnClickListener(v -> {
            performRegistration();
        });
    }

    /**
     * 갤러리를 열어 사진을 선택하게 합니다.
     * (최신 안드로이드 방식인 PickVisualMedia 사용)
     */
    private void openGallery() {
        // TODO: 사진 개수가 5개 미만일 때만 갤러리를 열도록 조건 추가
        // if (currentPhotoCount < 5) { ... }

        // 갤러리를 열어 이미지만 선택하도록 함
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());

        // 만약 여러 장의 사진을 한 번에 선택하게 하려면
        // (참고) pickMultipleMedia.launch(...)
    }

    /**
     * '등록하기' 버튼 클릭 시 호출되는 메서드
     */
    private void performRegistration() {
         // 1. 가격 정보 가져오기
         String price = priceEditText.getText().toString();
         if (price.isEmpty()) {
             Toast.makeText(this, "가격을 입력해주세요.", Toast.LENGTH_SHORT).show();
             return;
         }

        // 2. 사진 목록 정보 가져오기
        // (openGallery에서 동적으로 추가된 사진 목록을 수집하는 로직)

        // 3. (임시) 등록 로직 대신 토스트 메시지 표시
        Toast.makeText(this, "등록 완료!", Toast.LENGTH_SHORT).show();

        // 4. TODO: 실제 서버로 데이터 전송 (API 호출) 로직 구현

        // 5. TODO: 등록 완료 후, 완료 화면으로 이동하거나 앱의 메인 화면으로 이동
        Intent intent = new Intent(RegisterFinalActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 등록 플로우의 모든 액티비티 종료
    }
}