package vn.duonghai.client.models;

public class Favorite {
    private String productId;
    private long addedAt;

    public Favorite() {}

    public Favorite(String productId, long addedAt) {
        this.productId = productId;
        this.addedAt = addedAt;
    }

    // Getters và Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }
}
