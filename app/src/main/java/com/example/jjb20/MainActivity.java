package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

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
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RegisterTitleActivity.class);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(intent);
        });



    }

}