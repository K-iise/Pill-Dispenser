package com.capstone.pilldispenser;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.device_register);

/*        Toolbar toolbar = findViewById (R.id.toolbar);
        setSupportActionBar (toolbar); // 액티비티의 앱바(App Bar)로 지정
        ActionBar actionBar = getSupportActionBar (); // 앱바 제어를 위해 툴바 액세스

        //actionBar.setHomeAsUpIndicator(R.drawable.hamberger); // 햄버거 버튼 이미지 불러오기.
        //actionBar.setDisplayHomeAsUpEnabled(true);            // *
        //actionBar.setDisplayHomeAsUpEnabled (true); // 앱바에 뒤로가기 버튼 만들기
        //actionBar.setIcon(R.drawable.group_1);         // 앱바에 아이콘을 표시하기
        actionBar.setDisplayUseLogoEnabled(true) ;  // *
        actionBar.setDisplayShowHomeEnabled(true) ; // **/
    }

/*    // 메뉴 리소스 XML의 내용을 앱바(App Bar)에 반영
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;

    }*/

/*    // 햄버거 버튼을 클릭했을 때 action
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case android.R.id.home: // 툴바의 햄버거를 클릭하면 drawer 열리게
                main_layout.openDrawer(drawerView);
                break;
        }

        return super.onOptionsItemSelected(item);
    }*/
}