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
            storeRepo.checkOwnershipToStore(storeID, ownerID, newOwnerId);
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

    public boolean AddManagerToStore(int storeID, int ownerId, int mangerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'AddManagerToStore'");
    }

    public boolean givePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'givePermissions'");
    }

    public boolean changePermissions(int ownerId, int managerId, List<Permission> autorization) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'changePermissions'");
    }

    public boolean deleteManager(int storeId, int ownerId, int managerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteManager'");
    }

    public boolean deactovateStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deactovateStore'");
    }

    public boolean closeStore(int storeId, int ownerId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeStore'");
    }

}
