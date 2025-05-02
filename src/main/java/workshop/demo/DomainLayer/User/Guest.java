package workshop.demo.DomainLayer.User;

import java.util.List;

import workshop.demo.DTOs.ItemCartDTO;

public class Guest {
    
    private int id;
    private ShoppingCart cart=new ShoppingCart();
    
    public Guest(int id2) {
        id = id2;
        
    }
    
    public int getId(){
        return id;
    }

    public void addToCart(int storeId, ItemCartDTO item) {
        cart.addItem(storeId,item);
    }

    public void clearCart(){
        cart = new ShoppingCart();
    }

    public void addToCart(ItemCartDTO item) {
        cart.addItem(item.storeId, item);
    }

    public List<ItemCartDTO> getCart() {
        return cart.getAllCart();
    }

    
}
