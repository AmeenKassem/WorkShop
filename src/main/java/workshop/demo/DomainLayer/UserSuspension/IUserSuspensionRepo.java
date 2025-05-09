package workshop.demo.DomainLayer.UserSuspension;

import java.util.List;
import workshop.demo.DomainLayer.Exceptions.UIException;

public interface IUserSuspensionRepo {

    void suspendRegisteredUser(String username, int minutes) throws UIException;

    void suspendGuestUser(int guestId, int minutes) throws UIException;

    boolean isSuspended(Integer userId, String username);

    List<UserSuspension> getAllSuspensions();
    
    void pauseSuspension(Integer userId, String username) throws UIException;

    void resumeSuspension(Integer userId, String username) throws UIException;

}
