package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Vui lòng nhập Email");
            binding.edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.edtPassword.setError("Vui lòng nhập Mật khẩu");
            binding.edtPassword.requestFocus();
            return;
        }

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Đang đăng nhập...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        
                        boolean isRemember = binding.cbRemember.isChecked();
                        getSharedPreferences("LoginPrefs", MODE_PRIVATE)
                                .edit()
                                .putBoolean("rememberMe", isRemember)
                                .apply();
                        // Kiểm tra role để phân luồng
                        checkRoleAndNavigate(user);
                    } else {
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("Đăng Nhập");
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Check role
    private void checkRoleAndNavigate(FirebaseUser user) {
        if (user == null) return;

        FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = "customer"; // Mặc định
                        if (snapshot.exists() && snapshot.hasChild("role")) {
                            role = snapshot.child("role").getValue(String.class);
                        }

                        Intent intent;
                        if ("admin".equals(role)) {
                            Toast.makeText(LoginActivity.this, "Chào mừng Quản trị viên!", Toast.LENGTH_SHORT).show();
                            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                        } else {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                        }
                        startActivity(intent);
                        finishAffinity();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.btnLogin.setEnabled(true);
                        binding.btnLogin.setText("Đăng Nhập");
                        Toast.makeText(LoginActivity.this, "Lỗi kiểm tra quyền: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

