package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.*;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private IUserRepo userRepo;
    private IAuthRepo authRepo;
    private IStockRepo stockRepo;
    //Hmode
    private IStoreRepo storeRepo;

    @Autowired
    public UserService(IUserRepo userRepo, IAuthRepo authRepo,IStockRepo stockRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
    }
    //Hmode
    public UserService(IUserRepo userRepo, IAuthRepo authRepo,IStockRepo stockRepo,IStoreRepo storeRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.storeRepo=storeRepo;
    }

    public String generateGuest() throws UIException, Exception {
        logger.info("generateGuest called");
        int id = userRepo.generateGuest();
        logger.info("Generated guest with ID={}", id);
        return authRepo.generateGuestToken(id);
    }

    public void register(String token, String username, String password,int age) throws UIException {
        logger.info("register called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        userRepo.registerUser(username, password,age);
        
    }

    public String login(String token, String username, String pass) throws UIException {
        logger.info("login called for username={}", username);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int id = userRepo.login(username, pass);
        logger.info("User {} logged in .", username);
        return authRepo.generateUserToken(id, username);
      
    }

    public void destroyGuest(String token) throws UIException {
        logger.info("destroyGuest called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int id = authRepo.getUserId(token);
        logger.info("Destroyed guest with ID={}", id);
        userRepo.destroyGuest(id);

    }

    public String logoutUser(String token) throws UIException {
        logger.info("logoutUser called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        String userName = authRepo.getUserName(token);
        int id = userRepo.logoutUser(userName);
        logger.info("User {} logged out", userName);
        return authRepo.generateGuestToken(id);
    }

    public boolean setAdmin(String token, String adminKey, int id) throws UIException {
        logger.info("setAdmin called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        logger.info("User {} set as admin: {}");
        return userRepo.setUserAsAdmin(id, adminKey);

    }

    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd) throws UIException {
        logger.info("addToUserCart called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        ItemCartDTO item = new ItemCartDTO(itemToAdd);
        userRepo.addItemToGeustCart(authRepo.getUserId(token), item);
        logger.info("Item added to user cart");
        return true;
    }


    public SpecialCartItemDTO[] getSpecialCart(String token) throws UIException{
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        List<UserSpecialItemCart> specialIds = userRepo.getAllSpecialItems(userId);
        List<SpecialCartItemDTO> result = new ArrayList<>();
        for (UserSpecialItemCart item : specialIds) {
            SpecialCartItemDTO itemToSend = new SpecialCartItemDTO();
            itemToSend.setIds(item.storeId , item.specialId, item.bidId,item.type);
            if(item.type==SpecialType.Random){  
                ParticipationInRandomDTO card = stockRepo.getRandomCard(item.storeId,item.specialId,item.bidId);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId,item.specialId,item.type), card.isWinner, card.ended);
            }else{
                SingleBid bid = stockRepo.getBid(item.storeId,item.specialId,item.bidId,item.type);
                itemToSend.setValues(stockRepo.GetProductNameForBid(item.storeId,item.specialId,item.type), bid.isWinner()||bid.isAccepted(), bid.isEnded());
            }
            result.add(itemToSend);
        }
        return result.toArray(new SpecialCartItemDTO[0]);
    }


    public UserDTO getUserDTO(String token) throws UIException {
        logger.info("getUserDTO");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        return userRepo.getUserDTO(userId);
    }
    public List<StoreInfoDTO> viewSystemInfo(String token) throws UIException, DevException {
        logger.info("viewSystemInfo called");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        List<StoreDTO> allStores = storeRepo.viewAllStores();
        List<StoreDTO> activeStores = new ArrayList<>();
        for (StoreDTO store : allStores) {
            if (store.active) {
                activeStores.add(store);
            }
        }
        List<StoreInfoDTO> result = new ArrayList<>();
        for (StoreDTO store : activeStores) {
            int storeId = store.storeId;
            List<ItemStoreDTO> storeItems = stockRepo.getProductsInStore(storeId);
            List<ProductInfoDTO> products = new ArrayList<>();
            for (ItemStoreDTO item : storeItems) {
                String productName = item.productName;
                int quantity = item.getQuantity();
                products.add(new ProductInfoDTO(productName, quantity));
            }
            StoreInfoDTO storeInfo = new StoreInfoDTO(store.storeName, products);
            result.add(storeInfo);
        }
        logger.info("viewSystemInfo: {} active stores found", result.size());
        return result;
    }
}
