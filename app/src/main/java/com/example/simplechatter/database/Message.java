package com.example.simplechatter.database;

// Message.java
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(
        tableName = "messages",
        foreignKeys = @ForeignKey(
                entity = Conversation.class,
                parentColumns = "id",
                childColumns = "conversationId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("conversationId")} // 为外键建立索引以提高查询效率
)
public class Message {
    @PrimaryKey(autoGenerate = true) // 主键自增
    private int id;
    private int conversationId; // 外键，关联 Conversation 表
    private String content;
    private String type; // 例如 "text", "emoji"
    private long timestamp;
    private boolean isSentByMe; // 标记消息是由我发送的还是接收的

    public Message(int conversationId, String content, String type, long timestamp, boolean isSentByMe) {
        this.conversationId = conversationId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
    }

    // Getter and Setter...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSentByMe() { return isSentByMe; }
    public void setSentByMe(boolean sentByMe) { isSentByMe = sentByMe; }
}