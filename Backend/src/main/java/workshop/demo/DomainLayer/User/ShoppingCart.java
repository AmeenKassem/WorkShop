<<<<<<< HEAD:src/main/java/workshop/demo/DomainLayer/User/ShoppingCart.java
package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;

public class ShoppingCart {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCart.class);

    private HashMap<Integer, ShoppingBasket> storeBaskets = new HashMap<>();

    public HashMap<Integer, ShoppingBasket> getBaskets() {
        return storeBaskets;
    }

    public void addItem(int storeId, ItemCartDTO item) {
        logger.debug("addItem called: storeId={}, item={}", storeId, item);

        if (!storeBaskets.containsKey(storeId)) {
            logger.debug("No basket found for storeId={}.", storeId);

            storeBaskets.put(storeId, new ShoppingBasket(storeId));
        }
        logger.debug("Item added to basket: storeId={}.", storeId);

        storeBaskets.get(storeId).addItem(item);
    }

    public List<ItemCartDTO> getAllCart() {
        logger.debug("getAllCart called. Total baskets={}", storeBaskets.size());

        return storeBaskets.values().stream()
                .flatMap(basket -> basket.getItems().stream())
                .toList();
    }
}
=======
package workshop.demo.DomainLayer.User;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;

public class ShoppingCart {
    private static final Logger logger = LoggerFactory.getLogger(ShoppingCart.class);

    private HashMap<Integer, ShoppingBasket> storeBaskets = new HashMap<>();

    public HashMap<Integer, ShoppingBasket> getBaskets() {
        return storeBaskets;
    }

    public void addItem(int storeId, ItemCartDTO item) {
        logger.debug("addItem called: storeId={}, item={}", storeId, item);

        if (!storeBaskets.containsKey(storeId)) {
            logger.debug("No basket found for storeId={}.", storeId);

            storeBaskets.put(storeId, new ShoppingBasket(storeId));
        }
        logger.debug("Item added to basket: storeId={}.", storeId);

        storeBaskets.get(storeId).addItem(item);
    }

    public List<ItemCartDTO> getAllCart() {
        logger.debug("getAllCart called. Total baskets={}", storeBaskets.size());

        return storeBaskets.values().stream()
                .flatMap(basket -> basket.getItems().stream())
                .toList();
    }
}
>>>>>>> main:Backend/src/main/java/workshop/demo/DomainLayer/User/ShoppingCart.java
