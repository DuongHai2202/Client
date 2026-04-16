package vn.duonghai.client.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import vn.duonghai.client.R;
import vn.duonghai.client.fragments.AdminAccountsFragment;
import vn.duonghai.client.fragments.AdminHomeFragment;
import vn.duonghai.client.fragments.AdminMenuFragment;
import vn.duonghai.client.fragments.AdminOrdersFragment;
import vn.duonghai.client.fragments.AdminProfileFragment;
import vn.duonghai.client.fragments.AdminReviewsFragment;
import vn.duonghai.client.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.NonNull;

public class AdminMainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
        
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finishAffinity();
            return;
        }

        drawerLayout = findViewById(R.id.adminDrawerLayout);
        navigationView = findViewById(R.id.adminNavigationView);
        toolbar = findViewById(R.id.adminToolbar);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Trang chủ");
        }

        // Khởi tạo Drawer Toggle (Nút 3 gạch mở Menu)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Đổi màu tuỳ chỉnh cho icon 3 gạch nếu muốn, ở đây mặc định sẽ dùng màu của theme
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white, getTheme()));

        loadUserProfileToHeader(currentUser);

        // Tải Fragment mặc định ban đầu
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.adminFragmentContainer, new AdminHomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_admin_home);
        }

        // Cài đặt sự kiện chọn trên Nav Menu
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            String title = "";

            if (itemId == R.id.nav_admin_home) {
                selectedFragment = new AdminHomeFragment();
                title = "Trang chủ";
            } else if (itemId == R.id.nav_admin_menu) {
                selectedFragment = new AdminMenuFragment();
                title = "Quản lý sản phẩm";
            } else if (itemId == R.id.nav_admin_orders) {
                selectedFragment = new AdminOrdersFragment();
                title = "Quản lý đơn hàng";
            } else if (itemId == R.id.nav_admin_accounts) {
                selectedFragment = new AdminAccountsFragment();
                title = "Quản lý tài khoản";
            } else if (itemId == R.id.nav_admin_reviews) {
                selectedFragment = new AdminReviewsFragment();
                title = "Quản lý đánh giá";
            } else if (itemId == R.id.nav_admin_profile) {
                selectedFragment = new AdminProfileFragment();
                title = "Cài đặt hệ thống";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.adminFragmentContainer, selectedFragment)
                        .commit();

                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(title);
                }
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Xử lý nút Back của điện thoại
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false); // Tránh vòng lặp
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void loadUserProfileToHeader(FirebaseUser currentUser) {
        if (navigationView.getHeaderCount() > 0) {
            android.view.View headerView = navigationView.getHeaderView(0);
            TextView tvName = headerView.findViewById(R.id.tvAdminHeaderName);
            TextView tvEmail = headerView.findViewById(R.id.tvAdminHeaderEmail);

            if (tvEmail != null) tvEmail.setText(currentUser.getEmail());

            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User userProfile = snapshot.getValue(User.class);
                        if (userProfile != null && tvName != null) {
                            tvName.setText(userProfile.getName());
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }
}
