package com.example.simplechatter.database.Entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String email;
    @NonNull
    public String password;
    public String nickname;
    public String avatar;
    public long createTime;
    public long lastLoginTime;

    // 构造方法
    public User(@NonNull String email, @NonNull String password) {
        this.email = email;
        this.password = password;
        this.nickname = "";
        this.avatar = "";
        this.createTime = System.currentTimeMillis();
        this.lastLoginTime = System.currentTimeMillis();
    }

    // Getter 方法
    public int getId() { return id; }
    @NonNull public String getEmail() { return email; }
    @NonNull public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    public long getCreateTime() { return createTime; }
    public long getLastLoginTime() { return lastLoginTime; }

    // Setter 方法
    public void setId(int id) { this.id = id; }
    public void setEmail(@NonNull String email) { this.email = email; }
    public void setPassword(@NonNull String password) { this.password = password; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }

}
