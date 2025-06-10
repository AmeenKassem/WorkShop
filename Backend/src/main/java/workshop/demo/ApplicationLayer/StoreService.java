package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import elemental.json.Json;
import elemental.json.JsonObject;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.*;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

@Service
public class StoreService {

    private IStoreRepo storeRepo;
    private INotificationRepo notiRepo;
    private IAuthRepo authRepo;
    private IUserRepo userRepo;
    private IOrderRepo orderRepo;
    private ISUConnectionRepo suConnectionRepo;
    private IStockRepo stockRepo;
    private IUserSuspensionRepo susRepo;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    @Autowired
    public StoreService(IStoreRepo storeRepository, INotificationRepo notiRepo, IAuthRepo authRepo, IUserRepo userRepo, IOrderRepo orderRepo,
            ISUConnectionRepo sUConnectionRepo, IStockRepo stock, IUserSuspensionRepo susRepo) {
        this.storeRepo = storeRepository;
        this.notiRepo = notiRepo;
        this.authRepo = authRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.suConnectionRepo = sUConnectionRepo;
        this.stockRepo = stock;
        this.susRepo = susRepo;
        logger.info("created the StoreService");
    }

    public int addStoreToSystem(String token, String storeName, String category) throws UIException, DevException {
        logger.info("User attempting to add a new store: '{}', category: {}", storeName, category);
        logger.info(token);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int bossId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(bossId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossId);
        int storeId = storeRepo.addStoreToSystem(bossId, storeName, category);
        suConnectionRepo.addNewStoreOwner(storeId, bossId);
        stockRepo.addStore(storeId);
        //add store to history
        this.orderRepo.addStoreTohistory(storeId);
        logger.info("Store '{}' added successfully with ID {} by boss {}", storeName, storeId, bossId);
        return storeId;
    }

    private String convertNotificationToJson(String message, String receiverName, NotificationDTO.NotificationType type, boolean toBeOwner,
            String senderName, int storeId) {
        NotificationDTO dto = new NotificationDTO(message, receiverName, NotificationDTO.NotificationType.OFFER, toBeOwner, senderName, storeId);
        JsonObject json = Json.createObject();
        json.put("message", dto.getMessage());
        json.put("receiverName", dto.getReceiverName());
        json.put("type", dto.getType().name());
        json.put("toBeOwner", dto.getToBeOwner());
        json.put("senderName", dto.getSenderName());
        json.put("storeId", dto.getStoreId());
        return json.toJson();
    }

