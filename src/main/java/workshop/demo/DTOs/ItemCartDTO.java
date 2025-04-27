package workshop.demo.DTOs;

public class ItemCartDTO {
    public int id; 
    public int storeId;
    public Category category;
    public int productId;
    public int quantity;
    public int price;
    public int rank;

    public int getId() {
        return id;
    }

    public int getStoreId() {
        return storeId;
    }

    public Category getCategory() {
        return category;
    }

    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
    public int getRank() {
        return rank;
    }
}
