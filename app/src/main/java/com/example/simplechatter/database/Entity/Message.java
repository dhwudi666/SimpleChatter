package com.example.simplechatter.database.Entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages",
        foreignKeys = {
                @ForeignKey(entity = Conversation.class,
                        parentColumns = "id",
                        childColumns = "conversationId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class,
                        parentColumns = "id",
                        childColumns = "senderId",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {@Index("conversationId"), @Index("senderId"), @Index("timestamp")})
public class Message {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int conversationId; // 会话ID
    public int senderId; // 发送者ID
    public int receiverId; // 接收者ID

    public String content; // 消息内容
    public int messageType; // 消息类型
    public long timestamp; // 发送时间戳
    public int status; // 消息状态: 0=发送中, 1=已发送, 2=已送达, 3=已读

    // 消息类型常量
    public static final int TYPE_TEXT = 0;
    // 消息状态常量
    public static final int STATUS_SENDING = 0;
    public static final int STATUS_SENT = 1;
    public static final int STATUS_DELIVERED = 2;
    public static final int STATUS_READ = 3;

    public Message(int conversationId, int senderId, int receiverId, String content, int messageType) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.status = STATUS_SENT;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }

    public void setContent(String content) { this.content = content; }

    public int getMessageType() { return messageType; }
    public void setMessageType(int messageType) { this.messageType = messageType; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}