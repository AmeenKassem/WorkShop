package workshop.demo.DomainLayer.User;

import workshop.demo.DTOs.Category;

public class CartItem {

    
    public int productId;
    
    public Category category;

    public int quantity;

    public int price;

    public String name;
    
    public CartItem(int productId2, int quantity2,Category cat) {
        this.productId = productId2;
        this.quantity=quantity2;
        category = cat;
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
    
    public String getName() {
        return name;
    }
}
