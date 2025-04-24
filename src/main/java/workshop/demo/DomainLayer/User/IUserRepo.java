package workshop.demo.DomainLayer.User;

public interface IUserRepo {

    public int getUserId(String token);

    public boolean isRegisterd(String token);


    /**
     * 
     * 
     * @param guestToken the token will contains the id of the guest , to destroy geust token ...
     * @param username
     * @param password
     * @return the new token of loged user
     * 
     */
    public String login(String guestToken , String username,String password);



    /**
     * this function will destroy the guest . Frontend must call this function onClose event.
     * @param token
     */
    public void destroyGuest(String token);

    /**
     * This must destroy the token of the logged user. and return new token for user .
     * @param token
     */
    public String logoutUser(String token);


    /**
     * in this function , do not destroy the guest token , just add the user to the data ...
     * @param token
     * @param username
     * @param password
     */
    public void registerUser(String token , String username, String password);


    /**
     * this function must return a token with new id user for the guest , must be called onLoad event.
     * @return token contains id .
     */
    public String generateGuest();

    


}
