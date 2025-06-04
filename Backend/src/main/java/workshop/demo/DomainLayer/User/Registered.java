package workshop.demo.DomainLayer.User;

import java.util.ArrayList;
import java.util.List;

import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.InfrastructureLayer.Encoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;


public class Registered extends Guest {
    private static final Logger logger = LoggerFactory.getLogger(Registered.class);

    @Id
    private String username;
    private String encrybtedPassword;
    private boolean isOnline;
    private int age;
    private RoleOnSystem systemRole = RoleOnSystem.Regular;
    
    
    private List<UserSpecialItemCart> specialCart;

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

    
    public boolean check(Encoder encoder, String username, String password) {
        logger.debug("Registered user created:username={}", username);
        boolean res = encoder.matches(password, encrybtedPassword) && username.equals(this.username);
        logger.debug("Password match result: {}", res);
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

    public void addSpecialItemToCart(UserSpecialItemCart item) throws DevException{
        logger.debug("adding special item {}:{}:{}:{}",item.storeId,item.specialId,item.bidId,item.type.toString());
        if(item==null) throw new DevException("item is null ");
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
