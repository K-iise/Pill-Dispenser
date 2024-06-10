package com.capstone.pilldispenser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Alarm_Ringing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_ringing);

        ImageButton endButton = (ImageButton) findViewById(R.id.end_button);

        TextView Time = (TextView) findViewById(R.id.time);

        // 이전 화면으로부터 데이터 받아오기
        String time = getIntent().getStringExtra("time");

        Time.setText(time);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AlarmReceiver에서 재생 중인 ringtone을 중지시킴
                AlarmReceiver.stopAlarm();
                onBackPressed();
            }


        });
    }

}