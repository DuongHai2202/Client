package vn.duonghai.client.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Voucher;

public class AddVoucherActivity extends AppCompatActivity {

    private EditText edtCode, edtDiscount, edtMinOrder, edtQuantity;
    private CheckBox cbxActive;
    private DatabaseReference voucherRef;
    private String voucherId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_voucher);

        edtCode = findViewById(R.id.edtVoucherCode);
        edtDiscount = findViewById(R.id.edtVoucherDiscount);
        edtMinOrder = findViewById(R.id.edtVoucherMinOrder);
        edtQuantity = findViewById(R.id.edtVoucherQuantity);
        cbxActive = findViewById(R.id.cbxVoucherActive);
        Button btnSave = findViewById(R.id.btnSaveVoucher);

        voucherRef = FirebaseDatabase.getInstance().getReference("vouchers");

        if (getIntent() != null && getIntent().hasExtra("VOUCHER_ID")) {
            voucherId = getIntent().getStringExtra("VOUCHER_ID");
            loadVoucherData(voucherId);
        }

        btnSave.setOnClickListener(v -> saveVoucher());
    }

    private void loadVoucherData(String id) {
        voucherRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Voucher voucher = snapshot.getValue(Voucher.class);
                if (voucher != null) {
                    edtCode.setText(voucher.getCode());
                    edtDiscount.setText(String.valueOf((int)voucher.getDiscountValue()));
                    edtMinOrder.setText(String.valueOf((int)voucher.getMinOrderValue()));
                    edtQuantity.setText(String.valueOf(voucher.getQuantity()));
                    cbxActive.setChecked(voucher.isActive());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void saveVoucher() {
        String code = edtCode.getText().toString().trim().toUpperCase();
        String strDiscount = edtDiscount.getText().toString().trim();
        String strMinOrder = edtMinOrder.getText().toString().trim();
        String strQty = edtQuantity.getText().toString().trim();
        boolean isActive = cbxActive.isChecked();

        if (code.isEmpty() || strDiscount.isEmpty() || strMinOrder.isEmpty() || strQty.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double discount = Double.parseDouble(strDiscount);
        double minOrder = Double.parseDouble(strMinOrder);
        int qty = Integer.parseInt(strQty);

        Voucher voucher = new Voucher(code, discount, minOrder, qty, isActive);

        if (voucherId == null) {
            voucherId = voucherRef.push().getKey();
        }

        if (voucherId != null) {
            voucherRef.child(voucherId).setValue(voucher)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
