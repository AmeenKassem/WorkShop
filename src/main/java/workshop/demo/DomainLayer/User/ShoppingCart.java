package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

import workshop.demo.DTOs.ItemCartDTO;

public class ShoppingCart {

    private HashMap<Integer, ShoppingBasket> storeBaskets = new HashMap<>();

    public HashMap<Integer, ShoppingBasket> getBaskets() {
        return storeBaskets;
    }

    

    public void addItem(int storeId, ItemCartDTO item) {
        if (!storeBaskets.containsKey(storeId)) {
            storeBaskets.put(storeId, new ShoppingBasket(storeId));
        }
        storeBaskets.get(storeId).addItem(item);
    }

    public List<ItemCartDTO> getAllCart() {
        return storeBaskets.values().stream()
            .flatMap(basket -> basket.getItems().stream())
            .toList();
    }
}
