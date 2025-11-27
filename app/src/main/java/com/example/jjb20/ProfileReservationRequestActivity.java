package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.jjb20.adapter.ReservationListAdapter;
import com.example.jjb20.chat.ChatMsgFragment;
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
    private final ArrayList<ReservationDTO> requestList = new ArrayList<>();

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

        // 리스트 아이템 클릭 시 동작
        adapter = new ReservationListAdapter(this, requestList, item -> {
            switch (item.status) {
                case "IN_PROGRESS":
                    // 🔥 대기 중 예약: 승인/거절/채팅 다이얼로그
                    showPendingReservationDialog(item);
                    break;

                case "COMPLETED":
                    // 🔥 이용 완료 → 게스트 리뷰 작성
                    if (item.hasGuestReview) {
                        Toast.makeText(this,
                                "이미 리뷰를 작성한 게스트입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(this, GuestReviewActivity.class);
                    intent.putExtra("reservation", item);
                    intent.putExtra("reservationId", item.id);
                    startActivity(intent);
                    break;

                default:
                    Toast.makeText(this,
                            "이 상태에서는 할 수 있는 동작이 없습니다. (" + item.status + ")",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
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
        // PAST = COMPLETED + UNCOMPLETED
        fetchDataByStatus("PAST", "getPastRequestsForHost");
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
                    Toast.makeText(ProfileReservationRequestActivity.this,
                            "서버 오류: " + response.code(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(ProfileReservationRequestActivity.this,
                        "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // IN_PROGRESS 예약 클릭 시 뜨는 다이얼로그
    private void showPendingReservationDialog(ReservationDTO item) {
        // 1) 커스텀 뷰 inflate
        View view = getLayoutInflater().inflate(R.layout.dialog_reservation_actions, null);

        ImageView imgGuestProfile = view.findViewById(R.id.imgGuestProfile);
        TextView txtGuestName = view.findViewById(R.id.txtGuestName);
        TextView txtDateRange = view.findViewById(R.id.txtDateRange);
        AppCompatButton btnReject = view.findViewById(R.id.btnReject);
        AppCompatButton btnApprove = view.findViewById(R.id.btnApprove);
        AppCompatButton btnChat = view.findViewById(R.id.btnChat);

        // 2) 데이터 바인딩
        String guestName = (item.guestName != null ? item.guestName : ("게스트 #" + item.guestUserId));
        txtGuestName.setText(guestName);
        txtDateRange.setText(item.checkinDate + " ~ " + item.checkoutDate);

        // 프로필 이미지 (Glide 사용)
        if (item.guestProfileImageUrl != null && !item.guestProfileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(item.guestProfileImageUrl)
                    .placeholder(R.drawable.profile_image)
                    .circleCrop()
                    .into(imgGuestProfile);
        } else {
            imgGuestProfile.setImageResource(R.drawable.profile_image);
        }

        // 3) 다이얼로그 생성
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        // 4) 버튼 클릭 리스너
        btnApprove.setOnClickListener(v -> {
            approveReservation(item.id);
            dialog.dismiss();
        });

        btnReject.setOnClickListener(v -> {
            rejectReservation(item.id);
            dialog.dismiss();
        });

        btnChat.setOnClickListener(v -> {
            openChatWithGuest(item);
            dialog.dismiss();
        });

        dialog.show();
    }


    // 🔹 예약 승인
    private void approveReservation(long reservationId) {
        String idToken = PrefManager.get("idToken", null);
        if (idToken == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String bearerToken = "Bearer " + idToken;

        api.approveReservation(reservationId, bearerToken)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(ProfileReservationRequestActivity.this,
                                    "승인 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(ProfileReservationRequestActivity.this,
                                "예약을 승인했습니다.", Toast.LENGTH_SHORT).show();
                        // 🔥 목록 새로고침 (대기 탭)
                        loadInProgressRequests();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ProfileReservationRequestActivity.this,
                                "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 예약 거절
    private void rejectReservation(long reservationId) {
        String idToken = PrefManager.get("idToken", null);
        if (idToken == null) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String bearerToken = "Bearer " + idToken;

        api.rejectReservation(reservationId, bearerToken)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (!response.isSuccessful()) {
                            Toast.makeText(ProfileReservationRequestActivity.this,
                                    "거절 실패: " + response.code(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(ProfileReservationRequestActivity.this,
                                "예약을 거절했습니다.", Toast.LENGTH_SHORT).show();
                        // 🔥 목록 새로고침
                        loadInProgressRequests();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(ProfileReservationRequestActivity.this,
                                "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔹 게스트와 채팅 열기 (DM 룸 생성 방식)
    private void openChatWithGuest(ReservationDTO item) {
        // 내 userId (정수형) 가져오기
        long myUserId = PrefManager.getInt("userId", -1);
        if (myUserId <= 0 || item.guestUserId <= 0) {
            Toast.makeText(this,
                    "채팅 정보를 불러올 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        // 항상 같은 두 사람은 같은 방 이름을 갖도록 정렬
        long a = Math.min(myUserId, item.guestUserId);
        long b = Math.max(myUserId, item.guestUserId);
        String roomName = "dm_" + a + "_" + b;   // 예: dm_1_5

        View container = findViewById(R.id.chat_container);
        if (container == null) {
            // 만약 이 Activity에 chat_container가 없다면,
            // ChatActivity 같은 곳으로 roomName을 넘겨서 열도록 처리할 수도 있음.
            Toast.makeText(this,
                    "채팅 영역이 설정되어 있지 않습니다.(chat_container 없음)",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        container.setVisibility(View.VISIBLE);

        ChatMsgFragment chatFragment = ChatMsgFragment.newInstance(roomName);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chat_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}
