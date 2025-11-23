//package com.example.simplechatter.Adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.simplechatter.R;
//import com.example.simplechatter.database.Entity.Contact;
//
//import java.text.SimpleDateFormat;
//import java.util.List;
//import java.util.Locale;
//
//public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
//    private List<Contact> contactList;
//    private OnContactClickListener listener;
//    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//
//    public interface OnContactClickListener {
//        void onContactClick(Contact contact);
//        void onContactLongClick(Contact contact);
//    }
//
//    public ContactsAdapter(List<Contact> contactList, OnContactClickListener listener) {
//        this.contactList = contactList;
//        this.listener = listener;
//    }
//
//    @NonNull
//    @Override
//    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.item_contact, parent, false);
//        return new ContactViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
//        Contact contact = contactList.get(position);
//        holder.bind(contact);
//
//        holder.itemView.setOnClickListener(v -> {
//            if (listener != null) {
//                listener.onContactClick(contact);
//            }
//        });
//
//        holder.itemView.setOnLongClickListener(v -> {
//            if (listener != null) {
//                listener.onContactLongClick(contact);
//                return true;
//            }
//            return false;
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return contactList.size();
//    }
//
//    public void updateData(List<Contact> newContacts) {
//        this.contactList = newContacts;
//        notifyDataSetChanged();
//    }
//
//    class ContactViewHolder extends RecyclerView.ViewHolder {
//        private ImageView ivAvatar;
//        private TextView tvName, tvLastMessage, tvTime, tvUnreadCount;
//        private View vOnlineStatus;
//
//        ContactViewHolder(@NonNull View itemView) {
//            super(itemView);
//            ivAvatar = itemView.findViewById(R.id.ivAvatar);
//            tvName = itemView.findViewById(R.id.tvName);
//            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
//            tvTime = itemView.findViewById(R.id.tvTime);
//            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
//            vOnlineStatus = itemView.findViewById(R.id.vOnlineStatus);
//        }
//
//        void bind(Contact contact) {
//            tvName.setText(contact.getName());
//            tvLastMessage.setText(contact.getLastMessage());
//            tvTime.setText(timeFormat.format(contact.getLastMessageTime()));
//
//            // 在线状态
//            vOnlineStatus.setVisibility(contact.isOnline() ? View.VISIBLE : View.INVISIBLE);
//
//            // 未读消息数
//            if (contact.getUnreadCount() > 0) {
//                tvUnreadCount.setText(String.valueOf(contact.getUnreadCount()));
//                tvUnreadCount.setVisibility(View.VISIBLE);
//            } else {
//                tvUnreadCount.setVisibility(View.GONE);
//            }
//
//            // 头像设置（实际项目中从网络或本地加载）
//            // ivAvatar.setImageResource(getAvatarResource(contact.getAvatar()));
//        }
//
//        private int getAvatarResource(String avatarName) {
//            // 根据头像名称返回对应的资源ID
//            switch (avatarName) {
//                case "avatar_zhang": return R.drawable.avatar1;
//                case "avatar_li": return R.drawable.avatar2;
//                case "avatar_wang": return R.drawable.avatar3;
//                default: return R.drawable.ic_person;
//            }
//        }
//    }
//}