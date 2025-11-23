package com.example.simplechatter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.MainActivity;
import com.example.simplechatter.R;

public class PersonalView extends AppCompatActivity {
    private static final String TAG = "PersonalView";
    private static final String PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // 安全初始化所有组件
        safeInitialize();
    }

    /**
     * 安全初始化所有组件
     */
    private void safeInitialize() {
        try {
            // 1. 设置退出登录按钮（根据您的图片，布局中有btnQuit）
            setupLogoutButton();

            // 2. 设置底部导航按钮
            setupNavigationButtons();

            Log.d(TAG, "所有组件初始化完成");

        } catch (Exception e) {
            Log.e(TAG, "初始化失败: " + e.getMessage());
            Toast.makeText(this, "页面初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 设置退出登录按钮 - 使用布局中存在的btnQuit
     */
    private void setupLogoutButton() {
        Button btnLogout = findViewById(R.id.btnQuit);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "退出登录按钮点击");
                    showLogoutConfirmationDialog();
                }
            });
            Log.d(TAG, "退出登录按钮设置成功");
        } else {
            Log.e(TAG, "退出登录按钮未找到，ID: btnQuit");
        }
    }

    /**
     * 设置底部导航按钮
     */
    private void setupNavigationButtons() {
        // 首页按钮
        setupButton(R.id.btnHome, "首页", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMainActivity();
            }
        });

        // 信息按钮
        setupButton(R.id.btnMessage, "信息", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToContactsActivity();
            }
        });
    }

    /**
     * 安全设置按钮点击事件
     */
    private void setupButton(int buttonId, String buttonName, View.OnClickListener listener) {
        View button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(listener);
            Log.d(TAG, buttonName + "按钮设置成功，ID: " + getResources().getResourceEntryName(buttonId));
        } else {
            Log.w(TAG, buttonName + "按钮未找到，ID: " + getResources().getResourceEntryName(buttonId));
        }
    }

    /**
     * 显示退出登录确认对话框
     */
    private void showLogoutConfirmationDialog() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("退出登录")
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
        } catch (Exception e) {
            Log.e(TAG, "显示对话框失败: " + e.getMessage());
            // 直接执行退出
            performLogout();
        }
    }

    /**
     * 执行退出登录操作
     */
    private void performLogout() {
        try {
            Log.d(TAG, "开始执行退出登录");

            // 清除用户登录状态
            clearUserLoginStatus();

            Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();

            // 跳转到登录页面
            navigateToLoginPage();

        } catch (Exception e) {
            Log.e(TAG, "退出登录失败: " + e.getMessage());
            Toast.makeText(this, "退出登录失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 清除用户登录状态
     */
    private void clearUserLoginStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();
        Log.d(TAG, "用户登录状态已清除");
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
        Intent intent = new Intent(this, MainActivity.class);
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
     * 显示返回确认对话框
     */
    private void showBackConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定要返回吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PersonalView.super.onBackPressed();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "个人中心页面销毁");
    }
}