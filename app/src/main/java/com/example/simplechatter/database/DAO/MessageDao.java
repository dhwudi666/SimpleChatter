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

    // ✅ 修复：添加事务处理，确保消息插入和未读更新原子性
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
    // ✅ 新增：更新接收者联系人的未读消息
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
//    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
//    List<Message> getMessagesByConversation(int conversationId);
//    @Query("SELECT * FROM messages WHERE senderId = :senderId")
//    List<Message> getMessagesBySender(int senderId);
//    @Query("SELECT * FROM messages WHERE receiverId = :receiverId")
//    List<Message> getMessagesByReceiver(int receiverId);

    @Query("SELECT * FROM messages WHERE id = :messageId LIMIT 1")
    Message getMessageById(int messageId);

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    int updateMessageStatus(int messageId, int status);

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND status < 3")
    int getUnreadMessageCount(int conversationId);

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



    // 更新联系人的未读消息数和最后消息
    @Query("UPDATE contacts SET " +
            "lastMessage = :lastMessage, " +
            "lastMessageTime = :timestamp, " +
            "unreadCount = unreadCount + 1 " +
            "WHERE userId = :receiverId AND contactId = :senderId")
    void updateContactUnreadCount(int receiverId, int senderId, String lastMessage, long timestamp);

    // 简化版本
    @Query("UPDATE contacts SET unreadCount = unreadCount + 1 " +
            "WHERE userId = :receiverId AND contactId = :senderId")
    void updateContactUnreadCount(int receiverId, int senderId);
}