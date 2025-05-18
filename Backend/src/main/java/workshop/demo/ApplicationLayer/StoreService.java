package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.StoreDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.DevException;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
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

    private boolean sendMessageToTakeApproval(int sender, int reciver) {
        // TODO Auto-generated method stub
return true;
    }

    public int addStoreToSystem(String token, String storeName, String category) throws UIException, DevException {
        logger.info("User attempting to add a new store: '{}', category: {}", storeName, category);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int bossId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(bossId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossId);
        int storeId = storeRepo.addStoreToSystem(bossId, storeName, category);
        suConnectionRepo.addNewStoreOwner(storeId, bossId);
        stockRepo.addStore(storeId);
        logger.info("Store '{}' added successfully with ID {} by boss {}", storeName, storeId, bossId);
        return storeId;
    }

    public int AddOwnershipToStore(int storeId, String token, int newOwnerId) throws Exception, DevException {
        logger.info("User attempting to add a new owner (userId: {}) to store {}", newOwnerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userRepo.checkUserRegister_ThrowException(newOwnerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.checkToAddOwner(storeId, ownerId, newOwnerId);
        logger.info("Sending ownership approval request from {} to {}", ownerId, newOwnerId);
        boolean approved = sendMessageToTakeApproval(ownerId, newOwnerId);
        if (!approved) {
            logger.info("Ownership addition declined by user {}", newOwnerId);
            return -1;

        }
        suConnectionRepo.AddOwnershipToStore(storeId, ownerId, newOwnerId);
        logger.info("Successfully added user {} as owner to store {} by user {}", newOwnerId, storeId, ownerId);
        return newOwnerId;
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

    public int AddManagerToStore(int storeId, String token, int managerId, List<Permission> authorization) throws Exception, DevException {
        logger.info("User attempting to add manager {} to store {}", managerId, storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int ownerId = authRepo.getUserId(token);
        userRepo.checkUserRegisterOnline_ThrowException(ownerId);
        susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerId);
        userRepo.checkUserRegister_ThrowException(managerId);
        storeRepo.checkStoreExistance(storeId);
        storeRepo.checkStoreIsActive(storeId);
        suConnectionRepo.checkToAddManager(storeId, ownerId, managerId); // was missing
        boolean approved = sendMessageToTakeApproval(ownerId, managerId);
        if (!approved) {
            logger.info("Manager addition declined by user {}", managerId);
            return -1;
        }
        suConnectionRepo.AddManagerToStore(storeId, ownerId, managerId);
        logger.info("manager {} successfully added to store {} by {}", managerId, storeId, ownerId);
        suConnectionRepo.changePermissions(ownerId, managerId, storeId, authorization);
        logger.info("permissions updated for manager {} in store {}", managerId, storeId);
        return managerId;
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
        storeRepo.deactivateStore(storeId, ownerId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        logger.info("Store {} successfully deactivated by owner {}", storeId, ownerId);
        logger.info("About to notify all employees: {}", toNotify);
        ///we have to notify the employees here
        return storeId;
    }

    public int closeStore(int storeId, String token) throws Exception {
        logger.info("Admin attempting to close store {}", storeId);
        authRepo.checkAuth_ThrowTimeOutException(token, logger);
        int adminId = authRepo.getUserId(token);
        userRepo.checkAdmin_ThrowException(adminId);
        logger.info("trying to close store: {} by: {}", storeId, adminId);
        this.storeRepo.checkStoreExistance(storeId);
        List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
        this.storeRepo.closeStore(storeId);
        this.suConnectionRepo.closeStore(storeId);
        logger.info("store removed successfully!");
        //also notify the employees
        logger.info("About to notify all employees: {}", toNotify);
        return storeId;
    }

    public List<Integer> ViewRolesAndPermissions(int storeId) throws Exception {
        return suConnectionRepo.getWorkersInStore(storeId);
    }
     

    public StoreDTO getStoreDTO(String token, int storeId) throws UIException {
    logger.info("User attempting to get StoreDTO for store {}", storeId);
    authRepo.checkAuth_ThrowTimeOutException(token, logger);
    int userId = authRepo.getUserId(token);
    userRepo.checkUserRegisterOnline_ThrowException(userId);
    storeRepo.checkStoreExistance(storeId);
    return storeRepo.getStoreDTO(storeId);
    }
}
