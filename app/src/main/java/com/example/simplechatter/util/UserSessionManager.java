package com.example.simplechatter.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 用户会话管理工具类
 * 统一管理用户登录状态和用户信息
 */
public class UserSessionManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NICKNAME = "user_nickname";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_LOGIN_TIME = "last_login_time";

    private SharedPreferences prefs;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * 保存用户登录信息
     */
    public void saveUserLogin(int userId, String email, String nickname) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NICKNAME, nickname);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * 获取当前用户ID
     */
    public int getCurrentUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * 获取用户邮箱
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    /**
     * 获取用户昵称
     */
    public String getUserNickname() {
        return prefs.getString(KEY_USER_NICKNAME, "");
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUserId() != -1;
    }

    /**
     * 清除登录状态
     */
    public void clearLogin() {
        prefs.edit().clear().apply();
    }

    /**
     * 获取最后登录时间
     */
    public long getLastLoginTime() {
        return prefs.getLong(KEY_LAST_LOGIN_TIME, 0);
    }
}