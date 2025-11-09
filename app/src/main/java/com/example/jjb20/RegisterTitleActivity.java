package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterTitleActivity extends AppCompatActivity {

    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private MaterialButton nextButton;
    private TextInputLayout titleInputLayout;
    private TextInputEditText titleEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 1. XML 레이아웃 파일 설정
        setContentView(R.layout.activity_register_title);

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
        progressBar = findViewById(R.id.progressBar);
        nextButton = findViewById(R.id.next_button);
        titleInputLayout = findViewById(R.id.title_input_layout);

        // TextInputLayout에서 TextInputEditText를 가져옵니다.
        titleEditText = (TextInputEditText) titleInputLayout.getEditText();
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

                // 다음 등록 단계로 이동 (예: NextRegisterActivity)
                Intent intent = new Intent(RegisterTitleActivity.this, RegisterBasicsActivity.class);
                // intent.putExtra("HOUSE_TITLE", houseTitle);
                startActivity(intent);

                // (임시) 테스트용으로 토스트 메시지 표시
                // Toast.makeText(this, "입력된 제목: " + houseTitle, Toast.LENGTH_SHORT).show();
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
}