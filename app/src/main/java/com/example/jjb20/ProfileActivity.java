package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.jjb20.dto.ProfileStatsResponseDto;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout houseListBtn, reservationListBtn;
    TextView name;
    private TextView tvHouseCount, tvReserveCount;
    Button profileEditbtn;
    Button logoutBtn;
    ImageView profile_image;

    private FirebaseAuth mAuth;
    ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        // 각종 버튼
        houseListBtn = findViewById(R.id.houseList);
        reservationListBtn = findViewById(R.id.reservationList);
        profileEditbtn = findViewById(R.id.profileEditbtn);

        tvHouseCount = findViewById(R.id.tvHouseCount);
        tvReserveCount = findViewById(R.id.tvReserveCount);

        profile_image = findViewById(R.id.profile_image);
        name = findViewById(R.id.name);

        name.setText(PrefManager.get("userName"));

        String imageUrl = PrefManager.get("profile_image");
        //프로필 이미지 불러오기
        Glide.with(this)
                        .load(imageUrl)
                                .into(profile_image);

        houseListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileHouseListActivity.class);
            startActivity(intent);
        });

        reservationListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationListActivity.class);
            startActivity(intent);
        });

        profileEditbtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditProfileActivity.class);
            startActivity(intent);
        });

        
        //로그아웃
        logoutBtn = findViewById(R.id.logoutbtn);

        logoutBtn.setOnClickListener( v -> {
            mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        });
    }
}
