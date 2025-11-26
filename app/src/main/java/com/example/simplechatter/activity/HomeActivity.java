package com.example.simplechatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.Adapter.ContactsAdapter;
import com.example.simplechatter.R;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setupNavigation();
//        initViews();

    }
//    private void initViews() {
//        recyclerView = findViewById(R.id.rvContacts);
//
//        // 设置RecyclerView
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new ContactsAdapter(contactList, this);
//        recyclerView.setAdapter(adapter);
//    }
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

}