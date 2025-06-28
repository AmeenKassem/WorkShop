package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBidDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.Auction;
import workshop.demo.DomainLayer.Stock.IActivePurchasesRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.ParticipationInRandom;
import workshop.demo.DomainLayer.Stock.Product;
import workshop.demo.DomainLayer.Stock.Random;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Stock.UserAuctionBid;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.User.UserSpecialItemCart;
import workshop.demo.InfrastructureLayer.Encoder;
import workshop.demo.InfrastructureLayer.GuestJpaRepository;
import workshop.demo.InfrastructureLayer.IStockRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.UserJpaRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // private IUserRepo userRepo;
    @Autowired
    private IAuthRepo authRepo;

    @Autowired
    private IStockRepoDB stockRepo;
    @Autowired
    private IStoreRepoDB storeRepo;
    @Autowired
    private AdminInitilizer adminInitilizer;
    @Autowired
    private Encoder encoder = new Encoder();

    @Autowired
    private UserJpaRepository regJpaRepo;
    @Autowired
    private GuestJpaRepository guestJpaRepository;

    @Autowired
    private IActivePurchasesRepo activePurchasesRepo;

    public void setGuestJpaRepository(GuestJpaRepository guestJpaRepository) {
        this.guestJpaRepository = guestJpaRepository;
    }

    public void setActivePurchasesRepo(IActivePurchasesRepo activePurchasesRepo) {
        this.activePurchasesRepo = activePurchasesRepo;
    }

    public String generateGuest() throws UIException, Exception {
        logger.info("generateGuest called");
        Guest guest = new Guest();
        guestJpaRepository.save(guest);
        logger.info("Generated guest with ID={}", guest.getId());
        return authRepo.generateGuestToken(guest.getId());

    }

    public boolean register(String token, String username, String password, int age) throws UIException {
        logger.info("register called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        registerUser(username, password, age);
        // adminHandler.recordRegisterEvent();
        return true;

    }

    private int registerUser(String username, String password, int age) throws UIException {
        if (userExist(username)) {
            throw new UIException("another user try to register with used username", ErrorCodes.USERNAME_USED);
        }
        String encPass = encoder.encodePassword(password);
        Registered userToAdd = new Registered(username, encPass, age);

        regJpaRepo.save(userToAdd);
        logger.info("User {0} registered successfully,and persisted!", username);
        return userToAdd.getId();
    }

    public void registerAdminDirectly(String username, String password, int age) throws UIException {
        if (regJpaRepo.findByUsername(username).isPresent()) {
            throw new UIException("Admin user already exists", 1002);
        }

        String encryptedPassword = encoder.encodePassword(password);
        Registered admin = new Registered(username, encryptedPassword, age);
        admin.setAdmin();
        regJpaRepo.save(admin);
    }

    public boolean isAdmin(String username, String password) {
        Optional<Registered> reg = regJpaRepo.findByUsername(username);
        if (!reg.isPresent()) {
            return false;
        }

        Registered user = reg.get();
        boolean passwordMatches = encoder.matches(password, user.getEncodedPass());
        return user.isAdmin() && passwordMatches;
    }

    private boolean userExist(String username) {
        return regJpaRepo.existsByUsername(username) == 1;
    }

    public String login(String token, String username, String pass) throws UIException {
        logger.info("login called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);

        int id = login(username, pass);
        // Hmode
        // adminHandler.recordLoginEvent();
        // HmodeEnd
        logger.info("User {} logged in .", username);
        return authRepo.generateUserToken(id, username);

    }

    private int login(String username, String pass) throws UIException {
        List<Registered> regs = regJpaRepo.findRegisteredUsersByUsername(username);
        if (regs.size() == 1) {
            Registered user = regs.get(0);
            if (encoder.matches(pass, user.getEncodedPass())) {
                user.login();
                regJpaRepo.save(user);
                return user.getId();
            } else {
                throw new UIException("wrong password!!", ErrorCodes.WRONG_PASSWORD);
            }
        } else {
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
    }

    public Boolean destroyGuest(String token) throws UIException {
        logger.info("destroyGuest called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int id = authRepo.getUserId(token);
        logger.info("Destroyed guest with ID={}", id);
        guestJpaRepository.deleteById(id);
        // userRepo.destroyGuest(id);
        return true;

    }

    public String logoutUser(String token) throws UIException {
        logger.info("logoutUser called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        String userName = authRepo.getUserName(token);
        int id = logoutUserLogic(userName);
        // Hmode
        // adminHandler.recordLogoutEvent();
        // HmodeEnd
        logger.info("User {} logged out", userName);
        return authRepo.generateGuestToken(id);
    }

    public int logoutUserLogic(String username) throws UIException {
        List<Registered> regs = regJpaRepo.findRegisteredUsersByUsername(username);
        if (regs.size() == 1) {
            Registered user = regs.get(0);
            user.logout();
            regJpaRepo.save(user);
            logger.info("User logged out: {0}", username);
            return user.getId();
        } else {
            logger.info("User not found: {0}", username);
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
    }

    public boolean setAdmin(String token, String adminKey, int id) throws UIException {
        logger.info("setAdmin called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        return setUserAsAdmin(id, adminKey);

    }

    private boolean setUserAsAdmin(int id, String adminKey) {
        Optional<Registered> registered = regJpaRepo.findById(id);
        if (registered.isPresent()) {
            Registered user = registered.get();
            if (adminInitilizer.matchPassword(adminKey)) {
                user.setAdmin();
                regJpaRepo.save(user);
                logger.info("User {0} is now an admin.", user.getUsername());
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd, int quantity) throws UIException {
        logger.info("addToUserCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);

        ItemCartDTO item = new ItemCartDTO(itemToAdd.getStoreId(), itemToAdd.getProductId(), quantity,
                itemToAdd.getPrice(), itemToAdd.getProductName(), itemToAdd.getStoreName(), itemToAdd.getCategory());
        CartItem itemCart = new CartItem(item);

        Guest user = getUser(userId);

        user.addToCart(itemCart);
        guestJpaRepository.save(user);
        logger.info("Item added to user cart");
        return true;
    }

    private Guest getUser(int userId) throws UIException {
        Optional<Registered> reg = regJpaRepo.findById(userId);
        if (!reg.isPresent()) {
            Optional<Guest> guest = guestJpaRepository.findById(userId);
            if (!guest.isPresent()) {
                throw new UIException("id is not registered or guest", ErrorCodes.USER_NOT_FOUND);
            }
            return guest.get();
        }
        return reg.get();
    }

    @Transactional
    public boolean ModifyCartAddQToBuy(String token, int itemCartId, int quantity) throws UIException {
        logger.info("ModifyCartAddQToBuy called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        Guest user = getUser(userId);
        user.ModifyCartAddQToBuy(itemCartId, quantity);
        // System.out.println(user.geCart().getAllCart().get(0).quantity+"<-quantity");
        guestJpaRepository.saveAndFlush(user);
        // user.geCart().getAllCart().get(0).setQuantity(quantity);
        // userRepo.ModifyCartAddQToBuy(, productId, quantity);

        logger.info("Cart modified for productId={}", itemCartId);
        return true;
    }

    @Transactional
    public boolean removeItemFromCart(String token, int itemCartId) throws UIException {
        logger.info("removeItemFromCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        Guest user = getUser(authRepo.getUserId(token));
        user.removeItem(itemCartId);
        guestJpaRepository.saveAndFlush(user);
        logger.info("Item removed from cart for productId={}", itemCartId);
        return true;
    }

    public SpecialCartItemDTO[] getSpecialCart(String token) throws UIException, Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        Registered user = regJpaRepo.findById(userId)
                .orElseThrow(() -> new UIException("user not dound in reg db!", ErrorCodes.USER_NOT_FOUND));
        List<UserSpecialItemCart> specialIds = user.getSpecialCart();
        List<SpecialCartItemDTO> result = new ArrayList<>();
        for (UserSpecialItemCart item : specialIds) {
            logger.info("one special item found " + item.specialId);
            SpecialCartItemDTO itemToSend = new SpecialCartItemDTO();
            itemToSend.setIds(item.storeId, item.specialId, item.bidId, item.type);
            ActivePurcheses activePurcheses = activePurchasesRepo.findById(item.storeId).orElse(null);
            Store store = storeRepo.findById(item.storeId).orElse(null);
            // System.out.println(store.getStoreName());
            itemToSend.storeName = store.getStoreName();
            // System.out.println("product id is " + item.getProductId());
            Product product = stockRepo.findById(item.getProductId()).orElse(null);
            // if (product == null) {
            // System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadddddddddddddddddddddddddddddddddddddddiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiinnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn");
            // }
            if (item.type == SpecialType.Random) {
                ParticipationInRandomDTO card = activePurcheses.getRandomCard(item.storeId, item.specialId,
                        item.user.getId());
                Random random = activePurcheses.getRandom(item.specialId);
                itemToSend.setValues(product.getName(), card.isWinner, card.ended);
                itemToSend.dateEnd = random.getDateOfEnd(); //TODO , use the same function you have used on RandomDTO 
                itemToSend.quantity = random.getQuantity();
            } else if (item.type == SpecialType.BID) {
                SingleBid bid = activePurcheses.getBid(item.storeId, item.specialId, item.bidId, item.type);
                itemToSend.setValues(product.getName(), bid.isWinner() || bid.isAccepted(), bid.isEnded());
            } else if (item.type == SpecialType.Auction) {
                Auction auction = activePurcheses.getAuctionById(item.specialId);
                itemToSend.setValues(product.getName(), auction.bidIsWinner(item.bidId), auction.isEnded());
                itemToSend.myBid = auction.getBid(item.bidId).getBidPrice();
                itemToSend.maxBid = auction.getMaxBid();
                itemToSend.onTop = auction.bidIsTop(item.bidId);
                itemToSend.dateEnd = auction.getDateOfEnd();
            }
            result.add(itemToSend);
        }
        return result.toArray(new SpecialCartItemDTO[0]);
    }

    @Transactional
    public ItemCartDTO[] getRegularCart(String token) throws UIException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        // checkUserRegisterOnline_ThrowException(userId);
        List<CartItem> regularCartItems = getUser(userId).getCart();
        System.out.println(regularCartItems.size());
        ItemCartDTO[] dtos = new ItemCartDTO[regularCartItems.size()];
        for (int i = 0; i < regularCartItems.size(); i++) {
            CartItem item = regularCartItems.get(i);
            ItemCartDTO dto = new ItemCartDTO();
            dto.storeId = item.storeId;
            dto.productId = item.productId;
            dto.quantity = item.quantity;
            dto.price = item.price;
            dto.name = item.name;
            dto.storeName = storeRepo.findById(item.storeId)
                    .orElseThrow(() -> new UIException("Store with ID " + item.storeId + " not found", -1))
                    .getStoreName();

            // this back
            dto.itemCartId = item.getId();
            dtos[i] = dto;
        }

        return dtos;
    }

    public UserDTO getUserDTO(String token) throws UIException {
        logger.info("getUserDTO");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        Guest user = getUser(userId);
        return user.getUserDTO();
    }

    public List<UserDTO> getAllUsers(String token) throws UIException {
        logger.info("getAllUsers");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        regJpaRepo.findById(userId)
                .orElseThrow(() -> new UIException("user is not registered!", ErrorCodes.USER_NOT_LOGGED_IN));
        List<UserDTO> res = new ArrayList<>();
        regJpaRepo.findAll().forEach((user) -> res.add(user.getUserDTO()));
        return res;
    }

    public Registered checkUserRegisterOnline_ThrowException(int bossId) throws UIException {
        Optional<Registered> regs = regJpaRepo.findById(bossId);
        if (!regs.isPresent()) {
            throw new UIException("user not registered!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return regs.get();
    }

    public void checkAdmin_ThrowException(int adminId) throws UIException {
        Optional<Registered> reg = regJpaRepo.findById(adminId);
        if (!reg.isPresent()) {
            throw new UIException("user is not admin!!", ErrorCodes.NO_PERMISSION);
        }
    }

}
