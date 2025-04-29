package workshop.demo.ApplicationLayer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import workshop.demo.DTOs.Category;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DTOs.ItemStoreDTO;
import workshop.demo.DTOs.OrderDTO;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.DomainLayer.Order.IOrderRepo;

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
        logger.info("created the StoreService");
    }

    private boolean sendMessageToTakeApproval(int sender, int reciver) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendMessageToTakeApproval'");

    }

    public void addStoreToSystem(String token, String storeName, String Category) throws Exception {
        int bossID = 0;
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            bossID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(bossID)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", bossID));
            }
            //get an approval from the new owner then add it
            logger.info("trying to add new sotre to the system for the BOSS:", bossID);
            storeRepo.addStoreToSystem(bossID, storeName, Category);
            logger.info("added the store succsessfully");
        } catch (Exception e) {
            logger.error("failed to add the store for user: {}, Error: {}", bossID, e.getMessage());
        }
    }

    public void AddOwnershipToStore(int storeID, String token, int newOwnerId) {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerID)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerID));
            }
            if (!userRepo.isRegistered(newOwnerId)) {
                throw new Exception(String.format("can't make owner to unregistered user:%d ", newOwnerId));
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

    public void DeleteOwnershipFromStore(int storeID, String token, int OwnerToDelete) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerID = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerID)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerID));
            }
            if (!userRepo.isRegistered(OwnerToDelete)) {
                throw new Exception(String.format("can't delete owner:the user:%d is not registered to the system!", OwnerToDelete));
            }
            logger.info("trying to delete owner: {} from store: {} by: {}", OwnerToDelete, storeID, ownerID);
            storeRepo.DeleteOwnershipFromStore(storeID, ownerID, OwnerToDelete);
            logger.info("the owner has been deleted successfly with his workers");

        } catch (Exception e) {
            logger.error("failed to delete the owner, Error: {}", e.getMessage());
        }
    }

    public void AddManagerToStore(int storeID, String token, int managerId) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerId));
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new Exception(String.format("can't add as manager: the user:%d is not registered to the system!", managerId));
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
    public void changePermissions(String token, int managerId, int storeID, List<Permission> autorization) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerId));
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new Exception(String.format("can't change permssion: the user:%d is not registered to the system!", managerId));
            }
            logger.info("the owner: {} is trying to give authoriation to manager: {}", ownerId, managerId);
            storeRepo.changePermissions(ownerId, managerId, storeID, autorization);
            logger.info("authorizations have been added/changed succsesfully!");
        } catch (Exception e) {
            logger.error("failed to give/change permission:, ERROR:", e.getMessage());
        }
    }

    public void deleteManager(int storeId, String token, int managerId) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerId));
            }
            if (!userRepo.isRegistered(managerId)) {
                throw new Exception(String.format("can't delete manager: the user:%d is not registered to the system!", managerId));
            }
            logger.info("trying to delete manager: {} from store: {} by: {}", managerId, storeId, ownerId);
            storeRepo.deleteManager(storeId, ownerId, managerId);
            logger.info("the manager has been deleted successfly with his workers");

        } catch (Exception e) {
            logger.error("failed to delete the manager, Error: {}", e.getMessage());
        }
    }

    public void deactivateteStore(int storeId, String token) throws Exception {
        try {
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int ownerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(ownerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", ownerId));
            }
            logger.info("trying to deactivate store: {} by: {}", storeId, ownerId);
            List<Integer> toNotify = storeRepo.deactivateStore(storeId, ownerId);
            //here must notifu all the workes into the store by notification repo
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
                throw new Exception("unvalid token!");
            }
            int adminId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(adminId) || !userRepo.isAdmin(adminId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", adminId));
            }
            logger.info("trying to close store: {} by: {}", storeId, adminId);
            List<Integer> toNotify = storeRepo.closeStore(storeId);
            logger.info("store removed succesfully!");
            //here must notify all users using notifiaction Repo and this list
            logger.info("about to notify all the employees");

        } catch (Exception e) {
            logger.error("cannot close this store, Error: {}", e.getMessage());
        }
    }

    public List<ItemStoreDTO> getProductsInStore(int storeId) throws Exception {
        try {
            logger.info("about to get all the products in store: {}", storeId);
            return this.storeRepo.getProductsInStore(storeId);
        } catch (Exception e) {
            logger.error("could not get the products in store: {}, ERROR:", storeId, e.getMessage());

        }
        return null;
    }

    public void addItem(int storeId, String token, int productId, int quantity, int price, Category category) throws Exception {
        try {
            logger.info("about to to add an item into store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int adderId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(adderId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", adderId));
            }
            if (!storeRepo.manipulateItem(adderId, storeId, Permission.AddToStock)) {
                throw new Exception("this worker is not authorized!");
            }
            this.storeRepo.addItem(storeId, productId, quantity, price, category);
            logger.info("item added sucessfully!");

        } catch (Exception e) {
            logger.error("could not add item: {} in store: {}, ERROR:", productId, storeId, e.getMessage());

        }
    }

    public void removeItem(int storeId, String token, int productId) throws Exception {
        try {
            logger.info("about to to remove(quantity=0) an item into store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int removerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(removerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", removerId));
            }
            if (!storeRepo.manipulateItem(removerId, storeId, Permission.DeleteFromStock)) {
                throw new Exception("this worker is not authorized!");
            }
            logger.info("this worker is authorized");
            this.storeRepo.removeItem(storeId, productId);
            logger.info("item removed sucessfully!");

        } catch (Exception e) {
            logger.error("could not add item: {} in store: {}, ERROR:", productId, storeId, e.getMessage());

        }
    }

    public void decreaseQtoBuy(int storeId, String token, int productId) throws Exception {
        try {
            logger.info("about to to decrease quantity to buy an item from store: {}", storeId);
            if (!authRepo.validToken(token)) {//this token is for the buyer
                throw new Exception("unvalid token!");
            }
            this.storeRepo.decreaseQtoBuy(storeId, productId);
            logger.info("quantity decresed successfully!");
        } catch (Exception e) {
            logger.error("could not decreased ", e.getMessage());
        }
    }

    public void updateQuantity(int storeId, String token, int productId) throws Exception {
        try {
            logger.info("about to to update quantity from store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int changerId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(changerId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", changerId));
            }
            if (!storeRepo.manipulateItem(changerId, storeId, Permission.UpdateQuantity)) {
                throw new Exception("this worker is not authorized!");
            }
            logger.info("this worker is authorized");
            this.storeRepo.updateQuantity(storeId, productId, productId);
            logger.info("quantity updated sucessfully!");

        } catch (Exception e) {
            logger.error("could not update quantity ", e.getMessage());
        }

    }

    public void updatePrice(int storeId, String token, int productId, int newPrice) throws Exception {
        try {
            logger.info("about to to update price from store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int updaterId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(updaterId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", updaterId));
            }
            if (!storeRepo.manipulateItem(updaterId, storeId, Permission.UpdatePrice)) {
                throw new Exception("this worker is not authorized!");
            }
            logger.info("this worker is authorized");
            this.storeRepo.updatePrice(storeId, productId, newPrice);
            logger.info("price updated sucessfully!");

        } catch (Exception e) {
            logger.error("could not update quantity ", e.getMessage());
        }
    }

    public void rankProduct(int storeId, String token, int productId, int newRank) throws Exception {
        try {
            logger.info("about to rank product in store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            int updaterId = authRepo.getUserId(token);
            if (!userRepo.isRegistered(updaterId)) {
                throw new Exception(String.format("the user:%d is not registered to the system!", updaterId));
            }
            this.storeRepo.rankProduct(storeId, productId, newRank);
            logger.info("product ranked sucessfully!");
        } catch (Exception e) {
            logger.error("could notrank product", e.getMessage());
        }
    }

    //MUST CHECK WHO CAN DO IT???
    public List<OrderDTO> veiwStoreHistory(int storeId) throws Exception {
        return this.orderRepo.getAllOrderByStore(storeId);
    }

    public void rankStore(String token, int storeId, int newRank) throws Exception {
        try {
            logger.info("about to rank store: {}", storeId);
            if (!authRepo.validToken(token)) {
                throw new Exception("unvalid token!");
            }
            this.storeRepo.rankStore(storeId, newRank);
            logger.info("product ranked sucessfully!");
        } catch (Exception e) {
            logger.error("could notrank product", e.getMessage());
        }

    }

    //who can do it???
    public int getFinalRateInStore(int storeId) throws Exception {
        logger.info("about to get the final rank of the stroe");
        return this.storeRepo.getFinalRateInStore(storeId);

    }
}
