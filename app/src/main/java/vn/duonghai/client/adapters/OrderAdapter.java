package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vn.duonghai.client.R;
import vn.duonghai.client.models.CartItem;
import vn.duonghai.client.models.Order;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderVH> {

    private Context context;
    private List<Order> orderList;
    private DecimalFormat formatter = new DecimalFormat("###,###,###");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderVH holder, int position) {
        Order order = orderList.get(position);

        // Mã đơn (rút gọn)
        String shortId = order.getOrderId();
        if (shortId != null && shortId.length() > 8) {
            shortId = "#" + shortId.substring(shortId.length() - 8).toUpperCase();
        }
        holder.tvOrderId.setText(shortId);

        // Trạng thái
        holder.tvOrderStatus.setText(getStatusText(order.getStatus()));
        holder.tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));

        // Tóm tắt sản phẩm
        StringBuilder sb = new StringBuilder();
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                CartItem item = order.getItems().get(i);
                if (i > 0) sb.append(", ");
                sb.append(item.getProductName()).append(" x").append(item.getQuantity());
            }
        }
        holder.tvOrderItemsSummary.setText(sb.toString());

        // Ngày tạo
        holder.tvOrderDate.setText(dateFormat.format(new Date(order.getCreatedAt())));

        // Tổng tiền
        holder.tvOrderTotal.setText(formatter.format(order.getTotalAmount()) + " đ");
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static String getStatusText(String status) {
        if (status == null) return "Không rõ";
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "preparing": return "Đang pha chế";
            case "shipping": return "Đang giao";
            case "completed": return "Hoàn thành";
            case "cancelled": return "Đã hủy";
            default: return status;
        }
    }

    public static int getStatusColor(String status) {
        if (status == null) return 0xFF757575;
        switch (status) {
            case "pending": return 0xFFFF6F00;
            case "confirmed": return 0xFF1976D2;
            case "preparing": return 0xFF7B1FA2;
            case "shipping": return 0xFF0097A7;
            case "completed": return 0xFF388E3C;
            case "cancelled": return 0xFFD32F2F;
            default: return 0xFF757575;
        }
    }

    public static class OrderVH extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatus, tvOrderItemsSummary, tvOrderDate, tvOrderTotal;

        public OrderVH(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItemsSummary = itemView.findViewById(R.id.tvOrderItemsSummary);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
