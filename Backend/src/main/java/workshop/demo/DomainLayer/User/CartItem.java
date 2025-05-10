package workshop.demo.DomainLayer.User;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;

public class CartItem {

    public int productId;
    public int storeId;
    public Category category;
    public int quantity;
    public int price;
    public String name;
    public String description;

    
    public CartItem(ItemCartDTO dto) {
        this.productId = dto.productId;
        this.storeId = dto.storeId;
        this.category = dto.category;
        this.quantity = dto.quantity;
        this.price = dto.price;
        this.name = dto.name;
        this.description = dto.description;
    }

    // Optional manual constructor
    public CartItem(int productId, int quantity, Category cat) {
        this.productId = productId;
        this.quantity = quantity;
        this.category = cat;
    }

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

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
