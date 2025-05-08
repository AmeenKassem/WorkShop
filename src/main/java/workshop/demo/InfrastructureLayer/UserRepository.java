package workshop.demo.InfrastructureLayer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.ShoppingCart;

public class UserRepository implements IUserRepo {

    private AtomicInteger idGen;
    private ConcurrentHashMap<Integer, Guest> guests;
    private ConcurrentHashMap<String, Registered> users;
    private ConcurrentHashMap<Integer, String> idToUsername;
    private Encoder encoder;
    private AdminInitilizer adminInit;

    private static final Logger logger = Logger.getLogger(UserRepository.class.getName());

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
            logger.info("User logged out: " + username);
            return generateGuest();
        } else {
            logger.warning("User not found: " + username);
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
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
    public int login(String username, String password) throws UIException {
        if (userExist(username)) {
            Registered user = users.get(username);
            if (user.check(encoder, username, password)) {
                logger.info("User logged in: " + username);
                return user.getId();
            } else {
                logger.warning("Invalid password for user: " + username);
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
            logger.info("Item added to guest cart: " + item.getProdutId() + " for guest id: " + guestId);
        } else {
            throw new UIException("Guest not found: " + guestId, ErrorCodes.GUEST_NOT_FOUND);
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
        return registered != null && registered.isAdmin();
    }

    @Override
    public boolean isRegistered(int id) {
        return getRegisteredUser(id) != null;
    }

    @Override
    public boolean isOnline(int id) {
        Registered registered = getRegisteredUser(id);
        return registered != null && registered.isOnlien();
    }

    private Registered getRegisteredUser(int id) {
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
                logger.info("User " + registered.getUsername() + " is now an admin.");
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
    public void addBidToRegularCart(SingleBid bid) {
        try {
            getRegisteredUser(bid.getUserId()).addRegularBid(bid);
            logger.info("Bid added to regular cart for user id: " + bid.getUserId());
        } catch (RuntimeException e) {
            logger.warning("User not found: " + bid.getUserId());
            throw new RuntimeException(new UIException("User not found: " + bid.getUserId(), ErrorCodes.USER_NOT_FOUND));
        }
    }

    @Override
    public void addBidToAuctionCart(SingleBid bid) {
        try {
            getRegisteredUser(bid.getUserId()).addAuctionBid(bid);
            logger.info("Bid added to auction cart for user id: " + bid.getUserId());
        } catch (RuntimeException e) {
            logger.warning("User not found: " + bid.getUserId());
            throw new RuntimeException(new UIException("User not found: " + bid.getUserId(), ErrorCodes.USER_NOT_FOUND));
        }
    }

    @Override
    public void ParticipateInRandom(ParticipationInRandomDTO card) {
        getRegisteredUser(card.userId).addParticipationForRandom(card);
    }

    @Override
    public List<SingleBid> getWinningBids(int userId) {
        return getRegisteredUser(userId).getWinningBids();
    }

    @Override
    public List<ParticipationInRandomDTO> getWinningCards(int userId) {
        return getRegisteredUser(userId).getWinningCards();
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
    public void checkUserRegisterOnline(int userId) throws UIException {
        if (!(isRegistered(userId) && isOnline(userId))) {
            // logger.error("User not logged in for setProductToBid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }
}
