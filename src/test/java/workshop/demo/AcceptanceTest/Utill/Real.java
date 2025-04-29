package workshop.demo.AcceptanceTest.Utill;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import workshop.demo.ApplicationLayer.NotificationService;
import workshop.demo.ApplicationLayer.OrderService;
import workshop.demo.ApplicationLayer.PurchaseService;
import workshop.demo.ApplicationLayer.StockService;
import workshop.demo.ApplicationLayer.StoreService;
import workshop.demo.ApplicationLayer.UserService;
import workshop.demo.DomainLayer.Authentication.IAuthRepo;
import workshop.demo.DomainLayer.User.IUserRepo;

public class Real implements Bridge {

    private final UserService userService;
    private final StoreService storeService;
    private final StockService stockService;
    private final OrderService orderService;
    private final PurchaseService purchaseService;
    private final NotificationService notificationService;

    public Real() {
        IUserRepo fakeUserRepo = mock(IUserRepo.class);
        IAuthRepo fakeAuthRepo = mock(IAuthRepo.class);

        when(fakeUserRepo.generateGuest()).thenReturn(123);
        when(fakeAuthRepo.generateGuestToken(123)).thenReturn("guest-token");

        this.userService = mock(UserService.class);
        this.storeService = mock(StoreService.class);
        this.stockService = mock(StockService.class);
        this.orderService = mock(OrderService.class);
        this.purchaseService = mock(PurchaseService.class);
        this.notificationService = mock(NotificationService.class);
    }

