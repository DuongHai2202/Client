package vn.duonghai.client.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.OrderDetailAdapter;
import vn.duonghai.client.models.CartItem;
import vn.duonghai.client.models.Order;

public class OrderDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rcvOrderDetail;
    private OrderDetailAdapter adapter;
    private List<CartItem> itemList;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        toolbar = findViewById(R.id.toolbarOrderDetail);
        rcvOrderDetail = findViewById(R.id.rcvOrderDetail);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        itemList = new ArrayList<>();
        rcvOrderDetail.setLayoutManager(new LinearLayoutManager(this));

        loadOrderDetails();
    }

    private void loadOrderDetails() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Order order = snapshot.getValue(Order.class);
                    if (order != null && order.getItems() != null) {
                        itemList.clear();
                        itemList.addAll(order.getItems());

                        boolean isCompleted = "completed".equalsIgnoreCase(order.getStatus());
                        adapter = new OrderDetailAdapter(OrderDetailActivity.this, itemList, orderId, isCompleted);
                        rcvOrderDetail.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
