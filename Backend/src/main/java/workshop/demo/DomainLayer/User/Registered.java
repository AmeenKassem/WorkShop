package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;

@Entity
@Table(name = "registered")
public class Registered extends Guest {

    private static final Logger logger = LoggerFactory.getLogger(Registered.class);
    @Column(unique = true)
    private String username;
    private String encrybtedPassword;
    private boolean isOnline;
    private int age;
    private RoleOnSystem systemRole = RoleOnSystem.Regular;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserSpecialItemCart> specialCart;

    public Registered(int id2, String username, String encrybtedPassword, int age) {
        super();
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        specialCart = new ArrayList<>();
        this.age = age;
    }

    public Registered() {
        super();
        specialCart = new ArrayList<>();
    }

    public Registered(String username, String encrybtedPassword, int age) {
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        specialCart = new ArrayList<>();
        this.age = age;
    }

    public void setAdmin() {
        systemRole = RoleOnSystem.Admin;
        logger.debug("User {} set as Admin", username);

    }

    public boolean isAdmin() {
        return systemRole == RoleOnSystem.Admin;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void logout() {
        isOnline = false;
        logger.debug("User {} logged out", username);

    }

    public void login() {
        isOnline = true;
        logger.debug("User {} logged in", username);

    }
      public int getage() {
       return age;

    }

    public String getUsername() {
        return username;
    }

    public void addSpecialItemToCart(UserSpecialItemCart item) throws DevException {
        if (item == null) {
            throw new DevException("item is null ");
        }
        logger.debug("adding special item {}:{}:{}:{}", item.storeId, item.specialId, item.bidId, item.type.toString());
        item.user = this;
        for (UserSpecialItemCart userSpecialItemCart : specialCart) {
            if (userSpecialItemCart.bidId == item.bidId && item.specialId == userSpecialItemCart.specialId
                    && item.productId == userSpecialItemCart.productId && userSpecialItemCart.storeId == item.storeId
                    && userSpecialItemCart.type == item.type)
                return;//do not dublicate item!!!
        }
        specialCart.add(item);
    }

    public List<UserSpecialItemCart> getSpecialCart() {
        return specialCart;
    }

    @Override
    public UserDTO getUserDTO() {
        return new UserDTO(this.getId(), this.username, this.age, this.isOnline, this.isAdmin());
    }

    public String getEncodedPass() {
        return encrybtedPassword;
    }

    public void removeSpecialItem(UserSpecialItemCart itemToRemove) {
        specialCart.removeIf(item -> item.storeId == itemToRemove.storeId
                && item.specialId == itemToRemove.specialId
                && item.bidId == itemToRemove.bidId
                && item.type == itemToRemove.type);
    }

    public List<UserSpecialItemCart> getAllSpecialItems() {
        return specialCart;
    }

    public void clearSpecialCart() {
        specialCart.removeAll(specialCart);
    }

    public void clearSpecialCart(List<UserSpecialItemCart> itemsToRemove) {
        specialCart.removeAll(itemsToRemove);
    }


}
