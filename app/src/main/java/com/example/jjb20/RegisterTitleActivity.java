package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HouseUpdateRequestDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterTitleActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private ProgressBar progressBarStep;
    private MaterialButton nextButton;
    private TextInputLayout titleInputLayout;
    private TextInputEditText titleEditText;

    private boolean isEditMode = false;
    private long houseId = -1L;
    private String currentTitle;
    private ApiService apiService;



    //진행바




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_title);

        // edit 모드 여부 확인
        String mode = getIntent().getStringExtra("mode");
        isEditMode = "edit".equalsIgnoreCase(mode);
        houseId = getIntent().getLongExtra("houseId", -1L);
        currentTitle = getIntent().getStringExtra("currentTitle");

        if (isEditMode && houseId == -1L) {
            Toast.makeText(this, "집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // 2. 뷰 초기화
        initViews();

        // 3. 이벤트 리스너 설정
        setupListeners();

        // 4. (선택 사항) 초기 버튼 상태 확인
        // XML에서 이미 android:enabled="false"로 설정되어 있지만,
        // 만약 동적으로 초기 상태를 확인해야 한다면 여기서 호출
        checkButtonState();
    }

    /**
     * XML 레이아웃의 뷰들을 찾아와 변수에 할당합니다.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBarStep = findViewById(R.id.progressBarStep);
        nextButton = findViewById(R.id.next_button);
        titleInputLayout = findViewById(R.id.title_input_layout);

        // TextInputLayout에서 TextInputEditText를 가져옵니다.
        titleEditText = (TextInputEditText) titleInputLayout.getEditText();


        // 이 화면은 6단계 중 1단계
        progressBarStep.setMax(6);
        progressBarStep.setProgress(1);

        if (isEditMode) {
            nextButton.setText("완료");
            toolbar.setTitle("집 정보 수정");
            if (currentTitle != null && titleEditText != null) {
                titleEditText.setText(currentTitle);
                titleEditText.setSelection(currentTitle.length());
            }
        }
    }

    /**
     * 뷰에 필요한 이벤트 리스너를 설정합니다.
     */
    private void setupListeners() {
        // 툴바의 네비게이션 아이콘 (뒤로가기 화살표) 클릭 리스너
        toolbar.setNavigationOnClickListener(v -> {
            // 현재 액티비티 종료
            finish();
        });

        // "다음" 버튼 클릭 리스너
        nextButton.setOnClickListener(v -> {
            // 버튼이 활성화되었을 때만 동작
            if (nextButton.isEnabled()) {
                String houseTitle = titleEditText.getText().toString().trim();

                // SharedPreferences에 제목 저장
                PrefManager.put("house_title", houseTitle);

                if (isEditMode) {
                    updateHouseTitle(houseTitle);
                } else {
                    // 다음 등록 단계로 이동
                    Intent intent = new Intent(RegisterTitleActivity.this, RegisterBasicsActivity.class);
                    startActivity(intent);
                }
            }
        });

        // 제목 입력 EditText의 텍스트 변경 감지 리스너
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 텍스트 변경 전
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 텍스트 변경 중
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 텍스트 변경 후
                checkButtonState();
            }
        });
    }

    /**
     * EditText의 텍스트 유무에 따라 "다음" 버튼의 활성화 상태를 변경합니다.
     */
    private void checkButtonState() {
        // 공백을 제거한 텍스트
        String title = titleEditText.getText().toString().trim();

        // 텍스트가 비어있지 않으면 (true) 버튼 활성화, 비어있으면 (false) 비활성화
        nextButton.setEnabled(!title.isEmpty());

        // MaterialButton은 setEnabled(true/false)에 따라
        // app:backgroundTint의 상태 (StateList)에 맞춰 색상이 자동으로 변경됩니다.
        // XML에서 android:enabled="false"일 때 #BDBDBD로 설정했으므로,
        // setEnabled(true)가 되면 테마의 기본 버튼 색상(아마도 #1ABC9C)으로 변경될 것입니다.
        // (만약 색상 변경이 자동으로 안된다면, ColorStateList를 backgroundTint에 사용해야 합니다.)

        // 4. 색상 변경 로직 추가
        if (!title.isEmpty()) {
            // 활성화 상태: #00CBA8
            int color = Color.parseColor("#00CBA8");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            // 비활성화 상태: XML에 지정된 색상 유지
            int color = Color.parseColor("#BDBDBD");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(color));
        }

    }

    private void updateHouseTitle(String newTitle) {
        nextButton.setEnabled(false);
        nextButton.setText("저장 중...");

        HouseUpdateRequestDto dto = new HouseUpdateRequestDto();
        dto.setTitle(newTitle);

        apiService.updateHouseBasicInfo(houseId, dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterTitleActivity.this, "집 제목이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    restoreButtonState();
                    Toast.makeText(RegisterTitleActivity.this,
                            "수정에 실패했습니다. (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                restoreButtonState();
                Toast.makeText(RegisterTitleActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void restoreButtonState() {
        nextButton.setEnabled(true);
        nextButton.setText(isEditMode ? "완료" : "다음");
    }
}