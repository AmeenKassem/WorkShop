package workshop.demo.DomainLayer.Authentication;

public interface IAuthRepo {

    public String getUserName(String token);

    public int getUserId(String token);
    
    public boolean isRegistered(String token);

    public String generateGuestToken(int id);

    public String generateUserToken(int id,String username);

} 
