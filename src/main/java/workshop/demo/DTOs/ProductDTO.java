package workshop.demo.DTOs;

public class ProductDTO {

    private int productId;
    private String name;
    private Category category;
    private String description;

    // store:
    private boolean initStoreValues = false;
    private int storeId;
    private double price;
    private double rating;
    private String storeName;

    public ProductDTO(int productId, String name, Category category, String description) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    //Builder:
    public void initStoreValues(int storeId, double price, double rating, String storeName) {
        this.storeId = storeId;
        this.price = price;
        this.rating = rating;
        this.storeName = storeName;
        this.initStoreValues = true;
    }

    // Getters and setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStoreId() {
        return storeId;
    }
    
    public double getPrice() {
        return price;
    }
    
    public double getRating() {
        return rating;
    }
    
    public String getStoreName() {
        return storeName;
    }
    
    public boolean isInitStoreValues() {
        return initStoreValues;
    }
    
}
