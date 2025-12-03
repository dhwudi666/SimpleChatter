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

    // 获取用户的所有联系人（按最后消息时间排序，只查询未删除的）
    @Query("SELECT * FROM contacts WHERE userId = :userId AND isDeleted = 0 ORDER BY lastMessageTime DESC")
    List<Contact> getContactsByUserId(int userId);

    // 搜索联系人（只查询未删除的）
    @Query("SELECT * FROM contacts WHERE userId = :userId AND name LIKE '%' || :keyword || '%' AND isDeleted = 0")
    List<Contact> searchContacts(int userId, String keyword);

    // 清空未读消息数
    @Query("UPDATE contacts SET unreadCount = 0 WHERE id = :contactId")
    void clearUnreadCount(int contactId);

    // 获取总未读消息数（只统计未删除的）
    @Query("SELECT SUM(unreadCount) FROM contacts WHERE userId = :userId AND isDeleted = 0")
    int getTotalUnreadCount(int userId);


    // 根据用户ID和联系人ID获取特定联系人（包括已删除的，用于恢复）
    @Query("SELECT * FROM contacts WHERE userId = :userId AND contactId = :contactId")
    Contact getContact(int userId, int contactId);

    // 根据用户ID和联系人ID获取未删除的联系人
    @Query("SELECT * FROM contacts WHERE userId = :userId AND contactId = :contactId AND isDeleted = 0")
    Contact getActiveContact(int userId, int contactId);

    // 软删除：标记为已删除（不真正删除数据）
    @Query("UPDATE contacts SET isDeleted = 1 WHERE userId = :userId AND contactId = :contactId")
    int markContactAsDeleted(int userId, int contactId);

    // 恢复联系人：标记为未删除
    @Query("UPDATE contacts SET isDeleted = 0 WHERE userId = :userId AND contactId = :contactId")
    int restoreContact(int userId, int contactId);
}