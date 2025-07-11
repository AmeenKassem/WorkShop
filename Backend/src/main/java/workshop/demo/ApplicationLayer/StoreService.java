package workshop.demo.ApplicationLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import elemental.json.Json;
import elemental.json.JsonObject;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import workshop.demo.DTOs.CreateDiscountDTO;
import workshop.demo.DTOs.NotificationDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.DiscountEntities.DiscountEntity;
import workshop.demo.DomainLayer.DiscountEntities.DiscountMapper;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Stock.ActivePurcheses;
import workshop.demo.DomainLayer.Stock.StoreStock;
import workshop.demo.DomainLayer.Store.CompositeDiscount;
import workshop.demo.DomainLayer.Store.Discount;
import workshop.demo.DomainLayer.Store.DiscountFactory;
import workshop.demo.DomainLayer.Store.MultiplyDiscount;
import workshop.demo.DomainLayer.Store.PolicyManager;
import workshop.demo.DomainLayer.Store.PurchasePolicy;
import workshop.demo.DomainLayer.Store.Store;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Node;
import workshop.demo.DomainLayer.StoreUserConnection.Offer;
import workshop.demo.DomainLayer.StoreUserConnection.OfferKey;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.StoreUserConnection.StoreTreeEntity;
import workshop.demo.DomainLayer.StoreUserConnection.Tree;
import workshop.demo.DomainLayer.User.Registered;
import workshop.demo.DomainLayer.UserSuspension.UserSuspension;
import workshop.demo.InfrastructureLayer.DiscountJpaRepository;
import workshop.demo.InfrastructureLayer.IActivePurchasesRepo;
import workshop.demo.InfrastructureLayer.IOrderRepoDB;
import workshop.demo.InfrastructureLayer.IStoreRepoDB;
import workshop.demo.InfrastructureLayer.IStoreStockRepo;
import workshop.demo.InfrastructureLayer.OfferJpaRepository;
import workshop.demo.InfrastructureLayer.PolicyManagerRepository;
import workshop.demo.InfrastructureLayer.StoreTreeJPARepository;
import workshop.demo.InfrastructureLayer.UserJpaRepository;
import workshop.demo.InfrastructureLayer.UserSuspensionJpaRepository;

@Service
public class StoreService {

    @Autowired
    private NotificationService notifier;
    @Autowired
    private IAuthRepo authRepo;
    @Autowired
    private UserJpaRepository userRepo;
    @Autowired
    private IOrderRepoDB orderRepo;
    @Autowired
    private ISUConnectionRepo suConnectionRepo;
    @Autowired
    private UserSuspensionJpaRepository suspensionJpaRepo;
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
    @Autowired
    private IActivePurchasesRepo activePurchasesRepo;
    @Autowired
    private DiscountJpaRepository discountRepo;
    @Autowired
    private LockManager lockManager;
    @Autowired
    private PolicyManagerRepository policyManagerRepository;

    @PostConstruct
    public void loadStoreTreesIntoMemory() {
        System.out.println("innnn load");
        for (StoreTreeEntity entity : storeTreeJPARepo.findAllWithNodes()) {
            try {
                // DEBUG each node before building the Tree-------------------------------------
                for (Node node : entity.getAllNodes()) {
                    String authStatus = node.getMyAuth() != null ? "AUTH_ID=" + node.getMyAuth().getId() : "AUTH=NULL";
                    logger.info("[DEBUG] Loading Node: my_id={} store_id={} isManager={} {}",
                            node.getMyId(), node.getStoreId(), node.getIsManager(), authStatus);

                    if (node.getIsManager() && node.getMyAuth() == null) {
                        logger.error(
                                "[ERROR] Manager node id={} in store {} has NULL Authorization! Data issue detected.",
                                node.getMyId(), entity.getStoreId());
                    }
                }
                // ----------------------------------------------------------------
                // Force load Authorization permissions so permissions map is initialized
                for (Node node : entity.getAllNodes()) {
                    if (node.getIsManager() && node.getMyAuth() != null) {
                        if (node.getIsManager() && node.getMyAuth() != null) {
                            logger.info("[DEBUG] Initializing permissions for manager my_id={} in store {}",
                                    node.getMyId(), entity.getStoreId());

                            Map<Permission, Boolean> permissions = node.getMyAuth().getMyAutho();
                            for (Map.Entry<Permission, Boolean> entry : permissions.entrySet()) {
                                logger.info("  → Permission {} => {}", entry.getKey(), entry.getValue());
                            }

                            // Force loading if not already done
                            permissions.size();
                        }
                    }
                }
                Tree tree = new Tree(entity);
                logger.debug("Loading storeId=" + entity.getStoreId() + ", nodes=" + entity.getAllNodes().size());

                this.suConnectionRepo.getData().getEmployees().put(entity.getStoreId(), tree);
                // for offers:
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
            } catch (DevException e) {
                logger.debug("Failed to load store tree and offers for storeId=" + entity.getStoreId());
                e.printStackTrace();
            }
        }
    }

