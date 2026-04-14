package vn.duonghai.client.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vn.duonghai.client.R;
import vn.duonghai.client.models.Address;

public class AddAddressActivity extends AppCompatActivity {

    private TextInputEditText edtReceiverName, edtReceiverPhone, edtAddressLine, edtLabel;
    private SwitchMaterial swDefaultAddress;
    private Button btnSaveAddress;
    private ProgressBar pbLoading;
    private ImageView btnBackAddAddr;

    private FirebaseUser currentUser;
    private DatabaseReference addressesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi đăng nhập!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        addressesRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("addresses");

        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtReceiverPhone = findViewById(R.id.edtReceiverPhone);
        edtAddressLine = findViewById(R.id.edtAddressLine);
        edtLabel = findViewById(R.id.edtLabel);
        swDefaultAddress = findViewById(R.id.swDefaultAddress);
        btnSaveAddress = findViewById(R.id.btnSaveAddress);
        pbLoading = findViewById(R.id.pbLoading);
        btnBackAddAddr = findViewById(R.id.btnBackAddAddr);

        btnBackAddAddr.setOnClickListener(v -> finish());
        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        String name = edtReceiverName.getText().toString().trim();
        String phone = edtReceiverPhone.getText().toString().trim();
        String addrLine = edtAddressLine.getText().toString().trim();
        String label = edtLabel.getText().toString().trim();
        boolean isDefault = swDefaultAddress.isChecked();

        if (name.isEmpty() || phone.isEmpty() || addrLine.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ các thông tin có chữ (Bắt buộc)!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (label.isEmpty()) label = "Nhà riêng"; // Mặc định Label

        pbLoading.setVisibility(View.VISIBLE);
        btnSaveAddress.setEnabled(false);

        String pushId = addressesRef.push().getKey();
        if (pushId == null) {
            pbLoading.setVisibility(View.GONE);
            btnSaveAddress.setEnabled(true);
            return;
        }

        Address newAddr = new Address(pushId, currentUser.getUid(), label, name, phone, addrLine, isDefault);

        if (isDefault) {
            // Cần cập nhật các địa chỉ cũ thành isDefault = false trước
            addressesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            postSnapshot.getRef().child("default").setValue(false);
                            // Vì JSON serialize boolean isDefault hay aDefault có trắc trở, cần chú ý tên trường
                            // get/set của chúng ta là isDefault, Jackson sẽ serialize thành "default". Nên ta update field "default".
                        }
                    }
                    // Sau khi cập nhật xong thì lưu cái mới
                    pushAddress(pushId, newAddr);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    pbLoading.setVisibility(View.GONE);
                    btnSaveAddress.setEnabled(true);
                }
            });
        } else {
            pushAddress(pushId, newAddr);
        }
    }

    private void pushAddress(String pushId, Address newAddr) {
        addressesRef.child(pushId).setValue(newAddr).addOnCompleteListener(task -> {
            pbLoading.setVisibility(View.GONE);
            btnSaveAddress.setEnabled(true);
            if (task.isSuccessful()) {
                Toast.makeText(AddAddressActivity.this, "Đã lưu địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Đóng Activity và quay lại giỏ hàng / sổ địa chỉ
            } else {
                Toast.makeText(AddAddressActivity.this, "Lỗi lưu DB: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
