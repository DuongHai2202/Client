package vn.duonghai.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.WelcomeActivity;

public class AdminProfileFragment extends Fragment {

    private TextView tvAdminName, tvAdminEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        tvAdminName = view.findViewById(R.id.tvAdminName);
        tvAdminEmail = view.findViewById(R.id.tvAdminEmail);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tvAdminEmail.setText(user.getEmail());
            FirebaseDatabase.getInstance().getReference("users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.hasChild("name")) {
                                tvAdminName.setText(snapshot.child("name").getValue(String.class));
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        Button btnLogout = view.findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getActivity(), WelcomeActivity.class));
            if (getActivity() != null) {
                getActivity().finishAffinity();
            }
        });

        return view;
    }
}
