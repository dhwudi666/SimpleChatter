package com.example.simplechatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.MainActivity;
import com.example.simplechatter.R;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.database.Repository.UserRepository;

public class RegisterActivity extends AppCompatActivity {
    private UserRepository userRepository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);
        setupRegisterButton();
        ReturnHome();
    }

    private void setupRegisterButton() {
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.etEmailRegister)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etPasswordRegister)).getText().toString().trim();
            String confirmPassword = ((EditText) findViewById(R.id.etPasswordRegister2)).getText().toString().trim();
            String nickname = ((EditText) findViewById(R.id.etNameRegister)).getText().toString().trim();
            // 输入验证
            if (!validateInput(email, password, confirmPassword)) {
                return;
            }
            // 先检查邮箱是否已存在
            checkEmailAndRegister(email, password, nickname);
        });
    }

    /**
     * 检查邮箱是否存在，然后注册
     */
    private void checkEmailAndRegister(String email, String password, String nickname) {
        userRepository.checkEmailExists(email, new UserRepository.OnEmailCheckListener() {
            @Override
            public void onEmailCheckResult(boolean exists) {
                runOnUiThread(() -> {
                    if (exists) {
                        // 邮箱已存在
                        Toast.makeText(RegisterActivity.this, "该邮箱已被注册", Toast.LENGTH_SHORT).show();
                    } else {
                        // 邮箱不存在，执行注册
                        performRegistration(email, password, nickname);
                    }
                });
            }
        });
    }

    /**
     * 执行用户注册
     */
    private void performRegistration(String email, String password, String nickname) {
        // 创建用户对象
        User user = new User(email, password);
        user.setNickname(nickname);
        userRepository.register(user, new UserRepository.OnRegisterListener() {
            @Override
            public void onRegisterResult(boolean success, long userId) {
                runOnUiThread(() -> {
                    if (success) {
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        // 邮箱格式验证
        if (email.isEmpty()) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 密码长度验证
        if (password.length() < 6) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 密码确认验证
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void ReturnHome() {
        findViewById(R.id.btnReturn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}