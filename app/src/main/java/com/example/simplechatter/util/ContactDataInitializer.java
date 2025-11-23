package com.example.simplechatter.util;

import android.content.Context;
import android.util.Log;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.Entity.Contact;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactDataInitializer {
    private ContactDao contactDao;
    private ExecutorService executor;
    private static final String TAG = "ContactDataInitializer";

    public ContactDataInitializer(Context context) {
        AppDataBase db = AppDataBase.getInstance(context);
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public void initializeSampleData(int userId) {
        executor.execute(() -> {
            try {
                Log.d(TAG, "为用户 " + userId + " 初始化联系人数据");

                // 检查是否已有数据
                if (contactDao.getContactsByUserId(userId).isEmpty()) {
                    Log.d(TAG, "插入示例联系人数据");

                    // 根据用户ID生成不同的联系人数据
                    Contact[] contacts = generateContactsForUser(userId);

                    for (Contact contact : contacts) {
                        contactDao.insertContact(contact);
                    }

                    Log.d(TAG, "成功插入 " + contacts.length + " 个联系人");
                } else {
                    Log.d(TAG, "用户 " + userId + " 已有联系人数据，跳过初始化");
                }

            } catch (Exception e) {
                Log.e(TAG, "初始化联系人数据失败: " + e.getMessage());
            }
        });
    }

    private Contact[] generateContactsForUser(int userId) {
        // 根据用户ID生成不同的联系人列表
        switch (userId) {
            case 1:
                return new Contact[]{
                        createContact(userId, "user2", "张三", "avatar_zhang",
                                "你好，最近怎么样？", System.currentTimeMillis() - 3600000, 2, true),
                        createContact(userId, "user3", "李四", "avatar_li",
                                "明天开会别忘了", System.currentTimeMillis() - 7200000, 0, false),
                        createContact(userId, "user4", "王五", "avatar_wang",
                                "[图片]", System.currentTimeMillis() - 10800000, 1, true)
                };
            case 2:
                return new Contact[]{
                        createContact(userId, "user1", "系统管理员", "avatar_admin",
                                "欢迎使用聊天应用", System.currentTimeMillis() - 1800000, 0, true),
                        createContact(userId, "user3", "李四", "avatar_li",
                                "项目进展如何？", System.currentTimeMillis() - 5400000, 1, false)
                };
            default:
                return new Contact[]{
                        createContact(userId, "user1", "默认联系人", "avatar_default",
                                "你好！", System.currentTimeMillis() - 3600000, 0, true)
                };
        }
    }

    private Contact createContact(int userId, String contactId, String name, String avatar,
                                  String lastMessage, long lastMessageTime, int unreadCount, boolean isOnline) {
        Contact contact = new Contact(userId, contactId, name);
        contact.setAvatar(avatar);
        contact.setLastMessage(lastMessage);
        contact.setLastMessageTime(lastMessageTime);
        contact.setUnreadCount(unreadCount);
        contact.setOnline(isOnline);
        return contact;
    }

}