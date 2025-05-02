package workshop.demo.DomainLayer.User;

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

    public void addToCart(int storeId, CartItem item) {
        cart.addItem(storeId,item);
    }

    public void clearCart(){
        cart = new ShoppingCart();
    }

    public void addToCart(ItemCartDTO item) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addToCart'");
    }

    public ShoppingCart getCart() {
        return cart;
    }

    
}
