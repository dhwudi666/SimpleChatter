package com.example.simplechatter.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.MainActivity;
import com.example.simplechatter.R;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.util.NavigationHelper;
import com.example.simplechatter.util.UserSessionManager;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private UserDao userDao;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        sessionManager = new UserSessionManager(this);
        userDao = AppDataBase.getInstance(this).userDao();

        loadUserInfo();
        setupButtons();
    }

    private void loadUserInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int userId = sessionManager.getCurrentUserId();
                User user = userDao.getUserById(userId);
                if (user != null) {
                    runOnUiThread(() -> updateUserInfo(user));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateUserInfo(User user) {
        TextView tvUsername = findViewById(R.id.tvUsername);
        if (tvUsername != null) {
            String displayName = getDisplayName(user);
            tvUsername.setText(displayName);
        }
    }

    private String getDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname();
        } else {
            String email = user.getEmail();
            if (email.contains("@")) {
                return email.substring(0, email.indexOf("@"));
            }
            return email;
        }
    }

    private void setupButtons() {
        Button btnQuit = findViewById(R.id.btnQuit);
        if (btnQuit != null) {
            btnQuit.setOnClickListener(v -> showLogoutConfirmationDialog());
        }

        findViewById(R.id.btnHome).setOnClickListener(v -> NavigationHelper.navigateToHome(this));
        findViewById(R.id.btnMessage).setOnClickListener(v -> NavigationHelper.navigateToContacts(this));
        findViewById(R.id.btnProfile).setOnClickListener(v -> refreshUserInfo());
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        sessionManager.clearLogin();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
        NavigationHelper.navigateToLogin(this);
        finish();
    }

    private void refreshUserInfo() {
        loadUserInfo();
        Toast.makeText(this, "用户信息已刷新", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }
}