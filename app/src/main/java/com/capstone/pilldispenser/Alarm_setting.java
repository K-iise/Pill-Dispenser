package com.capstone.pilldispenser;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

public class Alarm_setting extends AppCompatActivity {

    private final static int REQUESTCODE_RINGTONE_PICKER = 1000;

    private String mRingtoneUri; // 선택된 벨소리의 URI를 저장할 변수
    private String mRingtoneTitle; // 선택된 벨소리의 제목을 저장할 변수

    private TextView mRingtoneName; // 선택된 벨소리의 이름을 표시할 TextView
    private RelativeLayout mRingtoneLayout; // 벨소리 선택 버튼을 포함하는 레이아웃

    private int addButtonClickCount = 0;
    private static final int MAX_CLICK_COUNT = 5;

    private TextView dayTextView;
    private Calendar calendar;

    private ArrayList<String> selectedDays = new ArrayList<>();

    // 요일 토글 버튼들
    private ToggleButton toggleButtonMon, toggleButtonTue, toggleButtonWed, toggleButtonThu,
            toggleButtonFri, toggleButtonSat, toggleButtonSun;

    private String deviceNumber;
    private String userId;

    private String userName;

    LinkedList<EditText> pillscoreList = new LinkedList<>();
    LinkedList<Spinner> spinnerList = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);

        // Device_register.java에서 기계 번호 받아오기
        deviceNumber = getIntent().getStringExtra("deviceNumber");

        // loginui.java에서 사용자 아이디 받아오기
        userId = getIntent().getStringExtra("userId");
        userName = getIntent().getStringExtra("userName");
        Button confirmButton = findViewById(R.id.check_button);

        EditText pillScore = findViewById(R.id.pill_score);
        pillscoreList.add(pillScore);

        Spinner spinnered = findViewById(R.id.gender_spinner);
        spinnerList.add(spinnered);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자가 입력한 값 받아오기
                String alarmNumber = getNewAlarmNumber();
                String alarmTime = getAlarmTime();
                String alarmDateWithDays = getAlarmDate();

                Log.d("Alarm_setAlarm", "deviceNumber: " + deviceNumber);
                Log.d("Alarm_setAlarm", "Alarm Time: " + alarmTime);
                Log.d("Alarm_setAlarm", "Alarm Date With Days: " + alarmDateWithDays);
                Log.d("Alarm_setAlarm", "quantity: " + pillScore.getText().toString());
                Log.d("Alarm_setAlarm", "userId: " + userId);

                // pillscoreList와 spinnerList의 크기가 같다고 가정합니다.
                for (int i = 0; i < pillscoreList.size(); i++) {
                    String quantity = pillscoreList.get(i).getText().toString();
                    String pillBoxNumber = spinnerList.get(i).getSelectedItem().toString().substring(4);
                    new InsertAlarm().execute(pillBoxNumber, deviceNumber, alarmTime, alarmDateWithDays, userId, "1", quantity);
                }

                // new InsertAlarm().execute("1", "2", "16677", alarmTime, alarmDateWithDays, userId, "1", pillScore.getText().toString());*/

                //알람 조회 메뉴 클릭 시 Alarm_select 액티비티로 이동하면서 userId 전달
                Intent intent = new Intent(Alarm_setting.this, Alarm_select.class);
                intent.putExtra("userId", userId);
                intent.putExtra("userName", userName);
                intent.putExtra("deviceNumber", deviceNumber);
                startActivity(intent);

                //new InsertAlarm().execute(alarmNumber, pillBoxNumber, machineNumber, alarmTime, alarmDateWithDays, userId, "1", quantity);
                //sendDataToServer(alarmNumber, pillNumber, machineNumber, alarmTime, alarmDateWithDays, userId);
            }
        });


        TimePicker timePicker = findViewById(R.id.timePicker);
        dayTextView = findViewById(R.id.daytextView);

        toggleButtonMon = findViewById(R.id.monday);
        toggleButtonTue = findViewById(R.id.tuesday);
        toggleButtonWed = findViewById(R.id.wednesday);
        toggleButtonThu = findViewById(R.id.thursday);
        toggleButtonFri = findViewById(R.id.friday);
        toggleButtonSat = findViewById(R.id.saturday);
        toggleButtonSun = findViewById(R.id.sunday);

        // 초기 시간 설정
        timePicker.setHour(6);
        timePicker.setMinute(00);

        // 글자 크기 조정
        setTimePickerTextSize(timePicker, 40); // 원하는 글자 크기 설정

        // 현재 시간 가져오기
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        // 현재 날짜 설정
        calendar = Calendar.getInstance();
        updateDateTextView(calendar);

        // 캘린더 버튼 클릭 이벤트 설정
        ImageButton calendarButton = findViewById(R.id.calenderbutton);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });


        // 시간 변경 리스너 설정
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // 시간 변경 시 현재 날짜 업데이트
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);
                updateDateTextView(selectedTime);
            }
        });


        // 유준 추가 부분. //

        // XML에서 정의한 뷰들을 참조하여 변수에 할당
        mRingtoneLayout = this.findViewById(R.id.music_layout);
        mRingtoneName = this.findViewById(R.id.music_name);
        this.findViewById(R.id.music_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRingtoneChooser();
            }
        });

        ArrayList<String> stringCategory = new ArrayList<String>();

        stringCategory.add("약통번호 1");
        stringCategory.add("약통번호 2");
        stringCategory.add("약통번호 3");
        stringCategory.add("약통번호 4");
        stringCategory.add("약통번호 5");
        stringCategory.add("약통번호 6");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, stringCategory);

        // 드롭다운 리스트에 사용될 레이아웃 설정
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Spinner에 어댑터 설정
        Spinner spinner = findViewById(R.id.gender_spinner);
        spinner.setAdapter(adapter);

        // 드롭다운 리스트의 TextView 크기 설정
        spinner.post(new Runnable() {
            @Override
            public void run() {
                View view = spinner.getSelectedView();
                if (view instanceof TextView) {
                    ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 텍스트 크기 설정
                }
            }
        });


        // 약통번호, 수량 동적 처리 부분.
        LinearLayout scroll = (LinearLayout) findViewById(R.id.alarm_relative);
        Button addButton = (Button) findViewById(R.id.pill_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                View customView = inflater.inflate(R.layout.storage_setting, null);

                pillscoreList.add(customView.findViewById(R.id.pill_score));

                // 마진 설정 (left, top, right, bottom 순서로 dp 값 변환 후 설정)
                int marginInDp = 20; // 원하는 마진 값을 dp 단위로 설정
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(0, 0, 0, marginInDp);

                // 뷰에 레이아웃 파라미터 설정
                customView.setLayoutParams(layoutParams);

                // 동적으로 생성된 Spinner에 ArrayAdapter 적용
                Spinner spinner = customView.findViewById(R.id.gender_spinner); // Spinner 찾기
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        Alarm_setting.this, android.R.layout.simple_spinner_item, stringCategory);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);


                // 드롭다운 리스트의 TextView 크기 설정
                spinner.post(new Runnable() {
                    @Override
                    public void run() {
                        View view = spinner.getSelectedView();
                        if (view instanceof TextView) {
                            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // 텍스트 크기 설정
                        }
                    }
                });

                spinnerList.add(customView.findViewById(R.id.gender_spinner));

                // 생성된 뷰를 버튼 위로 추가
                int buttonIndex = scroll.indexOfChild(addButton);
                scroll.addView(customView, buttonIndex);

                addButtonClickCount++;

                if (addButtonClickCount >= MAX_CLICK_COUNT) {
                    addButton.setVisibility(View.GONE); // 버튼을 숨김
                }
            }
        });

    }

    private void setTimePickerTextSize(TimePicker timePicker, float size) {
        try {
            Class<?> timePickerClass = Class.forName("android.widget.TimePicker");
            Field mDelegateField = timePickerClass.getDeclaredField("mDelegate");
            mDelegateField.setAccessible(true);

            Object mDelegate = mDelegateField.get(timePicker);

            if (mDelegate != null) {
                Field[] fields = mDelegate.getClass().getDeclaredFields();

                for (Field field : fields) {
                    if (field.getType() == TextView.class) {
                        field.setAccessible(true);
                        try {
                            TextView textView = (TextView) field.get(mDelegate);
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //-- 벨소리 선택 창을 띄우는 함수
    private void showRingtoneChooser() {
        // 기본 알림음 선택 창을 열기 위한 Intent 생성
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);

        // 알림음 선택 창에 표시할 제목과 기본 설정
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알림음 선택");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);

        // 이전에 선택한 벨소리가 있다면 기본값으로 설정
        if (mRingtoneUri != null && !mRingtoneUri.isEmpty()) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(mRingtoneUri));
        }

        // 벨소리 선택 창 열기
        this.startActivityForResult(intent, REQUESTCODE_RINGTONE_PICKER);
    }

    //-- 벨소리 선택 창에서 선택한 결과를 처리하는 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUESTCODE_RINGTONE_PICKER && resultCode == RESULT_OK) {
            Uri ringUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

            if (ringUri != null) {
                // 선택한 벨소리의 URI와 제목을 저장
                mRingtoneUri = ringUri.toString();
                Ringtone ringtone = RingtoneManager.getRingtone(this, ringUri);
                mRingtoneTitle = ringtone.getTitle(this);

                // 텍스트뷰에 벨소리 제목 반영
                mRingtoneName.setText(mRingtoneTitle);
            } else {
                // 벨소리를 선택하지 않았을 경우
                mRingtoneUri = null;
                mRingtoneTitle = null;
                mRingtoneName.setText("Choose ringtone");
            }
        }
    }

    // 달력 다이얼로그 표시
    private void showDatePicker() {

        toggleButtonMon.setChecked(false);
        toggleButtonTue.setChecked(false);
        toggleButtonWed.setChecked(false);
        toggleButtonThu.setChecked(false);
        toggleButtonFri.setChecked(false);
        toggleButtonSat.setChecked(false);
        toggleButtonSun.setChecked(false);
        selectedDays.clear();

        // 현재 날짜를 기본으로 설정
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // 달력 다이얼로그 생성
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        // 날짜 설정 후 텍스트뷰에 표시
                        calendar.set(selectedYear, selectedMonth, selectedDayOfMonth);
                        updateDateTextView(calendar);
                    }
                }, year, month, dayOfMonth);

        // 다이얼로그 표시
        datePickerDialog.show();
    }

    // 텍스트뷰에 날짜 표시
    private void updateDateTextView(Calendar selectedDate) {
        Calendar currentTime = Calendar.getInstance();

        if (selectedDate.after(currentTime)) {
            long differenceInDays = (selectedDate.getTimeInMillis() - currentTime.getTimeInMillis()) / (1000 * 60 * 60 * 24);

            if (differenceInDays == 0) {
                // 선택한 날짜가 오늘인 경우
                SimpleDateFormat dateFormat = new SimpleDateFormat("오늘-M월 dd일 (E)", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedDate.getTime());
                dayTextView.setText(formattedDate);
            } else if (differenceInDays == 1) {
                // 선택한 날짜가 내일인 경우
                SimpleDateFormat dateFormat = new SimpleDateFormat("내일-M월 dd일 (E)", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedDate.getTime());
                dayTextView.setText(formattedDate);
            } else {
                // 선택한 날짜가 오늘이나 내일이 아닌 경우
                SimpleDateFormat dateFormat = new SimpleDateFormat("M월 dd일 (E)", Locale.getDefault());
                String formattedDate = dateFormat.format(selectedDate.getTime());
                dayTextView.setText(formattedDate);
            }
        } else {
            // 선택한 날짜가 현재 날짜와 같거나 이전인 경우
            selectedDate.add(Calendar.DATE, 1); // 내일로 변경
            SimpleDateFormat dateFormat = new SimpleDateFormat("내일-M월 dd일 (E)", Locale.getDefault());
            String formattedDate = dateFormat.format(selectedDate.getTime());
            dayTextView.setText(formattedDate);
        }

        toggleButtonMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("월", toggleButtonMon);
            }
        });

        toggleButtonTue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("화", toggleButtonTue);
            }
        });

        toggleButtonWed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("수", toggleButtonWed);
            }
        });

        toggleButtonThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("목", toggleButtonThu);
            }
        });

        toggleButtonFri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("금", toggleButtonFri);
            }
        });

        toggleButtonSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("토", toggleButtonSat);
            }
        });

        toggleButtonSun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDay("일", toggleButtonSun);
            }
        });
    }

    // 요일 토글 기능 구현
    private void toggleDay(String day, ToggleButton toggleButton) {
        if (toggleButton.isChecked()) {
            if (!selectedDays.contains(day)) {
                selectedDays.add(day);
            }
        } else {
            selectedDays.remove(day);
        }
        updateDateTextView();
    }

    // 텍스트뷰에 선택된 요일 표시
    private void updateDateTextView() {
        TextView dayTextView = findViewById(R.id.daytextView);
        if (selectedDays.isEmpty()) {
            dayTextView.setText("오늘-" + (calendar.get(Calendar.MONTH) + 1) + "월 " +
                    calendar.get(Calendar.DAY_OF_MONTH) + "일 (" +
                    getDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK)) + ")");
        } else {
            StringBuilder selectedDaysText = new StringBuilder();
            for (String day : selectedDays) {
                selectedDaysText.append(day).append(", ");
            }
            selectedDaysText.delete(selectedDaysText.length() - 2, selectedDaysText.length());
            dayTextView.setText("매주 " + selectedDaysText.toString());
        }
    }

    // 숫자로 된 요일을 문자열로 변환
    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "일";
            case Calendar.MONDAY:
                return "월";
            case Calendar.TUESDAY:
                return "화";
            case Calendar.WEDNESDAY:
                return "수";
            case Calendar.THURSDAY:
                return "목";
            case Calendar.FRIDAY:
                return "금";
            case Calendar.SATURDAY:
                return "토";
            default:
                return "";
        }
    }

    private String getNewAlarmNumber() {
        // 여기서 알람 번호를 생성합니다. (예시로 1부터 증가하는 방식)
        // 실제 구현에서는 SharedPreferences나 DB를 사용해 고유 번호를 생성해야 합니다.
        return String.valueOf(getNextAlarmId());
    }

    private int getNextAlarmId() {
        // 예를 들어, SharedPreferences를 사용해 고유 알람 ID를 관리할 수 있습니다.
        SharedPreferences prefs = getSharedPreferences("alarms", MODE_PRIVATE);
        int nextId = prefs.getInt("nextAlarmId", 1);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("nextAlarmId", nextId + 1);
        editor.apply();
        return nextId;
    }


    private String getAlarmTime() {
        TimePicker timePicker = findViewById(R.id.timePicker);
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        return String.format("%02d:%02d", hour, minute);
    }

    private String getAlarmDate() {
        TextView dayTextView = findViewById(R.id.daytextView);
        String dayText = dayTextView.getText().toString();

        // 예시 텍스트: "오늘-6월 3일 (월)"
        String[] parts = dayText.split("-");
        String datePart = parts[1].trim();  // "6월 3일 (월)"

        // 날짜 파싱
        String[] dateParts = datePart.split(" ");
        String monthPart = dateParts[0].replace("월", "");
        String dayPart = dateParts[1].replace("일", "").replace("(", "").replace(")", "");

        int month = Integer.parseInt(monthPart);
        int day = Integer.parseInt(dayPart);

        // 현재 연도 가져오기
        int year = Calendar.getInstance().get(Calendar.YEAR);

        return String.format("%04d-%02d-%02d", year, month, day);
    }



    private class InsertAlarm extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String pillBoxNumber = params[0];
            String machineNumber = params[1];
            String alarmTime = params[2];
            String alarmDate = params[3];
            String userId = params[4];
            String hasTaken = params[5];
            String quantity = params[6];
            String url = "http://61.79.73.178:8080/PillJSP/Android/InsertAlarm.jsp?pillBoxNumber=" + pillBoxNumber + "&machineNumber="
                    + machineNumber + "&alarmTime=" + alarmTime + "&alarmDate=" + alarmDate + "&userId=" + userId + "&hasTaken="+ hasTaken + "&quantity=" + quantity;
            Log.d("InsertAlarm", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("InsertAlarm", "Response: " + response); // 응답 로그 출력
            return response;
        }

    }

}