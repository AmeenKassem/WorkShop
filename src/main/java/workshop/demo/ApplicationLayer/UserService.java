package workshop.demo.ApplicationLayer;

import workshop.demo.DomainLayer.User.IUserRepo;

public class UserService {

    private IUserRepo userRepo;

    public UserService(IUserRepo userRepo){
        this.userRepo=userRepo;
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

    
}
