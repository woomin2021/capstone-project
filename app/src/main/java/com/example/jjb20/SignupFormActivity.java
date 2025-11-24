package com.example.jjb20;

import android.os.Bundle;
import android.text.TextUtils;
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
 *  - 1단계: Firebase 이메일/비번 계정 생성 + 이메일 인증 메일 전송
 *  - 2단계: 이메일 인증 완료 여부 확인 후 ID 토큰을 서버 /api/auth/register 로 전송 → DB에 Users 레코드 생성
 */
public class SignupFormActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etFirstName, etLastName, etBirth, etEmail, etPassword;
    private CheckBox cbConsent;
    private MaterialButton btnNext;

    private FirebaseAuth auth;
    private ApiService api;
    private String phoneFromPrev; // 2단계(휴대폰 인증)에서 넘긴 전화번호

    // 🔹 이메일 인증 메일 보낸 상태인지
    private boolean verificationMailSent = false;

    // 🔹 서버 회원가입 시 다시 사용하기 위한 값
    private String fullName;
    private String phone; // phoneFromPrev 복사해서 사용

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
        cbConsent = findViewById(R.id.cbConsent);
        btnNext = findViewById(R.id.btnNext);

        phoneFromPrev = getIntent().getStringExtra("phone");

        btnBack.setOnClickListener(v -> finish());

        // 🔹 "다음" 버튼 – 상태에 따라 두 가지 동작
        btnNext.setOnClickListener(v -> {
            if (!verificationMailSent) {
                // 1단계: 계정 생성 + 이메일 인증 메일 전송
                startSignupAndSendEmail();
            } else {
                // 2단계: 이메일 인증 완료 확인 + 서버 회원가입
                completeSignupIfEmailVerified();
            }
        });
    }

    /**
     * 1단계:
     *  - 입력값 검증
     *  - Firebase 이메일/비밀번호 계정 생성
     *  - 이메일 인증 메일 전송
     */
    private void startSignupAndSendEmail() {
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

        // 나중에 서버로 보낼 값 저장
        fullName = lastName + firstName; // "홍" + "길동" → "홍길동"
        phone = phoneFromPrev;

        btnNext.setEnabled(false);

        // 1) Firebase 이메일/비밀번호 계정 생성
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (auth.getCurrentUser() == null) {
                        btnNext.setEnabled(true);
                        Toast.makeText(this, "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) 이메일 인증 메일 전송
                    auth.getCurrentUser().sendEmailVerification()
                            .addOnSuccessListener(unused -> {
                                btnNext.setEnabled(true);
                                verificationMailSent = true;

                                Toast.makeText(this,
                                        "이메일로 인증 링크를 보냈습니다.\n메일에서 인증을 완료한 뒤 '이메일 인증 완료' 버튼을 눌러주세요.",
                                        Toast.LENGTH_LONG).show();

                                // 버튼 텍스트 변경
                                btnNext.setText("이메일 인증 완료");
                            })
                            .addOnFailureListener(e -> {
                                btnNext.setEnabled(true);
                                Toast.makeText(this,
                                        "인증 메일 전송 실패: " + e.getMessage(),
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

    /**
     * 2단계:
     *  - Firebase User 정보 reload
     *  - isEmailVerified() 확인
     *  - true면 ID 토큰 발급 → 서버 /api/auth/register 호출
     */
    private void completeSignupIfEmailVerified() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);

        // 최신 상태로 갱신 후 이메일 인증 여부 확인
        auth.getCurrentUser().reload()
                .addOnSuccessListener(unused -> {
                    if (!auth.getCurrentUser().isEmailVerified()) {
                        btnNext.setEnabled(true);
                        Toast.makeText(this,
                                "이메일 인증이 아직 완료되지 않았습니다.\n메일의 링크를 먼저 눌러주세요.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ 여기서부터 이메일 인증 완료 상태
                    auth.getCurrentUser().getIdToken(true)
                            .addOnSuccessListener(result -> {
                                String idToken = result.getToken();
                                if (TextUtils.isEmpty(idToken)) {
                                    btnNext.setEnabled(true);
                                    Toast.makeText(this,
                                            "토큰을 가져오지 못했습니다.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 서버 /api/auth/register 호출
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
                                            // 여기서 메인으로 이동하거나 현재 액티비티 종료
                                            finish();
                                        } else if (response.code() == 400) {
                                            // 서버에서 "이메일 인증 안 됨"으로 막는 경우 대비
                                            Toast.makeText(SignupFormActivity.this,
                                                    "이메일 인증이 완료되지 않았습니다.(서버)",
                                                    Toast.LENGTH_SHORT).show();
                                        } else if (response.code() == 409) {
                                            Toast.makeText(SignupFormActivity.this,
                                                    "이미 가입된 사용자입니다.",
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SignupFormActivity.this,
                                                    "서버 에러(" + response.code() + ")",
                                                    Toast.LENGTH_SHORT).show();
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
                            "사용자 정보 갱신 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }
}
