package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {
    private MaterialCardView btnRent;
    private MaterialCardView btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //전역 SharedPreference 초기화
        PrefManager.init(this);
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnRent = findViewById(R.id.btnRent); //집 빌리기
        btnRegister = findViewById(R.id.btnRegister); // 집 등록하기

        //로그인 버튼 테스트용
        Button btnLoginTest = findViewById(R.id.testLoginbtn);
        btnLoginTest.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });

        btnRent.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RentHouseActivity.class);
            startActivity(intent);
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), RentHouseDetailActivity.class);
            startActivity(intent);
        });


    }

}