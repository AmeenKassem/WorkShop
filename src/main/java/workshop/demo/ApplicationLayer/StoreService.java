package workshop.demo.ApplicationLayer;

import java.util.List;

import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class StoreService {

    private IStoreRepo storeRepo;
    private INotificationRepo notRepo;

    public StoreService(IStoreRepo storeRepository, INotificationRepo notRepo) {
        this.storeRepo = storeRepository;
        this.notRepo = notRepo;
    }

    public void addStoreToSystem(int bossID, String storeName, String Category) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addStoreToSystem'");
    }

    public boolean AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'AddOwnershipToStore'");
    }

    public boolean DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'DeleteOwnershipFromStore'");
    }

    public boolean AddManagerToStore(int storeID, int ownerId, int mangerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'AddManagerToStore'");
    }

    public boolean givePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'givePermissions'");
    }

    public boolean changePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePermissions'");
    }

    public boolean deleteManager(int storeId, int ownerId, int managerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteManager'");
    }

    public boolean deactovateStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deactovateStore'");
    }

    public boolean closeStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeStore'");
    }

}
