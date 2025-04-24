package workshop.demo.InfrastructureLayer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.ui.context.ThemeSource;

import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.StoreDTO;
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
    public void checkToAdd(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't add new ownership/managment: store does not exist");
            }
            if (!findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership/managment: store IS DEactivated");
            }
            this.data.checkToAdd(storeID, ownerID, newOwnerId);
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

        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't delete ownership: store does not exist");
            }
            if (!findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership: store IS DEactivated");
            }
            this.data.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception {
        try {
            this.data.addNewManager(storeID, ownerId, managerId);

        } catch (Exception e) {
            throw e;
        }
    }

    // @Override
    // public void givePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throw Exception {
    // }
    @Override
    public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception {
        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't add/change permission: store does not exist");
            }
            if (!findStoreByID(storeID).isActive()) {
                throw new Exception("can't add/change permission: store IS DEactivated");
            }
            this.data.changeAuthoToManager(storeID, ownerId, managerId, autorization);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void deleteManager(int storeId, int ownerId, int managerId) throws Exception {
        try {
            if (findStoreByID(storeId) == null) {
                throw new Exception("can't delete manager: store does not exist");
            }
            if (!findStoreByID(storeId).isActive()) {
                throw new Exception("can't delete manager: store IS DEactivated");
            }
            this.data.deleteManager(storeId, ownerId, managerId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void deactivateStore(int storeId, int ownerId) throws Exception {
        try {
            if (findStoreByID(storeId) == null) {
                throw new Exception("can't deactivate store: store does not exist");
            }
            if (!findStoreByID(storeId).isActive()) {
                throw new Exception("can't deactivate an DEactivated store");
            }
            if (!this.data.checkDeactivateStore(storeId, ownerId)) {
                throw new Exception("only the boss/main owner can deactivate the store");
            }
            findStoreByID(storeId).setActive(false);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void closeStore(int storeId, int ownerId) {
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

    @Override
    public List<StoreDTO> viewAllStores() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'viewAllStores'");
    }

}
