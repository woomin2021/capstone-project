package com.example.jjb20;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

// MaterialCalendarView 라이브러리 import
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.listeners.OnRangeSelectedListener;

// 뷰 위젯 import
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterCalendarActivity extends AppCompatActivity {
    // 뷰 변수 선언
    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private TextInputEditText selectedDateText;
    private MaterialButton nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // XML 레이아웃 설정
        setContentView(R.layout.activity_register_calendar);

        // 뷰 ID로 초기화
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selected_date_text);
        nextButton = findViewById(R.id.next_button);

        // 툴바 설정
        setupToolbar();

        // 캘린더 설정
        setupCalendar();
    }

    /**
     * 툴바의 네비게이션 아이콘(뒤로가기) 클릭 리스너를 설정합니다.
     */
    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 Activity 종료 (이전 화면으로 돌아가기)
                finish();
            }
        });
    }

    /**
     * MaterialCalendarView의 리스너 및 옵션을 설정합니다.
     */
    private void setupCalendar() {
        // (선택 사항) 오늘 이전 날짜는 선택할 수 없도록 최소 날짜를 오늘로 설정
        Calendar today = Calendar.getInstance();
        calendarView.setMinimumDate(today);

        // [핵심] 날짜 범위(range) 선택 리스너 설정
        calendarView.setOnRangeSelectedListener(new OnRangeSelectedListener() {
            @Override
            public void onRangeSelected(List<Calendar> calendars) {
                // calendars 리스트에는 선택된 범위의 모든 날짜가 포함됩니다.

                // 선택된 날짜가 없는 경우 (예: 선택 해제)
                if (calendars.isEmpty()) {
                    selectedDateText.setText("");
                    nextButton.setEnabled(false);
                    return;
                }

                // 시작 날짜 (리스트의 첫 번째 항목)
                Calendar firstDay = calendars.get(0);
                // 종료 날짜 (리스트의 마지막 항목)
                Calendar lastDay = calendars.get(calendars.size() - 1);

                // 날짜 포맷 지정 (예: "10월 1일 - 10월 31일")
                SimpleDateFormat sdf = new SimpleDateFormat("M월 d일", Locale.KOREAN);
                String startDate = sdf.format(firstDay.getTime());
                String endDate = sdf.format(lastDay.getTime());

                // 텍스트 뷰에 선택된 기간 설정
                selectedDateText.setText(startDate + " - " + endDate);

                // '다음' 버튼 활성화
                nextButton.setEnabled(true);
            }
        });
    }
}
