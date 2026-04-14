package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.duonghai.client.databinding.ActivityRegisterBinding;
import vn.duonghai.client.models.User;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        binding.btnRegister.setOnClickListener(v -> registerUser());

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = binding.edtName.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String email = binding.edtEmail.getText().toString().trim();
        String password = binding.edtPassword.getText().toString().trim();
        String confirmPassword = binding.edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.edtName.setError("Vui lòng nhập Họ Tên");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            binding.edtPhone.setError("Vui lòng nhập Số điện thoại");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            binding.edtEmail.setError("Vui lòng nhập Email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.edtPassword.setError("Mật khẩu phải từ 6 ký tự");
            return;
        }
        if (!password.equals(confirmPassword)) {
            binding.edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Đang đăng ký...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = mAuth.getCurrentUser();
                        if (fUser != null) {
                            String uid = fUser.getUid();
                            User userModel = new User(name, email, phone, "customer");
                            
                            mDatabase.child(uid).setValue(userModel)
                                    .addOnCompleteListener(taskDB -> {
                                        if (taskDB.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            finishAffinity(); // Xoá sạch mọi activity cũ để đỡ back lại Register
                                        } else {
                                            binding.btnRegister.setEnabled(true);
                                            binding.btnRegister.setText("Đăng Ký");
                                            Toast.makeText(RegisterActivity.this, "Lỗi lưu DB: " + taskDB.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        }
                    } else {
                        binding.btnRegister.setEnabled(true);
                        binding.btnRegister.setText("Đăng Ký");
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
