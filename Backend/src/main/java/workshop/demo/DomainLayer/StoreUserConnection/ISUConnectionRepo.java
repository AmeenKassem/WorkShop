package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;

import workshop.demo.DomainLayer.Exceptions.DevException;

public interface ISUConnectionRepo {

    void addNewStoreOwner(int storeId, int bossID);

    void checkToAddOwner(int storeID, int ownerID, int newOwnerId) throws Exception;

    void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception;

    void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;

    void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception;

    void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;

    void deleteManager(int storeId, int ownerId, int managerId) throws Exception;

    Node getWorkerInStoreById(int storeId, int workerId) throws Exception;

    List<Integer> getWorkersInStore(int storeId) throws Exception;

    boolean checkDeactivateStore(int storeId, int ownerId) throws Exception;

    void closeStore(int storeId) throws Exception;

    boolean manipulateItem(int userId, int storeId, Permission permission) throws Exception;

    void checkMainOwner_ThrowException(int storeId, int userId) throws DevException;

    // for tests
    SuperDataStructure getData();

}
