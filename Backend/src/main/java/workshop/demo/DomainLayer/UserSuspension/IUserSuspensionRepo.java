package workshop.demo.DomainLayer.UserSuspension;

import java.util.List;
import workshop.demo.DomainLayer.Exceptions.UIException;

public interface IUserSuspensionRepo {

    void suspendRegisteredUser(Integer userId, int secs) throws UIException;

    void suspendGuestUser(int userId, int secs) throws UIException;

    boolean isSuspended(Integer userId);

    List<UserSuspension> getAllSuspensions();
    
    void pauseSuspension(Integer userId) throws UIException;

    void resumeSuspension(Integer userId) throws UIException;

    public void checkUserSuspensoin_ThrowExceptionIfSuspeneded(int userId) throws UIException;

}
