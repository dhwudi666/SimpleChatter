package com.example.simplechatter.database.DAO;

import androidx.room.*;
import com.example.simplechatter.database.Entity.Contact;

import java.util.List;

@Dao
public interface ContactDao {
    @Insert
    long insertContact(Contact contact);

    @Update
    int updateContact(Contact contact);

    @Delete
    int deleteContact(Contact contact);

    // 获取用户的所有联系人（按最后消息时间排序）
    @Query("SELECT * FROM contacts WHERE userId = :userId ORDER BY lastMessageTime DESC")
    List<Contact> getContactsByUserId(int userId);

    // 搜索联系人
    @Query("SELECT * FROM contacts WHERE userId = :userId AND name LIKE '%' || :keyword || '%'")
    List<Contact> searchContacts(int userId, String keyword);

    // 清空未读消息数
    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :contactId")
    void clearUnreadCount(int contactId);

    // ✅ 修复：添加按用户和联系人ID清空未读的方法
    @Query("UPDATE contacts SET unreadCount = 0 WHERE userId = :userId AND contactId = :contactId")
    int clearUnreadCountForUser(int userId, int contactId);
    // 更新最后消息
    @Query("UPDATE contacts SET lastMessage = :message, lastMessageTime = :time WHERE id = :contactId")
    void updateLastMessage(int contactId, String message, long time);

    // 未读消息数
    @Query("UPDATE contacts SET unreadCount = unreadCount + 1 WHERE id = :contactId")
    void incrementUnreadCount(int contactId);
    // ✅ 新增：收到消息时更新未读计数
    @Query("UPDATE contacts SET " +
            "lastMessage = :lastMessage, " +
            "lastMessageTime = :timestamp, " +
            "unreadCount = unreadCount + 1 " +
            "WHERE userId = :userId AND contactId = :contactId")
    int updateContactWithNewMessage(int userId, int contactId, String lastMessage, long timestamp);
    // 获取总未读消息数
    @Query("SELECT SUM(unreadCount) FROM contacts WHERE userId = :userId")
    int getTotalUnreadCount(int userId);
    // ✅ 新增：插入或更新联系人的方法（重要！）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOrUpdateContact(Contact contact);

    // 根据用户ID和联系人ID获取特定联系人
    @Query("SELECT * FROM contacts WHERE userId = :userId AND contactId = :contactId")
    Contact getContact(int userId, int contactId);

    //删除特定联系人
    @Query("DELETE FROM contacts WHERE id = :contactId")
    int deleteContact(int contactId);
}