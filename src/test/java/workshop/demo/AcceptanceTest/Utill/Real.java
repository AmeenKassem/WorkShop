package workshop.demo.AcceptanceTest.Utill;

import java.util.List;

import org.mockito.Mockito;

import workshop.demo.ApplicationLayer.NotificationService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.Notification.INotificationRepo;
import workshop.demo.DomainLayer.Store.IStoreRepo;
import workshop.demo.DomainLayer.StoreUserConnection.Permission;
import workshop.demo.DomainLayer.User.IUserRepo;
import workshop.demo.InfrastructureLayer.AuthenticationRepo;
import workshop.demo.InfrastructureLayer.UserRepository;

public class Real implements Bridge {
    AuthenticationRepo mockAuthRepo = Mockito.mock(AuthenticationRepo.class);
    UserRepository mockUserRepo = Mockito.mock(UserRepository.class);
    UserService userService = new UserService(mockUserRepo, mockAuthRepo);
    IStoreRepo mockStoreRepo = Mockito.mock(IStoreRepo.class);
    INotificationRepo mocknotiRepo = Mockito.mock(INotificationRepo.class);
    IAuthRepo authRepomock = Mockito.mock(IAuthRepo.class);
    IUserRepo userRepomock = Mockito.mock(IUserRepo.class);
    StoreService storeService = new StoreService(mockStoreRepo, mocknotiRepo, authRepomock, userRepomock);
    INotificationRepo mockNotificationRepo = Mockito.mock(INotificationRepo.class);
    NotificationService notificationService = new NotificationService(mockNotificationRepo, mockUserRepo);

    /////////////////////// System /////////////////////////////
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {

        return "TODO";
    }

