package com.capstone.pilldispenser;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Pill_edit extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Pill_addition";

    private String selectedPillNumber = "";
    private String selectedPillName = "";

    private String userId;

    DrawerLayout drawer;

    // 회원명, 현재 시간 표시에 쓰는 변수들.
    private TextView memberTimeTextView;
    private Handler handler;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_edit);

        new RetrieveDataTask().execute();

        initSpinners();

        String deviceNumber = getIntent().getStringExtra("deviceNumber");
        String cabinetNumber = getIntent().getStringExtra("cabinetNumber");
        TextView storageNumberTextView = findViewById(R.id.storage_number);
        storageNumberTextView.setText(cabinetNumber);

        // Menu Button, Drawer 생성
        ImageButton menuButton = (ImageButton) findViewById(R.id.action_ham);
        drawer = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!drawer.isDrawerOpen(Gravity.LEFT)) {
                    drawer.openDrawer(Gravity.LEFT);
                }
            }
        });

        findViewById(R.id.modify_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 알약명 가져오기
                TextView pillNameTextView = findViewById(R.id.pill_name_textview);
                pillNameTextView.setText(selectedPillName);

                // 수량 가져오기
                EditText quantityEditText = findViewById(R.id.quantity_edittext);
                String quantity = quantityEditText.getText().toString().trim();

                // 선택된 알약번호 가져오기
                String pillNumber = selectedPillNumber;

                // 약통번호 가져오기
                TextView storageNumberTextView = findViewById(R.id.storage_number);
                String storageNumber = storageNumberTextView.getText().toString().trim();

                // 기계번호 가져오기
                String deviceNumber = getIntent().getStringExtra("deviceNumber");

                // AsyncTask를 사용하여 데이터베이스 업데이트 실행
                new UpdateDataTask().execute(pillNumber, selectedPillName, quantity, storageNumber, deviceNumber);
            }
        });

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 알약 검색 버튼 클릭 이벤트 설정
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 스피너에서 선택된 항목 가져오기
                Spinner spinner = findViewById(R.id.drop_down);
                String selectedOption = spinner.getSelectedItem().toString();

                // 입력된 검색어 가져오기
                EditText searchText = findViewById(R.id.search_text);
                String searchKeyword = searchText.getText().toString().trim();

                // TableRow들을 모두 숨기기
                hideAllTableRows();

                // 입력된 검색어와 선택된 옵션에 따라 검색 기능 실행
                if (selectedOption.equals("알약이름")) {
                    // 알약 이름으로 검색
                    showMatchingTableRowsByPillName(searchKeyword);
                } else if (selectedOption.equals("제조업체")) {
                    // 제조업체로 검색
                    showMatchingTableRowsByCompanyName(searchKeyword);
                }
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

    private class RetrieveDataTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL("http://61.79.73.178:8080/PillJSP/Android/addInfo.jsp");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error ", e);
                return null;
            }
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONArray jsonArray = new JSONArray(result);
                    TableLayout tableLayout = findViewById(R.id.search_contents);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String pillNumber = jsonObject.getString("pill_number");
                        String pillName = jsonObject.getString("pill_name");
                        String companyName = jsonObject.getString("company_name");

                        TableRow tableRow = new TableRow(getApplicationContext());

                        TextView textView1 = new TextView(getApplicationContext());
                        textView1.setId(R.id.pillnum);
                        textView1.setLayoutParams(new TableRow.LayoutParams(0, 90, 1));
                        textView1.setGravity(Gravity.CENTER);
                        textView1.setTextColor(Color.parseColor("#111111"));
                        textView1.setBackgroundResource(R.drawable.pill_addition_search);
                        textView1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        textView1.setText(pillNumber);
                        tableRow.addView(textView1);

                        TextView textView2 = new TextView(getApplicationContext());
                        textView2.setId(R.id.pillname);
                        textView2.setLayoutParams(new TableRow.LayoutParams(0, 90, 2));
                        textView2.setGravity(Gravity.CENTER);
                        textView2.setTextColor(Color.parseColor("#111111"));
                        textView2.setBackgroundResource(R.drawable.pill_addition_search);
                        textView2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        textView2.setText(pillName);
                        tableRow.addView(textView2);

                        TextView textView3 = new TextView(getApplicationContext());
                        textView3.setId(R.id.pillmade);
                        textView3.setLayoutParams(new TableRow.LayoutParams(0, 90, 1));
                        textView3.setGravity(Gravity.CENTER);
                        textView3.setTextColor(Color.parseColor("#111111"));
                        textView3.setBackgroundResource(R.drawable.pill_addition_search);
                        textView3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        textView3.setText(companyName);
                        tableRow.addView(textView3);

                        tableRow.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TextView clickedPillNumberTextView = (TextView) ((TableRow) v).findViewById(R.id.pillnum);
                                selectedPillNumber = clickedPillNumberTextView.getText().toString();

                                TextView clickedPillNameTextView = (TextView) ((TableRow) v).findViewById(R.id.pillname);
                                selectedPillName = clickedPillNameTextView.getText().toString();

                                TextView pillNameTextView = findViewById(R.id.pill_name_textview);
                                pillNameTextView.setText(selectedPillName);
                            }
                        });

                        tableLayout.addView(tableRow);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error ", e);
                }
            }
        }
    }

    private class UpdateDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String pillNumber = params[0];
            String pillName = params[1];
            String quantity = params[2];
            String storageNumber = params[3];
            String deviceNumber = params[4];

            try {
                // POST 요청 보내기
                URL url = new URL("http://61.79.73.178:8080/PillJSP/Android/updatePill.jsp");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 요청 파라미터 설정
                String postData = "machinenumber=" + URLEncoder.encode(deviceNumber, "UTF-8")
                        + "&storagenumber=" + URLEncoder.encode(storageNumber, "UTF-8")
                        + "&pillnumber=" + URLEncoder.encode(pillNumber, "UTF-8")
                        + "&pillname=" + URLEncoder.encode(pillName, "UTF-8")
                        + "&quantity=" + URLEncoder.encode(quantity, "UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(postData.getBytes());
                os.flush();
                os.close();

                // 응답 받기
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // 연결 종료
                conn.disconnect();

                return response.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && result.contains("success")) {
                Toast.makeText(getApplicationContext(), "데이터 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "데이터가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initSpinners() {
        ArrayList<String> stringCategory = new ArrayList<String>();
        stringCategory.add("알약이름");
        stringCategory.add("제조업체");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stringCategory);

        ((Spinner) findViewById(R.id.drop_down)).setAdapter(adapter);

        TextView storageNumberTextView = findViewById(R.id.storage_number); // 변경된 부분
    }

    // 모든 TableRow를 숨기는 메서드
    private void hideAllTableRows() {
        TableLayout tableLayout = findViewById(R.id.search_contents);
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                child.setVisibility(View.GONE);
            }
        }
    }

    // 알약 이름으로 TableRow를 보이도록 설정하는 메서드
    private void showMatchingTableRowsByPillName(String searchKeyword) {
        TableLayout tableLayout = findViewById(R.id.search_contents);
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                TextView pillNameTextView = row.findViewById(R.id.pillname);
                if (pillNameTextView != null) {
                    String pillName = pillNameTextView.getText().toString().trim();
                    if (pillName.toLowerCase().contains(searchKeyword.toLowerCase())) {
                        row.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    // 제조업체명으로 TableRow를 보이도록 설정하는 메서드
    private void showMatchingTableRowsByCompanyName(String searchKeyword) {
        TableLayout tableLayout = findViewById(R.id.search_contents);
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            View child = tableLayout.getChildAt(i);
            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;
                TextView companyNameTextView = row.findViewById(R.id.pillmade);
                if (companyNameTextView != null) {
                    String companyName = companyNameTextView.getText().toString().trim();
                    if (companyName.toLowerCase().contains(searchKeyword.toLowerCase())) {
                        row.setVisibility(View.VISIBLE);
                    }
                }
            }
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

    // 메뉴바 클릭 이벤트.
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
}