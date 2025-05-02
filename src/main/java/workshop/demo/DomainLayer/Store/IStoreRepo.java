package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public interface IStoreRepo {

    List<StoreDTO> viewAllStores();

// <<<<<<< HEAD
    public int addStoreToSystem(int bossID, String storeName, String Category);
// =======
//     void addStoreToSystem(int bossID, String storeName, String Category);
// >>>>>>> development

    void checkToAdd(int storeID, int ownerID, int newOwnerId) throws Exception;

    void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception;

    Store findStoreByID(int Id);

    void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;

    void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception;

// <<<<<<< HEAD
    public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;
// =======
//     void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;
// >>>>>>> development

    void deleteManager(int storeId, int ownerId, int managerId) throws Exception;

    List<Integer> deactivateStore(int storeId, int ownerId) throws Exception;

    List<Integer> closeStore(int storeId) throws Exception;

    boolean checkAvailability(List<ItemCartDTO> cartItems);

    List<ItemStoreDTO> getProductsInStore(int storeId) throws Exception;

    boolean manipulateItem(int adderId, int storeId, Permission permission) throws Exception;

    void addItem(int storeId, int productId, int quantity, int price, Category category) throws Exception;

    void removeItem(int storeId, int productId) throws Exception;

    void decreaseQtoBuy(int storeId, int productId, int quantity) throws Exception;

    void updateQuantity(int storeId, int productId, int newQuantity) throws Exception;

    void updatePrice(int storeId, int productId, int newPrice) throws Exception;

    public void rankProduct(int storeId, int productId, int newRank) throws Exception;

    //STORE RANK:
    public void rankStore(int storeId, int newRank) throws Exception;

    public int getFinalRateInStore(int storeId) throws Exception;

    public List<Store> getStores();

    String getStoreNameById(int storeId);

// <<<<<<< HEAD
    public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts) throws Exception;
// =======
//     public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts);
// >>>>>>> development

    //another: getting info about the owners and manager->4.11
    public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws Exception;

    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store
    //auction:
    //done
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price) throws Exception;

    int addAuctionToStore(int StoreId, int userId, int productId, int quantity, long tome, double startPrice) throws Exception;

    AuctionDTO[] getAuctionsOnStore(int userId, int storeId) throws Exception;

    int addProductToBid(int storeId, int userid, int productId, int quantity) throws Exception;

    SingleBid bidOnBid(int bidId, double price, int userId, int storeId) throws Exception;

    BidDTO[] getAllBids(int userId, int storeId) throws Exception;

// <<<<<<< HEAD
//     public boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws Exception;

//     public SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws Exception;

//     //random:
//     public int addProductToRandom(int userId, int productId, int quantity, double productPrice, int storeId, long RandomTime) throws Exception;

//     public ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid) throws Exception;

//     public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception;

//     public RandomDTO[] getRandomsInStore(int storeId, int userId) throws Exception;

// =======
    boolean rejectBid(int userId, int storeId, int bidId, int userBidId) throws Exception;

    SingleBid acceptBid(int storeId, int bidId, int userId, int userBidId) throws Exception;

    int addProductToRandom(int userId, int productId, int quantity, double productPrice, int storeId, long RandomTime) throws Exception;

    ParticipationInRandomDTO participateInRandom(int userId, int randomId, int storeId, double amountPaid) throws Exception;

    // ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception; 
    public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception;

    RandomDTO[] getRandomsInStore(int storeId, int userId) throws Exception;

    double getProductPrice(int storeId, int productId) throws Exception;

    public Random getRandomById(int randomId) throws Exception;

    item getItemByStoreAndProductId(int storeId, int productId) throws Exception;
}
