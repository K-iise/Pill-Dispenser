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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import com.capstone.pilldispenser.HttpHandler;

public class Pill_select extends AppCompatActivity {
    private LinearLayout linearLayout;
    private TextView deviceNumberTextView;
    private TextView deviceNameTextView;

    private String pillNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_select);

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

        String deviceNum = "12312"; // 실제 기기 번호를 여기에 설정
        // 기기 번호를 사용하여 데이터베이스에서 데이터 가져오기
        new GetPillInfo().execute(deviceNumber);

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
        View pillView = inflater.inflate(R.layout.pill_bring,null);

        TextView cabinetNumberView = pillView.findViewById(R.id.cabinet_number);
        TextView pillNumberView = pillView.findViewById(R.id.pill_number);
        TextView pillNameView = pillView.findViewById(R.id.pill_name);
        TextView remainPillView = pillView.findViewById(R.id.remain_pill);

        cabinetNumberView.setText("약통번호" + cabinetNumber);
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

        // Pill_detail 액티비티 시작
        startActivity(intent);
    }


}