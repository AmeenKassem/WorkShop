package workshop.demo.InfrastructureLayer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ui.context.ThemeSource;

import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;

public class StoreRepository implements IStoreRepo {

    private List<Store> stores;
    private SuperDataStructure data;
    //switch it when use database!!
    private static final AtomicInteger counterSId = new AtomicInteger(1);

    public static int generateId() {
        return counterSId.getAndIncrement();
    }

    public StoreRepository() {
        this.stores = new LinkedList<>();
        data = new SuperDataStructure();
    }

    @Override
    public void addStoreToSystem(int bossID, String storeName, String Category) {
        int storeId = generateId();
        stores.add(new Store(storeId, storeName, Category));
        data.addNewStore(storeId, bossID);
    }

    @Override
    public void checkOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't add new ownership: store does not exist");
            }
            this.data.checkAddNewOwner(storeID, ownerID, newOwnerId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            this.data.addNewOwner(storeID, ownerID, newOwnerId);

        } catch (Exception e) {
            throw e;
        }

    }

    @Override
    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception {

        //Node toDelete=  get and check if the ownerID is the parent
        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't delete ownership: store does not exist");
            }
            this.data.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);

        } catch (Exception e) {
            throw e;
        }
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

    @Override
    public Store findStoreByID(int ID) {
        for (Store store : this.stores) {
            if (store.getStroeID() == ID) {
                return store;
            }
        }
        return null;
    }

    //for tests
    public SuperDataStructure getData() {
        return this.data;
    }

}
