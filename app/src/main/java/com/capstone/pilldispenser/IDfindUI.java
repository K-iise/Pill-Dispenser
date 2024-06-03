package com.capstone.pilldispenser;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IDfindUI extends AppCompatActivity {

    private EditText nameEditText, rrnEditText, phoneEditText;
    private Button findIdButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idfind_ui);

        nameEditText = findViewById(R.id.editTextId_Reg);
        rrnEditText = findViewById(R.id.editTextId);
        phoneEditText = findViewById(R.id.editTextNick_Reg);
        findIdButton = findViewById(R.id.btn_newMatching_submit);

        findIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String rrn = rrnEditText.getText().toString();
                String phone = phoneEditText.getText().toString();
                findId(name, rrn, phone);
            }
        });

    }

    private void findId(final String name, final String rrn, final String phone) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlString = "http://192.168.0.110:8080/PillJSP/Android/androidDB.jsp?name=" + name + "&rrn=" + rrn + "&phone=" + phone;
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    final StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    conn.disconnect();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String response = result.toString().trim();
                            if (response.isEmpty()) {
                                Toast.makeText(IDfindUI.this, "일치하는 정보가 없습니다", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(IDfindUI.this, "아이디: " + response, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}