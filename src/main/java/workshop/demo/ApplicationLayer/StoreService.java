package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.AuctionDTO;
import workshop.demo.DTOs.BidDTO;
import workshop.demo.DTOs.ParticipationInRandomDTO;
import workshop.demo.DTOs.RandomDTO;
import workshop.demo.DTOs.SingleBid;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.item;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;

public class StoreService {

    private IStoreRepo storeRepo;
    private INotificationRepo notiRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IOrderRepo orderRepo;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public StoreService(IStoreRepo storeRepository, INotificationRepo notiRepo, IAuthRepo authRepo, IUserRepo userRepo, IOrderRepo orderRepo) {
        this.storeRepo = storeRepository;
        this.notiRepo = notiRepo;
        this.authRepo = authRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        logger.info("created the StoreService");
    }

    private boolean sendMessageToTakeApproval(int sender, int reciver) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToTakeApproval'");

    }

    public int addStoreToSystem(String token, String storeName, String Category) throws UIException {
        int bossID = 0;
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            bossID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(bossID)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", bossID), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to add new sotre to the system for the BOSS:", bossID);
            return storeRepo.addStoreToSystem(bossID, storeName, Category);
        } catch (Exception e) {
            logger.error("failed to add the store for user: {}, Error: {}", bossID, e.getMessage());
        }
        return -1;
    }
    
    public void AddOwnershipToStore(int storeID, String token, int newOwnerId) {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerID)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerID), ErrorCodes.USER_NOT_FOUND);
            }
            if (!userRepo.isRegistered(newOwnerId)) {
                throw new UIException(String.format("can't make owner to unregistered user:%d ", newOwnerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to add a new owner to the store");
            storeRepo.checkToAddManager(storeID, ownerID, newOwnerId);
            logger.info("we can add a new owner to the store");
            boolean answer = this.sendMessageToTakeApproval(ownerID, newOwnerId);
            if (answer) {
                logger.info("the new owner has approved!");
            } else {
                logger.info("failed to add a new owner: the owner did not accept the offer");
                return;
            }
            storeRepo.AddOwnershipToStore(storeID, ownerID, newOwnerId);
            logger.info("added a new owner: {} by: {}", newOwnerId, ownerID);
        } catch (Exception e) {
            logger.error("failed to add a new owner, Error: {}", e.getMessage());
        }
    }
    
    public void DeleteOwnershipFromStore(int storeID, String token, int OwnerToDelete) throws UIException {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerID)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerID), ErrorCodes.USER_NOT_FOUND);
            }
            if (!userRepo.isRegistered(OwnerToDelete)) {
                throw new UIException(String.format("can't delete owner:the user:%d is not registered to the system!", OwnerToDelete), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to delete owner: {} from store: {} by: {}", OwnerToDelete, storeID, ownerID);
            storeRepo.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);
            logger.info("the owner has been deleted successfly with his workers");
        } catch (Exception e) {
            logger.error("failed to delete the owner, Error: {}", e.getMessage());
        }
    }
    
    public void AddManagerToStore(int storeID, String token, int managerId) throws UIException {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new UIException(String.format("can't add as manager: the user:%d is not registered to the system!", managerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to add manager: {} in store: {} by: {}", managerId, storeID, ownerId);
            storeRepo.checkToAddManager(storeID, ownerId, managerId);
            logger.info("we can add a new owner to the store");
            boolean answer = this.sendMessageToTakeApproval(ownerId, managerId);
            if (answer) {
                logger.info("the new manager has approved!");
            } else {
                logger.info("failed to add a new manager: the manager did not accept the offer");
                return;
            }
            storeRepo.AddManagerToStore(storeID, ownerId, managerId);
            logger.info("the manager has been added successfly ");
        } catch (Exception e) {
            logger.error("failed to add the manager, Error: {}", e.getMessage());
        }
    }
    
    public void changePermissions(String token, int managerId, int storeID, List<Permission> autorization) throws UIException {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new UIException(String.format("can't change permssion: the user:%d is not registered to the system!", managerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("the owner: {} is trying to give authoriation to manager: {}", ownerId, managerId);
            storeRepo.changePermissions(ownerId, managerId, storeID, autorization);
            logger.info("authorizations have been added/changed succsesfully!");
        } catch (Exception e) {
            logger.error("failed to give/change permission:, ERROR:", e.getMessage());
        }
    }
    
    public void deleteManager(int storeId, String token, int managerId) throws UIException {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new UIException(String.format("can't delete manager: the user:%d is not registered to the system!", managerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to delete manager: {} from store: {} by: {}", managerId, storeId, ownerId);
            storeRepo.deleteManager(storeId, ownerId, managerId);
            logger.info("the manager has been deleted successfly with his workers");
        } catch (Exception e) {
            logger.error("failed to delete the manager, Error: {}", e.getMessage());
        }
    }
    
    public boolean addBidOnAucction(String token, int auctionId, int storeId, double price) throws UIException, DevException {
        logger.info("User trying to bid on auction: {}, store: {}", auctionId, storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token provided for bidding on auction");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (userRepo.isRegistered(userId) && userRepo.isOnline(userId)) {
            SingleBid bid = storeRepo.bidOnAuction(storeId, userId, auctionId, price);
            userRepo.addBidToAuctionCart(bid);
            logger.info("Bid placed successfully by user: {} on auction: {}", userId, auctionId);
            return true;
        } else {
            logger.error("User not logged in for auction bid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }
    
    public boolean addRegularBid(String token, int bitId, int storeId, double price) throws UIException, DevException {
        logger.info("User attempting regular bid on bidId: {}, storeId: {}", bitId, storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token on addRegularBid");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (userRepo.isRegistered(userId) && userRepo.isOnline(userId)) {
            SingleBid bid = storeRepo.bidOnBid(bitId, price, userId, storeId);
            userRepo.addBidToRegularCart(bid);
            logger.info("Regular bid successful by user: {}", userId);
            return true;
        } else {
            logger.error("User not logged in for regular bid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }
    
    public AuctionDTO[] getAllAuctions(String token, int storeId) throws UIException, DevException {
        logger.info("User requesting all auctions in store: {}", storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token on getAllAuctions");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (userRepo.isRegistered(userId) && userRepo.isOnline(userId)) {
            logger.info("Returning auction list to user: {}", userId);
            return storeRepo.getAuctionsOnStore(userId, storeId);
        } else {
            logger.error("User not logged in for getAllAuctions: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
    }
    
    public int setProductToAuction(String token, int id, int productId, int quantity, long time, double startPrice) throws UIException, DevException {
        logger.info("Setting product {} to auction in store {}", productId, id);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token for setProductToAuction");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for setProductToAuction: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.addAuctionToStore(id, userId, productId, quantity, time, startPrice);
    }
    
    public int setProductToBid(String token, int storeid, int productId, int quantity) throws UIException, DevException {
        logger.info("User attempting to set product {} as bid in store {}", productId, storeid);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in setProductToBid");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for setProductToBid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.addProductToBid(storeid, userId, productId, quantity);
    }
    
    public BidDTO[] getAllBidsStatus(String token, int storeId) throws UIException, DevException {
        logger.info("Fetching bid status for store: {}", storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in getAllBidsStatus");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for getAllBidsStatus: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.getAllBids(userId, storeId);
    }
    
    public SingleBid acceptBid(String token, int storeId, int bidId, int bidToAcceptId) throws UIException, DevException {
        logger.info("User trying to accept bid: {} for bidId: {} in store: {}", bidToAcceptId, bidId, storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in acceptBid");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for acceptBid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        SingleBid winner = storeRepo.acceptBid(storeId, bidId, userId, bidToAcceptId);
        logger.info("Bid accepted. User: {} is the winner.", winner.getUserId());
        return winner;
    }
    
    public int setProductToRandom(String token, int productId, int quantity, double productPrice, int storeId, long RandomTime) throws UIException, DevException {
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in setProductToRandom");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for setProductToRandom: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.addProductToRandom(userId, productId, quantity, productPrice, storeId, RandomTime);
    }
    
    public ParticipationInRandomDTO endBid(String token, int storeId, int randomId) throws UIException, DevException {
        logger.info("Ending random bid {} in store {}", randomId, storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in endBid");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for endBid: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.endRandom(storeId, userId, randomId);
    }
    
    public RandomDTO[] getAllRandomInStore(String token, int storeId) throws UIException, DevException {
        logger.info("Fetching all randoms in store {}", storeId);
        if (!authRepo.validToken(token)) {
            logger.error("Invalid token in getAllRandomInStore");
            throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
        }
        int userId = authRepo.getUserId(token);
        if (!(userRepo.isRegistered(userId) && userRepo.isOnline(userId))) {
            logger.error("User not logged in for getAllRandomInStore: {}", userId);
            throw new UIException("You are not logged in!", ErrorCodes.USER_NOT_LOGGED_IN);
        }
        return storeRepo.getRandomsInStore(storeId, userId);
    }
    
    public List<ItemStoreDTO> getProductsInStore(int storeId) throws UIException {
        try {
            logger.info("about to get all the products in store: {}", storeId);
            return this.storeRepo.getProductsInStore(storeId);
        } catch (Exception e) {
            logger.error("could not get the products in store: {}, ERROR:", storeId, e.getMessage());
        }
        return null;
    }
    
    public void addItem(int storeId, String token, int productId, int quantity, int price, Category category) throws UIException {
        try {
            logger.info("about to to add an item into store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int adderId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(adderId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", adderId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!storeRepo.manipulateItem(adderId, storeId, Permission.AddToStock)) {
                throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
            }
            item toAdd = this.storeRepo.addItem(storeId, productId, quantity, price, category);
            logger.info("item added sucessfully!");
        } catch (Exception e) {
            logger.error("could not add item: {} in store: {}, ERROR:", productId, storeId, e.getMessage());
        }
    }
    
    public void removeItem(int storeId, String token, int productId) throws UIException {
        try {
            logger.info("about to to remove(quantity=0) an item into store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int removerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(removerId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", removerId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!storeRepo.manipulateItem(removerId, storeId, Permission.DeleteFromStock)) {
                throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
            }
            logger.info("this worker is authorized");
            this.storeRepo.removeItem(storeId, productId);
            logger.info("item removed sucessfully!");
        } catch (Exception e) {
            logger.error("could not add item: {} in store: {}, ERROR:", productId, storeId, e.getMessage());
        }
    }
    
    public void updateQuantity(int storeId, String token, int productId) throws UIException {
        try {
            logger.info("about to to update quantity from store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int changerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(changerId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", changerId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!storeRepo.manipulateItem(changerId, storeId, Permission.UpdateQuantity)) {
                throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
            }
            logger.info("this worker is authorized");
            this.storeRepo.updateQuantity(storeId, productId, productId);
            logger.info("quantity updated sucessfully!");
        } catch (Exception e) {
            logger.error("could not update quantity ", e.getMessage());
        }
    }
    
    public void updatePrice(int storeId, String token, int productId, int newPrice) throws UIException {
        try {
            logger.info("about to to update price from store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int updaterId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(updaterId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", updaterId), ErrorCodes.USER_NOT_FOUND);
            }
            if (!storeRepo.manipulateItem(updaterId, storeId, Permission.UpdatePrice)) {
                System.out.println("Hmode3");
                throw new UIException("this worker is not authorized!", ErrorCodes.NO_PERMISSION);
            }
            logger.info("this worker is authorized");
            this.storeRepo.updatePrice(storeId, productId, newPrice);
            logger.info("price updated sucessfully!");
        } catch (Exception e) {
            logger.error("could not update price", e.getMessage());
        }
    }
    
    public void rankProduct(int storeId, String token, int productId, int newRank) throws UIException {
        try {
            logger.info("about to rank product in store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int updaterId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(updaterId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", updaterId), ErrorCodes.USER_NOT_FOUND);
            }
            this.storeRepo.rankProduct(storeId, productId, newRank);
            logger.info("product ranked sucessfully!");
        } catch (Exception e) {
            logger.error("could not rank product", e.getMessage());
        }
    }
    
    // MUST CHECK WHO CAN DO IT???
    public List<OrderDTO> veiwStoreHistory(int storeId) throws Exception {
        return this.orderRepo.getAllOrderByStore(storeId);
    }
    
    public void rankStore(String token, int storeId, int newRank) throws UIException {
        try {
            logger.info("about to rank store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            this.storeRepo.rankStore(storeId, newRank);
            logger.info("store ranked sucessfully!");
        } catch (Exception e) {
            logger.error("could not rank store", e.getMessage());
        }
    }
    
    // who can do it???
    public int getFinalRateInStore(int storeId) throws Exception {
        logger.info("about to get the final rank of the store");
        return this.storeRepo.getFinalRateInStore(storeId);
    }
    
    public void deactivateteStore(int storeId, String token) throws UIException {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", ownerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to deactivate store: {} by: {}", storeId, ownerId);
            List<Integer> toNotify = storeRepo.deactivateStore(storeId, ownerId);
            logger.info("the store has been deactivated successfully!");
            logger.info("about to notify all the employees");
        } catch (Exception e) {
            logger.error("cannot deactivate this store, Error: {}", e.getMessage());
        }
    }
    
    public void closeStore(int storeId, String token) {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int adminId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(adminId) || !userRepo.isAdmin(adminId)) {
                throw new UIException(String.format("the user:%d is not registered to the system!", adminId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to close store: {} by: {}", storeId, adminId);
            List<Integer> toNotify = storeRepo.closeStore(storeId);
            logger.info("store removed successfully!");
            logger.info("about to notify all the employees");
        } catch (Exception e) {
            logger.error("cannot close this store, Error: {}", e.getMessage());
        }
    }
    
    public List<WorkerDTO> ViewRolesAndPermissions(int storeId) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}