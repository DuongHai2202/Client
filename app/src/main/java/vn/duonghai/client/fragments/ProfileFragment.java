package vn.duonghai.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.WelcomeActivity;
import vn.duonghai.client.models.User;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvProfileEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        
        TextView tvAddressBook = view.findViewById(R.id.tvAddressBook);
        tvAddressBook.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), vn.duonghai.client.activities.AddressListActivity.class));
        });
        
        TextView tvOrderHistory = view.findViewById(R.id.tvOrderHistory);
        tvOrderHistory.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), vn.duonghai.client.activities.OrderHistoryActivity.class));
        });

        TextView tvFavoriteDrinks = view.findViewById(R.id.tvFavoriteDrinks);
        tvFavoriteDrinks.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng Món nước yêu thích sắp ra mắt", Toast.LENGTH_SHORT).show();
        });
        
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), WelcomeActivity.class));
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });
        
        loadUserData();
        
        return view;
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User userProfile = snapshot.getValue(User.class);
                        if (userProfile != null) {
                            tvProfileName.setText(userProfile.getName());
                            tvProfileEmail.setText(userProfile.getEmail());
                        }
                    } else {
                        // Trường hợp thư mục User bị rỗng do Import nhầm File JSON
                        tvProfileName.setText("Chưa cập nhật Tên");
                        tvProfileEmail.setText(currentUser.getEmail());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Lỗi tải thông tin: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
