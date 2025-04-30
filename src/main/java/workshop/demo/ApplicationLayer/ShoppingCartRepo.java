package workshop.demo.ApplicationLayer;


import java.util.HashMap;

import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.ShoppingCart;

public class ShoppingCartRepo {

    private HashMap<Integer, ShoppingCart> userCarts = new HashMap<>();

    public ShoppingCart getCart(int userId) {
        return userCarts.get(userId);
    }

    public void createCartForUser(int userId) {
        userCarts.putIfAbsent(userId, new ShoppingCart());
    }

    public void addItemToCart(int userId, int storeId, CartItem item) {
        createCartForUser(userId);
        userCarts.get(userId).addItem(storeId, item);
    }
}