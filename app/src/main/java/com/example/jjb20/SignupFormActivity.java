package com.example.jjb20;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.RegisterRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 회원가입 3단계 : 이름/생년월일/이메일/비밀번호 입력
 *  - Firebase 이메일/비번 계정 생성
 *  - ID 토큰을 받아서 서버 /api/auth/register 로 전송 → DB에 Users 레코드 생성
 */
public class SignupFormActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etFirstName, etLastName, etBirth, etEmail, etPassword;
    private CheckBox cbShowPassword, cbConsent;
    private MaterialButton btnNext;

    private FirebaseAuth auth;
    private ApiService api;
    private String phoneFromPrev; // 2단계에서 넘긴 전화번호

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        auth = FirebaseAuth.getInstance();
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        btnBack = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirth = findViewById(R.id.etBirth);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        cbConsent = findViewById(R.id.cbConsent);
        btnNext = findViewById(R.id.btnNext);

        phoneFromPrev = getIntent().getStringExtra("phone");

        btnBack.setOnClickListener(v -> finish());

        // "비밀번호 표시" 체크박스
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(null);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.length());
        });

        // "다음(가입)" 버튼
        btnNext.setOnClickListener(v -> doSignUp());
    }

    private void doSignUp() {
        String firstName = safeText(etFirstName);
        String lastName  = safeText(etLastName);
        String birth     = safeText(etBirth);
        String email     = safeText(etEmail);
        String password  = safeText(etPassword);

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "이름과 성을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(birth) || birth.length() != 6) {
            Toast.makeText(this, "생년월일 6자리를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cbConsent.isChecked()) {
            Toast.makeText(this, "개인정보 수집 및 이용에 동의해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = lastName + firstName; // "홍" + "길동" → "홍길동"
        String phone = phoneFromPrev; // 전화번호는 2단계에서 검증해둔 값 사용

        btnNext.setEnabled(false);

        // 1) Firebase 이메일/비밀번호 계정 생성
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // 2) ID 토큰 가져오기
                    auth.getCurrentUser().getIdToken(true)
                            .addOnSuccessListener(result -> {
                                String idToken = result.getToken();
                                if (TextUtils.isEmpty(idToken)) {
                                    btnNext.setEnabled(true);
                                    Toast.makeText(this,
                                            "토큰을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 3) 서버 /api/auth/register 호출
                                RegisterRequestDto dto =
                                        new RegisterRequestDto(idToken, fullName, phone);

                                api.register(dto).enqueue(new Callback<UserResponseDto>() {
                                    @Override
                                    public void onResponse(@NonNull Call<UserResponseDto> call,
                                                           @NonNull Response<UserResponseDto> response) {
                                        btnNext.setEnabled(true);
                                        if (response.isSuccessful() && response.body() != null) {
                                            UserResponseDto user = response.body();
                                            Toast.makeText(SignupFormActivity.this,
                                                    "가입 완료! " + user.name + "님 환영합니다.",
                                                    Toast.LENGTH_LONG).show();

                                            // TODO: 메인 화면으로 이동하거나 현재 액티비티 종료
                                            // startActivity(new Intent(SignupInfoActivity.this, MainActivity.class));
                                            finish();
                                        } else if (response.code() == 409) {
                                            Toast.makeText(SignupFormActivity.this,
                                                    "이미 가입된 사용자입니다.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SignupFormActivity.this,
                                                    "서버 에러(" + response.code() + ")", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(@NonNull Call<UserResponseDto> call,
                                                          @NonNull Throwable t) {
                                        btnNext.setEnabled(true);
                                        Toast.makeText(SignupFormActivity.this,
                                                "서버 통신 실패: " + t.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                            .addOnFailureListener(e -> {
                                btnNext.setEnabled(true);
                                Toast.makeText(this,
                                        "토큰 획득 실패: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnNext.setEnabled(true);
                    Toast.makeText(this,
                            "Firebase 계정 생성 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
