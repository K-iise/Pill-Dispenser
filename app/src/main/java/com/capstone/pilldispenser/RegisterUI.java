package com.capstone.pilldispenser;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterUI extends AppCompatActivity {

    private EditText userIdEditText, passwordEditText, phoneNumberEditText, emailEditText, nameEditText, residentNumberEditText;
    private Button registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ui);

        userIdEditText = findViewById(R.id.editTextId_Reg);
        passwordEditText = findViewById(R.id.editTextPass_Reg);
        phoneNumberEditText = findViewById(R.id.editTextPhone_Reg);
        emailEditText = findViewById(R.id.editTextRePass_Reg);
        nameEditText = findViewById(R.id.editTextNick_Reg);
        residentNumberEditText = findViewById(R.id.editText);
        registerButton = findViewById(R.id.btn_newMatching_submit);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
        Button backButton = findViewById(R.id.btnRegister); // 뒤로가기 버튼의 id를 사용해 버튼 객체를 가져옴
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 현재 액티비티 종료
            }
        });
    }

    private void registerUser() {
        String userId = userIdEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String name = nameEditText.getText().toString();
        String residentNumber = residentNumberEditText.getText().toString();

        String url = "http://192.168.0.110:8080/PillJSP/Android/registerDB.jsp";

        new RegisterUserTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, userId, password, phoneNumber, email, name, residentNumber);

    }

    private class RegisterUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                String url = params[0];
                String userId = params[1];
                String password = params[2];
                String phoneNumber = params[3];
                String email = params[4];
                String name = params[5];
                String residentNumber = params[6];

                // HTTP 연결 설정
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // 데이터 전송
                OutputStream outputStream = connection.getOutputStream();
                String postData = "userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8") +
                        "&phoneNumber=" + URLEncoder.encode(phoneNumber, "UTF-8") +
                        "&email=" + URLEncoder.encode(email, "UTF-8") +
                        "&name=" + URLEncoder.encode(name, "UTF-8") +
                        "&residentNumber=" + URLEncoder.encode(residentNumber, "UTF-8");
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // 응답 코드 확인
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // 서버에서 받은 응답 처리
                    // 여기서는 일단 성공 메시지만 반환
                    return "회원가입 성공!";
                } else {
                    return "서버 오류 발생";
                }
            } catch (Exception e) {
                Log.e("RegisterUI", "Error occurred: " + e.getMessage(), e);
                return "오류 발생: " + e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(RegisterUI.this, result, Toast.LENGTH_SHORT).show();
        }
    }
}