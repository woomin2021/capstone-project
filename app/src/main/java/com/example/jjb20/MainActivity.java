package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
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

        btntest1 = findViewById(R.id.buttontest1);

        btntest1.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RentHouseActivity.class);
            startActivity(intent);
        });

        btntest2 = findViewById(R.id.buttontest2);

        btntest2.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RentHouseDetailActivity.class);
            startActivity(intent);
        });


    }


}