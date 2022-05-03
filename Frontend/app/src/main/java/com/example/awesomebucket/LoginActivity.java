package com.example.awesomebucket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class LoginActivity extends Activity {

    Button loginBtn;

    long pressedTime = 0; //'뒤로가기' 버튼 클릭했을 때의 시간

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }


    //**************************** 뒤로가기 버튼을 눌렀을 때의 이벤트 처리 ********************************
    @Override
    public void onBackPressed() {
        //마지막으로 누른 '뒤로가기' 버튼 클릭 시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 크면
        if(System.currentTimeMillis() > pressedTime + 2000){
            //현재 시간을 pressedTime 에 저장
            pressedTime = System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        }

        //마지막 '뒤로가기' 버튼 클릭시간이 이전의 '뒤로가기' 버튼 클릭 시간과의 차이가 2초보다 작으면
        else{
            moveTaskToBack(true);  // 태스크를 백그라운드로 이동
            finishAndRemoveTask();  // 액티비티 종료 + 태스크 리스트에서 지우기
            android.os.Process.killProcess(android.os.Process.myPid());  // 앱 프로세스 종료
        }
    }
}
