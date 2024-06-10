package com.capstone.pilldispenser;

import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class Alarm_delete extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout linearLayout;

    private String userId;
    DrawerLayout drawer;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_delete);

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

        // 이전 화면으로부터 데이터 받아오기
        userId = getIntent().getStringExtra("userId");

        // activity_main.xml에서 ScrollView 안의 LinearLayout을 참조
        linearLayout = (LinearLayout) findViewById(R.id.alarm_linear);

        new GetAlarmInfo().execute(userId);

        // 회원명, 현재 시간 표시
        userName = getIntent().getStringExtra("userName");

        // TextView 찾기
        memberTimeTextView = findViewById(R.id.membertime);

        // Handler 생성
        handler = new Handler();

        // Runnable 생성 및 실행
        handler.post(updateTimeRunnable);

        // 알람 선택 화면으로 돌아가는 버튼.
        ImageButton checkButton = (ImageButton) findViewById(R.id.check);

        // 알람 선택 화면으로 돌아가는 이벤트.
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Alarm_select 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Alarm_delete.this, Alarm_select.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });

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

                    addAlarmView(Devicenumber, Alarmtime, Alarmday);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addAlarmView(String Devicenumber, String Alarmtime, String Alarmday) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View alarmView = inflater.inflate(R.layout.alarm_delete_bring,null);

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

    public void onDeleteButtonClick(View view) {
        new AlertDialog.Builder(Alarm_delete.this)
                .setTitle("삭제 확인")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // "예" 버튼을 눌렀을 때의 동작을 여기에 작성합니다.

                        // 클릭된 정보 버튼의 부모 RelativeLayout 찾기
                        RelativeLayout AlarmLayout = (RelativeLayout) view.getParent();


                        // 약통번호 찾기.
                        TextView devicenumber = AlarmLayout.findViewById(R.id.Devicenumber);
                        TextView alarmtime = AlarmLayout.findViewById(R.id.Alarmtime);
                        TextView alarmday = AlarmLayout.findViewById(R.id.Alarmday);

                        String Devicenumber = devicenumber.getText().toString();
                        String Alarmtime = alarmtime.getText().toString();
                        String Alarmday = alarmday.getText().toString();


                        // 함수 실행.
                        new deleteAlarm().execute(userId, Devicenumber, Alarmtime, Alarmday);

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // "아니오" 버튼을 눌렀을 때의 동작을 여기에 작성합니다.
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // 회원이 등록한 알람을 DB에서 제거하는 메소드.
    private class deleteAlarm extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String devicenumber = params[1];
            String alarmtime = params[2];
            String alarmday = params[3];
            String url = "http://61.79.73.178:8080/PillJSP/Android/deleteAlarm.jsp?userId=" + userId + "&devicenumber=" + devicenumber + "&alarmtime=" + alarmtime + "&alarmday=" + alarmday;
            Log.d("deleteAlarm", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("deleteAlarm", "Response: " + response); // 응답 로그 출력
            return response;
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