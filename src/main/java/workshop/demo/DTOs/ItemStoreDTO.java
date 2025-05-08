package workshop.demo.DTOs;

public class ItemStoreDTO {

    private int id;
    // public int quantity;
    public int price;
    public Category category;
    public int rank;
    public int storeId;
    public String productName;
    public String storeName;

    public ItemStoreDTO(int id, int quantity, int price, Category category, int rank, int storeId) {
        this.id = id;
        // this.quantity = quantity;
        this.price = price;
        this.category = category;
        this.rank = rank;
        this.storeId = storeId;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // public int getQuantity() {
    //     return quantity;
    // }

    // public void setQuantity(int quantity) {
    //     this.quantity = quantity;
    // }

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

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public static ItemStoreDTO fromJSON(String itemToAddJson) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'fromJSON'");
    }
}
