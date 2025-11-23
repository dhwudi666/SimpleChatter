package com.example.simplechatter.activity;

import android.content.Context;
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

    private int currentUserId = 1; // 当前登录用户ID
    private int contactId; // 联系人ID
    private int conversationId = -1; // 会话ID，初始化为-1
    private String contactName;

    private List<Message> messageList = new ArrayList<>();
    private boolean isKeyboardMode = true; // 当前是否为键盘模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Log.d("ChatActivity", "=== 启动ChatActivity ===");

        // 获取传递的参数
        getIntentData();
        // 初始化视图
        initViews();
        // 初始化数据
        initData();
        // 设置事件监听
        setupListeners();
        // 设置键盘功能
        setupKeyboardFunction();
        // 加载消息
        loadMessages();
        // 清空未读消息
        clearUnreadCount();
    }

    private void getIntentData() {
        contactId = getIntent().getIntExtra("contact_id", -1);
        contactName = getIntent().getStringExtra("contact_name");

        Log.d("ChatActivity", "接收参数 - contactId: " + contactId + ", contactName: " + contactName);

        if (contactId == -1) {
            Toast.makeText(this, "联系人信息错误", Toast.LENGTH_SHORT).show();
            Log.e("ChatActivity", "联系人ID无效，关闭Activity");
            finish();
        }
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        btnEmoji = findViewById(R.id.btnEmoji);
        btnVoice = findViewById(R.id.btnVoice); // 语音/键盘切换按钮
        tvChatTitle = findViewById(R.id.tvChatTitle);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);

        // 设置标题
        tvChatTitle.setText(contactName != null ? contactName : "聊天对象");

        // 设置消息列表
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

        // 获取或创建会话
        getOrCreateConversation();
    }

    private void getOrCreateConversation() {
        Log.d("ChatActivity", "开始获取或创建会话，contactId: " + contactId);

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

                    Log.d("ChatActivity", "新会话创建成功，ID: " + conversationId);

                    // 为新会话添加初始消息
                    addInitialMessages(conversationId);
                } else {
                    conversationId = conversation.getId();
                    Log.d("ChatActivity", "找到现有会话，ID: " + conversationId);
                }

            } catch (Exception e) {
                Log.e("ChatActivity", "获取或创建会话失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(this, "会话初始化失败", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void addInitialMessages(int convId) {
        try {
            // 检查是否已有消息
            List<Message> existingMessages = messageDao.getMessagesByConversation(convId);
            if (existingMessages.isEmpty()) {
                Log.d("ChatActivity", "添加初始测试消息");

                // 根据联系人ID添加不同的初始消息
                String[] initialMessages = getInitialMessagesForContact(contactId);

                for (int i = 0; i < initialMessages.length; i++) {
                    boolean isSentByMe = (i % 2 == 0); // 交替发送

                    Message message = new Message(convId,
                            isSentByMe ? currentUserId : contactId,
                            isSentByMe ? contactId : currentUserId,
                            initialMessages[i],
                            Message.TYPE_TEXT);

                    message.setSentByMe(isSentByMe);
                    message.setStatus(Message.STATUS_SENT);
                    message.setTimestamp(System.currentTimeMillis() - (initialMessages.length - i) * 600000);

                    messageDao.insertMessage(message);
                }

                // 更新最后一条消息
                if (initialMessages.length > 0) {
                    String lastMessage = initialMessages[initialMessages.length - 1];
                    conversationDao.updateLastMessage(convId, lastMessage, System.currentTimeMillis());
                    contactDao.updateLastMessage(contactId, lastMessage, System.currentTimeMillis());
                }

                Log.d("ChatActivity", "初始消息添加完成");

                // 刷新消息列表
                runOnUiThread(() -> loadMessages());
            }
        } catch (Exception e) {
            Log.e("ChatActivity", "添加初始消息失败: " + e.getMessage());
        }
    }

    private String[] getInitialMessagesForContact(int contactId) {
        // 根据联系人ID返回不同的初始消息
        switch (contactId) {
            case 2: // 张三
                return new String[]{"你好！", "最近怎么样？", "项目进展顺利吗？"};
            case 3: // 李四
                return new String[]{"明天开会别忘了", "资料准备好了吗？", "记得带笔记本电脑"};
            case 4: // 王五
                return new String[]{"[图片]", "这张照片拍得不错", "周末一起去拍照吧"};
            case 5: // 赵六
                return new String[]{"好的，没问题", "我已经安排好了", "随时联系"};
            case 6: // 钱七
                return new String[]{"语音通话 00:32", "刚才信号不好", "重新打给你"};
            case 7: // 孙八
                return new String[]{"晚上一起吃饭？", "餐厅我订好了", "6点见"};
            case 8: // 周九
                return new String[]{"项目进展如何？", "有什么需要帮忙的吗？", "保持沟通"};
            default:
                return new String[]{"你好！", "很高兴和你聊天"};
        }
    }

    private void setupListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> {
            Log.d("ChatActivity", "返回按钮点击");
            finish();
        });

        // 发送按钮
        btnSend.setOnClickListener(v -> {
            Log.d("ChatActivity", "发送消息点击");
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
        Log.d("ChatActivity", "显示键盘");
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
        Log.d("ChatActivity", "隐藏键盘");
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
        Log.d("ChatActivity", "切换到键盘模式");
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

        Toast.makeText(this, "键盘模式", Toast.LENGTH_SHORT).show();
    }

    /**
     * 切换到语音模式
     */
    private void switchToVoiceMode() {
        Log.d("ChatActivity", "切换到语音模式");
        isKeyboardMode = false;

        // 更新按钮图标为麦克风
        if (btnVoice != null) {
            btnVoice.setImageResource(R.drawable.mic_24px);
            btnVoice.setContentDescription("切换到键盘输入");
        }

        // 隐藏键盘
        hideKeyboard();

        // 显示语音录制提示
        showVoiceRecordingUI();

        Toast.makeText(this, "语音模式", Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示语音录制界面
     */
    private void showVoiceRecordingUI() {
        // 这里可以实现语音录制UI
        Toast.makeText(this, "语音功能开发中", Toast.LENGTH_SHORT).show();

        // 3秒后自动切换回键盘模式
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchToKeyboardMode();
            }
        }, 3000);
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

    /**
     * 检查键盘是否可见
     */
    private boolean isKeyboardVisible() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm != null && imm.isActive(etMessage);
    }

    private void sendMessage() {
        String content = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            return;
        }

        if (conversationId == -1) {
            Toast.makeText(this, "会话未准备好，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            try {
                // 创建消息对象
                Message message = new Message(conversationId, currentUserId, contactId, content, Message.TYPE_TEXT);
                message.setSentByMe(true);
                message.setStatus(Message.STATUS_SENT);

                // 保存到数据库
                long messageId = messageDao.insertMessage(message);
                message.setId((int) messageId);

                // 更新会话最后消息
                conversationDao.updateLastMessage(conversationId, content, System.currentTimeMillis());

                // 更新联系人最后消息
                contactDao.updateLastMessage(contactId, content, System.currentTimeMillis());

                runOnUiThread(() -> {
                    // 添加到消息列表
                    messageList.add(message);
                    messageAdapter.notifyItemInserted(messageList.size() - 1);

                    // 清空输入框
                    etMessage.setText("");

                    // 滚动到底部
                    scrollToBottom();

                    Toast.makeText(ChatActivity.this, "消息已发送", Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                Log.e("ChatActivity", "发送消息失败: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void loadMessages() {
        executor.execute(() -> {
            try {
                if (conversationId != -1) {
                    List<Message> messages = messageDao.getMessagesByConversation(conversationId);
                    runOnUiThread(() -> {
                        messageList.clear();
                        messageList.addAll(messages);
                        messageAdapter.notifyDataSetChanged();
                        scrollToBottom();
                        Log.d("ChatActivity", "加载消息完成，数量: " + messages.size());
                    });
                }
            } catch (Exception e) {
                Log.e("ChatActivity", "加载消息失败: " + e.getMessage());
            }
        });
    }

    private void clearUnreadCount() {
        executor.execute(() -> {
            try {
                if (conversationId != -1) {
                    conversationDao.clearUnreadCount(conversationId);
                    contactDao.clearUnreadCount(contactId);
                    Log.d("ChatActivity", "清空未读消息数");
                }
            } catch (Exception e) {
                Log.e("ChatActivity", "清空未读消息数失败: " + e.getMessage());
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
    protected void onPause() {
        super.onPause();
        hideKeyboard();
    }

    /**
     * 处理返回键 - 先隐藏键盘再退出
     */
    @Override
    public void onBackPressed() {
        if (isKeyboardVisible()) {
            hideKeyboard();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("ChatActivity", "=== 销毁ChatActivity ===");

        if (executor != null) {
            executor.shutdown();
        }
    }
}