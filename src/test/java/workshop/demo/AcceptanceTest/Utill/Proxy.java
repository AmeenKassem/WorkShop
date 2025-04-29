package workshop.demo.AcceptanceTest.Utill;

public class Proxy implements Bridge {
    private final Real real = new Real();

    /////////////////////// System /////////////////////////////
    @Override
    public String testSystem_InitMarket(String admin) throws Exception {
        return real.testSystem_InitMarket(admin);
    }

    /////////////////////// Guest /////////////////////////////
    @Override
    public String testGuest_Enter() throws Exception {
        return real.testGuest_Enter();
    }

    @Override
    public String testGuest_Exit(String token) throws Exception {
        return real.testGuest_Exit(token);
    }

    @Override
    public String testGuest_Register(String token, String username, String password, int age) throws Exception {
        return real.testGuest_Register(token, username, password, age);
    }

    @Override
    public String testGuest_GetStoreInfo(String token, int storeID) throws Exception {
        return real.testGuest_GetStoreInfo(token, storeID);
    }

    @Override
    public String testGuest_GetProductInfo(String token, int productID) throws Exception {
        return real.testGuest_GetProductInfo(token, productID);
    }

    @Override
    public String testGuest_SearchProduct(String token, String productname) throws Exception {
        return real.testGuest_SearchProduct(token, productname);
    }

    @Override
    public String testGuest_SearchProductInStore(String token, int storeID, int productID) throws Exception {
        return real.testGuest_SearchProductInStore(token, storeID, productID);
    }

    @Override
    public String testGuest_AddProductToCart(String token, int storeID, int productID, int count) throws Exception {
        return real.testGuest_AddProductToCart(token, storeID, productID, count);
    }

    @Override
    public String testGuest_ModifyCart(String token, int cartID) throws Exception {
        return real.testGuest_ModifyCart(token, cartID);
    }

    @Override
    public String testGuest_BuyCart(String token, int cartID) throws Exception {
        return real.testGuest_BuyCart(token, cartID);
    }

    //////////////////////////// User ////////////////////////////
    @Override
    public String testUser_LogIn(String token, String username, String password) throws Exception {
        return real.testUser_LogIn(token, username, password);
    }

    @Override
    public String testUser_LogOut(String token) throws Exception {
        return real.testUser_LogOut(token);
    }

    @Override
    public String testUser_OpenStore(String token, String storeName, String category) throws Exception {
        return real.testUser_OpenStore(token, storeName, category);
    }

    @Override
    public String testUser_AddReview(String token, int storeId, String review) throws Exception {
        return real.testUser_AddReview(token, storeId, review);
    }

    @Override
    public String testUser_RateProductAndStore(String token, int storeID, int productID, int rate) throws Exception {
        return real.testUser_RateProductAndStore(token, storeID, productID, rate);
    }

    @Override
    public String testUser_SendMessageToStoreOwner(String token, int storeID, String msg) throws Exception {
        return real.testUser_SendMessageToStoreOwner(token, storeID, msg);
    }

    @Override
    public String testUser_SendMessageToAdmin(String token, String msg) throws Exception {
        return real.testUser_SendMessageToAdmin(token, msg);
    }

    @Override
    public String testUser_CheckPurchaseHistory(String token) throws Exception {
        return real.testUser_CheckPurchaseHistory(token);
    }

    @Override
    public String testUser_updateProfile(String token) throws Exception {
        return real.testUser_updateProfile(token);
    }

    @Override
    public String testUser_AddBid(String token, int storeID, int productID, int bid) throws Exception {
        return real.testUser_AddBid(token, storeID, productID, bid);
    }

    @Override
    public String testUser_JoinAuction(String token, int storeID, int auctionID) throws Exception {
        return real.testUser_JoinAuction(token, storeID, auctionID);
    }

    @Override
    public String testUser_JoinRandom(String token, int storeID, int randomID, int num) throws Exception {
        return real.testUser_JoinRandom(token, storeID, randomID, num);
    }

    @Override
    public String testUser_setAdmin(String token, String newAdminUsername) throws Exception {
        return real.testUser_setAdmin(token, newAdminUsername);
    }

