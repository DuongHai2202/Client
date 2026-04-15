package vn.duonghai.client.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vn.duonghai.client.R;
import vn.duonghai.client.fragments.AdminHomeFragment;
import vn.duonghai.client.fragments.AdminMenuFragment;
import vn.duonghai.client.fragments.AdminOrdersFragment;
import vn.duonghai.client.fragments.AdminProfileFragment;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavAdmin);

        if (savedInstanceState == null) {
            loadFragment(new AdminHomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_admin_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_admin_home) {
                selectedFragment = new AdminHomeFragment();
            } else if (itemId == R.id.nav_admin_menu) {
                selectedFragment = new AdminMenuFragment();
            } else if (itemId == R.id.nav_admin_orders) {
                selectedFragment = new AdminOrdersFragment();
            } else if (itemId == R.id.nav_admin_profile) {
                selectedFragment = new AdminProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
