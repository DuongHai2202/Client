package vn.duonghai.client.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import vn.duonghai.client.databinding.ActivityWelcomeBinding;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Xử lý logic Ghi nhớ Đăng nhập (Remember Me)
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("rememberMe", false);

        if (mAuth.getCurrentUser() != null) {
            if (rememberMe) {
                // Nếu đã đăng nhập trước đó và có chọn "Ghi nhớ", nhảy thẳng vào Home
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finishAffinity();
                return;
            } else {
                // Nếu không chọn ghi nhớ, tự động Sign Out khi bật lại App
                mAuth.signOut();
            }
        }

        binding.btnGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });

        binding.btnGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, RegisterActivity.class));
        });
    }
}
