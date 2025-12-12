package com.example.simplechatter.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.simplechatter.R;
import com.example.simplechatter.database.Repository.UserRepository;
import com.example.simplechatter.util.EmailCodeManager;
import com.example.simplechatter.util.InputValidator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;

public class ForgetPasswordActivity extends AppCompatActivity {
    private TextInputEditText etEmail;
    private TextInputEditText etVerificationCode;
    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnSendCode;
    private Button btnResetPassword;
    private UserRepository userRepository;
    private EmailCodeManager emailCodeManager;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initViews();
        setupListeners();
        userRepository = new UserRepository(this);
        emailCodeManager = EmailCodeManager.getInstance();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    private void setupListeners() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();

        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            //输入框立即获得焦点
            etEmail.requestFocus();
            return;
        }

        userRepository.checkEmailExists(email, exists -> {
            if (!exists) {
                Toast.makeText(this, "该邮箱未注册，请检查邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendCode.setEnabled(false);
            btnSendCode.setText("发送中...");

            Executors.newSingleThreadExecutor().execute(() -> {
                boolean success = emailCodeManager.sendVerificationCode(email);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "验证码已发送到您的邮箱，请查收", Toast.LENGTH_LONG).show();
                        startCountDown();
                    } else {
                        btnSendCode.setEnabled(true);
                        btnSendCode.setText("发送验证码");
                        long remainingTime = emailCodeManager.getRemainingSendTime(email);
                        if (remainingTime > 0) {
                            Toast.makeText(this, "发送过于频繁，请" + remainingTime + "秒后再试", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "发送失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        });
    }
    //60 秒防重复点击
    private void startCountDown() {
        btnSendCode.setEnabled(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            //实时更新按钮文字
            public void onTick(long millisUntilFinished) {
                btnSendCode.setText((millisUntilFinished / 1000) + "秒后重发");
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("发送验证码");
            }
        };
        countDownTimer.start();
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(email, code, newPassword, confirmPassword)) {
            return;
        }

        if (!emailCodeManager.verifyCode(email, code)) {
            Toast.makeText(this, "验证码错误或已过期，请重新获取", Toast.LENGTH_SHORT).show();
            return;
        }

        resetPasswordForEmail(email, newPassword);
    }

    private boolean validateInput(String email, String code, String newPassword, String confirmPassword) {
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (!InputValidator.isValidVerificationCode(code)) {
            Toast.makeText(this, "验证码为6位数字", Toast.LENGTH_SHORT).show();
            etVerificationCode.requestFocus();
            return false;
        }

        if (!InputValidator.isValidPassword(newPassword)) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return false;
        }

        if (!InputValidator.isPasswordMatch(newPassword, confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void resetPasswordForEmail(String email, String newPassword) {
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("重置中...");

        userRepository.updatePasswordByEmail(email, newPassword, success -> {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("重置密码");

            if (success) {
                Toast.makeText(this, "密码重置成功，请使用新密码登录", Toast.LENGTH_LONG).show();
                etEmail.postDelayed(this::finish, 1500);
            } else {
                Toast.makeText(this, "密码重置失败，请重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}