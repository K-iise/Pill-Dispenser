package com.capstone.pilldispenser;

import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginUI extends AppCompatActivity {

    // 레이아웃에서 아이디와 비밀번호를 입력받는 EditText
    private EditText editTextUserId;
    private EditText editTextPassword;

    private String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ui);

        // EditText 초기화
        editTextUserId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);

        // 로그인 버튼 클릭 시 이벤트 처리
        Button buttonLogin = findViewById(R.id.btn_newMatching_submit);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 아이디와 비밀번호 입력값 가져오기
                userId = editTextUserId.getText().toString();
                String password = editTextPassword.getText().toString();

                // LoginUI AsyncTask 실행
                LoginTask loginTask = new LoginTask();
                loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, userId, password);
            }
        });
        Button buttonRegister = findViewById(R.id.btnRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // RegisterUI 액티비티 시작
                Intent intent = new Intent(LoginUI.this, RegisterUI.class);
                startActivity(intent);
            }
        });
        Button buttonFindId = findViewById(R.id.button1);
        buttonFindId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // IDfindUI 액티비티 시작
                Intent intent = new Intent(LoginUI.this, IDfindUI.class);
                startActivity(intent);
            }
        });
    }

    // LoginUI AsyncTask 정의
    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            // 네트워크 작업 등 로그인 처리 코드 작성
            // doInBackground()에서는 메인(UI) 쓰레드와 별도로 실행됩니다.

            // 아이디와 비밀번호 가져오기
            String userId = params[0];
            String password = params[1];
            String result = "";

            try {
                // 서버 URL 설정
                URL url = new URL("http://61.79.73.178:8080/PillJSP/Android/androidDB.jsp"); // JSP 파일의 URL로 수정해주세요.

                // HTTP 연결 설정
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // 파라미터 설정
                String postData = "userId=" + URLEncoder.encode(userId, "UTF-8") +
                        "&password=" + URLEncoder.encode(password, "UTF-8");

                // 데이터 전송
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(postData.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                // 응답 받기
                InputStream inputStream;
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = conn.getInputStream();
                } else {
                    inputStream = conn.getErrorStream();
                }

                // 응답 내용 읽기
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                result = stringBuilder.toString();

                // 자원 해제
                bufferedReader.close();
                inputStream.close();
                conn.disconnect();

            } catch (IOException e) {
                Log.e("LoginUI", "IOException occurred: " + e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // doInBackground()의 결과를 처리하는 코드 작성
            // onPostExecute()에서는 메인(UI) 쓰레드에서 실행됩니다.

            if (result.contains("로그인 성공")) {
                // 로그인 성공 시 처리
                Toast.makeText(LoginUI.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginUI.this, Device_select.class);
                intent.putExtra("userId", userId);
                startActivity(intent);

            } else {
                // 로그인 실패 시 처리
                Toast.makeText(LoginUI.this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
                // 여기에 실패했을 때의 동작을 추가하세요.
            }
        }

    }
}
