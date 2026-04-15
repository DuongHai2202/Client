package vn.duonghai.client.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean("rememberMe", false);

        if (mAuth.getCurrentUser() != null) {
            if (rememberMe) {
                // Auto-login: kiểm tra role rồi phân luồng
                checkRoleAndNavigate(mAuth.getCurrentUser());
                return;
            } else {
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

    private void checkRoleAndNavigate(FirebaseUser user) {
        FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = "customer";
                        if (snapshot.exists() && snapshot.hasChild("role")) {
                            role = snapshot.child("role").getValue(String.class);
                        }

                        Intent intent;
                        if ("admin".equals(role)) {
                            intent = new Intent(WelcomeActivity.this, AdminMainActivity.class);
                        } else {
                            intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        }

                        startActivity(intent);
                        finishAffinity();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Nếu lỗi, vẫn cho vào trang Welcome bình thường
                    }
                });
    }
}

