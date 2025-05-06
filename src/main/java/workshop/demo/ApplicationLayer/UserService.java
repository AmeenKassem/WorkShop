package workshop.demo.ApplicationLayer;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.User.IUserRepo;

public class UserService {

    private IUserRepo userRepo;
    private IAuthRepo authRepo;

    public UserService(IUserRepo userRepo, IAuthRepo authRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    public String generateGuest() throws Exception {
        int id = userRepo.generateGuest();
        return authRepo.generateGuestToken(id);
    }

    public void register(String token, String username, String password) {
        if (authRepo.validToken(token)) {
            userRepo.registerUser(username, password);
        } else {
            throw new TokenNotFoundException();
        }
    }

    public String login(String token, String username, String pass) {
        if (authRepo.validToken(token)) {
            int id = userRepo.login(username, pass);
            return authRepo.generateUserToken(id, username);
        } else {
            throw new TokenNotFoundException();
        }
    }

    public void destroyGuest(String token) {
        if (authRepo.validToken(token)) {
            int id = authRepo.getUserId(token);
            userRepo.destroyGuest(id);
        }
    }

    public String logoutUser(String token) {
        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            return authRepo.generateGuestToken(id);
        } else {
            throw new TokenNotFoundException();
        }
    }

    public boolean setAdmin(String token, String adminKey) {
        if (authRepo.validToken(token)) {
            String userName = authRepo.getUserName(token);
            int id = userRepo.logoutUser(userName);
            return userRepo.setUserAsAdmin(id, adminKey);
        } else {
            throw new TokenNotFoundException();
        }
    }

    //it's for guest must do something else for user
    public boolean addToUserCart(String token, ItemStoreDTO itemToAdd) throws Exception {
        if (!authRepo.validToken(token)) {
            throw new TokenNotFoundException();
        }
        ItemCartDTO item = new ItemCartDTO(itemToAdd);
        userRepo.addItemToGeustCart(authRepo.getUserId(token), item);
        return true;
    }

    //the signture must be changed
    public String updateProfile(String token) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public String RemoveUser(String token, String userToRemove) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    //must user order repo to take all the history
    //use this ->     public List<OrderDTO> getAllOrdersInSystem()
    public String ViewSystemPurchaseHistory(String token) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");

    }

    //what is that???
    public String ViewSystemInfo(String token) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
