package vn.duonghai.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import vn.duonghai.client.adapters.CartAdapter;
import vn.duonghai.client.models.CartItem;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {

    private RecyclerView rcvCart;
    private LinearLayout llEmptyCart;
    private ProgressBar progressBarCart;
    private CardView bottomCheckoutPanel;
    private TextView tvSubtotal, tvTotalPrice;
    private Button btnCheckout;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private DatabaseReference cartRef;
    private FirebaseUser currentUser;

    private DecimalFormat formatter = new DecimalFormat("###,###,###");
    private final double SHIPPING_FEE = 15000.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rcvCart = view.findViewById(R.id.rcvCart);
        llEmptyCart = view.findViewById(R.id.llEmptyCart);
        progressBarCart = view.findViewById(R.id.progressBarCart);
        bottomCheckoutPanel = view.findViewById(R.id.bottomCheckoutPanel);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        rcvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(getContext(), cartItemList, this);
        rcvCart.setAdapter(cartAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            cartRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("cart");
            loadCartItems();
        }

        btnCheckout.setOnClickListener(v -> {
            if (currentUser != null) {
                DatabaseReference addressRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(currentUser.getUid()).child("addresses");
                
                addressRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            // Chuyển sang CheckoutActivity
                            startActivity(new android.content.Intent(getActivity(), vn.duonghai.client.activities.CheckoutActivity.class));
                        } else {
                            Toast.makeText(getContext(), "Vui lòng thêm ít nhất 1 Địa chỉ để giao hàng!", Toast.LENGTH_LONG).show();
                            startActivity(new android.content.Intent(getActivity(), vn.duonghai.client.activities.AddAddressActivity.class));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        return view;
    }

    private void loadCartItems() {
        progressBarCart.setVisibility(View.VISIBLE);
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItemList.clear();
                double subTotal = 0.0;

                if (snapshot.exists()) {
                    for (DataSnapshot itemSnap : snapshot.getChildren()) {
                        CartItem item = itemSnap.getValue(CartItem.class);
                        if (item != null) {
                            cartItemList.add(item);
                            subTotal += (item.getUnitPrice() * item.getQuantity());
                        }
                    }
                }

                cartAdapter.notifyDataSetChanged();
                progressBarCart.setVisibility(View.GONE);

                if (cartItemList.isEmpty()) {
                    llEmptyCart.setVisibility(View.VISIBLE);
                    rcvCart.setVisibility(View.GONE);
                    bottomCheckoutPanel.setVisibility(View.GONE);
                } else {
                    llEmptyCart.setVisibility(View.GONE);
                    rcvCart.setVisibility(View.VISIBLE);
                    bottomCheckoutPanel.setVisibility(View.VISIBLE);
                    
                    tvSubtotal.setText(formatter.format(subTotal) + " đ");
                    double total = subTotal + SHIPPING_FEE;
                    tvTotalPrice.setText(formatter.format(total) + " đ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBarCart.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải giỏ hàng!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        int newQuantity = item.getQuantity() + 1;
        cartRef.child(item.getCartItemId()).child("quantity").setValue(newQuantity);
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            int newQuantity = item.getQuantity() - 1;
            cartRef.child(item.getCartItemId()).child("quantity").setValue(newQuantity);
        } else {
            // Giảm bằng 1 thì hỏi xóa luôn không? Hoặc xóa luôn
            onDeleteItem(item);
        }
    }

    @Override
    public void onDeleteItem(CartItem item) {
        cartRef.child(item.getCartItemId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful() && getContext() != null) {
                Toast.makeText(getContext(), "Đã xóa khỏi giỏ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
