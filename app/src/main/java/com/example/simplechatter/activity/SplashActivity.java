package com.example.simplechatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.MainActivity;
import com.example.simplechatter.R;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // 启动主界面
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // 关闭启动页
            finish();
        }, 2000); // 2秒延迟
    }
}
