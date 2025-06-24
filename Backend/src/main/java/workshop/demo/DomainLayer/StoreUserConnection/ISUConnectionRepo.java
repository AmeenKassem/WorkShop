package workshop.demo.DomainLayer.StoreUserConnection;

import java.util.List;

import org.springframework.stereotype.Repository;

import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;

public interface ISUConnectionRepo {

    boolean addNewStoreOwner(int storeId, int bossID);

    boolean checkToAddOwner(int storeID, int ownerID, int newOwnerId) throws Exception;

    void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception;

    boolean AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception;

    void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception;

    void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception;

    void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception;

    void deleteManager(int storeId, int ownerId, int managerId) throws Exception;

    Node getWorkerInStoreById(int storeId, int workerId) throws Exception;

    List<Integer> getWorkersInStore(int storeId) throws Exception;

    boolean checkDeactivateStore(int storeId, int ownerId) throws Exception;

    void closeStore(int storeId) throws Exception;

    boolean manipulateItem(int userId, int storeId, Permission permission) throws Exception;

    void checkMainOwnerToDeactivateStore_ThrowException(int storeId, int userId) throws DevException;

    void makeOffer(int storeId, int senderId, int reciverId, boolean toBeOwner, List<Permission> per, String Message) throws Exception;

    public List<Permission> deleteOffer(int storeId, int senderId, int reciverId) throws Exception;

    public Offer getOffer(int storeId, int senderId, int reciverId) throws Exception;

    public List<Integer> getStoresIdForUser(int userId);

    public int removeUserAccordingly(int userId) throws Exception;

    public Permission[] getPermissions(Node node);

    public List<Node> getAllWorkers(int storeId) throws Exception;

    // for tests
    SuperDataStructure getData();

    public boolean hasPermission(int userId, int storeId, Permission permission);

    public List<Node> getOwnersInStore(int storeId) throws UIException, DevException;

}
