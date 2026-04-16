package vn.duonghai.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Category;
import vn.duonghai.client.models.Product;
import vn.duonghai.client.utils.ImgbbUploader;

public class AddProductActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextInputEditText edtProductName, edtDescription;
    private Spinner spinnerCategory;
    private TextInputEditText edtBasePrice, edtPriceM, edtPriceL;
    private TextInputEditText edtToppings, edtSugar, edtIce;
    
    private CheckBox cbIsAvailable;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private CardView btnSelectImage;
    private ImageButton btnBack;
    private android.widget.TextView tvTitle;

    private Uri selectedImageUri = null;
    private String editProductId = null;
    private String oldImageUrl = null;
    private String oldCategoryId = null;

    private List<Category> categoryList = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    imgPreview.setImageURI(selectedImageUri);
                    imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        imgPreview = findViewById(R.id.imgPreview);
        edtProductName = findViewById(R.id.edtProductName);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        edtBasePrice = findViewById(R.id.edtBasePrice);
        edtPriceM = findViewById(R.id.edtPriceM);
        edtPriceL = findViewById(R.id.edtPriceL);
        edtToppings = findViewById(R.id.edtToppings);
        edtSugar = findViewById(R.id.edtSugar);
        edtIce = findViewById(R.id.edtIce);
        
        cbIsAvailable = findViewById(R.id.cbIsAvailable);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnBack = findViewById(R.id.btnBackAddProduct);
        tvTitle = findViewById(R.id.tvAddProductTitle);

        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        loadCategories();

        btnBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> handleSubmission());

        editProductId = getIntent().getStringExtra("PRODUCT_ID");
        if (editProductId != null) {
            tvTitle.setText("Cập Nhật Món Nước");
            btnSubmit.setText("CẬP NHẬT MÓN");
            loadProductDataForEdit(editProductId);
        }
    }

    private void loadCategories() {
        FirebaseDatabase.getInstance().getReference("categories")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        categoryList.clear();
                        categoryNames.clear();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Category cat = snap.getValue(Category.class);
                            if (cat != null) {
                                cat.setId(snap.getKey());
                                categoryList.add(cat);
                                categoryNames.add(cat.getName());
                            }
                        }
                        categoryAdapter.notifyDataSetChanged();

                        // Nếu đang edit, set lại selected item
                        if (oldCategoryId != null) {
                            setSpinnerSelection(oldCategoryId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddProductActivity.this, "Lỗi tải danh mục: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setSpinnerSelection(String categoryId) {
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).getId().equals(categoryId)) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void loadProductDataForEdit(String productId) {
        setLoadingState(true);
        FirebaseDatabase.getInstance().getReference("products").child(productId)
            .get().addOnSuccessListener(snapshot -> {
                setLoadingState(false);
                Product p = snapshot.getValue(Product.class);
                if (p != null) {
                    edtProductName.setText(p.getName());
                    edtDescription.setText(p.getDescription());
                    cbIsAvailable.setChecked(p.getIsAvailable());
                    
                    oldCategoryId = p.getCategoryId();
                    if (!categoryList.isEmpty() && oldCategoryId != null) {
                        setSpinnerSelection(oldCategoryId);
                    }
                    
                    oldImageUrl = p.getImage();
                    if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                        com.bumptech.glide.Glide.with(this).load(oldImageUrl).into(imgPreview);
                    }
                    
                    if (p.getSizes() != null) {
                        if (p.getSizes().containsKey("S")) edtBasePrice.setText(String.valueOf((int)p.getSizes().get("S").getPrice()));
                        if (p.getSizes().containsKey("M")) edtPriceM.setText(String.valueOf((int)p.getSizes().get("M").getPrice()));
                        if (p.getSizes().containsKey("L")) edtPriceL.setText(String.valueOf((int)p.getSizes().get("L").getPrice()));
                    }
                    if (p.getAvailableToppings() != null) edtToppings.setText(android.text.TextUtils.join(", ", p.getAvailableToppings()));
                    if (p.getSugarOptions() != null) edtSugar.setText(android.text.TextUtils.join(", ", p.getSugarOptions()));
                    if (p.getIceOptions() != null) edtIce.setText(android.text.TextUtils.join(", ", p.getIceOptions()));
                }
            }).addOnFailureListener(e -> {
                setLoadingState(false);
                Toast.makeText(this, "Lỗi khi tải dữ liệu cũ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleSubmission() {
        String name = edtProductName.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        
        int selectedIndex = spinnerCategory.getSelectedItemPosition();
        if (selectedIndex == -1 || categoryList.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải danh mục hoặc thêm danh mục trước!", Toast.LENGTH_SHORT).show();
            return;
        }
        String categoryId = categoryList.get(selectedIndex).getId();
        
        String priceStr = edtBasePrice.getText().toString().trim();
        String priceMStr = edtPriceM.getText().toString().trim();
        String priceLStr = edtPriceL.getText().toString().trim();
        
        String toppingsStr = edtToppings.getText().toString().trim();
        String sugarStr = edtSugar.getText().toString().trim();
        String iceStr = edtIce.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên và Giá S (Cơ bản)!", Toast.LENGTH_SHORT).show();
            return;
        }

        double basePrice;
        try {
            basePrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá tiền phải là con số!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            if (editProductId != null && oldImageUrl != null) {
                // Giữ nguyên ảnh cũ, bỏ qua upload ImgBB
                setLoadingState(true);
                saveProductToFirebase(name, description, categoryId, basePrice, priceMStr, priceLStr, 
                                        toppingsStr, sugarStr, iceStr, oldImageUrl, cbIsAvailable.isChecked());
                return;
            } else {
                Toast.makeText(this, "Vui lòng chọn 1 bức ảnh đại diện!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        setLoadingState(true);

        ImgbbUploader.uploadImage(this, selectedImageUri, new ImgbbUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveProductToFirebase(name, description, categoryId, basePrice, priceMStr, priceLStr, 
                                        toppingsStr, sugarStr, iceStr, imageUrl, cbIsAvailable.isChecked());
            }

            @Override
            public void onError(String error) {
                setLoadingState(false);
                Toast.makeText(AddProductActivity.this, "Lỗi up ảnh: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProductToFirebase(String name, String description, String category, 
                                       double basePrice, String priceMStr, String priceLStr,
                                       String toppingsStr, String sugarStr, String iceStr, 
                                       String imageUrl, boolean isAvailable) {
        
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");
        String finalProductId = (editProductId != null) ? editProductId : databaseRef.push().getKey();

        if (finalProductId == null) {
            setLoadingState(false);
            Toast.makeText(this, "Lỗi Database (Không tạo được Node Mới)", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.setId(finalProductId);
        product.setName(name);
        product.setDescription(description.isEmpty() ? "Món nước ngon tuyệt hảo!" : description);
        product.setCategoryId(category);
        product.setImage(imageUrl);
        product.setIsAvailable(isAvailable);

        // 1. Phân rã SIZE
        Map<String, Product.SizeOption> sizesMap = new HashMap<>();
        sizesMap.put("S", new Product.SizeOption(basePrice));
        if (!priceMStr.isEmpty()) {
            try { sizesMap.put("M", new Product.SizeOption(Double.parseDouble(priceMStr))); } catch (Exception ignored) {}
        }
        if (!priceLStr.isEmpty()) {
            try { sizesMap.put("L", new Product.SizeOption(Double.parseDouble(priceLStr))); } catch (Exception ignored) {}
        }
        product.setSizes(sizesMap);

        // 2. Phân rã Thuộc Tính Mở Rộng
        product.setAvailableToppings(parseCommaString(toppingsStr));
        product.setSugarOptions(parseCommaString(sugarStr.isEmpty() ? "0%,50%,100%" : sugarStr));
        product.setIceOptions(parseCommaString(iceStr.isEmpty() ? "Không Đá,Biết Đá,Nhiều Đá" : iceStr));

        databaseRef.child(finalProductId).setValue(product)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        String msg = (editProductId != null) ? "Cập nhật thành công!" : "Đăng món nước thành công rực rỡ!";
                        Toast.makeText(AddProductActivity.this, msg, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Lỗi lưu DB: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<String> parseCommaString(String input) {
        List<String> list = new ArrayList<>();
        if (input == null || input.isEmpty()) return list;
        
        String[] parts = input.split(",");
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                list.add(p.trim());
            }
        }
        return list;
    }

    private void setLoadingState(boolean isLoading) {
        btnSubmit.setEnabled(!isLoading);
        btnSelectImage.setEnabled(!isLoading);
        edtProductName.setEnabled(!isLoading);
        edtBasePrice.setEnabled(!isLoading);
        spinnerCategory.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setText(isLoading ? "Đang xử lý..." : "Tải ảnh lên");
    }
}
