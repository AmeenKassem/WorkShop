package workshop.demo.DomainLayer.Store;

import java.util.List;

import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.OfferDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public interface IStoreRepo {

    List<StoreDTO> viewAllStores();

    public int addStoreToSystem(int bossID, String storeName, String Category);

    Store findStoreByID(int Id);

    boolean checkStoreExistance(int ID) throws UIException;

    void deactivateStore(int storeId, int ownerId) throws Exception;

    void closeStore(int storeId) throws Exception;

    public void rankStore(int storeId, int newRank) throws UIException, DevException;

    public int getFinalRateInStore(int storeId) throws UIException, DevException;
    // public int getFinalRateInStore(int storeId) throws UIException, DevException;

    public List<Store> getStores();

    String getStoreNameById(int storeId) throws UIException;

    //another: getting info about the owners and manager->4.11
    public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws UIException, DevException;

    //another: messages to response->4.12
    //another: getting info about the history of purcheses in a specific store -> dpne in order
    void checkStoreIsActive(int storeId) throws DevException;

    public StoreDTO getStoreDTO(int storeId) throws UIException;

    void fillWithStoreName(ItemStoreDTO[] items);
}
