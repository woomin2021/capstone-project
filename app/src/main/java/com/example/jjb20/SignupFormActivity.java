package com.example.jjb20;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
 * 회원가입 3단계 : 이름/생년월일/이메일/비밀번호 입력
 *  - Firebase 이메일/비번 계정 생성
 *  - ID 토큰을 받아서 서버 /api/auth/register 로 전송 → DB에 Users 레코드 생성
 */
public class SignupFormActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputEditText etFirstName, etLastName, etBirth, etPhone, etEmail, etPassword;
    private CheckBox cbConsent;
    private CardView btnAddPhoto;
    private MaterialButton btnNext;
    private Uri selectedImageUri;   // 갤러리에서 선택한 1장의 사진
    private StorageReference storageReference;
    private ImageView imgProfilePhoto;
    private FirebaseAuth auth;
    private ApiService api;
    private String phoneFromPrev; // 2단계에서 넘긴 전화번호
    // 갤러리에서 이미지를 선택하기 위한 최신 방식 (ActivityResultLauncher)
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private void uploadImageToFirebase() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "업로드할 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading File...");
        progressDialog.show();

        // 파일명 생성
        String userId = PrefManager.get("uid", "unknown");

        if (userId.isEmpty()) userId = "임시_ID";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
        String fileName = userId + "_Profile_Image_" + sdf.format(new Date());

        storageReference = FirebaseStorage.getInstance()
                .getReference("profile_image/" + fileName);

        storageReference.putFile(selectedImageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // 업로드 성공 → 다운로드 URL 받기
                    return storageReference.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    // DB서버에 이미지 URL 저장
                    String imageUrl = downloadUri.toString();
                    PrefManager.put("profile_image_url", imageUrl);

                    Toast.makeText(this,
                            "성공적으로 업로드 되었습니다.",
                            Toast.LENGTH_SHORT).show();

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "업로드 실패했습니다: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();

                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
    }


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
        etPhone = findViewById(R.id.etphone);

        phoneFromPrev = getIntent().getStringExtra("phone");

        btnBack.setOnClickListener(v -> finish());

        // "다음(가입)" 버튼
        btnNext.setOnClickListener(v -> doSignUp());

        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        btnAddPhoto = findViewById(R.id.cardProfilePhoto);
        btnAddPhoto.setOnClickListener(v -> {
            openGallery();
        });
//  갤러리 런처 초기화 (onCreate 내부에 추가)
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;

                        imgProfilePhoto.clearColorFilter();
                        imgProfilePhoto.setImageURI(uri);
                        // 사진 업로드
                        uploadImageToFirebase();
                        Toast.makeText(this, "사진 선택됨", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    private void doSignUp() {
        String firstName = safeText(etFirstName);
        String lastName  = safeText(etLastName);
        String birth     = safeText(etBirth);
        String email     = safeText(etEmail);
        String password  = safeText(etPassword);
        String phone = safeText(etPhone);
        String imageUrl = PrefManager.get("profile_image_url");

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
        if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "전화번호를 입력하세요", Toast.LENGTH_SHORT).show();
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
        if (imageUrl == null) {
            imageUrl = ""; // 빈 문자열로 처리
        }


        String fullName = lastName + firstName; // "홍" + "길동" → "홍길동"
//        String phone = phoneFromPrev; // 전화번호는 2단계에서 검증해둔 값 사용

        btnNext.setEnabled(false);

        RegisterRequestDto rdto = new RegisterRequestDto();
        rdto.profileImageUrl = imageUrl;

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
                                rdto.idToken = idToken;
                                rdto.name = fullName;
                                rdto.phone = phone;

                                

                                api.register(rdto).enqueue(new Callback<UserResponseDto>() {
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

    private void openGallery() {


        // 갤러리를 열어 이미지만 선택하도록 함
        if (pickMedia != null) {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } else {
            Toast.makeText(this, "갤러리를 열 수 없습니다.", Toast.LENGTH_SHORT).show();
        }


    }
}
