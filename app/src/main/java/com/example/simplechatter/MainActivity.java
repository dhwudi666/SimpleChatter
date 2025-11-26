package com.example.simplechatter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.activity.ContactsActivity;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.database.Repository.UserRepository;
import com.example.simplechatter.activity.RegisterActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //注册事件
        registerButton();
        //登录事件
        setupLoginButton();
        // 检查是否已登录（自动登录）
        checkAutoLogin();
        //忘记密码
//        setupForgotPassword();
    }
    private void registerButton(){
        findViewById(R.id.tvRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // 自动跳转到联系人列表
            navigateToContactsActivity();
        }
    }

    private void navigateToContactsActivity() {
        Intent intent = new Intent(MainActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish(); // 关闭登录页面
    }
    private void setupLoginButton() {
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();
            UserRepository userRepository = new UserRepository(this);
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写邮箱和密码", Toast.LENGTH_SHORT).show();
                return;
            }
            userRepository.login(email, password, new UserRepository.OnLoginListener() {
                @Override
                public void onLoginResult(boolean success, User user) {
                    runOnUiThread(() -> {
                        if (success) {
                            // 保存用户登录状态
                            saveUserLogin(user);
                            // 跳转到主页面
                            startActivity(new Intent(MainActivity.this, ContactsActivity.class));
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "邮箱或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
    }
    private void saveUserLogin(User user) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("user_id", user.getId());
        editor.putString("user_email", user.getEmail());
        editor.putString("user_nickname", user.getNickname());
        editor.putBoolean("is_logged_in", true);
        editor.putLong("last_login_time", System.currentTimeMillis());

        boolean success = editor.commit(); //提交保存
    }
}