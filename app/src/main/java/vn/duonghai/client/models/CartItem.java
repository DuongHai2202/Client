package vn.duonghai.client.models;

import java.util.List;

public class CartItem {
    private String cartItemId; // Firebase push key for cart item
    private String productId;
    private String productName;
    private String productImage;
    private int quantity;
    private double unitPrice; // Giá 1 ly đã cộng thêm tiền Topping/Size
    
    private String selectedSize;
    private String selectedSugar;
    private String selectedIce;
    private List<String> selectedToppings;

    public CartItem() {
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getSelectedSugar() {
        return selectedSugar;
    }

    public void setSelectedSugar(String selectedSugar) {
        this.selectedSugar = selectedSugar;
    }

    public String getSelectedIce() {
        return selectedIce;
    }

    public void setSelectedIce(String selectedIce) {
        this.selectedIce = selectedIce;
    }

    public List<String> getSelectedToppings() {
        return selectedToppings;
    }

    public void setSelectedToppings(List<String> selectedToppings) {
        this.selectedToppings = selectedToppings;
    }
}
