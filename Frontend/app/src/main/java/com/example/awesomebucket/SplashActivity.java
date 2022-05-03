package com.example.awesomebucket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 명시적 인텐트를 사용하여 LoginActivity 호출
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);

        finish();
    }
}