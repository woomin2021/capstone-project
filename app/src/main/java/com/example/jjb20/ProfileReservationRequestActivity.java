package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class ProfileReservationRequestActivity extends AppCompatActivity {

    private static final String TAG = "ProfileReservationRequest";

    private RecyclerView recyclerView;
    private ReservationListAdapter adapter;
    private ArrayList<ReservationDTO> requestList = new ArrayList<>();

    private ApiService api;

    private AppCompatButton btnInProgress;
    private AppCompatButton btnTradeDone;
    private AppCompatButton btnPast;

    // 0: IN_PROGRESS, 1: TRADE_DONE, 2: Past (COMPLETED, UNCOMPLETED)
    private int currentTab = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_reservation_request);

        api = RetrofitClient.getInstance().create(ApiService.class);

        recyclerView = findViewById(R.id.requestListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ReservationListAdapter(this, requestList, item -> {
            if (!"COMPLETED".equals(item.status)) {
                String msg = "이용이 완료된 게스트만 리뷰를 작성할 수 있습니다.";
                Toast.makeText(ProfileReservationRequestActivity.this, msg, Toast.LENGTH_SHORT).show();
                return;
            }
            if (item.hasGuestReview) {
                Toast.makeText(ProfileReservationRequestActivity.this,
                        "이미 리뷰를 작성한 게스트입니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(ProfileReservationRequestActivity.this, GuestReviewActivity.class);
            intent.putExtra("reservation", item);
            intent.putExtra("reservationId", item.id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnInProgress = findViewById(R.id.btnCurrentRequests);
        btnTradeDone = findViewById(R.id.btnTradeDoneRequests);
        btnPast = findViewById(R.id.btnPastRequests);

        btnInProgress.setOnClickListener(v -> {
            if (currentTab != 0) {
                currentTab = 0;
                updateTabUi();
                loadInProgressRequests();
            }
        });

        btnTradeDone.setOnClickListener(v -> {
            if (currentTab != 1) {
                currentTab = 1;
                updateTabUi();
                loadTradeDoneRequests();
            }
        });

        btnPast.setOnClickListener(v -> {
            if (currentTab != 2) {
                currentTab = 2;
                updateTabUi();
                loadPastRequests();
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        updateTabUi();
        loadInProgressRequests(); // Initial load
    }

    private void updateTabUi() {
        // Reset all buttons
        btnInProgress.setAlpha(0.5f);
        btnInProgress.setEnabled(true);
        btnTradeDone.setAlpha(0.5f);
        btnTradeDone.setEnabled(true);
        btnPast.setAlpha(0.5f);
        btnPast.setEnabled(true);

        // Highlight the selected tab
        switch (currentTab) {
            case 0:
                btnInProgress.setAlpha(1.0f);
                btnInProgress.setEnabled(false);
                break;
            case 1:
                btnTradeDone.setAlpha(1.0f);
                btnTradeDone.setEnabled(false);
                break;
            case 2:
                btnPast.setAlpha(1.0f);
                btnPast.setEnabled(false);
                break;
        }
    }

    private void loadInProgressRequests() {
        fetchDataByStatus("IN_PROGRESS", "getInProgressRequestsForHost");
    }

    private void loadTradeDoneRequests() {
        fetchDataByStatus("TRADE_DONE", "getTradeDoneRequestsForHost");
    }

    private void loadPastRequests() {
        fetchDataByStatus("PAST", "getPastRequestsForHost"); // PAST represents COMPLETED and UNCOMPLETED
    }

    private void fetchDataByStatus(String status, String apiMethodName) {
        // 🔹 userId 대신, Firebase ID Token 사용
        String idToken = PrefManager.get("idToken", null);
        if (idToken == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String bearerToken = "Bearer " + idToken;

        Log.d(TAG, "Loading requests with status: " + status);

        Call<List<ReservationDTO>> call;

        switch (apiMethodName) {
            case "getInProgressRequestsForHost":
                call = api.getInProgressReservationsForHost(bearerToken);
                break;
            case "getTradeDoneRequestsForHost":
                call = api.getTradeDoneReservationsForHost(bearerToken);
                break;
            case "getPastRequestsForHost":
            default:
                call = api.getPastReservationsForHost(bearerToken);
                break;
        }

        call.enqueue(new Callback<List<ReservationDTO>>() {
            @Override
            public void onResponse(Call<List<ReservationDTO>> call, Response<List<ReservationDTO>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(ProfileReservationRequestActivity.this, "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ReservationDTO> list = response.body();
                requestList.clear();
                if (list != null) {
                    requestList.addAll(list);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ReservationDTO>> call, Throwable t) {
                Toast.makeText(ProfileReservationRequestActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
