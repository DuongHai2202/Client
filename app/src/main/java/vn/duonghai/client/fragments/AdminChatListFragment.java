package vn.duonghai.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.ChatActivity;
import vn.duonghai.client.adapters.AdminUserAdapter;
import vn.duonghai.client.models.User;

public class AdminChatListFragment extends Fragment {

    private RecyclerView rvAdminChatList;
    private ProgressBar pbAdminChatList;

    private AdminUserAdapter adapter;
    private List<User> chatUserList;
    private List<String> chatUserIds; // Lưu các cây khóa của 'chats'

    private DatabaseReference chatsRef;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_chat_list, container, false);

        rvAdminChatList = view.findViewById(R.id.rvAdminChatList);
        pbAdminChatList = view.findViewById(R.id.pbAdminChatList);

        rvAdminChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatUserList = new ArrayList<>();
        chatUserIds = new ArrayList<>();
        
        // Ta sử dụng lại AdminUserAdapter luôn vì giao diện card quá hợp để hiển thị User
        adapter = new AdminUserAdapter(getContext(), chatUserList, new AdminUserAdapter.OnUserClickListener() {
            @Override
            public void onEditClick(User user) {
                // Biến đổi nút Sửa thành việc bật Khung Chat
                openChatWithUser(user);
            }

            @Override
            public void onDeleteClick(User user) {
                Toast.makeText(getContext(), "Tính năng xoá hòm thư đang được bảo trì an toàn!", Toast.LENGTH_SHORT).show();
            }
        });
        
        rvAdminChatList.setAdapter(adapter);

        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadInbox();

        return view;
    }

    private void loadInbox() {
        pbAdminChatList.setVisibility(View.VISIBLE);
        chatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUserIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String userId = snap.getKey();
                    if (userId != null && !userId.equals("admin")) { 
                        // Thu thập tất cả key user đã từng chat
                        chatUserIds.add(userId);
                    }
                }
                loadUserDetails();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbAdminChatList.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserDetails() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatUserList.clear();
                for (String uid : chatUserIds) {
                    if (snapshot.hasChild(uid)) {
                        User user = snapshot.child(uid).getValue(User.class);
                        if (user != null) {
                            user.setId(uid);
                            chatUserList.add(user);
                        }
                    } else {
                        // User đã xoá nhưng đoạn hội thoại vẫn còn
                        User unknownUser = new User("Người dùng bị ẩn", "(Xóa DB)", "", "customer");
                        unknownUser.setId(uid);
                        chatUserList.add(unknownUser);
                    }
                }
                adapter.notifyDataSetChanged();
                pbAdminChatList.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbAdminChatList.setVisibility(View.GONE);
            }
        });
    }

    private void openChatWithUser(User user) {
        if (getContext() != null) {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            // Gửi id của user qua để Admin mở đúng nhánh Chat
            intent.putExtra("receiverId", user.getId());
            startActivity(intent);
        }
    }
}
