package com.capstone.pilldispenser;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;

public class Alarm_select extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_SCHEDULE_EXACT_ALARM = 1;
    private LinearLayout linearLayout;
    private String userId;
    private String deviceNumber;
    DrawerLayout drawer;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_select);

        // Menu Button, Drawer 생성
        ImageButton menuButton = findViewById(R.id.action_ham);
        drawer = findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 메뉴바 클릭 이벤트(Drawer 출력)
        menuButton.setOnClickListener(v -> {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        // 이전 화면으로부터 데이터 받아오기
        userId = getIntent().getStringExtra("userId");
        deviceNumber = getIntent().getStringExtra("deviceNumber");

        // activity_main.xml에서 ScrollView 안의 LinearLayout을 참조
        linearLayout = findViewById(R.id.alarm_linear);

        // 권한 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM}, REQUEST_CODE_SCHEDULE_EXACT_ALARM);
        } else {
            new GetAlarmInfo().execute(userId);
        }

        // 회원명, 현재 시간 표시
        userName = getIntent().getStringExtra("userName");

        // TextView 찾기
        memberTimeTextView = findViewById(R.id.membertime);

        // Handler 생성
        handler = new Handler();

        // Runnable 생성 및 실행
        handler.post(updateTimeRunnable);

        // 알람 삭제 버튼 생성.
        ImageButton option = (ImageButton) findViewById(R.id.option);

        // 알람 삭제 버튼 클릭 이벤트.
        option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Alarm_delete 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Alarm_select.this, Alarm_delete.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("deviceNumber", deviceNumber);
                startActivity(intent);

            }
        });

        // 알람 생성 버튼 생성.
        ImageButton add = (ImageButton) findViewById(R.id.add);

        // 알람 생성 버튼 클릭 이벤트
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alarm_setting 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Alarm_select.this, Alarm_setting.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("deviceNumber", deviceNumber);
                startActivity(intent);
            }
        });

        // 새로 고침 Button 생성.
        ImageButton returnButton = (ImageButton) findViewById(R.id.action_return);

        // 새로 고침 클릭 메소드.
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("deviceNumber", deviceNumber);
                finish();
                startActivity(intent);
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new GetAlarmInfo().execute(userId);
            } else {
                Log.e("Alarm_select", "Permission denied to schedule exact alarms");
            }
        }
    }

    private class GetAlarmInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getAlarmInfo.jsp?userId=" + userId;
            Log.d("GetAlarmInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetAlarmInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String Devicenumber = jsonObject.getString("Devicenumber");
                    String Alarmtime = jsonObject.getString("Alarmtime");
                    String Alarmday = jsonObject.getString("Alarmday");
                    int hasTaken = jsonObject.getInt("hasTaken");

                    if(hasTaken == 1) {
                        addAlarmView(Devicenumber, Alarmtime, Alarmday);
                        // 알람 설정
                        Log.d("Alarm_setAlarm", "Setting alarm for: " + Alarmday + " " + Alarmtime);
                        setAlarm(Alarmday, Alarmtime, i);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addAlarmView(String Devicenumber, String Alarmtime, String Alarmday) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alarmView = inflater.inflate(R.layout.alarm_bring, null);

        TextView devicenumber = alarmView.findViewById(R.id.Devicenumber);
        TextView alarmtime = alarmView.findViewById(R.id.Alarmtime);
        SwitchCompat alarmday = alarmView.findViewById(R.id.Alarmday);

        devicenumber.setText(Devicenumber);
        alarmtime.setText(Alarmtime);
        alarmday.setText(Alarmday);

        // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
        int marginInDp = 50; // 원하는 마진 값을 dp 단위로 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, marginInDp);

        // 뷰에 레이아웃 파라미터 설정
        alarmView.setLayoutParams(layoutParams);

        linearLayout.addView(alarmView);
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
            // 추가 작업을 여기에 작성
            // 복용 기록 조회 메뉴 클릭 시 Pill_record 액티비티로 이동하면서 userId 전달
            Intent intent = new Intent(this, Pill_record.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        } else if (itemId == R.id.menu_logout) {
            Intent intent = new Intent(this, LoginUI.class);
            startActivity(intent);
            // 추가 작업을 여기에 작성 (예: 로그아웃 기능)
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setAlarm(String alarmDay, String alarmTime, int alarmIndex) {
        // 알람 시간을 파싱하기 위한 날짜 형식 설정
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            // 알람 날짜와 시간을 결합하여 파싱
            Date date = dateFormat.parse(alarmDay + " " + alarmTime);
            if (date != null) {
                // Calendar 객체에 파싱된 날짜와 시간 설정
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // 현재 시간이 이미 지난 경우, 알람을 다음 날로 설정
                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                // AlarmManager 객체 가져오기
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                // 알람이 발생했을 때 실행할 Intent 생성
                Intent intent = new Intent(this, AlarmReceiver.class);
                intent.putExtra("alarmDay", alarmDay);
                intent.putExtra("alarmTime",alarmTime);
                intent.putExtra("deviceNumber",deviceNumber);
                intent.putExtra("userId",userId);

                // PendingIntent 생성: 알람이 발생할 때 실행될 인텐트
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, alarmIndex, intent, PendingIntent.FLAG_IMMUTABLE);

                // AlarmManager가 null이 아닌지 확인
                if (alarmManager != null) {
                    // 정확한 알람 설정이 가능한지 확인
                    if (alarmManager.canScheduleExactAlarms()) {
                        // 정확한 시간에 알람 설정 (RTC_WAKEUP은 장치를 깨운다)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        Log.d("Alarm_setAlarm", "Alarm set for: " + calendar.getTime().toString());
                    } else {
                        // 정확한 알람 설정이 불가능한 경우에 대한 처리
                        Log.e("Alarm_setAlarm", "Cannot schedule exact alarms");
                    }
                }
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
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
