package workshop.demo.DomainLayer.Authentication;

import org.slf4j.Logger;

import workshop.demo.DomainLayer.Exceptions.UIException;

public interface IAuthRepo {

    public String getUserName(String token) throws UIException;

    public int getUserId(String token)throws UIException;

    public boolean isRegistered(String token)throws UIException;

    public String generateGuestToken(int id)throws UIException;

    public String generateUserToken(int id, String username)throws UIException;

    public boolean validToken(String token)throws UIException;

    public void checkAuth_ThrowTimeOutException(String token,Logger logger)throws UIException;

}
