package com.example.simplechatter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.R;
import com.example.simplechatter.Adapter.ContactsAdapter;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.DAO.UserDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.database.Entity.User;
import com.example.simplechatter.util.ContactManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ContactsAdapter adapter;
    private ContactDao contactDao;
    private UserDao userDao;
    private ExecutorService executor;
    private ContactManager contactManager;
    private TextView tvTotalUnread;
    private int currentUserId;
    private List<Contact> contactList = new ArrayList<>();
    private static final String TAG = "ContactsActivity";
    private static final String PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // 初始化
        getCurrentUserId();
        initViews();
        initData();
        loadContacts("");
        setupButtons();

        Log.d(TAG, "联系人页面初始化完成，用户ID: " + currentUserId);
    }

    private void getCurrentUserId() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt("user_id", 1);
        } catch (Exception e) {
            Log.e(TAG, "获取用户ID失败: " + e.getMessage());
            currentUserId = 1;
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvContacts);
        etSearch = findViewById(R.id.etSearch);
        tvTotalUnread = findViewById(R.id.tvTotalUnread); // 在标题栏显示总未读
        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loadContacts(s.toString().trim());
            }
        });
    }

    private void initData() {
        AppDataBase db = AppDataBase.getInstance(this);
        contactDao = db.contactDao();
        userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
        contactManager = new ContactManager(this);
    }

    private void setupButtons() {
        //主页按钮
        setupButton(R.id.btnHome, "个人中心", new View.OnClickListener() {
            @Override
            public void onClick(View v) { navigateToHomeView(); }
        });
        // 个人中心按钮
        setupButton(R.id.btnProfile, "个人中心", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPersonalView();
            }
        });

        // 添加联系人按钮
        setupButton(R.id.btnAddContact, "添加联系人", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddContactDialog();
            }
        });

    }
    private void setupButton(int buttonId, String buttonName, View.OnClickListener listener) {
        View button = findViewById(buttonId);
        if (button != null) {
            button.setOnClickListener(listener);
        } else {
            Log.w(TAG, buttonName + "按钮未找到");
        }
    }

    /**
     * 显示添加联系人对话框
     */
    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加联系人");

        // 创建输入框
        final EditText input = new EditText(this);
        input.setHint("请输入邮箱");
        builder.setView(input);

        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userInput = input.getText().toString().trim();
                if (!TextUtils.isEmpty(userInput)) {
                    addContact(userInput);
                } else {
                    Toast.makeText(ContactsActivity.this, "请输入用户ID或邮箱", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", null);
        builder.show();
    }

    /**
     * 添加联系人
     */
    private void addContact(String userInput) {
        executor.execute(() -> {
            try {
                User targetUser;
                targetUser = userDao.getUserByEmail(userInput);
                if (targetUser != null) {
                    if (targetUser.getId() == currentUserId) {
                        runOnUiThread(() ->
                                Toast.makeText(this, "不能添加自己为联系人", Toast.LENGTH_SHORT).show()
                        );
                        return;
                    }
                    // 使用ContactManager添加联系人
                    contactManager.addContact(currentUserId, targetUser.getId(), new ContactManager.OnContactAddListener() {
                        @Override
                        public void onContactAddResult(boolean success, String message) {
                            runOnUiThread(() -> {
                                Toast.makeText(ContactsActivity.this, message, Toast.LENGTH_SHORT).show();
                                if (success) {
                                    loadContacts(""); // 刷新列表
                                }
                            });
                        }
                    });

                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show()
                    );
                }

            } catch (Exception e) {
                Log.e(TAG, "添加联系人失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadContacts(String keyword) {
        executor.execute(() -> {
            try {
                List<Contact> contacts = keyword.isEmpty()
                        ? contactDao.getContactsByUserId(currentUserId)
                        : contactDao.searchContacts(currentUserId, keyword);

                int totalUnread = contactDao.getTotalUnreadCount(currentUserId);
                runOnUiThread(() -> {
                    contactList.clear();
                    contactList.addAll(contacts);
                    adapter.updateData(contactList);

                    updateTotalUnreadCount(totalUnread);
                    Log.d(TAG, "加载联系人完成，数量: " + contacts.size());
                });

            } catch (Exception e) {
                Log.e(TAG, "加载联系人失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "加载联系人失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * 更新总未读消息数显示
     */
    private void updateTotalUnreadCount(int totalUnread) {
        if (tvTotalUnread != null) {
            if (totalUnread > 0) {
                tvTotalUnread.setVisibility(View.VISIBLE);
                if (totalUnread > 99) {
                    tvTotalUnread.setText("99+");
                } else {
                    tvTotalUnread.setText(String.valueOf(totalUnread));
                }
            } else {
                tvTotalUnread.setVisibility(View.GONE);
            }
        }
    }
    // 联系人点击事件 - 跳转到聊天
    @Override
    public void onContactClick(Contact contact) {
        Log.d(TAG, "打开与 " + contact.getName() + " 的聊天");

        // 清空未读消息
        executor.execute(() -> {
            try {
                contactDao.clearUnreadCount(contact.getId());
                int totalUnread = contactDao.getTotalUnreadCount(currentUserId);
                runOnUiThread(() -> {
                    loadContacts("");
                    updateTotalUnreadCount(totalUnread);

                    // 跳转到聊天界面
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("contact_id", contact.getContactId()); // 传递联系人用户ID
                    intent.putExtra("contact_name", contact.getName());
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e(TAG, "清空未读消息失败: " + e.getMessage());
                // 即使失败也允许跳转
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("contact_id", contact.getContactId());
                    intent.putExtra("contact_name", contact.getName());
                    startActivity(intent);
                });
            }
        });
    }

    // 联系人长按事件 - 显示操作菜单
    @Override
    public void onContactLongClick(Contact contact) {
        showContactActionMenu(contact);
    }
    /**
     * 显示联系人操作菜单
     */
    private void showContactActionMenu(final Contact contact) {
        String[] options = {"发送消息", "删除联系人", "查看资料", "取消"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contact.getName());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // 发送消息
                        onContactClick(contact);
                        break;
                    case 1: // 删除联系人
                        showDeleteConfirmation(contact);
                        break;
                    case 2: // 查看资料
                        showContactInfo(contact);
                        break;
                }
            }
        });
        builder.show();
    }
    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmation(final Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除联系人 " + contact.getName() + " 吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteContact(contact);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
    /**
     * 删除联系人
     */
    private void deleteContact(Contact contact) {
        executor.execute(() -> {
            try {
                int result = contactDao.deleteContact(contact.getId());
                runOnUiThread(() -> {
                    if (result > 0) {
                        Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
                        loadContacts(""); // 刷新列表
                    } else {
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "删除联系人失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    /**
     * 显示联系人信息
     */
    private void showContactInfo(Contact contact) {
        executor.execute(() -> {
            try {
                User contactUser = userDao.getUserById(contact.getContactId());
                runOnUiThread(() -> {
                    String info = "姓名: " + contact.getName() + "\n" +
                            "邮箱: " + (contactUser != null ? contactUser.getEmail() : "未知") + "\n" +
                            "状态: " + (contact.isOnline() ? "在线" : "离线") + "\n" +
                            "最后消息: " + contact.getLastMessage();

                    new AlertDialog.Builder(ContactsActivity.this)
                            .setTitle("联系人信息")
                            .setMessage(info)
                            .setPositiveButton("确定", null)
                            .show();
                });
            } catch (Exception e) {
                Log.e(TAG, "获取联系人信息失败: " + e.getMessage());
            }
        });
    }
    /**
     * 跳转到个人中心
     */
    private void navigateToPersonalView() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    /**
     * 跳转到主页
     */
    private void navigateToHomeView() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "页面恢复，刷新联系人列表");
        loadContacts("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}