package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;

public class StoreService {

    private IStoreRepo storeRepo;
    private INotificationRepo notiRepo;
    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public StoreService(IStoreRepo storeRepository, INotificationRepo notiRepo) {
        this.storeRepo = storeRepository;
        this.notiRepo = notiRepo;
        logger.info("created the StoreService");
    }

    private boolean sendMessageToTakeApproval(int sender, int reciver) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToTakeApproval'");

    }

    public void addStoreToSystem(int bossID, String storeName, String Category) {
        //must check if the bossID is regestired user -> UserRepo
        //check token
        //get an approval from the new owner then add it
        try {
            logger.info("trying to add new sotre to the system for the BOSS:", bossID);
            storeRepo.addStoreToSystem(bossID, storeName, Category);
            logger.info("added the store succsessfully");
        } catch (Exception e) {
            logger.error("failed to add the store for user: {}, Error: {}", bossID, e.getMessage());
        }
    }

    public void AddOwnershipToStore(int storeID, int ownerID, int newOwnerId) {
        try {
            //must check if the bossID is regestired user -> UserRepo
            //check token
            logger.info("trying to add a new owner to the store");
            storeRepo.checkToAdd(storeID, ownerID, newOwnerId);
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

    public void DeleteOwnershipFromStore(int storeID, int ownerID, int OwnerToDelete) throws Exception {
        try {
            logger.info("trying to delete owner: {} from store: {} by: {}", OwnerToDelete, storeID, ownerID);
            storeRepo.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);
            logger.info("the owner has been deleted successfly with his workers");

        } catch (Exception e) {
            logger.error("failed to delete the owner, Error: {}", e.getMessage());
        }
    }

    public void AddManagerToStore(int storeID, int ownerId, int managerId) throws Exception {
        try {
            //must check if the bossID is regestired user -> UserRepo
            //must check if manager is a registered user
            //check token
            logger.info("trying to add manager: {} in store: {} by: {}", managerId, storeID, ownerId);
            storeRepo.checkToAdd(storeID, ownerId, managerId);
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
            //In UI should ask to select autho to give to the manager
            // List<Permission> autorization= //get from UI
            // //then call give per:
            // this.givePermissions(ownerId, managerId, storeID, autorization);

        } catch (Exception e) {
            logger.error("failed to add the manager, Error: {}", e.getMessage());
        }
    }

    // private void givePermissions(int ownerId, int managerId, int storeId, List<Permission> autorization) {
    //     try {
    //         logger.info("the owner: {} is trying to give authoriation to manager: {}", ownerId, managerId);
    //         storeRepo.changePermissions(ownerId, managerId, storeId, autorization);
    //         logger.info("authorizations have been added succsesfully!");
    //     } catch (Exception e) {
    //         logger.error("failed to give permission:, ERROR:", e.getMessage());
    //     }
    // }
    public void changePermissions(int ownerId, int managerId, int storeID, List<Permission> autorization) throws Exception {
        try {
            logger.info("the owner: {} is trying to give authoriation to manager: {}", ownerId, managerId);
            storeRepo.changePermissions(ownerId, managerId, storeID, autorization);
            logger.info("authorizations have been added/changed succsesfully!");
        } catch (Exception e) {
            logger.error("failed to give permission:, ERROR:", e.getMessage());
        }
    }

    public void deleteManager(int storeId, int ownerId, int managerId) {
        try {
            logger.info("trying to delete manager: {} from store: {} by: {}", managerId, storeId, ownerId);
            storeRepo.deleteManager(storeId, ownerId, managerId);
            logger.info("the manager has been deleted successfly with his workers");

        } catch (Exception e) {
            logger.error("failed to delete the manager, Error: {}", e.getMessage());
        }
    }

    public boolean deactivateteStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deactivateStore'");
    }

    public boolean closeStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeStore'");
    }

}
