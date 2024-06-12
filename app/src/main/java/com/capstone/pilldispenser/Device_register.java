package com.capstone.pilldispenser;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;

public class Device_register extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText deviceIdEditText, deviceNameEditText, manufactureDateEditText;
    private Button registerButton, duplicateCheckButton;
    private String userId;
    DrawerLayout drawer;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_register);

        // Menu Button, Drawer 생성
        ImageButton menuButton = (ImageButton) findViewById(R.id.action_ham);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 메뉴바 클릭 이벤트(Drawer 출력)
        menuButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT) ;
                }
            }
        });

        // 이전 화면으로부터 userID 받아오기
        userId = getIntent().getStringExtra("userId");

        deviceIdEditText = findViewById(R.id.editTextId_Reg);
        deviceNameEditText = findViewById(R.id.editTextId);
        manufactureDateEditText = findViewById(R.id.editTextday);
        registerButton = findViewById(R.id.register_button);
        duplicateCheckButton = findViewById(R.id.btnCheckNick_Reg);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerDevice();
            }
        });

        duplicateCheckButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceId = deviceIdEditText.getText().toString();
                if (!deviceId.isEmpty()) {
                    checkDuplicateDevice(deviceId);
                } else {
                    Toast.makeText(Device_register.this, "기계번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Device_register.this, Device_select.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

        // 회원명, 현재 시간 표시
        userName = getIntent().getStringExtra("userName");

        // TextView 찾기
        memberTimeTextView = findViewById(R.id.membertime);

        // Handler 생성
        handler = new Handler();

        // Runnable 생성 및 실행
        handler.post(updateTimeRunnable);


    }

    private void registerDevice() {
        String deviceId = deviceIdEditText.getText().toString();
        String deviceName = deviceNameEditText.getText().toString();
        String manufactureDate = manufactureDateEditText.getText().toString();

        String url = "http://61.79.73.178:8080/PillJSP/Android/device_registerDB.jsp";

        new RegisterDeviceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, deviceId, deviceName, manufactureDate, userId);
    }

    private void checkDuplicateDevice(String deviceId) {
        String url = "http://61.79.73.178:8080/PillJSP/Android/check_device_duplicate.jsp";

        new CheckDuplicateDeviceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, deviceId);
    }

    private class RegisterDeviceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                String deviceId = params[1];
                String deviceName = params[2];
                String manufactureDate = params[3];
                String userId = params[4];

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                String postData = "deviceId=" + URLEncoder.encode(deviceId, "UTF-8") +
                        "&deviceName=" + URLEncoder.encode(deviceName, "UTF-8") +
                        "&manufactureDate=" + URLEncoder.encode(manufactureDate, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8");
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "기기 등록 성공!";
                } else {
                    return "서버 오류 발생";
                }
            } catch (Exception e) {
                Log.e("RegisterUI", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Device_register.this, result, Toast.LENGTH_SHORT).show();
            if (result.equals("기기 등록 성공!")) {
                String deviceId = deviceIdEditText.getText().toString();
                new InsertDeviceInfoTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, deviceId, userId);
            }
        }
    }

    private class CheckDuplicateDeviceTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                String deviceId = params[1];

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                String postData = "deviceId=" + URLEncoder.encode(deviceId, "UTF-8");
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 응답 메시지 읽기
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return "서버 오류 발생";
                }
            } catch (Exception e) {
                Log.e("CheckDuplicate", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("duplicate")) {
                Toast.makeText(Device_register.this, "이미 사용 중인 기계입니다", Toast.LENGTH_SHORT).show();
            } else if (result.equals("unique")) {
                Toast.makeText(Device_register.this, "사용 가능한 기계입니다", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Device_register.this, result, Toast.LENGTH_SHORT).show();
            }
        }
    }

    // DB에 기기 정보를 삽입하는 함수.
    private class InsertDeviceInfoTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String deviceId = params[0];
                String userId = params[1];

                String url = "http://61.79.73.178:8080/PillJSP/Android/addInfo.jsp";

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                String postData = "deviceId=" + URLEncoder.encode(deviceId, "UTF-8") +
                        "&userId=" + URLEncoder.encode(userId, "UTF-8");
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return "등록 정보 추가 성공!";
                } else {
                    return "서버 오류 발생";
                }
            } catch (Exception e) {
                Log.e("InsertDeviceInfo", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(Device_register.this, result, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_alarm) {
            // 알람 조회 메뉴 클릭 시 Alarm_select 액티비티로 이동하면서 userId 전달
            Intent intent = new Intent(this, Alarm_select.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            startActivity(intent);
            // 추가 작업을 여기에 작성 (예: 새로운 액티비티 시작)
        } else if (itemId == R.id.menu_record) {
            // 복용 기록 조회 메뉴 클릭 시 Pill_record 액티비티로 이동하면서 userId 전달
            Intent intent = new Intent(this, Pill_record.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            // 추가 작업을 여기에 작성
        } else if (itemId == R.id.menu_logout) {

            Intent intent = new Intent(this, LoginUI.class);
            startActivity(intent);
            // 추가 작업을 여기에 작성 (예: 로그아웃 기능)
        }
        drawer.closeDrawer(Gravity.LEFT);
        return true;
    }

    // Runnable 정의
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            // 현재 시간 가져오기
            String currentTime = getCurrentTime();

            // 텍스트 설정
            String memberTimeText = userName + "님. " + currentTime;
            memberTimeTextView.setText(memberTimeText);

            // 다음 업데이트를 위해 Handler에 Runnable 재등록 (일정 시간 간격으로 반복)
            handler.postDelayed(this, 1000); // 1초마다 업데이트
        }
    };

    // 현재 시간을 가져오는 메서드
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd (HH:mm:ss)", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티가 종료될 때 Handler의 Runnable 제거
        handler.removeCallbacks(updateTimeRunnable);
    }


}
