package com.example.simplechatter.util;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 邮箱验证码管理器
 * 使用JavaMail发送邮件
 */
public class EmailCodeManager {
    private static final String TAG = "EmailCodeManager";
    private static EmailCodeManager instance;

    // 存储验证码：key=邮箱, value=验证码信息
    private Map<String, CodeInfo> codeMap = new HashMap<>();

    // 验证码有效期（10分钟）
    private static final long CODE_VALID_TIME = 10 * 60 * 1000;

    // 发送间隔（60秒）
    private static final long SEND_INTERVAL = 60 * 1000;

    private EmailCodeManager() {}

    //线程安全地延迟创建唯一实例，供全 App 复用
    public static synchronized EmailCodeManager getInstance() {
        if (instance == null) {
            instance = new EmailCodeManager();
        }
        return instance;
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    /**
     * 发送验证码
     * @param email 邮箱地址
     * @return 是否发送成功
     */
    public boolean sendVerificationCode(String email) {
        // 检查发送间隔
        CodeInfo lastCode = codeMap.get(email);
        if (lastCode != null) {
            long timeSinceLastSend = System.currentTimeMillis() - lastCode.sendTime;
            if (timeSinceLastSend < SEND_INTERVAL) {
                long remainingSeconds = (SEND_INTERVAL - timeSinceLastSend) / 1000;
                Log.w(TAG, "发送过于频繁，请" + remainingSeconds + "秒后再试");
                return false;
            }
        }

        // 生成验证码
        String code = generateCode();
        long sendTime = System.currentTimeMillis();

        // 保存验证码信息
        codeMap.put(email, new CodeInfo(code, sendTime));

        // 异步发送邮件
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean success = sendEmail(email, code);
            if (!success) {
                // 发送失败，移除验证码
                codeMap.remove(email);
            }
        });

        return true;
    }

    /**
     * 使用JavaMail发送邮件
     */
    private boolean sendEmail(String toEmail, String code) {
        Transport transport = null;
        try {
            // 检查配置
            if (EmailConfig.FROM_EMAIL.equals("your-email@qq.com") ||
                    EmailConfig.FROM_PASSWORD.equals("your-app-password")) {
                Log.e(TAG, "请先配置EmailConfig中的邮箱和授权码");
                Log.d(TAG, "【测试模式】验证码: " + code + " (邮箱: " + toEmail + ")");
                return true;
            }

            // 配置邮件服务器属性
            Properties props = new Properties();
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.host", EmailConfig.SMTP_HOST);
            props.put("mail.smtp.port", EmailConfig.SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");

            // 创建Session
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EmailConfig.FROM_EMAIL, EmailConfig.FROM_PASSWORD);
                }
            });

            // 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.FROM_EMAIL));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("密码重置验证码 - SimpleChatter");

            // 使用纯文本内容，避免HTML相关的AWT依赖
            String textContent = "您的验证码是：" + code + "\n\n" +
                    "验证码有效期为10分钟，请及时使用。\n\n" +
                    "如果这不是您的操作，请忽略此邮件。\n\n" +
                    "SimpleChatter 团队";
            message.setText(textContent, "UTF-8");

            // 发送邮件
            transport = session.getTransport("smtp");
            transport.connect(EmailConfig.SMTP_HOST, Integer.parseInt(EmailConfig.SMTP_PORT),
                    EmailConfig.FROM_EMAIL, EmailConfig.FROM_PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());

            Log.d(TAG, "验证码已成功发送到: " + toEmail);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "发送邮件失败: " + e.getMessage());
            e.printStackTrace();
            Log.d(TAG, "【测试模式】验证码: " + code + " (邮箱: " + toEmail + ")");
            return false;
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    Log.e(TAG, "关闭Transport失败: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 验证验证码
     * @param email 邮箱地址
     * @param inputCode 用户输入的验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String email, String inputCode) {
        CodeInfo codeInfo = codeMap.get(email);

        if (codeInfo == null) {
            Log.w(TAG, "未找到该邮箱的验证码");
            return false;
        }

        // 检查是否过期
        long timeSinceSend = System.currentTimeMillis() - codeInfo.sendTime;
        if (timeSinceSend > CODE_VALID_TIME) {
            codeMap.remove(email);
            Log.w(TAG, "验证码已过期");
            return false;
        }

        // 验证码是否正确
        if (codeInfo.code.equals(inputCode)) {
            // 验证成功后删除验证码（防止重复使用）
            codeMap.remove(email);
            return true;
        } else {
            Log.w(TAG, "验证码错误");
            return false;
        }
    }

    /**
     * 验证码信息类
     */
    private static class CodeInfo {
        String code;
        long sendTime;

        CodeInfo(String code, long sendTime) {
            this.code = code;
            this.sendTime = sendTime;
        }
    }

    /**
     * 获取剩余发送时间（秒）
     */
    public long getRemainingSendTime(String email) {
        CodeInfo codeInfo = codeMap.get(email);
        if (codeInfo == null) {
            return 0;
        }
        long timeSinceLastSend = System.currentTimeMillis() - codeInfo.sendTime;
        if (timeSinceLastSend >= SEND_INTERVAL) {
            return 0;
        }
        return (SEND_INTERVAL - timeSinceLastSend) / 1000;
    }
}