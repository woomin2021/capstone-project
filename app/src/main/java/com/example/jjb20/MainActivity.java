package com.example.jjb20;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.jjb20.dto.HostProfileDto;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.activity.OnBackPressedCallback;

public class MainActivity extends AppCompatActivity {

    // TODO
    // 1. 예약 방법 radiobutton 하나만 선택할수 있게 변경
    // 2. 예약 완료 시 상태 변경 및 빌리기 리스트에 안뜨게 하기
    // 3. 프로필 수정 구현
    // 4. 프로필 이미지 업로드?
    // 5. 채팅 시 이름 불러오기
    // 6. 집 등록하기, 집 빌리기 진행도 바 통일
    // 7. 폰트 고민
    // 8. 집 정보 수정
    // 9. 회원가입 시 전화번호 저장
    // 10 메인에서 뒤로가기 시 로그인 화면으로 이동 x
    // 11. 로그아웃 기능 구현
    // 12. 전화번호로 회원가입
    // 13. 회원가입 비밀번호 표시 없애기
    // 14. 예약 내역 지난예약, 예약 내역 나누기
    // 15. 프로필에 집 개수 예약 개수 뜨게 하기
    // 16. 모든 뒤로가기 버튼 체크
    // 17. 프로필 수정 이메일 2번 하고 뒤로갔다 올시 팅김
    // 18 .비밀번호 변경 시 현재 비밀번호 확인

    private MaterialCardView btnRent;
    private MaterialCardView btnRegister;
    private ImageView btnProfile;

    Button btntest1, btntest2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // 메인 화면에서 뒤로가기(버튼/제스처) 막기
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 아무 것도 안 하면 뒤로가기 동작 자체가 막힘
                // 필요하면 여기서 "뒤로가기가 안됩니다" 토스트 띄워도 됨
                // Toast.makeText(MainActivity.this, "메인 화면에서는 뒤로가기가 동작하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        };

// 이 액티비티의 back dispatcher에 콜백 등록
        getOnBackPressedDispatcher().addCallback(this, callback);

        btnRent = findViewById(R.id.btnRent); //집 빌리기
        btnRegister = findViewById(R.id.btnRegister); // 집 등록하기
        btnProfile = findViewById(R.id.btn_profile); // 프로필 버튼

//        //로그인 버튼 테스트용
//        Button btnLoginTest = findViewById(R.id.testLoginbtn);
//        btnLoginTest.setOnClickListener(v -> {
//            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//            startActivity(intent);
//        });

        btnRent.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RentHouseActivity.class);
            startActivity(intent);
        });

        //혜진님꺼 안합쳐서 여기 안바뀜 아직
        //호스트 등록하기 조건부로 넘기기
        btnRegister.setOnClickListener(v -> {
//            Intent intent = new Intent(getApplicationContext(), RegisterTitleActivity.class);
//            startActivity(intent);
            checkHostProfile();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });



    }
    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        // 뒤로가기 막기
        super.onBackPressed();
    }

    private void checkHostProfile() {
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.getMyHostProfile().enqueue(new Callback<HostProfileDto>() {
            @Override
            public void onResponse(Call<HostProfileDto> call, Response<HostProfileDto> response) {

                if (response.isSuccessful()) {
                    // 200 OK → 호스트 프로필 있음
                    Log.d("checkHost", "호스트 프로필 있음");
                    Intent intent = new Intent(getApplicationContext(), RegisterTitleActivity.class);
                    startActivity(intent);
                    return;
                }

                if (response.code() == 404) {
                    // 404 → 호스트 프로필 없음
                    Log.d("checkHost", "호스트 프로필 없음");
                    Intent intent = new Intent(getApplicationContext(), RegisterHostActivity.class);
                    startActivity(intent);
                    return;
                }

                // 그 외 다른 오류
                Log.e("checkHost", "기타 오류: " + response.code());
                Toast.makeText(MainActivity.this, "알 수 없는 오류", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<HostProfileDto> call, Throwable t) {
                Toast.makeText(MainActivity.this, "서버 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }

}