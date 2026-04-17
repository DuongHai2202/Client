package vn.duonghai.client.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_sent, parent, false);
            return new MessageViewHolder(view, true);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_received, parent, false);
            return new MessageViewHolder(view, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        
        holder.tvMessage.setText(message.getMessage());
        
        // Format thời gian
        String timeString = DateFormat.format("HH:mm", message.getTimestamp()).toString();
        holder.tvTime.setText(timeString);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        // Nếu ng gửi là current user => view bên phải (Right)
        if (messageList.get(position).getSenderId().equals(currentUserId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMessage;
        public TextView tvTime;

        public MessageViewHolder(@NonNull View itemView, boolean isSent) {
            super(itemView);
            if (isSent) {
                tvMessage = itemView.findViewById(R.id.tvSentMessage);
                tvTime = itemView.findViewById(R.id.tvSentTime);
            } else {
                tvMessage = itemView.findViewById(R.id.tvReceivedMessage);
                tvTime = itemView.findViewById(R.id.tvReceivedTime);
            }
        }
    }
}
