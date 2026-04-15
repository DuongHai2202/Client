package vn.duonghai.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Product;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgDetailProduct;
    private TextView tvDetailName, tvDetailPrice, tvDetailDescription, tvDetailCategories;
    private Button btnOpenOptions;
    
    private DecimalFormat formatter = new DecimalFormat("###,###,###");
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        imgDetailProduct = findViewById(R.id.imgDetailProduct);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailPrice = findViewById(R.id.tvDetailPrice);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailCategories = findViewById(R.id.tvDetailCategories);
        btnOpenOptions = findViewById(R.id.btnOpenOptions);
        
        ImageButton btnBack = findViewById(R.id.btnBackDetail);
        btnBack.setOnClickListener(v -> finish());

        // Quay lại trang trước để thêm giỏ (do popup chung nằm ở Adapter)
        btnOpenOptions.setOnClickListener(v -> {
            Toast.makeText(this, "Hãy thoát ra và bấm dấu [+] để thêm món nhé!", Toast.LENGTH_SHORT).show();
            finish();
        });

        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId != null) {
            loadProductDetails();
        } else {
            Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadProductDetails() {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        tvDetailName.setText(product.getName());
                        tvDetailPrice.setText(formatter.format(product.getBasePrice()) + " đ");
                        tvDetailDescription.setText(product.getDescription() != null ? product.getDescription() : "Chưa có mô tả chi tiết cho sản phẩm này.");
                        
                        Glide.with(ProductDetailActivity.this)
                                .load(product.getImage())
                                .centerCrop()
                                .into(imgDetailProduct);
                                
                        // Thêm logic get Tên category nếu muốn
                        tvDetailCategories.setText("Sản phẩm xịn của quán");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
