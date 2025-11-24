package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.FirebaseLoginRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin, btnSignup, btnGoogleLogin;

    private FirebaseAuth firebaseAuth;
    private ApiService api;

    // 구글 로그인용
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrefManager.init(this);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etUserID);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnSignup  = findViewById(R.id.btnSignup);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin); // 새로 추가한 버튼

        firebaseAuth = FirebaseAuth.getInstance();

        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // 1) 이메일/비번 로그인 기존 로직 그대로
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doLoginWithEmail();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> doLoginWithEmail());

        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupFormActivity.class))
        );

        // 2) 구글 로그인 세팅
        setupGoogleSignIn();
    }

    /** ================= 이메일/비번 로그인 ================= */
    private void doLoginWithEmail() {
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

        btnLogin.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email, pw)
                .addOnSuccessListener(authResult -> {
                    firebaseAuth.getCurrentUser()
                            .getIdToken(true)
                            .addOnSuccessListener(result -> {
                                String idToken = result.getToken();
                                if (TextUtils.isEmpty(idToken)) {
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(this, "토큰을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
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

    /** ================= 공통: 서버 로그인 ================= */
    private void callServerLogin(@NonNull String idToken) {
        FirebaseLoginRequestDto body = new FirebaseLoginRequestDto(idToken);
        api.login(body).enqueue(new Callback<UserResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<UserResponseDto> call,
                                   @NonNull Response<UserResponseDto> response) {
                btnLogin.setEnabled(true);
                btnGoogleLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    UserResponseDto me = response.body();
                    Toast.makeText(LoginActivity.this,
                            "환영합니다, " + (me.name != null ? me.name : me.email),
                            Toast.LENGTH_SHORT).show();

                    // 정보 저장
                    PrefManager.put("idToken", idToken);
                    PrefManager.put("uid", me.firebaseUid);
                    PrefManager.put("email", me.email);
                    PrefManager.put("userId", me.id);
                    PrefManager.put("userName", me.name);
                    PrefManager.put("phone", me.phone);
                    PrefManager.put("profile_image", me.profileImageUrl);

                    Log.d("PrefCheck", "idToken: " + PrefManager.get("idToken", "없음"));
                    Log.d("PrefCheck", "uid: " + PrefManager.get("uid", "없음"));
                    Log.d("PrefCheck", "email: " + PrefManager.get("email", "없음"));
                    Log.d("PrefCheck", "userName: " + PrefManager.get("userName", "없음"));
                    Log.d("PrefCheck", "userId: " + PrefManager.getLong("userId"));
                    Log.d("PrefCheck", "phone: " + PrefManager.get("phone"));
                    Log.d("PrefCheck", "photourl: " + PrefManager.get("profile_image"));

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    // finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "서버 로그인 실패(" + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserResponseDto> call,
                                  @NonNull Throwable t) {
                btnLogin.setEnabled(true);
                btnGoogleLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this,
                        "서버 통신 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ================= 구글 로그인 관련 ================= */
    private void setupGoogleSignIn() {
        // google-services.json에서 자동으로 만들어진 web client id
        // res/values/strings.xml 에 default_web_client_id 가 있어야 함
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ActivityResultLauncher 등록
        googleSignInLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task =
                                GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    }
                });

        // 버튼 클릭 → 구글 로그인 시작
        btnGoogleLogin.setOnClickListener(v -> {
            btnGoogleLogin.setEnabled(false);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // 구글 계정 → Firebase Auth 연동
            AuthCredential credential =
                    GoogleAuthProvider.getCredential(account.getIdToken(), null);

            firebaseAuth.signInWithCredential(credential)
                    .addOnSuccessListener(authResult -> {
                        firebaseAuth.getCurrentUser()
                                .getIdToken(true)
                                .addOnSuccessListener(result -> {
                                    String idToken = result.getToken();
                                    if (TextUtils.isEmpty(idToken)) {
                                        btnGoogleLogin.setEnabled(true);
                                        Toast.makeText(this,
                                                "토큰을 가져오지 못했습니다.",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    // 이메일/비번 로그인 때와 똑같이 서버 로그인 호출
                                    callServerLogin(idToken);
                                })
                                .addOnFailureListener(e -> {
                                    btnGoogleLogin.setEnabled(true);
                                    Toast.makeText(this,
                                            "토큰 획득 실패: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnGoogleLogin.setEnabled(true);
                        Toast.makeText(this,
                                "Google 계정 연동 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });

        } catch (ApiException e) {
            btnGoogleLogin.setEnabled(true);
            Toast.makeText(this,
                    "Google 로그인 실패: " + e.getStatusCode(),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
