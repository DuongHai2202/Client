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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.CategoryAdapter;
import vn.duonghai.client.adapters.ProductAdapter;
import vn.duonghai.client.models.Category;
import vn.duonghai.client.models.Product;

public class MenuFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvProducts;
    private RecyclerView rvCategoriesMenu;
    private ProgressBar progressBar;
    
    private DatabaseReference mDatabaseProducts;
    private DatabaseReference mDatabaseCategories;
    
    private ProductAdapter adapter;
    private CategoryAdapter categoryAdapter;
    
    private List<Product> productList;
    private List<Category> categoryList;

    private ValueEventListener currentProductListener;
    private Query currentQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        
        rvProducts = view.findViewById(R.id.rvProducts);
        rvCategoriesMenu = view.findViewById(R.id.rvCategoriesMenu);
        progressBar = view.findViewById(R.id.progressBar);

        mDatabaseProducts = FirebaseDatabase.getInstance().getReference("products");
        mDatabaseCategories = FirebaseDatabase.getInstance().getReference("categories");

        rvProducts.setHasFixedSize(true);
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        
        rvCategoriesMenu.setHasFixedSize(true);
        rvCategoriesMenu.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(getContext(), 3));

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, this);
        rvCategoriesMenu.setAdapter(categoryAdapter);

        productList = new ArrayList<>();
        adapter = new ProductAdapter(getContext(), productList);
        rvProducts.setAdapter(adapter);

        // Nút "Tất cả" danh mục giả để reset bộ lọc
        Category allCategory = new Category("all", "Tất Cả", "https://i.ibb.co/68fDkHj/all-icon.png");

        // Tải Danh Mục
        mDatabaseCategories.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                categoryList.add(allCategory); // Luôn có mục Tất cả ở đầu
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Category cat = snap.getValue(Category.class);
                    if (cat != null) {
                        cat.setId(snap.getKey());
                        categoryList.add(cat);
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Điểm khác biệt với Home: load ALL products lúc đầu thay vì 20 cái
        loadProducts(null);

        return view;
    }

    private void loadProducts(String filterCategoryId) {
        progressBar.setVisibility(View.VISIBLE);
        
        if (currentQuery != null && currentProductListener != null) {
            currentQuery.removeEventListener(currentProductListener);
        }

        if (filterCategoryId == null || filterCategoryId.equals("all")) {
            currentQuery = mDatabaseProducts; // Lấy toàn bộ thực đơn
        } else {
            currentQuery = mDatabaseProducts.orderByChild("categoryId").equalTo(filterCategoryId);
        }

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
                progressBar.setVisibility(View.GONE);
                
                if (productList.isEmpty() && getContext() != null) {
                    Toast.makeText(getContext(), "Không có sản phẩm nào trong danh mục này", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải Menu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        currentQuery.addValueEventListener(currentProductListener);
    }

    @Override
    public void onCategoryClick(Category category) {
        loadProducts(category.getId());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentQuery != null && currentProductListener != null) {
            currentQuery.removeEventListener(currentProductListener);
        }
    }
}
