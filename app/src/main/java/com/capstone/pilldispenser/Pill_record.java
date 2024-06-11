package com.capstone.pilldispenser;

import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Pill_record extends AppCompatActivity {

    DrawerLayout drawer;
    private String userId;

    private TableLayout tableLayout;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_record);

        // 이전 화면으로부터 데이터 받아오기
        userId = getIntent().getStringExtra("userId");

        // 전체 복용 기록 조회.

        // activity_pill_record.xml에서 ScrollView 안의 TableLayout 참조
        tableLayout = (TableLayout) findViewById(R.id.search_contents);

        new GetRecordInfo().execute(userId);

        // 최근 1개월간 복용 기록 조회.
        Button recentonemonth = (Button) findViewById(R.id.recentOneMonth);
        recentonemonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 테이블 레이아웃의 모든 자식 뷰 제거
                tableLayout.removeAllViews();
                new GetRecordInfoRecent().execute(userId, "-1");
            }
        });

        // 최근 3개월간 복용 기록 조회.
        Button recentthreemonth = (Button) findViewById(R.id.recentThreeMonth);
        recentthreemonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 테이블 레이아웃의 모든 자식 뷰 제거
                tableLayout.removeAllViews();
                new GetRecordInfoRecent().execute(userId, "-3");
            }
        });

        // 최근 6개월간 복용 기록 조회.
        Button recentsixmonth = (Button) findViewById(R.id.recentSixMonth);
        recentsixmonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 테이블 레이아웃의 모든 자식 뷰 제거
                tableLayout.removeAllViews();
                new GetRecordInfoRecent().execute(userId, "-6");
            }
        });

        // 최근 1년간 복용 기록 조회.
        Button recentoneyear = (Button) findViewById(R.id.recentOneYear);
        recentoneyear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 테이블 레이아웃의 모든 자식 뷰 제거
                tableLayout.removeAllViews();
                new GetRecordInfoRecent().execute(userId, "-12");
            }
        });

        // 직접입력을 통해서 특정 기간 복용 기록 조회.
        CheckBox directcheckbox = (CheckBox) findViewById(R.id.direct_check);
        EditText startEditText = (EditText) findViewById(R.id.start_edit);
        EditText endEditText = (EditText) findViewById(R.id.end_edit);

        directcheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // CheckBox의 체크 여부에 따라 EditText와 ImageButton의 활성화 여부를 변경합니다.
                startEditText.setEnabled(isChecked);
                endEditText.setEnabled(isChecked);
            }
        });

        // 달력 버튼을 클릭해서 특정 기간 복용 기록을 조회.
        ImageButton StartCalender = (ImageButton) findViewById(R.id.start_calender);
        StartCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar calendar;
                calendar = Calendar.getInstance();
                // 현재 날짜를 기본으로 설정
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // 달력 다이얼로그 생성
                DatePickerDialog datePickerDialog = new DatePickerDialog(Pill_record.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // 날짜 설정 후 텍스트뷰에 표시
                                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = dateFormat.format(calendar.getTime());
                                startEditText.setText(formattedDate);



                            }
                        }, year, month, dayOfMonth);

                // 다이얼로그 표시
                datePickerDialog.show();
            }
        });

        ImageButton EndCalender = (ImageButton) findViewById(R.id.end_calender);
        EndCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar;
                calendar = Calendar.getInstance();
                // 현재 날짜를 기본으로 설정
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

                // 달력 다이얼로그 생성
                DatePickerDialog datePickerDialog = new DatePickerDialog(Pill_record.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                                // 날짜 설정 후 텍스트뷰에 표시
                                calendar.set(selectedYear, selectedMonth, selectedDayOfMonth);

                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                String formattedDate = dateFormat.format(calendar.getTime());
                                endEditText.setText(formattedDate);



                            }
                        }, year, month, dayOfMonth);

                // 다이얼로그 표시
                datePickerDialog.show();
            }
        });
    }

    // DB의 복용기록 테이블에서 전체 정보를 가져오는 메소드.
    private class GetRecordInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getRecordInfo.jsp?userId=" + userId;
            Log.d("GetRecordInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetRecordInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String alarmDate = jsonObject.getString("alarmDate");
                    String alarmTime = jsonObject.getString("alarmTime");
                    String pillName = jsonObject.getString("pillName");
                    String quantity = jsonObject.getString("quantity");
                    alarmDate = alarmDate.substring(0, 10);
                    addTableRowView(alarmDate, alarmTime, pillName,quantity);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // DB의 복용기록 테이블에서 최근 1달 ~ 1년의 정보를 가져오는 메소드.
    private class GetRecordInfoRecent extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String userId = params[0];
            String recent = params[1];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getRecordInfoRecent.jsp?userId=" + userId + "&recent=" + recent;
            Log.d("GetRecordOneMonthInfo", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("GetRecordOneMonthInfo", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String alarmDate = jsonObject.getString("alarmDate");
                    String alarmTime = jsonObject.getString("alarmTime");
                    String pillName = jsonObject.getString("pillName");
                    String quantity = jsonObject.getString("quantity");
                    alarmDate = alarmDate.substring(0, 10);
                    addTableRowView(alarmDate, alarmTime, pillName,quantity);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 동적으로 TableLayout에 TableRow를 추가하는 메소드.
    private void addTableRowView(String alarmDate, String alarmTime, String pillName, String quantity) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View TableRowView = inflater.inflate(R.layout.record_bring,null);

        TextView alarmdate = TableRowView.findViewById(R.id.alarmDate);
        TextView alarmtime = TableRowView.findViewById(R.id.alarmTime);
        TextView pillname = TableRowView.findViewById(R.id.pillName);
        TextView Quantity = TableRowView.findViewById(R.id.quantity);

        alarmdate.setText(alarmDate);
        alarmtime.setText(alarmTime);
        pillname.setText(pillName);
        Quantity.setText(quantity);

        // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
        int marginInDp = 0; // 원하는 마진 값을 dp 단위로 설정
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, marginInDp, 0, marginInDp);

        // 뷰에 레이아웃 파라미터 설정
        TableRowView.setLayoutParams(layoutParams);

        tableLayout.addView(TableRowView);
    }
}