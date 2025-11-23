//package com.example.simplechatter.activity;
//
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.widget.EditText;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.simplechatter.R;
//import com.example.simplechatter.Adapter.ContactsAdapter;
//import com.example.simplechatter.database.AppDataBase;
//import com.example.simplechatter.database.DAO.ContactDao;
//import com.example.simplechatter.database.Entity.Contact;
//
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class ContactsActivity extends AppCompatActivity implements ContactsAdapter.OnContactClickListener {
//    private RecyclerView recyclerView;
//    private EditText etSearch;
//    private ContactsAdapter adapter;
//    private ContactDao contactDao;
//    private ExecutorService executor;
//    private int currentUserId = 1; // 当前登录用户ID
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_contacts);
//
//        initViews();
//        initData();
//        loadContacts("");
//    }
//
//    private void initViews() {
//        recyclerView = findViewById(R.id.rvContacts);
//        etSearch = findViewById(R.id.etSearch);
//
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new ContactsAdapter(null, this);
//        recyclerView.setAdapter(adapter);
//
//        // 搜索功能
//        etSearch.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                loadContacts(s.toString());
//            }
//        });
//    }
//
//    private void initData() {
//        AppDataBase db = AppDataBase.getInstance(this);
//        contactDao = db.contactDao();
//        executor = Executors.newSingleThreadExecutor();
//    }
//
//    private void loadContacts(String keyword) {
//        executor.execute(() -> {
//            List<Contact> contacts;
//            if (keyword.isEmpty()) {
//                contacts = contactDao.getContactsByUserId(currentUserId);
//            } else {
//                contacts = contactDao.searchContacts(currentUserId, keyword);
//            }
//
//            runOnUiThread(() -> adapter.updateData(contacts));
//        });
//    }
//
//    @Override
//    public void onContactClick(Contact contact) {
//        // 跳转到聊天界面
//        ChatActivity.start(this, contact.getId(), contact.getName());
//
//        // 清空未读消息数
//        executor.execute(() -> {
//            contactDao.clearUnreadCount(contact.getId());
//        });
//    }
//
//    @Override
//    public void onContactLongClick(Contact contact) {
//        // 长按菜单：删除、置顶等
//        showContactMenu(contact);
//    }
//
//    private void showContactMenu(Contact contact) {
//        // 实现长按菜单
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (executor != null) {
//            executor.shutdown();
//        }
//    }
//}