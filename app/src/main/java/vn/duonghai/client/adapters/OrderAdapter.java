package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

        // Trạng thái text
        holder.tvOrderStatus.setText(getStatusText(order.getStatus()));
        holder.tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));

        // Thanh tiến trình bước
        updateStatusSteps(holder, order.getStatus());

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

    private void updateStatusSteps(OrderVH holder, String status) {
        int stepIndex = getStepIndex(status);
        boolean isCancelled = "cancelled".equals(status);

        // Danh sách các dot view
        View[] dots = {holder.dotPending, holder.dotConfirmed, holder.dotPreparing, holder.dotShipping, holder.dotCompleted};
        TextView[] labels = {holder.tvStepPending, holder.tvStepConfirmed, holder.tvStepPreparing, holder.tvStepShipping, holder.tvStepCompleted};
        View[] lines = {holder.linePendingConfirmed, holder.lineConfirmedPreparing, holder.linePreparingShipping, holder.lineShippingCompleted};

        if (isCancelled) {
            // Đơn bị hủy -> ẩn thanh tiến trình
            holder.llStatusSteps.setVisibility(View.GONE);
            return;
        }

        holder.llStatusSteps.setVisibility(View.VISIBLE);

        for (int i = 0; i < dots.length; i++) {
            if (i < stepIndex) {
                // Đã qua -> xanh lá
                dots[i].setBackgroundResource(R.drawable.status_dot_active);
                labels[i].setTextColor(0xFF4CAF50);
            } else if (i == stepIndex) {
                // Bước hiện tại -> cam
                dots[i].setBackgroundResource(R.drawable.status_dot_current);
                labels[i].setTextColor(0xFFFF6F00);
            } else {
                // Chưa tới -> xám
                dots[i].setBackgroundResource(R.drawable.status_dot_inactive);
                labels[i].setTextColor(0xFFBDBDBD);
            }
        }

        for (int i = 0; i < lines.length; i++) {
            if (i < stepIndex) {
                lines[i].setBackgroundColor(0xFF4CAF50); // xanh lá
            } else {
                lines[i].setBackgroundColor(0xFFE0E0E0); // xám
            }
        }
    }

    private int getStepIndex(String status) {
        if (status == null) return 0;
        switch (status) {
            case "pending": return 0;
            case "confirmed": return 1;
            case "preparing": return 2;
            case "shipping": return 3;
            case "completed": return 4;
            default: return 0;
        }
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
        // Step indicators
        LinearLayout llStatusSteps;
        View dotPending, dotConfirmed, dotPreparing, dotShipping, dotCompleted;
        View linePendingConfirmed, lineConfirmedPreparing, linePreparingShipping, lineShippingCompleted;
        TextView tvStepPending, tvStepConfirmed, tvStepPreparing, tvStepShipping, tvStepCompleted;

        public OrderVH(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderItemsSummary = itemView.findViewById(R.id.tvOrderItemsSummary);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);

            llStatusSteps = itemView.findViewById(R.id.llStatusSteps);
            dotPending = itemView.findViewById(R.id.dotPending);
            dotConfirmed = itemView.findViewById(R.id.dotConfirmed);
            dotPreparing = itemView.findViewById(R.id.dotPreparing);
            dotShipping = itemView.findViewById(R.id.dotShipping);
            dotCompleted = itemView.findViewById(R.id.dotCompleted);

            linePendingConfirmed = itemView.findViewById(R.id.linePendingConfirmed);
            lineConfirmedPreparing = itemView.findViewById(R.id.lineConfirmedPreparing);
            linePreparingShipping = itemView.findViewById(R.id.linePreparingShipping);
            lineShippingCompleted = itemView.findViewById(R.id.lineShippingCompleted);

            tvStepPending = itemView.findViewById(R.id.tvStepPending);
            tvStepConfirmed = itemView.findViewById(R.id.tvStepConfirmed);
            tvStepPreparing = itemView.findViewById(R.id.tvStepPreparing);
            tvStepShipping = itemView.findViewById(R.id.tvStepShipping);
            tvStepCompleted = itemView.findViewById(R.id.tvStepCompleted);
        }
    }
}
