package com.capstone.pilldispenser;

import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Field;

public class Alarm_setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);

        TimePicker timePicker = findViewById(R.id.timePicker);

        // 초기 시간 설정
        timePicker.setHour(12);
        timePicker.setMinute(30);

        // 글자 크기 조정
        setTimePickerTextSize(timePicker, 40); // 원하는 글자 크기 설정

        // 시간 변경 리스너 설정
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // 시간 변경 시 수행할 작업
                System.out.println("Selected Time: " + hourOfDay + ":" + minute);
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
}