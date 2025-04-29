package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
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

    //another: getting info about the owners and manager->4.11
    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store -> taking it from Layan
}
