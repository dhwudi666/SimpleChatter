package com.example.simplechatter.util;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.Entity.Contact;

public class ContactDataInitializer {
    private ContactDao contactDao;
    private ExecutorService executor;

    public ContactDataInitializer(Context context) {
        AppDataBase db = AppDataBase.getInstance(context);
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void initializeSampleData(int userId) {
        executor.execute(() -> {
            // 检查是否已有数据
            if (contactDao.getContactsByUserId(userId).isEmpty()) {
                // 使用简化构造函数，然后通过setter设置其他属性
                Contact contact1 = new Contact(userId, "user2", "张三");
                contact1.setAvatar("avatar_zhang");
                contact1.setLastMessage("你好，最近怎么样？");
                contact1.setLastMessageTime(System.currentTimeMillis() - 3600000); // 1小时前
                contact1.setUnreadCount(2);
                contact1.setOnline(true);
                contact1.setCreateTime(System.currentTimeMillis());

                Contact contact2 = new Contact(userId, "user3", "李四");
                contact2.setAvatar("avatar_li");
                contact2.setLastMessage("明天开会别忘了");
                contact2.setLastMessageTime(System.currentTimeMillis() - 7200000); // 2小时前
                contact2.setUnreadCount(0);
                contact2.setOnline(false);
                contact2.setCreateTime(System.currentTimeMillis());

                Contact contact3 = new Contact(userId, "user4", "王五");
                contact3.setAvatar("avatar_wang");
                contact3.setLastMessage("[图片]");
                contact3.setLastMessageTime(System.currentTimeMillis() - 10800000); // 3小时前
                contact3.setUnreadCount(1);
                contact3.setOnline(true);
                contact3.setCreateTime(System.currentTimeMillis());

                Contact contact4 = new Contact(userId, "user5", "赵六");
                contact4.setAvatar("avatar_zhao");
                contact4.setLastMessage("好的，没问题");
                contact4.setLastMessageTime(System.currentTimeMillis() - 14400000); // 4小时前
                contact4.setUnreadCount(0);
                contact4.setOnline(false);
                contact4.setCreateTime(System.currentTimeMillis());

                Contact contact5 = new Contact(userId, "user6", "钱七");
                contact5.setAvatar("avatar_qian");
                contact5.setLastMessage("语音通话 00:32");
                contact5.setLastMessageTime(System.currentTimeMillis() - 18000000); // 5小时前
                contact5.setUnreadCount(3);
                contact5.setOnline(true);
                contact5.setCreateTime(System.currentTimeMillis());

                Contact contact6 = new Contact(userId, "user7", "孙八");
                contact6.setAvatar("avatar_sun");
                contact6.setLastMessage("晚上一起吃饭？");
                contact6.setLastMessageTime(System.currentTimeMillis() - 21600000); // 6小时前
                contact6.setUnreadCount(0);
                contact6.setOnline(true);
                contact6.setCreateTime(System.currentTimeMillis());

                Contact contact7 = new Contact(userId, "user8", "周九");
                contact7.setAvatar("avatar_zhou");
                contact7.setLastMessage("项目进展如何？");
                contact7.setLastMessageTime(System.currentTimeMillis() - 25200000); // 7小时前
                contact7.setUnreadCount(1);
                contact7.setOnline(false);
                contact7.setCreateTime(System.currentTimeMillis());

                Contact[] contacts = {contact1, contact2, contact3, contact4, contact5, contact6, contact7};

                for (Contact contact : contacts) {
                    contactDao.insertContact(contact);
                }

                System.out.println("示例联系人数据插入完成，共插入 " + contacts.length + " 个联系人");
            } else {
                System.out.println("联系人数据已存在，跳过初始化");
            }
        });
    }

    // 清空联系人数据（用于测试）
    public void clearContactData(int userId) {
        executor.execute(() -> {
            List<Contact> contacts = contactDao.getContactsByUserId(userId);
            for (Contact contact : contacts) {
                contactDao.deleteContact(contact);
            }
            System.out.println("已清空联系人数据");
        });
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}