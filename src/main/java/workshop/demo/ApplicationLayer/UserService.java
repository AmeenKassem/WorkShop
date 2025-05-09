package workshop.demo.ApplicationLayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
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

    public void register(String token, String username, String password) throws UIException {
        logger.info("register called for username={}", username);

        if (authRepo.validToken(token)) {
            logger.info("User {} registered", username);

            userRepo.registerUser(username, password);
        } else {
            logger.error("Invalid token in register");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public String login(String token, String username, String pass) throws UIException {
        logger.info("login called for username={}", username);

        if (authRepo.validToken(token)) {
            int id = userRepo.login(username, pass);
            logger.info("User {} logged in .", username);

            return authRepo.generateUserToken(id, username);
        } else {
            logger.error("Invalid token in login");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public void destroyGuest(String token) throws UIException {
        logger.info("destroyGuest called");

        if (authRepo.validToken(token)) {
            int id = authRepo.getUserId(token);
            logger.info("Destroyed guest with ID={}", id);

            userRepo.destroyGuest(id);
            
        } else {
            logger.error("Invalid token in destroyGuest");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public String logoutUser(String token) throws UIException {
        logger.info("logoutUser called");

        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            logger.info("User {} logged out", userName);

            return authRepo.generateGuestToken(id);
        } else {
            logger.error("Invalid token in logoutUser");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public boolean setAdmin(String token, String adminKey) throws UIException {
        logger.info("setAdmin called");

        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            logger.info("User {} set as admin: {}", userName);

            return userRepo.setUserAsAdmin(id, adminKey);
        } else {
            logger.error("Invalid token in setAdmin");

            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd) throws UIException {
        logger.info("addToUserCart called");

        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        ItemCartDTO item = new ItemCartDTO(itemToAdd);
        userRepo.addItemToGeustCart(authRepo.getUserId(token), item);
        logger.info("Item added to user cart");

        return true;
    }
}
