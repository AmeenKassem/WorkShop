package workshop.demo.InfrastructureLayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DomainLayer.Exceptions.UIException;
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

    //======================================

    @Override
    public SingleBid bidOnAuction(int StoreId, int userId, int auctionId, double price) throws Exception {
        if (findStoreByID(StoreId) == null) {
            throw new Exception("can't delete manager: store does not exist");
        }
        return findStoreByID(StoreId).bidOnAuctionProduct(auctionId, userId, price);
    }

    @Override
    public int addAuctionToStore(int StoreId,int userId, int productId, int quantity, long tome, double startPrice)
            throws Exception {
                if (findStoreByID(StoreId) == null) {
                    throw new Exception("can't delete manager: store does not exist");
                }
                if(!data.checkPermession(userId,StoreId ,Permission.SpecialType)){
                    throw new UIException("you have no permession to add auction.");
                }
                
                return findStoreByID(StoreId).addProductToAuction(userId,productId, quantity, startPrice, tome);
        
    }

    @Override
    public AuctionDTO[] getAuctionsOnStore(int storeId,int userId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        
        return findStoreByID(storeId).getAllAuctions();
    }


    //=============Bid
    @Override
    public int addProductToBid(int storeId, int userId, int productId, int quantity) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        
        return findStoreByID(storeId).addProductToBid(userId, productId, quantity);
    }

    @Override
    public SingleBid bidOnBid(int bidId, double price, int userId,int storeId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        
        return findStoreByID(storeId).bidOnBid(bidId, userId, price);
    }

    @Override
    public BidDTO[] getAllBids(int userId, int storeId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        
        return findStoreByID(storeId).getAllBids();
    }

    @Override
    public SingleBid acceptBid(int storeId, int bidId,int userId,int userBidId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        
        return findStoreByID(storeId).acceptBid(bidId,userBidId);
    }

    //===================Random

    @Override
    public int addProductToRandom(int productId,int userId, int storeId, int quantity, int cardsNumber, double priceForCard) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        return findStoreByID(storeId).addProductToRandom(productId,quantity,cardsNumber,priceForCard);
    }

    @Override
    public ParticipationInRandomDTO participateInRandom(int userId,int randomId,int storeId, double amountPaid) throws Exception{
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        return findStoreByID(storeId).participateInRandom(userId, randomId, amountPaid);
    }

    @Override
    public ParticipationInRandomDTO endRandom(int storeId, int userId, int randomId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        return findStoreByID(storeId).end( randomId);
    }


    @Override
    public RandomDTO[] getRandomsInStore(int storeId, int userId) throws Exception{
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        if(!data.checkPermession(userId,storeId ,Permission.SpecialType)){
            throw new UIException("you have no permession to see auctions info.");
        }
        return findStoreByID(storeId).getRandoms();
    }

    public double getStoreRating(int storeId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStoreRating'");
    }

    // public double getPriceForCard(int storeId, int randomId) throws Exception {
    //     if (findStoreByID(storeId) == null) {
    //         throw new Exception("can't delete manager: store does not exist.");
    //     }
    //     return findStoreByID(storeId).getCardPrice(randomId);
    // }

    public double getProductPrice(int storeId, int randomId) throws Exception {
        if (findStoreByID(storeId) == null) {
            throw new Exception("can't delete manager: store does not exist.");
        }
        return findStoreByID(storeId).getProductPrice(randomId);
    }

   
    

    
}
