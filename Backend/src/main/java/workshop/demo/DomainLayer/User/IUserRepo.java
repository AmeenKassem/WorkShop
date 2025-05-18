package workshop.demo.DomainLayer.User;

import java.util.List;

import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.UserDTO;
import workshop.demo.DTOs.UserSpecialItemCart;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;

public interface IUserRepo {

    // added for tests
    public boolean guestExist(int id);

    /**
     *
     *
     * @param guestToken the token will contains the id of the guest , to
     * destroy geust token ...
     * @param username
     * @param password
     * @return the new token of loged user
     *
     */
    public int login(String username, String password) throws UIException;

    /**
     *
     * @param username of user to be loged out
     *
     * @return new id for guest
     */
    public int logoutUser(String username) throws UIException;

    /**
     * destroy cart and all data of the user
     *
     * @param id
     */
    public void destroyGuest(int id);

    /**
     * in this function , do not destroy the guest token , just add the user to
     * the data ...
     *
     * @param token
     * @param username
     * @param password
     */
    public int registerUser(String username, String password, int age) throws UIException;

    /**
     * this function must return a token with new id user for the guest , must
     * be called onLoad event.
     *
     * @return id .
     */
    public int generateGuest();

    //===========
    public void addItemToGeustCart(int guestId, ItemCartDTO item) throws UIException;

    public void removeItemFromGeustCart(int guestId, int productId) throws UIException;

    public void ModifyCartAddQToBuy(int guestId, int productId, int quantity) throws UIException;

    public boolean isAdmin(int id) throws UIException;

    public boolean isRegistered(int id) throws UIException;

    public boolean isOnline(int id) throws UIException;

    //===========
    /**
     * The key is set on props of system if the key param is match the user will
     * be as an admin the user must be registerd before this function
     *
     * @param id
     * @param adminKey
     * @return
     */
    public boolean setUserAsAdmin(int id, String adminKey) throws UIException;

    public void addSpecialItemToCart(UserSpecialItemCart item, int userId) throws DevException;

    public List<UserSpecialItemCart> getAllSpecialItems(int userId);

    /**
     * Returns the shopping cart of the user (guest or registered)
     *
     * @param userId the id of the user
     * @return ShoppingCart instance
     */
    public Registered getRegisteredUser(int id);

    public ShoppingCart getUserCart(int userId) throws UIException;

    public List<ItemCartDTO> getCartForUser(int ownerId) throws UIException;

    public void checkUserRegisterOnline_ThrowException(int userId) throws UIException;

    public void checkUserRegister_ThrowException(int userId) throws UIException;

    public void checkAdmin_ThrowException(int userId) throws UIException;

    public UserDTO getUserDTO(int userId);

    List<String> getAllUsernames();

    public Registered getRegisteredUserByName(String name);
}
