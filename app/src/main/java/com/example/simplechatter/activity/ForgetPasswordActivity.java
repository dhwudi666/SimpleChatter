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
import com.google.android.material.textfield.TextInputEditText;

import java.util.concurrent.Executors;
import java.util.regex.Pattern;

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

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        initViews();
        setupListeners();

        // 初始化
        userRepository = new UserRepository(this);
        emailCodeManager = EmailCodeManager.getInstance();
    }

    /**
     * 初始化视图
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etVerificationCode = findViewById(R.id.etVerificationCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnResetPassword = findViewById(R.id.btnResetPassword);
    }

    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 发送验证码按钮
        btnSendCode.setOnClickListener(v -> sendVerificationCode());

        // 重置密码按钮
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    /**
     * 发送验证码
     */
    private void sendVerificationCode() {
        String email = etEmail.getText().toString().trim();

        // 验证邮箱
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return;
        }

        // 检查邮箱是否已注册
        userRepository.checkEmailExists(email, exists -> {
            if (!exists) {
                Toast.makeText(ForgetPasswordActivity.this, "该邮箱未注册，请检查邮箱地址", Toast.LENGTH_SHORT).show();
                return;
            }

            // 显示加载状态
            btnSendCode.setEnabled(false);
            btnSendCode.setText("发送中...");

            // 在后台线程发送验证码
            Executors.newSingleThreadExecutor().execute(() -> {
                boolean success = emailCodeManager.sendVerificationCode(email);

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(ForgetPasswordActivity.this, "验证码已发送到您的邮箱，请查收", Toast.LENGTH_LONG).show();
                        startCountDown();
                    } else {
                        btnSendCode.setEnabled(true);
                        btnSendCode.setText("发送验证码");

                        long remainingTime = emailCodeManager.getRemainingSendTime(email);
                        if (remainingTime > 0) {
                            Toast.makeText(ForgetPasswordActivity.this,
                                    "发送过于频繁，请" + remainingTime + "秒后再试", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgetPasswordActivity.this, "发送失败，请重试", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        });
    }

    /**
     * 开始倒计时
     */
    private void startCountDown() {
        btnSendCode.setEnabled(false);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                btnSendCode.setText(seconds + "秒后重发");
            }

            @Override
            public void onFinish() {
                btnSendCode.setEnabled(true);
                btnSendCode.setText("发送验证码");
            }
        };
        countDownTimer.start();
    }

    /**
     * 重置密码
     */
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String code = etVerificationCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 验证输入
        if (!validateInput(email, code, newPassword, confirmPassword)) {
            return;
        }

        // 验证验证码
        if (!emailCodeManager.verifyCode(email, code)) {
            Toast.makeText(this, "验证码错误或已过期，请重新获取", Toast.LENGTH_SHORT).show();
            return;
        }

        // 重置密码
        resetPasswordForEmail(email, newPassword);
    }

    /**
     * 验证输入
     */
    private boolean validateInput(String email, String code, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            Toast.makeText(this, "请输入正确的邮箱地址", Toast.LENGTH_SHORT).show();
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            etVerificationCode.requestFocus();
            return false;
        }

        if (code.length() != 6) {
            Toast.makeText(this, "验证码为6位数字", Toast.LENGTH_SHORT).show();
            etVerificationCode.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "密码长度至少6位", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 为指定邮箱重置密码
     */
    private void resetPasswordForEmail(String email, String newPassword) {
        // 显示加载提示
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("重置中...");

        // 使用UserRepository的updatePasswordByEmail方法
        userRepository.updatePasswordByEmail(email, newPassword, success -> {
            btnResetPassword.setEnabled(true);
            btnResetPassword.setText("重置密码");

            if (success) {
                // 重置成功
                Toast.makeText(ForgetPasswordActivity.this, "密码重置成功，请使用新密码登录", Toast.LENGTH_LONG).show();

                // 延迟关闭页面
                etEmail.postDelayed(() -> {
                    finish();
                }, 1500);
            } else {
                // 重置失败
                Toast.makeText(ForgetPasswordActivity.this, "密码重置失败，请重试", Toast.LENGTH_SHORT).show();
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