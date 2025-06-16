package workshop.demo.DomainLayer.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;

@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public int storeId;
    public int productId;
    public int quantity;
    public int price;
    public String name;

    @Enumerated(EnumType.STRING)
    public Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_cart_id")
    private ShoppingCart shoppingCart;

    public CartItem(ItemCartDTO dto) {
        this.productId = dto.productId;
        this.storeId = dto.storeId;
        this.quantity = dto.quantity;
        this.price = dto.price;
        this.name = dto.name;
        this.category = dto.category;
    }

    public CartItem() {
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

    

    public int getId() {
        return id;
    }

    public void setId(int cartItemId) {
        id = cartItemId;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CartItem) {
            if (((CartItem) other).getId() == this.id)
                return true;
        }
        return false;
    }

}
