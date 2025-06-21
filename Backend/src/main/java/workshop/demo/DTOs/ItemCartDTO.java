package workshop.demo.DTOs;

import workshop.demo.DomainLayer.User.CartItem;

public class ItemCartDTO {

    public int storeId;
    public int productId;
    public int quantity;
    public int price;
    public String name;
    public String storeName;
    public Category category;
    public int itemCartId;

    public ItemCartDTO(int storeId, int productId, int quantity, int price, String name, String storeName,Category category) {
        this.storeId = storeId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.name = name;
        this.storeName =storeName;
        this.category=category;
    }
//
//    public ItemCartDTO(ItemStoreDTO itemToAdd, int quantity) {
//        this.storeId = itemToAdd.getStoreId();
//        this.productId = itemToAdd.getProductId();
//        this.quantity = quantity;
//        this.price = itemToAdd.getPrice();
//        // need item name but itemstore doesnt have name
//        // need description but itemstore doesn have description
//    }

    public ItemCartDTO() {
    }

    public int getProductId() {
        return productId;
    }
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }


    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public int getId() {
        return itemCartId;
    }
}
