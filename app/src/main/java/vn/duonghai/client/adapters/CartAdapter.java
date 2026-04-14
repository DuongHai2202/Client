package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.CartItem;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartList;
    private OnCartItemInteractionListener listener;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public interface OnCartItemInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onDeleteItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartList, OnCartItemInteractionListener listener) {
        this.context = context;
        this.cartList = cartList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartList.get(position);

        holder.tvCartProductName.setText(item.getProductName());
        holder.tvCartPrice.setText(formatter.format(item.getUnitPrice() * item.getQuantity()) + " đ");
        holder.tvCartQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgCartProduct);

        // Nối các Topping/Options thành dãy String
        StringBuilder optionsStr = new StringBuilder();
        if (item.getSelectedSize() != null && !item.getSelectedSize().isEmpty()) {
            optionsStr.append("Size ").append(item.getSelectedSize()).append(" | ");
        }
        if (item.getSelectedIce() != null && !item.getSelectedIce().isEmpty()) {
            optionsStr.append(item.getSelectedIce()).append(" | ");
        }
        if (item.getSelectedSugar() != null && !item.getSelectedSugar().isEmpty()) {
            optionsStr.append(item.getSelectedSugar()).append(" | ");
        }
        if (item.getSelectedToppings() != null && !item.getSelectedToppings().isEmpty()) {
            for (String top : item.getSelectedToppings()) {
                optionsStr.append(top).append(", ");
            }
        }
        // Xoá ký tự ' | ' hoặc ', ' thừa ở cuối
        String finalOptions = optionsStr.toString();
        if (finalOptions.endsWith(" | ")) finalOptions = finalOptions.substring(0, finalOptions.length() - 3);
        if (finalOptions.endsWith(", ")) finalOptions = finalOptions.substring(0, finalOptions.length() - 2);
        
        holder.tvCartOptions.setText(finalOptions);

        holder.btnCartPlus.setOnClickListener(v -> listener.onIncreaseQuantity(item));
        holder.btnCartMinus.setOnClickListener(v -> listener.onDecreaseQuantity(item));
        holder.btnDeleteCartItem.setOnClickListener(v -> listener.onDeleteItem(item));
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCartProduct;
        TextView tvCartProductName, tvCartOptions, tvCartPrice, tvCartQuantity;
        ImageButton btnCartMinus, btnCartPlus, btnDeleteCartItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartProduct = itemView.findViewById(R.id.imgCartProduct);
            tvCartProductName = itemView.findViewById(R.id.tvCartProductName);
            tvCartOptions = itemView.findViewById(R.id.tvCartOptions);
            tvCartPrice = itemView.findViewById(R.id.tvCartPrice);
            tvCartQuantity = itemView.findViewById(R.id.tvCartQuantity);
            btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
            btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
            btnDeleteCartItem = itemView.findViewById(R.id.btnDeleteCartItem);
        }
    }
}
