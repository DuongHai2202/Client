package vn.duonghai.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.activities.AddVoucherActivity;
import vn.duonghai.client.adapters.AdminVoucherAdapter;
import vn.duonghai.client.models.Voucher;

public class AdminVouchersFragment extends Fragment {

    private RecyclerView rvAdminVouchers;
    private AdminVoucherAdapter adapter;
    private List<Voucher> voucherList;
    private DatabaseReference voucherRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_vouchers, container, false);

        rvAdminVouchers = view.findViewById(R.id.rvAdminVouchers);
        rvAdminVouchers.setLayoutManager(new LinearLayoutManager(getContext()));

        voucherList = new ArrayList<>();
        adapter = new AdminVoucherAdapter(getContext(), voucherList);
        rvAdminVouchers.setAdapter(adapter);
        
        view.findViewById(R.id.btnAddVoucher).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddVoucherActivity.class));
        });

        voucherRef = FirebaseDatabase.getInstance().getReference("vouchers");
        loadVouchers();

        return view;
    }

    private void loadVouchers() {
        voucherRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                voucherList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Voucher voucher = ds.getValue(Voucher.class);
                    if (voucher != null) {
                        voucher.setId(ds.getKey());
                        voucherList.add(voucher);
                    }
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải voucher", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
