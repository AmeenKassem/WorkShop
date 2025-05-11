package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.WorkerDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Exceptions.ErrorCodes;
import workshop.demo.DomainLayer.Exceptions.UIException;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Stock.IStockRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.ISUConnectionRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;

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

    public StoreService(IStoreRepo storeRepository, INotificationRepo notiRepo, IAuthRepo authRepo, IUserRepo userRepo, IOrderRepo orderRepo,
            ISUConnectionRepo sUConnectionRepo, IStockRepo stock,IUserSuspensionRepo susRepo) {
        this.storeRepo = storeRepository;
        this.notiRepo = notiRepo;
        this.authRepo = authRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
        this.suConnectionRepo = sUConnectionRepo;
        this.stockRepo = stock;
        this.susRepo=susRepo;
        logger.info("created the StoreService");
    }

    private boolean sendMessageToTakeApproval(int sender, int reciver) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToTakeApproval'");

    }

    public int addStoreToSystem(String token, String storeName, String Category) throws UIException {
        int bossID = 0;
        int storeId = -1;
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            bossID = authRepo.getUserId(token);
            userRepo.checkUserRegisterOnline_ThrowException(bossID);
            susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossID);
            logger.info("trying to add new sotre to the system for the BOSS:", bossID);
            storeId = storeRepo.addStoreToSystem(bossID, storeName, Category);
            this.suConnectionRepo.addNewStoreOwner(storeId, bossID);
            stockRepo.addStore(storeId);
            logger.info("added the store succsessfully");
        } catch (Exception e) {
            logger.error("failed to add the store for user: {}, Error: {}", bossID, e.getMessage());
        }
        return storeId;
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
           susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(ownerID);
            if (!userRepo.isRegistered(newOwnerId)) {
                throw new UIException(String.format("can't make owner to unregistered user:%d ", newOwnerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to add a new owner to the store");
            this.storeRepo.checkStoreExistance(storeID);
            if (!this.storeRepo.findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership/managment: store IS DEactivated");
            }
            this.suConnectionRepo.checkToAddOwner(storeID, ownerID, newOwnerId);
            logger.info("we can add a new owner to the store");
            boolean answer = this.sendMessageToTakeApproval(ownerID, newOwnerId);
            if (answer) {
                logger.info("the new owner has approved!");
            } else {
                logger.info("failed to add a new owner: the owner did not accept the offer");
                return;
            }
            this.suConnectionRepo.AddOwnershipToStore(storeID, ownerID, newOwnerId);
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
            susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossID);
            if (!userRepo.isRegistered(OwnerToDelete)) {
                throw new UIException(String.format("can't delete owner:the user:%d is not registered to the system!", OwnerToDelete), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to delete owner: {} from store: {} by: {}", OwnerToDelete, storeID, ownerID);
            this.storeRepo.checkStoreExistance(storeID);
            if (!this.storeRepo.findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership: store IS DEactivated");
            }
            this.suConnectionRepo.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);
            logger.info("the owner has been deleted successfly with his workers");
        } catch (Exception e) {
            logger.error("failed to delete the owner, Error: {}", e.getMessage());
        }
    }

    public void AddManagerToStore(int storeID, String token, int managerId, List<Permission> autorization) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new UIException("Invalid token!", ErrorCodes.INVALID_TOKEN);
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new UIException(String.format("The user:%d is not registered to the system!", ownerId), ErrorCodes.USER_NOT_FOUND);
            }
           susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossID);
            if (!userRepo.isRegistered(managerId)) {
                throw new UIException(String.format("can't add as manager: the user:%d is not registered to the system!", managerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to add manager: {} in store: {} by: {}", managerId, storeID, ownerId);
            this.storeRepo.checkStoreExistance(storeID);
            if (!this.storeRepo.findStoreByID(storeID).isActive()) {
                throw new Exception("can't add new ownership/managment: store IS DEactivated");
            }
            this.suConnectionRepo.checkToAddManager(storeID, ownerId, managerId);
            logger.info("we can add a new owner to the store");
            boolean answer = this.sendMessageToTakeApproval(ownerId, managerId);
            if (answer) {
                logger.info("the new manager has approved!");
            } else {
                logger.info("failed to add a new manager: the manager did not accept the offer");
                return;
            }
            this.suConnectionRepo.AddManagerToStore(storeID, ownerId, managerId);
            logger.info("the manager has been added successfly ");
            // //then call give per:
            this.suConnectionRepo.changePermissions(ownerId, managerId, storeID, autorization);

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
            this.storeRepo.checkStoreExistance(storeID);
            if (!storeRepo.findStoreByID(storeID).isActive()) {
                throw new Exception("can't add/change permission: store IS DEactivated");
            }
            suConnectionRepo.changePermissions(ownerId, managerId, storeID, autorization);
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
            susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(bossID);
            if (!userRepo.isRegistered(managerId)) {
                throw new UIException(String.format("can't delete manager: the user:%d is not registered to the system!", managerId), ErrorCodes.USER_NOT_FOUND);
            }
            logger.info("trying to delete manager: {} from store: {} by: {}", managerId, storeId, ownerId);
            this.storeRepo.checkStoreExistance(storeId);
            if (!this.storeRepo.findStoreByID(storeId).isActive()) {
                throw new Exception("can't delete manager: store IS DEactivated");
            }
            suConnectionRepo.deleteManager(storeId, ownerId, managerId);
            logger.info("the manager has been deleted successfly with his workers");
        } catch (Exception e) {
            logger.error("failed to delete the manager, Error: {}", e.getMessage());
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
            int userId = authRepo.getUserId(token);
            susRepo.checkUserSuspensoin_ThrowExceptionIfSuspeneded(userId); 
            this.storeRepo.checkStoreExistance(storeId);
            this.storeRepo.rankStore(storeId, newRank);
            logger.info("store ranked sucessfully!");
        } catch (Exception e) {
            logger.error("could not rank store", e.getMessage());
        }
    }

    // who can do it??? -> might be just in repo
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
            this.storeRepo.checkStoreExistance(storeId);
            storeRepo.deactivateStore(storeId, ownerId);
            if (!this.suConnectionRepo.checkDeactivateStore(storeId, ownerId)) {
                throw new Exception("only the boss/main owner can deactivate the store");
            }
            List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
            logger.info("the store has been deactivated succesfully!");
            //here must notify all users using notifiaction Repo and this list
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
            this.storeRepo.checkStoreExistance(storeId);
            List<Integer> toNotify = suConnectionRepo.getWorkersInStore(storeId);
            this.storeRepo.closeStore(storeId);
            this.suConnectionRepo.closeStore(storeId);
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
