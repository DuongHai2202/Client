package vn.duonghai.client.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import vn.duonghai.client.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnResetPass.setOnClickListener(v -> resetPassword());
        
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = binding.edtResetEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.edtResetEmail.setError("Vui lòng nhập Email");
            return;
        }

        binding.btnResetPass.setEnabled(false);
        binding.btnResetPass.setText("Đang gửi...");

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Hãy kiểm tra hộp thư Email của bạn!", Toast.LENGTH_LONG).show();
                        finish(); // Trở về trang đăng nhập
                    } else {
                        binding.btnResetPass.setEnabled(true);
                        binding.btnResetPass.setText("Gửi Email Khôi Phục");
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
