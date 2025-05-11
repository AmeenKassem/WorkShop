package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.IUserRepo;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private IUserRepo userRepo;
    private IAuthRepo authRepo;

    public UserService(IUserRepo userRepo, IAuthRepo authRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public String generateGuest() throws UIException , Exception{
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

    public boolean setAdmin(String token, String adminKey,int id) throws UIException {
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
}
