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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class Device_delete extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout linearLayout;
    private RelativeLayout deviceLayout;
    private String deviceNumber;
    private String deviceName;
    DrawerLayout drawer;
    private String userId;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_delete);

        // Menu Button, Drawer 생성
        ImageButton menuButton = (ImageButton) findViewById(R.id.action_ham);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // 메뉴바 클릭 이벤트(Drawer 출력)
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT);
                }
            }
        });

        // 이전 화면으로부터 데이터 받아오기
        userId = getIntent().getStringExtra("userId");


        // 기기 확인 Button 생성. ( 기기 선택 화면으로 돌아가기 위한 )
        ImageButton checkButton = (ImageButton) findViewById(R.id.action_check);

        // 기기 확인 버튼 클릭 메소드
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Device_delete 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Device_delete.this, Device_select.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            }
        });

        // activity_main.xml에서 ScrollView 안의 LinearLayout을 참조
        linearLayout = (LinearLayout) findViewById(R.id.device_Linear);
        new GetDeviceInfo().execute(userId);

        // 새로 고침 Button 생성.
        ImageButton returnButton = (ImageButton) findViewById(R.id.action_return);

        // 새로 고침 클릭 메소드.
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
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

    // DB에서 로그인된 회원이 등록한 기기 정보들을 불러오고 출력하는 함수.
    private class GetDeviceInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getDeviceInfo.jsp?userId=" + userId;
            Log.d("GetDeviceInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetDeviceInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String deviceNumber = jsonObject.getString("deviceNumber");
                    String deviceName = jsonObject.getString("deviceName");
                    String productionDate = jsonObject.getString("productionDate");

                    addDeviceView(deviceNumber, deviceName, productionDate);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addDeviceView(String deviceNumber, String deviceName, String productionDate) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View deviceView = inflater.inflate(R.layout.device_delete_bring, null);

        TextView devicenumbered = deviceView.findViewById(R.id.devicenumber);
        TextView deviceNameed = deviceView.findViewById(R.id.devicename);
        TextView productionDateed = deviceView.findViewById(R.id.register);

        devicenumbered.setText(deviceNumber);
        deviceNameed.setText(deviceName);
        productionDateed.setText(productionDate);

        // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
        int marginInDp = 20; // 원하는 마진 값을 dp 단위로 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, marginInDp, 0, marginInDp);

        // 뷰에 레이아웃 파라미터 설정
        deviceView.setLayoutParams(layoutParams);

        linearLayout.addView(deviceView);
    }

    public void onDeleteButtonClick(View view) {
        new AlertDialog.Builder(Device_delete.this)
                .setTitle("삭제 확인")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // "예" 버튼을 눌렀을 때의 동작을 여기에 작성합니다.

                        // 클릭된 정보 버튼의 부모 RelativeLayout 찾기
                        RelativeLayout DeviceLayout = (RelativeLayout) view.getParent();

                        // DeviceNumber TextView 찾기
                        TextView DeviceNumberView = DeviceLayout.findViewById(R.id.devicenumber);
                        String devicenumber = DeviceNumberView.getText().toString();

                        // 기기 정보 삭제.
                        new deleteDevice().execute(userId, devicenumber);

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

    // 회원이 등록한 기기를 DB에서 제거하는 메소드.
    private class deleteDevice extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String devicenumber = params[1];
            String url = "http://61.79.73.178:8080/PillJSP/Android/deleteDevice.jsp?userId=" + userId + "&devicenumber=" + devicenumber;
            Log.d("GetDeviceInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetDeviceInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }

    }

    // menu_bar 이벤트.
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