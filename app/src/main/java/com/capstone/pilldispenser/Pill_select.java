package com.capstone.pilldispenser;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class Pill_select extends AppCompatActivity {

    private TextView deviceNumberTextView;
    private TextView deviceNameTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_select);

        // Menu Button, Drawer 생성
        ImageButton menuButton = (ImageButton) findViewById(R.id.action_ham);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);

        // 메뉴바 클릭 이벤트(Drawer 출력)
        menuButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT) ;
                }
            }
        });

        deviceNumberTextView = findViewById(R.id.devicenump);
        deviceNameTextView = findViewById(R.id.devicenamp);

        // 이전 화면으로부터 데이터 받아오기
        String deviceNumber = getIntent().getStringExtra("deviceNumber");
        String deviceName = getIntent().getStringExtra("deviceName");

        // 받아온 데이터를 TextView에 설정
        deviceNumberTextView.setText(deviceNumber);
        deviceNameTextView.setText(deviceName);

    }
}