package com.example.simplechatter;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.activity.ContactsActivity;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.database.Repository.UserRepository;
import com.example.simplechatter.util.InputValidator;
import com.example.simplechatter.util.NavigationHelper;
import com.example.simplechatter.util.UserSessionManager;

public class MainActivity extends AppCompatActivity {
    private UserRepository userRepository;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userRepository = new UserRepository(this);
        sessionManager = new UserSessionManager(this);

        setupViews();
        checkAutoLogin();
    }

    private void setupViews() {
        // 注册按钮
        findViewById(R.id.tvRegister).setOnClickListener(v ->
                NavigationHelper.navigateToRegister(this));

        // 忘记密码
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    NavigationHelper.navigateToForgetPassword(this));
        }

        // 登录按钮
        findViewById(R.id.btnLogin).setOnClickListener(v -> handleLogin());
    }

    private void checkAutoLogin() {
        if (sessionManager.isLoggedIn()) {
            NavigationHelper.navigateToContacts(this);
            finish();
        }
    }

    private void handleLogin() {
        String email = ((EditText) findViewById(R.id.etEmail)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString().trim();

        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!InputValidator.isValidPassword(password)) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.login(email, password, (success, user) -> {
            if (success && user != null) {
                sessionManager.saveUserLogin(user.getId(), user.getEmail(), user.getNickname());
                NavigationHelper.navigateToContacts(this);
                finish();
            } else {
                Toast.makeText(this, "邮箱或密码错误", Toast.LENGTH_SHORT).show();
            }
        });
    }
}