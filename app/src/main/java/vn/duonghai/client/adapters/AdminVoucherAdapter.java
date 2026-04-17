package vn.duonghai.client.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.AddVoucherActivity;
import vn.duonghai.client.models.Voucher;

public class AdminVoucherAdapter extends RecyclerView.Adapter<AdminVoucherAdapter.VoucherVH> {

    private Context context;
    private List<Voucher> voucherList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public AdminVoucherAdapter(Context context, List<Voucher> voucherList) {
        this.context = context;
        this.voucherList = voucherList;
    }

    @NonNull
    @Override
    public VoucherVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_voucher, parent, false);
        return new VoucherVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoucherVH holder, int position) {
        Voucher voucher = voucherList.get(position);

        holder.tvCode.setText(voucher.getCode());
        holder.tvDiscount.setText("Giảm: " + formatter.format(voucher.getDiscountValue()) + " đ");
        holder.tvConditions.setText("Đơn tối thiểu: " + formatter.format(voucher.getMinOrderValue()) + " đ | Còn: " + voucher.getQuantity());

        if (voucher.isActive()) {
            holder.tvStatus.setText("Hoạt động");
            holder.tvStatus.setTextColor(0xFF4CAF50);
        } else {
            holder.tvStatus.setText("Tạm dừng");
            holder.tvStatus.setTextColor(0xFFE53935);
        }

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddVoucherActivity.class);
            intent.putExtra("VOUCHER_ID", voucher.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Xóa Voucher")
                    .setMessage("Bạn có chắc muốn xóa mã \"" + voucher.getCode() + "\"?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference("vouchers")
                                .child(voucher.getId()).removeValue()
                                .addOnSuccessListener(unused -> 
                                        Toast.makeText(context, "Đã xóa mã " + voucher.getCode(), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return voucherList != null ? voucherList.size() : 0;
    }

    public static class VoucherVH extends RecyclerView.ViewHolder {
        TextView tvCode, tvStatus, tvDiscount, tvConditions;
        Button btnEdit, btnDelete;

        public VoucherVH(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvStatus = itemView.findViewById(R.id.tvVoucherStatus);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvConditions = itemView.findViewById(R.id.tvVoucherConditions);
            btnEdit = itemView.findViewById(R.id.btnEditVoucher);
            btnDelete = itemView.findViewById(R.id.btnDeleteVoucher);
        }
    }
}
