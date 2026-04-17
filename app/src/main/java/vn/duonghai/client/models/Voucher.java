package vn.duonghai.client.models;

import com.google.firebase.database.Exclude;

public class Voucher {
    @Exclude
    private String id;
    
    private String code;
    private double discountValue; // Giá trị giảm giá cố định (VNĐ)
    private double minOrderValue; // Đơn hàng tối thiểu để áp dụng
    private int quantity; // Số lượng voucher còn lại
    private boolean isActive;

    public Voucher() {
    }

    public Voucher(String code, double discountValue, double minOrderValue, int quantity, boolean isActive) {
        this.code = code;
        this.discountValue = discountValue;
        this.minOrderValue = minOrderValue;
        this.quantity = quantity;
        this.isActive = isActive;
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public void setMinOrderValue(double minOrderValue) {
        this.minOrderValue = minOrderValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
