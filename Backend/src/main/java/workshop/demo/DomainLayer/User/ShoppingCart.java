package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Embeddable;
public class ShoppingCart {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCart.class);

    private HashMap<Integer, ShoppingBasket> storeBaskets = new HashMap<>();

    public HashMap<Integer, ShoppingBasket> getBaskets() {
        return storeBaskets;
    }

    public void addItem(int storeId, CartItem item) {
        logger.debug("addItem called: storeId={}, item={}", storeId, item);

        if (!storeBaskets.containsKey(storeId)) {
            logger.debug("No basket found for storeId={}.", storeId);

            storeBaskets.put(storeId, new ShoppingBasket(storeId));
        }
        logger.debug("Item added to basket: storeId={}.", storeId);

        storeBaskets.get(storeId).addItem(item);
    }

    public List<CartItem> getAllCart() {
        logger.debug("getAllCart called. Total baskets={}", storeBaskets.size());

        return storeBaskets.values().stream()
                .flatMap(basket -> basket.getItems().stream())
                .toList();
    }

    public void ModifyCartAddQToBuy(int productId, int quantity) {
        logger.debug("ModifyCartAddQToBuy called: productId={}, quantity={}", productId, quantity);

        for (ShoppingBasket basket : storeBaskets.values()) {
            if (basket.getItems().stream().anyMatch(item -> item.productId == productId)) {
                basket.ModifyCartAddQToBuy(productId, quantity);
                return;
            }
        }
        logger.debug("Product not found in any basket: productId={}", productId);
    }
    public  void clear(){
          storeBaskets = new HashMap<>();


    }
}
