package workshop.demo.DTOs;

public class ItemStoreDTO {

    private int productId;
    private int quantity;
    private int price;
    private Category category;
    private int rank;
    private int storeId;
    private String productName;
    private String storeName;

    public ItemStoreDTO(int id, int quantity, int price, Category category, int rank, int storeId, String productName,
            String storeName) {
        this.productId = id;
        this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.rank = rank;
        this.storeId = storeId;
        this.productName = productName;
        this.storeName = storeName;

    }

    public ItemStoreDTO() {
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int id) {
        this.productId = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;

    }
}
