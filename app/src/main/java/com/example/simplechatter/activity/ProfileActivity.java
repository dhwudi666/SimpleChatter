package com.example.simplechatter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {
    private UserDao userDao;
    private ExecutorService executor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        // 加载用户信息
        loadUserInfo();
        // 设置按钮功能
        setupButtons();
    }
    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 获取当前用户ID
                int userId = getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .getInt("user_id", 1);

                // 从数据库获取用户信息
                UserDao userDao = AppDataBase.getInstance(this).userDao();
                User user = userDao.getUserById(userId);

                if (user != null) {
                    runOnUiThread(() -> {
                        updateUserInfo(user);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    /**
     * 更新用户信息到界面
     */
    private void updateUserInfo(User user) {
        try {
            // 设置用户名
            TextView tvUsername = findViewById(R.id.tvUsername);
            if (tvUsername != null) {
                String displayName = getDisplayName(user);
                tvUsername.setText(displayName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取显示名称
     */
    private String getDisplayName(User user) {
        if (user.getNickname() != null && !user.getNickname().trim().isEmpty()) {
            return user.getNickname();
        } else {
            // 从邮箱中提取用户名
            String email = user.getEmail();
            if (email.contains("@")) {
                return email.substring(0, email.indexOf("@"));
            }
            return email;
        }
    }

    /**
     * 设置按钮功能
     */
    private void setupButtons() {
        // 退出登录按钮 - 保留弹窗功能
        Button btnQuit = findViewById(R.id.btnQuit);
        if (btnQuit != null) {
            btnQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showLogoutConfirmationDialog();
                }
            });
        }

        // 首页按钮
        Button btnHome = findViewById(R.id.btnHome);
        if (btnHome != null) {
            btnHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToMainActivity();
                }
            });
        }

        // 消息按钮
        Button btnMessage = findViewById(R.id.btnMessage);
        if (btnMessage != null) {
            btnMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToContactsActivity();
                }
            });
        }

        // 个人中心按钮（刷新）
        Button btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshUserInfo();
                }
            });
        }
    }

    /**
     * 显示退出登录确认对话框 - 保留弹窗功能
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
    }
    /**
     * 执行退出登录
     */
    private void performLogout() {
        try {
            // 清除登录状态
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
            // 跳转到登录页面
            navigateToLoginPage();

        } catch (Exception e) {
            Toast.makeText(this, "退出登录失败", Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * 跳转到登录页面
     */
    private void navigateToLoginPage() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    /**
     * 跳转到主页面
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * 跳转到联系人页面
     */
    private void navigateToContactsActivity() {
        Intent intent = new Intent(this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
    /**
     * 刷新用户信息
     */
    private void refreshUserInfo() {
        loadUserInfo();
        Toast.makeText(this, "用户信息已刷新", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 页面恢复时刷新用户信息
        loadUserInfo();
    }
}