package workshop.demo.DomainLayer.User;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;

public class CartItem {

    public int productId;
    public int storeId;
    public int quantity;
    public int price;
    public String name;
    public Category category;

    public CartItem(ItemCartDTO dto) {
        this.productId = dto.productId;
        this.storeId = dto.storeId;
        this.quantity = dto.quantity;
        this.price = dto.price;
        this.name = dto.name;
        this.category =dto.category;
    }

//    // Optional manual constructor
//    public CartItem(int productId, int quantity, Category cat) {
//        this.productId = productId;
//        this.quantity = quantity;
//    }

    // Getters
    public int getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

}
