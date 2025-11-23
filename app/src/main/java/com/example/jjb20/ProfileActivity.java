package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private LinearLayout houseListBtn, reservationListBtn;

    Button profileEditbtn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        houseListBtn = findViewById(R.id.houseList);
        reservationListBtn = findViewById(R.id.reservationList);
        profileEditbtn = findViewById(R.id.profileEditbtn);

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
}
