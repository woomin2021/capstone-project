package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.jjb20.SearchAddressActivity;

public class RegisterDetailsActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;

    // 입력 필드 변수
    private TextInputLayout addressInputLayout;
    private TextInputEditText addressEditText;
    private TextInputLayout addressDetailInputLayout;
    private TextInputEditText addressDetailEditText;
    private TextInputLayout descriptionInputLayout;
    private TextInputEditText descriptionEditText;
    private TextInputLayout summaryInputLayout;
    private TextInputEditText summaryEditText;

    private ActivityResultLauncher<Intent> addressSearchLauncher;

    // 모든 EditText의 변경을 감지할 공용 TextWatcher
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            // 텍스트가 변경될 때마다 버튼 상태 확인
            checkButtonState();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_details);

        // 2. ActivityResultLauncher 초기화
        // AddressSearchActivity가 반환한 주소값을 받아서 addressEditText에 설정
        addressSearchLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String address = result.getData().getStringExtra("address");
                        if (address != null && addressEditText != null) {
                            addressEditText.setText(address);
                            // 주소를 받으면 상세 주소 입력 필드로 포커스 이동
                            if (addressDetailEditText != null) {
                                addressDetailEditText.requestFocus();
                            }
                        }
                    }
                }
        );

        // 3. 뷰 초기화
        initViews();

        // 4. 이벤트 리스너 설정
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nextButton = findViewById(R.id.next_button);

        // 주소
        addressInputLayout = findViewById(R.id.address_input_layout);
        addressEditText = findViewById(R.id.address_edit_text);

        // 상세 주소
        addressDetailInputLayout = findViewById(R.id.address_detail_input_layout);
        addressDetailEditText = findViewById(R.id.address_detail_edit_text);

        // 설명
        descriptionInputLayout = findViewById(R.id.description_input_layout);
        descriptionEditText = findViewById(R.id.description_edit_text);

        // 한줄 설명
        summaryInputLayout = findViewById(R.id.summary_input_layout);
        summaryEditText = findViewById(R.id.summary_edit_text);
    }

    private void setupListeners() {
        // 툴바 (뒤로가기)
        toolbar.setNavigationOnClickListener(v -> finish());

        // '다음' 버튼 클릭 리스너
        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                // 입력 데이터 가져오기
                String address = (addressEditText != null) ? addressEditText.getText().toString().trim() : "";
                String addressDetail = (addressDetailEditText != null) ? addressDetailEditText.getText().toString().trim() : "";
                String description = (descriptionEditText != null) ? descriptionEditText.getText().toString().trim() : "";
                
                // 주소와 상세 주소를 합쳐서 저장
                String fullAddress = address;
                if (!addressDetail.isEmpty()) {
                    fullAddress += " " + addressDetail;
                }
                
                // SharedPreferences에 저장
                PrefManager.put("house_address", fullAddress);
                PrefManager.put("house_description", description);
                // city와 country는 일단 기본값으로 설정 (나중에 주소에서 파싱하거나 별도 입력 가능)
                PrefManager.put("house_city", "서울");
                PrefManager.put("house_country", "한국");
                
                // 다음 액티비티로 이동
                Intent intent = new Intent(RegisterDetailsActivity.this, RegisterCalendarActivity.class);
                startActivity(intent);
            }
        });

        // 주소 EditText 클릭 리스너 (주소 검색 실행)
        if (addressEditText != null) {
            addressEditText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RegisterDetailsActivity.this, SearchAddressActivity.class);
                    addressSearchLauncher.launch(intent);
                }
            });
            // TextWatcher는 계속 유지 (주소가 설정될 때 버튼 상태 체크)
            addressEditText.addTextChangedListener(textWatcher);
        }

        // 3개의 EditText에 공용 TextWatcher 연결 (null 체크)
        if (addressDetailEditText != null) {
            addressDetailEditText.addTextChangedListener(textWatcher);
        }
        if (descriptionEditText != null) {
            descriptionEditText.addTextChangedListener(textWatcher);
        }
        if (summaryEditText != null) {
            summaryEditText.addTextChangedListener(textWatcher);
        }
    }
    private final ActivityResultLauncher<Intent> getSearchResult=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Search Activity 로부터의 결과값이 이곳으로 전달됨(by setResult)
                if(result.getResultCode()==RESULT_OK){
                    if(result.getData()!=null){
                        String data=result.getData().getStringExtra("address");
                        addressEditText.setText(data);
                    }
                }
            }
    );

    /**
     * 모든 필수 입력 필드가 채워졌는지 확인하고 '다음' 버튼 상태를 업데이트
     */
    private void checkButtonState() {
        // 각 EditText가 null인지 먼저 확인
        String address = (addressEditText != null) ? addressEditText.getText().toString().trim() : "";
        String detail = (addressDetailEditText != null) ? addressDetailEditText.getText().toString().trim() : "";
        String description = (descriptionEditText != null) ? descriptionEditText.getText().toString().trim() : "";
        String summary = (summaryEditText != null) ? summaryEditText.getText().  toString().trim() : "";

        // 모든 필드가 비어있지 않은지 확인
        boolean allFieldsFilled = !address.isEmpty() &&
                !detail.isEmpty() &&
                !description.isEmpty() &&
                !summary.isEmpty();

        // 모든 필드가 채워졌으면 버튼 활성화
        nextButton.setEnabled(allFieldsFilled);

        // 색상 변경 로직
        if (allFieldsFilled) {
            // 활성화 상태: #00CBA8
            int color = Color.parseColor("#00CBA8");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            // 비활성화 상태: XML에 지정된 #BDBDBD 색상 유지
            int color = Color.parseColor("#BDBDBD");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }
}