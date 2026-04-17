package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import vn.duonghai.client.adapters.AdminInventoryAdapter;
import vn.duonghai.client.models.Material;

public class AdminInventoryActivity extends AppCompatActivity {

    private RecyclerView rvInventory;
    private AdminInventoryAdapter adapter;
    private List<Material> materialList;
    private List<Material> displayList;
    private DatabaseReference invRef;
    private EditText edtSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inventory);

        rvInventory = findViewById(R.id.rvInventory);
        edtSearch = findViewById(R.id.edtSearchMaterial);
        
        rvInventory.setLayoutManager(new LinearLayoutManager(this));

        materialList = new ArrayList<>();
        displayList = new ArrayList<>();
        adapter = new AdminInventoryAdapter(this, displayList);
        rvInventory.setAdapter(adapter);

        findViewById(R.id.btnBackInventory).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnAddMaterial).setOnClickListener(v -> {
            startActivity(new Intent(AdminInventoryActivity.this, AddMaterialActivity.class));
        });

        invRef = FirebaseDatabase.getInstance().getReference("inventory");
        loadInventory();

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadInventory() {
        invRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                materialList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Material material = ds.getValue(Material.class);
                    if (material != null) {
                        material.setId(ds.getKey());
                        materialList.add(material);
                    }
                }
                filter(edtSearch.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminInventoryActivity.this, "Lỗi tải kho", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filter(String text) {
        displayList.clear();
        if (text.isEmpty()) {
            displayList.addAll(materialList);
        } else {
            text = text.toLowerCase();
            for (Material item : materialList) {
                if (item.getName().toLowerCase().contains(text)) {
                    displayList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
