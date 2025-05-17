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
import workshop.demo.DomainLayer.User.AdminInitilizer;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.UserRepository;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private IUserRepo userRepo;
    private IAuthRepo authRepo;
    private IStockRepo stockRepo;
    private final AdminInitilizer adminInitilizer;

    @Autowired
    public UserService(IUserRepo userRepo, IAuthRepo authRepo,IStockRepo stockRepo,AdminInitilizer adminInitilizer) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.stockRepo = stockRepo;
        this.adminInitilizer = adminInitilizer;

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

    public void registerAdminIfNotExists(String username, String password, int age) throws Exception {
        try {
            int id = userRepo.login(username, password);
            System.out.println("🟡 Admin already exists with username: " + username);
            return;
        } catch (UIException e) {
            System.out.println("🔄 Admin doesn't exist. Proceeding with creation.");
        }

        String guestToken = this.generateGuest();

        this.register(guestToken, username, password, age);

        String userToken = this.login(guestToken, username, password);

        System.out.println(userToken);
        int adminId = userRepo.login(username, password);
        this.setAdmin(userToken, adminInitilizer.getPassword(), adminId);

        //  Print all registered usernames:
        System.out.println(" Registered users:");
        for (String u : ((UserRepository) userRepo).getAllUsernames()) {
            System.out.println(" - " + u);
        }


        System.out.println(" Admin registered and promoted: " + username);
        System.out.println(" All registered usernames: " + userRepo.getAllUsernames().get(0));

    }


}
