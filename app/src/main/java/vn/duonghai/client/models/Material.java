package vn.duonghai.client.models;

import com.google.firebase.database.Exclude;

public class Material {
    @Exclude
    private String id;
    
    private String name;
    private double quantity;
    private String unit;
    private double importPrice;
    private double threshold;

    public Material() {}

    public Material(String name, double quantity, String unit, double importPrice, double threshold) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.importPrice = importPrice;
        this.threshold = threshold;
    }

    @Exclude
    public String getId() { return id; }

    @Exclude
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getImportPrice() { return importPrice; }
    public void setImportPrice(double importPrice) { this.importPrice = importPrice; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
}
