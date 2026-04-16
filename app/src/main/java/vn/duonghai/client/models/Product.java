package vn.duonghai.client.models;

import com.google.firebase.database.Exclude;
import java.util.List;
import java.util.Map;

public class Product {

    @Exclude
    private String id;

    private String name;
    private String categoryId;
    private String image;
    private boolean isAvailable;

    private Map<String, SizeOption> sizes;
    private List<String> availableToppings;
    private List<String> sugarOptions;
    private List<String> iceOptions;

    public static class SizeOption {
        private double price;

        public SizeOption() {
        }

        public SizeOption(double price) {
            this.price = price;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    private String description;

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean getIsAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean available) {
        this.isAvailable = available;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, SizeOption> getSizes() {
        return sizes;
    }

    public void setSizes(Map<String, SizeOption> sizes) {
        this.sizes = sizes;
    }

    public List<String> getAvailableToppings() {
        return availableToppings;
    }

    public void setAvailableToppings(List<String> availableToppings) {
        this.availableToppings = availableToppings;
    }

    public List<String> getSugarOptions() {
        return sugarOptions;
    }

    public void setSugarOptions(List<String> sugarOptions) {
        this.sugarOptions = sugarOptions;
    }

    public List<String> getIceOptions() {
        return iceOptions;
    }

    public void setIceOptions(List<String> iceOptions) {
        this.iceOptions = iceOptions;
    }

    @Exclude
    public double getBasePrice() {
        if (sizes != null && !sizes.isEmpty()) {
            if (sizes.containsKey("S"))
                return sizes.get("S").getPrice();
            if (sizes.containsKey("M"))
                return sizes.get("M").getPrice();
            return sizes.values().iterator().next().getPrice();
        }
        return 0;
    }
}
