package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalTime;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    LinearLayout topArea;
    TextView txtSchedule, txtTimeline, txtSelectedDate;
    CalendarView calendarView;
    Button btnCustomizeCalendar, btnSettings, btnMenu;


    String selectedDate = "";
    SharedPreferences sharedPreferences;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSchedule = findViewById(R.id.txtSchedule);
        txtTimeline = findViewById(R.id.txtTimeline);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        calendarView = findViewById(R.id.calendarView);
        btnCustomizeCalendar = findViewById(R.id.btnCustomizeCalendar);
        btnSettings = findViewById(R.id.btnSettings);
        btnMenu = findViewById(R.id.btnMenu);

        if (topArea != null) {
            topArea.setBackgroundColor(Color.parseColor("#FFBB86FC"));
        }

        sharedPreferences = getSharedPreferences("MySchedules", MODE_PRIVATE);

        updateScheduleView(); // Show today's schedule by default

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtSelectedDate.setText("Selected Date: " + selectedDate);

            // Validate past date
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            if (selectedCal.before(today)) {
                Toast.makeText(this, "You selected a past date.", Toast.LENGTH_SHORT).show();
                btnCustomizeCalendar.setEnabled(false);
            } else {
                btnCustomizeCalendar.setEnabled(true);
            }

            updateScheduleView();
        });

        btnCustomizeCalendar.setOnClickListener(v -> {
            if (selectedDate == null || selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a date first.", Toast.LENGTH_SHORT).show();
                return;
            }
            showScheduleInputDialog();
        });

        btnSettings.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, accountSettingsActivity.class);
                intent.putExtra("fullName", "John Doe");
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening settings: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(MainActivity.this, SubscriptionActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Error opening subscription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateScheduleView() {
        String scheduleText = sharedPreferences.getString(selectedDate, "");
        StringBuilder scheduleBuilder = new StringBuilder();

        if (!scheduleText.isEmpty()) {
            scheduleBuilder.append("Schedule for ").append(selectedDate).append(":\n").append(scheduleText);
        } else {
            scheduleBuilder.append("No schedule for ").append(selectedDate);
        }

        txtSchedule.setText(scheduleBuilder.toString());
        updateTimelineView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateTimelineView() {
        String scheduleText = sharedPreferences.getString(selectedDate, "");
        StringBuilder timeline = new StringBuilder();

        if (!scheduleText.isEmpty()) {
            String[] lines = scheduleText.split("\n");
            LocalTime now = LocalTime.now();

            for (String line : lines) {
                if (line.matches("^\\d{2}:\\d{2}.*")) {
                    String[] parts = line.split(" ", 2);
                    try {
                        LocalTime eventTime = LocalTime.parse(parts[0]);
                        if (eventTime.isBefore(now)) {
                            timeline.append("\u2022 ").append(line).append("\n");
                        }
                    } catch (Exception e) {
                        // skip invalid time format
                    }
                }
            }

            if (timeline.length() == 0) {
                timeline.append("No past events yet.");
            }
        } else {
            timeline.append("No events for timeline.");
        }

        txtTimeline.setText("Timeline:\n" + timeline.toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showScheduleInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Schedule for " + selectedDate);

        final EditText input = new EditText(this);
        input.setHint("Format:\nHH:mm Task\nExample:\n09:00 Warm-up");
        input.setMinLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setGravity(Gravity.TOP);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String scheduleText = input.getText().toString().trim();
            if (!scheduleText.isEmpty()) {
                sharedPreferences.edit().putString(selectedDate, scheduleText).apply();
                Toast.makeText(this, "Schedule saved.", Toast.LENGTH_SHORT).show();
                updateScheduleView();
            } else {
                Toast.makeText(this, "Empty schedule not saved.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
