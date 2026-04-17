package vn.duonghai.client.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vn.duonghai.client.databinding.ActivityReviewBinding;
import vn.duonghai.client.models.Review;

public class ReviewActivity extends AppCompatActivity {

    private ActivityReviewBinding binding;
    private DatabaseReference reviewRef;
    private FirebaseAuth mAuth;

    private String productId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        reviewRef = FirebaseDatabase.getInstance().getReference("reviews");

        productId = getIntent().getStringExtra("productId");
        orderId = getIntent().getStringExtra("orderId");

        binding.btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        int rating = (int) binding.ratingBar.getRating();
        String comment = binding.edtComment.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Chọn số sao", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        String reviewId = reviewRef.push().getKey();

        Review review = new Review(
                reviewId,
                userId,
                productId,
                orderId,
                rating,
                comment,
                System.currentTimeMillis()
        );

        reviewRef.child(reviewId).setValue(review)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}