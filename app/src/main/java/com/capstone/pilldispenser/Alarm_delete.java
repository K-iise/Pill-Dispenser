package com.capstone.pilldispenser;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class Alarm_delete extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout linearLayout;

    private String userId;

    DrawerLayout drawer;
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

        new GetAlarmInfo().execute("1233");

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
}