package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HouseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private LinearLayout houseListBtn, reservationListBtn;

    Button profileEditbtn;
    private TextView houseCountTextView;
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        houseListBtn = findViewById(R.id.houseList);
        reservationListBtn = findViewById(R.id.reservationList);
        profileEditbtn = findViewById(R.id.profileEditbtn);
        houseCountTextView = findViewById(R.id.textHouseCount);

        apiService = RetrofitClient.getInstance().create(ApiService.class);
        PrefManager.init(getApplicationContext());
        loadHouseCount();

        houseListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileHouseListActivity.class);
            startActivity(intent);
        });

        reservationListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProfileReservationListActivity.class);
            startActivity(intent);
        });

        profileEditbtn.setOnClickListener(v -> {
//            Intent intent = new Intent()
        });
    }

    private void loadHouseCount() {
        int myUserId = PrefManager.getInt("userId", -1);
        if (myUserId == -1) {
            houseCountTextView.setText("-");
            Toast.makeText(this, "로그인 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getMyHouses(myUserId).enqueue(new Callback<List<HouseDto>>() {
            @Override
            public void onResponse(Call<List<HouseDto>> call, Response<List<HouseDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    houseCountTextView.setText("-");
                    return;
                }
                int count = response.body().size();
                houseCountTextView.setText(count + "개");
            }

            @Override
            public void onFailure(Call<List<HouseDto>> call, Throwable t) {
                houseCountTextView.setText("-");
            }
        });
    }
}
