package com.example.simplechatter.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.simplechatter.R;
import com.example.simplechatter.database.Entity.Contact;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contactList;
    private OnContactClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
        void onContactLongClick(Contact contact);
    }

    public ContactsAdapter(List<Contact> contactList, OnContactClickListener listener) {
        this.contactList = contactList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.bind(contact);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onContactClick(contact);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onContactLongClick(contact);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return contactList == null ? 0 : contactList.size();
    }

    public void updateData(List<Contact> newContacts) {
        this.contactList = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAvatar;
        private TextView tvName, tvLastMessage, tvTime, tvUnreadCount;
        private View vOnlineStatus;
        private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
            vOnlineStatus = itemView.findViewById(R.id.vOnlineStatus);
        }

        void bind(Contact contact) {
            // 设置联系人姓名
            tvName.setText(contact.getName());

            // 设置最后一条消息
            tvLastMessage.setText(contact.getLastMessage());

            // 设置时间
            tvTime.setText(timeFormat.format(contact.getLastMessageTime()));

            // 设置在线状态
            vOnlineStatus.setVisibility(contact.isOnline() ? View.VISIBLE : View.INVISIBLE);

            // 设置未读消息数
            if (contact.getUnreadCount() > 0) {
                tvUnreadCount.setText(String.valueOf(contact.getUnreadCount()));
                tvUnreadCount.setVisibility(View.VISIBLE);
            } else {
                tvUnreadCount.setVisibility(View.GONE);
            }

            // 设置头像（实际项目中从网络或本地加载）
            setAvatar(contact.getAvatar());
        }

        private void setAvatar(String avatarName) {
            // 根据头像名称设置不同的头像资源
            int avatarResId = R.drawable.ic_launcher_background; // 默认头像

            if (avatarName != null) {
                switch (avatarName) {
                    case "avatar_zhang":
                        avatarResId = R.drawable.avatar1;
                        break;
                    case "avatar_li":
                        avatarResId = R.drawable.avatar1;
                        break;
                    case "avatar_wang":
                        avatarResId = R.drawable.avatar1;
                        break;
                    // 添加更多头像...
                }
            }
            ivAvatar.setImageResource(avatarResId);
        }
    }
}