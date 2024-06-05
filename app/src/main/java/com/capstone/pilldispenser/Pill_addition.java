package com.capstone.pilldispenser;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class Pill_addition extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_addition);

        ArrayList<String> stringCategory = new ArrayList<String>();

        stringCategory.add("알약이름   ");
        stringCategory.add("제조업체   ");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stringCategory);

        ((Spinner) findViewById(R.id.drop_down)).setAdapter(adapter);

        ArrayList<String> stringCategory2 = new ArrayList<String>();
        stringCategory2.add("약통번호1");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, stringCategory2);


        ((Spinner) findViewById(R.id.storage_number)).setAdapter(adapter2);
    }
}