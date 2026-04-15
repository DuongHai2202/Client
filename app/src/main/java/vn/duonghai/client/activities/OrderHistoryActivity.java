package vn.duonghai.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.OrderAdapter;
import vn.duonghai.client.models.Order;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rcvOrderHistory;
    private TextView tvEmptyOrders;
    private OrderAdapter adapter;
    private List<Order> orderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rcvOrderHistory = findViewById(R.id.rcvOrderHistory);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        ImageButton btnBack = findViewById(R.id.btnBackOrderHistory);

        btnBack.setOnClickListener(v -> finish());

        rcvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        orderList = new ArrayList<>();
        adapter = new OrderAdapter(this, orderList);
        rcvOrderHistory.setAdapter(adapter);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        // Truy vấn đơn hàng theo userId
        Query query = FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("userId").equalTo(user.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Order order = snap.getValue(Order.class);
                    if (order != null) {
                        orderList.add(order);
                    }
                }
                // Sắp xếp mới nhất lên đầu
                Collections.sort(orderList, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                adapter.notifyDataSetChanged();

                if (orderList.isEmpty()) {
                    tvEmptyOrders.setVisibility(View.VISIBLE);
                    rcvOrderHistory.setVisibility(View.GONE);
                } else {
                    tvEmptyOrders.setVisibility(View.GONE);
                    rcvOrderHistory.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
