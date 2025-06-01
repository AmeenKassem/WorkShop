package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.SpecialCartItemDTO;
import workshop.demo.DTOs.SpecialType;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.CartItem;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.InfrastructureLayer.StoreRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private IUserRepo userRepo;
    private IAuthRepo authRepo;
    private IStockRepo stockRepo;
    private IStoreRepo storeRepo;
    private final AdminInitilizer adminInitilizer;
    private final AdminHandler adminHandler;

    @Autowired
    public UserService(IUserRepo userRepo, IAuthRepo authRepo, IStockRepo stockRepo, AdminInitilizer adminInitilizer, AdminHandler adminHandler, IStoreRepo storeRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo = storeRepo;
        this.adminInitilizer = adminInitilizer;
        this.adminHandler = adminHandler;
    }

    public String generateGuest() throws UIException, Exception {
        logger.info("generateGuest called");
        int id = userRepo.generateGuest();
        logger.info("Generated guest with ID={}", id);
        return authRepo.generateGuestToken(id);
    }

    public boolean register(String token, String username, String password, int age) throws UIException {
        logger.info("register called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        userRepo.registerUser(username, password, age);
        adminHandler.recordRegisterEvent();
        return true;

    }

    public String login(String token, String username, String pass) throws UIException {
        logger.info("login called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int id = userRepo.login(username, pass);
        //Hmode
        adminHandler.recordLoginEvent();
        //HmodeEnd
        logger.info("User {} logged in .", username);
        return authRepo.generateUserToken(id, username);

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
        int id = userRepo.logoutUser(userName);
        //Hmode
        adminHandler.recordLogoutEvent();
        //HmodeEnd
        logger.info("User {} logged out", userName);
        return authRepo.generateGuestToken(id);
    }

    public boolean setAdmin(String token, String adminKey, int id) throws UIException {
        logger.info("setAdmin called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        return userRepo.setUserAsAdmin(id, adminKey);

    }

    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd, int quantity) throws UIException {
        logger.info("addToUserCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        String storeName = this.storeRepo.getStoreNameById(itemToAdd.getStoreId());
        ItemCartDTO item = new ItemCartDTO(itemToAdd.getStoreId(), itemToAdd.getProductId(), quantity, itemToAdd.getPrice(), itemToAdd.getProductName(), storeName, itemToAdd.getCategory());
        userRepo.addItemToGeustCart(authRepo.getUserId(token), item);
        logger.info("Item added to user cart");
        return true;
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
        userRepo.removeItemFromGeustCart(authRepo.getUserId(token), productId);
        logger.info("Item removed from cart for productId={}", productId);
        return true;
    }

    public SpecialCartItemDTO[] getSpecialCart(String token) throws UIException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        List<UserSpecialItemCart> specialIds = userRepo.getAllSpecialItems(userId);
        List<SpecialCartItemDTO> result = new ArrayList<>();
        for (UserSpecialItemCart item : specialIds) {
            SpecialCartItemDTO itemToSend = new SpecialCartItemDTO();
            itemToSend.setIds(item.storeId, item.specialId, item.bidId, item.type);
            if (item.type == SpecialType.Random) {
                ParticipationInRandomDTO card = stockRepo.getRandomCard(item.storeId, item.specialId, item.bidId);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId, item.specialId, item.type), card.isWinner, card.ended);
            } else {
                SingleBid bid = stockRepo.getBid(item.storeId, item.specialId, item.bidId, item.type);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId, item.specialId, item.type), bid.isWinner() || bid.isAccepted(), bid.isEnded());
            }
            result.add(itemToSend);
        }
        return result.toArray(new SpecialCartItemDTO[0]);
    }

    public ItemCartDTO[] getRegularCart(String token) throws UIException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        //userRepo.checkUserRegisterOnline_ThrowException(userId);
        List<CartItem> regularCartItems = userRepo.getUserCart(userId).getAllCart();
        ItemCartDTO[] dtos = new ItemCartDTO[regularCartItems.size()];
        for (int i = 0; i < regularCartItems.size(); i++) {
            CartItem item = regularCartItems.get(i);
            ItemCartDTO dto = new ItemCartDTO();
            dto.storeId = item.storeId;
            dto.productId = item.productId;
            dto.quantity = item.quantity;
            dto.price = item.price;
            dto.name = item.name;
            dto.storeName = this.storeRepo.getStoreNameById(item.storeId);
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
}
