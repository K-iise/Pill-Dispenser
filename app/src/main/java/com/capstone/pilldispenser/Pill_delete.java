package com.capstone.pilldispenser;

import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class Pill_delete extends AppCompatActivity {

    private LinearLayout linearLayout;
    private TextView deviceNumberTextView;
    private TextView deviceNameTextView;
    private String pillNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_delete);

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

        // 알약 삭제 확인 버튼.
        ImageButton checkButton = (ImageButton) findViewById(R.id.check_button);

        // 알약 조회 화면으로 돌아가는 메소드.
        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // DeviceNumber TextView 찾기
                TextView DeviceNumberView = (TextView) findViewById(R.id.devicenump);

                // DeviceName TextView 찾기
                TextView DeviceNameView = (TextView) findViewById(R.id.devicenamp);


                // DeviceName, DeviceNumber텍스트 가져오기
                String deviceNumber = DeviceNumberView.getText().toString();
                String deviceName = DeviceNameView.getText().toString();

                // Pill_select 액티비티로 전달할 Intent 생성
                Intent intent = new Intent(Pill_delete.this, Pill_select.class);
                intent.putExtra("deviceNumber", deviceNumber);
                intent.putExtra("deviceName", deviceName);
                startActivity(intent);
            }
        });

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

                    addPillView(cabinetNumber, pillNumber, pillName, remainPill);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addPillView(String cabinetNumber, String pillNumber, String pillName, String remainingPills) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View pillView = inflater.inflate(R.layout.pill_delete_bring,null);

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

        linearLayout.addView(pillView);
    }

    public void onDeleteButtonClick(View view) {
        new AlertDialog.Builder(Pill_delete.this)
                .setTitle("삭제 확인")
                .setMessage("삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // "예" 버튼을 눌렀을 때의 동작을 여기에 작성합니다.

                        // 클릭된 정보 버튼의 부모 RelativeLayout 찾기
                        RelativeLayout DeviceLayout = (RelativeLayout) view.getParent();

                        DeviceLayout = (RelativeLayout) DeviceLayout.getParent();

                        // 약통번호 찾기.
                        TextView cabinetNumberView = DeviceLayout.findViewById(R.id.cabinet_number);
                        String cabinetnumber = cabinetNumberView.getText().toString();
                        cabinetnumber = cabinetnumber.substring(5);

                        // DeviceNumber  찾기
                        TextView DeviceNumberView = (TextView) findViewById(R.id.devicenump);
                        String devicenumber = DeviceNumberView.getText().toString();

                        // 함수 실행.
                        new deletePill().execute(cabinetnumber, devicenumber);

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
    private class deletePill extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String cabinetnumber = params[0];
            String devicenumber = params[1];
            String url = "http://61.79.73.178:8080/PillJSP/Android/deletePill.jsp?cabinetnumber=" + cabinetnumber + "&devicenumber=" + devicenumber;
            Log.d("deletePill", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("deletePill", "Response: " + response); // 응답 로그 출력
            return response;
        }

    }
}