package com.example.simplechatter.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.MainActivity;
import com.example.simplechatter.R;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.database.Repository.UserRepository;
import com.example.simplechatter.util.InputValidator;
import com.example.simplechatter.util.NavigationHelper;

public class RegisterActivity extends AppCompatActivity {
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);
        setupRegisterButton();
        setupReturnButton();
    }

    private void setupRegisterButton() {
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String email = ((EditText) findViewById(R.id.etEmailRegister)).getText().toString().trim();
            String password = ((EditText) findViewById(R.id.etPasswordRegister)).getText().toString().trim();
            String confirmPassword = ((EditText) findViewById(R.id.etPasswordRegister2)).getText().toString().trim();
            String nickname = ((EditText) findViewById(R.id.etNameRegister)).getText().toString().trim();

            if (!validateInput(email, password, confirmPassword)) {
                return;
            }

            checkEmailAndRegister(email, password, nickname);
        });
    }

    private void setupReturnButton() {
        findViewById(R.id.btnReturn).setOnClickListener(v -> {
            NavigationHelper.navigateToLogin(this);
            finish();
        });
    }

    private void checkEmailAndRegister(String email, String password, String nickname) {
        userRepository.checkEmailExists(email, exists -> {
            if (exists) {
                Toast.makeText(this, "该邮箱已被注册", Toast.LENGTH_SHORT).show();
            } else {
                performRegistration(email, password, nickname);
            }
        });
    }

    private void performRegistration(String email, String password, String nickname) {
        User user = new User(email, password);
        user.setNickname(nickname);
        userRepository.register(user, (success, userId) -> {
            if (success) {
                NavigationHelper.navigateToLogin(this);
                finish();
            } else {
                Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isValidPassword(password)) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!InputValidator.isPasswordMatch(password, confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}