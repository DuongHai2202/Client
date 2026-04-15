package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.AdminOrderAdapter;
import vn.duonghai.client.models.Order;

public class AdminOrdersFragment extends Fragment {

    private RecyclerView rcvAdminOrders;
    private TextView tvEmptyAdminOrders;
    private AdminOrderAdapter adapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        rcvAdminOrders = view.findViewById(R.id.rcvAdminOrders);
        tvEmptyAdminOrders = view.findViewById(R.id.tvEmptyAdminOrders);

        rcvAdminOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        orderList = new ArrayList<>();
        adapter = new AdminOrderAdapter(getContext(), orderList);
        rcvAdminOrders.setAdapter(adapter);

        loadAllOrders();
        return view;
    }

    private void loadAllOrders() {
        // Lắng nghe TOÀN BỘ đơn hàng realtime — khi khách đặt, admin thấy ngay
        FirebaseDatabase.getInstance().getReference("orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Order order = snap.getValue(Order.class);
                            if (order != null) {
                                orderList.add(order);
                            }
                        }
                        // Mới nhất lên đầu
                        Collections.sort(orderList, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                        adapter.notifyDataSetChanged();

                        if (orderList.isEmpty()) {
                            tvEmptyAdminOrders.setVisibility(View.VISIBLE);
                            rcvAdminOrders.setVisibility(View.GONE);
                        } else {
                            tvEmptyAdminOrders.setVisibility(View.GONE);
                            rcvAdminOrders.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
