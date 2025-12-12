package com.example.simplechatter.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.R;
import com.example.simplechatter.Adapter.MessageAdapter;
import com.example.simplechatter.database.AppDataBase;
import com.example.simplechatter.database.DAO.ContactDao;
import com.example.simplechatter.database.DAO.ConversationDao;
import com.example.simplechatter.database.DAO.MessageDao;
import com.example.simplechatter.database.Entity.Contact;
import com.example.simplechatter.database.Entity.Conversation;
import com.example.simplechatter.database.Entity.Message;
import com.example.simplechatter.util.UserSessionManager;
import com.example.simplechatter.util.ContactManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnBack, btnMore;
    private TextView tvChatTitle;

    private MessageAdapter messageAdapter;
    private MessageDao messageDao;
    private ConversationDao conversationDao;
    private ContactDao contactDao;
    private ExecutorService executor;
    private UserSessionManager sessionManager;
    private ContactManager contactManager;

    private int contactId;
    private int conversationId = -1;
    private String contactName;
    private List<Message> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new UserSessionManager(this);
        contactManager = new ContactManager(this);

        if (!checkLoginStatus() || !getIntentData()) {
            return;
        }

        initViews();
        initData();
        setupListeners();
        observeMessages();
        loadMessages();
        clearUnreadCount();
    }

    private boolean checkLoginStatus() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private boolean getIntentData() {
        contactId = getIntent().getIntExtra("contact_id", -1);
        contactName = getIntent().getStringExtra("contact_name");

        if (contactId == -1) {
            Toast.makeText(this, "联系人信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        int currentUserId = sessionManager.getCurrentUserId();
        if (contactId == currentUserId) {
            Toast.makeText(this, "不能与自己聊天", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        return true;
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        tvChatTitle = findViewById(R.id.tvChatTitle);

        tvChatTitle.setText(contactName != null ? contactName : "聊天对象");

        int currentUserId = sessionManager.getCurrentUserId();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
    }

    private void initData() {
        AppDataBase db = AppDataBase.getInstance(this);
        messageDao = db.messageDao();
        conversationDao = db.conversationDao();
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();
        getOrCreateConversation();
    }

    private void getOrCreateConversation() {
        int currentUserId = sessionManager.getCurrentUserId();
        executor.execute(() -> {
            try {
                // 先尝试获取未删除的会话
                Conversation conversation = conversationDao.getActiveConversation(currentUserId, contactId);

                if (conversation == null) {
                    // 检查是否存在已删除的会话，如果存在则恢复
                    Conversation deletedConversation = conversationDao.getConversation(currentUserId, contactId);
                    if (deletedConversation != null && deletedConversation.getIsDeleted() == 1) {
                        // 恢复已删除的会话
                        conversationDao.restoreConversation(currentUserId, contactId);
                        conversationId = deletedConversation.getId();
                        Log.d(TAG, "恢复已删除的会话，ID: " + conversationId);
                    } else {
                        // 创建新会话
                        conversation = new Conversation(currentUserId, contactId);
                        conversation.setIsDeleted(0);
                        long newConversationId = conversationDao.insertConversation(conversation);
                        conversationId = (int) newConversationId;
                        Log.d(TAG, "创建新会话，ID: " + conversationId);
                    }
                } else {
                    conversationId = conversation.getId();
                    Log.d(TAG, "找到现有会话，ID: " + conversationId);
                }
            } catch (Exception e) {
                Log.e(TAG, "获取或创建会话失败: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "会话初始化失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSend.setOnClickListener(v -> sendMessage());

        btnMore.setOnClickListener(v ->
                Toast.makeText(this, "更多功能", Toast.LENGTH_SHORT).show());

        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                btnSend.setVisibility(TextUtils.isEmpty(s.toString()) ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void sendMessage() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "用户未登录，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "消息内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        int currentUserId = sessionManager.getCurrentUserId();

        // 检查双向好友关系
        executor.execute(() -> {
            boolean isMutual = contactManager.isMutualFriend(currentUserId, contactId);
            runOnUiThread(() -> {
                if (!isMutual) {
                    Toast.makeText(this, "你还不是对方的好友，无法发送消息", Toast.LENGTH_LONG).show();
                    return;
                }

                // 双向好友，可以发送消息
                performSendMessage(content, currentUserId);
            });
        });
    }

    private void performSendMessage(String content, int currentUserId) {
        btnSend.setEnabled(false);

        executor.execute(() -> {
            try {
                // 确保会话ID有效
                if (conversationId == -1) {
                    Conversation conversation = conversationDao.getActiveConversation(currentUserId, contactId);
                    if (conversation != null) {
                        conversationId = conversation.getId();
                    } else {
                        throw new Exception("会话不存在");
                    }
                }

                Message message = new Message(conversationId, currentUserId, contactId, content, Message.TYPE_TEXT);
                message.setTimestamp(System.currentTimeMillis());
                message.setStatus(Message.STATUS_SENT);

                long messageId = messageDao.insertMessageAndUpdateUnread(message);
                if (messageId > 0) {
                    message.setId((int) messageId);
                    runOnUiThread(() -> {
                        // 去重检查：如果列表最后一条已经是同一条（id 或 时间+内容+发送者相同），就不重复添加
                        boolean shouldAdd = true;
                        if (!messageList.isEmpty()) {
                            Message last = messageList.get(messageList.size() - 1);
                            if (last.getId() == message.getId()
                                    || (last.getTimestamp() == message.getTimestamp()
                                    && last.getSenderId() == message.getSenderId()
                                    && last.getContent() != null
                                    && last.getContent().equals(message.getContent()))) {
                                shouldAdd = false;
                            }
                        }

                        if (shouldAdd) {
                            messageList.add(message);
                            messageAdapter.notifyItemInserted(messageList.size() - 1);
                            scrollToBottom();
                        } else {
                            // 确保界面状态正确，选择刷新或更新最后一项
                            messageAdapter.notifyDataSetChanged();
                        }

                        etMessage.setText("");
                        btnSend.setEnabled(true);
                    });
                } else {
                    throw new Exception("插入消息失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "发送消息失败: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadMessages() {
        int currentUserId = sessionManager.getCurrentUserId();
        executor.execute(() -> {
            try {
                // 加载所有消息（包括删除联系人之前的消息）
                List<Message> messages = messageDao.getMessagesBetweenUsers(currentUserId, contactId);
                runOnUiThread(() -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    messageAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });
            } catch (Exception e) {
                Log.e(TAG, "加载消息失败: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "加载消息失败", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void observeMessages() {
        int currentUserId = sessionManager.getCurrentUserId();
        messageDao.getMessagesBetweenUsersLiveData(currentUserId, contactId)
                .observe(this, messages -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    messageAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });
    }

    private void clearUnreadCount() {
        executor.execute(() -> {
            try {
                if (conversationId != -1) {
                    conversationDao.clearUnreadCount(conversationId);
                    // 只清除未删除的联系人的未读数
                    Contact contact = contactDao.getActiveContact(sessionManager.getCurrentUserId(), contactId);
                    if (contact != null) {
                        contactDao.clearUnreadCount(contact.getId());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "清空未读消息数失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}