    public void MakeofferToAddOwnershipToStore(int storeId, String token, String newOwnerName) throws Exception, DevException {
        logger.info("User attempting to add a new owner (userId: {}) to store {}", newOwnerName, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        int newOwnerId = userRepo.getRegisteredUserByName(newOwnerName).getId();
        userRepo.checkUserRegister_ThrowException(newOwnerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.checkToAddOwner(storeId, ownerId, newOwnerId);
        logger.info("Making an offer to be a store owner from {} to {}", ownerId, newOwnerId);
        String owner = this.userRepo.getRegisteredUser(ownerId).getUsername();
        String storeName = this.storeRepo.getStoreNameById(storeId);
        String Message = String.format(
                "In store: %s, the owner: %s is offering you: %s to become an owner of this store.",
                storeName, owner, newOwnerName
        );

        String jssonMessage = convertNotificationToJson(Message, newOwnerName, NotificationDTO.NotificationType.OFFER, true, owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, newOwnerId, true, null, Message);

        this.notiRepo.sendDelayedMessageToUser(newOwnerName, jssonMessage);

    }

    public void reciveAnswerToOffer(int storeId, String senderName, String recievierName, boolean answer, boolean toBeOwner) throws Exception {
        int senderId = userRepo.getRegisteredUserByName(senderName).getId();
        int recievierId = userRepo.getRegisteredUserByName(recievierName).getId();
        //OfferDTO offer = suConnectionRepo.getOffer(storeId, senderId, recievierId);
        if (toBeOwner) {
            AddOwnershipToStore(storeId, senderId, recievierId, answer);
        } else {
            AddManagerToStore(storeId, senderId, recievierId, answer);
        }
    }

    public int AddOwnershipToStore(int storeId, int ownerId, int newOwnerId, boolean decide) throws Exception {
        userRepo.checkUserRegister_ThrowException(newOwnerId);
        if (decide) {
            suConnectionRepo.getOffer(storeId, ownerId, newOwnerId);
            suConnectionRepo.AddOwnershipToStore(storeId, ownerId, newOwnerId);
            suConnectionRepo.deleteOffer(storeId, ownerId, newOwnerId);
            logger.info("Successfully added user {} as owner to store {} by user {}", newOwnerId, storeId, ownerId);
            return newOwnerId;
        }
        logger.info("Ownership addition declined by user {}", newOwnerId);
        suConnectionRepo.deleteOffer(storeId, ownerId, newOwnerId);
        return -1;
    }

    public void DeleteOwnershipFromStore(int storeId, String token, int ownerToDelete) throws Exception, DevException {
        logger.info("user attempting to delete ownership of user {} from store {}", ownerToDelete, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userRepo.checkUserRegister_ThrowException(ownerToDelete);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.DeleteOwnershipFromStore(storeId, ownerId, ownerToDelete);
        logger.info("Successfully removed owner {} from store {} by {}", ownerToDelete, storeId, ownerId);
    }

    public void MakeOfferToAddManagerToStore(int storeId, String token, String managerName, List<Permission> authorization) throws Exception, DevException {

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        logger.info("User {} attempting to add manager {} to store {}", ownerId, managerName, storeId);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        int managerId = userRepo.getRegisteredUserByName(managerName).getId();
        userRepo.checkUserRegister_ThrowException(managerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.checkToAddManager(storeId, ownerId, managerId);

        logger.info("Making an offer to be a store manager from {} to {}", ownerId, managerId);
        String owner = this.userRepo.getRegisteredUser(ownerId).getUsername();
        String nameNew = this.userRepo.getRegisteredUser(managerId).getUsername();
        String storeName = this.storeRepo.getStoreNameById(storeId);
        String message = String.format(
                "In store: %s, the owner: %s is offering you: %s to be a manager of this store.",
                storeName, owner, nameNew
        );
        String jssonMessage = convertNotificationToJson(message, nameNew, NotificationDTO.NotificationType.OFFER, false, owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, managerId, false, authorization, message);
        this.notiRepo.sendDelayedMessageToUser(nameNew, jssonMessage);

    }

    public int AddManagerToStore(int storeId, int ownerId, int managerId, boolean decide) throws Exception {
        userRepo.checkUserRegister_ThrowException(managerId);
        if (decide) {
            suConnectionRepo.getOffer(storeId, ownerId, managerId);
            suConnectionRepo.AddManagerToStore(storeId, ownerId, managerId);
            List<Permission> authorization = suConnectionRepo.deleteOffer(storeId, ownerId, managerId);
            suConnectionRepo.changePermissions(ownerId, managerId, storeId, authorization);
            logger.info("Successfully added user {} as a manager to store {} by user {}", managerId, storeId, ownerId);
            return managerId;
        }
        logger.info("Managment updated addition declined by user {}", ownerId);
        suConnectionRepo.deleteOffer(storeId, ownerId, managerId);
        return -1;

    }

    public void changePermissions(String token, int managerId, int storeId, List<Permission> authorization) throws Exception, DevException {
        logger.info("user attempting to update permissions for manager {} in store {}", managerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        userRepo.checkUserRegister_ThrowException(managerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.changePermissions(ownerId, managerId, storeId, authorization);
        logger.info("permissions updated successfully for manager {} in store {} by owner {}", managerId, storeId, ownerId);
    }

    public void deleteManager(int storeId, String token, int managerId) throws Exception, DevException {
        logger.info("user attempting to delete manager {} from store {}", managerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userRepo.checkUserRegister_ThrowException(managerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.deleteManager(storeId, ownerId, managerId);
        logger.info("manager {} successfully deleted from store {} by owner {}", managerId, storeId, ownerId);
    }

    // MUST CHECK WHO CAN DO IT???
    public List<OrderDTO> veiwStoreHistory(int storeId) throws Exception {
        return this.orderRepo.getAllOrderByStore(storeId);
    }

    public void rankStore(String token, int storeId, int newRank) throws Exception {
        logger.info("about to rank store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        this.storeRepo.checkStoreExistance(storeId);
        this.storeRepo.rankStore(storeId, newRank);
        logger.info("store ranked sucessfully!");

    }

    // who can do it??? -> might be just in repo
    public int getFinalRateInStore(int storeId) throws Exception {
        logger.info("about to get the final rank of the store");
        return this.storeRepo.getFinalRateInStore(storeId);
    }

    public int deactivateteStore(int storeId, String token) throws Exception {
        logger.info("user attempting to deactivate store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        storeRepo.checkStoreExistance(storeId);
        suConnectionRepo.checkMainOwnerToDeactivateStore_ThrowException(storeId, ownerId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        storeRepo.deactivateStore(storeId, ownerId);
        String storeName = storeRepo.getStoreNameById(storeId);
        logger.info("Store {} successfully deactivated by owner {}", storeId, ownerId);
        logger.info("About to notify all employees");
        ///we have to notify the employees here
         for (int userId : toNotify) {
            String userName = this.userRepo.getRegisteredUser(userId).getUsername();
            String message = String.format("The store: %s is deactivated ✅", storeName);
            this.notiRepo.sendDelayedMessageToUser(userName, message);
        }
        return storeId;
    }

    public int closeStore(int storeId, String token) throws Exception {
        logger.info("Admin attempting to close store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int adminId = authRepo.getUserId(token);
        userRepo.checkAdmin_ThrowException(adminId);
        logger.info("trying to close store: {} by: {}", storeId, adminId);
        this.storeRepo.checkStoreExistance(storeId);
        String storeName = storeRepo.getStoreNameById(storeId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        this.storeRepo.closeStore(storeId);
        this.suConnectionRepo.closeStore(storeId);
        logger.info("store removed successfully!");
        logger.info("About to notify all employees");
        //also notify the employees
        for (int userId : toNotify) {
            String userName = this.userRepo.getRegisteredUser(userId).getUsername();
            String message = String.format("The store: %s has been closed, you are no longer an employee there.", storeName);

            this.notiRepo.sendDelayedMessageToUser(userName, message);
        }

        return storeId;
    }

    //return the workers in specific store
    public List<WorkerDTO> ViewRolesAndPermissions(String token, int storeId) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        List<Node> nodes = suConnectionRepo.getAllWorkers(storeId);  //return this as nodes 
        String storeName = storeRepo.getStoreNameById(storeId);
        List<WorkerDTO> result = new ArrayList<>();
        for (Node node : nodes) {
            String username = userRepo.getRegisteredUser(node.getMyId()).getUsername();
            boolean isManager = node.getIsManager();
            Permission[] permissions = suConnectionRepo.getPermissions(node);
            boolean setByMe = node.getParentId() == userId;
            result.add(new WorkerDTO(userId, username, isManager, !isManager, storeName, permissions, setByMe));
        }
        return result;
    }

    public StoreDTO getStoreDTO(String token, int storeId) throws UIException {
        logger.info("User attempting to get StoreDTO for store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        storeRepo.checkStoreExistance(storeId);
        return storeRepo.getStoreDTO(storeId);
    }

    public List<StoreDTO> getStoresOwnedByUser(String token) throws Exception, UIException {
        List<StoreDTO> result = new ArrayList<>();
        logger.info("trying to get the stores of the user");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        List<Integer> storesId = suConnectionRepo.getStoresIdForUser(userId);
        logger.info("got the stores Id for user:{}" + userId);
        for (int storeId : storesId) {
            storeRepo.checkStoreExistance(storeId);
            StoreDTO dto = storeRepo.getStoreDTO(storeId);
            result.add(dto);
        }
        return result;
    }

    public List<StoreDTO> getAllStores() {
        List<Store> stores = storeRepo.getStores();
        List<StoreDTO> res = new ArrayList<>();
        for (Store store : stores) {
            res.add(store.getStoreDTO());
        }
        return res;
    }
//    private String name;
//    private double percent;
//    private CreateDiscountDTO.Type type;
//    private String condition; // e.g. "CATEGORY:DAIRY", "TOTAL>100", or null
//    private CreateDiscountDTO.Logic logic ;// default to simple discount
//    private List<CreateDiscountDTO> subDiscounts;
    public void addDiscountToStore(int storeId, String token, String name, double percent, CreateDiscountDTO.Type type
    , String condition, CreateDiscountDTO.Logic logic,String[] subDiscountsNames) throws Exception {
        logger.info("User attempting to add a discount to store {}", storeId);
        //
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        boolean hasPermission = suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY);
        if (!hasPermission) {
            throw new UIException("You do not have permission to add discounts to this store", ErrorCodes.NO_PERMISSION);
        }

        Store store = storeRepo.findStoreByID(storeId);
        //Hmode
        List<Discount> subDiscounts = new ArrayList<>();
        //Find createDiscountDTO for each subDiscount and add it to the subDiscounts list
        for(String target : subDiscountsNames){
            Discount d = store.findDiscountByName(target);
            if(d==null)
                throw new Exception("Discount "+target+" not found in store");
            boolean removed = store.removeDiscountByName(target);
            if(!removed)
                throw new Exception("Failed to remove discount "+target+"!");
            subDiscounts.add(d);
        }
        CreateDiscountDTO dto = new CreateDiscountDTO(name,percent,type,condition,logic,List.of());
        Discount discount = DiscountFactory.fromDTO(dto);
        if (!subDiscounts.isEmpty()) {
            if (!(discount instanceof CompositeDiscount comp))
                throw new Exception("Chosen logic does not allow sub‑discounts");
            subDiscounts.forEach(comp::addDiscount);
        }
        store.addDiscount(discount);
        logger.info("Discount '{}' added successfully to store {}", discount.getName(), storeId);
    }

    public void removeDiscountFromStore(String token, int storeId, String discountName) throws UIException, DevException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);

        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }

        Store store = storeRepo.findStoreByID(storeId);
        boolean removed = store.removeDiscountByName(discountName);
        if (!removed) {
            throw new UIException("Discount not found: " + discountName, ErrorCodes.DISCOUNT_NOT_FOUND);
        }

        logger.info("Discount '{}' removed from store {}", discountName, storeId);
    }
    public void addPurchasePolicy(String token,int storeId,String policyKey/*"NO_ALCOHOL""MIN_QTY"*/,
                                  Integer param/*when MIN_QTY*/) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token,logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }
        Store store = storeRepo.findStoreByID(storeId);
        switch (policyKey){
            case "NO_ALCOHOL" ->
                store.addPurchasePolicy(PurchasePolicy.noAlcoholUnder18());
            case "MIN_QTY" ->
                store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(param));
            default -> throw new UIException("Unknown Policy!",ErrorCodes.NO_POLICY);
        }
    }
    public void removePurchasePolicy(String token,int storeId,String policyKey/*"NO_ALCOHOL""MIN_QTY"*/,
                                  Integer param/*when MIN_QTY*/) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token,logger);
        int userId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }
        Store store = storeRepo.findStoreByID(storeId);
        PurchasePolicy policy = switch (policyKey){
            case "NO_ALCOHOL" -> PurchasePolicy.noAlcoholUnder18();
            case "MIN_QTY" -> {
                if(param==null)
                    throw new Exception("Param is required!");
                yield PurchasePolicy.minQuantityPerProduct(param);
            }
            default -> throw new UIException("Unknown Policy!",ErrorCodes.NO_POLICY);
        };
        store.removePurchasePolicy(policy);
    }
}
