package vn.duonghai.client.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Locale;

import vn.duonghai.client.R;

public class PaymentQRActivity extends AppCompatActivity {
    private static final String BANK_ID = "bidv";
    private static final String ACCOUNT_NO = "5012169430";
    private static final String ACCOUNT_NAME = "DUONG VAN HAI";
    private static final String BANK_DISPLAY = "BIDV";
    private static final long PAYMENT_TIMEOUT_MS = 5 * 60 * 1000; // 5 phút

    private ImageView imgQRCode;
    private ProgressBar pbLoadingQR;
    private TextView tvBankName, tvAccountNo, tvAccountName, tvQRAmount, tvTransferContent;
    private TextView tvCountdown;

    private String orderId;
    private double totalAmount;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");
    private CountDownTimer countDownTimer;
    private boolean isConfirmed = false;
    
    private DatabaseReference orderStatusRef;
    private ValueEventListener statusListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_qr);

        orderId = getIntent().getStringExtra("ORDER_ID");
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0);

        imgQRCode = findViewById(R.id.imgQRCode);
        pbLoadingQR = findViewById(R.id.pbLoadingQR);
        tvBankName = findViewById(R.id.tvBankName);
        tvAccountNo = findViewById(R.id.tvAccountNo);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvQRAmount = findViewById(R.id.tvQRAmount);
        tvTransferContent = findViewById(R.id.tvTransferContent);
        tvCountdown = findViewById(R.id.tvCountdown);
        ImageButton btnBack = findViewById(R.id.btnBackQR);

        btnBack.setOnClickListener(v -> showExitConfirmDialog());

        tvBankName.setText(BANK_DISPLAY);
        tvAccountNo.setText(ACCOUNT_NO);
        tvAccountName.setText(ACCOUNT_NAME);
        tvQRAmount.setText(formatter.format(totalAmount) + " đ");

        String shortOrderId = orderId;
        if (shortOrderId != null && shortOrderId.length() > 8) {
            shortOrderId = shortOrderId.substring(shortOrderId.length() - 8).toUpperCase();
        }
        String transferContent = "DH " + shortOrderId;
        tvTransferContent.setText(transferContent);

        loadVietQR((int) totalAmount, transferContent);
        // Bắt đầu đếm ngược 5 phút
        startCountdown();
        // Tự động đợi admin xác nhận
        listenForPaymentConfirmation();
        // Xử lý nút Back hệ thống
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmDialog();
            }
        });
    }

    private void listenForPaymentConfirmation() {
        if (orderId == null) return;
        
        orderStatusRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId).child("status");
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                // Nếu Admin đổi thành "confirmed" (Đã xác nhận thanh toán & nhận đơn)
                if ("confirmed".equals(status)) {
                    isConfirmed = true;
                    if (countDownTimer != null) countDownTimer.cancel();
                    
                    Toast.makeText(PaymentQRActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                    finish(); // Trở về màn hình trước
                } else if ("cancelled".equals(status)) {
                    // Admin hủy đơn
                    isConfirmed = true;
                    if (countDownTimer != null) countDownTimer.cancel();
                    Toast.makeText(PaymentQRActivity.this, "Đơn hàng bị hủy.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        };
        orderStatusRef.addValueEventListener(statusListener);
    }

    private void startCountdown() {
        countDownTimer = new CountDownTimer(PAYMENT_TIMEOUT_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                String timeText = String.format(Locale.getDefault(), "⏱ Còn lại: %02d:%02d", minutes, seconds);
                tvCountdown.setText(timeText);

                // Đổi sang đỏ khi còn dưới 1 phút
                if (millisUntilFinished < 60000) {
                    tvCountdown.setTextColor(0xFFD32F2F);
                }
            }

            @Override
            public void onFinish() {
                if (!isConfirmed) {
                    // Hết giờ → Tự động hủy đơn hàng
                    tvCountdown.setText("Đã hết thời gian thanh toán! Vui lòng đặt lại đơn hàng");
                    tvCountdown.setTextColor(0xFFD32F2F);
                    cancelOrder();
                }
            }
        }.start();
    }

    private void cancelOrder() {
        if (orderId != null) {
            FirebaseDatabase.getInstance().getReference("orders")
                    .child(orderId).child("status").setValue("cancelled")
                    .addOnSuccessListener(unused -> {
                        new AlertDialog.Builder(this)
                                .setTitle("Hết thời gian thanh toán")
                                .setMessage("Đơn hàng đã bị hủy do không nhận được thanh toán trong 5 phút.\n\nVui lòng đặt lại nếu bạn muốn tiếp tục.")
                                .setPositiveButton("Đã hiểu", (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                    });
        }
    }

    private void showExitConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy thanh toán?")
                .setMessage("Nếu bạn thoát, đơn hàng sẽ bị hủy. Bạn có chắc chắn?")
                .setPositiveButton("Hủy đơn", (d, w) -> {
                    isConfirmed = true; // Set true de onFinish không gọi nữa
                    if (countDownTimer != null) countDownTimer.cancel();
                    cancelOrder();
                })
                .setNegativeButton("Ở lại", null)
                .show();
    }

    private void loadVietQR(int amount, String addInfo) {
        String qrUrl = "https://img.vietqr.io/image/"
                + BANK_ID + "-" + ACCOUNT_NO + "-compact2.png"
                + "?amount=" + amount
                + "&addInfo=" + addInfo
                + "&accountName=" + ACCOUNT_NAME.replace(" ", "%20");

        Glide.with(this)
                .load(qrUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(imgQRCode);

        imgQRCode.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            pbLoadingQR.setVisibility(View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (orderStatusRef != null && statusListener != null) {
            orderStatusRef.removeEventListener(statusListener);
        }
    }
}
