package workshop.demo.DTOs;

public class ProductDTO {

    public int productId;
    public String name;
    public Category category;
    public String description;

    public ProductDTO(int productId, String name, Category category, String description) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.description = description;
    }

    // // store:
    // public boolean initStoreValues = false;
    // public int storeId;
    // public double price;
    // public double rating;
    // public String storeName;

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

    // public double getRating() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getRating'");
    // }
}
