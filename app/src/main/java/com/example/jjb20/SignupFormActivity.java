package com.example.jjb20;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.RegisterRequestDto;
import com.example.jjb20.dto.UserResponseDto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * 회원가입 3단계 :
 *  - 이름/생년월일/전화번호/이메일/비밀번호 입력
 *  - 프로필 사진 등록
 *  - 1단계: Firebase 계정 생성 + 이메일 인증 메일 발송
 *  - 2단계: 이메일 인증 완료 확인 → 서버 register 호출
 */
public class SignupFormActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etFirstName, etLastName, etBirth, etEmail, etPassword, etPhone;
    private CheckBox cbConsent;
    private MaterialButton btnNext;

    private CardView btnAddPhoto;
    private ImageView imgProfilePhoto;
    private Uri selectedImageUri;
    private StorageReference storageReference;
    private TextView tvProfilePhotoHint;

    private FirebaseAuth auth;
    private ApiService api;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

//    private String phoneFromPrev;  // 이전 단계에서 받은 전화번호 미쳤지 이거 누구야 ㅅㅂ
    private boolean verificationMailSent = false;

    private String fullName;
    private String fullphone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_form);

        auth = FirebaseAuth.getInstance();
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        btnBack     = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etBirth     = findViewById(R.id.etBirth);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        cbConsent   = findViewById(R.id.cbConsent);
        etPhone = findViewById(R.id.etphone);
        tvProfilePhotoHint = findViewById(R.id.tvProfilePhotoHint);


        btnNext = findViewById(R.id.btnNext);
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        btnAddPhoto     = findViewById(R.id.cardProfilePhoto);

        btnBack.setOnClickListener(v -> finish());



        // 사진 선택
        btnAddPhoto.setOnClickListener(v -> openGallery());

        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // 카드뷰 전체에 사진 채우기
                        tvProfilePhotoHint.setVisibility(View.GONE);
                        imgProfilePhoto.setPadding(0,0,0,0); // 아이콘 padding 제거
                        imgProfilePhoto.setColorFilter(null); // tint 제거
                        Glide.with(this).load(uri).into(imgProfilePhoto);
                        uploadImageToFirebase();
                    } else {
                        Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // "다음" 버튼: 1단계 → 2단계
        btnNext.setOnClickListener(v -> {
            if (!verificationMailSent) startSignupAndSendEmail();
            else completeSignupIfEmailVerified();
        });
    }

    // -------------------------
    // 1단계: 계정 생성 + 인증 메일 발송
    // -------------------------
    //TODO. user 테이블 생년월일 추가
    private void startSignupAndSendEmail() {

        String firstName = safeText(etFirstName);
        String lastName  = safeText(etLastName);
        String birth     = safeText(etBirth);
        String email     = safeText(etEmail);
        String password  = safeText(etPassword);
        String phone = safeText(etPhone);
        String imageUrl  = PrefManager.get("profile_image_url");

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "이름과 성을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(birth) || birth.length() != 6) {
            Toast.makeText(this, "생년월일 6자리를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!cbConsent.isChecked()) {
            Toast.makeText(this, "개인정보 수집에 동의해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (imageUrl == null) imageUrl = "";

        // 서버 요청 때 사용할 값 저장
        fullName = lastName + firstName;
        fullphone = phone;

        btnNext.setEnabled(false);

        // Firebase 계정 생성
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    if (auth.getCurrentUser() == null) {
                        btnNext.setEnabled(true);
                        Toast.makeText(this, "사용자 정보를 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    auth.getCurrentUser().sendEmailVerification()
                            .addOnSuccessListener(unused -> {
                                btnNext.setEnabled(true);
                                verificationMailSent = true;

                                Toast.makeText(this,
                                        "인증메일을 발송했습니다.\n메일에서 인증을 완료한 뒤 버튼을 다시 눌러주세요.",
                                        Toast.LENGTH_LONG).show();

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

    // -------------------------
    // 2단계: 이메일 인증 확인 → 서버 회원가입
    // -------------------------
    private void completeSignupIfEmailVerified() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnNext.setEnabled(false);

        auth.getCurrentUser().reload()
                .addOnSuccessListener(unused -> {

                    if (!auth.getCurrentUser().isEmailVerified()) {
                        btnNext.setEnabled(true);
                        Toast.makeText(this,
                                "이메일 인증이 아직 완료되지 않았습니다.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // 인증 완료 → ID 토큰 발급
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

                                RegisterRequestDto dto = new RegisterRequestDto(
                                        idToken,
                                        fullName,
                                        fullphone
                                );
                                dto.profileImageUrl = PrefManager.get("profile_image_url");

                                api.register(dto).enqueue(new Callback<UserResponseDto>() {
                                    @Override
                                    public void onResponse(@NonNull Call<UserResponseDto> call,
                                                           @NonNull Response<UserResponseDto> response) {

                                        btnNext.setEnabled(true);

                                        if (response.isSuccessful() && response.body() != null) {
                                            Toast.makeText(SignupFormActivity.this,
                                                    "가입 완료! " + response.body().name + "님 환영합니다.",
                                                    Toast.LENGTH_LONG).show();
                                            finish();
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
                                        "토큰 발급 실패: " + e.getMessage(),
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

    private AlertDialog progressDialog;
    // -------------------------
    // 프로필 사진 업로드
    // -------------------------
    private void uploadImageToFirebase() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "업로드할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = safeText(etEmail);
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "사진을 업로드하려면 이메일을 먼저 입력해야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show custom progress dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.dialog_progress);
        progressDialog = builder.create();
        progressDialog.show();


        String sanitizedEmail = email.replace("@", "_").replace(".", "_");
        String filename = sanitizedEmail + "_profile_" + System.currentTimeMillis();


        storageReference = FirebaseStorage.getInstance()
                .getReference("profile_image/" + filename);

        storageReference.putFile(selectedImageUri)
                .continueWithTask(task -> storageReference.getDownloadUrl())
                .addOnSuccessListener(uri -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    PrefManager.put("profile_image_url", uri.toString());
                    Toast.makeText(this, "사진 업로드 완료", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String safeText(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void openGallery() {
        pickMedia.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }
}