    /////////////////////// System /////////////////////////////
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return "TODO";
    }

    /////////////////////// Guest /////////////////////////////
    @Override
    public String testGuest_Enter() throws Exception {
        return userService.generateGuest();
    }

    @Override
    public String testGuest_Exit(String token) throws Exception {
        return userService.destroyGuest(authRepo.getUserId(token));
    }

    @Override
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        return userService.register(token, username, password);
    }

    @Override
    public String testGuest_GetStoreInfo(String token, int storeID) throws Exception {
        return storeService.getStoreInfo(storeID);
    }

    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return stockService.getProductInfo(productID);
    }

    @Override
    public String testGuest_SearchProduct(String token, String productname) throws Exception {
        return stockService.searchProduct(productname);
    }

    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return stockService.searchProductInStore(storeID, productID);
    }

    @Override
    public String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        return stockService.addProductToCart(token, storeID, productID, count);
    }

    @Override
    public String testGuest_ModifyCart(String token, int cartID) throws Exception {
        return stockService.modifyCart(token, cartID);
    }

    @Override
    public String testGuest_BuyCart(String token, int cartID) throws Exception {
        return purchaseService.buyCart(token, cartID);
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
        return storeService.openStore(token, storeName, category);
    }

    @Override
    public String testUser_AddReview(String token, int storeId, String review) throws Exception {
        return storeService.addReview(token, storeId, review);
    }

    @Override
    public String testUser_RateProductAndStore(String token, int storeID, int productID, int rate) throws Exception {
        return stockService.rateProductAndStore(token, storeID, productID, rate);
    }

    @Override
    public String testUser_SendMessageToStoreOwner(String token, int storeID, String msg) throws Exception {
        return notificationService.sendMessageToStoreOwner(token, storeID, msg);
    }

    @Override
    public String testUser_SendMessageToAdmin(String token, String msg) throws Exception {
        return notificationService.sendMessageToAdmin(token, msg);
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token) throws Exception {
        return purchaseService.getPurchaseHistory(token);
    }

    @Override
    public String testUser_updateProfile(String token) throws Exception {
        return userService.updateProfile(token);
    }

    @Override
    public String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception {
        return purchaseService.addBid(token, storeID, productID, bid);
    }

    @Override
    public String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception {
        return purchaseService.joinAuction(token, storeID, auctionID);
    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return purchaseService.joinRandom(token, storeID, raffleID, num);
    }
    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return userService.setUserAdmin(token, newAdminUsername);
    }


    //////////////////////////// Owner ////////////////////////////
    @Override
    public String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount) throws Exception {
        return stockService.addProductToInventory(token, storeID, productID, amount);
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return stockService.removeProductFromInventory(token, storeID, productID);
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID) throws Exception {
        return stockService.updateProductDetails(token, storeID, productID);
    }
    @Override
    public String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        int userId = authRepo.getUserId(token);
        if (!storeService.hasSpecialPermission(userId, storeID)) {
            throw new Exception("User does not have permission to set purchase policies");
        }
        storeService.setPurchasePolicy(userId, storeID, newPolicies);
        return "Purchase policies set successfully.";
    }
    
    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        int userId = authRepo.getUserId(token);
        if (!storeService.hasSpecialPermission(userId, storeID)) {
            throw new Exception("User does not have permission to set discount policies");
        }
        storeService.setDiscountPolicy(userId, storeID, newPolicies);
        return "Discount policies set successfully.";
    }
    
    @Override
    public String testOwner_AssignNewOwner(String token, int storeID, String newOwnerUsername) throws Exception {
        int ownerId = authRepo.getUserId(token);
        int newOwnerId = userService.getUserIdByUsername(newOwnerUsername); 
        return storeService.addOwnershipToStore(storeID, ownerId, newOwnerId);
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, String ownerToRemoveUsername) throws Exception {
        int ownerId = authRepo.getUserId(token);
        int ownerToDeleteId = userService.getUserIdByUsername(ownerToRemoveUsername); 
        return storeService.deleteOwnershipFromStore(storeID, ownerId, ownerToDeleteId);
    }


    @Override
    public String testOwner_ResignOwnership(String token, int storeID) throws Exception {
        int ownerId = authRepo.getUserId(token);
        return storeService.deleteOwnershipFromStore(storeID, ownerId, ownerId);
    }


    @Override
    public String testOwner_AssignManager(String token, int storeID, String managerUsername) throws Exception {
        int ownerId = authRepo.getUserId(token);
        int managerId = userService.getUserIdByUsername(managerUsername);
        return storeService.addManagerToStore(storeID, ownerId, managerId);
    }


    @Override
    public String testOwner_EditManagerPermissions(String token, int storeID, String managerUsername, String newPermission) throws Exception {
        int ownerId = authRepo.getUserId(token);
        int managerId = userService.getUserIdByUsername(managerUsername); 
        List<Permission> permissions = Permission.parsePermissions(newPermission); 
        return storeService.changePermissions(ownerId, managerId, storeID, permissions);
    }
    @Override
    public String testOwner_RemoveManager(String token, int storeID, String managerUsername) throws Exception {
        int ownerId = authRepo.getUserId(token);
        int managerId = userService.getUserIdByUsername(managerUsername);
        return storeService.deleteManager(storeID, token, managerId);
    }
    @Override
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        int ownerId = authRepo.getUserId(token);
        return storeService.closeStore(storeID, token);
    }
    @Override
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        int ownerId = authRepo.getUserId(token);
        return storeService.reopenStore(storeID, ownerId);
    }
    @Override
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        int ownerId = authRepo.getUserId(token);
        return storeService.viewRolesAndPermissions(storeID, ownerId);
    }

    @Override
    public String testOwner_ReceiveNotifications(String token) throws Exception {
        int userId = authRepo.getUserId(token);
        return notificationService.receiveNotifications(userId);
    }
    
    @Override
    public String testOwner_ReplyToMessages(String token) throws Exception {
        int userId = authRepo.getUserId(token);
        return notificationService.replyToMessages(userId);
    }
    @Override
    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        int ownerId = authRepo.getUserId(token);
        return purchaseService.viewStorePurchaseHistory(ownerId, storeID);
    }
        //////////////////////////// Manager ////////////////////////////
    // @Override
    // public String testManager_PerformPermittedActions(String token, int storeID) throws Exception {
    //     return "TODO";
    // }

    //////////////////////////// Admin ////////////////////////////
    
    @Override
    public String testAdmin_CloseStore(String token, int storeID) throws Exception {
        int adminId = authRepo.getUserId(token);
        if (!userService.isAdmin(adminId)) {
            throw new Exception("User is not an admin");
        }
        return storeService.closeStore(storeID);
    }
    
    @Override
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        int adminId = authRepo.getUserId(token);
        if (!userService.isAdmin(adminId)) {
            throw new Exception("User is not an admin");
        }
        return userService.removeUser(userToRemove);
    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        int adminId = authRepo.getUserId(token);
        if (!userService.isAdmin(adminId)) {
            throw new Exception("User is not an admin");
        }
        return purchaseService.getSystemPurchaseHistory();
    }
    
    @Override
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        int adminId = authRepo.getUserId(token);
        if (!userService.isAdmin(adminId)) {
            throw new Exception("User is not an admin");
        }
    
        int numberOfStores = storeService.getStoreCount();
        int numberOfRegisteredUsers = userService.getRegisteredUserCount();
        int numberOfGuests = userService.getGuestCount();
        int numberOfProducts = stockService.getProductCount();
    
        return String.format(
            "System Info:\nStores: %d\nRegistered Users: %d\nGuests: %d\nProducts: %d",
            numberOfStores, numberOfRegisteredUsers, numberOfGuests, numberOfProducts
        );
    }
    // @Override
    // public String testManager_PerformPermittedActions(String token, int storeID) throws Exception {
    //     throw new UnsupportedOperationException("Not supported yet.");
    // }
    
}