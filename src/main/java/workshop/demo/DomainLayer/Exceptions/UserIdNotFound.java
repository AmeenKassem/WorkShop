package workshop.demo.DomainLayer.Exceptions;

public class UserIdNotFound extends RuntimeException{
    
    public UserIdNotFound(String username){
        super("User "+username + "'s not found!");
    }


}
