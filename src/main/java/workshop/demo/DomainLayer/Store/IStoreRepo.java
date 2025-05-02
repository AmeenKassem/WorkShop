package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
//import workshop.demo.DomainLayer.Stock.ProductDTO;

public interface IStoreRepo {
    //boss is the main owner/root of the tree

    public List<StoreDTO> viewAllStores();

    public int addStoreToSystem(int bossID, String storeName, String Category);

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

    //STOCK MANAGMENT:
    public List<ItemStoreDTO> getProductsInStore(int storeId) throws Exception;

    public boolean manipulateItem(int adderId, int storeId, Permission permission) throws Exception;

    public void addItem(int storeId, int productId, int quantity, int price, Category category) throws Exception;

    public void removeItem(int storeId, int productId) throws Exception;

    public void decreaseQtoBuy(int storeId, int productId) throws Exception;

    public void updateQuantity(int storeId, int productId, int newQuantity) throws Exception;

    public void updatePrice(int storeId, int productId, int newPrice) throws Exception;

    public void rankProduct(int storeId, int productId, int newRank) throws Exception;

    //STORE RANK:
    public void rankStore(int storeId, int newRank) throws Exception;

    public int getFinalRateInStore(int storeId) throws Exception;

    public List<Store> getStores();

    String getStoreNameById(int storeId);

    public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts) throws Exception;

    //another: getting info about the owners and manager->4.11
    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store
    //auction:
    //done
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price) throws Exception;

    //done
    public int addAuctionToStore(int StoreId, int userId, int productId, int quantity, long tome, double startPrice) throws Exception;

    //done
    public AuctionDTO[] getAuctionsOnStore(int userId, int storeId) throws Exception;

    //bid:
    public int addProductToBid(int storeId, int userid, int productId, int quantity) throws Exception;

    public SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws Exception;

    public BidDTO[] getAllBids(int userId, int storeId) throws Exception;

    public boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws Exception;

    public SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws Exception;

    //random:
    public int addProductToRandom(int userId, int productId, int quantity, double productPrice, int storeId, long RandomTime) throws Exception;

    public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid) throws Exception;

    public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception;

    public RandomDTO[] getRandomsInStore(int storeId, int userId) throws Exception;

}
