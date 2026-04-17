package vn.duonghai.client.models;

public class Review {
    private String reviewId;
    private String userId;
    private String productId;
    private String orderId;
    private int rating;
    private String comment;
    private long createdAt;

    public Review() {}

    public Review(String reviewId, String userId, String productId,
                  String orderId, int rating, String comment, long createdAt) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.productId = productId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getReviewId() { return reviewId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public String getOrderId() { return orderId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public long getCreatedAt() { return createdAt; }
}