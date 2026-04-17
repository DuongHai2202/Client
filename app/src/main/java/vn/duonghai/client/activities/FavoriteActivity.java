package vn.duonghai.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

public class FavoriteActivity extends AppCompatActivity {
    private RecyclerView rcvFavorites;
    private ProductAdapter adapter;
    private List<Product> favProducts;
    private TextView tvEmptyFavorites;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Ánh xạ
        rcvFavorites = findViewById(R.id.rcvFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);
        btnBack = findViewById(R.id.btnBackFavorite);

        // Thiết lập RecyclerView
        rcvFavorites.setLayoutManager(new LinearLayoutManager(this));
        favProducts = new ArrayList<>();
        adapter = new ProductAdapter(this, favProducts);
        rcvFavorites.setAdapter(adapter);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        loadFavoriteProducts();
    }

    private void loadFavoriteProducts() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("favorites");
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products");

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                favProducts.clear();
                if (!snapshot.exists()) {
                    adapter.notifyDataSetChanged();
                    tvEmptyFavorites.setVisibility(View.VISIBLE);
                    return;
                }

                tvEmptyFavorites.setVisibility(View.GONE);
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String productId = snap.getKey();
                    productRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot pSnap) {
                            Product p = pSnap.getValue(Product.class);
                            if (p != null) {
                                p.setId(pSnap.getKey());
                                favProducts.add(p);
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }
}
