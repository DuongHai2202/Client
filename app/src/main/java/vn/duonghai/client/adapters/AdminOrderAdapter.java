package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.duonghai.client.R;
import vn.duonghai.client.models.CartItem;
import vn.duonghai.client.models.Order;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderVH> {

    private Context context;
    private List<Order> orderList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public AdminOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public AdminOrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderVH holder, int position) {
        Order order = orderList.get(position);

        // Mã đơn rút gọn
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = "#" + shortId.substring(shortId.length() - 8).toUpperCase();
        }
        holder.tvOrderId.setText(shortId);

        // Trạng thái
        holder.tvStatus.setText(OrderAdapter.getStatusText(order.getStatus()));
        holder.tvStatus.setTextColor(OrderAdapter.getStatusColor(order.getStatus()));

        // Thông tin khách
        holder.tvCustomer.setText(order.getUserName() + " — " + order.getReceiverPhone());
        holder.tvAddress.setText(order.getAddressLine());

        // Sản phẩm
        StringBuilder sb = new StringBuilder("🧾 ");
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                if (i > 0) sb.append(", ");
                sb.append(item.getProductName()).append(" (").append(item.getSelectedSize()).append(") x").append(item.getQuantity());
            }
        }
        holder.tvItems.setText(sb.toString());

        // Thanh toán & ngày
        holder.tvPayment.setText(order.getPaymentMethod());
        holder.tvDate.setText(dateFormat.format(new Date(order.getCreatedAt())));
        holder.tvTotal.setText(formatter.format(order.getTotalAmount()) + " đ");

        // Ghi chú
        if (order.getNote() != null && !order.getNote().isEmpty()) {
            holder.tvNote.setText(order.getNote());
            holder.tvNote.setVisibility(View.VISIBLE);
        } else {
            holder.tvNote.setVisibility(View.GONE);
        }

        // Nút hành động theo trạng thái
        String status = order.getStatus();
        if ("completed".equals(status) || "cancelled".equals(status)) {
            holder.llActions.setVisibility(View.GONE);
        } else {
            holder.llActions.setVisibility(View.VISIBLE);

            // Đặt text nút "tiến trình tiếp theo"
            String nextStatus = getNextStatus(status);
            String nextLabel = getNextStatusLabel(status);
            holder.btnNext.setText(nextLabel);

            holder.btnNext.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("orders")
                        .child(order.getOrderId()).child("status").setValue(nextStatus)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context, "Đã cập nhật: " + OrderAdapter.getStatusText(nextStatus), Toast.LENGTH_SHORT).show());
            });

            holder.btnReject.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("orders")
                        .child(order.getOrderId()).child("status").setValue("cancelled")
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show());
            });
        }
    }

    private String getNextStatus(String current) {
        if (current == null) return "confirmed";
        switch (current) {
            case "pending": return "confirmed";
            case "confirmed": return "preparing";
            case "preparing": return "shipping";
            case "shipping": return "completed";
            default: return "confirmed";
        }
    }

    private String getNextStatusLabel(String current) {
        if (current == null) return "Xác nhận";
        switch (current) {
            case "pending": return "Xác nhận";
            case "confirmed": return "Bắt đầu pha chế";
            case "preparing": return "Giao hàng";
            case "shipping": return "Hoàn thành";
            default: return "Xác nhận";
        }
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class AdminOrderVH extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvStatus, tvCustomer, tvAddress, tvItems, tvPayment, tvDate, tvTotal, tvNote;
        LinearLayout llActions;
        Button btnReject, btnNext;

        public AdminOrderVH(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvAdminOrderId);
            tvStatus = itemView.findViewById(R.id.tvAdminOrderStatus);
            tvCustomer = itemView.findViewById(R.id.tvAdminOrderCustomer);
            tvAddress = itemView.findViewById(R.id.tvAdminOrderAddress);
            tvItems = itemView.findViewById(R.id.tvAdminOrderItems);
            tvPayment = itemView.findViewById(R.id.tvAdminOrderPayment);
            tvDate = itemView.findViewById(R.id.tvAdminOrderDate);
            tvTotal = itemView.findViewById(R.id.tvAdminOrderTotal);
            tvNote = itemView.findViewById(R.id.tvAdminOrderNote);
            llActions = itemView.findViewById(R.id.llAdminOrderActions);
            btnReject = itemView.findViewById(R.id.btnRejectOrder);
            btnNext = itemView.findViewById(R.id.btnNextStatus);
        }
    }
}
