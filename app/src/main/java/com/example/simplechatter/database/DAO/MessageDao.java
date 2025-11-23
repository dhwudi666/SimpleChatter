package com.example.simplechatter.database.DAO;

import androidx.room.*;
import com.example.simplechatter.database.Entity.Message;
import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    long insertMessage(Message message);

    @Update
    int updateMessage(Message message);

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<Message> getMessagesByConversation(int conversationId);

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    Message getMessageById(int messageId);

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    int updateMessageStatus(int messageId, int status);

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND status < 3")
    int getUnreadMessageCount(int conversationId);
}