package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

public class ShoppingCart {

    private HashMap<Integer, ShoppingBasket> storeBaskets = new HashMap<>();

    public HashMap<Integer, ShoppingBasket> getBaskets() {
        return storeBaskets;
    }
    

    public void addItem(int storeId, CartItem item) {
        if (!storeBaskets.containsKey(storeId)) {
            storeBaskets.put(storeId, new ShoppingBasket(storeId));
        }
        storeBaskets.get(storeId).addItem(item);
    }

    public List<CartItem> getAllCart() {
        return storeBaskets.values().stream()
            .flatMap(basket -> basket.getItems().stream())
            .toList();
    }
}
