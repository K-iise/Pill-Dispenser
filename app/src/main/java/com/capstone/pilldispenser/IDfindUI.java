package com.capstone.pilldispenser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class IDfindUI extends AppCompatActivity {

    private EditText nameEditText, ssnEditText, phoneEditText;
    private Button findIdButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idfind_ui);

        nameEditText = findViewById(R.id.editTextId_Reg);
        ssnEditText = findViewById(R.id.editTextId);
        phoneEditText = findViewById(R.id.editTextNick_Reg);
        findIdButton = findViewById(R.id.btn_newMatching_submit);

        findIdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findId();
            }
        });

        Button backButton = findViewById(R.id.btnRegister);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findId() {
        String name = nameEditText.getText().toString();
        String ssn = ssnEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        String url = "http://192.168.0.2:8080/PillJSP/Android/IDfindDB.jsp";

        new FindIdTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, name, ssn, phone);
    }

    private class FindIdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                String name = params[1];
                String ssn = params[2];
                String phone = params[3];

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                String postData = "name=" + URLEncoder.encode(name, "UTF-8") +
                        "&ssn=" + URLEncoder.encode(ssn, "UTF-8") +
                        "&phone=" + URLEncoder.encode(phone, "UTF-8");
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else {
                    return "서버 오류 발생";
                }
            } catch (Exception e) {
                Log.e("FindId", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(IDfindUI.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
