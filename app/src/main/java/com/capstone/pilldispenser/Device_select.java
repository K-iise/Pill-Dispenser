package com.capstone.pilldispenser;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Device_select extends AppCompatActivity {


    private LinearLayout linearLayout;
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

        // activity_main.xml에서 ScrollView 안의 LinearLayout을 참조
        linearLayout = (LinearLayout) findViewById(R.id.device_Linear);

        new GetDeviceInfo().execute(userId);

/*        // RelativeLayout 클릭 이벤트 설정
        deviceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭 이벤트 발생 시 Pill_select 액티비티로 이동
                Intent intent = new Intent(Device_select.this, Pill_select.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("deviceName", deviceName);
                startActivity(intent);
            }
        });*/


    }

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
        View deviceView = inflater.inflate(R.layout.device_bring,null);

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

    public void onInformationButtonClick(View view) {
        // 클릭된 정보 버튼의 부모 RelativeLayout 찾기
        RelativeLayout DeviceLayout = (RelativeLayout) view.getParent();

        // DeviceNumber TextView 찾기
        TextView DeviceNumberView = DeviceLayout.findViewById(R.id.devicenumber);

        // DeviceName TextView 찾기
        TextView DeviceNameView = DeviceLayout.findViewById(R.id.devicename);


        // DeviceName, DeviceNumber텍스트 가져오기
        deviceNumber = DeviceNumberView.getText().toString();
        deviceName = DeviceNameView.getText().toString();

        // Pill_select 액티비티로 전달할 Intent 생성
        Intent intent = new Intent(this, Pill_select.class);
        intent.putExtra("deviceNumber", deviceNumber);
        intent.putExtra("deviceName", deviceName);
        startActivity(intent);

    }


}