package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Product;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductVH> {

    private Context context;
    private List<Product> productList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public AdminProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public AdminProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductVH holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(formatter.format(product.getBasePrice()) + " đ");
        holder.tvCategory.setText("Danh mục: " + product.getCategoryId());
        holder.tvStatus.setText(product.getIsAvailable() ? "Đang bán" : "Tạm ẩn");
        holder.tvStatus.setTextColor(product.getIsAvailable() ? 0xFF4CAF50 : 0xFFE53935);

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgProduct);

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa sản phẩm")
                    .setMessage("Bạn có chắc muốn xóa \"" + product.getName() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("products")
                                .child(product.getId()).removeValue()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(context, "Đã xóa " + product.getName(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class AdminProductVH extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvCategory, tvStatus;
        ImageButton btnDelete;

        public AdminProductVH(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgAdminProduct);
            tvName = itemView.findViewById(R.id.tvAdminProductName);
            tvPrice = itemView.findViewById(R.id.tvAdminProductPrice);
            tvCategory = itemView.findViewById(R.id.tvAdminProductCategory);
            tvStatus = itemView.findViewById(R.id.tvAdminProductStatus);
            btnDelete = itemView.findViewById(R.id.btnDeleteProduct);
        }
    }
}
