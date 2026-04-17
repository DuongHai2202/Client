package vn.duonghai.client.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Material;

public class AddMaterialActivity extends AppCompatActivity {
    private EditText edtName, edtQty, edtUnit, edtPrice, edtThreshold;
    private Button btnSave;
    private TextView tvTitle;
    private String materialId = null;
    private DatabaseReference invRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material);

        edtName = findViewById(R.id.edtMaterialName);
        edtQty = findViewById(R.id.edtMaterialQty);
        edtUnit = findViewById(R.id.edtMaterialUnit);
        edtPrice = findViewById(R.id.edtMaterialPrice);
        edtThreshold = findViewById(R.id.edtMaterialThreshold);
        btnSave = findViewById(R.id.btnSaveMaterial);
        tvTitle = findViewById(R.id.tvAddMaterialTitle);
        
        findViewById(R.id.btnBackAddMaterial).setOnClickListener(v -> finish());

        invRef = FirebaseDatabase.getInstance().getReference("inventory");

        if (getIntent().hasExtra("MATERIAL_ID")) {
            materialId = getIntent().getStringExtra("MATERIAL_ID");
            tvTitle.setText("Sửa Nguyên Vật Liệu");
            loadMaterialData();
        }

        btnSave.setOnClickListener(v -> saveMaterial());
    }

    private void loadMaterialData() {
        if (materialId != null) {
            invRef.child(materialId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Material m = snapshot.getValue(Material.class);
                    if (m != null) {
                        edtName.setText(m.getName());
                        
                        String qtyStr = m.getQuantity() == (long)m.getQuantity() ? String.format("%d", (long)m.getQuantity()) : String.valueOf(m.getQuantity());
                        edtQty.setText(qtyStr);
                        
                        edtUnit.setText(m.getUnit());
                        
                        String priceStr = m.getImportPrice() == (long)m.getImportPrice() ? String.format("%d", (long)m.getImportPrice()) : String.valueOf(m.getImportPrice());
                        edtPrice.setText(priceStr);
                        
                        String thresholdStr = m.getThreshold() == (long)m.getThreshold() ? String.format("%d", (long)m.getThreshold()) : String.valueOf(m.getThreshold());
                        edtThreshold.setText(thresholdStr);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void saveMaterial() {
        String name = edtName.getText().toString().trim();
        String qtyStr = edtQty.getText().toString().trim();
        String unit = edtUnit.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String threshStr = edtThreshold.getText().toString().trim();

        if (name.isEmpty() || qtyStr.isEmpty() || unit.isEmpty() || priceStr.isEmpty() || threshStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double qty = Double.parseDouble(qtyStr);
            double price = Double.parseDouble(priceStr);
            double thresh = Double.parseDouble(threshStr);

            Material material = new Material(name, qty, unit, price, thresh);
            
            if (materialId == null) {
                materialId = invRef.push().getKey();
            }
            
            invRef.child(materialId).setValue(material).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddMaterialActivity.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddMaterialActivity.this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
