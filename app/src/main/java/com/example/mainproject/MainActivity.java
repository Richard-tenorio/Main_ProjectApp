package com.example.mainproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout topArea;
    TextView txtSchedule, txtSelectedDate;
    CalendarView calendarView;
    Button btnCustomizeCalendar, btnSettings, btnMenu;

    String selectedDate = "";
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ensure your XML file is named correctly

        // Link UI elements to their IDs in XML
        topArea = findViewById(R.id.topArea);
        txtSchedule = findViewById(R.id.txtSchedule);
        txtSelectedDate = findViewById(R.id.txtSelectedDate);
        calendarView = findViewById(R.id.calendarView);
        btnCustomizeCalendar = findViewById(R.id.btnCustomizeCalendar);
        btnSettings = findViewById(R.id.btnSettings);
        btnMenu = findViewById(R.id.btnMenu);

        // Optional: Customize top bar color
        if (topArea != null) {
            topArea.setBackgroundColor(Color.parseColor("#FFBB86FC"));
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MySchedules", MODE_PRIVATE);

        // Calendar date selection
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            txtSelectedDate.setText("Selected Date: " + selectedDate);

            // Load saved schedule or show default
            String schedule = sharedPreferences.getString(selectedDate, "No schedule for this date.");
            txtSchedule.setText("Schedule for " + selectedDate + ":\n" + schedule);
        });

        // Customize Calendar button
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

        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Subscription feature coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void showScheduleInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Schedule for " + selectedDate);

        final EditText input = new EditText(this);
        input.setHint("E.g., • Meeting at 10AM\n• Call at 3PM");
        input.setMinLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setGravity(Gravity.TOP);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String scheduleText = input.getText().toString().trim();
            if (!scheduleText.isEmpty()) {
                sharedPreferences.edit().putString(selectedDate, scheduleText).apply();
                txtSchedule.setText("Schedule for " + selectedDate + ":\n" + scheduleText);
                Toast.makeText(this, "Schedule saved.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Empty schedule not saved.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
