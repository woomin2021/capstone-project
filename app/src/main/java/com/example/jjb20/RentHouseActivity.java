package com.example.jjb20;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.chat.ChatRoomFragment;
import com.example.jjb20.dto.HouseDto;
import com.example.jjb20.entity.HotelItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RentHouseActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommended;
    private RecyclerView recyclerHot;
    private Button chattingButton;

    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house);

        PrefManager.init(this);

        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        recyclerHot = findViewById(R.id.recyclerHot);
        chattingButton = findViewById(R.id.chattingButton);
        TextView textGreeting = findViewById(R.id.textGreeting);
        // 채팅 프래그먼트 열기
        chattingButton.setOnClickListener(v -> {
            ChatRoomFragment fragment = new ChatRoomFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_up, 0);
            transaction.replace(R.id.main, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 로그인한 사용자 이름 가져오기
        String userName = PrefManager.get("userName", "회원");


        if (userName == null || userName.trim().isEmpty()) {
            userName = "회원";
        }

        // UI에 적용
        textGreeting.setText(userName + "님, 여긴 어떠세요?");

        // 레이아웃 매니저 설정
        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerHot.setLayoutManager(new GridLayoutManager(this, 2));

        // Retrofit
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // 서버에서 집 목록 불러오기 이거 나중가면 등록 많이 되면 알아서 올라감
        loadHouses();
    }

    private void loadHouses() {
        String token = PrefManager.get("idToken", null);
        String bearer = TextUtils.isEmpty(token) ? null : "Bearer " + token;

        Call<List<HouseDto>> call =
                (bearer == null) ? api.getHouses(null) : api.getHouses(bearer);

        call.enqueue(new Callback<List<HouseDto>>() {
            @Override
            public void onResponse(Call<List<HouseDto>> call, Response<List<HouseDto>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("RentHouse", "houses load failed: " + response.code());

                    recyclerRecommended.setAdapter(new RecommendedAdapter(new ArrayList<>()));
                    recyclerHot.setAdapter(new HotAdapter(new ArrayList<>()));
                    return;
                }

                List<HouseDto> houses = response.body();

                List<HouseDto> recommended = new ArrayList<>();
                List<HouseDto> hot = new ArrayList<>();

// 추천 숙소: 앞에 최대 4개만
                int recommendCount = Math.min(4, houses.size());
                for (int i = 0; i < recommendCount; i++) {
                    recommended.add(houses.get(i));
                }

// 핫한 숙소: 전체
                hot.addAll(houses);

                recyclerRecommended.setAdapter(new RecommendedAdapter(recommended));
                recyclerHot.setAdapter(new HotAdapter(hot));
            }

            @Override
            public void onFailure(Call<List<HouseDto>> call, Throwable t) {
                Log.e("RentHouse", "houses load error", t);
                recyclerRecommended.setAdapter(new RecommendedAdapter(new ArrayList<>()));
                recyclerHot.setAdapter(new HotAdapter(new ArrayList<>()));
            }
        });
    }

    // 화면 표시 유틸
    private String buildDateRange(String start, String end) {
        if (TextUtils.isEmpty(start) && TextUtils.isEmpty(end)) return "";
        if (TextUtils.isEmpty(start)) return end;
        if (TextUtils.isEmpty(end)) return start;
        // 서버가 YYYY-MM-DD로 주는까 모르겠으면 포스트맨 get 값 줘보셈 안되면 나도 몰루
        return start + " ~ " + end;
    }

    private String buildLocation(String city, String addressLine1) {
        if (!TextUtils.isEmpty(city) && !TextUtils.isEmpty(addressLine1)) {
            return city + " · " + addressLine1;
        }
        if (!TextUtils.isEmpty(city)) return city;
        if (!TextUtils.isEmpty(addressLine1)) return addressLine1;
        return "";
    }

    private String formatPrice(Integer pricePerNight) {
        if (pricePerNight == null) return "";
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.KOREA);
        return nf.format(pricePerNight) + "원 · 1박";
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}