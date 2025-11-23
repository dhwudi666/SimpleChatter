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

    // 根据邮箱查询用户（登录验证）
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    // 验证登录（邮箱和密码匹配）
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    // 检查邮箱是否已存在
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);

    // 更新用户信息
    @Update
    int updateUser(User user);

    // 更新最后登录时间
    @Query("UPDATE users SET lastLoginTime = :loginTime WHERE id = :userId")
    void updateLoginTime(int userId, long loginTime);

    // 更新密码（重置密码功能）
    @Query("UPDATE users SET password = :newPassword WHERE email = :email")
    int updatePassword(String email, String newPassword);

    // 获取所有用户（管理功能）
    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    // 删除用户
    @Delete
    int deleteUser(User user);
}