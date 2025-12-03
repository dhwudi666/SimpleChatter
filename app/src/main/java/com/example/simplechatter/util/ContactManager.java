package com.example.simplechatter.util;

import android.content.Context;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.database.Entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactManager {
    private ContactDao contactDao;
    private UserDao userDao;
    private ExecutorService executor;

    public ContactManager(Context context) {
        AppDataBase db = AppDataBase.getInstance(context);
        contactDao = db.contactDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * 添加联系人
     */
    public void addContact(int userId, int contactUserId, OnContactAddListener listener) {
        executor.execute(() -> {
            try {
                // 检查是否已是联系人
                Contact existing = contactDao.getContact(userId, contactUserId);
                if (existing != null) {
                    // 已是联系人，更新信息
                    updateContactInfo(existing, contactUserId);
                    if (listener != null) {
                        listener.onContactAddResult(true, "联系人已存在，信息已更新");
                    }
                    return;
                }

                // 获取联系人的用户信息（用于同步昵称）
                User contactUser = userDao.getUserById(contactUserId);
                if (contactUser == null) {
                    if (listener != null) {
                        listener.onContactAddResult(false, "用户不存在");
                    }
                    return;
                }

                // 创建新联系人，使用用户的昵称
                String displayName = contactUser.getNickname() != null && !contactUser.getNickname().isEmpty()
                        ? contactUser.getNickname()
                        : contactUser.getEmail().split("@")[0]; // 使用邮箱用户名作为备选

                Contact contact = new Contact(userId, contactUserId, displayName);
                contact.setAvatar(contactUser.getAvatar()); // 同步头像

                long result = contactDao.insertContact(contact);

                if (listener != null) {
                    listener.onContactAddResult(result > 0, result > 0 ? "添加成功" : "添加失败");
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.onContactAddResult(false, "添加失败: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 更新联系人信息（同步用户最新信息）
     */
    private void updateContactInfo(Contact contact, int contactUserId) {
        User contactUser = userDao.getUserById(contactUserId);
        if (contactUser != null) {
            // 同步最新昵称和头像
            String newName = contactUser.getNickname() != null && !contactUser.getNickname().isEmpty()
                    ? contactUser.getNickname()
                    : contactUser.getEmail().split("@")[0];

            contact.setName(newName);
            contact.setAvatar(contactUser.getAvatar());
            contactDao.updateContact(contact);
        }
    }

    /**
     * 获取用户的所有联系人（自动同步最新信息）
     */
    public void getContacts(int userId, OnContactsLoadListener listener) {
        executor.execute(() -> {
            try {
                List<Contact> contacts = contactDao.getContactsByUserId(userId);

                // 同步每个联系人的最新信息
                for (Contact contact : contacts) {
                    updateContactInfo(contact, contact.getContactId());
                }

                if (listener != null) {
                    listener.onContactsLoaded(contacts);
                }

            } catch (Exception e) {
                if (listener != null) {
                    listener.onContactsLoadFailed("加载失败: " + e.getMessage());
                }
            }
        });
    }
//

//    private void addContactSync(int userId, int contactUserId) {
//        Contact existing = contactDao.getContact(userId, contactUserId);
//        if (existing == null) {
//            User contactUser = userDao.getUserById(contactUserId);
//            if (contactUser != null) {
//                String displayName = contactUser.getNickname() != null && !contactUser.getNickname().isEmpty()
//                        ? contactUser.getNickname()
//                        : contactUser.getEmail().split("@")[0];
//
//                Contact contact = new Contact(userId, contactUserId, displayName);
//                contact.setAvatar(contactUser.getAvatar());
//                contactDao.insertContact(contact);
//            }
//        }
//    }

    // 回调接口
    public interface OnContactAddListener {
        void onContactAddResult(boolean success, String message);
    }

    public interface OnContactsLoadListener {
        void onContactsLoaded(List<Contact> contacts);
        void onContactsLoadFailed(String error);
    }

}