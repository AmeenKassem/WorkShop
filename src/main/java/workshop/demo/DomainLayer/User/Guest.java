package workshop.demo.DomainLayer.User;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;

public class Guest {
    private static final Logger logger = LoggerFactory.getLogger(Guest.class);

    private int id;
    private ShoppingCart cart = new ShoppingCart();

    public Guest(int id2) {
        id = id2;
        logger.debug("Guest created with ID={}", id2);

    }

    public int getId() {
        return id;
    }

    public void addToCart(int storeId, ItemCartDTO item) {
        logger.debug("addToCart called (explicit store): guestId={}, storeId={}, product={}", id, storeId,
                item.productId);

        cart.addItem(storeId, item);
    }

    public void clearCart() {
        logger.debug("clearCart called for guestId={}", id);

        cart = new ShoppingCart();
    }

    public void addToCart(ItemCartDTO item) {
        logger.debug("addToCart called (from item.storeId): guestId={}, storeId={}, product={}", id, item.storeId,
                item.productId);

        cart.addItem(item.storeId, item);
    }

    public List<ItemCartDTO> getCart() {
        logger.debug("getCart called for guestId={}, totalItems={}", id, cart.getAllCart().size());

        return cart.getAllCart();
    }

    public ShoppingCart geCart() {
        return cart;
    }

}
