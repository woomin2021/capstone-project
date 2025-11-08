package com.example.jjb20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.FirebaseLoginRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnSignup;

    private FirebaseAuth firebaseAuth;
    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etUserID);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnSignup  = findViewById(R.id.btnSignup);

        firebaseAuth = FirebaseAuth.getInstance();

        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // 키보드 "완료"로 로그인
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doLogin();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> doLogin());

        /*
        btnSignup.setOnClickListener(v -> {
            // 회원가입 화면이 있다면 이동, 없다면 Firebase createUserWithEmailAndPassword 로직을 붙여도 됩니다.
            startActivity(new Intent(this, SignupActivity.class)); // 없으면 주석처리
        }); */
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String pw    = etPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력하세요");
            return;
        }
        if (TextUtils.isEmpty(pw)) {
            etPassword.setError("비밀번호를 입력하세요");
            return;
        }

        // 1) 파이어베이스 이메일/비번 로그인
        btnLogin.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener(authResult -> {
                    // 2) ID 토큰 가져오기
                    firebaseAuth.getCurrentUser()
                            .getIdToken(true)
                            .addOnSuccessListener(result -> {
                                String idToken = result.getToken();
                                if (TextUtils.isEmpty(idToken)) {
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(this, "토큰을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 3) 서버 로그인 호출 (본문에 idToken)
                                callServerLogin(idToken);
                            })
                            .addOnFailureListener(e -> {
                                btnLogin.setEnabled(true);
                                Toast.makeText(this, "토큰 획득 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "로그인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void callServerLogin(@NonNull String idToken) {
        FirebaseLoginRequestDto body = new FirebaseLoginRequestDto(idToken);
        api.login(body).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call, @NonNull Response<UserResponseDto> response) {
                btnLogin.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto me = response.body();
                    Toast.makeText(LoginActivity.this, "환영합니다, " + (me.name != null ? me.name : me.email), Toast.LENGTH_SHORT).show();

                    //정보 저장용
                    //TODO 토큰 갱신
                    PrefManager.put("idToken", idToken);
                    PrefManager.put("uid", me.firebaseUid);
                    PrefManager.put("email", me.email);
//                    if (me.name != null) PrefManager.put("name", me.name);

                    Log.d("PrefCheck", "idToken: " + PrefManager.get("idToken", "없음"));
                    Log.d("PrefCheck", "uid: " + PrefManager.get("uid", "없음"));
                    Log.d("PrefCheck", "email: " + PrefManager.get("email", "없음"));
//                    Log.d("PrefCheck", "name: " + PrefManager.get("name", "없음"));

                    // TODO : 이후 메인 화면으로 이동 등, sharedPreference등에 사용자 정보/ 토큰 저장
                     startActivity(new Intent(LoginActivity.this, MainActivity.class));
                     finish();
                } else {
                    Toast.makeText(LoginActivity.this, "서버 로그인 실패(" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call, @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "서버 통신 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
