package com.capstone.pilldispenser;

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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import com.capstone.pilldispenser.HttpHandler;
import com.google.android.material.navigation.NavigationView;

import java.util.Date;
import java.util.Locale;

public class Pill_select extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private LinearLayout linearLayout;
    private TextView deviceNumberTextView;
    private TextView deviceNameTextView;
    private String pillNumber;
    DrawerLayout drawer;
    private String userId;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_select);

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

        deviceNumberTextView = findViewById(R.id.devicenump);
        deviceNameTextView = findViewById(R.id.devicenamp);

        // 이전 화면으로부터 데이터 받아오기
        String deviceNumber = getIntent().getStringExtra("deviceNumber");
        String deviceName = getIntent().getStringExtra("deviceName");

        // 받아온 데이터를 TextView에 설정
        deviceNumberTextView.setText(deviceNumber);
        deviceNameTextView.setText(deviceName);

        // 저장된 알약 정보를 DB에서 동적으로 추가.

        // activity_main.xml에서 ScrollView 안의 LinearLayout을 참조
        linearLayout = (LinearLayout) findViewById(R.id.scroll_linear);

        // 기기 번호를 사용하여 데이터베이스에서 데이터 가져오기
        new GetPillInfo().execute(deviceNumber);
        
        // 알약 삭제 버튼 생성.
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deletebutton);

        // 알약 삭제 버튼 클릭 이벤트. (알약 삭제 UI 호출)
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // DeviceNumber TextView 찾기
                TextView DeviceNumberView = (TextView) findViewById(R.id.devicenump);

                // DeviceName TextView 찾기
                TextView DeviceNameView = (TextView) findViewById(R.id.devicenamp);


                // DeviceName, DeviceNumber텍스트 가져오기
                String deviceNumber = DeviceNumberView.getText().toString();
                String deviceName = DeviceNameView.getText().toString();

                // Pill_delete 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Pill_select.this, Pill_delete.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("deviceName", deviceName);
                intent.putExtra("userName", userName);
                startActivity(intent);

            }
        });

        Button addButton = findViewById(R.id.addbutton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 약품 추가 버튼 클릭 시 처리할 내용
                Intent intent = new Intent(Pill_select.this, Pill_addition.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("userName", userName);
                startActivity(intent);
            }
        });
        ImageButton editButton = findViewById(R.id.optionbutton);

        // 회원명, 현재 시간 표시
        userName = getIntent().getStringExtra("userName");

        // TextView 찾기
        memberTimeTextView = findViewById(R.id.membertime);

        // Handler 생성
        handler = new Handler();

        // Runnable 생성 및 실행
        handler.post(updateTimeRunnable);


    }

    private class GetPillInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String deviceNum = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getPillInfo.jsp?deviceNum=" + deviceNum;
            Log.d("GetPillInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetPillInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String cabinetNumber = jsonObject.getString("cabinetNumber");
                    String pillNumber = jsonObject.getString("pillNumber");
                    String pillName = jsonObject.getString("pillName");
                    String remainPill = jsonObject.getString("remainPill");

                    addPillView(deviceNumberTextView.getText().toString(), cabinetNumber, pillNumber, pillName, remainPill);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addPillView(String deviceNumber, String cabinetNumber, String pillNumber, String pillName, String remainingPills) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pillView = inflater.inflate(R.layout.pill_bring, null);

        TextView cabinetNumberView = pillView.findViewById(R.id.cabinet_number);
        TextView pillNumberView = pillView.findViewById(R.id.pill_number);
        TextView pillNameView = pillView.findViewById(R.id.pill_name);
        TextView remainPillView = pillView.findViewById(R.id.remain_pill);

        cabinetNumberView.setText("약통번호 " + cabinetNumber);
        pillNumberView.setText(pillNumber);
        pillNameView.setText(pillName);
        remainPillView.setText(remainingPills + "정");

        // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
        int marginInDp = 20; // 원하는 마진 값을 dp 단위로 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, marginInDp);

        // 뷰에 레이아웃 파라미터 설정
        pillView.setLayoutParams(layoutParams);

        // RelativeLayout 찾기
        RelativeLayout deviceLayout = pillView.findViewById(R.id.device);

        // RelativeLayout 클릭 이벤트 설정
        deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Pill_select.this, Pill_edit.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("cabinetNumber", cabinetNumber);
                intent.putExtra("pillNumber", pillNumber);
                intent.putExtra("pillName", pillName);
                intent.putExtra("remainingPills", remainingPills);
                startActivity(intent);
            }
        });

        linearLayout.addView(pillView);
    }

    public void onInformationButtonClick(View view) {
        // 클릭된 정보 버튼의 부모 RelativeLayout 찾기
        RelativeLayout pillLayout = (RelativeLayout) view.getParent();

        // pill_number TextView 찾기
        TextView pillNumberView = pillLayout.findViewById(R.id.pill_number);

        // pill_number 텍스트 가져오기
        pillNumber = pillNumberView.getText().toString();

        // Pill_detail 액티비티로 전달할 Intent 생성
        Intent intent = new Intent(this, Pill_detail.class);
        intent.putExtra("pill_number", pillNumber);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        // Pill_detail 액티비티 시작
        startActivity(intent);
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