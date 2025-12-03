package com.example.simplechatter.util;

/**
 * 邮件配置
 */
public class EmailConfig {
    // ========== QQ邮箱配置 ==========
    public static final String QQ_SMTP_HOST = "smtp.qq.com";
    public static final String QQ_SMTP_PORT = "587";

    // 请修改为实际使用的邮箱服务商
    public static final String SMTP_HOST = QQ_SMTP_HOST;
    public static final String SMTP_PORT = QQ_SMTP_PORT;

    // 发送邮件的邮箱地址
    public static final String FROM_EMAIL = "1048617882@qq.com";

    // 邮箱授权码或密码
    public static final String FROM_PASSWORD = "jrhjnvtyhomgbcfh";

    // 发送者名称
    public static final String FROM_NAME = "SimpleChatter";
}