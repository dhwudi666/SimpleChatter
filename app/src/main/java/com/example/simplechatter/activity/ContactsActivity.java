package com.example.simplechatter.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.R;
import com.example.simplechatter.Adapter.ContactsAdapter;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.util.ContactDataInitializer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {
    private RecyclerView recyclerView;
    private EditText etSearch;
    private ContactsAdapter adapter;
    private ContactDao contactDao;
    private ExecutorService executor;

    private int currentUserId; // 从登录状态获取真实用户ID
    private List<Contact> contactList = new ArrayList<>();

    private static final String TAG = "ContactsActivity";
    private static final String PREFS_NAME = "user_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // 1. 获取当前登录用户ID
        getCurrentUserId();
        // 2. 初始化视图
        initViews();
        // 3. 初始化数据
        initData();
        // 4. 加载当前用户的联系人
        loadContacts("");
        // 5. 设置个人中心按钮
        setupProfileButton();
    }

    /**
     * 从SharedPreferences获取当前登录用户ID
     */
    private void getCurrentUserId() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentUserId = prefs.getInt("user_id", -1); // 默认值-1表示未登录

            if (currentUserId == -1) {
                Log.w(TAG, "未找到登录用户，使用默认用户ID: 1");
                currentUserId = 1; // 回退到默认值
            }
            Log.d(TAG, "当前登录用户ID: " + currentUserId);

        } catch (Exception e) {
            Log.e(TAG, "获取用户ID失败: " + e.getMessage());
            currentUserId = 1; // 出错时使用默认值
            Toast.makeText(this, "用户信息加载失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvContacts);
        etSearch = findViewById(R.id.etSearch);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        // 设置搜索功能
//        setupSearch();

        Log.d(TAG, "视图初始化完成");
    }

    private void initData() {
        AppDataBase db = AppDataBase.getInstance(this);
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();
        // 初始化示例数据（如果数据库空）
        initializeSampleData();
    }

    private void setupProfileButton() {
        View btnProfile = findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "跳转到个人中心");
                    Intent intent = new Intent(ContactsActivity.this, PersonalView.class);
                    startActivity(intent);
                }
            });
        } else {
            Log.w(TAG, "个人中心按钮未找到");
        }
    }

    private void initializeSampleData() {
        executor.execute(() -> {
            try {
                List<Contact> existingContacts = contactDao.getContactsByUserId(currentUserId);
                Log.d(TAG, "用户 " + currentUserId + " 现有联系人数量: " + existingContacts.size());

                if (existingContacts.isEmpty()) {
                    Log.d(TAG, "为用户 " + currentUserId + " 初始化示例数据");

                    // 插入示例数据
                    ContactDataInitializer initializer = new ContactDataInitializer(this);
                    initializer.initializeSampleData(currentUserId);

                } else {
                    Log.d(TAG, "用户 " + currentUserId + " 已有联系人数据，跳过初始化");
                }
            } catch (Exception e) {
                Log.e(TAG, "初始化示例数据失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "数据初始化失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

//    private void setupSearch() {
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//            @Override
//            public void afterTextChanged(Editable s) {
//                loadContacts(s.toString());
//            }
//        });
//    }

    private void loadContacts(String keyword) {
        executor.execute(() -> {
            try {
                List<Contact> contacts;
                if (keyword.isEmpty()) {
                    // 加载当前用户的所有联系人，按最后消息时间排序
                    contacts = contactDao.getContactsByUserId(currentUserId);
                    Log.d(TAG, "加载用户 " + currentUserId + " 的所有联系人，数量: " + contacts.size());
                } else {
                    // 搜索当前用户的联系人
                    contacts = contactDao.searchContacts(currentUserId, keyword);
                    Log.d(TAG, "搜索用户 " + currentUserId + " 的联系人，关键词: " + keyword + "，结果: " + contacts.size());
                }

                runOnUiThread(() -> {
                    contactList.clear();
                    contactList.addAll(contacts);
                    adapter.updateData(contactList);
                });

            } catch (Exception e) {
                Log.e(TAG, "加载联系人失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(ContactsActivity.this, "加载联系人失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    // 联系人点击事件
    @Override
    public void onContactClick(Contact contact) {
        Log.d(TAG, "点击联系人: " + contact.getName() + " (ID: " + contact.getId() + ")");

        // 跳转到聊天界面
        Toast.makeText(this, "打开与 " + contact.getName() + " 的聊天", Toast.LENGTH_SHORT).show();

        // 清空该联系人的未读消息数
        executor.execute(() -> {
            try {
                contactDao.clearUnreadCount(contact.getId());
                Log.d(TAG, "已清空联系人 " + contact.getName() + " 的未读消息");

                // 刷新列表显示
                runOnUiThread(() -> loadContacts(""));

            } catch (Exception e) {
                Log.e(TAG, "清空未读消息失败: " + e.getMessage());
            }
        });

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("contact_id", contact.getId());
        intent.putExtra("contact_name", contact.getName());
        startActivity(intent);
    }

    // 联系人长按事件
    @Override
    public void onContactLongClick(Contact contact) {
        Log.d(TAG, "长按联系人: " + contact.getName());
        // 显示联系人操作菜单
        showContactMenu(contact);
    }

    private void showContactMenu(Contact contact) {
        // 实现长按菜单（删除、置顶、免打扰等）
        Toast.makeText(this, "长按了联系人: " + contact.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "联系人页面恢复，刷新数据");
        // 页面恢复时刷新数据
        loadContacts("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "联系人页面销毁");
        if (executor != null) {
            executor.shutdown();
        }
    }
}