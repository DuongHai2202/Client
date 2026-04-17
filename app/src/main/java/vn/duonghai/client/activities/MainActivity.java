package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.fragments.AboutUsFragment;
import vn.duonghai.client.fragments.CartFragment;
import vn.duonghai.client.fragments.HomeFragment;
import vn.duonghai.client.fragments.MenuFragment;
import vn.duonghai.client.fragments.ProfileFragment;
import vn.duonghai.client.models.User;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, WelcomeActivity.class));
            finishAffinity();
            return;
        }

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabChat = findViewById(R.id.fabCustomerChat);

        // Nút chat dành cho khách hàng
        fabChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", "admin"); // Gửi đến admin
            startActivity(intent);
        });

        // Thiết lập Toolbar cho Activity
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Thay đổi màu nút 3 gạch qua XML được xử lý ngầm, nhưng nếu muốn đổi màu, sẽ cấu hình app:itemIconTint ở file layout
            getSupportActionBar().setTitle("Trang chủ");
        }

        // Tạo nút Hamburger (3 gạch) gắn liền với DrawerLayout
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 
            R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // Đôi khi icon 3 gạch mặc định là màu đen/trắng, ở đây ta đang dùng DarkActionBar trong layout nên nó mặc định sẽ trắng. Ở đây ta có thể tuỳ chỉnh nếu cần.
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white, getTheme()));

        // Tải thông tin cá nhân lên Header của Drawer
        loadUserProfileToHeader(currentUser);

        // Đặt Fragment mặc định khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            String title = "";

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "Trang chủ";
            } else if (itemId == R.id.nav_menu) {
                selectedFragment = new MenuFragment();
                title = "Menu";
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
                title = "Giỏ hàng";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "Tài khoản";
            } else if (itemId == R.id.nav_about) {
                selectedFragment = new AboutUsFragment();
                title = "Về chúng tôi";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
            }

            // Đóng Drawer sau khi chạm
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Xử lý khi bấm nút Back trên điện thoại (Sử dụng OnBackPressedDispatcher thay vì OnBackPressed() cũ bị gạch)
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Nếu đang không mở drawer thì back thoát activity
                    setEnabled(false); // tắt cái này để tránh vòng lặp vô hạn
                    getOnBackPressedDispatcher().onBackPressed();
                    setEnabled(true);
                }
            }
        });
    }

    private void loadUserProfileToHeader(FirebaseUser currentUser) {
        if (navigationView.getHeaderCount() > 0) {
            android.view.View headerView = navigationView.getHeaderView(0);
            TextView tvName = headerView.findViewById(R.id.tvNavHeaderName);
            TextView tvEmail = headerView.findViewById(R.id.tvNavHeaderEmail);

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
                    // Mute
                }
            });
        }
    }

    /**
     * Phương thức hỗ trợ các Fragment yêu cầu chuyển Tab/Trang trong MainActivity
     * @param itemId ID của menu item trong NavigationView (ví dụ: R.id.nav_menu)
     */
    public void selectNavigationItem(int itemId) {
        android.view.MenuItem item = navigationView.getMenu().findItem(itemId);
        if (item != null) {
            // NavigationView của công dự án này sử dụng listener trực tiếp để thay đổi fragment
            // Nên ta có thể lấy listener của nó ra hoặc đơn giản là gọi logic giống trong listener
            navigationView.setCheckedItem(itemId);
            
            Fragment selectedFragment = null;
            String title = "";

            if (itemId == R.id.nav_home) {
                selectedFragment = new vn.duonghai.client.fragments.HomeFragment();
                title = "Trang chủ";
            } else if (itemId == R.id.nav_menu) {
                selectedFragment = new vn.duonghai.client.fragments.MenuFragment();
                title = "Menu";
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new vn.duonghai.client.fragments.CartFragment();
                title = "Giỏ hàng";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new vn.duonghai.client.fragments.ProfileFragment();
                title = "Tài khoản";
            } else if (itemId == R.id.nav_about) {
                selectedFragment = new vn.duonghai.client.fragments.AboutUsFragment();
                title = "Về chúng tôi";
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
                
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
            }
        }
    }
}