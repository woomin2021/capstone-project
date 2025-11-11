package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 회원가입 1단계 : 전화번호 입력 화면
 *  - 전화번호를 입력받고 Firebase에 "문자 인증 요청"만 보낸다.
 *  - 코드가 전송되면 verificationId 를 들고 2단계 화면으로 이동.
 */
public class SignupPhoneActivity extends AppCompatActivity {

    private EditText etPhone;
    private MaterialButton btnNext;
    private ImageButton btnBack;

    private FirebaseAuth auth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_phone); // 네가 만든 전화번호 화면 레이아웃

        auth = FirebaseAuth.getInstance();

        etPhone = findViewById(R.id.etPhone);
        btnNext = findViewById(R.id.btnNext);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        // "다음" 버튼 누르면 문자 인증 시작
        btnNext.setOnClickListener(v -> startPhoneVerification());

        // 콜백 정의 (한 번만 만들어 두고 재사용)
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // 일부 단말에서 자동으로 코드가 읽히면 여기로 들어온다.
                // 지금은 자동완성은 무시하고, 2단계에서 직접 입력하게 둘게.
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                btnNext.setEnabled(true);
                Toast.makeText(SignupPhoneActivity.this,
                        "인증번호 전송 실패: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                btnNext.setEnabled(true);

                String rawPhone = etPhone.getText().toString().trim();

                // 2단계 화면으로 이동 (phone + verificationId 전달)
                Intent i = new Intent(SignupPhoneActivity.this, VerifyCodeActivity.class);
                i.putExtra("phone", rawPhone);
                i.putExtra("verificationId", verificationId);
                startActivity(i);
            }
        };
    }

    private void startPhoneVerification() {
        String rawPhone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(rawPhone)) {
            etPhone.setError("전화번호를 입력하세요");
            return;
        }

        // 예: "01012345678" 또는 "010-1234-5678" → "+821012345678" 형태로 변환
        String normalized = rawPhone.replaceAll("[^0-9]", "");
        if (normalized.startsWith("0")) {
            normalized = normalized.substring(1);
        }
        String e164Phone = String.format(Locale.KOREA, "+82%s", normalized);

        btnNext.setEnabled(false);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(e164Phone)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }
}
