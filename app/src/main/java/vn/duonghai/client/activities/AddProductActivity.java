package vn.duonghai.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Product;
import vn.duonghai.client.utils.ImgbbUploader;

public class AddProductActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextInputEditText edtProductName, edtCategory, edtBasePrice;
    private CheckBox cbIsAvailable;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private CardView btnSelectImage;
    private ImageButton btnBack;

    private Uri selectedImageUri = null;

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
        edtCategory = findViewById(R.id.edtCategory);
        edtBasePrice = findViewById(R.id.edtBasePrice);
        cbIsAvailable = findViewById(R.id.cbIsAvailable);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnBack = findViewById(R.id.btnBackAddProduct);

        btnBack.setOnClickListener(v -> finish());
        btnSelectImage.setOnClickListener(v -> openGallery());
        btnSubmit.setOnClickListener(v -> handleSubmission());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleSubmission() {
        String name = edtProductName.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();
        String priceStr = edtBasePrice.getText().toString().trim();

        if (name.isEmpty() || category.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên, Mã danh mục và Giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng chọn 1 bức ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        double basePrice;
        try {
            basePrice = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá tiền phải là con số!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        ImgbbUploader.uploadImage(this, selectedImageUri, new ImgbbUploader.UploadCallback() {
            @Override
            public void onSuccess(String imageUrl) {
                saveProductToFirebase(name, category, basePrice, imageUrl, cbIsAvailable.isChecked());
            }

            @Override
            public void onError(String error) {
                setLoadingState(false);
                Toast.makeText(AddProductActivity.this, "Lỗi up ảnh: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProductToFirebase(String name, String category, double basePrice, String imageUrl, boolean isAvailable) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("products");
        String pushId = databaseRef.push().getKey();

        if (pushId == null) {
            setLoadingState(false);
            Toast.makeText(this, "Lỗi Database", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product();
        product.setId(pushId);
        product.setName(name);
        product.setCategoryId(category);
        product.setImage(imageUrl);
        product.setAvailable(isAvailable);

        Map<String, Product.SizeOption> sizesMap = new HashMap<>();
        sizesMap.put("S", new Product.SizeOption(basePrice));
        product.setSizes(sizesMap);

        product.setAvailableToppings(new ArrayList<>());
        product.setSugarOptions(new ArrayList<>());
        product.setIceOptions(new ArrayList<>());

        databaseRef.child(pushId).setValue(product)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AddProductActivity.this, "Đăng món nước thành công!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddProductActivity.this, "Lỗi lưu DB: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        btnSubmit.setEnabled(!isLoading);
        btnSelectImage.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSubmit.setText(isLoading ? "ĐANG XỬ LÝ..." : "TẢI ẢNH LÊN & ĐĂNG MÓN");
    }
}
