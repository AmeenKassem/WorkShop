package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
//import workshop.demo.DomainLayer.Stock.ProductDTO;

public interface IStoreRepo {
    //boss is the main owner/root of the tree

    public List<StoreDTO> viewAllStores();

    public void addStoreToSystem(int bossID, String storeName, String Category);

    //stock managment->
    public void checkToAdd(int storeID, int ownerID, int newOwnerId) throws Exception;

    public void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception;

    public Store findStoreByID(int Id);

    public void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;

    public void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception;

    //public void givePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;
    public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;

    public void deleteManager(int storeId, int ownerId, int managerId) throws Exception;

    public List<Integer> deactivateStore(int storeId, int ownerId) throws Exception;

    public List<Integer> closeStore(int storeId) throws Exception;

    //another: getting info about the owners and manager->4.11
    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store

    //auction:

    public SingleBid bidOnAuction(int StoreId,int userId, int auctionId , double price) throws Exception;

    public int addAuctionToStore(int StoreId,int userId, int productId,int quantity,long tome,double startPrice) throws Exception;

    public AuctionDTO[] getAuctionsOnStore(int userId, int storeId) throws Exception ;

    
    
    //bid:

    public int addProductToBid(int storeId,int userid,int productId,int quantity) throws Exception;

    public SingleBid bidOnBid(int bidId,double price,int userId,int storeId) throws Exception;

    public BidDTO[] getAllBids(int userId,int storeId) throws Exception;

    public SingleBid acceptBid(int storeId,int bidId) throws Exception;

    //random:

    public int addProductToRandom(int productId,int storeId,int quantity,int cardsNumber,double priceForCard);

}
