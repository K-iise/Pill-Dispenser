package com.capstone.pilldispenser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import com.capstone.pilldispenser.HttpHandler;
import com.google.android.material.navigation.NavigationView;

public class Pill_detail extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private String pillNumber;
    String storage;
    String caution;
    String sideEffect;
    String efficacy;

    DrawerLayout drawer;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_detail);

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

        pillNumber = getIntent().getStringExtra("pill_number");
        new getPillDetails().execute(pillNumber);

        // 효능 효과 버튼 클릭 이벤트 설정
        RadioButton efficacyButton = findViewById(R.id.efficacy_button);
        efficacyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView informationTextView = findViewById(R.id.information_text);
                TextView textinformationTextView = findViewById(R.id.text_information);
                textinformationTextView.setText("❖ 효능 · 효과");
                informationTextView.setText(efficacy);
            }
        });

        // 주의 사항 버튼 클릭 이벤트 설정
        RadioButton cautionButton = findViewById(R.id.caution_button);
        cautionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView informationTextView = findViewById(R.id.information_text);
                TextView textinformationTextView = findViewById(R.id.text_information);
                textinformationTextView.setText("❖ 주의사항");
                informationTextView.setText(caution);
            }
        });

        // 부작용 버튼 클릭 이벤트 설정
        RadioButton sideEffectButton = findViewById(R.id.side_effect_button);
        sideEffectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView informationTextView = findViewById(R.id.information_text);
                TextView textinformationTextView = findViewById(R.id.text_information);
                textinformationTextView.setText("❖ 부작용");
                informationTextView.setText(sideEffect);
            }
        });

        // 보관법 버튼 클릭 이벤트 설정
        RadioButton storageButton = findViewById(R.id.storage_button);
        storageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView informationTextView = findViewById(R.id.information_text);
                TextView textinformationTextView = findViewById(R.id.text_information);
                textinformationTextView.setText("❖ 보관법");
                informationTextView.setText(storage);
            }
        });

    }


    private class getPillDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String pillNumber = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getPillDetails.jsp?pillNumber=" + pillNumber;
            Log.d("getPillDetails", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("getPillDetails", "Response: " + response); // 응답 로그 출력
            return response;
        }
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                String pillName  = jsonObject.getString("pillName");
                String companyName = jsonObject.getString("companyName");
                storage   = jsonObject.getString("storage");
                caution  = jsonObject.getString("caution");
                sideEffect  = jsonObject.getString("side_effect");
                efficacy  = jsonObject.getString("efficacy");


                TextView pillnameTextView = (TextView) findViewById(R.id.pill_name);
                TextView companynameTextView = (TextView) findViewById(R.id.company_name);

                pillnameTextView.setText(pillName);
                companynameTextView.setText(companyName);
                // AsyncTask의 결과를 받아서 UI 업데이트를 수행하는 메서드 호출

            } catch (Exception e) {
                e.printStackTrace();
            }
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