package workshop.demo.ApplicationLayer;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class UserService {

    private IUserRepo userRepo;
    private IAuthRepo authRepo;

    public UserService(IUserRepo userRepo,IAuthRepo authRepo){
        this.userRepo=userRepo;
        this.authRepo = authRepo;
    }

    public String generateGuest(){
        return userRepo.generateGuest();
    }

    public void register(String token,String username,String password){
        userRepo.registerUser(token, username, password);
    }

    public String login(String token , String username, String pass){
        return userRepo.login(token, username, pass);
    }

    public void destroyGuest(String token){
        if(authRepo.validToken(token)){
            int id = authRepo.getUserId(token);
            userRepo.destroyGuest(id);
        }
    }


}
