package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.ProductAdapter;
import vn.duonghai.client.models.Product;

public class MenuFragment extends Fragment {

    private RecyclerView rvProducts;
    private ProgressBar progressBar;
    
    private DatabaseReference mDatabase;
    private ProductAdapter adapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        
        rvProducts = view.findViewById(R.id.rvProducts);
        progressBar = view.findViewById(R.id.progressBar);

        mDatabase = FirebaseDatabase.getInstance().getReference("products");

        rvProducts.setHasFixedSize(true);
        rvProducts.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        
        productList = new ArrayList<>();
        // Truyền getContext() thay vì 'this'
        adapter = new ProductAdapter(getContext(), productList);
        rvProducts.setAdapter(adapter);

        loadProducts();

        return view;
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        
        mDatabase.addValueEventListener(new ValueEventListener() {
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
                progressBar.setVisibility(View.GONE);
                
                if (productList.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "Chưa có đồ uống nào!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải Menu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
