package vn.duonghai.client.fragments;

import android.graphics.Typeface;
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
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private String currentFilter = null;

    private TextView tabAll, tabPending, tabConfirmed, tabPreparing, tabShipping, tabCompleted, tabCancelled;
    private TextView[] allTabs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        rcvAdminOrders = view.findViewById(R.id.rcvAdminOrders);
        tvEmptyAdminOrders = view.findViewById(R.id.tvEmptyAdminOrders);

        rcvAdminOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminOrderAdapter(getContext(), filteredOrders);
        rcvAdminOrders.setAdapter(adapter);

        // Thiết lập các tab lọc
        tabAll = view.findViewById(R.id.tabAdminAll);
        tabPending = view.findViewById(R.id.tabAdminPending);
        tabConfirmed = view.findViewById(R.id.tabAdminConfirmed);
        tabPreparing = view.findViewById(R.id.tabAdminPreparing);
        tabShipping = view.findViewById(R.id.tabAdminShipping);
        tabCompleted = view.findViewById(R.id.tabAdminCompleted);
        tabCancelled = view.findViewById(R.id.tabAdminCancelled);

        allTabs = new TextView[]{tabAll, tabPending, tabConfirmed, tabPreparing, tabShipping, tabCompleted, tabCancelled};

        tabAll.setOnClickListener(v -> applyFilter(null));
        tabPending.setOnClickListener(v -> applyFilter("pending"));
        tabConfirmed.setOnClickListener(v -> applyFilter("confirmed"));
        tabPreparing.setOnClickListener(v -> applyFilter("preparing"));
        tabShipping.setOnClickListener(v -> applyFilter("shipping"));
        tabCompleted.setOnClickListener(v -> applyFilter("completed"));
        tabCancelled.setOnClickListener(v -> applyFilter("cancelled"));

        highlightTab(tabAll);

        loadAllOrders();
        return view;
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
            switch (status) {
                case "pending": highlightTab(tabPending);
                break;
                case "confirmed": highlightTab(tabConfirmed);
                break;
                case "preparing": highlightTab(tabPreparing);
                break;
                case "shipping": highlightTab(tabShipping);
                break;
                case "completed": highlightTab(tabCompleted);
                break;
                case "cancelled": highlightTab(tabCancelled);
                break;
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredOrders.isEmpty()) {
            tvEmptyAdminOrders.setVisibility(View.VISIBLE);
            rcvAdminOrders.setVisibility(View.GONE);
        } else {
            tvEmptyAdminOrders.setVisibility(View.GONE);
            rcvAdminOrders.setVisibility(View.VISIBLE);
        }
    }

    private void highlightTab(TextView activeTab) {
        for (TextView tab : allTabs) {
            tab.setTextColor(0xFF8D6E63);
            tab.setTypeface(null, Typeface.NORMAL);
        }
        activeTab.setTextColor(0xFF4E342E);
        activeTab.setTypeface(null, Typeface.BOLD);
    }

    private void loadAllOrders() {
        FirebaseDatabase.getInstance().getReference("orders")
                .addValueEventListener(new ValueEventListener() {
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
                        if (getContext() != null)
                            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
