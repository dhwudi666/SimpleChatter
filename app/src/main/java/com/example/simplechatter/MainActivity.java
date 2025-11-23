package com.example.simplechatter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.activity.ContactsActivity;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.database.Repository.UserRepository;
import com.example.simplechatter.ui.MessageList;
import com.example.simplechatter.activity.RegisterActivity;
import com.example.simplechatter.util.ContactDataInitializer;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 初始化视图
        initViews();
        // 初始化数据库连接
        initializeDatabase();
        //注册事件
        registerButton();
        //登录事件
        setupLoginButton();
        // 检查是否已登录（自动登录）
        checkAutoLogin();
        //忘记密码
//        setupForgotPassword();
    }
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }
    private void initializeDatabase(){
        // 初始化联系人数据
        ContactDataInitializer initializer = new ContactDataInitializer(this);
        initializer.initializeSampleData(1); // 为用户1初始化数据
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
                            Toast.makeText(MainActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
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
        // 使用 SharedPreferences 保存用户登录状态
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("user_id", user.getId())
                .putString("user_email", user.getEmail())
                .putBoolean("is_logged_in", true)
                .apply();
    }

//    private boolean validateInput(String email, String password) {
//        // 验证邮箱是否为空
//        if (email.isEmpty()) {
//            etEmail.setError("请输入邮箱地址");
//            etEmail.requestFocus();
//            return false;
//        }
//        // 验证邮箱格式
//        if (!isValidEmail(email)) {
//            etEmail.setError("请输入有效的邮箱地址");
//            etEmail.requestFocus();
//            return false;
//        }
//        // 验证密码是否为空
//        if (password.isEmpty()) {
//            etPassword.setError("请输入密码");
//            etPassword.requestFocus();
//            return false;
//        }
//        // 验证密码长度
//        if (password.length() < 6) {
//            etPassword.setError("密码长度至少6位");
//            etPassword.requestFocus();
//            return false;
//        }
//        return true;
//    }
//    private boolean isValidEmail(String email) {
//        // 简单的邮箱格式验证
//        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
//    }
}