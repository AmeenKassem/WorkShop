package workshop.demo.DTOs;

import workshop.demo.DomainLayer.User.CartItem;

public class ItemCartDTO {

    public int storeId;
    public Category category;
    public int productId;
    public int quantity;
    public int price;
    public String name;
    public String description;
    public String storeName;

    public ItemCartDTO(int storeId, Category category, int productId, int quantity, int price, String name, String description, String storeName) {
        this.storeId = storeId;
        this.category = category;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.name = name;
        this.description = description;
    }

    public ItemCartDTO(ItemStoreDTO itemToAdd, int quantity) {
        this.storeId = itemToAdd.getStoreId();
        this.category = itemToAdd.getCategory();
        this.productId = itemToAdd.getProductId();
        this.quantity = quantity;
        this.price = itemToAdd.getPrice();
        // need item name but itemstore doesnt have name 
        // need description but itemstore doesn have description
    }

    public ItemCartDTO() {
    }

    public ItemCartDTO(CartItem item) {
        //TODO Auto-generated constructor stub
    }

    public int getProductId() {
        return productId;
    }
}
