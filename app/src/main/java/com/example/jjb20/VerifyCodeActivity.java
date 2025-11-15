package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

/**
 * 회원가입 2단계 : 문자로 받은 6자리 코드 입력
 *  - verificationId + 입력한 code 로 Firebase 인증 시도
 *  - 성공하면 3단계(정보 입력) 화면으로 이동 + phone 을 함께 넘김
 */
public class VerifyCodeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvDesc, tvResend;
    private TextInputEditText etCode;
    private MaterialButton btnContinue;

    private FirebaseAuth auth;
    private String verificationId;
    private String phone;   // 1단계에서 넘겨준 원본 전화번호 문자열

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code); // 인증코드 입력 레이아웃

        auth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvDesc = findViewById(R.id.tvDesc);
        tvResend = findViewById(R.id.tvResend);
        etCode = findViewById(R.id.etCode);
        btnContinue = findViewById(R.id.btnContinue);

        btnBack.setOnClickListener(v -> finish());

        // 1단계에서 넘긴 값 받기
        phone = getIntent().getStringExtra("phone");
        verificationId = getIntent().getStringExtra("verificationId");

        if (phone != null) {
            tvDesc.setText("문자 메세지를 통해 " + phone + " 번호로 보내드린 코드를 입력하세요.");
        }

        // 계속하기 버튼 → 코드 검증
        btnContinue.setOnClickListener(v -> verifyCode());

        // "다시 보내기"는 지금은 토스트만 (ForceResendingToken 넘기려면 구조가 좀 복잡해짐)
        tvResend.setOnClickListener(v ->
                Toast.makeText(this, "재전송 기능은 나중에 구현 예정입니다.", Toast.LENGTH_SHORT).show()
        );
    }

    private void verifyCode() {
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            etCode.setError("6자리 코드를 입력하세요");
            return;
        }
        if (TextUtils.isEmpty(verificationId)) {
            Toast.makeText(this, "인증 세션이 만료되었습니다. 처음부터 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnContinue.setEnabled(false);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> {
                    btnContinue.setEnabled(true);

                    // 여기까지 오면 "해당 전화번호가 실제 사용자 것"이라는 정도만 확인한 것.
                    // 실제 로그인/회원 식별은 3단계에서 email/password 계정 생성으로 처리.

                    Intent i = new Intent(VerifyCodeActivity.this, SignupFormActivity.class);
                    i.putExtra("phone", phone);
                    startActivity(i);
                })
                .addOnFailureListener(e -> {
                    btnContinue.setEnabled(true);
                    Toast.makeText(VerifyCodeActivity.this,
                            "코드 인증 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
