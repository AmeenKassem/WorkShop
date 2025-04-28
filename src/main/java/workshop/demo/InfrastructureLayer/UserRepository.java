package workshop.demo.InfrastructureLayer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.GuestNotFoundException;
import workshop.demo.DomainLayer.Exceptions.IncorrectLogin;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Exceptions.UserIdNotFound;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;

public class UserRepository implements IUserRepo {

    private AtomicInteger idGen;
    private ConcurrentHashMap<Integer, Guest> guests;
    private ConcurrentHashMap<String, Registered> users;
    private ConcurrentHashMap<Integer, String> idToUsername;
    private Encoder encoder;
    // @Autowired
    private AdminInitilizer adminInit;

    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());


    public UserRepository(Encoder encoder,AdminInitilizer adminInit) {
        this.encoder = encoder;
        this.idGen = new AtomicInteger(1); // Start from 1 to avoid 0 as a valid ID
        this.adminInit=adminInit;
        users = new ConcurrentHashMap<>();
        guests = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();
    }

    @Override
    public int logoutUser(String username) {
        if(userExist(username)){
            Registered user = users.get(username);
            user.logout();
            logger.info("User logged out: " + username);
            return generateGuest();
        }else
            logger.warning("User not found: " + username);
            throw new UserIdNotFound(username);
    }

    @Override
    public int registerUser(String username, String password) {
        if (validPassword(username, password)) {
            String encPass = encoder.encodePassword(password);
            int id = idGen.getAndIncrement();
            Registered userToAdd = new Registered(id, username, encPass);
            users.put(username, userToAdd);
            idToUsername.put(id, username);
            logger.info("User " + username + " registered successfully");
            return id;
        }
        logger.warning("Invalid password for user: " + username);
        return -1;
    }

    private boolean validPassword(String username, String password) {
        return !users.containsKey(username);
    }

    @Override
    public int generateGuest() {
        int id = idGen.getAndIncrement();
        Guest newGuest = new Guest(id);
        guests.put(id, newGuest);
        return id;
    }

    @Override
    public int login(String username, String password) {
        if (userExist(username)) {
            Registered user = users.get(username);
            if (user.check(encoder, username, password)) {
                logger.info("User logged in: " + username);
                return user.getId();
            } else {
                logger.warning("Invalid password for user: " + username);
                throw new IncorrectLogin();
            }
        } else {
            throw new UserIdNotFound(username);
        }
    }

    private boolean userExist(String username) {
        return users.containsKey(username);
    }

    private boolean guestExist(int id) {
        return guests.containsKey(id);
    }

    @Override
    public void addItemToGeustCart(int guestId, ItemCartDTO item) {
        if (guestExist(guestId)) {
            Guest geust = guests.get(guestId);
            geust.addToCart(item);
            logger.info("Item added to guest cart: " + item.getProdutId() + " for guest id: " + guestId);
        } else {
            throw new GuestNotFoundException(guestId);
        }
    }

    @Override
    public void destroyGuest(int id) {
        guests.remove(id);
        logger.info("guest destroyed: " + id);
    }

    @Override
    public boolean isAdmin(int id) {
        Registered registered = getRegisteredUser(id);
        if(registered!=null)
            return registered.isAdmin();
        return false;
    }

    @Override
    public boolean isRegistered(int id) {
        return getRegisteredUser(id)!=null;
        
    }

    @Override
    public boolean isOnline(int id) {
        Registered registered = getRegisteredUser(id);
        if(registered!=null)
            return registered.isOnlien();
        return false;
    }

    private Registered getRegisteredUser(int id){
        if(idToUsername.containsKey(id)){
            String username = idToUsername.get(id);
            if(users.containsKey(username)){
                return users.get(username);
            }
            else throw new UserIdNotFound(username);
        }
        return null;
    }

	@Override
	public boolean setUserAsAdmin(int id, String adminKey) {
		Registered registered = getRegisteredUser(id);
        if(registered!=null){
            if(adminInit.matchPassword(adminKey)){
                registered.setAdmin();
                logger.info("User " + registered.getUsername() + " is now an admin.");
                return true;
            }
        }
        return false;
	}

    @Override
    public void addBidToSpecialCart(SingleBid bid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addBidToSpecialCart'");
    }

    @Override
    public void removeItemFromGeustCart(int guestId, int productId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeItemFromGeustCart'");
    }

}
