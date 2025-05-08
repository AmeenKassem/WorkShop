package workshop.demo.ApplicationLayer;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.IUserRepo;

public class UserService {
    private IUserRepo userRepo;
    private IAuthRepo authRepo;

    public UserService(IUserRepo userRepo, IAuthRepo authRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public String generateGuest() throws UIException , Exception {
        int id = userRepo.generateGuest();
        return authRepo.generateGuestToken(id);
    }

    public void register(String token, String username, String password) throws UIException {
        if (authRepo.validToken(token)) {
            userRepo.registerUser(username, password);
        } else {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public String login(String token, String username, String pass) throws UIException {
        if (authRepo.validToken(token)) {
            int id = userRepo.login(username, pass);
            return authRepo.generateUserToken(id, username);
        } else {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public void destroyGuest(String token) throws UIException {
        if (authRepo.validToken(token)) {
            int id = authRepo.getUserId(token);
            userRepo.destroyGuest(id);
        } else {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public String logoutUser(String token) throws UIException {
        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            return authRepo.generateGuestToken(id);
        } else {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public boolean setAdmin(String token, String adminKey) throws UIException {
        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            return userRepo.setUserAsAdmin(id, adminKey);
        } else {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
    }

    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd) throws UIException {
        if (!authRepo.validToken(token)) {
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        ItemCartDTO item = new ItemCartDTO(itemToAdd);
        userRepo.addItemToGeustCart(authRepo.getUserId(token), item);
        return true;
    }
}
