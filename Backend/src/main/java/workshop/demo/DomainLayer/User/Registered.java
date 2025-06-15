package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.InfrastructureLayer.Encoder;

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
    @Transient
    private List<UserSpecialItemCart> specialCart;
    private Encoder encoder = new Encoder();
    public Registered(int id2, String username, String encrybtedPassword, int age) {

        super(id2);
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        // regularBids = new ArrayList<SingleBid>();
        // auctionBids = new ArrayList<SingleBid>();
        // participationsOnRandoms = new ArrayList<ParticipationInRandomDTO>();
        specialCart = new ArrayList<>();
        this.age = age;
    }

    public Registered() {
    }

    public Registered(String username, String encrybtedPassword, int age) {
        this.username = username;
        this.encrybtedPassword = encrybtedPassword;
        // regularBids = new ArrayList<SingleBid>();
        // auctionBids = new ArrayList<SingleBid>();
        // participationsOnRandoms = new ArrayList<ParticipationInRandomDTO>();
        specialCart = new ArrayList<>();
        this.age = age;
    }

    

    public boolean login(String username, String password) {
        boolean res = encoder.matches(password, encrybtedPassword) && username.equals(this.username);
        if (res) {
            login();
        }
        return res;
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

    public String getUsername() {
        return username;
    }

    public void addSpecialItemToCart(UserSpecialItemCart item) throws DevException {
        logger.debug("adding special item {}:{}:{}:{}", item.storeId, item.specialId, item.bidId, item.type.toString());
        if (item == null) {
            throw new DevException("item is null ");
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

}
