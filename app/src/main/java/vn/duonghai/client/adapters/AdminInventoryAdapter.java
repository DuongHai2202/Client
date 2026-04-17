package vn.duonghai.client.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.AddMaterialActivity;
import vn.duonghai.client.models.Material;

public class AdminInventoryAdapter extends RecyclerView.Adapter<AdminInventoryAdapter.MaterialVH> {

    private Context context;
    private List<Material> materialList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public AdminInventoryAdapter(Context context, List<Material> materialList) {
        this.context = context;
        this.materialList = materialList;
    }

    @NonNull
    @Override
    public MaterialVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_inventory, parent, false);
        return new MaterialVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaterialVH holder, int position) {
        Material material = materialList.get(position);

        holder.tvName.setText(material.getName());
        
        // Ensure integer mapping logic for quantity
        String qtyString;
        if (material.getQuantity() == (long) material.getQuantity()) {
            qtyString = String.format("%d", (long)material.getQuantity());
        } else {
            qtyString = String.format("%s", material.getQuantity());
        }
        holder.tvQuantity.setText(qtyString);
        
        holder.tvUnit.setText(" " + material.getUnit());
        holder.tvPrice.setText("Giá nhập: " + formatter.format(material.getImportPrice()) + " đ");

        // Logic cảnh báo
        if (material.getQuantity() < material.getThreshold()) {
            holder.tvQuantity.setTextColor(Color.parseColor("#D32F2F")); // Red
            holder.imgWarning.setVisibility(View.VISIBLE);
        } else {
            holder.tvQuantity.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.imgWarning.setVisibility(View.GONE);
        }

        // Tăng giảm nhanh số lượng
        holder.btnPlusQty.setOnClickListener(v -> updateQuantity(material, 1));
        holder.btnMinusQty.setOnClickListener(v -> updateQuantity(material, -1));

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddMaterialActivity.class);
            intent.putExtra("MATERIAL_ID", material.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa Nguyên Liệu")
                    .setMessage("Bạn có chắc muốn xóa \"" + material.getName() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("inventory")
                                .child(material.getId()).removeValue()
                                .addOnSuccessListener(unused -> 
                                        Toast.makeText(context, "Đã xóa " + material.getName(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void updateQuantity(Material material, double amount) {
        double newQty = material.getQuantity() + amount;
        if (newQty < 0) newQty = 0;
        
        FirebaseDatabase.getInstance().getReference("inventory")
                .child(material.getId())
                .child("quantity")
                .setValue(newQty);
    }

    @Override
    public int getItemCount() {
        return materialList != null ? materialList.size() : 0;
    }

    public static class MaterialVH extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvUnit, tvPrice;
        ImageView imgWarning;
        Button btnMinusQty, btnPlusQty, btnEdit, btnDelete;

        public MaterialVH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMaterialName);
            tvQuantity = itemView.findViewById(R.id.tvMaterialQuantity);
            tvUnit = itemView.findViewById(R.id.tvMaterialUnit);
            tvPrice = itemView.findViewById(R.id.tvMaterialPrice);
            imgWarning = itemView.findViewById(R.id.imgWarning);
            
            btnMinusQty = itemView.findViewById(R.id.btnMinusQty);
            btnPlusQty = itemView.findViewById(R.id.btnPlusQty);
            btnEdit = itemView.findViewById(R.id.btnEditMaterial);
            btnDelete = itemView.findViewById(R.id.btnDeleteMaterial);
        }
    }
}
