package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.ProductAdapter;
import vn.duonghai.client.models.Product;

import vn.duonghai.client.activities.MainActivity;

public class HomeFragment extends Fragment {

    private RecyclerView rcvFeaturedProducts;
    private ProgressBar pbHomeLoading;
    private TextView tvFeaturedTitle;
    private android.widget.Button btnSeeMoreMenu;
    
    private DatabaseReference mDatabaseProducts;
    private ProductAdapter adapter;
    private List<Product> productList;
    
    private ValueEventListener currentProductListener;
    private Query currentQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        rcvFeaturedProducts = view.findViewById(R.id.rcvFeaturedProducts);
        pbHomeLoading = view.findViewById(R.id.pbHomeLoading);
        tvFeaturedTitle = view.findViewById(R.id.tvFeaturedTitle);
        btnSeeMoreMenu = view.findViewById(R.id.btnSeeMoreMenu);

        mDatabaseProducts = FirebaseDatabase.getInstance().getReference("products");

        rcvFeaturedProducts.setHasFixedSize(true);
        rcvFeaturedProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        productList = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), productList);
        rcvFeaturedProducts.setAdapter(adapter);

        btnSeeMoreMenu.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).selectNavigationItem(R.id.nav_menu);
            }
        });

        // Fetch Top 4 Best Sellers
        loadProducts("Sản phẩm bán chạy");
        
        return view;
    }
    
    private void loadProducts(String title) {
        pbHomeLoading.setVisibility(View.VISIBLE);
        if (tvFeaturedTitle != null) {
            tvFeaturedTitle.setText(title);
        }
        
        // Remove old listener if exists
        if (currentQuery != null && currentProductListener != null) {
            currentQuery.removeEventListener(currentProductListener);
        }

        // Chỉ lấy 4 sản phẩm tiêu biểu (Bán chạy)
        currentQuery = mDatabaseProducts.limitToFirst(4);

        currentProductListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.setId(postSnapshot.getKey());
                        productList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
                pbHomeLoading.setVisibility(View.GONE);
                
                if (productList.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "Không có sản phẩm nào", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbHomeLoading.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            }
        };

        currentQuery.addValueEventListener(currentProductListener);
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentQuery != null && currentProductListener != null) {
            currentQuery.removeEventListener(currentProductListener);
        }
    }
}

