package com.example.simplechatter.database;
// Conversation.java
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;

@Entity(
        tableName = "conversations",
        foreignKeys = @ForeignKey(
                entity = Contact.class,
                parentColumns = "id",
                childColumns = "contactId",
                onDelete = ForeignKey.CASCADE // 当联系人被删除时，同步删除会话
        )
)
public class Conversation {
    @PrimaryKey
    private int id;
    private int contactId; // 外键，关联 Contact 表
    private String lastMessage;
    private long timestamp; // 最后一条消息的时间戳
    private int unreadCount;

    public Conversation(int id, int contactId, String lastMessage, long timestamp, int unreadCount) {
        this.id = id;
        this.contactId = contactId;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.unreadCount = unreadCount;
    }

    // Getter and Setter...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContactId() { return contactId; }
    public void setContactId(int contactId) { this.contactId = contactId; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}