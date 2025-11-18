package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.adapter.ReservationListAdapter;
import com.example.jjb20.dto.ReservationDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileReservationListActivity extends AppCompatActivity {

    private static final String TAG = "ProfileReservationList";

    private RecyclerView recyclerView;
    private ReservationListAdapter adapter;
    private ArrayList<ReservationDTO> reservationList = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_house_reservation);

        api = RetrofitClient.getInstance().create(ApiService.class);

        recyclerView = findViewById(R.id.reservationListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservationListAdapter(this, reservationList, item -> {
            // 클릭 → 리뷰 작성 화면 이동
            Intent intent = new Intent(ProfileReservationListActivity.this, HouseReviewActivity.class);
            intent.putExtra("reservation", item); // DTO 통째로 넘긴다
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        loadReservations();
    }

    private void loadReservations() {

        int myUserId = PrefManager.getInt("userId", -1);

        if (myUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "예약 목록 요청 시작 /api/reservations/my?userId=" + myUserId);

        api.getReservations(myUserId).enqueue(new Callback<List<ReservationDTO>>() {
            @Override
            public void onResponse(Call<List<ReservationDTO>> call, Response<List<ReservationDTO>> response) {

                Log.d(TAG, "응답 코드 = " + response.code());

                if (!response.isSuccessful()) {
                    Toast.makeText(ProfileReservationListActivity.this,
                            "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ReservationDTO> list = response.body();

                if (list != null) {
                    reservationList.clear();
                    reservationList.addAll(list);
                    adapter.notifyDataSetChanged();
                }

                if (list != null) {
                    for (ReservationDTO r : list) {
                        Log.d(TAG, "예약 ID = " + r.id + ", houseId=" + r.houseId);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                Toast.makeText(ProfileReservationListActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
