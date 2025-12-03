package com.example.simplechatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupNavigation();
        setupCardClickListeners();
    }

    private void setupNavigation() {
        Button btnHome = findViewById(R.id.btnHome);
        Button btnMessage = findViewById(R.id.btnMessage);
        Button btnProfile = findViewById(R.id.btnProfile);

        btnHome.setOnClickListener(v -> {
            // 已经在首页，不需要跳转
        });

        btnMessage.setOnClickListener(v -> {
            // 跳转到聊天列表
            startActivity(new Intent(this, ContactsActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        btnProfile.setOnClickListener(v -> {
            // 跳转到个人资料
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }


    private void setupCardClickListeners() {
        // 今日推荐按钮
        findViewById(R.id.btnViewNow).setOnClickListener(v -> {
            Toast.makeText(this, "今日推荐功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 资讯卡片
        findViewById(R.id.cardNews).setOnClickListener(v -> {
            Toast.makeText(this, "资讯功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 知识卡片
        findViewById(R.id.cardKnowledge).setOnClickListener(v -> {
            Toast.makeText(this, "知识功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 趋势卡片
        findViewById(R.id.cardTrends).setOnClickListener(v -> {
            Toast.makeText(this, "趋势功能开发中", Toast.LENGTH_SHORT).show();
        });

        // 社区卡片
        findViewById(R.id.cardCommunity).setOnClickListener(v -> {
            Toast.makeText(this, "社区功能开发中", Toast.LENGTH_SHORT).show();
        });
    }
}