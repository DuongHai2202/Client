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
import vn.duonghai.client.adapters.CategoryAdapter;
import vn.duonghai.client.adapters.ProductAdapter;
import vn.duonghai.client.models.Category;
import vn.duonghai.client.models.Product;

public class HomeFragment extends Fragment {

    private RecyclerView rcvFeaturedProducts;
    private RecyclerView rcvCategories;
    private ProgressBar pbHomeLoading;
    
    private DatabaseReference mDatabase;
    private ProductAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private List<Product> productList;
    private List<Category> categoryList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        rcvFeaturedProducts = view.findViewById(R.id.rcvFeaturedProducts);
        rcvCategories = view.findViewById(R.id.rcvCategories);
        pbHomeLoading = view.findViewById(R.id.pbHomeLoading);

        mDatabase = FirebaseDatabase.getInstance().getReference("products");

        // Use vertical layout
        rcvFeaturedProducts.setHasFixedSize(true);
        rcvFeaturedProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup Categories
        rcvCategories.setHasFixedSize(true);
        rcvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryList = new ArrayList<>();
        // Tạo dữ liệu mồi cho Categories
        categoryList.add(new Category("c1", "Trà Sữa", android.R.drawable.ic_btn_speak_now));
        categoryList.add(new Category("c2", "Cà Phê", android.R.drawable.ic_menu_agenda));
        categoryList.add(new Category("c3", "Sinh Tố", android.R.drawable.ic_menu_gallery));
        categoryList.add(new Category("c4", "Ăn Vặt", android.R.drawable.ic_menu_compass));
        
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
        rcvCategories.setAdapter(categoryAdapter);

        // Khởi tạo List Sản phẩm nổi bật
        productList = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), productList);
        rcvFeaturedProducts.setAdapter(adapter);

        // Fetch Featured Products
        loadFeaturedProducts();
        
        return view;
    }
    
    private void loadFeaturedProducts() {
        pbHomeLoading.setVisibility(View.VISIBLE);
        
        // Limit query to 5 items to show as "Featured"
        mDatabase.limitToFirst(5).addValueEventListener(new ValueEventListener() {
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
                    Toast.makeText(getContext(), "Chưa có đồ uống nào nổi bật!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbHomeLoading.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
