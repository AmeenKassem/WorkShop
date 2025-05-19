package workshop.demo.InfrastructureLayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import workshop.demo.DTOs.OfferDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

@Repository
public class StoreRepository implements IStoreRepo {

    private List<Store> stores;

    // switch it when use database!!
    private static  AtomicInteger counterSId = new AtomicInteger(1);

    public static int generateId() {
        return counterSId.getAndIncrement();
    }

    @Autowired
    public StoreRepository() {
        this.stores = Collections.synchronizedList(new LinkedList<>());
    }

    @Override
    public int addStoreToSystem(int bossID, String storeName, String Category) {
        int storeId = generateId();
        stores.add(new Store(storeId, storeName, Category));
        return storeId;
    }

    @Override
    public void deactivateStore(int storeId, int ownerId) throws Exception {
        if (!findStoreByID(storeId).isActive()) {
            throw new UIException("can't deactivate an DEactivated store", ErrorCodes.DEACTIVATED_STORE);
        }
        findStoreByID(storeId).setActive(false);

    }

    @Override
    public void closeStore(int storeId) throws Exception {
        try {
            stores.removeIf(store -> store.getStoreID() == storeId);
        } catch (Exception e) {
            throw e;

        }

    }

    @Override
    public Store findStoreByID(int ID) {
        for (Store store : this.stores) {
            if (store.getStoreID() == ID) {
                return store;
            }
        }
        return null;
    }

    @Override
    public boolean checkStoreExistance(int ID) throws UIException {
        for (Store store : this.stores) {
            if (store.getStoreID() == ID) {
                return true;
            }
        }
        throw new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND);
    }

    @Override
    public List<StoreDTO> viewAllStores() {// here must check it view it with products??
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'viewAllStores'");
    }

    public String getStoreNameById(int storeId) throws UIException {
        Store store = findStoreByID(storeId);
        if (store != null) {
            return store.getStoreName();
        } else {
            throw new UIException("Store not found for ID: " + storeId, ErrorCodes.STORE_NOT_FOUND);
        }
    }

    // RANK STORE:
    @Override
    public void rankStore(int storeId, int newRank) throws UIException, DevException {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
        }
        store.rankStore(newRank);
    }

    @Override
    public int getFinalRateInStore(int storeId) throws UIException, DevException {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new UIException("Store does not exist", ErrorCodes.STORE_NOT_FOUND);
        }
        return store.getFinalRateInStore(storeId);

    }

    @Override
    public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws UIException, DevException {
        List<WorkerDTO> toReturn = new LinkedList<>();

        return toReturn;
    }

    // public Random getRandomById(int randomId) throws UIException, DevException {
    //     for (Store store : stores) {
    //         try {
    //             return store.getRandom(randomId);
    //         } catch (Exception e) {
    //             // Ignore the exception
    //         }
    //     }
    //     throw new UIException("Random with ID " + randomId + " not found in any store.", ErrorCodes.RANDOM_NOT_FOUND);
    // }
    //for tests:
    public List<Store> getStores() {
        return this.stores;
    }

    public void checkStoreIsActive(int storeId) throws DevException {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new DevException("Store not found with ID: " + storeId);
        }
        if (!store.isActive()) {
            throw new DevException(" store is not active");
        }
    }

    public StoreDTO getStoreDTO(int storeId) throws UIException {
        Store store = findStoreByID(storeId);
        if (store == null) {
            throw new UIException("Store not found for ID: " + storeId, ErrorCodes.STORE_NOT_FOUND);
        }
        return store.getStoreDTO();
    }
   public  void clear() {
    counterSId.set(1);
    this.stores = Collections.synchronizedList(new LinkedList<>());

} 

}
