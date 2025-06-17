package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

@Entity
public class ShoppingCart {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCart.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;

    public ShoppingCart() {
    }

    // public HashMap<Integer, ShoppingBasket> getBaskets() {
    // return storeBaskets;
    // }

    @PostLoad
    public void buildCartFromItems() {
        // cart = new ShoppingCart();
        // for (CartItem item : cartItems) {
        // cart.addItem(item.storeId, item);
        // }
    }

    public void addItem(int storeId, CartItem item) {
        logger.debug("addItem called: storeId={}, item={}", storeId, item);

        // if (!storeBaskets.containsKey(storeId)) {
        // logger.debug("No basket found for storeId={}.", storeId);

        // storeBaskets.put(storeId, new ShoppingBasket(storeId));
        // }
        logger.debug("Item added to basket: storeId={}.", storeId);

        cartItems.add(item);
        item.setCart(this);
    }

    public List<CartItem> getAllCart() {
        logger.debug("getAllCart called. Total baskets={}", cartItems.size());

        return cartItems;
    }

    public void ModifyCartAddQToBuy(int cartItemId, int quantity) {
        logger.debug("ModifyCartAddQToBuy called: cartItemId={}, quantity={}", cartItemId, quantity);
        for (CartItem cartItem : cartItems) {
            if (cartItem.getProductId() == cartItemId) {
                // cartItem.setCart(this);
                System.out.println("curr quantity :"+cartItem.quantity);
                cartItem.setQuantity(quantity);
                if (quantity == 0)
                    removeItem(cartItemId);
                System.out.println("-------------------------------------------------------------"+(cartItem.getShoppingCart()==this));
            }
        }

        // logger.debug("Product not found in any basket: item cart Id={}", cartItemId);
    }

    public void removeItem(int cartItemId) {
        CartItem itemToRemove = new CartItem();
        itemToRemove.setId(cartItemId);
        cartItems.remove(itemToRemove);
    }

    public void clear() {
        cartItems.removeAll(cartItems);
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public Collection<ShoppingBasket> getBaskets() {
        HashMap<Integer, ShoppingBasket> baskets = new HashMap<>();
        for (CartItem cartItem : cartItems) {
            if (!baskets.containsKey(cartItem.storeId))
                baskets.put(cartItem.storeId, new ShoppingBasket(cartItem.storeId));
            baskets.get(cartItem.storeId).addItem(cartItem);
        }
        return baskets.values();
    }

    public void setGuest(Guest guest){
        this.guest=guest;
    }
}
