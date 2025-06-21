package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;

// @Entity
public class ShoppingBasket {

    //private List<ItemCartDTO> itemsOnCart=new ArrayList<>();
    private List<CartItem> itemsOnCart = new ArrayList<>();
    private int storeId;

    

    public ShoppingBasket(int id) {
        storeId = id;
    }

    public void addItem(CartItem item) {
        itemsOnCart.add(item);
    }

    public List<CartItem> getItems() {
        return itemsOnCart;
    }

    public int getStoreId() {
        return storeId;
    }


}
