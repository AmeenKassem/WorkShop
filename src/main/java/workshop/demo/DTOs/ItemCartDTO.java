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

    public ItemCartDTO(int storeId, Category category, int productId, int quantity) {
        this.storeId = storeId;
        this.category = category;
        this.productId = productId;
        this.quantity = quantity;
    }

    public ItemCartDTO(ItemStoreDTO itemToAdd) {
        //TODO Auto-generated constructor stub
    }

    public ItemCartDTO(CartItem item) {
        //TODO Auto-generated constructor stub
    }

    public int getProdutId() {
        return productId;
    }
}
