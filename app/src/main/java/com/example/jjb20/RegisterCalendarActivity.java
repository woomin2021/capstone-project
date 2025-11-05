//package com.example.jjb20;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.TextView;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.material.appbar.MaterialToolbar;
//import com.google.android.material.button.MaterialButton;
//import com.google.android.material.textfield.TextInputEditText;
//import com.kizitonwose.calendar.core.CalendarDay;
//import com.kizitonwose.calendar.core.DayPosition;
//import com.kizitonwose.calendar.view.CalendarView;
//import com.kizitonwose.calendar.view.DayBinder;
//import com.kizitonwose.calendar.view.ViewContainer;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.time.temporal.WeekFields;
//import java.util.Locale;
//
//public class RegisterCalendarActivity extends AppCompatActivity {
//
//    private MaterialToolbar toolbar;
//    private CalendarView calendarView;
//    private TextInputEditText selectedDateText;
//    private MaterialButton nextButton;
//
//    private LocalDate startDate = null;
//    private LocalDate endDate = null;
//    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M월 d일", Locale.getDefault());
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register_calendar);
//
//        toolbar = findViewById(R.id.toolbar);
//        calendarView = findViewById(R.id.calendarView);
//        selectedDateText = findViewById(R.id.selected_date_text);
//        nextButton = findViewById(R.id.next_button);
//
//        setupToolbar();
//        setupCalendar();
//    }
//
//    private void setupToolbar() {
//        if (toolbar != null) {
//            toolbar.setNavigationOnClickListener(v -> finish());
//        }
//    }
//
//    private void setupCalendar() {
//        // Provide day cell layout resource programmatically
//        calendarView.setDayViewResource(R.layout.calendar_day_layout);
//
//        // Bind day cells
//        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
//            @Override
//            public void bind(DayViewContainer container, CalendarDay day) {
//                container.textView.setText(String.valueOf(day.getDate().getDayOfMonth()));
//
//                // Default appearance
//                if (day.getPosition() == DayPosition.MonthDate) {
//                    container.textView.setTextColor(Color.BLACK);
//                } else {
//                    container.textView.setTextColor(Color.GRAY);
//                }
//
//                // Selection styling
//                styleSelection(container, day.getDate());
//
//                container.getView().setOnClickListener(v -> {
//                    if (day.getPosition() == DayPosition.MonthDate) {
//                        handleDateClick(day.getDate());
//                    }
//                });
//            }
//
//            @Override
//            public DayViewContainer create(View view) {
//                return new DayViewContainer(view);
//            }
//        });
//
//        YearMonth currentMonth = YearMonth.now();
//        YearMonth endMonth = currentMonth.plusMonths(12);
//        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
//
//        calendarView.setup(currentMonth, endMonth, firstDayOfWeek);
//        calendarView.scrollToMonth(currentMonth);
//    }
//
//    private void handleDateClick(LocalDate date) {
//        if (startDate == null) {
//            startDate = date;
//            endDate = null;
//        } else if (endDate == null) {
//            if (date.isBefore(startDate)) {
//                startDate = date;
//            } else if (date.equals(startDate)) {
//                // single-day toggle -> clear
//                startDate = null;
//                endDate = null;
//            } else {
//                endDate = date;
//            }
//        } else {
//            startDate = date;
//            endDate = null;
//        }
//
//        updateSelectionUi();
//        calendarView.notifyCalendarChanged();
//    }
//
//    private void updateSelectionUi() {
//        if (startDate != null && endDate != null) {
//            String text = startDate.format(formatter) + " - " + endDate.format(formatter);
//            selectedDateText.setText(text);
//            nextButton.setEnabled(true);
//        } else if (startDate != null) {
//            selectedDateText.setText(startDate.format(formatter) + " - ");
//            nextButton.setEnabled(false);
//        } else {
//            selectedDateText.setText("");
//            nextButton.setEnabled(false);
//        }
//    }
//
//    private void styleSelection(DayViewContainer container, LocalDate day) {
//        // Clear background first
//        container.getView().setBackgroundColor(Color.TRANSPARENT);
//
//        if (startDate != null && endDate != null) {
//            if ((day.isAfter(startDate) || day.equals(startDate)) &&
//                    (day.isBefore(endDate) || day.equals(endDate))) {
//                container.getView().setBackgroundColor(0x201ABC9C); // light mint range
//                container.textView.setTextColor(Color.BLACK);
//            }
//        } else if (startDate != null && day.equals(startDate)) {
//            container.getView().setBackgroundColor(0x401ABC9C); // start highlight
//            container.textView.setTextColor(Color.BLACK);
//        }
//    }
//
//    static class DayViewContainer extends ViewContainer {
//        final TextView textView;
//
//        DayViewContainer(View view) {
//            super(view);
//            textView = view.findViewById(R.id.calendar_day_text);
//        }
//    }
//}