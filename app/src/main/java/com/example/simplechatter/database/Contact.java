package com.example.simplechatter.database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "contacts")
public class Contact {
    @PrimaryKey
    private int id;
    private String name;
    private String avatar; // 存储头像的 URI 或 URL

    // 构造方法
    public Contact(int id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    // Getter 和 Setter 方法 (Room 依赖它们)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}