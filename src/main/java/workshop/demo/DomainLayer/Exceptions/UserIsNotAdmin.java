package workshop.demo.DomainLayer.Exceptions;

public class UserIsNotAdmin extends RuntimeException {
    public UserIsNotAdmin(){
        super("User is not admin!!");
    }
}
