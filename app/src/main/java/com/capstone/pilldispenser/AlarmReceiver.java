package com.capstone.pilldispenser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        // 로그 메시지 출력: 알람이 수신되었음을 나타냄
        Log.d("AlarmReceiver", "Alarm received!");

        String alarmTime = intent.getStringExtra("time");

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
        }
    }
}