    @Override
    public String testSystem_sendDMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        notificationService.sendDMessageToAll(receiversIds, message, senderId);
        return "Done";
    }

    @Override
    public String testSystem_sendRTMessageToAll(List<Integer> receiversIds, String message, int senderId)
            throws Exception {
        notificationService.sendRTMessageToAll(receiversIds, message, senderId);
        return "Done";
    }

    /////////////////////// Guest /////////////////////////////
    @Override
    public String testGuest_Enter() throws Exception {
        userService.generateGuest();
        return "Done";
    }

    @Override
    public String testGuest_Exit(String token) throws Exception {
        // return userService.destroyGuest(token);
        userService.destroyGuest(token);
        return "Done";
    }

    @Override
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        // return userService.register(token, username, password);
        userService.register(token, username, password);
        return "Done";
    }

    @Override
    public String testGuest_GetStoreInfo(String token, int storeID) throws Exception {
        return "TO-DO";
    }

    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return "TO=DO";
    }

    @Override
    public String testGuest_SearchProduct(String token, String productname) throws Exception {
        // return stockService.searchProduct(productname);
        return "TO=DO";

    }

    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        // return stockService.searchProductInStore(storeID, productID);
        return "TO=DO";

    }

    @Override
    public String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        // return stockService.addProductToCart(token, storeID, productID, count);
        return "TO=DO";

    }

    @Override
    public String testGuest_ModifyCart(String token, int cartID) throws Exception {
        // return stockService.modifyCart(token, cartID);
        return "TO=DO";

    }

    @Override
    public String testGuest_BuyCart(String token, int cartID) throws Exception {
        // return purchaseService.buyCart(token, cartID);
        return "TO=DO";

    }

    //////////////////////////// User ////////////////////////////
    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return userService.login(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return userService.logoutUser(token);
    }

    @Override
    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        storeService.addStoreToSystem(token, storeName, category);
        return "Done";
    }

    @Override
    public String testUser_AddReviewToStore(String token, int storeId, String review) throws Exception {
        // return storeService.addReview(token, storeId, review);
        return "TO=DO";

    }

    @Override
    public String testUser_AddReviewToProduct(String token, int storeId, int productId, String review)
            throws Exception {
        // return productService.addReview(token, storeId, review);
        return "TO=DO";

    }

    @Override
    public String testUser_RateProduct(String token, int storeID, int productID, int rate) throws Exception {
        // return productService.rateProduct(token, storeID, productID, rate);
        return "TO=DO";

    }

    @Override
    public String testUser_RateStore(String token, int storeID, int productID, int rate) throws Exception {
        // return storeService.rateStore(token, storeID, productID, rate);
        return "TO=DO";

    }

    @Override
    public String testUser_SendMessageToStoreOwner(int userId, int ownerId, String msg) throws Exception {
        notificationService.sendDMessageToUser(userId, ownerId, msg);
        return "Done";
    }

    @Override
    public String testUser_SendMessageToAdmin(String msg, int userId, int adminId) throws Exception {
        notificationService.sendDMessageToUser(userId, adminId, msg);
        return "Done";
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token) throws Exception {
        // return purchaseService.getPurchaseHistory(token);
        return "TO=DO";

    }

    @Override
    public String testUser_updateProfile(String token) throws Exception {
        // return userService.updateProfile(token);
        return "TO=DO";

    }

    @Override
    public String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception {
        // return purchaseService.addBid(token, storeID, productID, bid);
        return "TO=DO";

    }

    @Override
    public String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception {
        // return purchaseService.joinAuction(token, storeID, auctionID);
        return "TO=DO";

    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        // return purchaseService.joinRandom(token, storeID, raffleID, num);
        return "TO=DO";

    }

    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        if (userService.setAdmin(token, newAdminUsername))
            return "Done";
        else
            return "false";
    }

    //////////////////////////// Owner ////////////////////////////
    @Override
    public String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount)
            throws Exception {
        return "TO-DO";
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return "TO-DO";

    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID)
            throws Exception {
        return "TO-DO";

    }

    @Override
    public String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        return "TO-DO";

    }

    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return "TO-DO";

    }

    @Override
    public String testOwner_AssignNewOwner(String token, int storeID, int NewOwner) throws Exception {
        storeService.AddOwnershipToStore(storeID, token, NewOwner);
        return "Done";
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, int ownerToRemoveId) throws Exception {
        storeService.DeleteOwnershipFromStore(storeID, token, ownerToRemoveId);
        return "Done";
    }

    // @Override
    // public String testOwner_ResignOwnership(String token, int NewOwner, int
    // storeID) throws Exception {

    // }

    @Override
    public String testOwner_AssignManager(String token, int storeID, int mangerId) throws Exception {
        storeService.AddManagerToStore(storeID, token, mangerId);
        return "Done";
    }

    @Override
    public String testOwner_EditManagerPermissions(String token, int managerId, int storeID,
            List<Permission> autorization) throws Exception {
        storeService.changePermissions(token, managerId, storeID, autorization);
        return "Done";
    }

    @Override
    public String testOwner_RemoveManager(String token, int storeID, int managerId) throws Exception {
        storeService.deleteManager(storeID, token, managerId);
        return "Done";
    }

    @Override
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        storeService.deactivateteStore(storeID, token);
        return "Done";
    }

    @Override
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return "TO-DO";
    }

    @Override
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return "TO-DO";

    }

    @Override
    public String testOwner_ReceiveNotifications(int userId) throws Exception {
        notificationService.getDelayedMessages(userId);
        return "Done";
    }

    @Override
    public String testOwner_ReplyToMessages(String msg, int ownerId, int UserId) throws Exception {
        notificationService.sendRTMessageToUser(msg, ownerId, UserId);
        return "Done";
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return "TO-DO";
    }
    //////////////////////////// Manager ////////////////////////////
    // @Override
    // public String testManager_PerformPermittedActions(String token, int storeID)
    // throws Exception {
    // return "TODO";
    // }

    //////////////////////////// Admin ////////////////////////////

    @Override
    public String testAdmin_CloseStore(int storeID, String token) throws Exception {
        storeService.closeStore(storeID, token);
        return "Done";
    }

    @Override
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return "To-do";

    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return "To-do";

    }

    @Override
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        return "To-do";
    }

}