package vn.duonghai.client.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.duonghai.client.R;
import vn.duonghai.client.adapters.AddressAdapter;
import vn.duonghai.client.models.Address;

public class AddressListActivity extends AppCompatActivity implements AddressAdapter.OnAddressInteractionListener {

    private RecyclerView rcvAddresses;
    private TextView tvEmptyAddrs;
    private ProgressBar pbLoadingAddrs;
    private Button btnAddAddrNew;
    private ImageView btnBackAddrs;

    private AddressAdapter adapter;
    private List<Address> addressList;
    private DatabaseReference addressesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);

        rcvAddresses = findViewById(R.id.rcvAddresses);
        tvEmptyAddrs = findViewById(R.id.tvEmptyAddrs);
        pbLoadingAddrs = findViewById(R.id.pbLoadingAddrs);
        btnAddAddrNew = findViewById(R.id.btnAddAddrNew);
        btnBackAddrs = findViewById(R.id.btnBackAddrs);

        rcvAddresses.setLayoutManager(new LinearLayoutManager(this));
        addressList = new ArrayList<>();
        adapter = new AddressAdapter(this, addressList, this);
        rcvAddresses.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            addressesRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("addresses");
            loadAddresses();
        }

        btnBackAddrs.setOnClickListener(v -> finish());
        btnAddAddrNew.setOnClickListener(v -> {
            startActivity(new Intent(AddressListActivity.this, AddAddressActivity.class));
        });
    }

    private void loadAddresses() {
        pbLoadingAddrs.setVisibility(View.VISIBLE);
        addressesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot snap : snapshot.getChildren()) {
                        Address addr = snap.getValue(Address.class);
                        if (addr != null && addr.getId() != null) {
                            addressList.add(addr);
                        }
                    }
                }
                
                // Sort danh sách: Đưa Mặc định lên đầu
                addressList.sort((a1, a2) -> Boolean.compare(a2.isDefault(), a1.isDefault()));
                
                adapter.notifyDataSetChanged();
                pbLoadingAddrs.setVisibility(View.GONE);

                if (addressList.isEmpty()) {
                    tvEmptyAddrs.setVisibility(View.VISIBLE);
                    rcvAddresses.setVisibility(View.GONE);
                } else {
                    tvEmptyAddrs.setVisibility(View.GONE);
                    rcvAddresses.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pbLoadingAddrs.setVisibility(View.GONE);
                Toast.makeText(AddressListActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteAddress(Address address) {
        addressesRef.child(address.getId()).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Đã xoá địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
