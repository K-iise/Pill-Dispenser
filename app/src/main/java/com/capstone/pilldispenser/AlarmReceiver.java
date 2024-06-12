package com.capstone.pilldispenser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class AlarmReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;
    private static OutputStream outputStream;
    private static final String TAG = "pilldispenser";

    static String alarmTime;
    static String alarmDay;
    static String userId;
    static String deviceNumber;

    static String deviceMessage = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 로그 메시지 출력: 알람이 수신되었음을 나타냄
        Log.d("AlarmReceiver", "Alarm received!");

        alarmDay = intent.getStringExtra("alarmDay");
        alarmTime = intent.getStringExtra("alarmTime");
        userId = intent.getStringExtra("userId");
        deviceNumber = intent.getStringExtra("deviceNumber");

        MyApplication app = (MyApplication) context.getApplicationContext(); // 여기서 getApplicationContext() 사용
        outputStream = app.getOutputStream();

        // 기본 알림 소리의 URI 가져오기
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            // 기본 알림 소리가 없을 경우 소리를 설정하지 않음
            Log.e("AlarmReceiver", "Failed to get default notification sound URI");
            return;
        }

        // 알람 소리를 재생할 Ringtone 객체 생성
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone == null) {
            Log.e("AlarmReceiver", "Failed to get Ringtone object for default notification sound URI");
            return;
        }

        // Ringtone 재생
        ringtone.play();
        Log.d("AlarmReceiver", "Notification sound played");
        new getPillQuantity().execute(deviceNumber, alarmTime, alarmDay, userId);
        // Alarm_Ringing 액티비티 시작
        Intent alarmRingingIntent = new Intent(context, Alarm_Ringing.class);
        alarmRingingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 새로운 태스크에서 액티비티 시작
        alarmRingingIntent.putExtra("time", alarmTime);
        context.startActivity(alarmRingingIntent);
    }

    // 알람 중지 메서드
    public static void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
            Log.d("AlarmReceiver", "Notification sound stopped");
            Log.d("AlarmReceiverString", "deviceMessage = " + deviceMessage);
            new UpdateAlarm().execute(deviceNumber, alarmTime, alarmDay, userId);
            //sendData(deviceMessage);
        }
    }

    private static void sendData(String message) {

        if (outputStream != null) {
            try {
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error sending data", e);
            }
        }
    }

    // 약통번호와 복용수량을 DB에서 가져오는 메소드.
    private class getPillQuantity extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String deviceNumber = params[0];
            String alarmTime = params[1];
            String alarmDay = params[2];
            String userId = params[3];
            String url = "http://61.79.73.178:8080/PillJSP/Android/getPillQuantity.jsp?deviceNumber=" + deviceNumber + "&alarmTime="
                    + alarmTime + "&alarmDay=" + alarmDay + "&userId=" + userId;
            Log.d("getPillQuantity", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("getPillQuantity", "Response: " + response); // 응답 로그 출력
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                StringBuilder deviceMessageBuilder = new StringBuilder();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String pillBoxNumber = jsonObject.getString("pillBoxNumber");
                    int quantity = jsonObject.getInt("Quantity");

                    for (int k = 0; k < quantity; k++) {
                        deviceMessageBuilder.append(pillBoxNumber);
                    }
                }

                deviceMessage = deviceMessageBuilder.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private static class UpdateAlarm extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String deviceNumber = params[0];
            String alarmTime = params[1];
            String alarmDay = params[2];
            String userId = params[3];
            String url = "http://61.79.73.178:8080/PillJSP/Android/UpdateAlarm.jsp?deviceNumber=" + deviceNumber + "&alarmTime="
                    + alarmTime + "&alarmDay=" + alarmDay + "&userId=" + userId;
            Log.d("UpdateAlarm", "URL: " + url); // URL 로그 출력
            HttpHandler httpHandler = new HttpHandler();
            String response = httpHandler.makeServiceCall(url);
            Log.d("UpdateAlarm", "Response: " + response); // 응답 로그 출력
            return response;
        }


    }
}
