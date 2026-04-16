package vn.duonghai.client.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private String currentFilter = null; // null = tất cả

    private TextView tabAll, tabPending, tabConfirmed, tabPreparing, tabShipping, tabCompleted, tabCancelled;
    private TextView[] allTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        rcvOrderHistory = findViewById(R.id.rcvOrderHistory);
        tvEmptyOrders = findViewById(R.id.tvEmptyOrders);
        ImageButton btnBack = findViewById(R.id.btnBackOrderHistory);

        btnBack.setOnClickListener(v -> finish());

        rcvOrderHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, filteredOrders);
        rcvOrderHistory.setAdapter(adapter);

        // Thiết lập các tab lọc
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabConfirmed = findViewById(R.id.tabConfirmed);
        tabPreparing = findViewById(R.id.tabPreparing);
        tabShipping = findViewById(R.id.tabShipping);
        tabCompleted = findViewById(R.id.tabCompleted);
        tabCancelled = findViewById(R.id.tabCancelled);

        allTabs = new TextView[]{tabAll, tabPending, tabConfirmed, tabPreparing, tabShipping, tabCompleted, tabCancelled};

        tabAll.setOnClickListener(v -> applyFilter(null));
        tabPending.setOnClickListener(v -> applyFilter("pending"));
        tabConfirmed.setOnClickListener(v -> applyFilter("confirmed"));
        tabPreparing.setOnClickListener(v -> applyFilter("preparing"));
        tabShipping.setOnClickListener(v -> applyFilter("shipping"));
        tabCompleted.setOnClickListener(v -> applyFilter("completed"));
        tabCancelled.setOnClickListener(v -> applyFilter("cancelled"));

        // Highlight tab mặc định
        highlightTab(tabAll);

        loadOrders();
    }

    private void applyFilter(String status) {
        currentFilter = status;
        filteredOrders.clear();

        if (status == null) {
            filteredOrders.addAll(allOrders);
            highlightTab(tabAll);
        } else {
            for (Order o : allOrders) {
                if (status.equals(o.getStatus())) {
                    filteredOrders.add(o);
                }
            }
            // Highlight tab tương ứng
            switch (status) {
                case "pending": highlightTab(tabPending); break;
                case "confirmed": highlightTab(tabConfirmed); break;
                case "preparing": highlightTab(tabPreparing); break;
                case "shipping": highlightTab(tabShipping); break;
                case "completed": highlightTab(tabCompleted); break;
                case "cancelled": highlightTab(tabCancelled); break;
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredOrders.isEmpty()) {
            tvEmptyOrders.setVisibility(View.VISIBLE);
            rcvOrderHistory.setVisibility(View.GONE);
        } else {
            tvEmptyOrders.setVisibility(View.GONE);
            rcvOrderHistory.setVisibility(View.VISIBLE);
        }
    }

    private void highlightTab(TextView activeTab) {
        for (TextView tab : allTabs) {
            tab.setTextColor(0xFF8D6E63);
            tab.setTypeface(null, Typeface.NORMAL);
            tab.setBackgroundColor(0x00000000); // trong suốt
        }
        activeTab.setTextColor(0xFF4E342E);
        activeTab.setTypeface(null, Typeface.BOLD);
        // Thêm gạch chân bằng underline background
        activeTab.setBackgroundResource(0);
        activeTab.setPaintFlags(activeTab.getPaintFlags()); // giữ nguyên
    }

    private void loadOrders() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        Query query = FirebaseDatabase.getInstance().getReference("orders")
                .orderByChild("userId").equalTo(user.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Order order = snap.getValue(Order.class);
                    if (order != null) {
                        allOrders.add(order);
                    }
                }
                Collections.sort(allOrders, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                applyFilter(currentFilter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryActivity.this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
