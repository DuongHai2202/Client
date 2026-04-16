package vn.duonghai.client.adapters;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.AddCategoryActivity;
import vn.duonghai.client.models.Category;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.AdminCategoryVH> {

    private Context context;
    private List<Category> categoryList;

    public AdminCategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public AdminCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new AdminCategoryVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminCategoryVH holder, int position) {
        Category category = categoryList.get(position);

        holder.tvCategoryName.setText(category.getName());
        holder.tvCategoryId.setText("ID: " + category.getId());

        Glide.with(context)
                .load(category.getImage())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(holder.imgCategory);

        // Edit
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddCategoryActivity.class);
            intent.putExtra("CATEGORY_ID", category.getId());
            context.startActivity(intent);
        });

        // Delete
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa Danh Mục")
                    .setMessage("Bạn có chắc muốn xóa danh mục: " + category.getName() + "?\nCác sản phẩm thuộc danh mục này sẽ bị mồ côi.")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("categories")
                                .child(category.getId()).removeValue()
                                .addOnSuccessListener(unused ->
                                        Toast.makeText(context, "Đã xóa " + category.getName(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return categoryList != null ? categoryList.size() : 0;
    }

    public static class AdminCategoryVH extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvCategoryName, tvCategoryId;
        ImageButton btnEdit, btnDelete;

        public AdminCategoryVH(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryId = itemView.findViewById(R.id.tvCategoryId);
            btnEdit = itemView.findViewById(R.id.btnEditCategory);
            btnDelete = itemView.findViewById(R.id.btnDeleteCategory);
        }
    }
}
