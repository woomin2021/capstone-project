package com.example.jjb20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import com.example.jjb20.entity.HotelItem;
import com.google.android.material.appbar.MaterialToolbar;

// Google Map import
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class RentHouseDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_HOUSE  = "house";
    public static final String EXTRA_DATE   = "extra_date";
    public static final String EXTRA_GUESTS = "extra_guests";
    public static final String EXTRA_PRICE  = "extra_price";

    private ViewPager2 viewPagerImages;

    private TextView tvTitle, tvLocation, tvPrice;
    private HotelItem item;
    private String selectedDateText = "";

    // 데모 좌표(제주). 실제로는 숙소 주소→좌표 변환 후 주입하세요.
    private static final LatLng DEMO_LATLNG = new LatLng(33.450701, 126.570667);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house_detail_page);

        viewPagerImages = findViewById(R.id.viewPagerImages);

        tvTitle    = findAnyTextView("textTitle", "textTitleDetail", "tvTitle");
        tvLocation = findAnyTextView("textLocation", "textLocationDetail", "tvLocation");
        tvPrice    = findAnyTextView("textPrice", "textPriceDetail", "tvPrice");

        item = (HotelItem) getIntent().getSerializableExtra(EXTRA_HOUSE);

        if (item != null) {
            if (tvTitle != null)    tvTitle.setText(item.getTitle());
            if (tvLocation != null) tvLocation.setText(item.getLocation());
            if (tvPrice != null)    tvPrice.setText(item.getPrice());

            List<Integer> images = new ArrayList<>();
            images.add(item.getImageRes());
            ImageSliderAdapter adapter = new ImageSliderAdapter(images);
            viewPagerImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            viewPagerImages.setAdapter(adapter);
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        View seg1 = findViewById(R.id.seg1);
        View seg2 = findViewById(R.id.seg2);

        CalendarView calendarView = findViewById(R.id.calendarView);
        AppCompatButton btnNext = findViewById(R.id.btnNext);
        if (btnNext != null) btnNext.setEnabled(false);

        if (calendarView != null && btnNext != null) {
            calendarView.setOnDateChangeListener((view, y, m, d) -> {
                selectedDateText = String.format("%d년 %d월 %d일", y, m + 1, d);
                btnNext.setEnabled(true);
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                int GREEN = Color.parseColor("#14D8B4");
                int GRAY  = getColor(R.color.basic2);
                if (seg1 != null) seg1.setBackgroundColor(GRAY);
                if (seg2 != null) seg2.setBackgroundColor(GREEN);

                String priceText = (item != null && item.getPrice() != null)
                        ? item.getPrice() : "90,000원 · 3박";
                Intent i = new Intent(this, ReserveConfirmActivity.class);
                i.putExtra(EXTRA_DATE,   selectedDateText.isEmpty() ? "12월 15일 ~ 12월 18일" : selectedDateText);
                i.putExtra(EXTRA_GUESTS, "성인 1명");
                i.putExtra(EXTRA_PRICE,  priceText);
                startActivity(i);
            });
        }

        // ===== Google Map 준비 =====
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // 지도 준비 완료 콜백
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        String title = (item != null && item.getTitle()!=null) ? item.getTitle() : "숙소 위치";

        // 마커 & 카메라
        googleMap.addMarker(new MarkerOptions()
                .position(DEMO_LATLNG)
                .title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEMO_LATLNG, 15f));

        // UI 옵션(필요시)
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }

    private TextView findAnyTextView(String... ids) {
        for (String id : ids) {
            int resId = getResources().getIdentifier(id, "id", getPackageName());
            if (resId != 0) {
                TextView tv = findViewById(resId);
                if (tv != null) return tv;
            }
        }
        return null;
    }
}