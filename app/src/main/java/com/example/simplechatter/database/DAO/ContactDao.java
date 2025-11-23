package com.example.simplechatter.database.DAO;

import androidx.room.*;
import com.example.simplechatter.database.Entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    // 插入联系人
    @Insert
    long insertContact(Contact contact);

    // 更新联系人
    @Update
    int updateContact(Contact contact);

    // 删除联系人
    @Delete
    int deleteContact(Contact contact);

    // 获取用户的所有联系人（按最后消息时间排序）
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY lastMessageTime DESC")
    List<Contact> getContactsByUserId(int userId);

    // 根据ID获取特定联系人（图片中显示的方法）
    @Query("SELECT * FROM contacts WHERE userId = :userId AND contactId = :contactId LIMIT 1")
    Contact getContact(int userId, String contactId);

    // 搜索联系人
    @Query("SELECT * FROM contacts WHERE userId = :userId AND name LIKE '%' || :keyword || '%'")
    List<Contact> searchContacts(int userId, String keyword);

    // 清空未读消息数
    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :contactId")
    void clearUnreadCount(int contactId);

    // 更新最后消息
    @Query("UPDATE contacts SET lastMessage = :message, lastMessageTime = :time WHERE id = :contactId")
    void updateLastMessage(int contactId, String message, long time);

    // 增加未读消息数
    @Query("UPDATE contacts SET unreadCount = unreadCount + 1 WHERE id = :contactId")
    void incrementUnreadCount(int contactId);

    // 更新在线状态
    @Query("UPDATE contacts SET isOnline = :isOnline WHERE id = :contactId")
    void updateOnlineStatus(int contactId, boolean isOnline);
}