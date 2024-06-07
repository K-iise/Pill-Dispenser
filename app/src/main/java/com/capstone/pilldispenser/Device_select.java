package com.capstone.pilldispenser;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Device_select extends AppCompatActivity {

    private TextView deviceNumberTextView;
    private TextView deviceNameTextView;
    private RelativeLayout deviceLayout;
    private String deviceNumber;
    private String deviceName;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        
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

        // 이전 화면으로부터 데이터 받아오기
        userId = getIntent().getStringExtra("userId");

        deviceNumberTextView = findViewById(R.id.devicenumber);
        deviceNameTextView = findViewById(R.id.devicename);
        deviceLayout = findViewById(R.id.device);

        // RelativeLayout 클릭 이벤트 설정
        deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 이벤트 발생 시 Pill_select 액티비티로 이동
                Intent intent = new Intent(Device_select.this, Pill_select.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("deviceName", deviceName);
                startActivity(intent);
            }
        });

        // 기계번호 및 기계명 가져오는 메서드 호출
        sendHttpRequestForDeviceNumber();
        sendHttpRequestForDeviceName();
    }

    private void sendHttpRequestForDeviceNumber() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://61.79.73.178:8080/PillJSP/Android/device_selectDB.jsp");
                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    deviceNumber = response.toString();
                    // 기계번호를 UI에 업데이트
                    updateDeviceNumberUI(deviceNumber);

                } catch (IOException e) {
                    e.printStackTrace();
                    // 에러 처리
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void sendHttpRequestForDeviceName() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://61.79.73.178:8080/PillJSP/Android/device_selectDB2.jsp");
                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    deviceName = response.toString();
                    // 기계명을 UI에 업데이트
                    updateDeviceNameUI(deviceName);

                } catch (IOException e) {
                    e.printStackTrace();
                    // 에러 처리
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void updateDeviceNumberUI(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 기계번호를 TextView에 설정
                deviceNumberTextView.setText(response);
            }
        });
    }

    private void updateDeviceNameUI(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 기계명을 TextView에 설정
                deviceNameTextView.setText(response);
            }
        });
    }
}