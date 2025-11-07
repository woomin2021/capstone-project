// 파일: app/src/main/java/com/example/jjb20/PaymentCompleteActivity.java
package com.example.jjb20;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class PaymentCompleteActivity extends AppCompatActivity {

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_complete);

        // 툴바 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        // 완료/확인 버튼이 있다면 닫기 (여러 후보 id 지원)
        View btn = findFirst("btnDone", "btnOk", "btnFinish", "btnHome");
        if (btn != null) btn.setOnClickListener(v -> finish());
    }

    private View findFirst(String... ids) {
        for (String name : ids) {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v != null) return v;
            }
        }
        return null;
    }
}