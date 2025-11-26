package com.example.simplechatter.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.R;
import com.example.simplechatter.database.Entity.Message;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messageList;
    private int currentUserId;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;

    public MessageAdapter(List<Message> messageList, int currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);

        // ✅ 修复：动态计算消息方向，而不是调用不存在的方法
        boolean isSentByMe = message.getSenderId() == currentUserId;

        Log.d("MessageAdapter", "消息方向判断 - 发送者: " + message.getSenderId() +
                ", 当前用户: " + currentUserId + ", 是否我发送: " + isSentByMe);

        return isSentByMe ? TYPE_SENT : TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = timeFormat.format(message.getTimestamp());

        // 添加调试日志
        boolean isSentByMe = message.getSenderId() == currentUserId;
        Log.d("MessageAdapter", "绑定消息 - 位置: " + position +
                ", 内容: " + message.getContent() +
                ", 发送者: " + message.getSenderId() +
                ", 类型: " + (isSentByMe ? "发送" : "接收"));

        if (holder.getItemViewType() == TYPE_SENT) {
            ((SentMessageViewHolder) holder).bind(message, time);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message, time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateData(List<Message> newMessages) {
        this.messageList.clear();
        this.messageList.addAll(newMessages);
        notifyDataSetChanged();

        // 添加调试日志
        Log.d("MessageAdapter", "数据更新，消息数量: " + newMessages.size());
        for (int i = 0; i < newMessages.size(); i++) {
            Message msg = newMessages.get(i);
            Log.d("MessageAdapter", "消息" + i + ": 发送者=" + msg.getSenderId() +
                    ", 接收者=" + msg.getReceiverId() + ", 内容=" + msg.getContent());
        }
    }

    // 发送的消息ViewHolder
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage, tvTime, tvStatus;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        void bind(Message message, String time) {
            tvMessage.setText(message.getContent());
            tvTime.setText(time);

            if (tvStatus != null) {
                switch (message.getStatus()) {
                    case Message.STATUS_SENDING:
                        tvStatus.setText("🕐");
                        break;
                    case Message.STATUS_SENT:
                        tvStatus.setText("✓");
                        break;
                    case Message.STATUS_DELIVERED:
                        tvStatus.setText("✓✓");
                        break;
                    case Message.STATUS_READ:
                        tvStatus.setText("✓✓");
                        tvStatus.setTextColor(0xFF07C160);
                        break;
                    default:
                        tvStatus.setText("❌");
                }
            }
        }
    }

    // 接收的消息ViewHolder
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage, tvTime;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(Message message, String time) {
            tvMessage.setText(message.getContent());
            tvTime.setText(time);
        }
    }
}