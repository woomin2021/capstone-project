package com.example.jjb20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jjb20.dto.HouseDetailResponseDto;
import com.google.android.material.appbar.MaterialToolbar;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class EditHouseActivity extends AppCompatActivity {

    private long houseId;
    private String houseTitle;
    private String houseAddress;
    private String houseAddressDetail;
    private String houseDescription;
    private String houseSummary;
    private String houseCity;
    private String houseCountry;
    private String houseStartDay;
    private String houseEndDay;
    private Integer housePrice;
    private String houseCoverPhoto;
    private ApiService apiService;

    private TextView tvCurrentTitle;
    private TextView tvCurrentAddress;
    private TextView tvCurrentDescription;
    private TextView tvCurrentPeriod;
    private TextView tvCurrentPrice;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        apiService = RetrofitClient.getInstance().create(ApiService.class);

        houseId = getIntent().getLongExtra("houseId", -1L);
        houseTitle = getIntent().getStringExtra("houseTitle");
        houseAddress = getIntent().getStringExtra("houseAddress");
        houseAddressDetail = getIntent().getStringExtra("houseAddressDetail");
        houseDescription = getIntent().getStringExtra("houseDescription");
        houseSummary = getIntent().getStringExtra("houseSummary");
        houseCity = getIntent().getStringExtra("houseCity");
        houseCountry = getIntent().getStringExtra("houseCountry");
        houseStartDay = getIntent().getStringExtra("houseStartDay");
        houseEndDay = getIntent().getStringExtra("houseEndDay");
        if (getIntent().hasExtra("housePrice")) {
            housePrice = getIntent().getIntExtra("housePrice", -1);
        }
        houseCoverPhoto = getIntent().getStringExtra("houseCoverPhoto");
        if (houseId == -1L) {
            Toast.makeText(this, "집 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RelativeLayout editNameButton = findViewById(R.id.edit_name_button);
        RelativeLayout editAddressButton = findViewById(R.id.edit_address_button);
        RelativeLayout editDescriptionButton = findViewById(R.id.edit_description_button);
        RelativeLayout editPeriodButton = findViewById(R.id.edit_period_button);
        RelativeLayout editPriceButton = findViewById(R.id.edit_price_button);
        RelativeLayout editPhotosButton = findViewById(R.id.edit_photos_button);
        tvCurrentTitle = findViewById(R.id.tvEditInfoTitle);
        tvCurrentAddress = findViewById(R.id.tvEditInfoAddress);
        tvCurrentDescription = findViewById(R.id.tvEditInfoDescription);
        tvCurrentPeriod = findViewById(R.id.tvEditInfoPeriod);
        tvCurrentPrice = findViewById(R.id.tvEditInfoPrice);

        updateDisplayTexts();
        loadHouseInformation();

        // 이름 수정 → RegisterHouseActivity
        editNameButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterTitleActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("houseId", houseId);
            intent.putExtra("currentTitle", houseTitle);
            startActivity(intent);
        });

        // 주소 & 설명 수정 → RegisterDetailsActivity
        editAddressButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterDetailsActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "address");  // 주소 수정을 구별
            intent.putExtra("houseId", houseId);
            intent.putExtra("currentAddress", houseAddress);
            intent.putExtra("currentAddressDetail", houseAddressDetail);
            intent.putExtra("houseCity", houseCity);
            intent.putExtra("houseCountry", houseCountry);
            startActivity(intent);
        });

        editDescriptionButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterDetailsActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "description"); // 설명 수정을 구별
            intent.putExtra("houseId", houseId);
            intent.putExtra("currentDescription", houseDescription);
            intent.putExtra("currentSummary", houseSummary);
            startActivity(intent);
        });

        // 임대 기간 수정 → RegisterCalendarActivity
        editPeriodButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterCalendarActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("houseId", houseId);
            intent.putExtra("startDay", houseStartDay);
            intent.putExtra("endDay", houseEndDay);
            startActivity(intent);
        });

        // 가격, 사진 수정 → RegisterFinalActivity
        editPriceButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterFinalActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "price");
            intent.putExtra("houseId", houseId);
            if (housePrice != null && housePrice > -1) {
                intent.putExtra("currentPrice", housePrice);
            }
            intent.putExtra("currentImageUrl", houseCoverPhoto);
            startActivity(intent);
        });

        editPhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(EditHouseActivity.this, RegisterFinalActivity.class);
            intent.putExtra("mode", "edit");
            intent.putExtra("target", "photos");
            intent.putExtra("houseId", houseId);
            intent.putExtra("currentImageUrl", houseCoverPhoto);
            if (housePrice != null && housePrice > -1) {
                intent.putExtra("currentPrice", housePrice);
            }
            startActivity(intent);
        });

        // 뒤로가기
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadHouseInformation() {
        apiService.getHouseFullDetail(houseId).enqueue(new retrofit2.Callback<HouseDetailResponseDto>() {
            @Override
            public void onResponse(retrofit2.Call<HouseDetailResponseDto> call, retrofit2.Response<HouseDetailResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    return;
                }

                HouseDetailResponseDto detail = response.body();

                houseTitle = detail.getTitle();
                houseAddress = detail.getAddressLine1();
                houseDescription = detail.getDescription();
                housePrice = detail.getPricePerNight();
                houseCoverPhoto = detail.getCoverPhotoUrl();
                houseStartDay = detail.getStartDay();
                houseEndDay = detail.getEndDay();

                updateDisplayTexts();
            }

            @Override
            public void onFailure(retrofit2.Call<HouseDetailResponseDto> call, Throwable t) {
                // silently ignore, existing extras will be used
            }
        });
    }

    private void updateDisplayTexts() {
        setTextOrDash(tvCurrentTitle, houseTitle);
        setTextOrDash(tvCurrentAddress, houseAddress);
        setTextOrDash(tvCurrentDescription, houseDescription);
        tvCurrentPeriod.setText(formatPeriodText());
        tvCurrentPrice.setText(formatPriceText());
    }

    private void setTextOrDash(TextView view, String value) {
        if (view == null) return;
        view.setText(!TextUtils.isEmpty(value) ? value : "-");
    }

    private String formatPeriodText() {
        if (!TextUtils.isEmpty(houseStartDay) && !TextUtils.isEmpty(houseEndDay)) {
            return String.format(Locale.getDefault(), "%s ~ %s",
                    formatDate(houseStartDay),
                    formatDate(houseEndDay));
        }
        return "-";
    }

    private String formatPriceText() {
        if (housePrice != null && housePrice >= 0) {
            NumberFormat format = NumberFormat.getInstance(Locale.KOREA);
            return format.format(housePrice) + "원";
        }
        return "-";
    }

    private String formatDate(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) {
            return "-";
        }
        try {
            return LocalDate.parse(dateStr).format(dateFormatter);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }
}
