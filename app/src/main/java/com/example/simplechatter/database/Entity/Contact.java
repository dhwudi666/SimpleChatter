package com.example.simplechatter.database.Entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "contacts",
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
        indices = {
                @Index("userId"),
                @Index("contactId"),
                @Index(value = {"userId", "contactId"}, unique = true) // 唯一索引，避免重复联系人
        })
public class Contact {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int userId;
    public int contactId;

    public String name;
    public String avatar;
    public String lastMessage;
    public long lastMessageTime;
    public int unreadCount;
    public boolean isOnline;
    public long createTime;

    public Contact(int userId, int contactId, String name) {
        this.userId = userId;
        this.contactId = contactId;
        this.name = name;
        this.avatar = "";
        this.lastMessage = "";
        this.lastMessageTime = System.currentTimeMillis();
        this.unreadCount = 0;
        this.isOnline = false;
        this.createTime = System.currentTimeMillis();
    }

    // Getter 方法
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getContactId() { return contactId; }
    public String getName() { return name; }
    public String getAvatar() { return avatar; }
    public String getLastMessage() { return lastMessage; }
    public long getLastMessageTime() { return lastMessageTime; }
    public int getUnreadCount() { return unreadCount; }
    public boolean isOnline() { return isOnline; }
    public long getCreateTime() { return createTime; }

    // Setter 方法
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setContactId(int contactId) { this.contactId = contactId; }
    public void setName(String name) { this.name = name; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setOnline(boolean online) { isOnline = online; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
}