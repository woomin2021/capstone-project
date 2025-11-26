package com.example.jjb20;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import com.example.jjb20.adapter.ImageSliderAdapter;
import com.example.jjb20.dto.HouseDto;
import com.example.jjb20.dto.HouseDetailResponseDto;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

// Google Map
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

// Kizitonwose
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.CalendarMonth;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.view.CalendarView;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.time.temporal.ChronoUnit;

public class RentHouseDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_HOUSE  = "house";
    public static final String EXTRA_DATE   = "extra_date";
    public static final String EXTRA_GUESTS = "extra_guests";
    public static final String EXTRA_PRICE  = "extra_price";
    public static final String EXTRA_HOUSE_NAME = "extra_house_name";
    public static final String EXTRA_HOUSE_ADDR = "extra_house_addr";
    public static final String EXTRA_HOUSE_IMAGE = "extra_house_image";

    private ViewPager2 viewPagerImages;
    private TextView tvTitle, tvLocation, tvPrice;
    private HouseDto house;

    // 추가: 이 숙소의 호스트 user_id 저장용
    private long hostUserId = -1L;

    // 빌리는 사람  관련
    private int adultCount = 1;
    private TextView tvGuestCount;
    private ImageButton btnGuestPlus, btnGuestMinus;

    // 캘린더/기간 상태
    private CalendarView calendarView;
    private TextInputEditText selectedDateEt;
    private LocalDate startDate = null;
    private LocalDate endDate   = null;
    private final LocalDate today = LocalDate.now();
    private YearMonth startMonth;
    private YearMonth endMonth;
    private final DateTimeFormatter dayFmt   =
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private final DateTimeFormatter monthFmt =
            DateTimeFormatter.ofPattern("yyyy. MM");

    // 호스트 정보 영역
    private TextView tvHostLabel;
    private TextView tvHostName;
    private View divider2;

    // Retrofit
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house_detail_page);

        // Retrofit 준비
        apiService = RetrofitClient.getInstance().create(ApiService.class);


        viewPagerImages = findViewById(R.id.viewPagerImages);
        tvTitle    = findAnyTextView("textTitle", "textTitleDetail", "tvTitle");
        tvLocation = findAnyTextView("textAddress", "textLocationDetail", "tvLocation");
        tvPrice    = findAnyTextView("textPrice", "textPriceDetail", "tvPrice");
        selectedDateEt = findViewById(R.id.selected_date_text);
        calendarView   = findViewById(R.id.calendarView);

        // 빌리는 사람  섹션
        tvHostLabel = findViewById(R.id.textHostLabel);
        tvHostName  = findViewById(R.id.textHostName);
        divider2    = findViewById(R.id.divider2);
        hideHostSection();

        // 나머지 텍스트 뷰
        TextView tvDate               = findViewById(R.id.textDate);
        TextView tvAddressLabel       = findViewById(R.id.textAddressLabel);
        TextView tvAddressValue       = findViewById(R.id.textAddressValue);
        TextView tvDetailAddressLabel = findViewById(R.id.textDetailAddressLabel);
        TextView tvDetailAddressValue = findViewById(R.id.textDetailAddressValue);
        TextView tvDescLabel          = findViewById(R.id.textDescriptionLabel);
        TextView tvDescValue          = findViewById(R.id.textDescriptionValue);
        TextView tvShortLabel         = findViewById(R.id.textShortDescLabel);
        TextView tvShortValue         = findViewById(R.id.textShortDescValue);
        TextView tvMapLabel           = findViewById(R.id.textMapLabel);
        TextView tvAmenitiesLabel = findViewById(R.id.textAmenitiesLabel);
        TextView tvAmenitiesValue = findViewById(R.id.textAmenitiesValue);

        // 목록에서 넘어온 기본 정보
        house = (HouseDto) getIntent().getSerializableExtra(EXTRA_HOUSE);
        if (house != null) {
            Log.d("RentDetail", "lat=" + house.latitude + ", lng=" + house.longitude);

            if (tvTitle != null)    tvTitle.setText(house.title);
            if (tvLocation != null) {
                String locationText = house.addressLine1;
                if (house.city != null && !house.city.isEmpty()) {
                    locationText += "\n" + house.city;
                }
                if (house.country != null && !house.country.isEmpty()) {
                    locationText += " · " + house.country;
                }
                tvLocation.setText(locationText);
            }
            if (tvPrice != null && house.pricePerNight != null) {
                tvPrice.setText(house.pricePerNight + "원 · 1박");
            }

            // 이미지 슬라이더
            List<String> images = new ArrayList<>();
            if (house.coverPhotoUrl != null && !house.coverPhotoUrl.isEmpty()) {
                images.add(house.coverPhotoUrl);
            }
            ImageSliderAdapter adapter = new ImageSliderAdapter(images);
            viewPagerImages.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
            viewPagerImages.setAdapter(adapter);

            // 기타 텍스트
            if (tvDate != null && house.createdAt != null) {
                tvDate.setText(house.createdAt);
            }
            if (tvAddressLabel != null) tvAddressLabel.setText("주소");
            if (tvAddressValue != null) tvAddressValue.setText(house.addressLine1);

            if (tvDetailAddressLabel != null) tvDetailAddressLabel.setText("도시 / 국가");
            if (tvDetailAddressValue != null) {
                String detail = "";
                if (house.city != null && !house.city.isEmpty()) {
                    detail += house.city;
                }
                if (house.country != null && !house.country.isEmpty()) {
                    if (!detail.isEmpty()) detail += " · ";
                    detail += house.country;
                }
                tvDetailAddressValue.setText(detail);
            }

            if (tvDescLabel != null)  tvDescLabel.setText("숙소 설명");
            if (tvDescValue != null)  tvDescValue.setText(house.description);

            if (tvShortLabel != null) tvShortLabel.setText("한 줄 소개");
            if (tvShortValue != null) tvShortValue.setText(house.description);

            if (tvMapLabel != null)   tvMapLabel.setText("숙소 위치");

            // 여기서 서버 상세 조회 >>>>>> 호스트 이름 / 평점 채우기
            loadHouseDetailFromServer(house.id);
        }

        // 툴바
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        // 세그먼트 그리고 다음 버튼
        View seg1 = findViewById(R.id.seg1);
        View seg2 = findViewById(R.id.seg2);
        AppCompatButton btnNext = findViewById(R.id.btnNext);
        if (btnNext != null) btnNext.setEnabled(false);

        // 캘린더
        setupCalendar();

        // 인원
        tvGuestCount  = findViewById(R.id.tvGuestCount);
        btnGuestPlus  = findViewById(R.id.btnGuestPlus);
        btnGuestMinus = findViewById(R.id.btnGuestMinus);
        updateGuestUi();

        if (btnGuestPlus != null) {
            btnGuestPlus.setOnClickListener(v -> {
                adultCount = Math.min(adultCount + 1, 10);
                updateGuestUi();
            });
        }
        if (btnGuestMinus != null) {
            btnGuestMinus.setOnClickListener(v -> {
                adultCount = Math.max(adultCount - 1, 1);
                updateGuestUi();
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                int GREEN = Color.parseColor("#14D8B4");
                int GRAY  = getColor(R.color.basic2);
                if (seg1 != null) seg1.setBackgroundColor(GRAY);
                if (seg2 != null) seg2.setBackgroundColor(GREEN);

                int pricePerNight = (house != null && house.pricePerNight != null)
                        ? house.pricePerNight
                        : 90000;

                long nights = 1L;
                if (startDate != null && endDate != null) {
                    nights = ChronoUnit.DAYS.between(startDate, endDate);
                }

                long totalPrice = pricePerNight * nights;
                String priceText = totalPrice + "원 · " + nights + "박";

                String dateText;
                if (startDate != null && endDate != null) {
                    dateText = startDate.format(dayFmt) + " - " + endDate.format(dayFmt);
                } else {
                    dateText = "12월 15일 ~ 12월 18일";
                }

                // 여기서 이름/주소/이미지 준비
                String name    = (house != null) ? house.title : null;
                String address = null;
                if (house != null) {
                    String loc = house.addressLine1;
                    if (house.city != null && !house.city.isEmpty()) {
                        loc += "\n" + house.city;
                    }
                    if (house.country != null && !house.country.isEmpty()) {
                        loc += " · " + house.country;
                    }
                    address = loc;
                }
                String imageUrl = (house != null) ? house.coverPhotoUrl : null;


                // 1) DB에 저장할 raw 값들 준비 (yyyy-MM-dd 형태)
                long houseId = (house != null) ? house.id : -1L;
                String checkinDateRaw  = (startDate != null) ? startDate.toString() : null;   // 2026-01-06
                String checkoutDateRaw = (endDate   != null) ? endDate.toString()   : null;   // 2026-01-08



                // 예약 확인 화면으로 데이터 전달
                Intent i = new Intent(this, ReserveConfirmActivity.class);
                i.putExtra(EXTRA_DATE,   dateText);
                i.putExtra(EXTRA_GUESTS, "성인 " + adultCount + "명");
                i.putExtra(EXTRA_PRICE,  priceText);
                i.putExtra(EXTRA_HOUSE_NAME,  name);
                i.putExtra(EXTRA_HOUSE_ADDR,  address);
                i.putExtra(EXTRA_HOUSE_IMAGE, imageUrl);

                if (house != null) {
                    i.putExtra("houseId", house.id);                // 숙소 PK
                }
                if (startDate != null) {
                    i.putExtra("checkinDate", startDate.toString());   // "2025-11-21"
                }
                if (endDate != null) {
                    i.putExtra("checkoutDate", endDate.toString());    // "2025-11-23"
                }
                startActivity(i);
            });
        }

        // 지도
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    // 호스트 섹션 표시/숨김

    private void hideHostSection() {
        if (tvHostLabel != null) tvHostLabel.setVisibility(View.GONE);
        if (tvHostName  != null) tvHostName.setVisibility(View.GONE);
        if (divider2    != null) divider2.setVisibility(View.GONE);
    }

    private void showHostSection(String hostName,
                                 Double ratingAvg,
                                 Integer ratingCount) {

        if (hostName == null || hostName.trim().isEmpty()) {
            hideHostSection();
            return;
        }

        // 위쪽이 빌려주는 사람 이름
        if (tvHostLabel != null) {
            tvHostLabel.setText(hostName);   // 여기 이름만 표시
            tvHostLabel.setVisibility(View.VISIBLE);
        }

        //아래쪽 크게 보이는게 평점 이랍니다 스바
        if (tvHostName != null) {
            String ratingText;
            if (ratingAvg != null && ratingCount != null && ratingCount > 0) {
                ratingText = String.format("★ %.1f (%d명)", ratingAvg, ratingCount);
            } else {
                ratingText = "평점 없음";
            }
            tvHostName.setText(ratingText);
            tvHostName.setVisibility(View.VISIBLE);
            tvHostName.setOnClickListener(v -> {
                Intent intent = new Intent(RentHouseDetailActivity.this, HostReviewActivity.class);

                //필요하면 호스트 ID 같은 것도 같이 넘길 수 있음
                intent.putExtra("hostId", hostUserId);

                startActivity(intent);
            });
        }

        if (divider2 != null) divider2.setVisibility(View.VISIBLE);
    }

    //서버에서 상세 정보(호스트 이름/평점 /어매니티까지) 를 다시 받아오기
    private void loadHouseDetailFromServer(long houseId) {
        apiService.getHouseFullDetail(houseId).enqueue(new Callback<HouseDetailResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<HouseDetailResponseDto> call,
                                   @NonNull Response<HouseDetailResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("RentDetail", "detail fail: " + response.code());
                    return;
                }

                HouseDetailResponseDto dto = response.body();

                String hostName     = dto.getHostName();
                Double ratingAvg    = dto.getHostRatingAvg();
                Integer ratingCount = dto.getHostRatingCount();

                // 여기서 호스트 id 저장
                Long dtoHostId = dto.getHostId();
                if (dtoHostId != null) {
                    hostUserId = dtoHostId;
                } else {
                    hostUserId = -1L;
                }

                Log.d("RentDetail",
                        "hostName=" + hostName +
                                ", rating=" + ratingAvg +
                                ", cnt=" + ratingCount);

                showHostSection(hostName, ratingAvg, ratingCount);

                // ===== 어메니티 UI 채우기 =====
                TextView tvAmenitiesLabel = findViewById(R.id.textAmenitiesLabel);
                TextView tvAmenitiesValue = findViewById(R.id.textAmenitiesValue);

                if (tvAmenitiesLabel != null) {
                    tvAmenitiesLabel.setText("편의시설");
                }

                if (tvAmenitiesValue != null) {
                    StringBuilder sb = new StringBuilder();

                    if (Boolean.TRUE.equals(dto.getParking()))        sb.append("주차장 · ");
                    if (Boolean.TRUE.equals(dto.getWifi()))           sb.append("와이파이 · ");
                    if (Boolean.TRUE.equals(dto.getAirConditioning())) sb.append("에어컨 · ");
                    if (Boolean.TRUE.equals(dto.getHeating()))        sb.append("난방 · ");
                    if (Boolean.TRUE.equals(dto.getKitchen()))        sb.append("주방 · ");
                    if (Boolean.TRUE.equals(dto.getWasher()))         sb.append("세탁기 · ");
                    if (Boolean.TRUE.equals(dto.getDryer()))          sb.append("건조기 · ");
                    if (Boolean.TRUE.equals(dto.getBathtub()))        sb.append("욕조 · ");
                    if (Boolean.TRUE.equals(dto.getDiningTable()))    sb.append("식탁 · ");
                    if (Boolean.TRUE.equals(dto.getMicrowave()))      sb.append("전자레인지 · ");
                    if (Boolean.TRUE.equals(dto.getRefrigerator()))   sb.append("냉장고 · ");
                    if (Boolean.TRUE.equals(dto.getTv()))             sb.append("TV · ");

                    if (sb.length() == 0) {
                        sb.append("등록된 편의시설이 없습니다.");
                    } else {
                        sb.setLength(sb.length() - 3); // 마지막 " · " 제거
                    }

                    tvAmenitiesValue.setText(sb.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<HouseDetailResponseDto> call,
                                  @NonNull Throwable t) {
                Log.e("RentDetail", "detail error", t);
            }
        });
    }

    // 인원 / 캘린더 / 지도

    private void updateGuestUi() {
        if (tvGuestCount != null) {
            tvGuestCount.setText("성인 " + adultCount + "명");
        }
        if (btnGuestMinus != null) {
            btnGuestMinus.setEnabled(adultCount > 1);
            btnGuestMinus.setAlpha(adultCount > 1 ? 1f : 0.4f);
        }
        if (btnGuestPlus != null) {
            btnGuestPlus.setEnabled(adultCount < 10);
            btnGuestPlus.setAlpha(adultCount < 10 ? 1f : 0.4f);
        }
    }

    private void setupCalendar() {
        class DayViewContainer extends ViewContainer {
            TextView dayText;
            CalendarDay day;

            DayViewContainer(@NonNull View view) {
                super(view);
                dayText = view.findViewById(R.id.calendar_day_text);
                view.setOnClickListener(v -> onDayClicked(day));
            }
        }

        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer c,
                             @NonNull CalendarDay d) {
                c.day = d;
                LocalDate date = d.getDate();
                c.dayText.setText(String.valueOf(date.getDayOfMonth()));

                if (d.getPosition() != DayPosition.MonthDate || date.isBefore(today)) {
                    c.dayText.setTextColor(Color.parseColor("#BDBDBD"));
                    c.dayText.setBackground(null);
                    return;
                }

                c.dayText.setTextColor(Color.BLACK);
                c.dayText.setBackground(null);

                if (startDate != null && endDate == null && date.equals(startDate)) {
                    c.dayText.setBackgroundColor(Color.parseColor("#14D8B4"));
                    c.dayText.setTextColor(Color.WHITE);
                } else if (startDate != null && endDate != null) {
                    if (date.equals(startDate) || date.equals(endDate)) {
                        c.dayText.setBackgroundColor(Color.parseColor("#14D8B4"));
                        c.dayText.setTextColor(Color.WHITE);
                    } else if (date.isAfter(startDate) && date.isBefore(endDate)) {
                        c.dayText.setBackgroundColor(Color.parseColor("#E0F7F3"));
                        c.dayText.setTextColor(Color.BLACK);
                    }
                }
            }
        });

        startMonth = YearMonth.now();
        endMonth   = startMonth.plusMonths(12);
        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(startMonth);

        TextView tvMonth   = findViewById(R.id.tvMonth);
        View btnPrevMonth  = findViewById(R.id.btnPrevMonth);
        View btnNextMonth  = findViewById(R.id.btnNextMonth);

        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override public Unit invoke(CalendarMonth month) {
                if (tvMonth != null)
                    tvMonth.setText(month.getYearMonth().format(monthFmt));
                if (btnPrevMonth != null) {
                    boolean enablePrev = month.getYearMonth().isAfter(startMonth);
                    btnPrevMonth.setEnabled(enablePrev);
                    btnPrevMonth.setAlpha(enablePrev ? 1f : .3f);
                }
                if (btnNextMonth != null) {
                    boolean enableNext = month.getYearMonth().isBefore(endMonth);
                    btnNextMonth.setEnabled(enableNext);
                    btnNextMonth.setAlpha(enableNext ? 1f : .3f);
                }
                return Unit.INSTANCE;
            }
        });

        if (btnPrevMonth != null) {
            btnPrevMonth.setOnClickListener(v -> {
                YearMonth cur = calendarView.findFirstVisibleMonth().getYearMonth();
                if (cur.isAfter(startMonth))
                    calendarView.smoothScrollToMonth(cur.minusMonths(1));
            });
        }
        if (btnNextMonth != null) {
            btnNextMonth.setOnClickListener(v -> {
                YearMonth cur = calendarView.findFirstVisibleMonth().getYearMonth();
                if (cur.isBefore(endMonth))
                    calendarView.smoothScrollToMonth(cur.plusMonths(1));
            });
        }
    }

    private void onDayClicked(CalendarDay day) {
        LocalDate date = day.getDate();
        if (day.getPosition() != DayPosition.MonthDate || date.isBefore(today)) return;

        if (startDate == null) {
            startDate = date;
        } else if (endDate == null) {
            if (date.isAfter(startDate)) {
                endDate = date;
            } else {
                startDate = date;
                endDate   = null;
            }
        } else {
            startDate = date;
            endDate   = null;
        }

        calendarView.notifyCalendarChanged();
        updateDateTextAndButton();
    }

    private void updateDateTextAndButton() {
        AppCompatButton btnNext = findViewById(R.id.btnNext);
        if (startDate != null && endDate != null) {
            String text = startDate.format(dayFmt) + " - " + endDate.format(dayFmt);
            if (selectedDateEt != null) selectedDateEt.setText(text);
            if (btnNext != null) btnNext.setEnabled(true);
        } else if (startDate != null) {
            String text = startDate.format(dayFmt) + " - ";
            if (selectedDateEt != null) selectedDateEt.setText(text);
            if (btnNext != null) btnNext.setEnabled(false);
        } else {
            if (selectedDateEt != null) selectedDateEt.setText("");
            if (btnNext != null) btnNext.setEnabled(false);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng position;
        if (house != null && house.latitude != null && house.longitude != null) {
            position = new LatLng(house.latitude, house.longitude);
        } else {
            position = new LatLng(33.450701, 126.570667);
        }

        String title = (house != null && house.title != null)
                ? house.title : "숙소 위치";
        googleMap.addMarker(new MarkerOptions().position(position).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));
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