package workshop.demo.InfrastructureLayer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingCart;

@Repository
public class UserRepository implements IUserRepo {

    private AtomicInteger idGen;
    private ConcurrentHashMap<Integer, Guest> guests; // id -> Guest
    private ConcurrentHashMap<String, Registered> users; // username -> Registered
    private ConcurrentHashMap<Integer, String> idToUsername; // id -> username
    private Encoder encoder;
    private AdminInitilizer adminInit;

    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    @Autowired
    public UserRepository(Encoder encoder, AdminInitilizer adminInit) {
        this.encoder = encoder;
        this.idGen = new AtomicInteger(1);
        this.adminInit = adminInit;
        users = new ConcurrentHashMap<>();
        guests = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();
    }

    @Override
    public int logoutUser(String username) throws UIException {
        if (userExist(username)) {
            Registered user = users.get(username);
            user.logout();
            logger.log(Level.INFO, "User logged out: {0}", username);
            return generateGuest();
        } else {
            logger.log(Level.WARNING, "User not found: {0}", username);
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
    }

    @Override
    public int registerUser(String username, String password, int age) throws UIException {
        if (userExist(username)) {

            throw new UIException("another user try to register with used username", ErrorCodes.USERNAME_USED);
        }
        String encPass = encoder.encodePassword(password);
        int id = idGen.getAndIncrement();
        Registered userToAdd = new Registered(id, username, encPass, age);
        users.put(username, userToAdd);
        idToUsername.put(id, username);
        logger.log(Level.INFO, "User {0} registered successfully", username);
        return id;

    }

    @Override
    public int generateGuest() {
        int id = idGen.getAndIncrement();
        Guest newGuest = new Guest(id);
        guests.put(id, newGuest);
        return id;
    }

    @Override
    public int login(String username, String password) throws UIException {
        if (userExist(username)) {
            Registered user = users.get(username);
            if (user.check(encoder, username, password)) {
                logger.log(Level.INFO, "User logged in: {0}", username);
                return user.getId();
            } else {
                logger.log(Level.WARNING, "Invalid password for user: {0}", username);
                throw new UIException("Incorrect username or password.", ErrorCodes.WRONG_PASSWORD);
            }
        } else {
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
    }

    private boolean userExist(String username) {
        return users.containsKey(username);
    }

    private boolean guestExist(int id) {
        return guests.containsKey(id);
    }

    @Override
    public void addItemToGeustCart(int guestId, ItemCartDTO item) throws UIException {
        if (guestExist(guestId)) {
            Guest geust = guests.get(guestId);
            geust.addToCart(item);
            logger.log(Level.INFO, "Item added to guest cart: {0} for guest id: {1}", new Object[]{item.getProdutId(), guestId});
        } else {
            throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);
        }
    }

    @Override
    public void destroyGuest(int id) {
        guests.remove(id);
        logger.log(Level.INFO, "guest destroyed: {0}", id);
    }

    @Override
    public boolean isAdmin(int id) {
        Registered registered = getRegisteredUser(id);
        return registered != null && registered.isAdmin();
    }

    @Override
    public boolean isRegistered(int id) {
        return getRegisteredUser(id) != null;
    }

    @Override
    public boolean isOnline(int id) {
        Registered registered = getRegisteredUser(id);
        return registered != null && registered.isOnline();
    }

    @Override
    public Registered getRegisteredUser(int id) {
        if (idToUsername.containsKey(id)) {
            String username = idToUsername.get(id);
            if (users.containsKey(username)) {
                return users.get(username);
            } else {
                throw new RuntimeException(new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND));
            }
        }
        return null;
    }

    @Override
    public boolean setUserAsAdmin(int id, String adminKey) {
        Registered registered = getRegisteredUser(id);
        if (registered != null) {
            if (adminInit.matchPassword(adminKey)) {
                registered.setAdmin();
                logger.log(Level.INFO, "User {0} is now an admin.", registered.getUsername());
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeItemFromGeustCart(int guestId, int productId) {
        throw new UnsupportedOperationException("Unimplemented method 'removeItemFromGeustCart'");
    }

    

    @Override
    public ShoppingCart getUserCart(int userId) {
        if (guests.containsKey(userId)) {
            return guests.get(userId).geCart();
        }
        Registered registered = getRegisteredUser(userId);
        if (registered != null) {
            return registered.geCart();
        }
        throw new RuntimeException(new UIException("User with ID " + userId + " not found", ErrorCodes.USER_NOT_FOUND));
    }

    @Override
    public List<ItemCartDTO> getCartForUser(int ownerId) {
        throw new UnsupportedOperationException("Unimplemented method 'getCartForUser'");
    }

    @Override
    public void checkUserRegisterOnline_ThrowException(int userId) throws UIException {
        if (!(isRegistered(userId) && isOnline(userId))) {
            // logger.error("User not logged in for setProductToBid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }

    @Override
    public void checkAdmin_ThrowException(int userId) throws UIException {
        checkUserRegisterOnline_ThrowException(userId);
        if (!isAdmin(userId)) {
            throw new UIException("User is not an admin", ErrorCodes.NO_PERMISSION);
        }
    }

    @Override
    public void checkUserRegister_ThrowException(int userId) throws UIException {
        if (!(isRegistered(userId))) {
            // logger.error("User not logged in for setProductToBid: {}", userId);
            throw new UIException("You are not regestered user!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }

    @Override
    public void addSpecialItemToCart(UserSpecialItemCart item, int userId) throws DevException {
        getRegisteredUser(userId).addSpecialItemToCart(item);
    }

    @Override
    public List<UserSpecialItemCart> getAllSpecialItems(int userId) {
        return getRegisteredUser(userId).getSpecialCart();
    }

    @Override
    public UserDTO getUserDTO(int userId) {
        if (isRegistered(userId)) {
            logger.log(Level.INFO, "getUserDTO for registered user ID={}", userId);
            return getRegisteredUser(userId).getUserDTO();
        } else if (guests.containsKey(userId)) {
            logger.log(Level.INFO, "getUserDTO for guests user ID={}", userId);
            return guests.get(userId).getUserDTO();
        } else {
            throw new RuntimeException(new UIException("User not found with ID: " + userId, ErrorCodes.USER_NOT_FOUND));
        }
    }
}

