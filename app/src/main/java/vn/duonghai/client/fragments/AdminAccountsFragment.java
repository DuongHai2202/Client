package vn.duonghai.client.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
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
import vn.duonghai.client.adapters.AdminUserAdapter;
import vn.duonghai.client.models.User;

public class AdminAccountsFragment extends Fragment implements AdminUserAdapter.OnUserClickListener {

    private RecyclerView rvAdminUsers;
    private ProgressBar pbAdminUsers;
    private FloatingActionButton fabAddUser;

    private AdminUserAdapter adapter;
    private List<User> userList;

    private DatabaseReference usersRef;
    private ValueEventListener usersListener;

    private FirebaseApp secondaryApp;
    private FirebaseAuth tempAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_accounts, container, false);

        rvAdminUsers = view.findViewById(R.id.rvAdminUsers);
        pbAdminUsers = view.findViewById(R.id.pbAdminUsers);
        fabAddUser = view.findViewById(R.id.fabAddUser);

        rvAdminUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(getContext(), userList, this);
        rvAdminUsers.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initSecondaryFirebaseApp();

        loadUsers();

        fabAddUser.setOnClickListener(v -> showAddEditUserDialog(null));

        return view;
    }

    private void initSecondaryFirebaseApp() {
        if (getContext() == null) return;
        try {
            secondaryApp = FirebaseApp.getInstance("SecondaryApp");
        } catch (IllegalStateException e) {
            FirebaseOptions options = FirebaseApp.getInstance().getOptions();
            secondaryApp = FirebaseApp.initializeApp(getContext(), options, "SecondaryApp");
        }
        tempAuth = FirebaseAuth.getInstance(secondaryApp);
    }

    private void loadUsers() {
        pbAdminUsers.setVisibility(View.VISIBLE);
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    User user = snap.getValue(User.class);
                    if (user != null) {
                        user.setId(snap.getKey());
                        userList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
                pbAdminUsers.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbAdminUsers.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        usersRef.addValueEventListener(usersListener);
    }

    @Override
    public void onEditClick(User user) {
        showAddEditUserDialog(user);
    }

    @Override
    public void onDeleteClick(User user) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xoá người dùng")
                .setMessage("Bạn có chắc chắn muốn xoá thẻ người dùng này ra khỏi hệ thống rỗng? (Sẽ không xoá khỏi Authentication do hạn chế bảo mật Client)")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    if (user.getId() != null) {
                        usersRef.child(user.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Đã xoá", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Xoá thất bại", Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }

    private void showAddEditUserDialog(User user) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_edit_user, null);
        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        TextInputEditText edtName = view.findViewById(R.id.edtAdminUserName);
        TextInputEditText edtEmail = view.findViewById(R.id.edtAdminUserEmail);
        TextInputEditText edtPassword = view.findViewById(R.id.edtAdminUserPassword);
        TextInputEditText edtPhone = view.findViewById(R.id.edtAdminUserPhone);
        TextInputLayout tilPassword = view.findViewById(R.id.tilAdminUserPassword);
        Spinner spinnerRole = view.findViewById(R.id.spinnerAdminUserRole);
        Button btnCancel = view.findViewById(R.id.btnCancelUserDialog);
        Button btnSave = view.findViewById(R.id.btnSaveUserDialog);

        String[] roles = {"customer", "admin"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roles);
        spinnerRole.setAdapter(roleAdapter);

        if (user != null) {
            edtName.setText(user.getName());
            edtEmail.setText(user.getEmail());
            edtPhone.setText(user.getPhone());
            if ("admin".equals(user.getRole())) {
                spinnerRole.setSelection(1);
            } else {
                spinnerRole.setSelection(0);
            }
            tilPassword.setVisibility(View.GONE);
            edtEmail.setEnabled(false); // Không hỗ trợ đổi email thông qua Client
        } else {
            tilPassword.setVisibility(View.VISIBLE);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = edtName.getText() != null ? edtName.getText().toString().trim() : "";
            String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
            String phone = edtPhone.getText() != null ? edtPhone.getText().toString().trim() : "";
            String password = edtPassword.getText() != null ? edtPassword.getText().toString().trim() : "";
            String role = spinnerRole.getSelectedItem().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Vui lòng nhập đủ Tên và Email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null && (TextUtils.isEmpty(password) || password.length() < 6)) {
                Toast.makeText(getContext(), "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setEnabled(false);
            btnSave.setText("Đang lưu...");

            User newUser = new User(name, email, phone, role);

            if (user == null) {
                // Thêm mới: Tạo bên Auth, sau đó đưa lên DB
                tempAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser fUser = tempAuth.getCurrentUser();
                                if (fUser != null) {
                                    String newId = fUser.getUid();
                                    usersRef.child(newId).setValue(newUser)
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(getContext(), "Tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            })
                                            .addOnFailureListener(e -> {
                                                btnSave.setEnabled(true);
                                                btnSave.setText("Lưu");
                                                Toast.makeText(getContext(), "Lỗi lưu DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                }
                            } else {
                                btnSave.setEnabled(true);
                                btnSave.setText("Lưu");
                                Toast.makeText(getContext(), "Tạo Auth thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                // Cập nhật (Chỉ sửa DB)
                usersRef.child(user.getId()).setValue(newUser)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            btnSave.setEnabled(true);
                            btnSave.setText("Lưu");
                            Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
        if (view.getBackground() == null) {
            view.setBackgroundResource(R.color.white);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (usersRef != null && usersListener != null) {
            usersRef.removeEventListener(usersListener);
        }
    }
}
