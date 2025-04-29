package workshop.demo.InfrastructureLayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.SuperDataStructure;

public class StoreRepository implements IStoreRepo {

    private final List<Store> stores;
    private final SuperDataStructure data;
    //switch it when use database!!
    private static final AtomicInteger counterSId = new AtomicInteger(1);

    public static int generateId() {
        return counterSId.getAndIncrement();
    }

    public StoreRepository() {
        this.stores = Collections.synchronizedList(new LinkedList<>());
        data = new SuperDataStructure();
    }

    @Override
    public void addStoreToSystem(int bossID, String storeName, String Category) {
        int storeId = generateId();
        stores.add(new Store(storeId, storeName, Category));
        data.addNewStore(storeId, bossID);
    }

    @Override
    public void checkToAdd(int storeID, int ownerID, int newOwnerId) throws Exception {//for owner
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
    public void checkToAddManager(int storeID, int ownerID, int newOwnerId) throws Exception {
        try {
            if (findStoreByID(storeID) == null) {
                throw new Exception("can't add new ownership/managment: store does not exist");
            }
            if (!findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership/managment: store IS DEactivated");
            }
            this.data.checkToAddManager(storeID, ownerID, newOwnerId);
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
    public List<Integer> deactivateStore(int storeId, int ownerId) throws Exception {
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
            return this.data.getWorkersInStore(storeId);

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Integer> closeStore(int storeId) throws Exception {
        try {
            if (findStoreByID(storeId) == null) {
                throw new Exception("can't be closed store: store does not exist");
            }
            List<Integer> toNotify = this.data.getWorkersInStore(storeId);
            stores.removeIf(store -> store.getStroeID() == storeId);
            this.data.closeStore(storeId);
            return toNotify;

        } catch (Exception e) {
            throw e;

        }

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
    public List<StoreDTO> viewAllStores() {//here must check it view it with products??
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'viewAllStores'");
    }

    //stock managment:
    @Override
    public List<ItemStoreDTO> getProductsInStore(int storeId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("store does not exist");
        }

        return findStoreByID(storeId).getProductsInStore();

    }

    @Override
    public void addItem(int storeId, int productId, int quantity, int price, Category category) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        item toAdd = new item(productId, quantity, price, category);
        store.addItem(toAdd);
    }

    @Override
    public void removeItem(int storeId, int productId) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.removeItem(productId);
    }

    @Override
    public void decreaseQtoBuy(int storeId, int productId) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.decreaseQtoBuy(productId);

    }

    @Override
    public void updateQuantity(int storeId, int productId, int newQuantity) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.changeQuantity(productId, newQuantity);
    }

    @Override
    public void updatePrice(int storeId, int productId, int newPrice) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.updatePrice(productId, newPrice);
    }

    @Override
    public void rankProduct(int storeId, int productId, int newRank) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.rankProduct(productId, newRank);
    }

    @Override
    public boolean manipulateItem(int adderId, int storeId, Permission permission) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        Node Worker = this.data.getWorkersTreeInStore(storeId).getNodeById(storeId);
        if (Worker == null) {
            throw new Exception("this user is not a worker in this store");
        }
        //owner is fully authorized:
        if (!Worker.getIsManager() || Worker.getMyAuth().hasAutho(permission)) {
            return true;
        } else {
            return false;
        }

    }
    public List<Store> getStores() {
        return stores; 
    }

    
    public String getStoreNameById(int storeId) {
        Store store = findStoreByID(storeId);
        if (store != null) {
            return store.getStoreName();
        } else {
            throw new IllegalArgumentException("Store not found for ID: " + storeId);
        }
    }

    //RANK STORE:
    @Override
    public void rankStore(int storeId, int newRank) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        store.rankStore(newRank);
    }

    @Override
    public int getFinalRateInStore(int storeId) throws Exception {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new Exception("store does not exist");
        }
        return store.getFinalRateInStore(storeId);

    }

}