    //////////////////////////// Owner ////////////////////////////
    @Override
    public String testOwner_ManageInventory_AddProduct(String token, int storeID, int productID, int amount) throws Exception {
        return real.testOwner_ManageInventory_AddProduct(token, storeID, productID, amount);
    }

    @Override
    public String testOwner_ManageInventory_RemoveProduct(String token, int storeID, int productID) throws Exception {
        return real.testOwner_ManageInventory_RemoveProduct(token, storeID, productID);
    }

    @Override
    public String testOwner_ManageInventory_UpdateProductDetails(String token, int storeID, int productID) throws Exception {
        return real.testOwner_ManageInventory_UpdateProductDetails(token, storeID, productID);
    }

    @Override
    public String testOwner_SetPurchasePolicies(String token, int storeID, String newPolicies) throws Exception {
        return real.testOwner_SetPurchasePolicies(token, storeID, newPolicies);
    }

    @Override
    public String testOwner_SetDiscountPolicies(String token, int storeID, String newPolicies) throws Exception {
        return real.testOwner_SetDiscountPolicies(token, storeID, newPolicies);
    }

    @Override
    public String testOwner_AssignNewOwner(String token, int storeID, String newOwnerUsername) throws Exception {
        return real.testOwner_AssignNewOwner(token, storeID, newOwnerUsername);
    }

    @Override
    public String testOwner_RemoveOwner(String token, int storeID, String ownerToRemoveUsername) throws Exception {
        return real.testOwner_RemoveOwner(token, storeID, ownerToRemoveUsername);
    }

    @Override
    public String testOwner_ResignOwnership(String token, int storeID) throws Exception {
        return real.testOwner_ResignOwnership(token, storeID);
    }

    @Override
    public String testOwner_AssignManager(String token, int storeID, String managerUsername) throws Exception {
        return real.testOwner_AssignManager(token, storeID, managerUsername);
    }

    @Override
    public String testOwner_EditManagerPermissions(String token, int storeID, String managerUsername, String newPermission) throws Exception {
        return real.testOwner_EditManagerPermissions(token, storeID, managerUsername, newPermission);
    }

    @Override
    public String testOwner_RemoveManager(String token, int storeID, String managerUsername) throws Exception {
        return real.testOwner_RemoveManager(token, storeID, managerUsername);
    }

    @Override
    public String testOwner_CloseStore(String token, int storeID) throws Exception {
        return real.testOwner_CloseStore(token, storeID);
    }

    @Override
    public String testOwner_ReopenStore(String token, int storeID) throws Exception {
        return real.testOwner_ReopenStore(token, storeID);
    }

    @Override
    public String testOwner_ViewRolesAndPermissions(String token, int storeID) throws Exception {
        return real.testOwner_ViewRolesAndPermissions(token, storeID);
    }

    @Override
    public String testOwner_ReceiveNotifications(String token) throws Exception {
        return real.testOwner_ReceiveNotifications(token);
    }

    @Override
    public String testOwner_ReplyToMessages(String token) throws Exception {
        return real.testOwner_ReplyToMessages(token);
    }

    @Override
    public String testOwner_ViewStorePurchaseHistory(String token, int storeID) throws Exception {
        return real.testOwner_ViewStorePurchaseHistory(token, storeID);
    }

    //////////////////////////// Manager ////////////////////////////
    // @Override
    // public String testManager_PerformPermittedActions(String token, int storeID) throws Exception {
    //     return real.testManager_PerformPermittedActions(token, storeID);
    // }

    //////////////////////////// Admin ////////////////////////////
    @Override
    public String testAdmin_CloseStore(String token, int storeID) throws Exception {
        return real.testAdmin_CloseStore(token, storeID);
    }

    @Override
    public String testAdmin_RemoveUser(String token, String userToRemove) throws Exception {
        return real.testAdmin_RemoveUser(token, userToRemove);
    }

    @Override
    public String testAdmin_ViewSystemPurchaseHistory(String token) throws Exception {
        return real.testAdmin_ViewSystemPurchaseHistory(token);
    }

    @Override
    public String testAdmin_ViewSystemInfo(String token) throws Exception {
        return real.testAdmin_ViewSystemInfo(token);
    }
}
