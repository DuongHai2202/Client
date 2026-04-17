package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.User;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onEditClick(User user);
        void onDeleteClick(User user);
    }

    public AdminUserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvAdminUserName.setText(user.getName() != null ? user.getName() : "Không tên");
        holder.tvAdminUserEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : ""));
        holder.tvAdminUserPhone.setText("Phone: " + (user.getPhone() != null ? user.getPhone() : ""));
        holder.tvAdminUserRole.setText(user.getRole() != null ? user.getRole().toUpperCase() : "CUSTOMER");

        holder.btnEditUser.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(user);
        });

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvAdminUserName, tvAdminUserEmail, tvAdminUserPhone, tvAdminUserRole;
        ImageButton btnEditUser, btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAdminUserName = itemView.findViewById(R.id.tvAdminUserName);
            tvAdminUserEmail = itemView.findViewById(R.id.tvAdminUserEmail);
            tvAdminUserPhone = itemView.findViewById(R.id.tvAdminUserPhone);
            tvAdminUserRole = itemView.findViewById(R.id.tvAdminUserRole);
            btnEditUser = itemView.findViewById(R.id.btnEditUser);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
