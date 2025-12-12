package com.example.simplechatter.activity;

import android.content.DialogInterface;
import android.content.Intent;
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
import com.example.simplechatter.util.InputValidator;
import com.example.simplechatter.util.NavigationHelper;
import com.example.simplechatter.util.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {
    private static final String TAG = "ContactsActivity";

    private RecyclerView recyclerView;
    private EditText etSearch;
    private ContactsAdapter adapter;
    private ContactDao contactDao;
    private UserDao userDao;
    private ExecutorService executor;
    private ContactManager contactManager;
    private TextView tvTotalUnread;
    private UserSessionManager sessionManager;
    private List<Contact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        sessionManager = new UserSessionManager(this);
        initViews();
        initData();
        loadContacts("");
        setupButtons();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvContacts);
        etSearch = findViewById(R.id.etSearch);
        tvTotalUnread = findViewById(R.id.tvTotalUnread);

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
        findViewById(R.id.btnHome).setOnClickListener(v -> NavigationHelper.navigateToHome(this));
        findViewById(R.id.btnProfile).setOnClickListener(v -> NavigationHelper.navigateToProfile(this));
        findViewById(R.id.btnAddContact).setOnClickListener(v -> showAddContactDialog());
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加联系人");
        final EditText input = new EditText(this);
        input.setHint("请输入邮箱");
        builder.setView(input);

        builder.setPositiveButton("添加", (dialog, which) -> {
            String userInput = input.getText().toString().trim();
            if (!TextUtils.isEmpty(userInput)) {
                addContact(userInput);
            } else {
                Toast.makeText(this, "请输入用户邮箱", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addContact(String email) {
        if (!InputValidator.isValidEmail(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                User targetUser = userDao.getUserByEmail(email);
                if (targetUser == null) {
                    runOnUiThread(() -> Toast.makeText(this, "用户不存在", Toast.LENGTH_SHORT).show());
                    return;
                }

                int currentUserId = sessionManager.getCurrentUserId();
                if (targetUser.getId() == currentUserId) {
                    runOnUiThread(() -> Toast.makeText(this, "不能添加自己为联系人", Toast.LENGTH_SHORT).show());
                    return;
                }

                // 使用新的ContactManager添加联系人
                contactManager.addContact(currentUserId, targetUser.getId(), (success, message, isMutualFriend) -> {
                    runOnUiThread(() -> {
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            loadContacts("");
                        }
                    });
                });
            } catch (Exception e) {
                Log.e(TAG, "添加联系人失败: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "添加失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadContacts(String keyword) {
        executor.execute(() -> {
            try {
                int currentUserId = sessionManager.getCurrentUserId();
                List<Contact> contacts = keyword.isEmpty()
                        ? contactDao.getContactsByUserId(currentUserId)
                        : contactDao.searchContacts(currentUserId, keyword);

                int totalUnread = contactDao.getTotalUnreadCount(currentUserId);
                runOnUiThread(() -> {
                    contactList.clear();
                    contactList.addAll(contacts);
                    adapter.updateData(contactList);
                    updateTotalUnreadCount(totalUnread);
                });
            } catch (Exception e) {
                Log.e(TAG, "加载联系人失败: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "加载联系人失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateTotalUnreadCount(int totalUnread) {
        if (tvTotalUnread != null) {
            if (totalUnread > 0) {
                tvTotalUnread.setVisibility(View.VISIBLE);
                tvTotalUnread.setText(totalUnread > 99 ? "99+" : String.valueOf(totalUnread));
            } else {
                tvTotalUnread.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onContactClick(Contact contact) {
        executor.execute(() -> {
            try {
                contactDao.clearUnreadCount(contact.getId());
                int totalUnread = contactDao.getTotalUnreadCount(sessionManager.getCurrentUserId());
                runOnUiThread(() -> {
                    loadContacts("");
                    updateTotalUnreadCount(totalUnread);
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("contact_id", contact.getContactId());
                    intent.putExtra("contact_name", contact.getName());
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e(TAG, "清空未读消息失败: " + e.getMessage());
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("contact_id", contact.getContactId());
                    intent.putExtra("contact_name", contact.getName());
                    startActivity(intent);
                });
            }
        });
    }

    @Override
    public void onContactLongClick(Contact contact) {
        showContactActionMenu(contact);
    }

    private void showContactActionMenu(Contact contact) {
        String[] options = {"发送消息", "删除联系人", "查看资料", "取消"};
        new AlertDialog.Builder(this)
                .setTitle(contact.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            onContactClick(contact);
                            break;
                        case 1:
                            showDeleteConfirmation(contact);
                            break;
                        case 2:
                            showContactInfo(contact);
                            break;
                    }
                })
                .show();
    }

    private void showDeleteConfirmation(Contact contact) {
        new AlertDialog.Builder(this)
                .setTitle("删除联系人")
                .setMessage("确定要删除联系人 " + contact.getName() + " 吗？\n删除后聊天记录将保留，重新添加后可继续查看。")
                .setPositiveButton("删除", (dialog, which) -> deleteContact(contact))
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteContact(Contact contact) {
        int currentUserId = sessionManager.getCurrentUserId();
        int contactId = contact.getContactId();

        // 使用ContactManager软删除联系人（保留聊天记录）
        contactManager.deleteContact(currentUserId, contactId, (success, message) -> {
            runOnUiThread(() -> {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                if (success) {
                    loadContacts("");
                }
            });
        });
    }

    private void showContactInfo(Contact contact) {
        executor.execute(() -> {
            try {
                User contactUser = userDao.getUserById(contact.getContactId());
                int currentUserId = sessionManager.getCurrentUserId();
                boolean isMutual = contactManager.isMutualFriend(currentUserId, contact.getContactId());

                runOnUiThread(() -> {
                    String info = "姓名: " + contact.getName() + "\n" +
                            "邮箱: " + (contactUser != null ? contactUser.getEmail() : "未知") + "\n" +
                            "好友关系: " + (isMutual ? "双向好友" : "单向好友（你还不是对方的好友）") + "\n" +
                            "状态: " + (contact.isOnline() ? "在线" : "离线") + "\n" +
                            "最后消息: " + contact.getLastMessage();

                    new AlertDialog.Builder(this)
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

    @Override
    protected void onResume() {
        super.onResume();
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