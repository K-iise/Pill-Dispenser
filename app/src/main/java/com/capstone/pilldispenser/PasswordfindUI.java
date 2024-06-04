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

public class PasswordfindUI extends AppCompatActivity {

    private EditText nameEditText, idEditText, ssnEditText, phoneEditText;
    private Button findPasswordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passwordfind_ui);

        nameEditText = findViewById(R.id.editTextId_Reg);
        idEditText = findViewById(R.id.editTextId4);
        ssnEditText = findViewById(R.id.editTextId);
        phoneEditText = findViewById(R.id.editTextNick_Reg);
        findPasswordButton = findViewById(R.id.btn_newMatching_submit);

        findPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPassword();
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

    private void findPassword() {
        String name = nameEditText.getText().toString();
        String id = idEditText.getText().toString();
        String ssn = ssnEditText.getText().toString();
        String phone = phoneEditText.getText().toString();

        String url = "http://192.168.0.2:8080/PillJSP/Android/PWfindDB.jsp";

        new FindPasswordTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, name, id, ssn, phone);
    }

    private class FindPasswordTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                String name = params[1];
                String id = params[2];
                String ssn = params[3];
                String phone = params[4];

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                String postData = "name=" + URLEncoder.encode(name, "UTF-8") +
                        "&id=" + URLEncoder.encode(id, "UTF-8") +
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
                Log.e("FindPassword", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(PasswordfindUI.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}
