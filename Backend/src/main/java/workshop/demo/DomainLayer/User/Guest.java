package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.UserDTO;

@Entity
@Table(name = "guest")
@Inheritance(strategy = InheritanceType.JOINED)
public class Guest {

    private static final Logger logger = LoggerFactory.getLogger(Guest.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(mappedBy = "guest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ShoppingCart cart ;

    public Guest(int id2) {
        id = id2;
        logger.debug("Guest created with ID={}", id2);

    }

    public Guest() {
    }

    public int getId() {
        return id;
    }

    public void addToCart(int storeId, CartItem item) {
        logger.debug("addToCart called (explicit store): guestId={}, storeId={}, product={}", id, storeId,
                item.productId);
        // cartItems.add(item);
        cart.addItem(storeId, item);
    }

    public void addToCart(CartItem item) {
        logger.debug("addToCart called (from item.storeId): guestId={}, storeId={}, product={}", id, item.storeId,
                item.productId);
        // cartItems.add(item);
        cart.addItem(item.storeId, item);
    }

    public List<CartItem> getCart() {
        // if (cart == null)
        // buildCartFromItems();
        logger.debug("getCart called for guestId={}, totalItems={}", id, cart.getAllCart().size());

        return cart.getAllCart();
    }

    public ShoppingCart geCart() {
        return cart;
    }

    public UserDTO getUserDTO() {
        return new UserDTO(this.id);
    }

    public void ModifyCartAddQToBuy(int productId, int quantity) {
        logger.debug("ModifyCartAddQToBuy called for guestId={}, productId={}, quantity={}", id, productId, quantity);

        cart.ModifyCartAddQToBuy(productId, quantity);
    }

    public void removeItem(int cartItemId) {
        logger.debug("removeItem called for guestId={}, productId={}", id, cartItemId);
        cart.removeItem(cartItemId);
    }

    public void clearCart() {
        cart.clear();
    }

    public boolean emptyCart() {
        return cart.isEmpty();
    }

    public Collection<ShoppingBasket> getBaskets() {
        return cart.getBaskets();
    }

}
