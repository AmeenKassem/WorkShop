package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemCartDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.ProductDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.ReceiptProduct;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ProductSearchCriteria;

public interface IStoreRepo {

    List<StoreDTO> viewAllStores();

    public int addStoreToSystem(int bossID, String storeName, String Category);
//------------------

    // void checkToAdd(int storeID, int ownerID, int newOwnerId) throws Exception;
    // void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception;
    // void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;
    // void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;
    // void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception;
    // public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;
    // void deleteManager(int storeId, int ownerId, int managerId) throws Exception;
    //-----------------------
    Store findStoreByID(int Id);

    boolean checkStoreExistance(int ID) throws UIException;

    //-----------------------------
    void deactivateStore(int storeId, int ownerId) throws Exception;

    void closeStore(int storeId) throws Exception;

    boolean checkAvailability(List<ItemCartDTO> cartItems);

    List<ItemStoreDTO> getProductsInStore(int storeId) throws Exception;

    item addItem(int storeId, int productId, int quantity, int price, Category category) throws UIException, DevException ;

    void removeItem(int storeId, int productId) throws UIException, DevException ;

    void decreaseQtoBuy(int storeId, int productId, int quantity) throws UIException, DevException ;

    void updateQuantity(int storeId, int productId, int newQuantity) throws UIException, DevException ;

    void updatePrice(int storeId, int productId, int newPrice) throws UIException, DevException ;

    public void rankProduct(int storeId, int productId, int newRank) throws UIException, DevException ;

    //STORE RANK:
    public void rankStore(int storeId, int newRank) throws UIException, DevException ;

    public int getFinalRateInStore(int storeId) throws UIException, DevException ;

    public List<Store> getStores();

    String getStoreNameById(int storeId) throws UIException;

    public ItemStoreDTO[] getMatchesItems(ProductSearchCriteria criteria, ProductDTO[] matchesProducts) throws Exception;

    //another: getting info about the owners and manager->4.11
    public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws UIException, DevException ;

    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store
    //auction:
    //done

    double getProductPrice(int storeId, int productId) throws UIException, DevException ;

    

    item getItemByStoreAndProductId(int storeId, int productId) throws UIException, DevException ;

    void validateAndDecreaseStock(int storeId, int productId, int amount) throws UIException, DevException ;

    public double calculateTotalPrice(List<ReceiptProduct> items);

    public ParticipationInRandomDTO validatedParticipation(int userId, int randomId, int storeId, double amountPaid) throws UIException, DevException ;

    public List<ReceiptProduct> processCartItemsForStore(int storeId, List<ItemCartDTO> cartItems, boolean isGuest) throws Exception;
}
