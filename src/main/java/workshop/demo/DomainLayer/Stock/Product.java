package workshop.demo.DomainLayer.Stock;

public class Product {

    private String name;
    private String productId;
    private int totalAmount;
    private double rating;
    private String description;
    private Category category;  // Using enum Category

    

    public Product(String name, String productId, Category category, String description) {
        this.name = name;
        this.productId = productId;
        this.category = category;
        this.description = description;
        this.totalAmount = 0;  
        this.rating = 0.0;     
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // Enum for Category
    public enum Category {
        FOOD,
        ELECTRONICS,
        CLOTHING,
        BEAUTY,
        HOME,
        TOYS
    }
}