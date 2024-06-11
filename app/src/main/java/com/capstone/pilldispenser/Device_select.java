package com.capstone.pilldispenser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import java.io.ByteArrayOutputStream;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import com.capstone.pilldispenser.R;

public class Device_select extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private LinearLayout linearLayout;
    private RelativeLayout deviceLayout;
    private String deviceNumber;
    private String deviceName;
    DrawerLayout drawer;
    private String userId;
    byte[] byteArray;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;

    // 블루투스 추가 부분.
    private static final String TAG = "pilldispenser";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // HC-06 UUID
    private static final String DEVICE_ADDRESS = "98:DA:60:0A:F9:F4"; // HC-06 MAC 주소 (HC-06 모듈의 실제 MAC 주소로 바꿔야 합니다)

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_select);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
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
        linearLayout = (LinearLayout) findViewById(R.id.device_Linear);

        new GetDeviceInfo().execute(userId);

        // 기기 삭제 Button 생성.
        ImageButton deleteButton = (ImageButton) findViewById(R.id.action_delete);

        // 기기 삭제 버튼 클릭 메소드
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Device_delete 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Device_select.this, Device_delete.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
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
                finish();
                startActivity(intent);
            }
        });

        // 회원명, 현재 시간 표시
        new GetUserName().execute(userId);

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
                addDeviceButton();
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

    // 등록된 기기들의 이미지 뷰를 선택하면 알약 선택화면으로 전환하는 함수.
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
        connectBluetooth();
        // Pill_select 액티비티로 전달할 Intent 생성
        Intent intent = new Intent(this, Pill_select.class);
        intent.putExtra("deviceNumber", deviceNumber);
        intent.putExtra("deviceName", deviceName);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
        startActivity(intent);

    }

    // 기기 등록 버튼을 호출하는 버튼 추가하는 함수.
    private void addDeviceButton() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View deviceButton = inflater.inflate(R.layout.device_add,null);

        // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
        int marginInDp = 20; // 원하는 마진 값을 dp 단위로 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, marginInDp, 0, marginInDp);

        // 뷰에 레이아웃 파라미터 설정
        deviceButton.setLayoutParams(layoutParams);

        linearLayout.addView(deviceButton);
    }

    public void onAddButtonClick(View view) {

        // add_button 클릭 시 Device_register 액티비티로 이동하면서 userId 전달
        Intent intent = new Intent(Device_select.this, Device_register.class);
        intent.putExtra("userId", userId);
        intent.putExtra("userName", userName);
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
            intent.putExtra("deviceNumber", deviceNumber);
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

    // DB에서 로그인된 회원의 이름을 가져오는 함수
    private class GetUserName extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getuserName.jsp?userId=" + userId;
            Log.d("GetUserName", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetUserName", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                userName = jsonObject.getString("userName");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void connectBluetooth() {
        // 블루투스 권한 확인
        if (checkBluetoothPermission()) {
            // 블루투스 연결 시도
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                MyApplication app = (MyApplication) getApplication();
                app.setOutputStream(outputStream);
                Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Error connecting to Bluetooth device", e);
                Toast.makeText(this, "Bluetooth Connection Failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (Exception ex) {
                Log.e(TAG, "Error connecting to Bluetooth device", ex);
                Toast.makeText(this, "Bluetooth Connection Failed", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        }
    }


    // 블루투스 권한 확인 및 요청
    private boolean checkBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            // 블루투스 권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            return false;
        } else {
            // 블루투스 권한이 있는 경우
            return true;
        }
    }

    // onRequestPermissionsResult() 메서드에서 권한 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 블루투스 권한 허용됨
                // 블루투스 연결 시도
                connectBluetooth();
            } else {
                // 블루투스 권한 거부됨
                // 사용자에게 권한 요청에 대한 설명 표시 또는 다른 조치 수행
            }
        }
    }





}