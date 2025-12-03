package com.example.simplechatter.util;

import android.content.Context;
import android.util.Log;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.DAO.ConversationDao;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.database.Entity.Conversation;
import com.example.simplechatter.database.Entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactManager {
    private static final String TAG = "ContactManager";

    private ContactDao contactDao;
    private ConversationDao conversationDao;
    private UserDao userDao;
    private ExecutorService executor;

    public ContactManager(Context context) {
        AppDataBase db = AppDataBase.getInstance(context);
        contactDao = db.contactDao();
        conversationDao = db.conversationDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * 检查是否为双向好友（只检查未删除的联系人）
     */
    public boolean isMutualFriend(int userId, int contactUserId) {
        try {
            // 检查A是否添加了B（未删除）
            Contact contact1 = contactDao.getActiveContact(userId, contactUserId);
            // 检查B是否添加了A（未删除）
            Contact contact2 = contactDao.getActiveContact(contactUserId, userId);

            return contact1 != null && contact2 != null;
        } catch (Exception e) {
            Log.e(TAG, "检查双向好友失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 添加联系人（自动创建会话，检查双向好友关系，支持恢复已删除的联系人）
     */
    public void addContact(int userId, int contactUserId, OnContactAddListener listener) {
        executor.execute(() -> {
            try {
                // 先检查是否存在已删除的联系人记录（用于恢复）
                Contact existing = contactDao.getContact(userId, contactUserId);

                if (existing != null) {
                    // 如果联系人已存在但被删除了，恢复它
                    if (existing.getIsDeleted() == 1) {
                        Log.d(TAG, "恢复已删除的联系人: userId=" + userId + ", contactId=" + contactUserId);
                        contactDao.restoreContact(userId, contactUserId);
                        existing.setIsDeleted(0);
                    }

                    // 更新联系人信息
                    updateContactInfo(existing, contactUserId);

                    // 检查并恢复/创建会话
                    Conversation conversation = conversationDao.getConversation(userId, contactUserId);
                    if (conversation != null) {
                        if (conversation.getIsDeleted() == 1) {
                            // 恢复已删除的会话
                            Log.d(TAG, "恢复已删除的会话: userId=" + userId + ", contactId=" + contactUserId);
                            conversationDao.restoreConversation(userId, contactUserId);
                        }
                    } else {
                        // 创建新会话
                        ensureConversationExists(userId, contactUserId);
                    }

                    // 检查双向好友关系
                    boolean isMutual = isMutualFriend(userId, contactUserId);
                    String message = isMutual
                            ? "联系人已恢复，可以开始聊天了"
                            : "联系人已恢复，但你还不是对方的好友";

                    if (listener != null) {
                        listener.onContactAddResult(true, message, isMutual);
                    }
                    return;
                }

                // 获取联系人的用户信息（用于同步昵称）
                User contactUser = userDao.getUserById(contactUserId);
                if (contactUser == null) {
                    if (listener != null) {
                        listener.onContactAddResult(false, "用户不存在", false);
                    }
                    return;
                }

                // 创建新联系人，使用用户的昵称
                String displayName = contactUser.getNickname() != null && !contactUser.getNickname().isEmpty()
                        ? contactUser.getNickname()
                        : contactUser.getEmail().split("@")[0]; // 使用邮箱用户名作为备选

                Contact contact = new Contact(userId, contactUserId, displayName);
                contact.setAvatar(contactUser.getAvatar()); // 同步头像
                contact.setIsDeleted(0); // 确保标记为未删除

                long result = contactDao.insertContact(contact);

                if (result > 0) {
                    // 检查是否存在已删除的会话，如果存在则恢复，否则创建新会话
                    Conversation conversation = conversationDao.getConversation(userId, contactUserId);
                    if (conversation != null && conversation.getIsDeleted() == 1) {
                        // 恢复已删除的会话
                        Log.d(TAG, "恢复已删除的会话: userId=" + userId + ", contactId=" + contactUserId);
                        conversationDao.restoreConversation(userId, contactUserId);
                    } else {
                        // 自动创建会话
                        ensureConversationExists(userId, contactUserId);
                    }

                    // 检查双向好友关系
                    boolean isMutual = isMutualFriend(userId, contactUserId);
                    String message = isMutual
                            ? "添加成功，可以开始聊天了"
                            : "添加成功，但你还不是对方的好友，无法发送消息";

                    if (listener != null) {
                        listener.onContactAddResult(true, message, isMutual);
                    }
                } else {
                    if (listener != null) {
                        listener.onContactAddResult(false, "添加失败", false);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, "添加联系人失败: " + e.getMessage());
                e.printStackTrace();
                if (listener != null) {
                    listener.onContactAddResult(false, "添加失败: " + e.getMessage(), false);
                }
            }
        });
    }

    /**
     * 确保会话存在（如果不存在则创建）
     */
    private void ensureConversationExists(int userId, int contactId) {
        try {
            Conversation conversation = conversationDao.getActiveConversation(userId, contactId);
            if (conversation == null) {
                // 检查是否存在已删除的会话
                Conversation deletedConversation = conversationDao.getConversation(userId, contactId);
                if (deletedConversation != null && deletedConversation.getIsDeleted() == 1) {
                    // 恢复已删除的会话
                    conversationDao.restoreConversation(userId, contactId);
                    Log.d(TAG, "恢复已删除的会话: userId=" + userId + ", contactId=" + contactId);
                } else {
                    // 创建新会话
                    Conversation newConversation = new Conversation(userId, contactId);
                    newConversation.setIsDeleted(0);
                    conversationDao.insertConversation(newConversation);
                    Log.d(TAG, "自动创建会话: userId=" + userId + ", contactId=" + contactId);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "创建会话失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 软删除联系人（标记为已删除，不真正删除数据，保留聊天记录）
     * @param userId 当前用户ID
     * @param contactId 联系人用户ID
     */
    public void deleteContact(int userId, int contactId, OnContactDeleteListener listener) {
        executor.execute(() -> {
            try {
                // 软删除联系人记录（标记为已删除，不真正删除）
                int contactResult = contactDao.markContactAsDeleted(userId, contactId);

                // 软删除会话记录（标记为已删除，不真正删除）
                int conversationResult = conversationDao.markConversationAsDeleted(userId, contactId);

                Log.d(TAG, "软删除联系人: userId=" + userId + ", contactId=" + contactId +
                        ", 联系人标记结果=" + contactResult + ", 会话标记结果=" + conversationResult);

                // 注意：不删除messages表中的消息，保留聊天记录

                if (listener != null) {
                    boolean success = contactResult > 0;
                    listener.onContactDeleteResult(success, success ? "删除成功，聊天记录已保留" : "删除失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "删除联系人失败: " + e.getMessage());
                e.printStackTrace();
                if (listener != null) {
                    listener.onContactDeleteResult(false, "删除失败: " + e.getMessage());
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

    // 回调接口
    public interface OnContactAddListener {
        /**
         * @param success 是否成功
         * @param message 提示消息
         * @param isMutualFriend 是否为双向好友
         */
        void onContactAddResult(boolean success, String message, boolean isMutualFriend);
    }

    public interface OnContactDeleteListener {
        void onContactDeleteResult(boolean success, String message);
    }
}