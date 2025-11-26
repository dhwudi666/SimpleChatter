package com.example.simplechatter.database.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.simplechatter.database.Entity.User;

import java.util.List;

@Dao
public interface UserDao {
    // 插入新用户（注册功能）
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);
    // 更新用户信息
    @Update
    int updateUser(User user);
    // 删除用户
    @Delete
    int deleteUser(User user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // 验证登录（邮箱和密码匹配）
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // 检查邮箱是否已存在
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(int userId);

    // 更新密码（重置密码功能）
    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    int updatePassword(int userId, String newPassword);
}