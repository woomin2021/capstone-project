package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
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
    private ImageView btnBack;

    private ApiService api;

    private AppCompatButton btnCurrent;
    private AppCompatButton btnPast;

    // 현재 어떤 탭인지 상태
    private boolean showingCurrent = true; // 기본: 예약 내역 탭

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_house_reservation);

        api = RetrofitClient.getInstance().create(ApiService.class);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // RecyclerView
        recyclerView = findViewById(R.id.reservationListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservationListAdapter(this, reservationList, item -> {

            // 1) 먼저 상태 체크: COMPLETED 아니면 리뷰 불가
            if (!"COMPLETED".equals(item.status)) {
                String msg;
                if (item.status == null) {
                    msg = "이 예약은 후기를 작성할 수 없어요.";
                } else {
                    switch (item.status) {
                        case "IN_PROGRESS":
                            msg = "아직 진행 중인 예약이라 후기를 작성할 수 없어요.";
                            break;
                        case "TRADE_DONE":
                            msg = "이용이 끝난 예약만 후기를 작성할 수 있어요.";
                            break;
                        case "UNCOMPLETED":
                            msg = "취소/거절된 예약은 후기를 작성할 수 없어요.";
                            break;
                        default:
                            msg = "이 예약은 후기를 작성할 수 없어요.";
                    }
                }
                Toast.makeText(ProfileReservationListActivity.this, msg, Toast.LENGTH_SHORT).show();
                return;
            }

            // 2) 상태는 COMPLETED인데 이미 리뷰 있다면 막기
            if (item.hasHouseReview) {
                Toast.makeText(ProfileReservationListActivity.this,
                        "이미 후기를 작성한 예약입니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) COMPLETED + 아직 리뷰 없음 → 리뷰 작성 화면으로 이동
            Intent intent = new Intent(ProfileReservationListActivity.this, HouseReviewActivity.class);
            intent.putExtra("reservation", item);      // DTO 통째로
            intent.putExtra("reservationId", item.id); // 별도로 id도
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnCurrent = findViewById(R.id.btnCurrentReservations);
        btnPast    = findViewById(R.id.btnPastReservations);

        btnCurrent.setOnClickListener(v -> {
            if (!showingCurrent) {
                showingCurrent = true;
                updateTabUi();
                loadCurrentReservations();
            }
        });

        btnPast.setOnClickListener(v -> {
            if (showingCurrent) {
                showingCurrent = false;
                updateTabUi();
                loadPastReservations();
            }
        });

        // 처음 화면은 "예약 내역" 탭으로
        updateTabUi();
        loadCurrentReservations();
    }

    // 탭 UI 상태 변경 (선택된 탭은 진하게, 다른 탭은 흐리게)
    private void updateTabUi() {
        if (btnCurrent == null || btnPast == null) return;

        if (showingCurrent) {
            // 현재: "예약 내역" 활성
            btnCurrent.setAlpha(1.0f);
            btnCurrent.setEnabled(false); // 이미 선택된 탭은 비활성화

            btnPast.setAlpha(0.5f);
            btnPast.setEnabled(true);
        } else {
            // 현재: "지난 내역" 활성
            btnPast.setAlpha(1.0f);
            btnPast.setEnabled(false);

            btnCurrent.setAlpha(0.5f);
            btnCurrent.setEnabled(true);
        }
    }

    // "예약 내역(진행 + 거래 완료)" 불러오기
    private void loadCurrentReservations() {
        int myUserId = PrefManager.getInt("userId", -1);

        if (myUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "현재 예약 내역 요청 /api/reservations/my/current?userId=" + myUserId);

        api.getCurrentReservations(myUserId).enqueue(new Callback<List<ReservationDTO>>() {
            @Override
            public void onResponse(Call<List<ReservationDTO>> call,
                                   Response<List<ReservationDTO>> response) {

                Log.d(TAG, "현재 예약 응답 코드 = " + response.code());

                if (!response.isSuccessful()) {
                    Toast.makeText(ProfileReservationListActivity.this,
                            "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ReservationDTO> list = response.body();

                reservationList.clear();
                if (list != null) {
                    reservationList.addAll(list);
                }
                adapter.notifyDataSetChanged();

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

    // "지난 내역(완료)" 불러오기
    private void loadPastReservations() {
        int myUserId = PrefManager.getInt("userId", -1);

        if (myUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "지난 예약 내역 요청 /api/reservations/my/past?userId=" + myUserId);

        api.getPastReservations(myUserId).enqueue(new Callback<List<ReservationDTO>>() {
            @Override
            public void onResponse(Call<List<ReservationDTO>> call,
                                   Response<List<ReservationDTO>> response) {

                Log.d(TAG, "지난 예약 응답 코드 = " + response.code());

                if (!response.isSuccessful()) {
                    Toast.makeText(ProfileReservationListActivity.this,
                            "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }

                List<ReservationDTO> list = response.body();

                reservationList.clear();
                if (list != null) {
                    reservationList.addAll(list);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                Toast.makeText(ProfileReservationListActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
