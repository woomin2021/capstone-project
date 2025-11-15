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