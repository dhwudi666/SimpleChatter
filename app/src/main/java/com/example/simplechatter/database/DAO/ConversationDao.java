package com.example.simplechatter.database.DAO;

import androidx.room.*;
import com.example.simplechatter.database.Entity.Conversation;

import java.util.List;

@Dao
public interface ConversationDao {
    @Insert
    long insertConversation(Conversation conversation);

    @Update
    int updateConversation(Conversation conversation);

    @Delete
    int deleteConversation(Conversation conversation);

    // 获取会话（包括已删除的，用于恢复）
    @Query("SELECT * FROM conversations WHERE userId = :userId AND contactId = :contactId LIMIT 1")
    Conversation getConversation(int userId, int contactId);

    // 获取未删除的会话
    @Query("SELECT * FROM conversations WHERE userId = :userId AND contactId = :contactId AND isDeleted = 0 LIMIT 1")
    Conversation getActiveConversation(int userId, int contactId);

    // 软删除：标记会话为已删除（不真正删除数据）
    @Query("UPDATE conversations SET isDeleted = 1 WHERE userId = :userId AND contactId = :contactId")
    int markConversationAsDeleted(int userId, int contactId);

    // 恢复会话：标记为未删除
    @Query("UPDATE conversations SET isDeleted = 0 WHERE userId = :userId AND contactId = :contactId")
    int restoreConversation(int userId, int contactId);

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    void incrementUnreadCount(int conversationId);

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    void clearUnreadCount(int conversationId);
}