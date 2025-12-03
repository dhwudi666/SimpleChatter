package com.example.simplechatter.database.Repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private UserDao userDao;
    private ExecutorService executor;
    private Handler mainHandler;

    public UserRepository(Context context) {
        AppDataBase db = AppDataBase.getInstance(context);
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // 注册新用户
    public void register(User user, OnRegisterListener listener) {
        executor.execute(() -> {
            try {
                long result = userDao.insertUser(user);
                boolean success = result > 0;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onRegisterResult(success, result);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onRegisterResult(false, -1L);
                    }
                });
            }
        });
    }

    // 用户登录
    public void login(String email, String password, OnLoginListener listener) {
        executor.execute(() -> {
            try {
                User user = userDao.login(email, password);
                boolean success = user != null;
                mainHandler.post(() -> {
                    if (listener !=  null) {
                        listener.onLoginResult(success, user);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onLoginResult(false, null);
                    }
                });
            }
        });
    }

    // 检查邮箱是否存在
    public void checkEmailExists(String email, OnEmailCheckListener listener) {
        executor.execute(() -> {
            try {
                int count = userDao.checkEmailExists(email);
                boolean exists = count > 0;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onEmailCheckResult(exists);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onEmailCheckResult(false);
                    }
                });
            }
        });
    }

    // 更新密码 - 修复参数问题
    public void updatePassword(int userId, String newPassword, OnPasswordUpdateListener listener) {
        executor.execute(() -> {
            try {
                int result = userDao.updatePassword(userId, newPassword);
                boolean success = result > 0;
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPasswordUpdateResult(success);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPasswordUpdateResult(false);
                    }
                });
            }
        });
    }

    // 根据邮箱更新密码
    public void updatePasswordByEmail(String email, String newPassword, OnPasswordUpdateListener listener) {
        executor.execute(() -> {
            try {
                // 先获取用户ID
                User user = userDao.getUserByEmail(email);
                if (user != null) {
                    int result = userDao.updatePassword(user.getId(), newPassword);
                    boolean success = result > 0;
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onPasswordUpdateResult(success);
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        if (listener != null) {
                            listener.onPasswordUpdateResult(false);
                        }
                    });
                }
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onPasswordUpdateResult(false);
                    }
                });
            }
        });
    }

    // 获取用户信息
//    public void getUserById(int userId, OnGetUserListener listener) {
//        executor.execute(() -> {
//            try {
//                User user = userDao.getUserById(userId);
//                mainHandler.post(() -> {
//                    if (listener != null) {
//                        listener.onGetUserResult(user != null, user);
//                    }
//                });
//            } catch (Exception e) {
//                mainHandler.post(() -> {
//                    if (listener != null) {
//                        listener.onGetUserResult(false, null);
//                    }
//                });
//            }
//        });
//    }

    // 回调接口
    public interface OnRegisterListener {
        void onRegisterResult(boolean success, long userId);
    }

    public interface OnLoginListener {
        void onLoginResult(boolean success, User user);
    }

    public interface OnEmailCheckListener {
        void onEmailCheckResult(boolean exists);
    }

    public interface OnPasswordUpdateListener {
        void onPasswordUpdateResult(boolean success);
    }

//    public interface OnGetUserListener {
//        void onGetUserResult(boolean success, User user);
//    }
}