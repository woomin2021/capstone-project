package com.example.jjb20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.adapter.HotAdapter;
import com.example.jjb20.adapter.RecommendedAdapter;
import com.example.jjb20.chat.ChatRoomFragment;
import com.example.jjb20.dto.HouseDto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
public class RentHouseActivity extends AppCompatActivity {

    private RecyclerView recyclerRecommended;
    private RecyclerView recyclerHot;
    private Button chattingButton;

    private ApiService api;

    // 전체 숙소 리스트
    private List<HouseDto> allHouses = new ArrayList<>();
    private HotAdapter hotAdapter;


    // 위치 관련
    private FusedLocationProviderClient fusedClient;
    private Double userLat = null;
    private Double userLng = null;

    // 반경 50km 안만 근처로 본다
    private static final float NEARBY_RADIUS_METERS = 50_000f;
    private static final int NEARBY_MAX_COUNT = 8;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house);

        PrefManager.init(this);

        // 뷰 연결
        recyclerRecommended = findViewById(R.id.recyclerRecommended);
        recyclerHot = findViewById(R.id.recyclerHot);
        chattingButton = findViewById(R.id.chattingButton);

        TextView textGreeting   = findViewById(R.id.textGreeting);
        TextView tvAllHouses    = findViewById(R.id.tvAllHouses);
        TextView tvNearbyHouses = findViewById(R.id.tvNearbyHouses);

        EditText searchEditText = findViewById(R.id.searchEditText);
        ImageView searchButton   = findViewById(R.id.searchButton);
        ImageView profileIcon = findViewById(R.id.btnProfile);
        Button btnRegisterHouse = findViewById(R.id.btnRegisterHouse);
        searchButton.setOnClickListener(v -> {
            performSearch();
        });


        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            boolean isImeSearch = actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE;

            boolean isEnterKey = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;

            if (isImeSearch || isEnterKey) {
                performSearch();
                return true;   // 이벤트 소비 (키보드에서 더 이상 처리 안 함)
            }
            return false;
        });

        // 프로필 버튼
        profileIcon.setOnClickListener(view -> {
            Intent i = new Intent(this, ProfileActivity.class);
            Log.d("ClickTest", "프로필 버튼 클릭됨");
            startActivity(i);
        });

        // 집 등록하기 버튼
        btnRegisterHouse.setOnClickListener(view -> {
            Intent i = new Intent(this, RegisterTitleActivity.class);
            Log.d("ClickTest", "집 등록하기 버튼 클릭됨");
            startActivity(i);
        });
        // 위치 클라이언트 초기화
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        requestLocationPermission();   // 위치 권한 요청 → 성공 시 userLat/userLng 셋팅됨


        // 채팅 버튼 → 프래그먼트 열기
        chattingButton.setOnClickListener(v -> {
            chattingButton.setVisibility(android.view.View.GONE); // 버튼 숨기기
            ChatRoomFragment fragment = new ChatRoomFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_out_left);
            transaction.replace(R.id.main, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // 프래그먼트 백스택 리스너
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackEntryCount == 0) {
                // 백스택에 프래그먼트가 없으면 버튼 다시 보이기
                chattingButton.setVisibility(android.view.View.VISIBLE);
            }
        });

        // 로그인한 사용자 이름 표시
        String userName = PrefManager.get("userName", "회원");
        if (userName == null || userName.trim().isEmpty()) userName = "회원";
        textGreeting.setText(userName + "님, 여긴 어떠세요?");

        // 리사이클러뷰 레이아웃 세팅
        recyclerRecommended.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerHot.setLayoutManager(new GridLayoutManager(this, 2));

        // HotAdapter 초기화
        hotAdapter = new HotAdapter(new ArrayList<>());

        recyclerHot.setAdapter(hotAdapter);

        // Retrofit API
        Retrofit retrofit = RetrofitClient.getInstance();
        api = retrofit.create(ApiService.class);

        // 서버에서 집 목록 로딩
        loadHouses();

        // "모든 숙소" 버튼
        tvAllHouses.setOnClickListener(v -> {
            hotAdapter.updateData(allHouses);  // 전체 출력
        });

        // "근처 추천 숙소" 버튼
        tvNearbyHouses.setOnClickListener(v -> {
            Log.d("RentHouse", "근처 버튼 클릭, userLat=" + userLat + ", userLng=" + userLng);
            List<HouseDto> nearby = getNearbyHouses(allHouses);
            Log.d("RentHouse", "nearby size=" + nearby.size());
            hotAdapter.updateData(nearby);
        });

    }

    private void performSearch() {
        String keyword = ((EditText) findViewById(R.id.searchEditText))
                .getText().toString().trim();

        if (keyword.isEmpty()) {
            // 검색어 없을 때 전체 숙소 출력
            hotAdapter.updateData(allHouses);
            return;
        }

        // 검색 필터 실행
        List<HouseDto> filtered = filterHouses(keyword);
        hotAdapter.updateData(filtered);
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
                    hotAdapter.updateData(new ArrayList<>());
                    return;
                }

                List<HouseDto> houses = response.body();

                List<HouseDto> recommended = new ArrayList<>();

                // 추천 숙소: 앞에 최대 4개만
                int recommendCount = Math.min(4, houses.size());
                for (int i = 0; i < recommendCount; i++) {
                    recommended.add(houses.get(i));
                }

                // 전체 숙소는 allHouses에 저장
                allHouses.clear();
                allHouses.addAll(houses);

                // 어댑터 세팅
                recyclerRecommended.setAdapter(new RecommendedAdapter(recommended));
                hotAdapter.updateData(allHouses);   // 기본은 “모든 숙소”



            }

            @Override
            public void onFailure(Call<List<HouseDto>> call, Throwable t) {
                Log.e("RentHouse", "houses load error", t);
                recyclerRecommended.setAdapter(new RecommendedAdapter(new ArrayList<>()));
                hotAdapter.updateData(new ArrayList<>());
            }
        });

    }


    private static final int REQ_LOCATION = 1001;

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_LOCATION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.d("RentHouse", "테스트용 위치: " + userLat + ", " + userLng);
        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();

                        userLng = location.getLongitude();
                        Log.d("RentHouse", "내 위치: " + userLat + ", " + userLng);
                    }
                    else {
                        Log.d("RentHouse", "location null");
                        Log.d("RentHouse", "기본 위치: " + userLat + ", " + userLng);
                    }
                });
    }

    private List<HouseDto> getNearbyHouses(List<HouseDto> houses) {

        if (userLat == null || userLng == null) {
            // 위치 못 얻은 경우 그냥 전체 보여줄지,
            // 아니면 빈 리스트로 할지는 선택
            return houses;
        }

        List<HouseDistance> temp = new ArrayList<>();

        for (HouseDto h : houses) {
            if (h.latitude == null || h.longitude == null) continue;

            float[] dist = new float[1];
            Location.distanceBetween(
                    userLat, userLng,
                    h.latitude, h.longitude,
                    dist
            );

            // 여기서 반경 안에 들어오는 것만 추가
            if (dist[0] <= NEARBY_RADIUS_METERS) {
                temp.add(new HouseDistance(h, dist[0]));
            }
        }

        // 반경 안에 아무것도 없으면 어떻게 할지
        if (temp.isEmpty()) {
            // 근처 숙소 없을 때 전체 보여주고 싶으면:
            return houses;

            // 아무것도 안 보이게 하고 싶으면 위 대신:
            // return new ArrayList<>();
        }

        Collections.sort(temp, (a, b) -> Float.compare(a.distance, b.distance));

        List<HouseDto> result = new ArrayList<>();
        for (int i = 0; i < Math.min(NEARBY_MAX_COUNT, temp.size()); i++) {
            result.add(temp.get(i).house);
        }

        return result;
    }

    private static class HouseDistance {
        HouseDto house;
        float distance;

        HouseDistance(HouseDto h, float d) {
            house = h;
            distance = d;
        }
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
    private List<HouseDto> filterHouses(String keyword) {
        List<HouseDto> result = new ArrayList<>();

        for (HouseDto h : allHouses) {
            boolean matchTitle = h.title != null && h.title.toLowerCase().contains(keyword.toLowerCase());
            boolean matchCity = h.city != null && h.city.toLowerCase().contains(keyword.toLowerCase());
            boolean matchAddress = h.addressLine1 != null && h.addressLine1.toLowerCase().contains(keyword.toLowerCase());

            if (matchTitle || matchCity || matchAddress) {
                result.add(h);
            }
        }

        return result;
    }
    private String safe(String s) {
        return s == null ? "" : s;
    }
}