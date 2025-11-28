package com.example.jjb20;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jjb20.adapter.HouseListAdapter;
import com.example.jjb20.dto.HouseDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileHouseListActivity extends AppCompatActivity {

    private static final String TAG = "HouseListActivity";
    private HouseListAdapter adapter;
    private ApiService apiService;
    private ArrayList<HouseDto> houseList = new ArrayList<>();

    private RecyclerView rvHouseList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_house_list);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        rvHouseList = findViewById(R.id.houseListRecyclerView);
        rvHouseList.setLayoutManager(new LinearLayoutManager(this));


        adapter = new HouseListAdapter(this, houseList, new HouseListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HouseDto item) {
                // 집 정보 상세 화면으로 이동
                Intent intent = new Intent(ProfileHouseListActivity.this, RentHouseDetailActivity.class);
                intent.putExtra(RentHouseDetailActivity.EXTRA_HOUSE, item);
                startActivity(intent);
            }

            @Override
            public void onEditClick(HouseDto item) {
                Intent intent = new Intent(ProfileHouseListActivity.this, EditHouseActivity.class);
                intent.putExtra("houseId", item.id);
                intent.putExtra("houseTitle", item.title);
                intent.putExtra("houseAddress", item.addressLine1);
                intent.putExtra("houseAddressDetail", item.addressDetail);
                intent.putExtra("houseDescription", item.description);
                intent.putExtra("houseSummary", item.summary);
                intent.putExtra("houseCity", item.city);
                intent.putExtra("houseCountry", item.country);
                intent.putExtra("houseStartDay", item.startDay);
                intent.putExtra("houseEndDay", item.endDay);
                if (item.pricePerNight != null) {
                    intent.putExtra("housePrice", item.pricePerNight);
                }
                intent.putExtra("houseCoverPhoto", item.coverPhotoUrl);
                startActivity(intent);
            }
        });
        rvHouseList.setAdapter(adapter);

        loadMyHouses();
    }

    /** 내 집 목록만 가져오기 */
    private void loadMyHouses() {

        int myUserId = PrefManager.getInt("userId", -1);

        if (myUserId == -1) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, " 서버에 요청 시작: GET /api/houses/my/" + myUserId);

        apiService.getMyHouses(myUserId).enqueue(new Callback<List<HouseDto>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<List<HouseDto>> call, Response<List<HouseDto>> response) {

                Log.d(TAG, " 서버 응답 코드 = " + response.code());

                if (!response.isSuccessful()) {
                    Toast.makeText(ProfileHouseListActivity.this,
                            "서버 오류: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                List<HouseDto> list = response.body();

                if (list != null) {
                    houseList.clear();
                    houseList.addAll(list);

                    // ★ 데이터가 변경되었음을 알림
                    adapter.notifyDataSetChanged();

                    Log.d(TAG, "받은 집 개수 = " + list.size());
                }

                if (list != null) {
                    for (HouseDto h : list) {
                        Log.d(TAG, "➡ ID: " + h.id +
                                ", title: " + h.title +
                                ", price: " + h.pricePerNight +
                                ", city: " + h.city +
                                ", cover: " + h.coverPhotoUrl);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<HouseDto>> call, Throwable t) {
                Toast.makeText(ProfileHouseListActivity.this,
                        "네트워크 오류: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