    public int addStoreToSystem(String token, String storeName, String category) throws UIException, DevException {
        logger.info("User attempting to add a new store: '{}', category: {}", storeName, category);
        logger.info(token);

        // Authorization and suspension checks
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int bossId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(bossId);
        UserSuspension suspension = suspensionJpaRepo.findById(bossId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        // Create and save new store
        Store newStore = new Store();
        newStore.setActive(true);
        newStore.setName(storeName);
        newStore.setCategory(category);
        newStore = storeJpaRepo.save(newStore);
        int storeId = newStore.getstoreId();

        // Create and save PolicyManager linked to the new store
        PolicyManager policyManager = new PolicyManager();
        policyManager.setStore(newStore); // Link the PolicyManager to the Store
        policyManager = policyManagerRepository.save(policyManager);

        // Link the PolicyManager to the Store (if your Store entity has this field)
        newStore.setPolicyManager(policyManager);
        storeJpaRepo.save(newStore); // Update store with the manager FK

        // Add the boss as the store owner
        suConnectionRepo.addNewStoreOwner(storeId, bossId);

        // Initialize stock and active purchases
        StoreStock stock4Store = new StoreStock();
        stock4Store.setStoreId(storeId);
        storeStock.save(stock4Store);

        ActivePurcheses active = new ActivePurcheses(storeId);
        activePurchasesRepo.save(active);

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
        UserSuspension suspension = suspensionJpaRepo.findById(ownerId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        List<Registered> usersFound = userRepo.findRegisteredUsersByUsername(newOwnerName);
        if (usersFound.isEmpty()) {
            throw new UIException("User '" + newOwnerName + "' is not registered", ErrorCodes.USER_NOT_FOUND);
        }
        int newOwnerId = usersFound.get(0).getId();

        // int newOwnerId =
        // userRepo.findRegisteredUsersByUsername(newOwnerName).get(0).getId();
        userService.checkUserRegisterOnline_ThrowException(newOwnerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);

        // throwExceptionIfNotActive(store);
        suConnectionRepo.checkToAddOwner(storeId, ownerId, newOwnerId);
        logger.info("Making an offer to be a store owner from {} to {}", ownerId, newOwnerId);
        String owner = this.userRepo.findById(ownerId).get().getUsername();
        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        String Message = String.format(
                "In store: %s, the owner: %s is offering you: %s to become an owner of this store.",
                storeName, owner, newOwnerName);

        String jssonMessage = convertNotificationToJson(Message, newOwnerName, NotificationDTO.NotificationType.OFFER,
                true, owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, newOwnerId, true, null, Message);

        this.notifier.sendDelayedMessageToUser(newOwnerName, jssonMessage);

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
        synchronized (lockManager.getStoreLock(storeId)) {
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
    }

    public void DeleteOwnershipFromStore(int storeId, String token, int ownerToDelete) throws Exception, DevException {
        logger.info("user attempting to delete ownership of user {} from store {}", ownerToDelete, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        UserSuspension suspension = suspensionJpaRepo.findById(ownerId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
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
        UserSuspension suspension = suspensionJpaRepo.findById(ownerId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        // int managerId =
        // userRepo.findRegisteredUsersByUsername(managerName).get(0).getId();
        List<Registered> usersFound = userRepo.findRegisteredUsersByUsername(managerName);
        if (usersFound.isEmpty()) {
            throw new UIException("User '" + managerName + "' is not registered", ErrorCodes.USER_NOT_FOUND);
        }
        int managerId = usersFound.get(0).getId();

        userService.checkUserRegisterOnline_ThrowException(managerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        suConnectionRepo.checkToAddManager(storeId, ownerId, managerId);

        logger.info("Making an offer to be a store manager from {} to {}", ownerId, managerId);
        String owner = this.userRepo.findById(ownerId).get().getUsername();
        String nameNew = this.userRepo.findById(managerId).get().getUsername();
        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        String message = String.format(
                "In store: %s, the owner: %s is offering you: %s to be a manager of this store.",
                storeName, owner, nameNew);
        String jssonMessage = convertNotificationToJson(message, nameNew, NotificationDTO.NotificationType.OFFER, false,
                owner, storeId);
        suConnectionRepo.makeOffer(storeId, ownerId, managerId, false, authorization, message);
        this.notifier.sendDelayedMessageToUser(nameNew, jssonMessage);

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
        UserSuspension suspension = suspensionJpaRepo.findById(ownerId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        userService.checkUserRegisterOnline_ThrowException(managerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        suConnectionRepo.deleteManager(storeId, ownerId, managerId);
        logger.info("manager {} successfully deleted from store {} by owner {}", managerId, storeId, ownerId);
    }

    @Transactional
    public void rankStore(String token, int storeId, int newRank) throws Exception {
        logger.info("about to rank store: {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        // userService.checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        store.rankStore(newRank);
        // storeJpaRepo.save(store);
        // this.storeRepo.rankStore(storeId, newRank);
        logger.info("store ranked sucessfully!");

    }

    // who can do it??? -> might be just in repo
    public int getFinalRateInStore(int storeId) throws Exception {
        logger.info("about to get the final rank of the store");
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new Exception("Store not found"));
        return store.getFinalRateInStore();
    }

    @Transactional
    public int deactivateteStore(int storeId, String token) throws Exception {
        logger.info("user attempting to deactivate store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        suConnectionRepo.checkMainOwnerToDeactivateStore_ThrowException(storeId, ownerId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        storeJpaRepo.deactivateStore(storeId);
        // storeRepo.deactivateStore(storeId, ownerId);
        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        logger.info("Store {} successfully deactivated by owner {}", storeId, ownerId);
        logger.info("About to notify all employees");
        /// we have to notify the employees here
        for (int userId : toNotify) {
            String userName = this.userRepo.findById(userId).get().getUsername();
            String message = String.format("The store: %s is deactivated ✅", storeName);
            this.notifier.sendDelayedMessageToUser(userName, message);
        }
        return storeId;
    }

    @Transactional
    public int activateteStore(int storeId, String token) throws Exception {
        logger.info("user attempting to deactivate store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(ownerId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        suConnectionRepo.checkMainOwnerToDeactivateStore_ThrowException(storeId, ownerId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        storeJpaRepo.activateStore(storeId);
        // storeRepo.deactivateStore(storeId, ownerId);
        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        logger.info("Store {} successfully deactivated by owner {}", storeId, ownerId);
        logger.info("About to notify all employees");
        for (int userId : toNotify) {
            String userName = this.userRepo.findById(userId).get().getUsername();
            String message = String.format("The store: %s is activated ✅", storeName);
            this.notifier.sendDelayedMessageToUser(userName, message);
        }
        return storeId;
    }

    @Transactional
    public int closeStore(int storeId, String token) throws Exception {
        logger.info("Admin attempting to close store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int adminId = authRepo.getUserId(token);
        userService.checkAdmin_ThrowException(adminId);
        logger.info("trying to close store: {} by: {}", storeId, adminId);
        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        // this.storeRepo.closeStore(storeId);
        // store.setActive(false);

        this.suConnectionRepo.closeStore(storeId);
        policyManagerRepository.delete(store.getPolicyManager());

        storeJpaRepo.delete(store);

        logger.info("store removed successfully!");
        logger.info("About to notify all employees");
        // also notify the employees
        for (int userId : toNotify) {
            String userName = this.userRepo.findById(userId).get().getUsername();
            String message = String.format("The store: %s has been closed, you are no longer an employee there.",
                    storeName);

            this.notifier.sendDelayedMessageToUser(userName, message);
        }

        return storeId;
    }

    // return the workers in specific store
    public List<WorkerDTO> ViewRolesAndPermissions(String token, int storeId) throws Exception {
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        List<Node> nodes = suConnectionRepo.getAllWorkers(storeId); // return this as nodes
        logger.info("ine view roles -> got the nodes! the size is {}" + nodes.size());

        String storeName = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("store not found hhhhhh", ErrorCodes.STORE_NOT_FOUND))
                .getStoreName();
        List<WorkerDTO> result = new ArrayList<>();
        for (Node node : nodes) {
            logger.info("ine view roles -> got the nodes -> on nodes!");
            String username = userRepo.findById(node.getMyId()).get().getUsername();
            boolean isManager = node.getIsManager();
            Permission[] permissions = suConnectionRepo.getPermissions(node);
            logger.info("ine view roles -> got the nodes -> no nodes! -> permisstions {}" + permissions.length);
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
        // Load discount from DB
        DiscountEntity rootEntity = discountRepo.findByName("ROOT_" + storeId)
                .orElse(null);

        if (rootEntity != null) {
            Discount discount = DiscountMapper.toDomain(rootEntity);
            store.setDiscount(discount);
        }

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

    @Transactional
    public void removeDiscountFromStore(String token, int storeId, String discountName)
            throws UIException, DevException {
        logger.info("User attempting to remove discount '{}' from store {}", discountName, storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user", ErrorCodes.USER_SUSPENDED);
        }

        Store store = storeJpaRepo.findById(storeId).orElseThrow(() -> storeNotFound());
        throwExceptionIfNotActive(store);
        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY)) {
            throw new UIException("No permission", ErrorCodes.NO_PERMISSION);
        }

        // Manually remove from domain
        boolean removed = store.removeDiscountByName(discountName);

        // if (!removed) {
        // throw new UIException("Discount not found", ErrorCodes.DISCOUNT_NOT_FOUND);
        // }
        DiscountEntity oldEntity = store.getDiscountEntity();
        if (oldEntity != null) {
            discountRepo.delete(oldEntity);
        }

        store.setDiscountEntity(null);
        store.setDiscount(null);

        Discount newTree = store.getDiscount();
        if (newTree != null) {
            DiscountEntity updated = DiscountMapper.toEntity(newTree);
            store.setDiscountEntity(updated);
        }

        storeJpaRepo.save(store);
        logger.info("Discount '{}' removed and store updated", discountName);
    }

    @Transactional
    public void addPurchasePolicy(String token, int storeId, String policyKey,
            int productId, Integer param) throws Exception {

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);

        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        Store store = storeJpaRepo.findById(storeId).orElseThrow(this::storeNotFound);
        throwExceptionIfNotActive(store);

        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to manage purchase policies", ErrorCodes.NO_PERMISSION);
        }

        switch (policyKey) {
            case "NO_PRODUCT_UNDER_AGE" -> {
                if (param == null) {
                    throw new UIException("Must specify minBuyerAge", ErrorCodes.BAD_INPUT);
                }
                var p = PurchasePolicy.noProductUnderAge(productId, param);
                p.setParam(param);
                p.setProductId(productId);
                store.addPurchasePolicy(p);

            }
            case "MIN_QTY" -> {
                if (param == null) {
                    throw new UIException("Must specify minQty", ErrorCodes.BAD_INPUT);
                }
                var p = PurchasePolicy.minQuantityPerProduct(productId, param);
                p.setParam(param);
                p.setProductId(productId);
                store.addPurchasePolicy(p);

            }
            default ->
                throw new UIException("Unknown Policy!", ErrorCodes.NO_POLICY);
        }
    }

    @Transactional
    public void removePurchasePolicy(String token, int storeId, String policyKey,
            int productId, Integer param) throws Exception {

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);

        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        Store store = storeJpaRepo.findById(storeId).orElseThrow(this::storeNotFound);
        throwExceptionIfNotActive(store);

        if (!suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_PURCHASE_POLICY)) {
            throw new UIException("You do not have permission to remove purchase policies", ErrorCodes.NO_PERMISSION);
        }

        // Map policyKey string to PolicyType enum
        PurchasePolicy.PolicyType type = switch (policyKey) {
            case "NO_PRODUCT_UNDER_AGE" ->
                PurchasePolicy.PolicyType.NO_PRODUCT_UNDER_AGE;
            case "MIN_QTY" ->
                PurchasePolicy.PolicyType.MIN_QTY_PER_PRODUCT;
            default ->
                throw new UIException("Unknown Policy!", ErrorCodes.NO_POLICY);
        };

        boolean removed = store.removePurchasePolicy(type, productId, param);
        if (!removed) {
            throw new UIException("Policy not found to remove", ErrorCodes.NO_POLICY);
        }
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

    public CreateDiscountDTO getFlattenedDiscounts(int storeId, String token) throws UIException {
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));

        Discount root = store.getDiscount();
        if (root == null) {
            return null;
        }

        return root.toDTO(); // 🌳 includes sub-discounts recursively
    }

    public List<String> getVisibleDiscountDescriptions(int storeId, String token) throws UIException {
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));

        if (store.getDiscount() == null) {
            return List.of();
        }

        return store.getDiscount()
                .getFlattenedVisibleDiscounts()
                .stream()
                .filter(d -> !d.isLogicalOnly()) // exclude logical-only helper discounts
                .map(Discount::toReadableString)
                .toList();
    }

    public List<CreateDiscountDTO> getAllDiscountsFlattened(int storeId, String token) throws UIException {
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));

        Discount root = store.getDiscount();
        if (root == null) {
            return List.of();
        }

        List<CreateDiscountDTO> result = new ArrayList<>();
        collectDiscountDTOs(root, result);
        return result;
    }

    private void collectDiscountDTOs(Discount discount, List<CreateDiscountDTO> list) {
        CreateDiscountDTO dto = discount.toDTO();
        list.add(dto);
        if (dto.getSubDiscounts() != null) {
            for (CreateDiscountDTO child : dto.getSubDiscounts()) {
                collectDiscountDTOs(DiscountFactory.fromDTO(child), list);
            }
        }
    }

    public List<CreateDiscountDTO> getDiscountTree(int storeId, String token) throws UIException {
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));

        Discount root = store.getDiscount();
        if (root == null) {
            return List.of();
        }

        return List.of(root.toDTO()); // the root DTO contains nested sub-discounts
    }

