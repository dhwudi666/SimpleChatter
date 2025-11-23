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
                @ForeignKey(entity = Contact.class,
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
    public boolean isPinned;
    public boolean isMuted;
    public long updateTime;

    public Conversation(int userId, int contactId) {
        this.userId = userId;
        this.contactId = contactId;
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.totalMessages = 0;
        this.isPinned = false;
        this.isMuted = false;
        this.updateTime = System.currentTimeMillis();
    }

    // Getter and Setter methods...
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

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public boolean isMuted() { return isMuted; }
    public void setMuted(boolean muted) { isMuted = muted; }

    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
}