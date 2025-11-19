package com.example.jjb20;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RegisterCalendarActivity extends AppCompatActivity {

    // 뷰 변수
    private MaterialToolbar toolbar;
    private MaterialButton nextButton;
    private TextInputEditText selectedDateText;
    private CalendarView calendarView;

    // 월 네비게이션 뷰 (XML에서 추가된 부분)
    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView tvMonth;

    // 날짜 상태 변수
    private LocalDate startDate = null;
    private LocalDate endDate = null;
    private final LocalDate today = LocalDate.now();

    // 캘린더 범위
    private YearMonth startMonth;
    private YearMonth endMonth;

    // 날짜 포맷터
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
    private final DateTimeFormatter monthTitleFormatter = DateTimeFormatter.ofPattern("yyyy.MM");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_calendar);

        initViews();
        setupListeners();
        setupCalendar();
        updateUi(); // 초기 UI 상태 설정
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nextButton = findViewById(R.id.next_button);
        selectedDateText = findViewById(R.id.selected_date_text);
        calendarView = findViewById(R.id.calendarView);

        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        tvMonth = findViewById(R.id.tvMonth);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        // 이전 월 버튼
        btnPrev.setOnClickListener(v -> {
            try {
                YearMonth current = calendarView.findFirstVisibleMonth().getYearMonth();
                if (current.isAfter(startMonth)) {
                    calendarView.smoothScrollToMonth(current.minusMonths(1));
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        // 다음 월 버튼
        btnNext.setOnClickListener(v -> {
            try {
                YearMonth current = calendarView.findFirstVisibleMonth().getYearMonth();
                if (current.isBefore(endMonth)) {
                    calendarView.smoothScrollToMonth(current.plusMonths(1));
                }
            } catch (Exception e) { e.printStackTrace(); }
        });

        nextButton.setOnClickListener(v -> {
            if (nextButton.isEnabled()) {
                if (startDate != null && endDate != null) {
                    PrefManager.put("house_available_start", startDate.toString());
                    PrefManager.put("house_available_end", endDate.toString());
                }
                Intent intent = new Intent(RegisterCalendarActivity.this, RegisterFinalActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Kizitonwose 캘린더뷰 설정
     */
    private void setupCalendar() {
        // --- 1. 날짜 셀(Day) 뷰 컨테이너 ---
        class DayViewContainer extends ViewContainer {
            TextView dayText;
            CalendarDay day;

            public DayViewContainer(View view) {
                super(view);
                // calendar_day_layout이 지정되지 않았을 경우를 대비하여 안전하게 획득
                TextView candidate = view.findViewById(R.id.calendar_day_text);
                if (candidate == null && view instanceof TextView) {
                    candidate = (TextView) view;
                }
                dayText = candidate;
                view.setOnClickListener(v -> onDayClicked(day));
            }
        }

        // --- 2. 날짜 바인더 설정 (Missing return statement 오류 해결 부분) ---
        calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay day) {
                container.day = day;
                // 날짜 숫자를 TextView에 설정
                container.dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

                // --- 날짜 상태에 따른 UI 변경 로직 ---
                if (day.getPosition() != DayPosition.MonthDate || day.getDate().isBefore(today)) {
                    // 이번 달 날짜가 아니거나 오늘 이전 날짜는 비활성화
                    container.dayText.setTextColor(Color.GRAY);
                    container.dayText.setBackground(null);
                } else {
                    // 선택 가능 날짜
                    container.dayText.setTextColor(Color.BLACK);

                    // 선택 상태에 따른 배경색 설정
                    if (day.getDate().equals(startDate) && endDate == null) {
                        // 시작일만 선택됨
                        container.dayText.setBackgroundColor(Color.parseColor("#1ABC9C"));
                        container.dayText.setTextColor(Color.WHITE);
                    } else if (day.getDate().equals(startDate) || day.getDate().equals(endDate)) {
                        // 시작일 또는 종료일
                        container.dayText.setBackgroundColor(Color.parseColor("#1ABC9C"));
                        container.dayText.setTextColor(Color.WHITE);
                    } else if (startDate != null && endDate != null &&
                            day.getDate().isAfter(startDate) && day.getDate().isBefore(endDate)) {
                        // 시작일과 종료일 사이 (선택 기간)
                        container.dayText.setBackgroundColor(Color.parseColor("#E0F7F3"));
                        container.dayText.setTextColor(Color.BLACK);
                    } else {
                        // 선택되지 않은 기본 상태
                        container.dayText.setBackground(null);
                    }
                }
            }
        });

        // --- 3. 캘린더 설정 ---
        startMonth = YearMonth.now();
        endMonth = startMonth.plusMonths(12);
        DayOfWeek firstDayOfWeek = DayOfWeek.SUNDAY;

        calendarView.setup(startMonth, endMonth, firstDayOfWeek);
        calendarView.scrollToMonth(startMonth);

        // --- 4. 월 스크롤 리스너 (XML 헤더 제어) ---
        calendarView.setMonthScrollListener(new Function1<CalendarMonth, Unit>() {
            @Override
            public Unit invoke(CalendarMonth month) {
                tvMonth.setText(month.getYearMonth().format(monthTitleFormatter));

                boolean canGoPrev = month.getYearMonth().isAfter(startMonth);
                boolean canGoNext = month.getYearMonth().isBefore(endMonth);

                btnPrev.setAlpha(canGoPrev ? 1.0f : 0.3f);
                btnNext.setAlpha(canGoNext ? 1.0f : 0.3f);
                btnPrev.setEnabled(canGoPrev);
                btnNext.setEnabled(canGoNext);

                return Unit.INSTANCE;
            }
        });

    }

    private void onDayClicked(CalendarDay day) {
        if (day.getPosition() != DayPosition.MonthDate || day.getDate().isBefore(today)) {
            return;
        }

        if (startDate == null) {
            startDate = day.getDate();
        } else if (endDate == null) {
            if (day.getDate().isAfter(startDate)) {
                endDate = day.getDate();
            } else {
                startDate = day.getDate();
                endDate = null;
            }
        } else {
            startDate = day.getDate();
            endDate = null;
        }

        calendarView.notifyCalendarChanged();
        updateUi();
    }

    private void updateUi() {
        boolean isComplete = (startDate != null && endDate != null);
        nextButton.setEnabled(isComplete);

        int enabledColor = Color.parseColor("#00CBA8");
        int disabledColor = Color.parseColor("#BDBDBD");

        if (isComplete) {
            String dateString = startDate.format(formatter) + " - " + endDate.format(formatter);
            selectedDateText.setText(dateString);
            nextButton.setBackgroundTintList(ColorStateList.valueOf(enabledColor));
        } else if (startDate != null) {
            selectedDateText.setText(startDate.format(formatter) + " - ");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(disabledColor));
        }
        else {
            selectedDateText.setText("");
            nextButton.setBackgroundTintList(ColorStateList.valueOf(disabledColor));
        }
    }


}