package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DataAccessLayer.GuestJpaRepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.SingleBid;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.Encoder;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private IUserRepo userRepo;
    private IAuthRepo authRepo;
    private IStockRepo stockRepo;
    private IStoreRepo storeRepo;
    private final AdminInitilizer adminInitilizer;
    // private final AdminHandler adminHandler;
    // @Autowired
    private Encoder encoder = new Encoder();

    private UserJpaRepository regJpaRepo;
    private GuestJpaRepository guestJpaRepository;

    @Autowired
    public UserService(UserJpaRepository regJpaRepo, IUserRepo userRepo, IAuthRepo authRepo, IStockRepo stockRepo,
            AdminInitilizer adminInitilizer,
            IStoreRepo storeRepo, GuestJpaRepository guestRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.adminInitilizer = adminInitilizer;
        // this.adminHandler = adminHandler;
        this.regJpaRepo = regJpaRepo;
        this.guestJpaRepository = guestRepo;
    }

    public String generateGuest() throws UIException, Exception {
        logger.info("generateGuest called");
        Guest guest = new Guest();
        guest = guestJpaRepository.save(guest);

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
        regJpaRepo.save(userToAdd); // ID will be auto-generated
        logger.info("User {0} registered successfully,and persisted!", username);
        return userToAdd.getId();
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
                return user.getId();
            } else
                throw new UIException("wrong password!!", ErrorCodes.WRONG_PASSWORD);
        } else {
            throw new UIException("User not found: " + username, ErrorCodes.USER_NOT_FOUND);
        }
    }

    public Boolean destroyGuest(String token) throws UIException {
        logger.info("destroyGuest called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int id = authRepo.getUserId(token);
        logger.info("Destroyed guest with ID={}", id);
        userRepo.destroyGuest(id);
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
            Registered user =regs.get(0);
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
                logger.info( "User {0} is now an admin.", user.getUsername());
                return true;
            }
        }
        return false;
    }

    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd, int quantity) throws UIException {
        logger.info("addToUserCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId=authRepo.getUserId(token);
        String storeName = this.storeRepo.getStoreNameById(itemToAdd.getStoreId());
        ItemCartDTO item = new ItemCartDTO(itemToAdd.getStoreId(), itemToAdd.getProductId(), quantity,
                itemToAdd.getPrice(), itemToAdd.getProductName(), storeName, itemToAdd.getCategory());
        CartItem itemCart = new CartItem(item);
        Guest user = getUser(userId);
        // itemCart.setGuest(user);
        user.addToCart(itemCart);
        guestJpaRepository.save(user);
        logger.info("Item added to user cart");
        return true;
    }

    private Guest getUser(int userId) throws UIException {
        Optional<Registered> reg = regJpaRepo.findById(userId);
        if(!reg.isPresent()){
            Optional<Guest> guest = guestJpaRepository.findById(userId);
            if(!guest.isPresent()) throw new UIException("id is not registered or guest", ErrorCodes.USER_NOT_FOUND);
            return guest.get();
        }
        return reg.get();
    }

    public boolean ModifyCartAddQToBuy(String token, int productId, int quantity) throws UIException {
        logger.info("ModifyCartAddQToBuy called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        userRepo.ModifyCartAddQToBuy(authRepo.getUserId(token), productId, quantity);
        logger.info("Cart modified for productId={}", productId);
        return true;
    }

    public boolean removeItemFromCart(String token, int productId) throws UIException {
        logger.info("removeItemFromCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        Guest user = getUser(authRepo.getUserId(token));
        user.removeItem(productId);
        guestJpaRepository.save(user);
        logger.info("Item removed from cart for productId={}", productId);
        return true;
    }

    public SpecialCartItemDTO[] getSpecialCart(String token) throws UIException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        checkUserRegisterOnline_ThrowException(userId);
        List<UserSpecialItemCart> specialIds = userRepo.getAllSpecialItems(userId);
        List<SpecialCartItemDTO> result = new ArrayList<>();
        for (UserSpecialItemCart item : specialIds) {
            SpecialCartItemDTO itemToSend = new SpecialCartItemDTO();
            itemToSend.setIds(item.storeId, item.specialId, item.bidId, item.type);
            if (item.type == SpecialType.Random) {
                ParticipationInRandomDTO card = stockRepo.getRandomCard(item.storeId, item.specialId, item.bidId);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId, item.specialId, item.type),
                        card.isWinner, card.ended);
            } else {
                SingleBid bid = stockRepo.getBid(item.storeId, item.specialId, item.bidId, item.type);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId, item.specialId, item.type),
                        bid.isWinner() || bid.isAccepted(), bid.isEnded());
            }
            result.add(itemToSend);
        }
        return result.toArray(new SpecialCartItemDTO[0]);
    }

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
            // dto.storeName = this.storeRepo.getStoreNameById(item.storeId); //TODO return this back
            dtos[i] = dto;
        }

        return dtos;
    }

    public UserDTO getUserDTO(String token) throws UIException {
        logger.info("getUserDTO");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        return userRepo.getUserDTO(userId);
    }

    public List<UserDTO> getAllUsers(String token) throws UIException {
        logger.info("getAllUsers");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkAdmin_ThrowException(userId);
        return userRepo.getAllUserDTOs();
    }

    public void checkUserRegisterOnline_ThrowException(int bossId) throws UIException {
        Optional<Registered> regs = regJpaRepo.findById(bossId);
        if(!regs.isPresent()) throw new UIException("user not registered!", ErrorCodes.USER_NOT_LOGGED_IN);
    }

	public void checkAdmin_ThrowException(int adminId) throws UIException {
		Optional<Registered> reg = regJpaRepo.findById(adminId);
        if(!reg.isPresent()) throw new UIException("user is not admin!!", ErrorCodes.NO_PERMISSION);
	}
}
