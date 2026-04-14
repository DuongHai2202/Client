package vn.duonghai.client.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Address;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {

    private Context context;
    private List<Address> addressList;
    private OnAddressInteractionListener listener;

    public interface OnAddressInteractionListener {
        void onDeleteAddress(Address address);
    }

    public AddressAdapter(Context context, List<Address> addressList, OnAddressInteractionListener listener) {
        this.context = context;
        this.addressList = addressList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        Address addr = addressList.get(position);

        holder.tvAddrLabel.setText(addr.getLabel());
        holder.tvReceiver.setText("Tên: " + addr.getReceiverName() + " | SĐT: " + addr.getReceiverPhone());
        holder.tvAddressLine.setText(addr.getAddressLine());

        if (addr.isDefault()) {
            holder.tvAddrDefaultTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvAddrDefaultTag.setVisibility(View.GONE);
        }

        holder.btnDeleteAddr.setOnClickListener(v -> listener.onDeleteAddress(addr));
    }

    @Override
    public int getItemCount() {
        return addressList != null ? addressList.size() : 0;
    }

    public static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddrLabel, tvAddrDefaultTag, tvReceiver, tvAddressLine;
        ImageButton btnDeleteAddr;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddrLabel = itemView.findViewById(R.id.tvAddrLabel);
            tvAddrDefaultTag = itemView.findViewById(R.id.tvAddrDefaultTag);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvAddressLine = itemView.findViewById(R.id.tvAddressLine);
            btnDeleteAddr = itemView.findViewById(R.id.btnDeleteAddr);
        }
    }
}
