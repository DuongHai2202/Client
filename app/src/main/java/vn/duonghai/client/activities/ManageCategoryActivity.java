package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import vn.duonghai.client.adapters.AdminCategoryAdapter;
import vn.duonghai.client.models.Category;

public class ManageCategoryActivity extends AppCompatActivity {

    private RecyclerView rcvManageCategories;
    private AdminCategoryAdapter adapter;
    private List<Category> categoryList;
    private DatabaseReference categoriesRef;
    private ImageButton btnBack;
    private FloatingActionButton fabAddCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_category);

        rcvManageCategories = findViewById(R.id.rcvManageCategories);
        btnBack = findViewById(R.id.btnBackCategory);
        fabAddCategory = findViewById(R.id.fabAddCategory);

        rcvManageCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryList = new ArrayList<>();
        adapter = new AdminCategoryAdapter(this, categoryList);
        rcvManageCategories.setAdapter(adapter);

        categoriesRef = FirebaseDatabase.getInstance().getReference("categories");

        btnBack.setOnClickListener(v -> finish());
        fabAddCategory.setOnClickListener(v -> startActivity(new Intent(this, AddCategoryActivity.class)));

        loadCategories();
    }

    private void loadCategories() {
        categoriesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Category c = snap.getValue(Category.class);
                    if (c != null) {
                        if (c.getId() == null) c.setId(snap.getKey());
                        categoryList.add(c);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
