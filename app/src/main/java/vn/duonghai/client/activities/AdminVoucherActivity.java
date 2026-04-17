package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import vn.duonghai.client.adapters.AdminVoucherAdapter;
import vn.duonghai.client.models.Voucher;

public class AdminVoucherActivity extends AppCompatActivity {

    private RecyclerView rvAdminVouchers;
    private AdminVoucherAdapter adapter;
    private List<Voucher> voucherList;
    private DatabaseReference voucherRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_voucher);

        rvAdminVouchers = findViewById(R.id.rvAdminVouchers);
        rvAdminVouchers.setLayoutManager(new LinearLayoutManager(this));

        voucherList = new ArrayList<>();
        adapter = new AdminVoucherAdapter(this, voucherList);
        rvAdminVouchers.setAdapter(adapter);

        findViewById(R.id.btnBackAdminVoucher).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnAddVoucher).setOnClickListener(v -> {
            startActivity(new Intent(AdminVoucherActivity.this, AddVoucherActivity.class));
        });

        voucherRef = FirebaseDatabase.getInstance().getReference("vouchers");
        loadVouchers();
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminVoucherActivity.this, "Lỗi tải voucher", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
