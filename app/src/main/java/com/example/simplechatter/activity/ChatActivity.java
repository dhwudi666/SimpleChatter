//package com.example.simplechatter.activity;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.simplechatter.adapter.MessageAdapter;
//import com.example.simplechatter.model.Message;
//
//
//public class ChatActivity extends AppCompatActivity {
//    private RecyclerView rvMessages;
//    private EditText etMessage;
//    private LinearLayout panelEmoji;
//    private MessageAdapter adapter;
//    private List<Message> messageList = new ArrayList<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//
//        initViews();
//        setupRecyclerView();
//        setupClickListeners();
////        loadMessages();
//    }
//
//    private void initViews() {
//        rvMessages = findViewById(R.id.rvMessages);
//        etMessage = findViewById(R.id.etMessage);
//        panelEmoji = findViewById(R.id.panelEmoji);
//    }
//
//    private void setupRecyclerView() {
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setStackFromEnd(true); // 消息从底部开始
//        rvMessages.setLayoutManager(layoutManager);
//
//        adapter = new MessageAdapter(messageList);
//        rvMessages.setAdapter(adapter);
//    }
//
//    private void setupClickListeners() {
//        // 表情按钮点击
//        findViewById(R.id.btnEmoji).setOnClickListener(v -> {
//            boolean isVisible = panelEmoji.getVisibility() == View.VISIBLE;
//            panelEmoji.setVisibility(isVisible ? View.GONE : View.VISIBLE);
//        });
//
//        // 发送按钮
//        findViewById(R.id.btnSend).setOnClickListener(v -> sendMessage());
//
//        // 输入框文本变化监听
//        etMessage.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void afterTextChanged(Editable s) {
//                boolean hasText = s.length() > 0;
//                // 根据是否有文本来显示发送按钮
//            }
//        });
////    }
//}
