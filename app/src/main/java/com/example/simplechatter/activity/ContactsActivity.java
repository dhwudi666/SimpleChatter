package com.example.simplechatter.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.simplechatter.ui.PersonalView;
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

    private int currentUserId = 1; // 当前登录用户ID
    private List<Contact> contactList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        initViews();
        initData();
        loadContacts(""); // 初始加载所有联系人
        ReturnProfile();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.rvContacts);
        etSearch = findViewById(R.id.etSearch);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactsAdapter(contactList, this);
        recyclerView.setAdapter(adapter);

        // 设置搜索功能
        setupSearch();
    }

    private void initData() {
        AppDataBase db = AppDataBase.getInstance(this);
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();

        // 初始化示例数据（如果数据库为空）
        initializeSampleData();
    }

    private void ReturnProfile(){
        findViewById(R.id.btnProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ContactsActivity.this, PersonalView.class);
                startActivity(intent);
            }
        });
    }

    private void initializeSampleData() {
        executor.execute(() -> {
            List<Contact> existingContacts = contactDao.getContactsByUserId(currentUserId);
            if (existingContacts.isEmpty()) {
                // 插入示例数据
                ContactDataInitializer initializer = new ContactDataInitializer(this);
                initializer.initializeSampleData(currentUserId);

                runOnUiThread(() ->
                        Toast.makeText(this, "已初始化示例联系人数据", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loadContacts(s.toString());
            }
        });
    }

    private void loadContacts(String keyword) {
        executor.execute(() -> {
            List<Contact> contacts;
            if (keyword.isEmpty()) {
                // 加载所有联系人，按最后消息时间排序
                contacts = contactDao.getContactsByUserId(currentUserId);
            } else {
                // 搜索联系人
                contacts = contactDao.searchContacts(currentUserId, keyword);
            }

            runOnUiThread(() -> {
                contactList.clear();
                contactList.addAll(contacts);
                adapter.updateData(contactList);

                // 显示联系人数量
                if (keyword.isEmpty()) {
                    Toast.makeText(ContactsActivity.this,
                            "共 " + contacts.size() + " 个联系人", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ContactsActivity.this,
                            "找到 " + contacts.size() + " 个相关联系人", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // 联系人点击事件
    @Override
    public void onContactClick(Contact contact) {
        // 跳转到聊天界面
        Toast.makeText(this, "打开与 " + contact.getName() + " 的聊天", Toast.LENGTH_SHORT).show();

        // 清空该联系人的未读消息数
        executor.execute(() -> {
            contactDao.clearUnreadCount(contact.getId());

            // 刷新列表显示
            runOnUiThread(() -> loadContacts(""));
        });

         Intent intent = new Intent(this, ChatActivity.class);
         intent.putExtra("contact_id", contact.getId());
         intent.putExtra("contact_name", contact.getName());
         startActivity(intent);
    }

    // 联系人长按事件
    @Override
    public void onContactLongClick(Contact contact) {
        // 显示联系人操作菜单
        showContactMenu(contact);
    }

    private void showContactMenu(Contact contact) {
        // 实现长按菜单（删除、置顶、免打扰等）
        Toast.makeText(this, "长按了联系人: " + contact.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}