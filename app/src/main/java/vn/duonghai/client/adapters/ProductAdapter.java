package vn.duonghai.client.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import vn.duonghai.client.R;
import vn.duonghai.client.models.CartItem;
import vn.duonghai.client.models.Product;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        
        if (!product.getIsAvailable()) {
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(formatter.format(product.getBasePrice()) + " đ");

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgProduct);

        holder.btnAddCart.setOnClickListener(v -> {
            if (product.getIsAvailable()) {
                showProductOptionsDialog(product);
            } else {
                Toast.makeText(context, "Món này hiện đang tạm hết!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Nhấn toàn bộ the Cung để hiện Popup
        holder.itemView.setOnClickListener(v -> {
            if (product.getIsAvailable()) {
                showProductOptionsDialog(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    private void showProductOptionsDialog(Product product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_product_options, null);
        bottomSheetDialog.setContentView(dialogView);

        ImageView imgDialogProduct = dialogView.findViewById(R.id.imgDialogProduct);
        TextView tvDialogName = dialogView.findViewById(R.id.tvDialogName);
        TextView tvDialogBasePrice = dialogView.findViewById(R.id.tvDialogBasePrice);
        
        RadioGroup rgSize = dialogView.findViewById(R.id.rgSize);
        RadioGroup rgSugar = dialogView.findViewById(R.id.rgSugar);
        RadioGroup rgIce = dialogView.findViewById(R.id.rgIce);
        LinearLayout llToppings = dialogView.findViewById(R.id.llToppings);
        
        TextView tvQuantity = dialogView.findViewById(R.id.tvQuantity);
        ImageButton btnMinus = dialogView.findViewById(R.id.btnMinus);
        ImageButton btnPlus = dialogView.findViewById(R.id.btnPlus);
        Button btnAddToCart = dialogView.findViewById(R.id.btnAddToCart);

        // Khởi tạo thông tin cơ bản
        tvDialogName.setText(product.getName());
        Glide.with(context).load(product.getImage()).centerCrop().into(imgDialogProduct);
        
        // Mảng lưu trạng thái
        final int[] currentQuantity = {1};
        final double[] currentSizePrice = {product.getBasePrice()};
        final double[] toppingPriceTotal = {0};

        // Hàm cập nhật Nút Tổng Tiền
        Runnable updateTotalPriceUI = () -> {
            double total = (currentSizePrice[0] + toppingPriceTotal[0]) * currentQuantity[0];
            btnAddToCart.setText("THÊM " + currentQuantity[0] + " - " + formatter.format(total) + " đ");
        };

        // Render RadioButtons cho Size
        Map<String, Product.SizeOption> sizes = product.getSizes();
        if (sizes != null && !sizes.isEmpty()) {
            boolean isFirst = true;
            for (Map.Entry<String, Product.SizeOption> entry : sizes.entrySet()) {
                RadioButton rb = new RadioButton(context);
                rb.setText("Size " + entry.getKey() + " (" + formatter.format(entry.getValue().getPrice()) + " đ)");
                rb.setTag(entry.getValue().getPrice());
                
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked},
                                new int[]{android.R.attr.state_checked}
                        },
                        new int[]{
                                ContextCompat.getColor(context, android.R.color.darker_gray),
                                ContextCompat.getColor(context, R.color.coffee_primary)
                        }
                );
                rb.setButtonTintList(colorStateList);
                
                rgSize.addView(rb);

                if (isFirst) {
                    rb.setChecked(true);
                    currentSizePrice[0] = entry.getValue().getPrice();
                    isFirst = false;
                }
            }
            rgSize.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRb = dialogView.findViewById(checkedId);
                if (checkedRb != null) {
                    currentSizePrice[0] = (double) checkedRb.getTag();
                    tvDialogBasePrice.setText(formatter.format(currentSizePrice[0]) + " đ");
                    updateTotalPriceUI.run();
                }
            });
        }
        tvDialogBasePrice.setText(formatter.format(currentSizePrice[0]) + " đ");

        // Render Đường
        List<String> sugars = product.getSugarOptions();
        if (sugars != null && !sugars.isEmpty()) {
            boolean isFirst = true;
            for (String sugar : sugars) {
                if (sugar == null || sugar.trim().isEmpty()) continue;
                RadioButton rb = new RadioButton(context);
                rb.setText(sugar);
                rb.setPadding(0,0,24,0);
                ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                        new int[]{ContextCompat.getColor(context, android.R.color.darker_gray), ContextCompat.getColor(context, R.color.coffee_primary)});
                rb.setButtonTintList(colorStateList);
                rgSugar.addView(rb);
                if (isFirst) { rb.setChecked(true); isFirst = false; }
            }
            if (rgSugar.getChildCount() == 0) {
                dialogView.findViewById(R.id.rgSugar).setVisibility(View.GONE);
            }
        } else {
            dialogView.findViewById(R.id.rgSugar).setVisibility(View.GONE);
        }

        // Render Đá
        List<String> ices = product.getIceOptions();
        if (ices != null && !ices.isEmpty()) {
            boolean isFirst = true;
            for (String ice : ices) {
                if (ice == null || ice.trim().isEmpty()) continue;
                RadioButton rb = new RadioButton(context);
                rb.setText(ice);
                rb.setPadding(0,0,24,0);
                ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                        new int[]{ContextCompat.getColor(context, android.R.color.darker_gray), ContextCompat.getColor(context, R.color.coffee_primary)});
                rb.setButtonTintList(colorStateList);
                rgIce.addView(rb);
                if (isFirst) { rb.setChecked(true); isFirst = false; }
            }
            if (rgIce.getChildCount() == 0) {
                dialogView.findViewById(R.id.rgIce).setVisibility(View.GONE);
            }
        } else {
            dialogView.findViewById(R.id.rgIce).setVisibility(View.GONE);
        }

        // Render Topping Checkbox
        List<String> toppings = product.getAvailableToppings();
        List<CheckBox> toppingCheckBoxes = new ArrayList<>();
        if (toppings != null && !toppings.isEmpty()) {
            for (String topping : toppings) {
                CheckBox cb = new CheckBox(context);
                cb.setText(topping);
                
                ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                        new int[]{ContextCompat.getColor(context, android.R.color.darker_gray), ContextCompat.getColor(context, R.color.coffee_primary)});
                cb.setButtonTintList(colorStateList);

                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) toppingPriceTotal[0] += 10000;
                    else toppingPriceTotal[0] -= 10000;
                    updateTotalPriceUI.run();
                });
                llToppings.addView(cb);
                toppingCheckBoxes.add(cb);
            }
        } else {
            dialogView.findViewById(R.id.llToppings).setVisibility(View.GONE);
        }

        // Logic Số Lượng
        btnPlus.setOnClickListener(v -> {
            currentQuantity[0]++;
            tvQuantity.setText(String.valueOf(currentQuantity[0]));
            updateTotalPriceUI.run();
        });

        btnMinus.setOnClickListener(v -> {
            if (currentQuantity[0] > 1) {
                currentQuantity[0]--;
                tvQuantity.setText(String.valueOf(currentQuantity[0]));
                updateTotalPriceUI.run();
            }
        });

        updateTotalPriceUI.run();

        // Gửi giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(context, "Vui lòng đăng nhập để đặt hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gôm thông tin chọn
            String selSize = "S";
            int selectedSizeRbId = rgSize.getCheckedRadioButtonId();
            if (selectedSizeRbId != -1) {
                RadioButton rb = dialogView.findViewById(selectedSizeRbId);
                String text = rb.getText().toString(); // VD: "Size L (45.000 đ)"
                selSize = text.split(" ")[1];
            }

            String selSugar = "";
            int selectedSugarRbId = rgSugar.getCheckedRadioButtonId();
            if (selectedSugarRbId != -1) {
                RadioButton rb = dialogView.findViewById(selectedSugarRbId);
                selSugar = rb.getText().toString();
            }

            String selIce = "";
            int selectedIceRbId = rgIce.getCheckedRadioButtonId();
            if (selectedIceRbId != -1) {
                RadioButton rb = dialogView.findViewById(selectedIceRbId);
                selIce = rb.getText().toString();
            }

            List<String> selectedToppingsList = new ArrayList<>();
            for (CheckBox cb : toppingCheckBoxes) {
                if (cb.isChecked()) {
                    selectedToppingsList.add(cb.getText().toString());
                }
            }

            double finalUnitPrice = currentSizePrice[0] + toppingPriceTotal[0];

            CartItem cartItem = new CartItem();
            cartItem.setProductId(product.getId());
            cartItem.setProductName(product.getName());
            cartItem.setProductImage(product.getImage());
            cartItem.setQuantity(currentQuantity[0]);
            cartItem.setUnitPrice(finalUnitPrice);
            cartItem.setSelectedSize(selSize);
            cartItem.setSelectedSugar(selSugar);
            cartItem.setSelectedIce(selIce);
            cartItem.setSelectedToppings(selectedToppingsList);

            // Bắn lên Firebase
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("cart");
            String key = cartRef.push().getKey();
            if (key != null) {
                cartItem.setCartItemId(key);
                cartRef.child(key).setValue(cartItem).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Đã thêm vào Giỏ món " + product.getName() + "!", Toast.LENGTH_SHORT).show();
                        bottomSheetDialog.dismiss();
                    } else {
                        Toast.makeText(context, "Lỗi thêm giỏ: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        bottomSheetDialog.show();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvProductPrice;
        ImageButton btnAddCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
        }
    }
}
