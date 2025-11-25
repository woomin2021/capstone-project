package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HostProfileDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterHostActivity extends AppCompatActivity {

    TextView etBusinessNum;
    TextView etHostingPolicy;
    Button btnNext;
    ApiService api;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_host);

        etBusinessNum = findViewById(R.id.etBNum);
        etHostingPolicy = findViewById(R.id.etHostingPolicy);
        btnNext = findViewById(R.id.btnNext);

        api = RetrofitClient.getInstance().create(ApiService.class);

        btnNext.setOnClickListener(v -> {
            registerHost();
        });
    }

    private void registerHost(){

        String businessNo = etBusinessNum.getText().toString().trim();
        String hostingPolicy = etHostingPolicy.getText().toString().trim();

        if (businessNo.isEmpty() || hostingPolicy.isEmpty()) {
            Toast.makeText(this, "모든 내용을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String realName = PrefManager.get("userName");

        HostProfileDto dto = new HostProfileDto(realName, businessNo, hostingPolicy);

        api.registerHostProfile(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(RegisterHostActivity.this, "호스트 등록 완료!", Toast.LENGTH_SHORT).show();

                // 집 등록 시작 화면으로 이동
                Intent intent = new Intent(RegisterHostActivity.this, RegisterTitleActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegisterHostActivity.this,
                        "서버 연결 오류", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
