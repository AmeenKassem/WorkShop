package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public interface IStoreRepo {
    //boss is the main owner/root of the tree

    public void addStoreToSystem(int bossID, String storeName, String Category);

    //stock managment->
    public void checkOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    public Store findStoreByID(int Id);

    public void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;

    public boolean AddManagerToStore(int storeID, int ownerId, int mangerId);

    public boolean givePermissions(int ownerId, int managerId, List<Permission> autorization);

    public boolean changePermissions(int ownerId, int managerId, List<Permission> autorization);

    public boolean deleteManager(int storeId, int ownerId, int managerId);

    public boolean deactovateStore(int storeId, int ownerId);

    public boolean closeStore(int storeId, int ownerId);

    //another: getting info about the owners and manager->4.11
    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store
}