    public void addDiscount(int storeId, String token, CreateDiscountDTO dto) throws Exception {
        logger.info("User attempting to add a discount tree to store {}", storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));
        throwExceptionIfNotActive(store);

        boolean hasPermission = suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY);
        if (!hasPermission) {
            throw new UIException("No permission to add discounts", ErrorCodes.NO_PERMISSION);
        }

        if (store.getDiscount() == null) {
            store.addDiscount(new MultiplyDiscount("MANUALLY_COMBINED_STORE" + storeId));

        }
        store.getDiscount();
        Discount newDiscount = DiscountFactory.fromDTO(dto);
        Discount old = store.getDiscount();
        store.addDiscount(newDiscount);
        newDiscount = store.getDiscount();
        if (old != null) {
            System.out.println("ASSI");
            removeDiscountFromStore(token, storeId, old.getName());
        }

        store.setDiscount(newDiscount);
        DiscountEntity entity = DiscountMapper.toEntity(newDiscount);
        store.setDiscountEntity(entity);
        discountRepo.save(entity);
    }

    @Transactional
    public List<PurchasePolicy> getStorePolicies(int storeId) {
        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Access the collection inside the transaction so it gets initialized
        // store.getPolicyManager().getPurchasePolicies().size();
        // Return a copy of the policies list
        return List.copyOf(store.getPurchasePolicies());
    }

    @Transactional
    public void addDiscountTest(int storeId, String token, CreateDiscountDTO dto) throws Exception {
        logger.info("User attempting to add a discount tree to store {}", storeId);

        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int userId = authRepo.getUserId(token);
        userService.checkUserRegisterOnline_ThrowException(userId);
        UserSuspension suspension = suspensionJpaRepo.findById(userId).orElse(null);
        if (suspension != null && !suspension.isExpired() && !suspension.isPaused()) {
            throw new UIException("Suspended user trying to perform an action", ErrorCodes.USER_SUSPENDED);
        }

        Store store = storeJpaRepo.findById(storeId)
                .orElseThrow(() -> new UIException("Store not found", ErrorCodes.STORE_NOT_FOUND));
        throwExceptionIfNotActive(store);

        boolean hasPermission = suConnectionRepo.hasPermission(userId, storeId, Permission.MANAGE_STORE_POLICY);
        if (!hasPermission) {
            throw new UIException("No permission to add discounts", ErrorCodes.NO_PERMISSION);
        }

        if (store.getDiscount() == null) {
            store.addDiscount(new MultiplyDiscount("MANUALLY_COMBINED_STORE" + storeId));

        }
        store.getDiscount();
        Discount newDiscount = DiscountFactory.fromDTO(dto);
        Discount old = store.getDiscount();
        store.addDiscount(newDiscount);
        newDiscount = store.getDiscount();
        if (old != null) {
            System.out.println("ASSI");
            removeDiscountFromStore(token, storeId, old.getName());
        }

        store.setDiscount(newDiscount);
        DiscountEntity entity = DiscountMapper.toEntity(newDiscount);
        store.setDiscountEntity(entity);
        discountRepo.save(entity);
    }
}
