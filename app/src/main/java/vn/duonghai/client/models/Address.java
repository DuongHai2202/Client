package vn.duonghai.client.models;

public class Address {
    private String id;
    private String userId; // Thêm trường liên kết với User
    private String label; // "Nhà riêng", "Công ty"...
    private String receiverName;
    private String receiverPhone;
    private String addressLine;
    private boolean isDefault;

    public Address() {
    }

    public Address(String id, String userId, String label, String receiverName, String receiverPhone, String addressLine, boolean isDefault) {
        this.id = id;
        this.userId = userId;
        this.label = label;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.addressLine = addressLine;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public void setAddressLine(String addressLine) {
        this.addressLine = addressLine;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
