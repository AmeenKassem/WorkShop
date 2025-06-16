package workshop.demo.InfrastructureLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
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
    // private Encoder encoder;
    private AdminInitilizer adminInit;
    @Autowired
    private UserJpaRepository regJpaRepo;
    @Autowired
    private GuestJpaRepository guestJpaRepository;
    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

    @Autowired
    public UserRepository(AdminInitilizer adminInit) {
        this.idGen = new AtomicInteger(1);
        this.adminInit = adminInit;
        users = new ConcurrentHashMap<>();
        guests = new ConcurrentHashMap<>();
        idToUsername = new ConcurrentHashMap<>();
    }

    public List<String> getAllUsernames() {
        return new ArrayList<>(users.keySet());
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

    // @Override
    // public int registerUser(String username, String password, int age) throws
    // UIException {
    // if (userExist(username)) {
    // throw new UIException("another user try to register with used username",
    // ErrorCodes.USERNAME_USED);
    // }
    // String encPass = encoder.encodePassword(password);
    // int id = idGen.getAndIncrement();
    // Registered userToAdd = new Registered(id, username, encPass, age);
    // users.put(username, userToAdd);
    // idToUsername.put(id, username);
    // jpaRepo.save(userToAdd);
    // logger.log(Level.INFO, "User {0} registered successfully", username);
    // return id;
    // }
    // @Override
    // public int registerUser(String username, String encPass, int age) throws UIException {
    //     if (userExist(username)) {
    //         throw new UIException("another user try to register with used username", ErrorCodes.USERNAME_USED);
    //     }
    //     // String encPass = encoder.encodePassword(password);

    //     // jpa handels id:
    //     Registered userToAdd = new Registered(username, encPass, age);

    //     regJpaRepo.save(userToAdd); // ID will be auto-generated

    //     users.put(username, userToAdd);
    //     idToUsername.put(userToAdd.getId(), username); // getId() after save()

    //     logger.log(Level.INFO, "User {0} registered successfully", username);
    //     return userToAdd.getId();
    // }

    @Override
    public int generateGuest() {
        // int id = idGen.getAndIncrement();
        // Guest newGuest = new Guest(id);
        // guests.put(id, newGuest);
        // guestJpaRepository.save(newGuest);
        // return id;
        Guest newGuest = new Guest(); // No ID passed
        Guest saved = guestJpaRepository.save(newGuest); // Hibernate assigns ID
        guests.put(saved.getId(), saved); // Use the generated ID
        return saved.getId();
    }

    // @Override
    // public int login(String username, String password) throws UIException {
    //     List<Registered> regs = regJpaRepo.findRegisteredUsersByUsername(username);
    //     if (regs.size()==1) {
    //         Registered user = regs.get(0);
    //         if(user.login(username, password)) return user.getId();
    //         else throw new UIException("wrong password!!", ErrorCodes.WRONG_PASSWORD);
    //     } else {
    //         throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
    //     }
    // }

    private boolean userExist(String username) {
        return regJpaRepo.existsByUsername(username)==1;
    }

    private boolean userExist(int userid) {
        return idToUsername.containsKey(userid);
    }
    // changed from private to public

    public boolean guestExist(int id) {
        return guestExist(id);
    }
    // changed it to handle users as well
    // added fucntion userexists that takes userid and returns name

    // @Override
    // public void addItemToGeustCart(int guestId, ItemCartDTO item) throws UIException {
    //     CartItem itemCart = new CartItem(item);
    //     if (guestExist(guestId)) {
    //         Guest geust = guests.get(guestId);
    //         geust.addToCart(itemCart);
    //         logger.log(Level.INFO, "Item added to guest cart: {0} for guest id: {1}",
    //                 new Object[] { item.getProductId(), guestId });
    //     } else if (userExist(guestId)) {
    //         getRegisteredUser(guestId).addToCart(itemCart);
    //         logger.log(Level.INFO, "Item added to guest cart: {0} for guest id: {1}",
    //                 new Object[] { item.getProductId(), guestId });
    //     } else {
    //         throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);

    //     }
    // }

    @Override
    public void ModifyCartAddQToBuy(int guestId, int productId, int quantity) throws UIException {
        if (guestExist(guestId)) {
            Guest geust = guests.get(guestId);
            geust.ModifyCartAddQToBuy(productId, quantity);
            logger.log(Level.INFO, "Item modified in guest cart: {0} for guest id: {1}",
                    new Object[] { productId, guestId });
        } else if (userExist(guestId)) {
            getRegisteredUser(guestId).ModifyCartAddQToBuy(productId, quantity);
            logger.log(Level.INFO, "Item modified in guest cart: {0} for guest id: {1}",
                    new Object[] { productId, guestId });
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
    public boolean isAdmin(int id) throws UIException {
        Registered registered = getRegisteredUser(id);
        return registered != null && registered.isAdmin();
    }

    @Override
    public boolean isRegistered(int id) throws UIException {
        return getRegisteredUser(id) != null;
    }

    @Override
    public boolean isOnline(int id) throws UIException {
        Registered registered = getRegisteredUser(id);
        return registered != null && registered.isOnline();
    }

    @Override
    public Registered getRegisteredUser(int id) throws UIException {
        if (idToUsername.containsKey(id)) {
            String username = idToUsername.get(id);
            if (users.containsKey(username)) {
                return users.get(username);
            } else {
                throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
            }
        }
        return null;
    }

    @Override
    public boolean setUserAsAdmin(int id, String adminKey) throws UIException {
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

    // @Override
    // public void removeItemFromGeustCart(int guestId, int productId) throws UIException {
    //     if (guestExist(guestId)) {
    //         Guest geust = guests.get(guestId);
    //         geust.removeItem(productId);
    //         logger.log(Level.INFO, "Item removed from guest cart: {0} for guest id: {1}",
    //                 new Object[] { productId, guestId });
    //     } else if (userExist(guestId)) {
    //         getRegisteredUser(guestId).removeItem(productId);
    //         logger.log(Level.INFO, "Item removed from guest cart: {0} for guest id: {1}",
    //                 new Object[] { productId, guestId });
    //     } else {
    //         throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);

    //     }
    // }

    @Override
    public ShoppingCart getUserCart(int userId) throws UIException {
        if (guests.containsKey(userId)) {
            return guests.get(userId).geCart();
        }
        Registered registered = getRegisteredUser(userId);
        if (registered != null) {
            return registered.geCart();
        }
        throw new UIException("User with ID " + userId + " not found", ErrorCodes.USER_NOT_FOUND);
    }

    // @Override
    // public List<ItemCartDTO> getCartForUser(int ownerId) {
    // // throw new UnsupportedOperationException("Unimplemented method
    // 'getCartForUser'");
    // }
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
    public void addSpecialItemToCart(UserSpecialItemCart item, int userId) throws DevException, UIException {
        getRegisteredUser(userId).addSpecialItemToCart(item);
    }

    @Override
    public List<UserSpecialItemCart> getAllSpecialItems(int userId) throws UIException {
        return getRegisteredUser(userId).getSpecialCart();
    }

    @Override
    public UserDTO getUserDTO(int userId) throws UIException {
        if (isRegistered(userId)) {
            logger.log(Level.INFO, "getUserDTO for registered user ID={}", userId);
            return getRegisteredUser(userId).getUserDTO();
        } else if (guests.containsKey(userId)) {
            logger.log(Level.INFO, "getUserDTO for guests user ID={}", userId);
            return guests.get(userId).getUserDTO();
        } else {
            throw new UIException("User not found with ID: " + userId, ErrorCodes.USER_NOT_FOUND);
        }
    }

    @Override
    public List<UserDTO> getAllUserDTOs() {
        List<UserDTO> result = new ArrayList<>();
        for (String username : users.keySet()) {
            Registered user = users.get(username);
            result.add(user.getUserDTO());
        }
        return result;
    }

    public void clear() {
        guests.clear();

        users.clear();

        idToUsername.clear();

        idGen.set(1); // Reset to starting ID

    }

    @Override
    public Registered getRegisteredUserByName(String name) throws UIException {
        Registered user = users.get(name);
        if (user == null) {
            throw new UIException("No user found with username: " + name, ErrorCodes.USER_NOT_FOUND);
        }
        return user;
    }

    public void removeSpecialItem(int userId, UserSpecialItemCart itemToRemove) throws UIException {
        Registered user = getRegisteredUser(userId);

        user.getSpecialCart().removeIf(item -> item.storeId == itemToRemove.storeId
                && item.specialId == itemToRemove.specialId
                && item.bidId == itemToRemove.bidId
                && item.type == itemToRemove.type);
    }

    public void removeBoughtSpecialItems(int userId, List<SingleBid> winningBids,
            List<ParticipationInRandomDTO> winningRandoms) throws UIException {
        Registered user = getRegisteredUser(userId);
        List<UserSpecialItemCart> cart = user.getSpecialCart();

        for (SingleBid bid : winningBids) {
            cart.removeIf(item -> item.storeId == bid.getStoreId()
                    && item.specialId == bid.getSpecialId()
                    && item.bidId == bid.getId()
                    && item.type == bid.getType());
        }

        for (ParticipationInRandomDTO card : winningRandoms) {
            cart.removeIf(item -> item.storeId == card.storeId
                    && item.specialId == card.randomId
                    && item.type == SpecialType.Random);
        }

        logger.log(Level.INFO, "Removed bought special items from user {}'s cart", userId);
    }

}
