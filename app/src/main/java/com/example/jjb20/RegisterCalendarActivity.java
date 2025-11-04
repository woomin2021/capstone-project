package com.example.jjb20;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class RegisterCalendarActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private TextInputEditText selectedDateText;
    private MaterialButton nextButton;

    private Calendar rangeStart = null;
    private Calendar rangeEnd   = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_calendar);

        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selected_date_text);
        nextButton = findViewById(R.id.next_button);

        setupToolbar();
        setupCalendar();
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { finish(); }
        });
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        calendarView.setMinimumDate(today);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                Calendar clicked = (Calendar) eventDay.getCalendar().clone();

                if (rangeStart == null || rangeEnd != null) {
                    rangeStart = clicked;
                    rangeEnd = null;
                } else {
                    if (clicked.before(rangeStart)) {
                        rangeStart = clicked;
                        rangeEnd = null;
                    } else {
                        rangeEnd = clicked;
                    }
                }
                updateSelection();
            }
        });
    }

    private void updateSelection() {
        List<Calendar> selected = new ArrayList<>();

        if (rangeStart != null && rangeEnd != null) {
            Calendar day = (Calendar) rangeStart.clone();
            while (!day.after(rangeEnd)) {
                selected.add((Calendar) day.clone());
                day.add(Calendar.DAY_OF_MONTH, 1);
            }
        } else if (rangeStart != null) {
            selected.add((Calendar) rangeStart.clone());
        }

        calendarView.setSelectedDates(selected);

        if (rangeStart != null && rangeEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M월 d일", Locale.KOREAN);
            selectedDateText.setText(sdf.format(rangeStart.getTime()) + " - " + sdf.format(rangeEnd.getTime()));
            nextButton.setEnabled(true);
        } else if (rangeStart != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M월 d일", Locale.KOREAN);
            selectedDateText.setText(sdf.format(rangeStart.getTime()));
            nextButton.setEnabled(false);
        } else {
            selectedDateText.setText("");
            nextButton.setEnabled(false);
        }
    }
}