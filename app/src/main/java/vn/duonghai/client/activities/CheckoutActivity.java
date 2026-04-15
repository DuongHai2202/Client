package vn.duonghai.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Address;
import vn.duonghai.client.models.CartItem;
import vn.duonghai.client.models.Order;

public class CheckoutActivity extends AppCompatActivity {

    private TextView tvCheckoutReceiver, tvCheckoutAddress, tvCheckoutPhone;
    private LinearLayout llOrderItems;
    private TextView tvCheckoutSubtotal, tvCheckoutShipping, tvCheckoutTotal;
    private RadioGroup rgPayment;
    private EditText edtNote;
    private Button btnPlaceOrder;
    private ProgressBar pbCheckout;
    private ImageButton btnBack;

    private FirebaseUser currentUser;
    private DatabaseReference cartRef, addressRef, ordersRef, userRef;
    private List<CartItem> cartItems = new ArrayList<>();
    private Address defaultAddress;
    private double subtotal = 0;
    private double shippingFee = 15000;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        tvCheckoutReceiver = findViewById(R.id.tvCheckoutReceiver);
        tvCheckoutAddress = findViewById(R.id.tvCheckoutAddress);
        tvCheckoutPhone = findViewById(R.id.tvCheckoutPhone);
        llOrderItems = findViewById(R.id.llOrderItems);
        tvCheckoutSubtotal = findViewById(R.id.tvCheckoutSubtotal);
        tvCheckoutShipping = findViewById(R.id.tvCheckoutShipping);
        tvCheckoutTotal = findViewById(R.id.tvCheckoutTotal);
        rgPayment = findViewById(R.id.rgPayment);
        edtNote = findViewById(R.id.edtNote);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        pbCheckout = findViewById(R.id.pbCheckout);
        btnBack = findViewById(R.id.btnBackCheckout);

        btnBack.setOnClickListener(v -> finish());

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) { finish(); return; }

        String uid = currentUser.getUid();
        cartRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("cart");
        addressRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("addresses");
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        loadDefaultAddress();
        loadCartItems();

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void loadDefaultAddress() {
        addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Address addr = snap.getValue(Address.class);
                    if (addr != null && addr.isDefault()) {
                        defaultAddress = addr;
                        break;
                    }
                }
                // Nếu không tìm thấy default, lấy cái đầu tiên
                if (defaultAddress == null) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        defaultAddress = snap.getValue(Address.class);
                        break;
                    }
                }
                if (defaultAddress != null) {
                    tvCheckoutReceiver.setText("Người nhận: " + defaultAddress.getReceiverName());
                    tvCheckoutAddress.setText("Địa chỉ: " + defaultAddress.getAddressLine());
                    tvCheckoutPhone.setText("SĐT: " + defaultAddress.getReceiverPhone());
                } else {
                    tvCheckoutReceiver.setText("Chưa có địa chỉ!");
                    tvCheckoutAddress.setText("Vui lòng thêm địa chỉ trước");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadCartItems() {
        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                llOrderItems.removeAllViews();
                subtotal = 0;
                for (DataSnapshot snap : snapshot.getChildren()) {
                    CartItem item = snap.getValue(CartItem.class);
                    if (item != null) {
                        cartItems.add(item);
                        subtotal += item.getUnitPrice() * item.getQuantity();
                        addItemRow(item);
                    }
                }
                tvCheckoutSubtotal.setText(formatter.format(subtotal) + " đ");
                tvCheckoutShipping.setText(formatter.format(shippingFee) + " đ");
                tvCheckoutTotal.setText(formatter.format(subtotal + shippingFee) + " đ");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addItemRow(CartItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 8);

        TextView tvName = new TextView(this);
        tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        tvName.setText(item.getProductName() + " (" + item.getSelectedSize() + ") x" + item.getQuantity());
        tvName.setTextColor(0xFF5D4037);
        tvName.setTextSize(14);

        TextView tvPrice = new TextView(this);
        tvPrice.setText(formatter.format(item.getUnitPrice() * item.getQuantity()) + " đ");
        tvPrice.setTextColor(0xFF3E2723);
        tvPrice.setTextSize(14);

        row.addView(tvName);
        row.addView(tvPrice);
        llOrderItems.addView(row);
    }

    private void placeOrder() {
        if (defaultAddress == null) {
            Toast.makeText(this, "Vui lòng thêm địa chỉ giao hàng!", Toast.LENGTH_LONG).show();
            return;
        }
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Lấy phương thức thanh toán
        String paymentMethod = "COD";
        int checkedId = rgPayment.getCheckedRadioButtonId();
//        if (checkedId == R.id.rbMomo) paymentMethod = "MoMo";
        if (checkedId == R.id.rbBanking) paymentMethod = "Banking";

        String note = edtNote.getText().toString().trim();
        String orderId = ordersRef.push().getKey();
        if (orderId == null) { setLoading(false); return; }

        // Lấy thông tin user
        String finalPaymentMethod = paymentMethod;
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                String userPhone = snapshot.child("phone").getValue(String.class);
                if (userName == null) userName = "Khách hàng";
                if (userPhone == null) userPhone = "";

                Order order = new Order();
                order.setOrderId(orderId);
                order.setUserId(currentUser.getUid());
                order.setUserName(userName);
                order.setUserPhone(userPhone);
                order.setAddressLine(defaultAddress.getAddressLine());
                order.setReceiverName(defaultAddress.getReceiverName());
                order.setReceiverPhone(defaultAddress.getReceiverPhone());
                order.setItems(cartItems);
                order.setSubtotal(subtotal);
                order.setShippingFee(shippingFee);
                order.setTotalAmount(subtotal + shippingFee);
                order.setStatus("pending");
                order.setPaymentMethod(finalPaymentMethod);
                order.setNote(note);
                order.setCreatedAt(System.currentTimeMillis());

                // Ghi đơn hàng vào Firebase
                ordersRef.child(orderId).setValue(order).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Xóa giỏ hàng sau khi đặt thành công
                        cartRef.removeValue();
                        Toast.makeText(CheckoutActivity.this, "🎉 Đặt hàng thành công! Quán sẽ xác nhận ngay.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        setLoading(false);
                        Toast.makeText(CheckoutActivity.this, "Lỗi đặt hàng!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
            }
        });
    }

    private void setLoading(boolean loading) {
        btnPlaceOrder.setEnabled(!loading);
        pbCheckout.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setText(loading ? "ĐANG ĐẶT HÀNG..." : "ĐẶT HÀNG");
    }
}
