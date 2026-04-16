package vn.duonghai.client.fragments;

import android.content.Intent;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.AddProductActivity;
import vn.duonghai.client.adapters.AdminProductAdapter;
import vn.duonghai.client.models.Product;

public class AdminMenuFragment extends Fragment {

    private RecyclerView rcvManageProducts;
    private TextView tvEmptyProducts;
    private AdminProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference productsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_menu, container, false);

        rcvManageProducts = view.findViewById(R.id.rcvManageProducts);
        tvEmptyProducts = view.findViewById(R.id.tvEmptyProducts);
        FloatingActionButton fabAddProduct = view.findViewById(R.id.fabAddProduct);

        rcvManageProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        productList = new ArrayList<>();
        adapter = new AdminProductAdapter(getContext(), productList);
        rcvManageProducts.setAdapter(adapter);

        productsRef = FirebaseDatabase.getInstance().getReference("products");

        fabAddProduct.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddProductActivity.class));
        });

        android.widget.ImageButton btnManageCategories = view.findViewById(R.id.btnManageCategories);
        btnManageCategories.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), vn.duonghai.client.activities.ManageCategoryActivity.class));
        });

        loadProducts();
        return view;
    }

    private void loadProducts() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product p = snap.getValue(Product.class);
                    if (p != null) {
                        p.setId(snap.getKey());
                        productList.add(p);
                    }
                }
                adapter.notifyDataSetChanged();

                if (productList.isEmpty()) {
                    tvEmptyProducts.setVisibility(View.VISIBLE);
                    rcvManageProducts.setVisibility(View.GONE);
                } else {
                    tvEmptyProducts.setVisibility(View.GONE);
                    rcvManageProducts.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
