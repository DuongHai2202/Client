package vn.duonghai.client.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.MessageAdapter;
import vn.duonghai.client.models.Message;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private RecyclerView rvChatMessages;
    private EditText edtChatMessage;
    private ImageButton btnSendMsg;

    private String currentUserId;
    private String receiverId;
    
    // Node xác định vị trí lưu tin nhắn giữa user hiện tại và receiver
    private String chatNodeId; 

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatToolbar = findViewById(R.id.chatToolbar);
        rvChatMessages = findViewById(R.id.rvChatMessages);
        edtChatMessage = findViewById(R.id.edtChatMessage);
        btnSendMsg = findViewById(R.id.btnSendMsg);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            currentUserId = "admin"; // Giả định nếu là admin thì ID là "admin" (tùy theo logic app của bạn)
        }

        // Nhận ID người bên kia (Admin hoặc ID Khách hàng)
        receiverId = getIntent().getStringExtra("receiverId");

        if (receiverId == null) {
            Toast.makeText(this, "Không tìm thấy người nhận!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (receiverId.equals("admin")) {
            // Khách nhắn cho admin thì nút gốc theo id khách
            chatNodeId = currentUserId; 
            setupToolbar("Chăm sóc khách hàng");
        } else {
            // Admin nhắn cho khách thì nút gốc theo id khách
            currentUserId = "admin"; // Ghi đè cứng nếu đang đóng vai trò admin
            chatNodeId = receiverId;
            setupToolbar("Đang nhắn với Khách: " + receiverId.substring(0, Math.min(5, receiverId.length())) + "...");
        }

        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatNodeId);

        rvChatMessages.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // Tin nhắn nảy từ dưới lên
        rvChatMessages.setLayoutManager(linearLayoutManager);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        rvChatMessages.setAdapter(messageAdapter);

        readMessages();

        btnSendMsg.setOnClickListener(v -> {
            String msgText = edtChatMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(msgText)) {
                sendMessage(currentUserId, receiverId, msgText);
                edtChatMessage.setText(""); // Xóa input
            }
        });
    }

    private void setupToolbar(String title) {
        setSupportActionBar(chatToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        chatToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void sendMessage(String sender, String receiver, String text) {
        long timestamp = System.currentTimeMillis();
        Message message = new Message(sender, receiver, text, timestamp);
        
        String msgId = chatRef.push().getKey();
        if (msgId != null) {
            message.setMessageId(msgId);
            chatRef.child(msgId).setValue(message);
        }
    }

    private void readMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Message message = snap.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                // Cuộn xuống dòng mới nhất
                if (messageList.size() > 0) {
                    rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Mute
            }
        });
    }
}
