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

//    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY lastMessageTime DESC")
//    List<Conversation> getConversationsByUserId(int userId);

    @Query("SELECT * FROM conversations WHERE userId = :userId AND contactId = :contactId LIMIT 1")
    Conversation getConversation(int userId, int contactId);

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :conversationId")
    void incrementUnreadCount(int conversationId);
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    void clearUnreadCount(int conversationId);

//    @Query("UPDATE conversations SET lastMessage = :message, lastMessageTime = :time WHERE id = :conversationId")
//    void updateLastMessage(int conversationId, String message, long time);
}