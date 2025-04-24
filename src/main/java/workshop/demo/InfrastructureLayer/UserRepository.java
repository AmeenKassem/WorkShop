package workshop.demo.InfrastructureLayer;

import java.util.HashMap;

import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.IncorrectLogin;
import workshop.demo.DomainLayer.Exceptions.TokenNotFoundException;
import workshop.demo.DomainLayer.Exceptions.UserIdNotFound;
import workshop.demo.DomainLayer.User.Guest;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.User.Registered;

public class UserRepository implements IUserRepo {

    private IAuthRepo authunticator;
    private int idGen;
    private HashMap<String,Registered> users;
    private HashMap<Integer,Guest> guests;
    private Encoder encoder ;

    public UserRepository(IAuthRepo authunticator,Encoder encoder){
        this.encoder = encoder;
        this.authunticator=authunticator;
        users = new HashMap<>();
        guests = new HashMap<>();
    }

    
    

    

    @Override
    public String logoutUser(String token) {
        return generateGuest();
    }

    @Override
    public void registerUser(String token, String username, String password) {
        if(authunticator.validToken(token)){
            if(validPassword(username,password)) {
                String encPass = encoder.encodePassword(password);
                int id = authunticator.getUserId(token);
                Registered userToAdd = new Registered(id, username, encPass);
                users.put(username,userToAdd);
            }
        } else 
            throw new TokenNotFoundException();
    }

    private boolean validPassword(String username, String password) {
        return !users.containsKey(username);
    }




    @Override
    public String generateGuest() {
        int id = idGen++;
        Guest newGuest = new Guest(id);
        guests.put(id, newGuest);
        return authunticator.generateGuestToken(id);
    }


    @Override
    public String login(String guestToken, String username, String password) {
        if(userExist(username)){
            Registered user = users.get(username);
            if(user.check(encoder,username,password)){
                return authunticator.generateUserToken(user.getId(), username);
            }else{

                throw new IncorrectLogin();
            }
        }else{
            throw new UserIdNotFound(username);
        }
        // return null;
    }




    private boolean userExist(String username) {
        return users.containsKey(username);
    }





    

}
