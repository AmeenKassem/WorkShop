package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.PostConstruct;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DataAccessLayer.OfferJpaRepository;
import workshop.demo.DataAccessLayer.StoreTreeJPARepository;
import workshop.demo.DataAccessLayer.UserJpaRepository;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Stock.IStoreStockRepo;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Store.CompositeDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountFactory;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.Store.IStoreRepoDB;
import workshop.demo.DomainLayer.Store.PurchasePolicy;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.OfferKey;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.StoreTreeEntity;
import workshop.demo.DomainLayer.StoreUserConnection.Tree;
import workshop.demo.DomainLayer.UserSuspension.IUserSuspensionRepo;

@Service
public class StoreService {

    @Autowired
    private IStoreRepo storeRepo;
    @Autowired
    private INotificationRepo notiRepo;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IOrderRepo orderRepo;
    @Autowired
    private ISUConnectionRepo suConnectionRepo;
    @Autowired
    private IStockRepo stockRepo;
    @Autowired
    private IUserSuspensionRepo susRepo;

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);
    @Autowired
    private UserService userService;
    @Autowired
    private IStoreRepoDB storeJpaRepo;
    @Autowired
    private IStoreStockRepo storeStock;
    @Autowired
    private StoreTreeJPARepository storeTreeJPARepo;
    @Autowired
    private OfferJpaRepository offerJPARepo;

    // @Autowired
    // public StoreService(UserService userService, IStoreRepo storeRepository, INotificationRepo notiRepo,
    //         IAuthRepo authRepo, UserJpaRepository userRepo, IOrderRepo orderRepo,
    //         ISUConnectionRepo sUConnectionRepo, IStockRepo stock, IUserSuspensionRepo susRepo,
    //         IStoreRepoDB storeJpaRepo, IStoreStockRepo storeStock) {
    //     this.storeRepo = storeRepository;
    //     this.notiRepo = notiRepo;
    //     this.authRepo = authRepo;
    //     this.orderRepo = orderRepo;
    //     this.userRepo = userRepo;
    //     this.suConnectionRepo = sUConnectionRepo;
    //     this.stockRepo = stock;
    //     this.susRepo = susRepo;
    //     this.userService = userService;
    //     this.storeJpaRepo = storeJpaRepo;
    //     this.storeStock = storeStock;
    //     logger.info("created the StoreService");
    // }
    @PostConstruct
    public void loadStoreTreesIntoMemory() {
        System.out.println("innnn load");
        for (StoreTreeEntity entity : storeTreeJPARepo.findAllWithNodes()) {
            try {
                Tree tree = new Tree(entity);
                logger.debug("Loading storeId=" + entity.getStoreId() + ", nodes=" + entity.getAllNodes().size());

                this.suConnectionRepo.getData().getEmployees().put(entity.getStoreId(), tree);
                //for offers:
                logger.info(">> Loading offers...");

                List<Offer> allOffers = offerJPARepo.findAll();

                for (Offer offer : allOffers) {
                    OfferKey key = offer.getId();
                    int storeId = key.getStoreId();

                    suConnectionRepo.getData().getOffers()
                            .computeIfAbsent(storeId, k -> new ArrayList<>())
                            .add(offer);
                }
                logger.info(">> Loaded " + allOffers.size() + " offers");
                //-----------will be deleted: just for testing-----------
                // Detailed breakdown
                // for (Map.Entry<Integer, List<Offer>> entry : suConnectionRepo.getData().getOffers().entrySet()) {
                //     int storeId = entry.getKey();
                //     List<Offer> offers = entry.getValue();
                //     logger.info("Store ID {} has {} offers:", storeId, offers.size());

                //     for (Offer offer : offers) {
                //         OfferKey key = offer.getId();
                //         String permissionsStr = offer.getPermissions() != null && !offer.getPermissions().isEmpty()
                //                 ? offer.getPermissions().toString()
                //                 : "[]";
                //         logger.info("  → sender={} | receiver={} | toBeOwner={} | permissions={}",
                //                 key.getSenderId(),
                //                 key.getReceiverId(),
                //                 offer.isToBeOwner(),
                //                 permissionsStr
                //         );
                //     }
                // }
            } catch (DevException e) {
                logger.debug("Failed to load store tree and offers for storeId=" + entity.getStoreId());
                e.printStackTrace();
            }
        }
    }

    public int addStoreToSystem(String token, String storeName, String category) throws UIException, DevException {
        logger.info("User attempting to add a new store: '{}', category: {}", storeName, category);
        logger.info(token);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int bossId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(bossId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossId);

        // persisting new store:
        Store newStore = new Store();
        newStore.setActive(true);
        newStore.setName(storeName);
        newStore.setCategory(category);
        newStore = storeJpaRepo.save(newStore);
        int storeId = newStore.getstoreId();
        suConnectionRepo.addNewStoreOwner(storeId, bossId);
        // stockRepo.addStore(storeId);
        StoreStock stock4Store = new StoreStock();
        stock4Store.setStoreId(storeId); 

        storeStock.save(stock4Store);
        // add store to history
        this.orderRepo.addStoreTohistory(storeId);
        logger.info("Store '{}' added successfully with ID {} by boss {}", storeName, storeId, bossId);
        return storeId;
    }

    private String convertNotificationToJson(String message, String receiverName, NotificationDTO.NotificationType type,
            boolean toBeOwner,
            String senderName, int storeId) {
        NotificationDTO dto = new NotificationDTO(message, receiverName, NotificationDTO.NotificationType.OFFER,
                toBeOwner, senderName, storeId);
        JsonObject json = Json.createObject();
        json.put("message", dto.getMessage());
        json.put("receiverName", dto.getReceiverName());
        json.put("type", dto.getType().name());
        json.put("toBeOwner", dto.getToBeOwner());
        json.put("senderName", dto.getSenderName());
        json.put("storeId", dto.getStoreId());
        return json.toJson();
    }

    private UIException storeNotFound() {
        return new UIException(" store does not exist.", ErrorCodes.STORE_NOT_FOUND);
    }

    public void MakeofferToAddOwnershipToStore(int storeId, String token, String newOwnerName)
            throws Exception, DevException {
        logger.info("User attempting to add a new owner (userId: {}) to store {}", newOwnerName, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        int newOwnerId = userRepo.findRegisteredUsersByUsername(newOwnerName).get(0).getId();
        userService.checkUserRegisterOnline_ThrowException(newOwnerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);

        // throwExceptionIfNotActive(store);
        suConnectionRepo.checkToAddOwner(storeId, ownerId, newOwnerId);
        logger.info("Making an offer to be a store owner from {} to {}", ownerId, newOwnerId);
        String owner = this.userRepo.findById(ownerId).get().getUsername();
        String storeName = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND)).getStoreName();
        String Message = String.format(
                "In store: %s, the owner: %s is offering you: %s to become an owner of this store.",
                storeName, owner, newOwnerName);

        String jssonMessage = convertNotificationToJson(Message, newOwnerName, NotificationDTO.NotificationType.OFFER,
                true, owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, newOwnerId, true, null, Message);

        this.notiRepo.sendDelayedMessageToUser(newOwnerName, jssonMessage);

    }

    private void throwExceptionIfNotActive(Store store) throws UIException {
        if (!store.isActive()) {
            throw new UIException("store is not active!", ErrorCodes.DEACTIVATED_STORE);
        }
    }

    public void reciveAnswerToOffer(int storeId, String senderName, String recievierName, boolean answer,
            boolean toBeOwner) throws Exception {
        int senderId = userRepo.findRegisteredUsersByUsername(senderName).get(0).getId();
        int recievierId = userRepo.findRegisteredUsersByUsername(recievierName).get(0).getId();
        // OfferDTO offer = suConnectionRepo.getOffer(storeId, senderId, recievierId);
        if (toBeOwner) {
            AddOwnershipToStore(storeId, senderId, recievierId, answer);
        } else {
            AddManagerToStore(storeId, senderId, recievierId, answer);
        }
    }

    public int AddOwnershipToStore(int storeId, int ownerId, int newOwnerId, boolean decide) throws Exception {
        userService.checkUserRegisterOnline_ThrowException(newOwnerId);
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
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userService.checkUserRegisterOnline_ThrowException(ownerToDelete);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        logger.info("about to send to repo");
        suConnectionRepo.DeleteOwnershipFromStore(storeId, ownerId, ownerToDelete);
        logger.info("Successfully removed owner {} from store {} by {}", ownerToDelete, storeId, ownerId);
    }

    public void MakeOfferToAddManagerToStore(int storeId, String token, String managerName,
            List<Permission> authorization) throws Exception, DevException {

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        logger.info("User {} attempting to add manager {} to store {}", ownerId, managerName, storeId);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        int managerId = userRepo.findRegisteredUsersByUsername(managerName).get(0).getId();
        userService.checkUserRegisterOnline_ThrowException(managerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        suConnectionRepo.checkToAddManager(storeId, ownerId, managerId);

        logger.info("Making an offer to be a store manager from {} to {}", ownerId, managerId);
        String owner = this.userRepo.findById(ownerId).get().getUsername();
        String nameNew = this.userRepo.findById(managerId).get().getUsername();
        String storeName = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND)).getStoreName();
        String message = String.format(
                "In store: %s, the owner: %s is offering you: %s to be a manager of this store.",
                storeName, owner, nameNew);
        String jssonMessage = convertNotificationToJson(message, nameNew, NotificationDTO.NotificationType.OFFER, false,
                owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, managerId, false, authorization, message);
        this.notiRepo.sendDelayedMessageToUser(nameNew, jssonMessage);

    }

    public int AddManagerToStore(int storeId, int ownerId, int managerId, boolean decide) throws Exception {
        userService.checkUserRegisterOnline_ThrowException(managerId);
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

    public void changePermissions(String token, int managerId, int storeId, List<Permission> authorization)
            throws Exception, DevException {
        logger.info("user attempting to update permissions for manager {} in store {}", managerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        userService.checkUserRegisterOnline_ThrowException(managerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        suConnectionRepo.changePermissions(ownerId, managerId, storeId, authorization);
        logger.info("permissions updated successfully for manager {} in store {} by owner {}", managerId, storeId,
                ownerId);
    }

    public void deleteManager(int storeId, String token, int managerId) throws Exception, DevException {
        logger.info("user attempting to delete manager {} from store {}", managerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userService.checkUserRegisterOnline_ThrowException(managerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
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
        userService.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
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
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        suConnectionRepo.checkMainOwnerToDeactivateStore_ThrowException(storeId, ownerId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        storeRepo.deactivateStore(storeId, ownerId);
        String storeName = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND)).getStoreName();
        logger.info("Store {} successfully deactivated by owner {}", storeId, ownerId);
        logger.info("About to notify all employees");
        /// we have to notify the employees here
        for (int userId : toNotify) {
            String userName = this.userRepo.findById(userId).get().getUsername();
            String message = String.format("The store: %s is deactivated ✅", storeName);
            this.notiRepo.sendDelayedMessageToUser(userName, message);
        }
        return storeId;
    }

    public int closeStore(int storeId, String token) throws Exception {
        logger.info("Admin attempting to close store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int adminId = authRepo.getUserId(token);
        userService.checkAdmin_ThrowException(adminId);
        logger.info("trying to close store: {} by: {}", storeId, adminId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        String storeName = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND)).getStoreName();
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        this.storeRepo.closeStore(storeId);
        this.suConnectionRepo.closeStore(storeId);
        logger.info("store removed successfully!");
        logger.info("About to notify all employees");
        // also notify the employees
        for (int userId : toNotify) {
            String userName = this.userRepo.findById(userId).get().getUsername();
            String message = String.format("The store: %s has been closed, you are no longer an employee there.",
                    storeName);

            this.notiRepo.sendDelayedMessageToUser(userName, message);
        }

        return storeId;
    }

    // return the workers in specific store
    public List<WorkerDTO> ViewRolesAndPermissions(String token, int storeId) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        List<Node> nodes = suConnectionRepo.getAllWorkers(storeId); // return this as nodes
        String storeName = storeJpaRepo.findById(storeId).orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND)).getStoreName();
        List<WorkerDTO> result = new ArrayList<>();
        for (Node node : nodes) {
            String username = userRepo.findById(node.getMyId()).get().getUsername();
            boolean isManager = node.getIsManager();
            Permission[] permissions = suConnectionRepo.getPermissions(node);
            boolean setByMe = node.getParentId() == userId;
            result.add(new WorkerDTO(node.getMyId(), username, isManager, !isManager, storeName, permissions, setByMe));
        }
        return result;
    }

    public StoreDTO getStoreDTO(String token, int storeId) throws UIException {
        logger.info("User attempting to get StoreDTO for store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        return store.getStoreDTO();
    }

    public List<StoreDTO> getStoresOwnedByUser(String token) throws Exception, UIException {
        List<StoreDTO> result = new ArrayList<>();
        logger.info("trying to get the stores of the user");
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        List<Integer> storesId = suConnectionRepo.getStoresIdForUser(userId);
        logger.info("got the stores Id for user:{}" + userId);
        for (int storeId : storesId) {
            Optional<Store> store = storeJpaRepo.findById(storeId);
            if (!store.isPresent()) {
                throwStoreNotFoundException();
            }
            result.add(store.get().getStoreDTO());
        }
        return result;
    }

    private void throwStoreNotFoundException() throws UIException {
        throw new UIException("store id not found", ErrorCodes.STORE_NOT_FOUND);
    }

    public List<StoreDTO> getAllStores() {
        List<Store> stores = storeJpaRepo.findAll();
        List<StoreDTO> res = new ArrayList<>();
        for (Store store : stores) {
            res.add(store.getStoreDTO());
        }
        return res;
    }
    // private String name;
    // private double percent;
    // private CreateDiscountDTO.Type type;
    // private String condition; // e.g. "CATEGORY:DAIRY", "TOTAL>100", or null
    // private CreateDiscountDTO.Logic logic ;// default to simple discount
    // private List<CreateDiscountDTO> subDiscounts;

    public void addDiscountToStore(int storeId, String token, String name, double percent, CreateDiscountDTO.Type type,
            String condition, CreateDiscountDTO.Logic logic, String[] subDiscountsNames) throws Exception {
        logger.info("User attempting to add a discount to store {}", storeId);
        //
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        boolean hasPermission = suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY);
        if (!hasPermission) {
            throw new UIException("You do not have permission to add discounts to this store",
                    ErrorCodes.NO_PERMISSION);
        }

        // Store store =storeJpaRepo.findById(storeId).get();
        // Hmode
        List<Discount> subDiscounts = new ArrayList<>();
        // Find createDiscountDTO for each subDiscount and add it to the subDiscounts
        // list
        for (String target : subDiscountsNames) {
            Discount d = store.findDiscountByName(target);
            if (d == null) {
                throw new Exception("Discount " + target + " not found in store");
            }
            boolean removed = store.removeDiscountByName(target);
            if (!removed) {
                throw new Exception("Failed to remove discount " + target + "!");
            }
            subDiscounts.add(d);
        }
        CreateDiscountDTO dto = new CreateDiscountDTO(name, percent, type, condition, logic, List.of());
        Discount discount = DiscountFactory.fromDTO(dto);
        if (!subDiscounts.isEmpty()) {
            if (!(discount instanceof CompositeDiscount comp)) {
                throw new Exception("Chosen logic does not allow sub‑discounts");
            }
            subDiscounts.forEach(comp::addDiscount);
        }
        store.addDiscount(discount);
        logger.info("Discount '{}' added successfully to store {}", discount.getName(), storeId);
    }

    public void removeDiscountFromStore(String token, int storeId, String discountName)
            throws UIException, DevException {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);

        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }

        // Store store =storeJpaRepo.findById(storeId).get();
        boolean removed = store.removeDiscountByName(discountName);
        if (!removed) {
            throw new UIException("Discount not found: " + discountName, ErrorCodes.DISCOUNT_NOT_FOUND);
        }

        logger.info("Discount '{}' removed from store {}", discountName, storeId);
    }

    public void addPurchasePolicy(String token, int storeId, String policyKey/* "NO_ALCOHOL""MIN_QTY" */,
            Integer param/* when MIN_QTY */) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }
        // Store store =storeJpaRepo.findById(storeId).get();
        switch (policyKey) {
            case "NO_ALCOHOL" ->
                store.addPurchasePolicy(PurchasePolicy.noAlcoholUnder18());
            case "MIN_QTY" ->
                store.addPurchasePolicy(PurchasePolicy.minQuantityPerProduct(param));
            default ->
                throw new UIException("Unknown Policy!", ErrorCodes.NO_POLICY);
        }
    }

    public void removePurchasePolicy(String token, int storeId, String policyKey/* "NO_ALCOHOL""MIN_QTY" */,
            Integer param/* when MIN_QTY */) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId);

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to remove discounts", ErrorCodes.NO_PERMISSION);
        }
        // Store store =storeJpaRepo.findById(storeId).get();
        PurchasePolicy policy = switch (policyKey) {
            case "NO_ALCOHOL" ->
                PurchasePolicy.noAlcoholUnder18();
            case "MIN_QTY" -> {
                if (param == null) {
                    throw new Exception("Param is required!");
                }
                yield PurchasePolicy.minQuantityPerProduct(param);
            }
            default ->
                throw new UIException("Unknown Policy!", ErrorCodes.NO_POLICY);
        };
        store.removePurchasePolicy(policy);
    }

    public String[] getAllDiscountNames(int storeId, String token) throws UIException {
        Store store = storeJpaRepo.findById(storeId).get(); // assumes auth already validated
        List<String> out = new ArrayList<>();
        collectNames(store.getDiscount(), out); // depth-first walk
        return out.toArray(String[]::new);
    }

    /* private DFS used by the public method */
    private void collectNames(Discount node, List<String> acc) {
        if (node == null) {
            return;
        }
        acc.add(node.getName());
        if (node instanceof CompositeDiscount comp) {
            comp.getDiscounts().forEach(d -> collectNames(d, acc));
        }
    }

}
