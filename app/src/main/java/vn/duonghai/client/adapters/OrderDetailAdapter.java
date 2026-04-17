package vn.duonghai.client.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.ReviewActivity;
import vn.duonghai.client.models.CartItem;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {

    private Context context;
    private List<CartItem> itemList;
    private String orderId;
    private boolean isCompleted;

    public OrderDetailAdapter(Context context, List<CartItem> itemList, String orderId, boolean isCompleted) {
        this.context = context;
        this.itemList = itemList;
        this.orderId = orderId;
        this.isCompleted = isCompleted;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = itemList.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvOptions.setText("Số lượng: " + item.getQuantity());

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgProduct);

        if (isCompleted) {
            holder.btnReview.setVisibility(View.VISIBLE);
            holder.btnReview.setOnClickListener(v -> {
                Intent intent = new Intent(context, ReviewActivity.class);
                intent.putExtra("productId", item.getProductId());
                intent.putExtra("orderId", orderId);
                context.startActivity(intent);
            });
        } else {
            holder.btnReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvOptions;
        Button btnReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvOptions = itemView.findViewById(R.id.tvOptions);
            btnReview = itemView.findViewById(R.id.btnReview);
        }
    }
}
