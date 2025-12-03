package com.example.simplechatter.database.DAO;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.simplechatter.database.Entity.Message;
import com.example.simplechatter.database.Entity.User;

import java.util.List;

@Dao
public interface MessageDao {
    @Insert
    long insertMessage(Message message);

    @Transaction
    default long insertMessageAndUpdateUnread(Message message) {
        try {
            // 1. 插入消息
            long messageId = insertMessage(message);

            if (messageId > 0) {
                // 2. 更新接收者的联系人未读计数
                updateReceiverContactUnread(message);
                // 3. 更新会话最后消息
                updateConversationLastMessage(message);
            }

            return messageId;
        } catch (Exception e) {
            Log.e("MessageDao", "插入消息失败: " + e.getMessage());
            return -1;
        }
    }
    default void updateConversationLastMessage(Message message) {
        // 更新会话的最后消息逻辑
    }

    @Query("UPDATE contacts SET " +
            "lastMessage = :content, " +
            "lastMessageTime = :timestamp, " +
            "unreadCount = unreadCount + 1 " +
            "WHERE userId = :receiverId AND contactId = :senderId")
    void updateReceiverContactUnread(int receiverId, int senderId, String content, long timestamp);

    // 辅助方法
    default void updateReceiverContactUnread(Message message) {
        updateReceiverContactUnread(
                message.getReceiverId(),
                message.getSenderId(),
                message.getContent(),
                message.getTimestamp()
        );
    }

    @Update
    int updateMessage(Message message);
    @Delete
    int Message(Message message);

    @Query("SELECT * FROM messages WHERE " +
            "(senderId = :userId1 AND receiverId = :userId2) OR " +
            "(senderId = :userId2 AND receiverId = :userId1) " +
            "ORDER BY timestamp ASC")
    List<Message> getMessagesBetweenUsers(int userId1, int userId2);

    @Query("SELECT * FROM messages WHERE " +
            "(senderId = :userId1 AND receiverId = :userId2) OR " +
            "(senderId = :userId2 AND receiverId = :userId1) " +
            "ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesBetweenUsersLiveData(int userId1, int userId2);
}