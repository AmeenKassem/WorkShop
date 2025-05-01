package workshop.demo.DomainLayer.User;

import java.util.List;

import workshop.demo.DTOs.CardForRandomDTO;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.SingleBid;

public interface IUserRepo {


    /**
     * 
     * 
     * @param guestToken the token will contains the id of the guest , to destroy geust token ...
     * @param username
     * @param password
     * @return the new token of loged user
     * 
     */
    public int login(String username,String password);



    
    /**
     * 
     * @param username of user to be loged out
     * 
     * @return new id for guest
     */
    public int logoutUser(String username);


    /**
     * destroy cart and all data of the user
     * @param id
     */
    public void destroyGuest(int id);

    /**
     * in this function , do not destroy the guest token , just add the user to the data ...
     * @param token
     * @param username
     * @param password
     */
    public int registerUser( String username, String password);


    /**
     * this function must return a token with new id user for the guest , must be called onLoad event.
     * @return id .
     */
    public int generateGuest();

    
    //===========

    public void addItemToGeustCart(int guestId, ItemCartDTO item);
    
    public void removeItemFromGeustCart(int guestId, int productId);



    public boolean isAdmin(int id);

    public boolean isRegistered(int id);


    public boolean isOnline(int id);
    
    //===========

    /**
     * The key is set on props of system
     * if the key param is match the user will be as an admin
     * the user must be registerd before this function
     * @param id
     * @param adminKey
     * @return
     */
    public boolean setUserAsAdmin(int id ,String adminKey);




    // /**
    //  * this function must add a bid to user special cart.
    //  * @param bid
    //  */
    // public void addBidToSpecialCart(SingleBid bid);

    /*
     * this function must add a bid to user regular cart.
     * @param bid
     */
    public void addBidToRegularCart(SingleBid bid);

    /**
     * this function must add a bid to user auction cart.
     * @param bid
     */
    public void addBidToAuctionCart(SingleBid bid);


    /**
     * this will add the card to user special cart
     * @param card
     */
    public void addRandomCardToCart(CardForRandomDTO card);


    public List<SingleBid> getWinningBids(int userId);
    public List<CardForRandomDTO> getWinningCards(int userId);
}
