package vn.duonghai.client.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Category;
import vn.duonghai.client.utils.ImgbbUploader;

public class AddCategoryActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private TextInputEditText edtId, edtName;
    private Button btnSave;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private TextView tvTitle;

    private Uri selectedImageUri = null;
    private String editCategoryId = null;
    private String oldImageUrl = null;

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
        setContentView(R.layout.activity_add_category);

        imgPreview = findViewById(R.id.imgCategoryPreview);
        edtId = findViewById(R.id.edtCategoryId);
        edtName = findViewById(R.id.edtCategoryName);
        btnSave = findViewById(R.id.btnSaveCategory);
        progressBar = findViewById(R.id.pbAddCategory);
        btnBack = findViewById(R.id.btnBackAddCategory);
        tvTitle = findViewById(R.id.tvAddCategoryTitle);

        btnBack.setOnClickListener(v -> finish());
        findViewById(R.id.btnSelectCategoryImage).setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> handleSubmission());

        editCategoryId = getIntent().getStringExtra("CATEGORY_ID");
        if (editCategoryId != null) {
            tvTitle.setText("Cập Nhật Danh Mục");
            edtId.setText(editCategoryId);
            edtId.setEnabled(false); // ID danh mục không nên đổi
            loadCategoryData(editCategoryId);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadCategoryData(String categoryId) {
        setLoadingState(true);
        FirebaseDatabase.getInstance().getReference("categories").child(categoryId)
                .get().addOnSuccessListener(snapshot -> {
                    setLoadingState(false);
                    Category c = snapshot.getValue(Category.class);
                    if (c != null) {
                        edtName.setText(c.getName());
                        oldImageUrl = c.getImage();
                        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                            Glide.with(this).load(oldImageUrl).into(imgPreview);
                            imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                    }
                }).addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Lỗi tải data cũ", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleSubmission() {
        String id = edtId.getText().toString().trim();
        String name = edtName.getText().toString().trim();

        if (id.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ ID và Tên!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);

        if (selectedImageUri != null) {
            // Có chọn ảnh mới -> Upload rồi mới lưu
            ImgbbUploader.uploadImage(this, selectedImageUri, new ImgbbUploader.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveCategoryToFirebase(id, name, imageUrl);
                }

                @Override
                public void onError(String error) {
                    setLoadingState(false);
                    Toast.makeText(AddCategoryActivity.this, "Lỗi up ảnh: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else if (editCategoryId != null && oldImageUrl != null) {
            // Không chọn ảnh mới nhưng đang Edit -> Dùng ảnh cũ
            saveCategoryToFirebase(id, name, oldImageUrl);
        } else {
            // Trường hợp thêm mới mà không chọn ảnh
            setLoadingState(false);
            Toast.makeText(this, "Vui lòng chọn ảnh cho danh mục!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCategoryToFirebase(String id, String name, String imageUrl) {
        Category category = new Category(id, name, imageUrl);
        FirebaseDatabase.getInstance().getReference("categories").child(id)
                .setValue(category)
                .addOnCompleteListener(task -> {
                    setLoadingState(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(AddCategoryActivity.this, "Lưu danh mục thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddCategoryActivity.this, "Lỗi lưu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoadingState(boolean isLoading) {
        btnSave.setEnabled(!isLoading);
        edtId.setEnabled(editCategoryId == null && !isLoading);
        edtName.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setText(isLoading ? "Đang xử lý..." : "LƯU DANH MỤC");
    }
}
