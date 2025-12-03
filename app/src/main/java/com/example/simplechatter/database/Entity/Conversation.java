package com.example.simplechatter.database.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversations",
        foreignKeys = {
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "contactId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("userId"), @Index("contactId")})
public class Conversation {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId; // 当前用户ID
    public int contactId; // 联系人ID
    public String lastMessage;
    public long lastMessageTime;
    public int unreadCount;
    public int totalMessages;
    public long updateTime;

    // 软删除标记：0表示未删除，1表示已删除
    public int isDeleted;

    public Conversation(int userId, int contactId) {
        this.userId = userId;
        this.contactId = contactId;
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.totalMessages = 0;
        this.updateTime = System.currentTimeMillis();
        this.isDeleted = 0; // 默认未删除
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }

    public int getTotalMessages() { return totalMessages; }
    public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }

    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }

    public int getIsDeleted() { return isDeleted; }
    public void setIsDeleted(int isDeleted) { this.isDeleted = isDeleted; }
}