package com.example.simplechatter.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnBack, btnMore, btnEmoji, btnVoice;
    private TextView tvChatTitle, tvOnlineStatus;

    private MessageAdapter messageAdapter;
    private MessageDao messageDao;
    private ConversationDao conversationDao;
    private ContactDao contactDao;
    private ExecutorService executor;

    private int contactId; // 联系人ID
    private int conversationId = -1; // 会话ID，初始化为-1
    private String contactName;

    private List<Message> messageList = new ArrayList<>();
    private boolean isKeyboardMode = true; // 当前是否为键盘模式

    // SharedPreferences 常量
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d("ChatActivity", "=== 启动ChatActivity ===");

        // 首先检查登录状态
        if (!checkLoginStatus()) {
            return; // 如果未登录，直接返回
        }

        // 获取传递的参数
        if (!getIntentData()) {
            return; // 参数无效，直接返回
        }

        // 初始化视图
        initViews();
        // 初始化数据
        initData();
        // 设置事件监听
        setupListeners();
        // 添加消息监听
        observeMessages();
        // 加载消息
        loadMessages();
        // 清空未读消息
        clearUnreadCount();
    }

    /**
     * 检查用户登录状态
     */
    private boolean checkLoginStatus() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        int currentUserId = prefs.getInt(KEY_USER_ID, -1);

        if (!isLoggedIn || currentUserId == -1) {
            Log.e("ChatActivity", "用户未登录，无法进入聊天界面");
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        Log.d("ChatActivity", "用户已登录，用户ID: " + currentUserId);
        return true;
    }

    /**
     * 获取当前登录用户的ID
     */
    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * 检查是否已登录
     */
    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getCurrentUserId() != -1;
    }

    private boolean getIntentData() {
        contactId = getIntent().getIntExtra("contact_id", -1);
        contactName = getIntent().getStringExtra("contact_name");

        if (contactId == -1) {
            Toast.makeText(this, "联系人信息错误", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "联系人ID无效，关闭Activity");
            finish();
            return false;
        }

        // 检查是否尝试与自己聊天
        int currentUserId = getCurrentUserId();
        if (contactId == currentUserId) {
            Toast.makeText(this, "不能与自己聊天", Toast.LENGTH_SHORT).show();
            Log.w("ChatActivity", "尝试与自己聊天，当前用户ID: " + currentUserId + ", 联系人ID: " + contactId);
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
        btnEmoji = findViewById(R.id.btnEmoji);
        btnVoice = findViewById(R.id.btnVoice);
        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);

        // 设置标题
        tvChatTitle.setText(contactName != null ? contactName : "聊天对象");

        // 设置消息列表 - 动态传入当前用户ID
        int currentUserId = getCurrentUserId();
        messageAdapter = new MessageAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        Log.d("ChatActivity", "初始化视图完成，当前用户ID: " + currentUserId);
    }

    private void initData() {
        AppDataBase db = AppDataBase.getInstance(this);
        messageDao = db.messageDao();
        conversationDao = db.conversationDao();
        contactDao = db.contactDao();
        executor = Executors.newSingleThreadExecutor();

        // 获取或创建会话
        getOrCreateConversation();
    }

    private void getOrCreateConversation() {
        int currentUserId = getCurrentUserId();
        Log.d("ChatActivity", "开始获取或创建会话，当前用户ID: " + currentUserId + ", 联系人ID: " + contactId);

        executor.execute(() -> {
            try {
                Conversation conversation = conversationDao.getConversation(currentUserId, contactId);
                if (conversation == null) {
                    Log.d("ChatActivity", "创建新会话");
                    // 创建新会话
                    conversation = new Conversation(currentUserId, contactId);
                    long newConversationId = conversationDao.insertConversation(conversation);
                    conversationId = (int) newConversationId;
                    conversation.setId(conversationId);

                    Log.d("ChatActivity", "新会话创建成功，ID: " + conversationId + ", 用户ID: " + currentUserId);

                } else {
                    conversationId = conversation.getId();
                    Log.d("ChatActivity", "找到现有会话，ID: " + conversationId + ", 用户ID: " + currentUserId);
                }

            } catch (Exception e) {
                Log.e("ChatActivity", "获取或创建会话失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "会话初始化失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> {
            Log.d("ChatActivity", "返回按钮点击，当前用户ID: " + getCurrentUserId());
            finish();
        });

        // 发送按钮
        btnSend.setOnClickListener(v -> {
            Log.d("ChatActivity", "发送消息点击，用户ID: " + getCurrentUserId());
            sendMessage();
        });

        // 更多按钮
        btnMore.setOnClickListener(v -> showMoreMenu());

        // 表情按钮
        btnEmoji.setOnClickListener(v -> toggleEmojiPanel());

        // 输入框文本变化监听
        etMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                boolean hasText = !TextUtils.isEmpty(s.toString());
                btnSend.setVisibility(hasText ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupKeyboardFunction() {
        // 设置输入框获取焦点时自动显示键盘
        etMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showKeyboard();
                }
            }
        });

        // 语音/键盘切换按钮点击事件
        if (btnVoice != null) {
            btnVoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleInputMode();
                }
            });
        }

        // 点击输入框区域时显示键盘
        etMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard();
            }
        });

        // 点击聊天区域时隐藏键盘（可选）
        rvMessages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    hideKeyboard();
                    etMessage.clearFocus();
                }
                return false;
            }
        });

        // 进入界面时自动显示键盘
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard();
            }
        }, 300);
    }

    /**
     * 显示软键盘
     */
    private void showKeyboard() {
        Log.d("ChatActivity", "显示键盘，用户ID: " + getCurrentUserId());
        etMessage.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT);
        }
        // 更新按钮状态为键盘模式
        setKeyboardMode(true);
    }

    /**
     * 隐藏软键盘
     */
    private void hideKeyboard() {
        Log.d("ChatActivity", "隐藏键盘，用户ID: " + getCurrentUserId());
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
        etMessage.clearFocus();
    }

    /**
     * 切换输入模式（键盘/语音）
     */
    private void toggleInputMode() {
        if (isKeyboardMode) {
            // 切换到语音模式
            switchToVoiceMode();
        } else {
            // 切换回键盘模式
            switchToKeyboardMode();
        }
    }

    /**
     * 切换到键盘模式
     */
    private void switchToKeyboardMode() {
        Log.d("ChatActivity", "切换到键盘模式，用户ID: " + getCurrentUserId());
        isKeyboardMode = true;

        // 更新按钮图标为键盘
        if (btnVoice != null) {
            btnVoice.setImageResource(R.drawable.keyboard_24px);
            btnVoice.setContentDescription("切换到语音输入");
        }

        // 显示输入框
        etMessage.setVisibility(View.VISIBLE);
        // 显示键盘
        showKeyboard();
    }

    /**
     * 切换到语音模式
     */
    private void switchToVoiceMode() {
        Log.d("ChatActivity", "切换到语音模式，用户ID: " + getCurrentUserId());
        isKeyboardMode = false;

        // 更新按钮图标为麦克风
        if (btnVoice != null) {
            btnVoice.setImageResource(R.drawable.mic_24px);
            btnVoice.setContentDescription("切换到键盘输入");
        }

        // 隐藏键盘
        hideKeyboard();
        Toast.makeText(this, "语音模式", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置键盘模式状态
     */
    private void setKeyboardMode(boolean keyboardMode) {
        isKeyboardMode = keyboardMode;
        if (btnVoice != null) {
            if (keyboardMode) {
                btnVoice.setImageResource(R.drawable.keyboard_24px);
            } else {
                btnVoice.setImageResource(R.drawable.mic_24px);
            }
        }
    }

    private void sendMessage() {
        int currentUserId = getCurrentUserId();

        // 检查登录状态
        if (!isUserLoggedIn()) {
            Toast.makeText(this, "用户未登录，请重新登录", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "发送消息失败：用户未登录");
            finish();
            return;
        }

        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "消息内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 禁用发送按钮防止重复发送
        btnSend.setEnabled(false);

        executor.execute(() -> {
            try {
                // 创建消息对象
                Message message = new Message(conversationId, currentUserId, contactId, content, Message.TYPE_TEXT);
                message.setTimestamp(System.currentTimeMillis());
                message.setStatus(Message.STATUS_SENT);

                // ✅ 修复：使用事务方法插入消息并更新未读计数
                long messageId = messageDao.insertMessageAndUpdateUnread(message);

                if (messageId > 0) {
                    message.setId((int) messageId);

                    runOnUiThread(() -> {
                        // 添加到消息列表
                        messageList.add(message);
                        messageAdapter.notifyItemInserted(messageList.size() - 1);
                        // 清空输入框
                        etMessage.setText("");
                        // 滚动到底部
                        scrollToBottom();
                        // 重新启用发送按钮
                        btnSend.setEnabled(true);

                        Log.d("ChatActivity", "消息发送成功，用户ID: " + currentUserId +
                                ", 消息ID: " + messageId + ", 内容: " + content);
                        Toast.makeText(ChatActivity.this, "消息已发送", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    throw new Exception("插入消息失败");
                }

            } catch (Exception e) {
                Log.e("ChatActivity", "发送消息失败，用户ID: " + currentUserId + ", 错误: " + e.getMessage());
                runOnUiThread(() -> {
                    btnSend.setEnabled(true);
                    Toast.makeText(ChatActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadMessages() {
        int currentUserId = getCurrentUserId();
        Log.d("ChatActivity", "开始加载消息，当前用户ID: " + currentUserId + ", 联系人ID: " + contactId);

        executor.execute(() -> {
            try {
                List<Message> messages = messageDao.getMessagesBetweenUsers(currentUserId, contactId);

                Log.d("ChatActivity", "数据库查询结果 - 消息数量: " + messages.size());
                for (Message msg : messages) {
                    Log.d("ChatActivity", "消息详情 - ID: " + msg.getId() +
                            ", 发送者: " + msg.getSenderId() +
                            ", 接收者: " + msg.getReceiverId() +
                            ", 内容: " + msg.getContent());
                }

                runOnUiThread(() -> {
                    messageList.clear();
                    messageList.addAll(messages);
                    messageAdapter.notifyDataSetChanged();
                    scrollToBottom();

                    Log.d("ChatActivity", "加载消息完成，数量: " + messages.size() +
                            ", 当前用户ID: " + currentUserId);

                    // 统计消息类型
                    int sentCount = 0, receivedCount = 0;
                    for (Message msg : messages) {
                        if (msg.getSenderId() == currentUserId) {
                            sentCount++;
                        } else if (msg.getReceiverId() == currentUserId) {
                            receivedCount++;
                        }
                    }

                    Log.d("ChatActivity", "消息统计 - 我发送: " + sentCount + ", 对方发送: " + receivedCount);

                    if (messages.isEmpty()) {
                        Toast.makeText(ChatActivity.this, "还没有消息，开始聊天吧！", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "加载了 " + messages.size() + " 条消息", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("ChatActivity", "加载消息失败，用户ID: " + currentUserId + ", 错误: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "加载消息失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void observeMessages() {
        int currentUserId = getCurrentUserId();

        messageDao.getMessagesBetweenUsersLiveData(currentUserId, contactId)
                .observe(this, messages -> {
                    Log.d("ChatActivity", "实时消息更新，数量: " + messages.size());
                    messageList.clear();
                    messageList.addAll(messages);
                    messageAdapter.notifyDataSetChanged();
                    scrollToBottom();
                });
    }
    private void clearUnreadCount() {
        int currentUserId = getCurrentUserId();
        Log.d("ChatActivity", "清空未读消息数，用户ID: " + currentUserId);
        executor.execute(() -> {
            try {
                if (conversationId != -1) {
                    conversationDao.clearUnreadCount(conversationId);
                    contactDao.clearUnreadCount(contactId);
                }
            } catch (Exception e) {
                Log.e("ChatActivity", "清空未读消息数失败，用户ID: " + currentUserId + ", 错误: " + e.getMessage());
            }
        });
    }

    private void scrollToBottom() {
        if (messageList.size() > 0) {
            rvMessages.scrollToPosition(messageList.size() - 1);
        }
    }

    private void toggleEmojiPanel() {
        View panelEmoji = findViewById(R.id.panelEmoji);
        boolean isVisible = panelEmoji.getVisibility() == View.VISIBLE;
        panelEmoji.setVisibility(isVisible ? View.GONE : View.VISIBLE);
    }

    private void showMoreMenu() {
        Toast.makeText(this, "更多功能", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ChatActivity", "页面恢复，刷新消息，用户ID: " + getCurrentUserId());

        // 每次恢复时检查登录状态
        if (!isUserLoggedIn()) {
            Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 页面恢复时刷新消息列表
        loadMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ChatActivity", "=== 销毁ChatActivity，用户ID: " + getCurrentUserId() + " ===");

        if (executor != null) {
            executor.shutdown();
        }
    }
}