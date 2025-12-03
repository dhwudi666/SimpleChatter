package com.example.simplechatter.util;

import android.content.Context;
import android.content.Intent;
import com.example.simplechatter.MainActivity;
import com.example.simplechatter.activity.ContactsActivity;
import com.example.simplechatter.activity.ForgetPasswordActivity;
import com.example.simplechatter.activity.HomeActivity;
import com.example.simplechatter.activity.ProfileActivity;
import com.example.simplechatter.activity.RegisterActivity;

/**
 * 导航辅助工具类
 * 统一管理Activity跳转
 */
public class NavigationHelper {

    /**
     * 跳转到登录页
     */
    public static void navigateToLogin(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 跳转到注册页
     */
    public static void navigateToRegister(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    /**
     * 跳转到忘记密码页
     */
    public static void navigateToForgetPassword(Context context) {
        context.startActivity(new Intent(context, ForgetPasswordActivity.class));
    }

    /**
     * 跳转到联系人列表
     */
    public static void navigateToContacts(Context context) {
        context.startActivity(new Intent(context, ContactsActivity.class));
    }

    /**
     * 跳转到首页
     */
    public static void navigateToHome(Context context) {
        context.startActivity(new Intent(context, HomeActivity.class));
    }

    /**
     * 跳转到个人中心
     */
    public static void navigateToProfile(Context context) {
        context.startActivity(new Intent(context, ProfileActivity.class));
    }
}