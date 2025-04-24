package workshop.demo.InfrastructureLayer;

import java.util.LinkedList;
import java.util.List;

import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class StoreRepository implements IStoreRepo {

    private List<Store> stores;

    public StoreRepository() {
        this.stores = new LinkedList<>();
    }

    @Override
    public void addStoreToSystem(int bossID, String storeName, String Category) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addStoreToSystem'");
    }

    @Override
    public boolean AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'AddOwnershipToStore'");
    }

    @Override
    public boolean DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'DeleteOwnershipFromStore'");
    }

    @Override
    public boolean AddManagerToStore(int storeID, int ownerId, int mangerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'AddManagerToStore'");
    }

    @Override
    public boolean givePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'givePermissions'");
    }

    @Override
    public boolean changePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePermissions'");
    }

    @Override
    public boolean deleteManager(int storeId, int ownerId, int managerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteManager'");
    }

    @Override
    public boolean deactovateStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deactovateStore'");
    }

    @Override
    public boolean closeStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeStore'");
    }

}
