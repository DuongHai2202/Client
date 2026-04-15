package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.AdminOrderAdapter;
import vn.duonghai.client.models.Order;

public class AdminHomeFragment extends Fragment {

    private TextView tvTotalProducts, tvTotalOrders, tvTotalUsers;
    private RecyclerView rcvRecentOrders;
    private TextView tvEmptyRecentOrders;
    
    private AdminOrderAdapter recentOrderAdapter;
    private List<Order> recentOrdersList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_home, container, false);

        tvTotalProducts = view.findViewById(R.id.tvTotalProducts);
        tvTotalOrders = view.findViewById(R.id.tvTotalOrders);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        
        rcvRecentOrders = view.findViewById(R.id.rcvRecentOrders);
        tvEmptyRecentOrders = view.findViewById(R.id.tvEmptyRecentOrders);

        rcvRecentOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        recentOrdersList = new ArrayList<>();
        recentOrderAdapter = new AdminOrderAdapter(getContext(), recentOrdersList);
        rcvRecentOrders.setAdapter(recentOrderAdapter);

        loadDashboardStats();
        loadRecentOrders();

        return view;
    }

    private void loadDashboardStats() {
        // Đếm sản phẩm
        FirebaseDatabase.getInstance().getReference("products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvTotalProducts.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Đếm đơn hàng
        FirebaseDatabase.getInstance().getReference("orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvTotalOrders.setText(String.valueOf(snapshot.getChildrenCount()));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Đếm người dùng
        FirebaseDatabase.getInstance().getReference("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String role = snap.child("role").getValue(String.class);
                            if ("customer".equals(role)) count++;
                        }
                        tvTotalUsers.setText(String.valueOf(count));
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadRecentOrders() {
        // Lấy 2 đơn hàng mới nhất
        Query query = FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("createdAt")
                .limitToLast(2);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recentOrdersList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Order order = snap.getValue(Order.class);
                    if (order != null) {
                        recentOrdersList.add(order);
                    }
                }
                // Vì limitToLast trả về thứ tự thời gian cũ -> mới, ta cần đảo ngược lại để mới nhất lên đầu
                Collections.reverse(recentOrdersList);
                recentOrderAdapter.notifyDataSetChanged();

                if (recentOrdersList.isEmpty()) {
                    tvEmptyRecentOrders.setVisibility(View.VISIBLE);
                    rcvRecentOrders.setVisibility(View.GONE);
                } else {
                    tvEmptyRecentOrders.setVisibility(View.GONE);
                    rcvRecentOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}

