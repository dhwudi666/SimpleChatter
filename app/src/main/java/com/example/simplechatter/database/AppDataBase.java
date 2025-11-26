package com.example.simplechatter.database;


import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.DAO.ConversationDao;
import com.example.simplechatter.database.DAO.MessageDao;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.database.Entity.Conversation;
import com.example.simplechatter.database.Entity.Message;
import com.example.simplechatter.database.Entity.User;

@Database(entities = {User.class, Contact.class, Conversation.class, Message.class},
        version = 5,
        exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ContactDao contactDao();
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();

    private static volatile AppDataBase INSTANCE;
    // 单例模式，确保只有一个数据库实例
    public static AppDataBase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                                    AppDataBase.class,
                            "simple_chatter.db"
                    ).fallbackToDestructiveMigration() //允许破坏性迁移
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
