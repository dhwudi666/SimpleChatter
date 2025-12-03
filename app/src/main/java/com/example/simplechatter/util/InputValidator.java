package com.example.simplechatter.util;

import android.text.TextUtils;
import java.util.regex.Pattern;

/**
 * 输入验证工具类
 * 统一管理邮箱、密码等验证逻辑
 */
public class InputValidator {
    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证密码长度
     */
    public static boolean isValidPassword(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= 6;
    }

    /**
     * 验证验证码格式（6位数字）
     */
    public static boolean isValidVerificationCode(String code) {
        return !TextUtils.isEmpty(code) && code.length() == 6 && code.matches("\\d{6}");
    }

    /**
     * 验证两次密码是否一致
     */
    public static boolean isPasswordMatch(String password, String confirmPassword) {
        return !TextUtils.isEmpty(password) && password.equals(confirmPassword);
    }
}